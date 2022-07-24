//
// Created by Lenovo on 2022/6/22.
//
#include "iostream"
#ifndef C___CLASS_MATRIX_H
#define C___CLASS_MATRIX_H
using namespace std;
typedef unsigned Index_T;
class Matrix
{
private:
    Index_T m_row, m_col;
    Index_T m_size;
    Index_T m_curIndex;
    double *m_ptr;
public:
    Matrix() :m_row(1), m_col(1)//�Ƿ�����
    {
        m_size = 1;
        if (m_size>0)
        {
            m_ptr = new double[m_size];
        }
        for (Index_T i=0;i<m_size;i++)
        {
            m_ptr[i] = 0;
        }
    };
    Matrix(Index_T r, Index_T c) :m_row(r), m_col(c)//�Ƿ�����
    {
        m_size = r*c;
        if (m_size>0)
        {
            m_ptr = new double[m_size];
        }
        else
            m_ptr = NULL;

        for (Index_T i=0;i<m_size;i++)
        {
            m_ptr[i] = 0;
        }
    };

    Matrix(Index_T n):m_row(n),m_col(n) //������
    {
        m_size = n*n;
        if (m_size>0)
        {
            m_ptr = new double[m_size];
        }
        else
            m_ptr = NULL;

        for (Index_T i=0;i<m_size;i++)
        {
            m_ptr[i] = 0;
        }
    };

    Matrix(const Matrix &rhs)//��������
    {
        m_row = rhs.m_row;
        m_col = rhs.m_col;
        m_size = rhs.m_size;
        m_ptr = new double[m_size];
        for (Index_T i = 0; i<m_size; i++)
            m_ptr[i] = rhs.m_ptr[i];
    }

    ~Matrix() //��������
    {
        if (m_ptr != NULL)
        {
            delete[]m_ptr;
            m_ptr = NULL;
        }
    }

    Matrix  &operator=(const Matrix&);  //������Ա��ָ�������д��ֵ������������ǳ�Ա
    friend Matrix  operator+(const Matrix&, const Matrix&);
    friend Matrix  operator-(const Matrix&, const Matrix&);
    friend Matrix  operator*(const Matrix&, const Matrix&);  //����˷�
    friend Matrix  operator*(double, const Matrix&);  //����˷�
    friend Matrix  operator*(const Matrix&, double);  //����˷�
    Matrix &operator+=(const Matrix&);
    Matrix &operator-=(const Matrix&);
    double& operator()(Index_T r, Index_T c){ return *(m_ptr + r*m_col + c); }

    Index_T row()const{ return m_row; }
    Index_T col()const{ return m_col; }
    Matrix getrow(Index_T index); // ���ص�index ��,������0 ����
    Matrix getcol(Index_T index); // ���ص�index ��
    Index_T rows()const{ return m_row; }
    Index_T cols()const{ return m_col; }
    void set(int nRow, int nCol, double value);
};

#endif //C___CLASS_MATRIX_H
