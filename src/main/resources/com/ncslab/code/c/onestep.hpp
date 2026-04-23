#ifndef ONESTEP_HPP
#define ONESTEP_HPP

#include "Matrix.hpp"
#include "ncslab.hpp"
#include "mainccode.hpp"

extern double singleStateReserve[][SINGLE_STATE_NUM];
extern Matrix matrixStateReserve[][MATRIX_STATE_NUM];

extern double singleDerivativeReserve[][SINGLE_STATE_NUM];
extern Matrix matrixDerivativeReserve[][MATRIX_STATE_NUM];

void ncslabLoop();
void ncslabLoopRealtime();

#endif
