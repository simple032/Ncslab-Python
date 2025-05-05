#ifndef UTIL_HPP
#define UTIL_HPP

#include "ncslabdefines.hpp"
#include "ncslabccode.hpp"

// 定义缓冲区结构体
typedef struct {
    double *times;  // 存储时间点
    double *values; // 存储对应的值
    int size;       // 缓冲区大小
    int head;       // 队列头索引
    int tail;       // 队列尾索引
    int count;      // 当前队列元素数量
} Buffer;

// 初始化缓冲区
void init_buffer(Buffer *buf, int size, double init_value);
// 释放缓冲区内存
void free_buffer(Buffer *buf);

// 插入新数据到缓冲区
void insert_to_buffer(Buffer *buf, double time, double value);

// 线性插值函数
double linear_interpolation(Buffer *buf, double target_time);

extern REAL real_sample_time;

double calalpoutput(double inputvalue);
void storeState(int);
void restoreState(int);
void storeDerivative(int);
REAL calculateStateDif(int,int);
void caculateDerivative(double *,int);
void discreteInit();
void discreteInitFixed();
double gcd( double, double);
double gcd1(double *);
int hasdiscrete(double *);
double distance(double,double);
void writeInformation();
double generateGaussianNoise(double mean, double stdDev);
double lowPassFilter(double input, double alpha);
unsigned char calcSum(unsigned char bytes[]);
#endif
