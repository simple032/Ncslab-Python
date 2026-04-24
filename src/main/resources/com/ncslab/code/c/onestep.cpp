#include <iostream>
#include <cmath>

#include <gsl/gsl_errno.h>
#include <gsl/gsl_matrix.h>
#include <gsl/gsl_odeiv2.h>
#include <gsl/gsl_math.h>

#include "ncslabdefines.hpp"
#include "util.hpp"
#include "mainccode.hpp"
#include "onestep.hpp"

#ifdef _WIN32
#include <windows.h>
#else
#include <unistd.h>
#include <sys/time.h>
#endif

#define INIT_POINT_NUM 1000
#define TOL 1E-4
#define REL_TOL 1E-3
#define MAX_RETRIES 5  // 增加最大重试次数

extern MODEL *mp;


double stepSize;
double maxStepSize;

extern double real_sample_time;

// 兼容旧版本，已经不适用了
double singleStateReserve[7][SINGLE_STATE_NUM];
Matrix matrixStateReserve[7][MATRIX_STATE_NUM];

double singleDerivativeReserve[7][SINGLE_STATE_NUM];
Matrix matrixDerivativeReserve[7][MATRIX_STATE_NUM];

// GSL ODE function that wraps NCSLabDerivative
int ncslab_ode_function(double t, const double y[], double dydt[], void *params) {
  // Copy state from GSL to our system
  MODEL *model = (MODEL *)params;
  model->offset = t - model->time;

  int index = 0;
  int singleIndex = 0;
  int matrixIndex = 0;

  for (int i = 0; i < STATE_NUM; i++) {
      if (model->states[i]->type == SINGLE) {
          *((REAL*)(model->states[i]->vp)) = y[index++];
      }else if(model->states[i]->type == MATRIX){
        Matrix *mat = (Matrix*)(model->states[i]->vp);
        int rows = mat->rows();
        int cols = mat->cols();

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
              (*mat)(r, c) = y[index++];
            }
        }
        matrixIndex++;
      }
  }

  // Calculate derivatives
  NCSLabOutput();
  NCSLabDerivative();

  // Copy derivatives back to GSL
  index = 0;
  singleIndex = 0;
  matrixIndex = 0;

  for (int i = 0; i < STATE_NUM; i++) {
      if (model->states[i]->type == SINGLE) {
          dydt[index++] = *((REAL*)(model->states[i]->dvp));
          singleIndex++;
      } else if (model->states[i]->type == MATRIX) {
        Matrix* mat = (Matrix*)(model->states[i]->dvp);
        int rows = mat->rows();
        int cols = mat->cols();

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                dydt[index++] = (*mat)(r, c);
            }
        }
        matrixIndex++;
      }
  }

  return GSL_SUCCESS;
}


void ncslabLoop() {
  maxStepSize = stepSize = (mp->stopTime - mp->startTime) / INIT_POINT_NUM;

  discreteInit();
  NCSLabOutput();
  NCSLabSinkOutput();

  while (mp->time < mp->stopTime) {
    //  writeInformation();
    NCSLabOneStep();
  }
}

void ncslabLoopRealtime() {
  maxStepSize = stepSize = (mp->stopTime - mp->startTime) / INIT_POINT_NUM;

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

  while (mp->time < mp->stopTime) {
    NCSLabOneStep();
    
    bool shouldSend = false;
#ifdef _WIN32
    DWORD nowTick = GetTickCount();
    if ((int)(nowTick - lastSendTick) >= 1000) {
      shouldSend = true;
    }
#else
    struct timeval nowTv;
    gettimeofday(&nowTv, NULL);
    long long nowMs = nowTv.tv_sec * 1000LL + nowTv.tv_usec / 1000;
    if ((int)(nowMs - lastSendMs) >= 1000) {
      shouldSend = true;
    }
#endif

    if (shouldSend) {
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
      if (sleepMs > 0) Sleep(sleepMs);
    }
#else
    struct timeval syncTv;
    gettimeofday(&syncTv, NULL);
    long long realElapsedMs = (syncTv.tv_sec * 1000LL + syncTv.tv_usec / 1000) - startMs;
    double realElapsedSec = realElapsedMs / 1000.0;
    if (simulatedElapsed > realElapsedSec) {
      long sleepUs = (long)((simulatedElapsed - realElapsedSec) * 1000000);
      if (sleepUs > 0) usleep(sleepUs);
    }
#endif
  }
}



void NCSLabOneStep() {
  mp->offset = 0;
  mp->majorStep = 1;
  // NCSLabOutput();
  // NCSLabSinkOutput();

  if (mp->discreteUpdate) {
    NCSLabDiscreteUpdate();
    mp->discreteUpdate = 0;
  }

  // Determine total state vector size (accounting for matrices)
  size_t totalStateSize = 0;
  for (int i = 0; i < STATE_NUM; i++) {
    if (mp->states[i]->type == SINGLE) {
      totalStateSize++;
    } else if (mp->states[i]->type == MATRIX) {
      Matrix* mat = (Matrix*)(mp->states[i]->vp);
      totalStateSize += mat->rows() * mat->cols();
    }
  }

  // Initialize state vector
  double *y = new double[totalStateSize];

  // Copy current state to y
  int index = 0;
  for (int i = 0; i < STATE_NUM; i++) {
    if (mp->states[i]->type == SINGLE) {
      y[index++] = *((REAL*)(mp->states[i]->vp));
    } else if (mp->states[i]->type == MATRIX) {
      Matrix* mat = (Matrix*)(mp->states[i]->vp);
      int rows = mat->rows();
      int cols = mat->cols();

      for (int r = 0; r < rows; r++) {
        for (int c = 0; c < cols; c++) {
          y[index++] = (*mat)(r, c);
        }
      }
    }
  }

  // Setup GSL system
  gsl_odeiv2_system sys = {ncslab_ode_function, NULL, totalStateSize, mp};

  // Determine the target time
  double currentTime = mp->time;
  double targetTime = mp->time + stepSize;

  // Make sure we don't go past the stop time
  if (targetTime > mp->stopTime) {
    targetTime = mp->stopTime;
  }

  // Set up adaptive step size control
  double abs_tol = TOL;                    // Absolute error tolerance
  double rel_tol = REL_TOL;                    // Relative error tolerance

  // Set up the stepper and control objects
  const gsl_odeiv2_step_type *stepType = gsl_odeiv2_step_rkf45;  // RKF45 method (like MATLAB's ode45)
  gsl_odeiv2_step *step = gsl_odeiv2_step_alloc(stepType, totalStateSize);
  gsl_odeiv2_control *control = gsl_odeiv2_control_y_new(TOL, REL_TOL);
  gsl_odeiv2_evolve *evolve = gsl_odeiv2_evolve_alloc(totalStateSize);

  double h = stepSize / 10.0;  // Initial step size
  double t = currentTime;      // Current time
  int status = GSL_SUCCESS;

  while (t < targetTime) {
    double h_old = h; // Save current step size

    // Apply one evolution step
    status = gsl_odeiv2_evolve_apply(evolve, control, step, &sys, &t, targetTime, &h, y);

    if (status != GSL_SUCCESS) {
      std::cerr << "Error: " << gsl_strerror(status) << std::endl;
      break;
    }

     // Print intermediate step information
     mp->time = t;
     NCSLabOutput();
     NCSLabSinkOutput();

    // Break if we've reached the target time
    if (fabs(t - targetTime) < TOL) {
      break;
    }
  }

  // Update the system state with the integration results
  index = 0;
  for (int i = 0; i < STATE_NUM; i++) {
    if (mp->states[i]->type == SINGLE) {
      *((REAL*)(mp->states[i]->vp)) = y[index++];
    } else if (mp->states[i]->type == MATRIX) {
      Matrix* mat = (Matrix*)(mp->states[i]->vp);
      int rows = mat->rows();
      int cols = mat->cols();

      for (int r = 0; r < rows; r++) {
        for (int c = 0; c < cols; c++) {
          (*mat)(r, c) = y[index++];
        }
      }
    }
  }

  mp->time = t;

   // Free GSL resources
   gsl_odeiv2_evolve_free(evolve);
   gsl_odeiv2_control_free(control);
   gsl_odeiv2_step_free(step);
   delete[] y;


  // Handle discrete events
  if (hasdiscrete(sample_time)) {
    while (mp->discreteTime - mp->time <= TOL) {
      mp->discreteTime += real_sample_time;
      mp->discreteUpdate = 1;
    }
  }
}
