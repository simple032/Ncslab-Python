#include <iostream>
#include <cmath>
#include <cstdlib>

#include "ncslabdefines.hpp"
#include "util.hpp"
#include "mainccode.hpp"
#include "onestep.hpp"

#include <gsl/gsl_errno.h>
#include <gsl/gsl_odeiv2.h>

extern MODEL *mp;
extern double sample_time[];
extern double real_sample_time;

// Required by util.cpp / onestep.hpp even though GSL ode113 does not use them
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

// Helper: compute total flattened dimension of all continuous states
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

// Helper: copy mp->states values -> gslY
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

// Helper: copy gslY -> mp->states values
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

// Helper: copy mp->states derivatives -> GSL f vector
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

// GSL system function
static int ncsLabFunc(double t, const double y[], double f[], void *params)
{
    double savedTime = mp->time;
    mp->time = t;
    gslYToStates(y);
    NCSLabOutput();
    NCSLabDerivative();
    dvpToGslF(f);
    mp->time = savedTime;
    return GSL_SUCCESS;
}

void ncslabLoop()
{
    gslDim = getStateDimension();

    if (gslDim > 0) {
        gslY = (double *)malloc(gslDim * sizeof(double));
        statesToGslY(gslY);

        sys.function = ncsLabFunc;
        sys.jacobian = NULL;
        sys.dimension = gslDim;
        sys.params = NULL;

        step = gsl_odeiv2_step_alloc(gsl_odeiv2_step_msadams, gslDim);
        control = gsl_odeiv2_control_standard_new(1e-6, 1e-6, 1.0, 0.0);
        evolve = gsl_odeiv2_evolve_alloc(gslDim);

        gslStepSize = (mp->stopTime - mp->startTime) / INIT_POINT_NUM;
    } else {
        // No continuous states: use a dummy small step to keep loop alive
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

void NCSLabOneStep()
{
    // Major step processing at current time
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

        // Limit step size to not cross discrete sample points
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

        // Enforce a maximum step size
        double maxStep = (mp->stopTime - mp->startTime) / INIT_POINT_NUM;
        if (gslStepSize > maxStep) {
            gslStepSize = maxStep;
        }
        if (gslStepSize < 1e-12) {
            gslStepSize = 1e-12;
        }

        int status = gsl_odeiv2_evolve_apply(evolve, control, step, &sys, &t, t1, &gslStepSize, gslY);

        if (status != GSL_SUCCESS) {
            fprintf(stderr, "GSL evolve_apply failed: %s\n", gsl_strerror(status));
            // Emergency fallback: advance by minimum step to avoid deadlock
            t += 1e-9;
        }

        mp->time = t;
        gslYToStates(gslY);
    } else {
        // No continuous states: simple fixed-step fallback
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
