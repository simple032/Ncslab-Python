#include <iostream>
#include <cmath>
#include "ricatti.hpp"

// Ricatti equation solver
// P is the solution of the Ricatti equation
Matrix ricatti(const Matrix& A, const Matrix& B, const Matrix& Q, const Matrix& R, 
    const double dt, const double tolerance, const uint maxIter)
{
    Matrix P = Q;
    Matrix P_next;

    Matrix AT = A.transpose();
    Matrix BT = B.transpose();
    Matrix Rinv = R.inv();

    double diff;
    for (uint i = 0; i < maxIter; ++i) {
        P_next = P + (P * A + AT * P - P * B * Rinv * BT * P + Q) * dt;
        diff = fabs((P_next - P).maxCoeff());
        P = P_next;
        if (diff < tolerance) {
            std::cout << "iteration mumber = " << i << std::endl;
            return P;
        }
    }
    return P;
}

Matrix lqr(const Matrix& A, const Matrix& B, const Matrix& Q, const Matrix& R)
{
    Matrix P = ricatti(A, B, Q, R);
    Matrix K = R.inv() * B.transpose() * P;
    return K;
}