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
double singleStateReserve[3][SINGLE_STATE_NUM];
Matrix matrixStateReserve[3][MATRIX_STATE_NUM];
double singleDerivativeReserve[7][SINGLE_STATE_NUM];
Matrix matrixDerivativeReserve[7][MATRIX_STATE_NUM];

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
        // We leave sys.jacobian = NULL because:
        // 1. MinGW GSL has a platform-level invalid-pointer bug with rk4imp/msbdf.
        // 2. A user-supplied numerical Jacobian (ncsLabJac) calls NCSLabOutput(),
        //    which updates discrete state as a side-effect and corrupts the model.
        // bsimp with internal numerical Jacobian works around both issues.
        sys.dimension = gslDim;
        sys.params = NULL;

        step = gsl_odeiv2_step_alloc(gsl_odeiv2_step_bsimp, gslDim);
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
