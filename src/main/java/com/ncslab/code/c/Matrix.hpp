#ifndef MATRIX_HPP
#define MATRIX_HPP

typedef unsigned Index_T;

class Matrix
{
private:
    Index_T m_row, m_col;
    Index_T m_size;
    Index_T m_curIndex;
    double* m_ptr;
public:
    Matrix();
    Matrix(Index_T, Index_T);
    Matrix(Index_T n);
    Matrix(const Matrix& rhs);
    Matrix(double* ptr, Index_T nRow, Index_T nCol);
    ~Matrix(); // destructor

    Matrix& operator=(const Matrix&);  // asign operator
    friend Matrix operator+(const Matrix&, const Matrix&);
    friend Matrix operator-(const Matrix&, const Matrix&);
    friend Matrix operator*(const Matrix&, const Matrix&);  // matrix * matrix
    friend Matrix operator*(double, const Matrix&);  // double * matrix
    friend Matrix operator*(const Matrix&, double);  // matrix * double
    friend bool operator==(const Matrix&, const Matrix&);
    Matrix& operator+=(const Matrix&);
    Matrix& operator-=(const Matrix&);
    double& operator()(Index_T r, Index_T c) { return *(m_ptr + r * m_col + c); }

    // negative
    Matrix operator-() const; 

    /**
     * @brief concatenate two matrix
     *
     * @param m1 matrix 1
     * @param m2 matrix 2
     * @param dim concatenate dimension
     * 1 for row (vertically)
     * 2 for column (horizontally)
     * @return Matrix
     */
    static Matrix concatenate(const Matrix& m1, const Matrix& m2, Index_T dim); 

    // concatenate a matrix to the current matrix
    void concatenate(const Matrix& m, Index_T dim);

    /**
     * @brief create a n by n identity matrix
     * 
     * @param n size of the identity matrix
     * @return Matrix 
     */
    static Matrix identity(Index_T n);

    // get the number of rows
    Index_T row()const { return m_row; }
    // same as row(), just for compatibility
    Index_T rows()const { return m_row; } 

    // get the number of columns
    Index_T col()const { return m_col; }
    // same as col(), just for compatibility
    Index_T cols()const { return m_col; } 

    // get data pointer of a matrix
    double* data() const { return m_ptr; }

    // get the index row, start from 1
    Matrix getrow(Index_T index);

    // get the index col, start from 1
    Matrix getcol(Index_T index);

    // set an element of a matrix
    void set(int nRow, int nCol, double value);

    // get max element of a matrix
    double maxCoeff() const; 

    // transpose
    Matrix transpose() const; 

    // determinant
    double det() const; 

    // inverse
    Matrix inv() const; 

    // adjugate
    Matrix adjugate() const; 

    // submatrix
    Matrix submatrix(Index_T r1, Index_T r2, Index_T c1, Index_T c2) const; 

    // create a diagonal matrix from a vector
    Matrix toDiagonal() const;

    // get the diagonal of a matrix
    Matrix diag() const;

    /**
     * @brief permute rows of a matrix according to a vector
     * one-based index
     *
     * @param p permutation vector
     * @return Matrix
     */
    Matrix permuteRow(const Matrix& p) const;

    // permute columns of a matrix according to a vector
    Matrix permuteCol(const Matrix& p) const;

    // check if a matrix is symmetric
    bool isSymmetric() const; 

    // check if a matrix is triangular
    bool isTriangular() const; 

    // print matrix
    void print() const; 

    /**
     * @brief calculate the cross product of two vectors
     * 
     * @param m vector
     * @return Matrix 
     */
    Matrix crossProduct(const Matrix& m) const; 
};

#endif