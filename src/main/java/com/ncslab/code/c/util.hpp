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

// 定义一维插值表结构体
typedef struct {
    double *x;                // x坐标数组
    double *y;                // y值数组
    int size;                 // 插值表的大小
    int extrapolation_strategy; // 外插策略：0=最近边界值，1=线性外插
} InterpolationTable1D;

// 初始化一维插值表
void init_interpolation_table_1d(InterpolationTable1D *table, int size, int extrapolation_strategy);
// 释放一维插值表内存
void free_interpolation_table_1d(InterpolationTable1D *table);
// 一维插值函数
double linear_interpolation_1d(InterpolationTable1D *table, double target_x);
void insert_to_x_table1d(InterpolationTable1D *table, int index, double value);
void insert_to_y_table1d(InterpolationTable1D *table, int index, double value);

// 定义二维插值表结构体
typedef struct {
    double **x;       // x坐标数组
    double **y;       // y坐标数组
    double **z;       // z值数组
    int width;        // 插值表的宽度（x方向）
    int height;       // 插值表的高度（y方向）
    int extrapolation_strategy; // 外插策略：0=最近边界值，1=线性外推
} InterpolationTable;

void init_interpolation_table(InterpolationTable *table, int width, int height, int extrapolation_strategy);
void free_interpolation_table(InterpolationTable *table);
double bilinear_interpolation(InterpolationTable *table, double target_x, double target_y);
void insert_to_x_table(InterpolationTable *table, int index, double value);
void insert_to_y_table(InterpolationTable *table, int index, double value);
void insert_to_z_table(InterpolationTable *table, int x_index, int y_index, double value);

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
