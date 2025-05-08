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

// Default constructor
    Matrix() : Eigen::MatrixXd() {}

    // Constructor that initializes with specific rows and columns
    Matrix(int rows, int cols) : Eigen::MatrixXd(rows, cols) {}

    // Copy constructor
    Matrix(const Matrix& other) : Eigen::MatrixXd(other) {
        // If you have extra data, you can copy it here
//        std::cout << "Copy constructor called" << std::endl;
    }

    // Move constructor
    Matrix(Matrix&& other) noexcept : Eigen::MatrixXd(std::move(other)) {
        // If you have extra data, you can move it here
//        std::cout << "Move constructor called" << std::endl;
    }

    // Copy assignment operator
    Matrix& operator=(const Matrix& other) {
        if (this != &other) {
            // We use Eigen's copy assignment
            Eigen::MatrixXd::operator=(other);

            // If you have extra data, you would copy it here
//            std::cout << "Copy assignment operator called" << std::endl;
        }
        return *this;
    }

    // Move Assignment Operator
    Matrix& operator=(Matrix&& other) noexcept {
        if (this != &other) {
            // We call the base class move assignment to efficiently move the data
            Eigen::MatrixXd::operator=(std::move(other));

            // Optionally, clear any additional members if needed (for example, if you have added extra data members)
            // Example: this->extraData = std::move(other.extraData);
        }
        return *this;
    }

    // Optionally add scalar assignment operator if needed (for example, initializing all elements to a scalar)
    Matrix& operator=(double value) {
        this->setConstant(value);  // Eigen's setConstant sets all elements to the same value
        return *this;
    }

    // 重载括号操作符，使用一维 index 访问，index 对应行列下标从 1 开始
    double& operator()(int index) {
        int cols = this->cols();
        int row = (index - 1) / cols;
        int col = (index - 1) % cols;
        return Eigen::MatrixXd::operator()(row, col);
    }

    const double& operator()(int index) const {
        int cols = this->cols();
        int row = (index - 1) / cols;
        int col = (index - 1) % cols;
        return Eigen::MatrixXd::operator()(row, col);
    }

        // 重载括号操作符，实现二维下标从 0 开始访问
        double& operator()(int row, int col) {
            return Eigen::MatrixXd::operator()(row, col);
        }

        const double& operator()(int row, int col) const {
            return Eigen::MatrixXd::operator()(row, col);
        }
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
