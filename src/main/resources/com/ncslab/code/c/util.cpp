#include <iostream>
#include <cmath>
#include <cstdio>
#ifndef _WIN32
#include <sys/time.h>
#else
#include <windows.h>
#endif // _WIN32
#include "ncslabdefines.hpp"
#include "ncslab.hpp"
#include "Matrix.hpp"
#include "onestep.hpp"
#include "util.hpp"
//#include <octave/oct.h>

extern MODEL* mp;

double calalpoutput(double inputvalue) {
	double result;
	if (inputvalue < 230) {
		result = inputvalue;
	}
	else if (inputvalue >= 230 && inputvalue < 240) {
		result = 1.5 * (inputvalue - 230) + 230;
	}
	else if (inputvalue >= 240 && inputvalue < 250) {
		result = 2.5 * (inputvalue - 240) + 245;
	}
	else if (inputvalue >= 250 && inputvalue < 270) {
		result = 1.25 * (inputvalue - 250) + 270;
	}
	else if (inputvalue >= 270 && inputvalue < 300) {
		result = 0.66 * (inputvalue - 270) + 295;
	}
	else {
		result = inputvalue + 15;
	}
	return result;
}
extern double sample_time[];

double real_sample_time = 0.0;

void discreteInit() {
	extern int sample_i;
	//extern double sample_time[];

	if (hasdiscrete(sample_time) == 1) {
		real_sample_time = gcd1(sample_time);
	}
	//end
	mp->discreteUpdate = 1;
	if (mp->stepSize > real_sample_time)
	{
		mp->stepSize = real_sample_time;
	}
}

void discreteInitFixed() {
	//xiazhiqiang:Evaluates the greatest common divisor by referencing the sampling time array:(The definition location is at line 31 of codeStructC)
	extern int sample_i;
	//extern double sample_time[];

	if (hasdiscrete(sample_time) == 1) {
		real_sample_time = gcd1(sample_time);
	}
	//end
	mp->discreteUpdate = 1;

}

//xiazhiqiang:Computes the greatest common divisor of the sampling time array
double gcd(double x, double y)
{
	int a = (int)(x * 1000);
	int b = (int)(y * 1000);
	int result;
	for (result = a; result >= 0; result--)
	{
		if (0 == a % result && 0 == b % result)
			return result / 1000.0;
	}
	return 0;
}

double gcd1(double a[]) {
	double d = a[0];
	int i;
	for (i = 0; i < mp->blockNum; i++) {
		if (a[i] == 0) {
			break;
		}
		d = gcd(d, a[i]);
	}
	return d;
}

// 函数生成正态分布随机数 (Box-Muller方法)
double generateGaussianNoise(double mean, double stdDev) {
    static int hasSpare = 0;
    static double spare;
    if (hasSpare) {
        hasSpare = 0;
        return mean + stdDev * spare;
    }
    hasSpare = 1;
    double u, v, s;
    do {
        u = (rand() / ((double)RAND_MAX)) * 2.0 - 1.0;
        v = (rand() / ((double)RAND_MAX)) * 2.0 - 1.0;
        s = u * u + v * v;
    } while (s >= 1.0 || s == 0.0);
    s = sqrt(-2.0 * log(s) / s);
    spare = v * s;
    return mean + stdDev * (u * s);
}

// 一阶RC低通滤波器
double lowPassFilter(double input, double alpha) {
  static double prevOutput = 0.0;
    double output = alpha * input + (1.0 - alpha) * (prevOutput);
    prevOutput = output;
    return output;
}
//xiazhiqiang:Check whether there are discrete modules

// int hasdiscrete(double a[]) {
// 	int i;
// 	for (i = 0; i < mp->blockNum; i++)
// 	{
// 		if (a[i] == 0)
// 		{
// 			break;
// 		}
// 	}
// 	if (i == 0)
// 	{
// 		return 0;
// 	}
// 	else
// 	{
// 		return 1;
// 	}
// }

int hasdiscrete(double a[])
{
	return a[0] != 0;
}


//xiazhiqang:Distance from the next sampling point
/*
double distance(double t,double s){
	long a=(long)(t*1000000000000000);
		long b=(long)(s*1000000000000000);
		double distance=s-(a%b)/1000000000000000.0;
		if(distance<0){
			distance=0;
		}
		return distance;
}*/


double distance(double t, double s) {
	long num = t / s;
	double dist = (num + 1) * s - t;
	if (dist < 0) {
		dist = 0;
	}
	return dist;
}

//end

void storeState(int num) {
	/*
	for(int i=0;i<STATE_NUM;i++){mbiguous
	  stateReserve[i]=*((REAL *)(mp->states[i]->vp));
	}*/
	int single = 0;
	int matrix = 0;
	for (int i = 0;i < STATE_NUM;i++) {
		switch (mp->states[i]->type) {
		case SINGLE:
			singleStateReserve[num][single++] = *((REAL*)(mp->states[i]->vp));
			break;
		case MATRIX:
			matrixStateReserve[num][matrix++] = *((Matrix*)(mp->states[i]->vp));
			break;
		}
	}
}

void restoreState(int num) {
	/*
	for(int i=0;i<STATE_NUM;i++){
	  *((REAL *)(mp->states[i]->vp))=stateReserve[i];
	}*/
	int single = 0;
	int matrix = 0;
	for (int i = 0;i < STATE_NUM;i++) {
		switch (mp->states[i]->type) {
		case SINGLE:
			*((REAL*)(mp->states[i]->vp)) = singleStateReserve[num][single++];
			break;
		case MATRIX:
			*((Matrix*)(mp->states[i]->vp)) = matrixStateReserve[num][matrix++];
			break;
		}

	}
}

void storeDerivative(int num) {
	/*
	for(int i=0;i<STATE_NUM;i++){
	  derivativeReserve[num][i]=*((REAL *)(mp->states[i]->dvp));
	}*/

	int single = 0;
	int matrix = 0;
	for (int i = 0;i < STATE_NUM;i++) {
		switch (mp->states[i]->type) {
		case SINGLE:
			singleDerivativeReserve[num][single++] = *((REAL*)(mp->states[i]->dvp));
			break;
		case MATRIX:
			matrixDerivativeReserve[num][matrix++] = *((Matrix*)(mp->states[i]->dvp));
			break;
		}

	}
}

void caculateDerivative(double* weights, int num) {
	/*
	for(int i=0;i<STATE_NUM;i++){
	  *((REAL *)(mp->states[i]->dvp))=0;
	  for(int j=0;j<num;j++){
		*((REAL *)(mp->states[i]->dvp))+=weights[j]*derivativeReserve[j][i];
	  }
	}*/

	int single = 0;
	int matrix = 0;
	for (int i = 0;i < STATE_NUM;i++) {
		switch (mp->states[i]->type) {
		case SINGLE:
			*((REAL*)(mp->states[i]->dvp)) = 0;
			for (int j = 0;j < num;j++) {
				*((REAL*)(mp->states[i]->dvp)) += weights[j] * singleDerivativeReserve[j][single];
			}
			single++;
			break;
		case MATRIX:
			*((Matrix*)(mp->states[i]->dvp)) = Matrix(mp->states[i]->height, mp->states[i]->width);
			for (int j = 0;j < num;j++) {
				*((Matrix*)(mp->states[i]->dvp)) += weights[j] * matrixDerivativeReserve[j][matrix];
			}
			matrix++;
			break;
		}

	}
}

REAL calculateStateDif(int seq1, int seq2) {
	REAL dif = 0;
	for (int i = 0;i < SINGLE_STATE_NUM;i++) {
		REAL difn = fabs(singleStateReserve[seq1][i] - singleStateReserve[seq2][i]);
		if (dif < difn) {
			dif = difn;
		}
	}

	for (int i = 0;i < MATRIX_STATE_NUM;i++) {
		Matrix* pm1 = &(matrixStateReserve[seq1][i]);
		Matrix* pm2 = &(matrixStateReserve[seq2][i]);
		for (int h = 0;h < pm1->rows();h++) {
			for (int w = 0;w < pm1->cols();w++) {
				REAL difn = fabs((*pm1)(h, w) - (*pm2)(h, w));
				if (dif < difn) {
					dif = difn;
				}
			}
		}
	}

	return dif;
}

static long oldSec = 0;

#ifndef _WIN32
void writeInformation() {
	struct timeval tv;
	gettimeofday(&tv, NULL);
	if (tv.tv_sec != oldSec) {
		fwrite(&(mp->time), 1, sizeof(mp->time), stdout);
		fflush(stdout);
		oldSec = tv.tv_sec;
	}

}
#else
void writeInformation() {
    // 获取当前系统时间
    FILETIME ft;
    SYSTEMTIME st;
    __int64 newSec;

    GetSystemTime(&st);
    SystemTimeToFileTime(&st, &ft);
    newSec = (static_cast<uint64_t>(ft.dwHighDateTime) << 32) | ft.dwLowDateTime;

    // 将 FILETIME 转换为秒
    newSec /= 10000000; // 10000000 微秒 = 1 秒

    if (newSec != oldSec) {
        // 假设 mp->time 是一个可以写入的变量
        // 这里需要根据实际情况来定义 mp->time 的类型和如何写入
        // 例如，如果 mp->time 是一个时间戳，可以这样写：
        fwrite(&(mp->time), 1, sizeof(mp->time), stdout);
        fflush(stdout);
        oldSec = newSec;
    }
}

#endif


// 初始化缓冲区
void init_buffer(Buffer *buf, int size, double init_value) {
    buf->times = (double *)calloc(size, sizeof(double));
    buf->values = (double *)calloc(size, sizeof(double));
    buf->size = size;
    buf->head = 0;
    buf->tail = 0;
    buf->count = 0;
	for(int i = 0; i < size; i++){
		buf->values[i] = init_value;
	}
}

// 释放缓冲区内存
void free_buffer(Buffer *buf) {
    free(buf->times);
    free(buf->values);
}

// 插入新数据到缓冲区
void insert_to_buffer(Buffer *buf, double time, double value) {
    if (buf->count == buf->size) {
        // 队列已满，移除队头元素
        buf->head = (buf->head + 1) % buf->size;
        buf->count--;
    }
    buf->times[buf->tail] = time;
    buf->values[buf->tail] = value;
    buf->tail = (buf->tail + 1) % buf->size;
    buf->count++;
}


// 线性插值函数
double linear_interpolation(Buffer *buf, double target_time) {
    if (target_time < 0) {
        // 元素数量不足，返回初始值
        return buf->values[buf->size-1];
    }

    int i = buf->head;
    for (int j = 0; j < buf->count - 1; j++) {
        int next_i = (i + 1) % buf->size;
        if (buf->times[i] <= target_time && target_time < buf->times[next_i]) {
            double t1 = buf->times[i];
            double y1 = buf->values[i];
            double t2 = buf->times[next_i];
            double y2 = buf->values[next_i];
            return y1 + (target_time - t1) * (y2 - y1) / (t2 - t1);
        }
        i = next_i;
    }
    // 如果没有找到合适的区间，返回最后一个值
    return buf->values[i];
}


// 初始化一维插值表
void init_interpolation_table_1d(InterpolationTable1D *table, int size, int extrapolation_strategy) {
    table->size = size;
    table->extrapolation_strategy = extrapolation_strategy;

    // 分配内存
    table->x = (double *)malloc(size * sizeof(double));
    table->y = (double *)malloc(size * sizeof(double));
}

// 释放一维插值表内存
void free_interpolation_table_1d(InterpolationTable1D *table) {
    free(table->x);
    free(table->y);
}

// 一维线性插值函数，包含外插策略
double linear_interpolation_1d(InterpolationTable1D *table, double target_x) {
    int x1, x2;
    double dx;

    // 检查目标点是否在x范围内
    if (target_x < table->x[0]) {
        // 在x范围外
        if (table->extrapolation_strategy == 0) {
            return table->y[0]; // 最近边界值
        } else {
            x1 = 0;
            x2 = 1;
            dx = (target_x - table->x[x1]) / (table->x[x2] - table->x[x1]);
            return table->y[x1] + dx * (table->y[x2] - table->y[x1]); // 线性外插
        }
    } else if (target_x > table->x[table->size - 1]) {
        // 在x范围外
        if (table->extrapolation_strategy == 0) {
            return table->y[table->size - 1]; // 最近边界值
        } else {
            x1 = table->size - 2;
            x2 = table->size - 1;
            dx = (target_x - table->x[x1]) / (table->x[x2] - table->x[x1]);
            return table->y[x1] + dx * (table->y[x2] - table->y[x1]); // 线性外插
        }
    } else {
        // 在x范围内
        for (x1 = 0; x1 < table->size - 1; x1++) {
            if (table->x[x1] <= target_x && target_x <= table->x[x1 + 1]) {
                break;
            }
        }
        x2 = x1 + 1;
        dx = (target_x - table->x[x1]) / (table->x[x2] - table->x[x1]);
    }

    // 线性插值
    return table->y[x1] * (1 - dx) + table->y[x2] * dx;
}

void insert_to_x_table1d(InterpolationTable1D *table, int index, double value)
{
    if (index < 0 || index >= table->size) {
        printf("Error: Index out of bounds.\n");
        return;
    }
    table->x[index] = value;
}
void insert_to_y_table1d(InterpolationTable1D *table, int index, double value)
{
    if (index < 0 || index >= table->size) {
        printf("Error: Index out of bounds.\n");
        return;
    }
    table->y[index] = value;
}


// 初始化二维插值表
void init_interpolation_table(InterpolationTable *table, int width, int height, int extrapolation_strategy) {
    table->width = width;
    table->height = height;
    table->extrapolation_strategy = extrapolation_strategy;

    // 分配内存
    table->x = (double **)malloc(width * sizeof(double *));
    table->y = (double **)malloc(height * sizeof(double *));
    table->z = (double **)malloc(width * sizeof(double *));

    for (int i = 0; i < width; i++) {
        table->x[i] = (double *)malloc(width * sizeof(double));
        table->z[i] = (double *)malloc(height * sizeof(double));
    }

    for (int i = 0; i < height; i++) {
        table->y[i] = (double *)malloc(height * sizeof(double));
    }
}

// 释放二维插值表内存
void free_interpolation_table(InterpolationTable *table) {
    for (int i = 0; i < table->width; i++) {
        free(table->x[i]);
        free(table->z[i]);
    }

    for (int i = 0; i < table->height; i++) {
        free(table->y[i]);
    }

    free(table->x);
    free(table->y);
    free(table->z);
}

// 双线性插值函数，包含外推策略
double bilinear_interpolation(InterpolationTable *table, double target_x, double target_y) {
    int x1, x2, y1, y2;
    double dx, dy;

    // 检查目标点是否在x范围内
    if (target_x < table->x[0][0]) {
        // 在x范围外
        if (table->extrapolation_strategy == 0) {
            x1 = x2 = 0;
            dx = 0.0;
        } else {
            x1 = 0;
            x2 = 1;
            dx = (target_x - table->x[x1][0]) / (table->x[x2][0] - table->x[x1][0]);
        }
    } else if (target_x > table->x[table->width - 1][0]) {
        // 在x范围外
        if (table->extrapolation_strategy == 0) {
            x1 = x2 = table->width - 1;
            dx = 0.0;
        } else {
            x1 = table->width - 2;
            x2 = table->width - 1;
            dx = (target_x - table->x[x1][0]) / (table->x[x2][0] - table->x[x1][0]);
        }
    } else {
        // 在x范围内
        for (x1 = 0; x1 < table->width - 1; x1++) {
            if (table->x[x1][0] <= target_x && target_x <= table->x[x1 + 1][0]) {
                break;
            }
        }
        x2 = x1 + 1;
        dx = (target_x - table->x[x1][0]) / (table->x[x2][0] - table->x[x1][0]);
    }

    // 检查目标点是否在y范围内
    if (target_y < table->y[0][0]) {
        // 在y范围外
        if (table->extrapolation_strategy == 0) {
            y1 = y2 = 0;
            dy = 0.0;
        } else {
            y1 = 0;
            y2 = 1;
            dy = (target_y - table->y[y1][0]) / (table->y[y2][0] - table->y[y1][0]);
        }
    } else if (target_y > table->y[table->height - 1][0]) {
        // 在y范围外
        if (table->extrapolation_strategy == 0) {
            y1 = y2 = table->height - 1;
            dy = 0.0;
        } else {
            y1 = table->height - 2;
            y2 = table->height - 1;
            dy = (target_y - table->y[y1][0]) / (table->y[y2][0] - table->y[y1][0]);
        }
    } else {
        // 在y范围内
        for (y1 = 0; y1 < table->height - 1; y1++) {
            if (table->y[y1][0] <= target_y && target_y <= table->y[y1 + 1][0]) {
                break;
            }
        }
        y2 = y1 + 1;
        dy = (target_y - table->y[y1][0]) / (table->y[y2][0] - table->y[y1][0]);
    }

    // 双线性插值
    double z1 = table->z[x1][y1] * (1 - dx) + table->z[x2][y1] * dx;
    double z2 = table->z[x1][y2] * (1 - dx) + table->z[x2][y2] * dx;
    double z = z1 * (1 - dy) + z2 * dy;

    return z;
}

void insert_to_x_table(InterpolationTable *table, int index, double value) {
    if (index < 0 || index >= table->width) {
        printf("Error: Index out of bounds.\n");
        return;
    }
    table->x[index][0] = value;
}

void insert_to_y_table(InterpolationTable *table, int index, double value) {
    if (index < 0 || index >= table->height) {
        printf("Error: Index out of bounds.\n");
        return;
    }
    table->y[index][0] = value;
}

void insert_to_z_table(InterpolationTable *table, int x_index, int y_index, double value) {
    if (x_index < 0 || x_index >= table->width || y_index < 0 || y_index >= table->height) {
        printf("Error: Index out of bounds.\n");
        return;
    }
    table->z[x_index][y_index] = value;
}
