//
// Created by Lenovo on 2022/6/22.
//
#include "iostream"
using namespace std;
#include "Matrix.h"

Matrix& Matrix::operator+=(const Matrix & rhs) {
    if (this->m_col != rhs.m_col || this->m_row != rhs.m_row)
    {
        cout << "operator+(): ����shape ������,m_col:"
             << this->m_col << "," << rhs.m_col << ".  m_row:" << this->m_row << ", " << rhs.m_row << endl;
        this->m_ptr = NULL;
        return *this; //���ݲ��Ϸ�ʱ�򣬷��ؿվ���
    }
    for (Index_T i = 0; i<this->m_size; i++)
    {
        this->m_ptr[i] = this->m_ptr[i] + rhs.m_ptr[i];
    }
    return *this;

}

Matrix& Matrix::operator-=(const Matrix & rhs) {
    if (this->m_col != rhs.m_col || this->m_row != rhs.m_row)
    {
        cout << "operator-(): ����shape ������,m_col:"
             << this->m_col << "," << rhs.m_col << ".  m_row:" << this->m_row << ", " << rhs.m_row << endl;
        this->m_ptr = NULL;
        return *this; //���ݲ��Ϸ�ʱ�򣬷��ؿվ���
    }
    for (Index_T i = 0; i<this->m_size; i++)
    {
        this->m_ptr[i] = this->m_ptr[i] - rhs.m_ptr[i];
    }
    return *this;

}

Matrix& Matrix::operator=(const Matrix & rhs) {
    if (this != &rhs)
    {
        m_row = rhs.m_row;
        m_col = rhs.m_col;
        m_size = rhs.m_size;
        if (m_ptr != NULL)
            delete[] m_ptr;
        m_ptr = new double [m_size];
        for (Index_T i=0;i<m_size;i++)
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
        Matrix temp(0, 0);
        temp.m_ptr = NULL;
        cout << "operator+(): ����shape ������,m_col:"
             << lm.m_col << "," << rm.m_col << ".  m_row:" << lm.m_row << ", " << rm.m_row << endl;
        return temp; //���ݲ��Ϸ�ʱ�򣬷��ؿվ���
    }
    Matrix ret(lm.m_row, lm.m_col);
    for (Index_T i = 0; i<ret.m_size; i++)
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
        temp.m_ptr = NULL;
        cout << "operator-(): ����shape ������,m_col:"
             <<lm.m_col<<","<<rm.m_col<<".  m_row:"<< lm.m_row <<", "<< rm.m_row << endl;

        return temp; //���ݲ��Ϸ�ʱ�򣬷��ؿվ���
    }
    Matrix ret(lm.m_row, lm.m_col);
    for (Index_T i = 0; i<ret.m_size; i++)
    {
        ret.m_ptr[i] = lm.m_ptr[i] - rm.m_ptr[i];
    }
    return ret;
}

Matrix operator*(const Matrix& lm, const Matrix& rm)  //����˷�
{
    if (lm.m_size == 0 || rm.m_size == 0 || lm.m_col != rm.m_row)
    {
        Matrix temp(0, 0);
        temp.m_ptr = NULL;
        cout << "operator*(): ����shape ������,m_col:"
             << lm.m_col << "," << rm.m_col << ".  m_row:" << lm.m_row << ", " << rm.m_row << endl;
        return temp; //���ݲ��Ϸ�ʱ�򣬷��ؿվ���
    }
    Matrix ret(lm.m_row, rm.m_col);
    for (Index_T i = 0; i<lm.m_row; i++) //����� ��
    {
        for (Index_T j = 0; j< rm.m_col; j++) //�ҳ��� ��
        {
            for (Index_T k = 0; k< lm.m_col; k++)//lm.m_col == rm.m_row
            {
                ret.m_ptr[i*rm.m_col + j] += lm.m_ptr[i*lm.m_col + k] * rm.m_ptr[k*rm.m_col + j];
            }
        }
    }
    return ret;
}
Matrix operator*(double val, const Matrix& rm)  //����� ����
{
    Matrix ret(rm.m_row, rm.m_col);
    for (Index_T i = 0; i<ret.m_size; i++)
    {
        ret.m_ptr[i] = val * rm.m_ptr[i];
    }
    return ret;
}
Matrix operator*(const Matrix&lm, double val)  //����� ����
{
    Matrix ret(lm.m_row, lm.m_col);
    for (Index_T i = 0; i<ret.m_size; i++)
    {
        ret.m_ptr[i] = val * lm.m_ptr[i];
    }
    return ret;
}

Matrix Matrix::getrow(Index_T index)//������
{
    Matrix ret(1, m_col); //һ�еķ���ֵ

    for (Index_T i = 0; i< m_col; i++)
    {

        ret(0,i) = m_ptr[(index) *m_col + i] ;

    }
    return ret;
}

Matrix Matrix::getcol(Index_T index)//������
{
    Matrix ret(m_row, 1); //һ�еķ���ֵ

    for (Index_T i = 0; i< m_row; i++)
    {

        ret(i,0) = m_ptr[i *m_col + index];

    }
    return ret;
}
