#include <iostream>
#include "Matrix.hpp"

Matrix::Matrix() : m_row(1), m_col(1)
{
    m_size = 1;
    if (m_size > 0)
    {
        m_ptr = new double[m_size];
    }
    for (Index_T i = 0; i < m_size; i++)
    {
        m_ptr[i] = 0;
    }
};

Matrix::Matrix(double* ptr, Index_T nRow, Index_T nCol) : m_row(nRow), m_col(nCol)
{
    m_size = nRow * nCol;
    if (m_size > 0)
    {
        m_ptr = new double[m_size];
    }
    else
        m_ptr = nullptr;

    for (Index_T i = 0; i < m_size; i++)
    {
        m_ptr[i] = ptr[i];
    }
};


Matrix::Matrix(Index_T r, Index_T c) : m_row(r), m_col(c)
{
    m_size = r * c;
    if (m_size > 0)
    {
        m_ptr = new double[m_size];
    }
    else
        m_ptr = nullptr;

    for (Index_T i = 0; i < m_size; i++)
    {
        m_ptr[i] = 0;
    }
};

Matrix::Matrix(Index_T n) : m_row(n), m_col(n)
{
    m_size = n * n;
    if (m_size > 0)
    {
        m_ptr = new double[m_size];
    }
    else
        m_ptr = nullptr;

    for (Index_T i = 0; i < m_size; i++)
    {
        m_ptr[i] = 0;
    }
};

Matrix::Matrix(const Matrix& rhs)
{
    if (this == &rhs)
        return;

    m_row = rhs.m_row;
    m_col = rhs.m_col;
    m_size = rhs.m_size;
    m_ptr = new double[m_size];
    for (Index_T i = 0; i < m_size; i++)
        m_ptr[i] = rhs.m_ptr[i];
}

Matrix::~Matrix()
{
    if (m_ptr != NULL)
    {
        delete[]m_ptr;
        m_ptr = NULL;
    }
}


Matrix& Matrix::operator+=(const Matrix& rhs) {
    if (this->m_col != rhs.m_col || this->m_row != rhs.m_row)
    {
        printf("operator+(): mismatched shape! column: %d, %d; row: %d, %d\n",
            this->m_col, rhs.m_col, this->m_row, rhs.m_row);
        this->m_ptr = nullptr;
        return *this; // reset to null
    }
    for (Index_T i = 0; i < this->m_size; i++)
    {
        this->m_ptr[i] = this->m_ptr[i] + rhs.m_ptr[i];
    }
    return *this;

}

Matrix& Matrix::operator-=(const Matrix& rhs) {
    if (this->m_col != rhs.m_col || this->m_row != rhs.m_row)
    {
        printf("operator-(): mismatched shape! column: %d, %d; row: %d, %d\n",
            this->m_col, rhs.m_col, this->m_row, rhs.m_row);
        this->m_ptr = nullptr;
        return *this;
    }

    for (Index_T i = 0; i < this->m_size; i++)
    {
        this->m_ptr[i] = this->m_ptr[i] - rhs.m_ptr[i];
    }
    return *this;

}

Matrix& Matrix::operator=(const Matrix& rhs) {
    if (this != &rhs)
    {
        m_row = rhs.m_row;
        m_col = rhs.m_col;
        m_size = rhs.m_size;
        if (m_ptr != nullptr)
            delete[] m_ptr;
        m_ptr = new double[m_size];

        for (Index_T i = 0; i < m_size; i++)
        {
            m_ptr[i] = rhs.m_ptr[i];
        }
    }
    return *this;
}

Matrix operator+(const Matrix& lm, const Matrix& rm)
{
    if (lm.m_col != rm.m_col || lm.m_row != rm.m_row)
    {
        Matrix tmp(0, 0);
        tmp.m_ptr = nullptr;
        printf("operator+(): mismatched shape! column: %d, %d; row: %d, %d\n",
            lm.m_col, rm.m_col, lm.m_row, rm.m_row);
        return tmp; // return null when shape mismatch
    }

    Matrix ret(lm.m_row, lm.m_col);
    for (Index_T i = 0; i < ret.m_size; i++)
    {
        ret.m_ptr[i] = lm.m_ptr[i] + rm.m_ptr[i];
    }
    return ret;
}

Matrix operator-(const Matrix& lm, const Matrix& rm)
{
    if (lm.m_col != rm.m_col || lm.m_row != rm.m_row)
    {
        Matrix temp(0, 0);
        temp.m_ptr = nullptr;
        printf("operator-(): mismatched shape! column: %d, %d; row: %d, %d\n",
            lm.m_col, rm.m_col, lm.m_row, rm.m_row);
        return temp; // return null when shape mismatch
    }
    Matrix ret(lm.m_row, lm.m_col);
    for (Index_T i = 0; i < ret.m_size; i++)
    {
        ret.m_ptr[i] = lm.m_ptr[i] - rm.m_ptr[i];
    }
    return ret;
}

Matrix operator*(const Matrix& lm, const Matrix& rm)
{
    if (lm.m_size == 0 || rm.m_size == 0 || lm.m_col != rm.m_row)
    {
        Matrix temp(0, 0);
        temp.m_ptr = nullptr;
        printf("operator*(): mismatched shape! column: %d, %d; row: %d, %d\n",
            lm.m_col, rm.m_col, lm.m_row, rm.m_row);
        return temp;
    }
    Matrix ret(lm.m_row, rm.m_col);
    // lm.m_col == rm.m_row
    // reduce jump times in memory
    for (Index_T i = 0; i < lm.m_row; i++)
    {
        for (Index_T k = 0; k < lm.m_col; k++)
        {
            double s = lm.m_ptr[i * lm.m_col + k];
            for (Index_T j = 0; j < rm.m_col; j++)
            {
                ret.m_ptr[i * rm.m_col + j] += s * rm.m_ptr[k * rm.m_col + j];
            }
        }
    }

    return ret;
}

Matrix operator*(double val, const Matrix& rm)
{
    Matrix ret(rm.m_row, rm.m_col);
    for (Index_T i = 0; i < ret.m_size; i++)
    {
        ret.m_ptr[i] = val * rm.m_ptr[i];
    }
    return ret;
}

Matrix operator*(const Matrix& lm, double val)
{
    Matrix ret(lm.m_row, lm.m_col);
    for (Index_T i = 0; i < ret.m_size; i++)
    {
        ret.m_ptr[i] = val * lm.m_ptr[i];
    }
    return ret;
}

bool operator==(const Matrix& lm, const Matrix& rm)
{
    if (lm.m_col != rm.m_col || lm.m_row != rm.m_row)
    {
        return false;
    }
    for (Index_T i = 0; i < lm.m_size; i++)
    {
        if (lm.m_ptr[i] != rm.m_ptr[i])
        {
            return false;
        }
    }
    return true;
}

Matrix Matrix::getrow(Index_T index)
{
    if (index < 1 || index > m_row)
    {
        throw "getrow(): index out of range!";
        Matrix tmp(0, 0);
        tmp.m_ptr = nullptr;
        return tmp;
    }

    index--;

    Matrix ret(1, m_col);

    for (Index_T i = 0; i < m_col; i++)
    {
        ret(0, i) = m_ptr[index * m_col + i];
    }
    return ret;
}

Matrix Matrix::getcol(Index_T index)
{
    if (index < 1 || index > m_col)
    {
        throw "getcol(): index out of range!";
        Matrix tmp(0, 0);
        tmp.m_ptr = nullptr;
        return tmp;
    }
    index--;
    Matrix ret(m_row, 1);

    for (Index_T i = 0; i < m_row; i++)
    {
        ret(i, 0) = m_ptr[i * m_col + index];
    }
    return ret;
}

Matrix Matrix::operator-() const
{
    Matrix ret(m_row, m_col);
    for (Index_T i = 0; i < m_size; i++)
    {
        ret.m_ptr[i] = -m_ptr[i];
    }
    return ret;
}

Matrix Matrix::concatenate(const Matrix& m1, const Matrix& m2, Index_T dim)
{
    if (dim == 1)
    {
        if (m1.m_col != m2.m_col)
        {
            printf("concatenate(): mismatched shape!\n");
            Matrix tmp(0, 0);
            tmp.m_ptr = nullptr;
            return tmp;
        }
        Matrix ret(m1.m_row + m2.m_row, m1.m_col);
        for (Index_T i = 0; i < m1.m_row; i++)
        {
            for (Index_T j = 0; j < m1.m_col; j++)
            {
                ret(i, j) = m1.m_ptr[i * m1.m_col + j];
            }
        }
        for (Index_T i = 0; i < m2.m_row; i++)
        {
            for (Index_T j = 0; j < m2.m_col; j++)
            {
                ret(i + m1.m_row, j) = m2.m_ptr[i * m2.m_col + j];
            }
        }
        return ret;
    }
    else if (dim == 2)
    {
        if (m1.m_row != m2.m_row)
        {
            printf("concatenate(): mismatched shape!\n");
            Matrix tmp(0, 0);
            tmp.m_ptr = nullptr;
            return tmp;
        }
        Matrix ret(m1.m_row, m1.m_col + m2.m_col);
        for (Index_T i = 0; i < m1.m_row; i++)
        {
            for (Index_T j = 0; j < m1.m_col; j++)
            {
                ret(i, j) = m1.m_ptr[i * m1.m_col + j];
            }
        }
        for (Index_T i = 0; i < m2.m_row; i++)
        {
            for (Index_T j = 0; j < m2.m_col; j++)
            {
                ret(i, j + m1.m_col) = m2.m_ptr[i * m2.m_col + j];
            }
        }
        return ret;
    }
    else
    {
        printf("concatenate(): dim should be 1 or 2!\n");
        Matrix tmp(0, 0);
        tmp.m_ptr = nullptr;
        return tmp;
    }
}

void Matrix::concatenate(const Matrix& m, Index_T dim)
{
    if (dim == 1)
    {
        if (m.m_col != m_col)
        {
            printf("concatenate(): mismatched shape!\n");
            return;
        }
        Matrix ret(m_row + m.m_row, m_col);
        for (Index_T i = 0; i < m_row; i++)
        {
            for (Index_T j = 0; j < m_col; j++)
            {
                ret(i, j) = m_ptr[i * m_col + j];
            }
        }
        for (Index_T i = 0; i < m.m_row; i++)
        {
            for (Index_T j = 0; j < m.m_col; j++)
            {
                ret(i + m_row, j) = m.m_ptr[i * m.m_col + j];
            }
        }
        *this = ret;
    }
    else if (dim == 2)
    {
        if (m.m_row != m_row)
        {
            printf("concatenate(): mismatched shape!\n");
            return;
        }
        Matrix ret(m_row, m_col + m.m_col);
        for (Index_T i = 0; i < m_row; i++)
        {
            for (Index_T j = 0; j < m_col; j++)
            {
                ret(i, j) = m_ptr[i * m_col + j];
            }
        }
        for (Index_T i = 0; i < m.m_row; i++)
        {
            for (Index_T j = 0; j < m.m_col; j++)
            {
                ret(i, j + m_col) = m.m_ptr[i * m.m_col + j];
            }
        }
        *this = ret;
    }
    else
    {
        printf("concatenate(): dim should be 1 or 2!\n");
        return;
    }
}

Matrix Matrix::identity(Index_T n)
{
    Matrix ret(n, n);
    for (Index_T i = 0; i < n; i++)
    {
        ret(i, i) = 1;
    }
    return ret;
}

void Matrix::set(int nRow, int nCol, double value)
{
    if (nRow < m_row && nCol < m_col)
    {
        m_ptr[nRow * m_col + nCol] = value;
    }
}

double Matrix::maxCoeff() const
{
    double max = m_ptr[0];
    for (Index_T i = 1; i < m_size; i++)
    {
        if (m_ptr[i] > max)
        {
            max = m_ptr[i];
        }
    }
    return max;
}

Matrix Matrix::transpose() const
{
    Matrix ret(m_col, m_row);
    for (Index_T i = 0; i < m_row; i++)
    {
        for (Index_T j = 0; j < m_col; j++)
        {
            ret(j, i) = m_ptr[i * m_col + j];
        }
    }
    return ret;
}

double Matrix::det() const
{
    if (m_row != m_col)
    {
        printf("det(): not a square matrix!\n");
        return 0;
    }
    if (m_row == 1)
    {
        return m_ptr[0];
    }
    else
    {
        double ret = 0;
        // choose first row, then do cofactor expansion
        // if choose first column, performance may be better
        for (Index_T i = 0; i < m_col; i++)
        {
            Matrix temp(m_row - 1, m_col - 1);
            for (Index_T j = 0; j < m_row - 1; j++)
            {
                for (Index_T k = 0; k < m_col - 1; k++)
                {
                    if (k < i)
                    {
                        temp(j, k) = m_ptr[(j + 1) * m_col + k];
                    }
                    else
                    {
                        temp(j, k) = m_ptr[(j + 1) * m_col + k + 1];
                    }
                }
            }
            // recursion
            ret += m_ptr[i] * temp.det() * ((i % 2 == 0) ? 1 : -1);
        }
        return ret;
    }
}

Matrix Matrix::inv() const
{
    // only non-singular square matrix has inverse matrix
    if (this->det() == 0)
    {
        Matrix tmp(0, 0);
        printf("inv(): singular or non-square matrix!\n");
        tmp.m_ptr = nullptr;
        return tmp;
    }

    Matrix ret(m_row, m_col);

    if (m_row == 1)
    {
        ret.m_ptr[0] = 1 / m_ptr[0];
        return ret;
    }
    else
    {
        // Gauss-Jordan method
        // 1. construct a new matrix
        Matrix temp(m_row, 2 * m_col);
        for (Index_T i = 0; i < m_row; i++)
        {
            for (Index_T j = 0; j < m_col; j++)
            {
                temp(i, j) = m_ptr[i * m_col + j];
            }
        }
        for (Index_T i = 0; i < m_row; i++)
        {
            temp(i, m_col + i) = 1;
        }
        // 2. Gauss-Jordan
        for (Index_T i = 0; i < m_row; i++)
        {
            // 2.1 find the max value in the column
            double max = temp(i, i);
            Index_T maxIndex = i;
            for (Index_T j = i + 1; j < m_row; j++)
            {
                if (temp(j, i) > max)
                {
                    max = temp(j, i);
                    maxIndex = j;
                }
            }
            // 2.2 swap the max row and the current row
            if (maxIndex != i)
            {
                for (Index_T j = 0; j < 2 * m_col; j++)
                {
                    double t = temp(i, j);
                    temp(i, j) = temp(maxIndex, j);
                    temp(maxIndex, j) = t;
                }
            }
            // 2.3 divide the current row by the pivot
            double pivot = temp(i, i);
            for (Index_T j = 0; j < 2 * m_col; j++)
            {
                temp(i, j) = temp(i, j) / pivot;
            }
            // 2.4 eliminate the other rows
            for (Index_T j = 0; j < m_row; j++)
            {
                if (j != i)
                {
                    double factor = temp(j, i);
                    for (Index_T k = 0; k < 2 * m_col; k++)
                    {
                        temp(j, k) = temp(j, k) - factor * temp(i, k);
                    }
                }
            }
        }
        // 3. copy the result to the return matrix
        for (Index_T i = 0; i < m_row; i++)
        {
            for (Index_T j = 0; j < m_col; j++)
            {
                ret(i, j) = temp(i, m_col + j);
            }
        }
        return ret;
    }
}

Matrix Matrix::adjugate() const
{
    if (m_row != m_col)
    {
        printf("adjugate(): not a square matrix!\n");
        Matrix tmp(0, 0);
        tmp.m_ptr = nullptr;
        return tmp;
    }
    Matrix ret(m_row, m_col);
    if (m_row == 1)
    {
        ret.m_ptr[0] = 1;
        return ret;
    }
    else
    {
        // adjugate matrix = transpose of cofactor matrix
        for (Index_T i = 0; i < m_row; i++)
        {
            for (Index_T j = 0; j < m_col; j++)
            {
                Matrix temp(m_row - 1, m_col - 1);
                for (Index_T k = 0; k < m_row - 1; k++)
                {
                    for (Index_T l = 0; l < m_col - 1; l++)
                    {
                        if (k < i && l < j)
                        {
                            temp(k, l) = m_ptr[k * m_col + l];
                        }
                        else if (k < i && l >= j)
                        {
                            temp(k, l) = m_ptr[k * m_col + l + 1];
                        }
                        else if (k >= i && l < j)
                        {
                            temp(k, l) = m_ptr[(k + 1) * m_col + l];
                        }
                        else
                        {
                            temp(k, l) = m_ptr[(k + 1) * m_col + l + 1];
                        }
                    }
                }
                ret(j, i) = temp.det() * ((i + j) % 2 == 0 ? 1 : -1);
            }
        }
        return ret;
    }

}

Matrix Matrix::submatrix(Index_T r1, Index_T r2, Index_T c1, Index_T c2) const
{
    if (r1 < 1 || r2 > m_row || c1 < 1 || c2 > m_col)
    {
        throw("submatrix(): index out of range!");
        Matrix tmp(0, 0);
        tmp.m_ptr = nullptr;
        return tmp;
    }
    r1--;
    r2--;
    c1--;
    c2--;
    Matrix ret(r2 - r1 + 1, c2 - c1 + 1);
    for (Index_T i = r1; i <= r2; i++)
    {
        for (Index_T j = c1; j <= c2; j++)
        {
            ret(i - r1, j - c1) = m_ptr[i * m_col + j];
        }
    }
    return ret;
}

Matrix Matrix::toDiagonal() const
{
    if (m_row != 1 && m_col != 1)
    {
        printf("toDiagonal(): not a vector!\n");
        Matrix tmp(0, 0);
        tmp.m_ptr = nullptr;
        return tmp;
    }

    Index_T n = (m_row > m_col) ? m_row : m_col;
    Matrix ret(n);

    for (Index_T i = 0; i < n; i++)
    {
        ret(i, i) = m_ptr[i];
    }
    return ret;
}

Matrix Matrix::diag() const
{
    // get the smaller index
    Index_T n = (m_row < m_col) ? m_row : m_col;
    Matrix ret(1, n);
    for (Index_T i = 0; i < n; i++)
    {
        ret(0, i) = m_ptr[i * m_col + i];
    }
    return ret;
}

Matrix Matrix::permuteRow(const Matrix& p) const
{
    if (p.m_row != 1 && p.m_col != 1)
    {
        printf("permuteRow(): not a vector!\n");
        Matrix tmp(0, 0);
        tmp.m_ptr = nullptr;
        return tmp;
    }
    if (p.m_row != m_row && p.m_col != m_row)
    {
        printf("permuteRow(): mismatched shape!\n");
        Matrix tmp(0, 0);
        tmp.m_ptr = nullptr;
        return tmp;
    }
    Matrix ret(m_row, m_col);
    for (Index_T i = 0; i < m_row; i++)
    {
        Index_T index = (Index_T)p.m_ptr[i] - 1;
        for (Index_T j = 0; j < m_col; j++)
        {
            ret(i, j) = m_ptr[index * m_col + j];
        }
    }
    return ret;
}

Matrix Matrix::permuteCol(const Matrix& p) const
{
    if (p.m_row != 1 && p.m_col != 1)
    {
        printf("permuteCol(): not a vector!\n");
        Matrix tmp(0, 0);
        tmp.m_ptr = nullptr;
        return tmp;
    }
    if (p.m_row != m_col && p.m_col != m_col)
    {
        printf("permuteCol(): mismatched shape!\n");
        Matrix tmp(0, 0);
        tmp.m_ptr = nullptr;
        return tmp;
    }
    Matrix ret(m_row, m_col);
    for (Index_T i = 0; i < m_col; i++)
    {
        Index_T index = (Index_T)p.m_ptr[i] - 1;
        for (Index_T j = 0; j < m_row; j++)
        {
            ret(j, i) = m_ptr[j * m_col + index];
        }
    }
    return ret;
}

bool Matrix::isSymmetric() const
{
    if (m_row != m_col)
    {
        printf("isSymmetric(): not a square matrix!\n");
        return false;
    }
    for (Index_T i = 0; i < m_row; i++)
    {
        for (Index_T j = 0; j < i; j++)
        {
            if (m_ptr[i * m_col + j] != m_ptr[j * m_col + i])
            {
                return false;
            }
        }
    }
    return true;
}

bool Matrix::isTriangular() const
{
    if (m_row != m_col)
    {
        printf("isTriangular(): not a square matrix!\n");
        return false;
    }

    bool isUpper = true;
    bool isLower = true;

    for (Index_T i = 0; i < m_row; i++)
    {
        // for loop can exit directly
        for (Index_T j = 0; j < i; j++)
        {
            if (m_ptr[i * m_col + j] != 0)
            {
                isUpper = false;
                break;
            }
        }
    }

    for (Index_T i = 0; i < m_row; i++)
    {
        for (Index_T j = i + 1; j < m_col; j++)
        {
            if (m_ptr[i * m_col + j] != 0)
            {
                isLower = false;
                break;
            }
        }
    }

    return isUpper || isLower;
}

void Matrix::print() const
{
    for (Index_T i = 0; i < m_row; i++)
    {
        for (Index_T j = 0; j < m_col; j++)
        {
            printf("%f ", m_ptr[i * m_col + j]);
        }
        printf("\n");
    }
    printf("\n");
}

Matrix Matrix::crossProduct(const Matrix& m) const {
    if (m_size != 3 || m.m_size != 3)
    {
        printf("crossProduct(): not a 3D vector!\n");
        Matrix tmp(0, 0);
        tmp.m_ptr = nullptr;
        return tmp;
    }

    Matrix ret(1, 3);
    ret(0, 0) = m_ptr[1] * m.m_ptr[2] - m_ptr[2] * m.m_ptr[1];
    ret(0, 1) = m_ptr[2] * m.m_ptr[0] - m_ptr[0] * m.m_ptr[2];
    ret(0, 2) = m_ptr[0] * m.m_ptr[1] - m_ptr[1] * m.m_ptr[0];
    return ret;
}