#include <iostream>
#include <cmath>
#include <cstdlib>
#include <cstring>

#include "ncslabdefines.hpp"
#include "util.hpp"
#include "mainccode.hpp"
#include "onestep.hpp"

#include <gsl/gsl_errno.h>
#include <gsl/gsl_odeiv2.h>
#include <gsl/gsl_machine.h>

extern MODEL *mp;
extern double sample_time[];
extern double real_sample_time;

// Placeholders required by util.cpp / onestep.hpp linkage
double singleStateReserve[3][SINGLE_STATE_RESERVE_NUM];
Matrix matrixStateReserve[3][MATRIX_STATE_RESERVE_NUM];
double singleDerivativeReserve[7][SINGLE_STATE_RESERVE_NUM];
Matrix matrixDerivativeReserve[7][MATRIX_STATE_RESERVE_NUM];

// GSL workspace
static gsl_odeiv2_system sys;
static gsl_odeiv2_step *step = NULL;
static gsl_odeiv2_control *control = NULL;
static gsl_odeiv2_evolve *evolve = NULL;
static double *gslY = NULL;
static int gslDim = 0;
static double gslStepSize;

#define INIT_POINT_NUM 100
#define TOL 1E-7

static int getStateDimension()
{
    int dim = 0;
    for (int i = 0; i < STATE_NUM; i++) {
        if (mp->states[i]->type == SINGLE) {
            dim += 1;
        } else if (mp->states[i]->type == MATRIX) {
            Matrix *m = (Matrix *)mp->states[i]->vp;
            dim += m->rows() * m->cols();
        }
    }
    return dim;
}

static void statesToGslY(double *dst)
{
    int idx = 0;
    for (int i = 0; i < STATE_NUM; i++) {
        if (mp->states[i]->type == SINGLE) {
            dst[idx++] = *((REAL *)mp->states[i]->vp);
        } else if (mp->states[i]->type == MATRIX) {
            Matrix *m = (Matrix *)mp->states[i]->vp;
            int rows = m->rows();
            int cols = m->cols();
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    dst[idx++] = (*m)(r, c);
                }
            }
        }
    }
}

static void gslYToStates(const double *src)
{
    int idx = 0;
    for (int i = 0; i < STATE_NUM; i++) {
        if (mp->states[i]->type == SINGLE) {
            *((REAL *)mp->states[i]->vp) = src[idx++];
        } else if (mp->states[i]->type == MATRIX) {
            Matrix *m = (Matrix *)mp->states[i]->vp;
            int rows = m->rows();
            int cols = m->cols();
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    (*m)(r, c) = src[idx++];
                }
            }
        }
    }
}

static void dvpToGslF(double *dst)
{
    int idx = 0;
    for (int i = 0; i < STATE_NUM; i++) {
        if (mp->states[i]->type == SINGLE) {
            dst[idx++] = *((REAL *)mp->states[i]->dvp);
        } else if (mp->states[i]->type == MATRIX) {
            Matrix *m = (Matrix *)mp->states[i]->dvp;
            int rows = m->rows();
            int cols = m->cols();
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    dst[idx++] = (*m)(r, c);
                }
            }
        }
    }
}

static int ncsLabFunc(double t, const double y[], double f[], void *params)
{
    double savedTime = mp->time;
    int savedMajorStep = mp->majorStep;
    mp->time = t;
    mp->majorStep = 1;
    gslYToStates(y);
    NCSLabOutput();
    NCSLabDerivative();
    dvpToGslF(f);
    mp->time = savedTime;
    mp->majorStep = savedMajorStep;
    return GSL_SUCCESS;
}

// Numerical Jacobian using forward differences
static int ncsLabJac(double t, const double y[], double *dfdy, double dydt[], void *params)
{
    int status = ncsLabFunc(t, y, dydt, params);
    if (status != GSL_SUCCESS) return status;

    double *y_tmp = (double *)malloc(gslDim * sizeof(double));
    double *f_tmp = (double *)malloc(gslDim * sizeof(double));
    if (!y_tmp || !f_tmp) {
        free(y_tmp);
        free(f_tmp);
        return GSL_ENOMEM;
    }

    memcpy(y_tmp, y, gslDim * sizeof(double));

    for (int i = 0; i < gslDim; i++) {
        double eps = sqrt(GSL_DBL_EPSILON) * (fabs(y[i]) + 1.0);
        if (eps < 1e-8) eps = 1e-8;

        y_tmp[i] = y[i] + eps;

        status = ncsLabFunc(t, y_tmp, f_tmp, params);
        if (status != GSL_SUCCESS) {
            free(y_tmp);
            free(f_tmp);
            return status;
        }

        double denom = eps;
        for (int j = 0; j < gslDim; j++) {
            dfdy[j * gslDim + i] = (f_tmp[j] - dydt[j]) / denom;
        }

        y_tmp[i] = y[i];
    }

    gslYToStates(y);

    free(y_tmp);
    free(f_tmp);
    return GSL_SUCCESS;
}

#ifdef _SIMU
void ncslabLoop()
{
    gslDim = getStateDimension();

    if (gslDim > 0) {
        gslY = (double *)malloc(gslDim * sizeof(double));
        statesToGslY(gslY);

        sys.function = ncsLabFunc;
        sys.jacobian = ncsLabJac;
        sys.dimension = gslDim;
        sys.params = NULL;

        step = gsl_odeiv2_step_alloc(gsl_odeiv2_step_rk2imp, gslDim);
        if (!step) {
            fprintf(stderr, "GSL step allocation failed\n");
            return;
        }
        control = gsl_odeiv2_control_standard_new(1e-6, 1e-6, 1.0, 0.0);
        if (!control) {
            fprintf(stderr, "GSL control allocation failed\n");
            gsl_odeiv2_step_free(step);
            return;
        }
        evolve = gsl_odeiv2_evolve_alloc(gslDim);
        if (!evolve) {
            fprintf(stderr, "GSL evolve allocation failed\n");
            gsl_odeiv2_control_free(control);
            gsl_odeiv2_step_free(step);
            return;
        }

        gslStepSize = (mp->stopTime - mp->startTime) / INIT_POINT_NUM;
    } else {
        gslStepSize = (mp->stopTime - mp->startTime) / INIT_POINT_NUM;
    }

    discreteInit();

    while (mp->time < mp->stopTime) {
        writeInformation();
        NCSLabOneStep();
    }

    if (evolve) { gsl_odeiv2_evolve_free(evolve); evolve = NULL; }
    if (control) { gsl_odeiv2_control_free(control); control = NULL; }
    if (step) { gsl_odeiv2_step_free(step); step = NULL; }
    if (gslY) { free(gslY); gslY = NULL; }
}
#endif


void ncslabLoopRealtime()
{
gslDim = getStateDimension();

    if (gslDim > 0) {
        gslY = (double *)malloc(gslDim * sizeof(double));
        statesToGslY(gslY);

        sys.function = ncsLabFunc;
        sys.jacobian = ncsLabJac;
        sys.dimension = gslDim;
        sys.params = NULL;

        step = gsl_odeiv2_step_alloc(gsl_odeiv2_step_rk2imp, gslDim);
        if (!step) {
            fprintf(stderr, "GSL step allocation failed\n");
            return;
        }
        control = gsl_odeiv2_control_standard_new(1e-6, 1e-6, 1.0, 0.0);
        if (!control) {
            fprintf(stderr, "GSL control allocation failed\n");
            gsl_odeiv2_step_free(step);
            return;
        }
        evolve = gsl_odeiv2_evolve_alloc(gslDim);
        if (!evolve) {
            fprintf(stderr, "GSL evolve allocation failed\n");
            gsl_odeiv2_control_free(control);
            gsl_odeiv2_step_free(step);
            return;
        }

        gslStepSize = (mp->stopTime - mp->startTime) / INIT_POINT_NUM;
    } else {
        gslStepSize = (mp->stopTime - mp->startTime) / INIT_POINT_NUM;
    }

    discreteInit();
  NCSLabOutput();
  NCSLabSinkOutput();
  sendDisplayUpdateForce();

#ifdef _WIN32
  DWORD startTick = GetTickCount();
  DWORD lastSendTick = startTick;
#else
  struct timeval startTv;
  gettimeofday(&startTv, NULL);
  long long startMs = startTv.tv_sec * 1000LL + startTv.tv_usec / 1000;
  long long lastSendMs = startMs;
#endif

  
  int realtimeUpdateMs = ncsRealtimeNativeIntervalMs();
  if (realtimeUpdateMs < 50) {
    realtimeUpdateMs = 50;
  }
while (mp->time < mp->stopTime) {
        // Pause/resume control
        while (g_simulationPaused) {
#ifdef _WIN32
          DWORD pauseTick = GetTickCount();
          Sleep(10);
          DWORD pauseDuration = GetTickCount() - pauseTick;
          startTick += pauseDuration;
          lastSendTick += pauseDuration;
#else
          struct timeval pauseTv;
          gettimeofday(&pauseTv, NULL);
          usleep(10000);
          struct timeval pauseEndTv;
          gettimeofday(&pauseEndTv, NULL);
          long long pauseDurationMs = (pauseEndTv.tv_sec * 1000LL + pauseEndTv.tv_usec / 1000) - (pauseTv.tv_sec * 1000LL + pauseTv.tv_usec / 1000);
          startMs += pauseDurationMs;
          lastSendMs += pauseDurationMs;
#endif
          checkParameterUpdates();
        }
    NCSLabOneStep();
    
    bool shouldSend = false;
#ifdef _WIN32
    DWORD nowTick = GetTickCount();
    if ((int)(nowTick - lastSendTick) >= realtimeUpdateMs) {
      shouldSend = true;
    }
#else
    struct timeval nowTv;
    gettimeofday(&nowTv, NULL);
    long long nowMs = nowTv.tv_sec * 1000LL + nowTv.tv_usec / 1000;
    if ((int)(nowMs - lastSendMs) >= realtimeUpdateMs) {
      shouldSend = true;
    }
#endif

    if (shouldSend) {
      checkParameterUpdates();
      sendRealtimeDataUpdate();
      #ifdef _WIN32
      lastSendTick = nowTick;
#else
      lastSendMs = nowMs;
#endif
    }

    double simulatedElapsed = mp->time - mp->startTime;
#ifdef _WIN32
    DWORD realElapsedMs = GetTickCount() - startTick;
    double realElapsedSec = realElapsedMs / 1000.0;
    if (simulatedElapsed > realElapsedSec) {
      DWORD sleepMs = (DWORD)((simulatedElapsed - realElapsedSec) * 1000);
      while (sleepMs > 0) {
        DWORD chunk = sleepMs > 100 ? 100 : sleepMs;
        Sleep(chunk);
        sleepMs -= chunk;
        DWORD nowTick = GetTickCount();
        if ((int)(nowTick - lastSendTick) >= realtimeUpdateMs) {
          checkParameterUpdates();
          sendRealtimeDataUpdate();
          lastSendTick = nowTick;
        }
      }
    }
#else
    struct timeval syncTv;
    gettimeofday(&syncTv, NULL);
    long long realElapsedMs = (syncTv.tv_sec * 1000LL + syncTv.tv_usec / 1000) - startMs;
    double realElapsedSec = realElapsedMs / 1000.0;
    if (simulatedElapsed > realElapsedSec) {
      long sleepUs = (long)((simulatedElapsed - realElapsedSec) * 1000000);
      while (sleepUs > 0) {
        long chunk = sleepUs > 100000 ? 100000 : sleepUs;
        usleep(chunk);
        sleepUs -= chunk;
        struct timeval nowTv;
        gettimeofday(&nowTv, NULL);
        long long nowMs = nowTv.tv_sec * 1000LL + nowTv.tv_usec / 1000;
        if ((int)(nowMs - lastSendMs) >= realtimeUpdateMs) {
          checkParameterUpdates();
          sendRealtimeDataUpdate();
          lastSendMs = nowMs;
        }
      }
    }
#endif
  }

if (evolve) { gsl_odeiv2_evolve_free(evolve); evolve = NULL; }
    if (control) { gsl_odeiv2_control_free(control); control = NULL; }
    if (step) { gsl_odeiv2_step_free(step); step = NULL; }
    if (gslY) { free(gslY); gslY = NULL; }
}
#endif
}
void NCSLabOneStep()
{
    mp->offset = 0;
    mp->majorStep = 1;
    NCSLabOutput();

    if (mp->discreteUpdate) {
        NCSLabDiscreteUpdate();
        mp->discreteUpdate = 0;
    }
    NCSLabSinkOutput();
    mp->majorStep = 0;

    if (gslDim > 0) {
        double t = mp->time;
        double t1 = mp->stopTime;

        if (hasdiscrete(sample_time)) {
            while (mp->discreteTime - t <= TOL) {
                mp->discreteTime += real_sample_time;
                mp->discreteUpdate = 1;
            }
            double dist = mp->discreteTime - t;
            if (gslStepSize > dist) {
                gslStepSize = dist;
            }
        }

        double maxStep = (mp->stopTime - mp->startTime) / INIT_POINT_NUM;
        if (gslStepSize > maxStep) gslStepSize = maxStep;
        if (gslStepSize < 1e-12) gslStepSize = 1e-12;

        int status = gsl_odeiv2_evolve_apply(evolve, control, step, &sys, &t, t1, &gslStepSize, gslY);

        if (status != GSL_SUCCESS) {
            fprintf(stderr, "GSL evolve_apply failed: %s\n", gsl_strerror(status));
            mp->time = mp->stopTime;
        } else {
            mp->time = t;
            gslYToStates(gslY);
        }
    } else {
        mp->time += gslStepSize;
        if (mp->time > mp->stopTime) {
            mp->time = mp->stopTime;
        }

        if (hasdiscrete(sample_time)) {
            while (mp->discreteTime - mp->time <= TOL) {
                mp->discreteTime += real_sample_time;
                mp->discreteUpdate = 1;
            }
            double dist = mp->discreteTime - mp->time;
            if (gslStepSize > dist) {
                gslStepSize = dist;
            }
        }
    }
}

