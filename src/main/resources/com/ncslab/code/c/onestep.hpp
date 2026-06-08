#ifndef ONESTEP_HPP
#define ONESTEP_HPP

#include "Matrix.hpp"
#include "ncslab.hpp"
#include "mainccode.hpp"

#ifndef SINGLE_STATE_RESERVE_NUM
#if SINGLE_STATE_NUM > 0
#define SINGLE_STATE_RESERVE_NUM SINGLE_STATE_NUM
#else
#define SINGLE_STATE_RESERVE_NUM 1
#endif
#endif

extern double singleStateReserve[][SINGLE_STATE_RESERVE_NUM];

#ifndef MATRIX_STATE_RESERVE_NUM
#if MATRIX_STATE_NUM > 0
#define MATRIX_STATE_RESERVE_NUM MATRIX_STATE_NUM
#else
#define MATRIX_STATE_RESERVE_NUM 1
#endif
#endif

extern Matrix matrixStateReserve[][MATRIX_STATE_RESERVE_NUM];

extern double singleDerivativeReserve[][SINGLE_STATE_RESERVE_NUM];
extern Matrix matrixDerivativeReserve[][MATRIX_STATE_RESERVE_NUM];

void ncslabLoop();
void ncslabLoopRealtime();

#endif
