#ifndef RICATTI_HPP
#define RICATTI_HPP

#include "Matrix.hpp"

using uint = unsigned int;

Matrix ricatti(const Matrix& A, const Matrix& B, const Matrix& Q, const Matrix& R, 
    const double dt=0.001, const double tolerance=1.E-5, const uint maxIter=100000);

Matrix lqr(const Matrix& A, const Matrix& B, const Matrix& Q, const Matrix& R);

#endif
