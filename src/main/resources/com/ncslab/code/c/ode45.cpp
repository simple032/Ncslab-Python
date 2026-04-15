#include <iostream>
#include <cmath>

#include "ncslabdefines.hpp"
#include "util.hpp"
#include "mainccode.hpp"
#include "onestep.hpp"

#define INIT_POINT_NUM 100
#define TOL 1E-7

extern MODEL *mp;
extern double sample_time[];
double singleStateReserve[3][SINGLE_STATE_NUM];
Matrix matrixStateReserve[3][MATRIX_STATE_NUM];

// Dormand-Prince 5(4) needs 7 derivative slots (K1..K7)
double singleDerivativeReserve[7][SINGLE_STATE_NUM];
Matrix matrixDerivativeReserve[7][MATRIX_STATE_NUM];

// Dormand-Prince 5(4) coefficients
const double DP_C2 = 1.0 / 5.0;
const double DP_C3 = 3.0 / 10.0;
const double DP_C4 = 4.0 / 5.0;
const double DP_C5 = 8.0 / 9.0;
const double DP_C6 = 1.0;

// a_ij weights for constructing intermediate stages
const double DP_A21 = 1.0 / 5.0;

const double DP_A31 = 3.0 / 40.0;
const double DP_A32 = 9.0 / 40.0;

const double DP_A41 = 44.0 / 45.0;
const double DP_A42 = -56.0 / 15.0;
const double DP_A43 = 32.0 / 9.0;

const double DP_A51 = 19372.0 / 6561.0;
const double DP_A52 = -25360.0 / 2187.0;
const double DP_A53 = 64448.0 / 6561.0;
const double DP_A54 = -212.0 / 729.0;

const double DP_A61 = 9017.0 / 3168.0;
const double DP_A62 = -355.0 / 33.0;
const double DP_A63 = 46732.0 / 5247.0;
const double DP_A64 = 49.0 / 176.0;
const double DP_A65 = -5103.0 / 18656.0;

// 5th order weights (local extrapolation - actual step)
const double DP_B1 = 35.0 / 384.0;
const double DP_B2 = 0.0;
const double DP_B3 = 500.0 / 1113.0;
const double DP_B4 = 125.0 / 192.0;
const double DP_B5 = -2187.0 / 6784.0;
const double DP_B6 = 11.0 / 84.0;
const double DP_B7 = 0.0;

// 4th order weights (error estimation only)
const double DP_BS1 = 5179.0 / 57600.0;
const double DP_BS2 = 0.0;
const double DP_BS3 = 7571.0 / 16695.0;
const double DP_BS4 = 393.0 / 640.0;
const double DP_BS5 = -92097.0 / 339200.0;
const double DP_BS6 = 187.0 / 2100.0;
const double DP_BS7 = 1.0 / 40.0;

double stepSize;
double nextStepSize;
double maxStepSize;
double prevErrorNorm = 1.0;

extern double real_sample_time;

static void buildStage(double a1, double a2, double a3, double a4, double a5, int numPrev)
{
    double w[5];
    w[0] = a1; w[1] = a2; w[2] = a3; w[3] = a4; w[4] = a5;
    caculateDerivative(w, numPrev);
    NCSLabUpdate();
}

static void copyDerivativeSlot(int from, int to)
{
    for (int i = 0; i < SINGLE_STATE_NUM; i++) {
        singleDerivativeReserve[to][i] = singleDerivativeReserve[from][i];
    }
    for (int i = 0; i < MATRIX_STATE_NUM; i++) {
        matrixDerivativeReserve[to][i] = matrixDerivativeReserve[from][i];
    }
}

void ncslabLoop()
{
    maxStepSize = stepSize = (mp->stopTime - mp->startTime) / INIT_POINT_NUM;
    discreteInit();

    while (mp->time < mp->stopTime)
    {
        writeInformation();
        NCSLabOneStep();
    }
}

void NCSLabOneStep()
{
    REAL dif;
    int accepted = 0;
    int isFirstStep = (fabs(mp->time - mp->startTime) < 1e-12);

    // Major step output at t_n
    mp->offset = 0;
    mp->majorStep = 1;
    NCSLabOutput();

    if (mp->discreteUpdate) {
        NCSLabDiscreteUpdate();
        mp->discreteUpdate = 0;
    }
    NCSLabSinkOutput();

    // Compute K1 for the first step; FSAL reuses derivativeReserve[0] for subsequent steps
    if (isFirstStep) {
        NCSLabDerivative();
        storeDerivative(0);
    }

    mp->majorStep = 0;

    while (!accepted)
    {
        storeState(0);  // backup y_n

        // ---- Stage 2 ----
        restoreState(0);
        mp->stepSize = stepSize;
        buildStage(DP_A21, 0.0, 0.0, 0.0, 0.0, 1);
        mp->offset = stepSize * DP_C2;
        NCSLabOutput();
        NCSLabDerivative();
        storeDerivative(1);

        // ---- Stage 3 ----
        restoreState(0);
        mp->stepSize = stepSize;
        buildStage(DP_A31, DP_A32, 0.0, 0.0, 0.0, 2);
        mp->offset = stepSize * DP_C3;
        NCSLabOutput();
        NCSLabDerivative();
        storeDerivative(2);

        // ---- Stage 4 ----
        restoreState(0);
        mp->stepSize = stepSize;
        buildStage(DP_A41, DP_A42, DP_A43, 0.0, 0.0, 3);
        mp->offset = stepSize * DP_C4;
        NCSLabOutput();
        NCSLabDerivative();
        storeDerivative(3);

        // ---- Stage 5 ----
        restoreState(0);
        mp->stepSize = stepSize;
        buildStage(DP_A51, DP_A52, DP_A53, DP_A54, 0.0, 4);
        mp->offset = stepSize * DP_C5;
        NCSLabOutput();
        NCSLabDerivative();
        storeDerivative(4);

        // ---- Stage 6 ----
        restoreState(0);
        mp->stepSize = stepSize;
        buildStage(DP_A61, DP_A62, DP_A63, DP_A64, DP_A65, 5);
        mp->offset = stepSize * DP_C6;
        NCSLabOutput();
        NCSLabDerivative();
        storeDerivative(5);

        // ---- 5th order estimate (local extrapolation, actual step) ----
        restoreState(0);
        mp->stepSize = stepSize;
        double w5[7] = { DP_B1, DP_B2, DP_B3, DP_B4, DP_B5, DP_B6, DP_B7 };
        caculateDerivative(w5, 6);  // K1..K6 (b7=0, K7 not needed)
        NCSLabUpdate();
        storeState(2);  // y_5th

        // ---- Compute K7 = f(t_{n+1}, y5) for error estimation and FSAL ----
        restoreState(2);
        mp->offset = stepSize;
        NCSLabOutput();
        NCSLabDerivative();
        storeDerivative(6);  // K7

        // ---- 4th order estimate (for error control only) ----
        restoreState(0);
        mp->stepSize = stepSize;
        double w4[7] = { DP_BS1, DP_BS2, DP_BS3, DP_BS4, DP_BS5, DP_BS6, DP_BS7 };
        caculateDerivative(w4, 7);  // K1..K7
        NCSLabUpdate();
        storeState(1);  // y_4th

        // ---- Error estimate ----
        dif = calculateStateDif(1, 2);

        // ---- Step size control (PI controller) ----
        double factor = 1.0;
        if (dif > 1e-15) {
            double errorNorm = dif / TOL;
            double pFactor = pow(1.0 / errorNorm, 0.2);
            double piFactor = pFactor * pow(errorNorm / prevErrorNorm, 0.08);
            factor = 0.9 * piFactor;

            if (factor > 5.0) factor = 5.0;
            if (factor < 0.2) factor = 0.2;
            prevErrorNorm = errorNorm;
        } else {
            factor = 5.0;
            prevErrorNorm = 1.0;
        }

        nextStepSize = stepSize * factor;
        if (nextStepSize > maxStepSize) nextStepSize = maxStepSize;
        if (nextStepSize < 1e-12) nextStepSize = 1e-12;

        // ---- Accept/reject step ----
        if (dif <= TOL) {
            // ACCEPT: y5 is already current state from K7 computation above
            mp->time += stepSize;

            // FSAL: K7 becomes next step's K1
            copyDerivativeSlot(6, 0);

            // Adjust next step for discrete sample hits
            if (hasdiscrete(sample_time)) {
                while (mp->discreteTime - mp->time <= TOL) {
                    mp->discreteTime += real_sample_time;
                    mp->discreteUpdate = 1;
                }
                double dist = mp->discreteTime - mp->time;
                if (nextStepSize > dist) {
                    nextStepSize = dist;
                }
            }

            stepSize = nextStepSize;
            accepted = 1;
        } else {
            // REJECT: restore y_n and retry with smaller step
            restoreState(0);
            mp->offset = 0.0;
            NCSLabOutput();
            stepSize = nextStepSize;
            // K1 in derivativeReserve[0] remains valid for retry
        }
    }
}
