#ifndef UTIL_HPP
#define UTIL_HPP

#include "ncslabdefines.hpp"
#include "ncslabccode.hpp"
#include <gsl/gsl_linalg.h>

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


//电路仿真的定义代码

typedef unsigned int uint32_T;

//逆表的数据结构
typedef struct{
	uint32_T size;	//逆阵的大小
	uint32_T *switchStatus; //逆阵对应的开关切换状态
	uint32_T isVariableChanged; //是否有参数发生变化，需要重新计算
	//REAL *rAA;
	gsl_matrix * inv; //逆阵的指针
}StoreGAA;

//逆阵表的数据结构
typedef struct{
	REAL *gAAOriginal;  //最基础的gAA副本指针
	uint32_T sizeOriginal; //原始的句子大小
	uint32_T switchNum; //开关的个数
	uint32_T *switchStatus; //开关的状态
	uint32_T storeGAASize; //逆阵表的大小
	StoreGAA *storeGAA; //指向逆阵的数据结构的指针
}SwitchGAA;

typedef struct{
	uint32_T refSize;
	uint32_T *refs;
	uint32_T size;
	int *vIndex;
	int *vIndexOriginal;
	
	REAL *iAOriginal;
	
	int isVariableChanged;
	
	void (*getUpdatedGAA)(REAL *);
	void (*getUpdatedIA)(REAL *);
	SwitchGAA *pSwitchGaa;
}Partition;

typedef struct{
	uint32_T size;
	Partition *partitions;
}Partitioner;

gsl_matrix *addSwitchCombine(SwitchGAA *psGaa,REAL *gAA,int switchNum,int size,uint32_T *switchStatus);
void setSwitchStatus(SwitchGAA *psGaa,int n,int status);
StoreGAA *findStoreGAA(SwitchGAA *psGaa,uint32_T *switchStatus);
int getSwtichStatus(SwitchGAA *psGaa,int pos);
void CircuitCombineIA(REAL *iA,int *vIndex,int *size_p,int* ref,int n,int m,int indexSize,int refSize);
gsl_matrix *caclulateInv(StoreGAA *pStoreGaa,REAL *gAA,int size);
#define CIRCUIT_THREAD_NUM 8

typedef struct{
	int id;
	SwitchGAA *psGaa;
	int size;
	
	int start;
	int num;
	
	gsl_matrix *inv;
	gsl_vector *b;
	gsl_vector *x;
	
#ifdef _WIN32_WINNT
	HANDLE  thread;
	DWORD threadId;	
	HANDLE hEvent;
	HANDLE rEvent;
#endif
	
}CircuitThread;

void startCircuitThread(CircuitThread *threads,int num);
void calculateCircuitMatrix(CircuitThread *threads,gsl_matrix *inv,gsl_vector *b,gsl_vector *x,int size);

int isPartRef(int nodeId,int *ref,int refSize,int *vIndex);

void CircuitOutput();
void CircuitUpdate();

void copyCircuitMartrix(REAL *gAA,REAL *gAAc,REAL *iA,REAL *iAc,int size);
void copyCircuitVector(REAL *iA,REAL *iAc,int size);
void CircuitCombine(REAL *,REAL *,int *,int *,int *,int,int,int,int);
#endif
