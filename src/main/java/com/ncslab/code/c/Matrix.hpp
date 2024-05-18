#ifndef MATRIX_HPP
#define MATRIX_HPP

#include <iostream>
#include <Eigen/Dense>

/**
 * @brief Extend Eigen::MatrixXd to add some useful functions
 * 
 * @example
 * row(index): get the index row 
 * col(index): get the index column
 * rows(): get the number of rows
 * maxCoeff(): get the max element of a matrix
 * 
 * @attention
 * Be careful about aliasing  
 * e.g. a = a.transpose() is wrong
 * Please us a = a.transposeInPlace() instead
 */
class Matrix : public Eigen::MatrixXd {
public:
    using Eigen::MatrixXd::MatrixXd;  // Inherit constructors

    /**
     * @brief concatenate two eigen matrix
     *
     * @param m1 matrix 1
     * @param m2 matrix 2
     * @param dim concatenate dimension
     */
    static Matrix concatenate(const Matrix& m1, const Matrix& m2, int dim);

    // concatenate a matrix to the current matrix
    void concatenate(const Matrix& m, int dim);

    /**
     * @brief permute rows of a matrix according to a vector
     * one-based index
     *
     * @param p permutation vector
     * @return Matrix
     */
    Matrix permuteRow(const Eigen::VectorXi& p);

    /**
     * @brief permute columns of a matrix according to a vector
     * one-based index
     *
     * @param p permutation vector
     * @return Matrix
     */
    Matrix permuteCol(const Eigen::VectorXi& p);

    // judge if a matrix is symmetric
    bool isSymmetric() {
        return this->isApprox(this->transpose());
    }

    // judege if a matrix is triangular
    bool isTriangular() {
        return this->isUpperTriangular() || this->isLowerTriangular();
    }

    // following code just for compatibility

    void set(int row, int col, double value) {
        this->operator()(row, col) = value;
    }

    // for compatibility: get the determinant of a matrix
    double det() const {
        return this->determinant();
    }

    // for compatibility: get the inverse of a matrix
    Matrix inv() const {
        return this->inverse();
    }

    // for compatibility: get the diagonal of a matrix
    Matrix diag() const {
        return this->diagonal();
    }

    // for compatibility: create a identity matrix
    static Matrix identity(int n) {
        return Matrix::Identity(n, n);
    }

    // for compatibility: get a submatrix
    Matrix submatrix(int startRow, int startCol, int blockRows, int blockCols) {
        return this->block(startRow - 1, startCol - 1, blockRows - 1, blockCols - 1);
    }

    // for compatibility: create a diagonal matrix from a vector
    Matrix toDiagonal() {
        return this->asDiagonal();
    }

    // for compatibility: print matrix
    void print() {
        std::cout << *this << std::endl;
    }
};

#endif // MATRIX_HPP