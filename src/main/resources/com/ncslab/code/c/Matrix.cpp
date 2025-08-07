#include "Matrix.hpp"


Matrix Matrix::concatenate(const Matrix& m1, const Matrix& m2, int dim) {
    Matrix m3;
    if (dim == 1) {
        m3 = Matrix::Zero(m1.rows() + m2.rows(), m1.cols());
        m3 << m1, m2;
    }
    else if (dim == 2) {
        m3 = Matrix::Zero(m1.rows(), m1.cols() + m2.cols());
        m3 << m1, m2;
    }
    return m3;
}

void Matrix::concatenate(const Matrix& m, int dim) {
    if (dim == 1) {
        *this = Matrix::concatenate(*this, m, 1);
    }
    else if (dim == 2) {
        *this = Matrix::concatenate(*this, m, 2);
    }
}

Matrix Matrix::permuteRow(const Eigen::VectorXi& p) {
    Matrix m(p.size(), this->cols());
    for (int i = 0; i < p.size(); i++) {
        m.row(i) = this->row(p(i) - 1);
    }
    return m;
}

Matrix Matrix::permuteCol(const Eigen::VectorXi& p) {
    Matrix m(this->rows(), p.size());
    for (int i = 0; i < p.size(); i++) {
        m.col(i) = this->col(p(i) - 1);
    }
    return m;
}