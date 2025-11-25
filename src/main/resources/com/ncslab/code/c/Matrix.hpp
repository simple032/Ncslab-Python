#ifndef MATRIX_HPP
#define MATRIX_HPP

#include <iostream>
#include <Eigen/Dense>

/**
 * @brief Templated Matrix class extending Eigen::Matrix to support multiple data types
 *
 * Extends Eigen::Matrix with useful utility functions and provides support for
 * multiple data types including double, float, integers (signed/unsigned), and bool.
 *
 * @tparam T The data type (double, float, int8_t, int16_t, int32_t, int64_t,
 *           uint8_t, uint16_t, uint32_t, uint64_t, bool)
 *
 * @example
 * Matrix<double> m1;  // Double precision matrix
 * Matrix<float> m2;   // Single precision matrix
 * Matrix<int32_t> m3; // Integer matrix
 * MatrixD m4;         // Default double precision (backward compatible)
 *
 * @note All methods are implemented inline for header-only template support
 *
 * @attention
 * Be careful about aliasing
 * e.g. a = a.transpose() is wrong
 * Please use a.transposeInPlace() instead
 */
template<typename T>
class MatrixT : public Eigen::Matrix<T, Eigen::Dynamic, Eigen::Dynamic> {
public:
    using Base = Eigen::Matrix<T, Eigen::Dynamic, Eigen::Dynamic>;
    using Base::Base;  // Inherit constructors

    // Default constructor
    MatrixT() : Base() {}

    // Constructor that initializes with specific rows and columns
    MatrixT(int rows, int cols) : Base(rows, cols) {}

    // Copy constructor
    MatrixT(const MatrixT& other) : Base(other) {}

    // Move constructor
    MatrixT(MatrixT&& other) noexcept : Base(std::move(other)) {}

    // Template constructor for different types (allows conversion)
    template<typename OtherDerived>
    MatrixT(const Eigen::MatrixBase<OtherDerived>& other) : Base(other) {}

    // Copy assignment operator
    MatrixT& operator=(const MatrixT& other) {
        if (this != &other) {
            Base::operator=(other);
        }
        return *this;
    }

    // Move assignment operator
    MatrixT& operator=(MatrixT&& other) noexcept {
        if (this != &other) {
            Base::operator=(std::move(other));
        }
        return *this;
    }

    // Template assignment operator for different types
    template<typename OtherDerived>
    MatrixT& operator=(const Eigen::MatrixBase<OtherDerived>& other) {
        Base::operator=(other);
        return *this;
    }

    // Scalar assignment operator (initializes all elements to a scalar)
    MatrixT& operator=(T value) {
        this->setConstant(value);
        return *this;
    }

    /**
     * @brief 1D index access operator (MATLAB-style 1-based indexing)
     *
     * Provides linear indexing into the matrix using 1-based indices.
     * Index is converted to row-major order.
     *
     * @param index 1-based linear index
     * @return Reference to the element
     */
    T& operator()(int index) {
        int cols = this->cols();
        int row = (index - 1) / cols;
        int col = (index - 1) % cols;
        return Base::operator()(row, col);
    }

    const T& operator()(int index) const {
        int cols = this->cols();
        int row = (index - 1) / cols;
        int col = (index - 1) % cols;
        return Base::operator()(row, col);
    }

    /**
     * @brief 2D index access operator (0-based indexing)
     *
     * Standard matrix element access with 0-based row and column indices.
     *
     * @param row Row index (0-based)
     * @param col Column index (0-based)
     * @return Reference to the element
     */
    T& operator()(int row, int col) {
        return Base::operator()(row, col);
    }

    const T& operator()(int row, int col) const {
        return Base::operator()(row, col);
    }

    // 与另一个矩阵相加
    MatrixT& operator+=(const MatrixT& other) {
        Base::operator+=(other); // 显式转为基类引用
        return *this; // 返回基类引用（*this 作为 Base 类型）
    }

    // 与标量 double 相加
    MatrixT& operator+=(const double other) {
        // Eigen 矩阵与标量相加需用 this->operator+=，触发标量重载
        this->operator+=(other); 
        return *this;
    }

    // 减法类似处理
    MatrixT& operator-=(const MatrixT& other) {
        Base::operator-=(other);
        return *this;
    }

    MatrixT& operator-=(const double other) {
        this->operator-=(other);
        return *this;
    }

    /**
     * @brief Concatenate two matrices along specified dimension
     *
     * @param m1 First matrix
     * @param m2 Second matrix
     * @param dim Concatenation dimension (1=vertical/rows, 2=horizontal/columns)
     * @return Concatenated matrix
     */
    static MatrixT concatenate(const MatrixT& m1, const MatrixT& m2, int dim) {
        MatrixT m3;
        if (dim == 1) {
            // Vertical concatenation (stack rows)
            m3 = MatrixT::Zero(m1.rows() + m2.rows(), m1.cols());
            m3 << m1, m2;
        }
        else if (dim == 2) {
            // Horizontal concatenation (stack columns)
            m3 = MatrixT::Zero(m1.rows(), m1.cols() + m2.cols());
            m3 << m1, m2;
        }
        return m3;
    }

    /**
     * @brief Concatenate another matrix to this matrix
     *
     * @param m Matrix to concatenate
     * @param dim Concatenation dimension (1=vertical/rows, 2=horizontal/columns)
     */
    void concatenate(const MatrixT& m, int dim) {
        if (dim == 1) {
            *this = MatrixT::concatenate(*this, m, 1);
        }
        else if (dim == 2) {
            *this = MatrixT::concatenate(*this, m, 2);
        }
    }

    /**
     * @brief Permute rows of a matrix according to a permutation vector
     *
     * @param p Permutation vector with 1-based indices
     * @return Matrix with permuted rows
     */
    MatrixT permuteRow(const Eigen::VectorXi& p) {
        MatrixT m(p.size(), this->cols());
        for (int i = 0; i < p.size(); i++) {
            m.row(i) = this->row(p(i) - 1);
        }
        return m;
    }

    /**
     * @brief Permute columns of a matrix according to a permutation vector
     *
     * @param p Permutation vector with 1-based indices
     * @return Matrix with permuted columns
     */
    MatrixT permuteCol(const Eigen::VectorXi& p) {
        MatrixT m(this->rows(), p.size());
        for (int i = 0; i < p.size(); i++) {
            m.col(i) = this->col(p(i) - 1);
        }
        return m;
    }

    /**
     * @brief Check if matrix is symmetric
     *
     * @return true if matrix equals its transpose (within numerical tolerance)
     */
    bool isSymmetric() const {
        return this->isApprox(this->transpose());
    }

    /**
     * @brief Check if matrix is triangular (upper or lower)
     *
     * @return true if matrix is upper or lower triangular
     */
    bool isTriangular() const {
        return this->isUpperTriangular() || this->isLowerTriangular();
    }

    // ============================================================================
    // Compatibility methods for legacy code
    // ============================================================================

    /**
     * @brief Set element at specified position (compatibility method)
     *
     * @param row Row index (0-based)
     * @param col Column index (0-based)
     * @param value Value to set
     */
    void set(int row, int col, T value) {
        this->operator()(row, col) = value;
    }

    /**
     * @brief Get the determinant of a matrix (compatibility method)
     *
     * @return Determinant value
     * @note Only valid for square matrices
     */
    T det() const {
        return this->determinant();
    }

    /**
     * @brief Get the inverse of a matrix (compatibility method)
     *
     * @return Inverse matrix
     * @note Only valid for square, invertible matrices
     */
    MatrixT inv() const {
        return this->inverse();
    }

    /**
     * @brief Get the diagonal of a matrix (compatibility method)
     *
     * @return Column vector containing diagonal elements
     */
    MatrixT diag() const {
        return this->diagonal();
    }

    /**
     * @brief Create an identity matrix (compatibility method)
     *
     * @param n Size of the identity matrix (n×n)
     * @return Identity matrix
     */
    static MatrixT identity(int n) {
        return MatrixT::Identity(n, n);
    }

    /**
     * @brief Extract a submatrix (compatibility method)
     *
     * @param startRow Starting row index (1-based)
     * @param startCol Starting column index (1-based)
     * @param blockRows Number of rows in submatrix
     * @param blockCols Number of columns in submatrix
     * @return Extracted submatrix
     */
    MatrixT submatrix(int startRow, int startCol, int blockRows, int blockCols) {
        return this->block(startRow - 1, startCol - 1, blockRows, blockCols);
    }

    /**
     * @brief Create a diagonal matrix from a vector (compatibility method)
     *
     * Treats this matrix as a vector and creates a diagonal matrix from it.
     *
     * @return Diagonal matrix
     */
    MatrixT toDiagonal() {
        return this->asDiagonal();
    }

    /**
     * @brief Print matrix to standard output (compatibility method)
     */
    void print() const {
        std::cout << *this << std::endl;
    }
};

// ============================================================================
// Type aliases for backward compatibility and convenience
// ============================================================================

// Default type (backward compatible with original Matrix class)
using MatrixD = MatrixT<double>;
using Matrix = MatrixD;  // For complete backward compatibility

// Floating point types
using MatrixF = MatrixT<float>;   // Single precision

// Signed integer types
using MatrixI8 = MatrixT<int8_t>;
using MatrixI16 = MatrixT<int16_t>;
using MatrixI32 = MatrixT<int32_t>;
using MatrixI64 = MatrixT<int64_t>;

// Unsigned integer types
using MatrixU8 = MatrixT<uint8_t>;
using MatrixU16 = MatrixT<uint16_t>;
using MatrixU32 = MatrixT<uint32_t>;
using MatrixU64 = MatrixT<uint64_t>;

// Boolean type
using MatrixB = MatrixT<bool>;

// ============================================================================
// Type mapping for SIMULINK data types
// ============================================================================

/**
 * @brief Map SIMULINK data type strings to corresponding Matrix types
 *
 * Usage example in code generation:
 * if (dataType == "double") return MatrixD
 * if (dataType == "single") return MatrixF
 * if (dataType == "int32") return MatrixI32
 * etc.
 */

#endif // MATRIX_HPP
