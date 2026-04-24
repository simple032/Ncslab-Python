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

#include <gsl/gsl_linalg.h>
//#include <octave/oct.h>

extern MODEL* mp;
extern TERMINAL* terminals[];


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
		}else if(a[i] < 0){
            d = STEP_SIZE;
            continue;
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

/*
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

#endif*/

void writeInformation(){
#ifdef _WIN32_WINNT
	struct timeval tv;
	PROGRESSTYPE progressType=Simulating;
	DWORD sec=(GetTickCount()/1000)%60;
	//gettimeofday(&tv,NULL);
	if(sec!=oldSec){
		fputc(0x55,stdout);
		fputc(0x55,stdout);
		fwrite(&progressType,1,sizeof(progressType),stdout);
		printf("%f\n",mp->time);
		fflush(stdout);

		// ===== Display 模块实时数据输出 =====
		// 遍历所有 Terminal，找出 Display 模块（maxDataLength == 1）
		// 通过 stdout 发送 Display 的 UUID 和当前值到 Java 后端
		int displayCount = 0;
		for (int i = 0; i < mp->terminalNum; i++) {
			TERMINAL* terminal = terminals[i];
			SCOPE* scope = (SCOPE*)terminal->terminal;
			if (scope->maxDataLength == 1) {
				displayCount++;
			}
		}
		if (displayCount > 0) {
			PROGRESSTYPE displayType = DisplayUpdate;
			fputc(0x55, stdout);
			fputc(0x55, stdout);
			fwrite(&displayType, 1, sizeof(displayType), stdout);
			fwrite(&displayCount, 1, sizeof(displayCount), stdout);
			for (int i = 0; i < mp->terminalNum; i++) {
				TERMINAL* terminal = terminals[i];
				SCOPE* scope = (SCOPE*)terminal->terminal;
				if (scope->maxDataLength == 1) {
					int uuidLen = strlen(scope->uuid);
					fwrite(&uuidLen, 1, sizeof(uuidLen), stdout);
					fwrite(scope->uuid, 1, uuidLen, stdout);
					double value = scope->dataList.empty() ? 0.0 : scope->dataList.back();
					fwrite(&value, 1, sizeof(value), stdout);
				}
			}
			fflush(stdout);
		}
		// ===== Display 模块实时数据输出结束 =====

		oldSec=sec;
	}
#else
	struct timeval tv;
	PROGRESSTYPE progressType=Simulating;
	gettimeofday(&tv,NULL);
	if(tv.tv_sec!=oldSec){
		fputc(0x55,stdout);
		fputc(0x55,stdout);
		fwrite(&progressType,1,sizeof(progressType),stdout);
		printf("%f\n",mp->time);
		fflush(stdout); 
		oldSec=tv.tv_sec;
	}
#endif	
}

/**
 * Force send Display block update without time-based throttling.
 * Used to send initial and final display values at simulation start/end.
 */
void sendDisplayUpdateForce() {
	int displayCount = 0;
	for (int i = 0; i < mp->terminalNum; i++) {
		TERMINAL* terminal = terminals[i];
		SCOPE* scope = (SCOPE*)terminal->terminal;
		if (scope->maxDataLength == 1) {
			displayCount++;
		}
	}
	if (displayCount > 0) {
		PROGRESSTYPE displayType = DisplayUpdate;
		fputc(0x55, stdout);
		fputc(0x55, stdout);
		fwrite(&displayType, 1, sizeof(displayType), stdout);
		fwrite(&displayCount, 1, sizeof(displayCount), stdout);
		for (int i = 0; i < mp->terminalNum; i++) {
			TERMINAL* terminal = terminals[i];
			SCOPE* scope = (SCOPE*)terminal->terminal;
			if (scope->maxDataLength == 1) {
				int uuidLen = strlen(scope->uuid);
				fwrite(&uuidLen, 1, sizeof(uuidLen), stdout);
				fwrite(scope->uuid, 1, uuidLen, stdout);
				double value = scope->dataList.empty() ? 0.0 : scope->dataList.back();
				fwrite(&value, 1, sizeof(value), stdout);
			}
		}
		fflush(stdout);
	}
}

/**
 * Send real-time Display + Scope data update to Java backend via stdout.
 * Called every 10000 steps or every 1 second during real-time simulation.
 * Each scope sends at most 10000 new data points (latest points if exceeded).
 */
void sendRealtimeDataUpdate() {
	if (mp->terminalNum <= 0) return;

	int displayCount = 0;
	int scopeCount = 0;
	for (int i = 0; i < mp->terminalNum; i++) {
		TERMINAL* terminal = terminals[i];
		if (terminal->type != Scope) continue;
		SCOPE* scope = (SCOPE*)terminal->terminal;
		if (scope->maxDataLength == 1) {
			displayCount++;
		} else {
			scopeCount++;
		}
	}
	if (displayCount == 0 && scopeCount == 0) return;

	PROGRESSTYPE type = RealtimeDataUpdate;
	fputc(0x55, stdout);
	fputc(0x55, stdout);
	fwrite(&type, 1, sizeof(type), stdout);

	fwrite(&displayCount, 1, sizeof(displayCount), stdout);
	for (int i = 0; i < mp->terminalNum; i++) {
		TERMINAL* terminal = terminals[i];
		if (terminal->type != Scope) continue;
		SCOPE* scope = (SCOPE*)terminal->terminal;
		if (scope->maxDataLength == 1) {
			int uuidLen = strlen(scope->uuid);
			fwrite(&uuidLen, 1, sizeof(uuidLen), stdout);
			fwrite(scope->uuid, 1, uuidLen, stdout);
			double value = scope->dataList.empty() ? 0.0 : scope->dataList.back();
			fwrite(&value, 1, sizeof(value), stdout);
		}
	}

	fwrite(&scopeCount, 1, sizeof(scopeCount), stdout);
	for (int i = 0; i < mp->terminalNum; i++) {
		TERMINAL* terminal = terminals[i];
		if (terminal->type != Scope) continue;
		SCOPE* scope = (SCOPE*)terminal->terminal;
		if (scope->maxDataLength == 1) continue;

		int uuidLen = strlen(scope->uuid);
		fwrite(&uuidLen, 1, sizeof(uuidLen), stdout);
		fwrite(scope->uuid, 1, uuidLen, stdout);
		fwrite(&(scope->width), 1, sizeof(scope->width), stdout);
		fwrite(&(scope->height), 1, sizeof(scope->height), stdout);

		int totalSize = (int)scope->timeList.size();
		int newPoints = totalSize - scope->sentCount;
		if (newPoints < 0) {
			scope->sentCount = 0;
			newPoints = totalSize;
		}
		if (newPoints > 10000) {
			scope->sentCount = totalSize - 10000;
			newPoints = 10000;
		}
		fwrite(&newPoints, 1, sizeof(newPoints), stdout);

		size_t timeIdx = scope->sentCount;
		size_t dataIdx = (size_t)scope->sentCount * scope->width * scope->height;
		size_t wh = scope->width * scope->height;

		for (int p = 0; p < newPoints && timeIdx < scope->timeList.size(); p++) {
			REAL t = scope->timeList[timeIdx];
			fwrite(&t, 1, sizeof(t), stdout);
			timeIdx++;
			for (int h = 0; h < scope->height; h++) {
				for (int w = 0; w < scope->width; w++) {
					if (dataIdx < scope->dataList.size()) {
						REAL val = scope->dataList[dataIdx];
						fwrite(&val, 1, sizeof(val), stdout);
						dataIdx++;
					} else {
						REAL zero = 0.0;
						fwrite(&zero, 1, sizeof(zero), stdout);
					}
				}
			}
		}

		scope->sentCount = totalSize;
	}

	fwrite(&(mp->time), 1, sizeof(mp->time), stdout);
	fflush(stdout);
}

/**
 * Send Stateflow state change update to Java backend via stdout.
 * Called from generated C code when a state transition occurs.
 */
void sendStateflowStateUpdate(const char* chartUUID, const char* stateId, const char* stateName) {
	if (chartUUID == NULL || stateId == NULL) return;
	
	PROGRESSTYPE type = StateflowStateUpdate;
	fputc(0x55, stdout);
	fputc(0x55, stdout);
	fwrite(&type, 1, sizeof(type), stdout);
	
	int chartUuidLen = strlen(chartUUID);
	fwrite(&chartUuidLen, 1, sizeof(chartUuidLen), stdout);
	fwrite(chartUUID, 1, chartUuidLen, stdout);
	
	int stateIdLen = strlen(stateId);
	fwrite(&stateIdLen, 1, sizeof(stateIdLen), stdout);
	fwrite(stateId, 1, stateIdLen, stdout);
	
	int stateNameLen = stateName ? strlen(stateName) : 0;
	fwrite(&stateNameLen, 1, sizeof(stateNameLen), stdout);
	if (stateNameLen > 0) {
		fwrite(stateName, 1, stateNameLen, stdout);
	}
	
	fflush(stdout);
}

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

void writeBuf(unsigned char *buf,int size){
	for(int i=0;i<size;i++){
		fputc(buf[i],stdout);
		//fflush(stdout); 
	}
}

void writeSavingInformation(int currentTerminal,int terminalNum){
	PROGRESSTYPE progressType=Saving;
	/*
	int n=0;
	while(n<sizeof(progressType)){
		char *pos=(char *)(&progressType);
		n+=fwrite(pos+n,1,sizeof(progressType)-n,stdout);	
	}
	
	n=0;
	while(n<sizeof(currentTerminal)){
		char *pos=(char *)(&currentTerminal);
		n+=fwrite(&currentTerminal,1,sizeof(currentTerminal)-n,stdout);
	}
	
	n=0;
	while(n<sizeof(terminalNum)){
		char *pos=(char *)(&terminalNum);
		n+=fwrite(&terminalNum,1,sizeof(terminalNum)-n,stdout);
	}*/
	
	fputc(0x55,stdout);
	fputc(0x55,stdout);
	
	writeBuf((unsigned char *)(&progressType),sizeof(progressType));
	writeBuf((unsigned char *)(&currentTerminal),sizeof(currentTerminal));
	writeBuf((unsigned char *)(&terminalNum),sizeof(terminalNum));
	fflush(stdout);
}

void copyCircuitMartrix(REAL *gAA,REAL *gAAc,REAL *iA,REAL *iAc,int size){
	memcpy(gAA,gAAc,sizeof(REAL)*size*size);
	memcpy(iA,iAc,sizeof(REAL)*size);
}

void copyCircuitVector(REAL *iA,REAL *iAc,int size){
	memcpy(iA,iAc,sizeof(REAL)*size);
}

bool isRef(int n,int* ref,int refSize){
	for(int i = 0;i<refSize;++i){
		if(n == ref[i])
			return true;
	}
	return false;
}
/*用来进行电路开关的矩阵合并*/
/*Vindex记录了原始节点与合并之后节点的对应关系*/
/*size记录了合并前的矩阵大小，合并之后size变化，需要通过指针回传*/
/*ref记录了参考节点的位置*/
/*n和m记录了要合并的两个节点*/
/**/
void CircuitCombine(REAL *gAA,REAL *iA,int *vIndex,int *size_p,int* ref,int n,int m,int indexSize,int refSize){
	int size=*size_p;
	/*
	for(int i=0;i<size;i++){
		for(int j=0;j<size;j++){
			printf("%f\t",gAA[i*size+j]);
		}
		printf("\n");
	}
	printf("\n");
	for(int i=0;i<size;i++){
		printf("%f\t",iA[i]);
	}
	printf("\n\n");

	printf("%d\t%d\n",n,m);*/

	//如果合并的节点中有参考节点，电压方程由参考节点决定，那么就只需要消去非参考节点的行列即可
	if(isRef(n,ref,refSize)||isRef(m,ref,refSize)){
		int nRef,nn;
		REAL gAAc[size][size];
		//复制原始数据到二维数组，便于进行计算，以后可以优化掉
		for(int i=0;i<size;i++){
			for(int j=0;j<size;j++){
				gAAc[i][j]=gAA[i*size+j];
			}
		}

		//让n成为消去的节点
		int refNode = m;
		if(isRef(n,ref,refSize)){
			refNode=n;
			n=m;
		}

		//nn是变换后的消去节点
		nn=vIndex[n];
		nRef=vIndex[refNode];
		
		//让消去的节点指向参考节点
		vIndex[n]=vIndex[refNode];
		
		//printf("%d\t%d\n",nRef,nn);

		//分别消去nn的节点行与列
		for(int i=nn;i<size-1;i++){
			for(int j=0;j<size;j++){
				gAAc[i][j]=gAAc[i+1][j];
			}
		}
		for(int i=nn;i<size-1;i++){
			for(int j=0;j<size-1;j++){
				gAAc[j][i]=gAAc[j][i+1];
			}
		}
		
		//消去nn在iA中的节点
		for(int i=nn;i<size-1;i++){
			iA[i]=iA[i+1];
		}
		
		/*
		for(int i=nn+1;i<size;i++){
			for(int j=0;j<size;j++){
				if(vIndex[j]==i){
					vIndex[j]-=1;
				}
			}
		}*/
		
		//重新更新vIndex，让nn后面的节点都减一
		for(int i=0;i<indexSize;i++){
			if(vIndex[i]>nn){
				vIndex[i]-=1;
			}
		}

		//矩阵的大小减一
		size--;
		//重新放回到一维数组中
		for(int i=0;i<size;i++){
			for(int j=0;j<size;j++){
				gAA[i*size+j]=gAAc[i][j];
			}
		}
	}
	else{
		//解析合并的两个点在新矩阵中的位置
		int nn=vIndex[n];
		int mm=vIndex[m];
		//如果不是一个点，才需要合并，如果是一个点，就忽略
		if(nn!=mm){
			REAL gAAc[size][size];
			for(int i=0;i<size;i++){
				for(int j=0;j<size;j++){
					gAAc[i][j]=gAA[i*size+j];
				}
			}
			//重新排布，让nn在前，mm在后
			if(nn>mm){
				int temp=nn,temp1=n;
				nn=mm;
				n=m;
				mm=temp;
				m=temp1;
			}
			//printf("%d\t%d\n",nn,mm);

			//分别把mm的行与列合并到nn里面去
			for(int i=0;i<size;i++){
				gAAc[nn][i]+=gAAc[mm][i];
			}
			for(int i=0;i<size;i++){
				gAAc[i][nn]+=gAAc[i][mm];
			}
			//iA也进行相应的合并
			iA[nn]+=iA[mm];

			//合并之后，要把mm后面的行和列依次向前
			for(int i=mm;i<size-1;i++){
				for(int j=0;j<size;j++){
					gAAc[i][j]=gAAc[i+1][j];
				}
			}
			for(int i=mm;i<size-1;i++){
				for(int j=0;j<size-1;j++){
					gAAc[j][i]=gAAc[j][i+1];
				}
			}
			//iA也是要把mm后面的元素依次向前
			for(int i=mm;i<size-1;i++){
				iA[i]=iA[i+1];
			}
			/*
			for(int i=0;i<size;i++){
				printf("%d\t",vIndex[i]);
			}
			printf("\n");*/

			//m的索引合并到n里面
			vIndex[m]=vIndex[n];
			
			/*
			for(int i=0;i<size;i++){
				printf("%d\t",vIndex[i]);
			}
			printf("\n");*/
			
			/*
			for(int i=mm+1;i<size;i++){
				for(int j=0;j<size;j++){
					if(vIndex[j]==i){
						vIndex[j]-=1;
					}
				}
			}*/

			//重新更新vIndex，让mm后面的节点都减一
			for(int i=0;i<indexSize;i++){
				if(vIndex[i]>mm){
					vIndex[i]-=1;
				}
			}

			//矩阵的大小减一
			size--;
			//重新放回到一维数组中
			for(int i=0;i<size;i++){
				for(int j=0;j<size;j++){
					gAA[i*size+j]=gAAc[i][j];
				}
			}
			/*
			for(int i=0;i<size;i++){
				printf("%d\t",vIndex[i]);
			}
			printf("\n");*/
			
		}
	
		
	}
	/*
	for(int i=0;i<size;i++){
		for(int j=0;j<size;j++){
			printf("%f\t",gAA[i*size+j]);
		}
		printf("\n");
	}
	printf("\n");
	for(int i=0;i<size;i++){
		printf("%f\t",iA[i]);
	}
	printf("\n\n");*/
	//exit(0);

	*size_p=size;
}


//电路仿真方面的函数

/*计算一种组合的Inv矩阵*/
//SwitchGAA *psGaa指向SwitchGaa表的指针
//REAL *gAA, 经过开关状态合并之后的gAA矩阵
//int switchNum 开关的个数
//int size,经过开关状态合并之后的gAA矩阵大小
//uint32_T *switchStatus，指向现在开关状态的指针
gsl_matrix *addSwitchCombine(SwitchGAA *psGaa,REAL *gAA,int switchNum,int size,uint32_T *switchStatus){
	int pos;
	
	//SwitchGAA表的大小加一
	psGaa->storeGAASize++;
	//添加的Inv矩阵表格的位置Pos
	pos=psGaa->storeGAASize-1;
	//按照增加的SwitchGAA大小，从新分配内存
	psGaa->storeGAA=(StoreGAA *)realloc(psGaa->storeGAA,sizeof(StoreGAA)*psGaa->storeGAASize);
	//为新的inv表格对应的开关状态表分配内存，并且在数据结构中复制一份
	psGaa->storeGAA[pos].switchStatus=(uint32_T *)malloc((switchNum/32+1)*sizeof(uint32_T));
	memcpy(psGaa->storeGAA[pos].switchStatus,switchStatus,(switchNum/32+1)*sizeof(uint32_T));
	
	//Inv表格中记录矩阵大小
	psGaa->storeGAA[pos].size=size;
	//都是根据最新参数进行的计算，因此有无参数改变的标志为0
	psGaa->storeGAA[pos].isVariableChanged=0;
	//psGaa->storeGAA[pos].rAA=(REAL *)malloc(sizeof(REAL)*size*size);
	
	/*
	printf("%d\t",psGaa->storeGAASize);
	for(int i=0;i<psGaa->switchNum/32+1;i++){
		printf("swtich Status:%4x\t",switchStatus[i]);
	}
	
	printf("size:%d\tSwitchNum:%d\n",size,psGaa->switchNum);
	*/
	
	//gsl_matrix_view A = gsl_matrix_view_array(gAA,size, size);
	
	//计算逆阵inv
	gsl_matrix * inv = gsl_matrix_alloc(size,size);
    gsl_permutation * p = gsl_permutation_alloc(size);
    
    int signum=0;
    
    gsl_matrix *matrixA=gsl_matrix_alloc(size,size);
    for(size_t i=0;i<size;++i){
        for(size_t j=0;j<size;++j){
			gsl_matrix_set(matrixA,i,j,gAA[i*size+j]);
		}
    }

    gsl_linalg_LU_decomp(matrixA, p, &signum);
    
    gsl_linalg_LU_invert(matrixA, p, inv);
    
    //在表格中保存逆阵inv
    psGaa->storeGAA[pos].inv=inv;
    
    /*
    for(size_t i=0;i<size;++i){
        for(size_t j=0;j<size;++j){
			printf("%8.4f", gsl_matrix_get(inv,i,j));
		}
		putchar('\n');
    }*/
    
    gsl_matrix_free(matrixA);
    gsl_permutation_free(p);
    
    //返回计算的逆阵指针，可以用来进行状态计算
    return inv;
    
}

//如果有无参数改变的标志为1,说明矩阵中有些参数发生变化，因此需要重新计算
//StoreGAA *pStoreGaa，inv矩阵表项的指针
//REAL *gAA, 经过开关状态合并之后的gAA矩阵
//int size,经过开关状态合并之后的gAA矩阵大小
gsl_matrix *caclulateInv(StoreGAA *pStoreGaa,REAL *gAA,int size){
	//释放原有的inv矩阵
	gsl_matrix_free(pStoreGaa->inv);
	
	//根据Gaa中的数值，计算新的Inv矩阵
	gsl_matrix * inv = gsl_matrix_alloc(size,size);
    gsl_permutation * p = gsl_permutation_alloc(size);
    
    int signum=0;
    
    gsl_matrix *matrixA=gsl_matrix_alloc(size,size);
    for(size_t i=0;i<size;++i){
        for(size_t j=0;j<size;++j){
			gsl_matrix_set(matrixA,i,j,gAA[i*size+j]);
		}
    }

    gsl_linalg_LU_decomp(matrixA, p, &signum);
    
    gsl_linalg_LU_invert(matrixA, p, inv);
    
    //保存计算的inv矩阵
    pStoreGaa->inv=inv;
    
    //返回inv矩阵指针
    return inv;
}

//设定指定位置的开关状态
//pos，开关的编号
//status 开关状态
void setSwitchStatus(SwitchGAA *psGaa,int pos,int status){
	int word=pos/32;
	int bit=pos%32;
	if(status){
		psGaa->switchStatus[word]|=(0x01)<<bit;	
	}
	else{
		psGaa->switchStatus[word]&=(~(0x01<<bit));		
	}
	//printf("%d\t%d\n",pos,status);
	//printf("%x\n",psGaa->switchStatus[0]);
}

//匹配开关状态表，寻找匹配的inv矩阵，如果找到，就返回找到矩阵，如果没有，就返回NULL
StoreGAA *findStoreGAA(SwitchGAA *psGaa,uint32_T *switchStatus){
	for(int i=0;i<psGaa->storeGAASize;i++){
		int same=1;
		for(int j=0;j<psGaa->switchNum/32+1;j++){
			if(psGaa->storeGAA[i].switchStatus[j]!=switchStatus[j]){
				same=0;
			}
		}
		if(same){
			return &(psGaa->storeGAA[i]);
		}
	}
	return NULL;
}

//获取pos位置的开关状态
int getSwtichStatus(SwitchGAA *psGaa,int pos){
	int word=pos/32;
	int bit=pos%32;
	
	return (psGaa->switchStatus[word])&(0x01<<bit);
}


//与circuitCombine类似的功能，但是只合并iA和索引表
void CircuitCombineIA(REAL *iA,int *vIndex,int *size_p,int* ref,int n,int m,int indexSize,int refSize){
	int size=*size_p;

	//如果合并的节点中有参考节点，电压方程由参考节点决定，那么就只需要消去非参考节点的行列即可
	if(isRef(n,ref,refSize)||isRef(m,ref,refSize)){
		int nRef,nn;
		

		//让n成为消去的节点
		int refNode = m;
		if(isRef(n,ref,refSize)){
			refNode=n;
			n=m;
		}

		//nn是变换后的消去节点
		nn=vIndex[n];
		nRef=vIndex[refNode];
		
		//让消去的节点指向参考节点
		vIndex[n]=vIndex[refNode];
		
		//消去nn在iA中的节点
		for(int i=nn;i<size-1;i++){
			iA[i]=iA[i+1];
		}
				
		//重新更新vIndex，让nn后面的节点都减一
		for(int i=0;i<indexSize;i++){
			if(vIndex[i]>nn){
				vIndex[i]-=1;
			}
		}

		//矩阵的大小减一
		size--;
	}
	else{
		//解析合并的两个点在新矩阵中的位置
		int nn=vIndex[n];
		int mm=vIndex[m];
		//如果不是一个点，才需要合并，如果是一个点，就忽略
		if(nn!=mm){
			
			//重新排布，让nn在前，mm在后
			if(nn>mm){
				int temp=nn,temp1=n;
				nn=mm;
				n=m;
				mm=temp;
				m=temp1;
			}
			//printf("%d\t%d\n",nn,mm);

			
			//iA也进行相应的合并
			iA[nn]+=iA[mm];

			
			//iA也是要把mm后面的元素依次向前
			for(int i=mm;i<size-1;i++){
				iA[i]=iA[i+1];
			}
			/*
			for(int i=0;i<size;i++){
				printf("%d\t",vIndex[i]);
			}
			printf("\n");*/

			//m的索引合并到n里面
			vIndex[m]=vIndex[n];
			
			

			//重新更新vIndex，让mm后面的节点都减一
			for(int i=0;i<indexSize;i++){
				if(vIndex[i]>mm){
					vIndex[i]-=1;
				}
			}

			//矩阵的大小减一
			size--;
			
		}
	
		
	}

	*size_p=size;
}

int isPartRef(int nodeId,int *ref,int refSize,int *vIndex){

	for(int i=0;i<refSize;i++){
		if(vIndex[ref[i]]==vIndex[nodeId]){
			return 1;
		}
	}

	return 0;
}

#include <fstream>
#include "nlohmann/json.hpp"
using json = nlohmann::json;

/**
 * Check for parameter update file written by Java backend and apply changes.
 * Called periodically during real-time simulation (before each data send).
 */
void checkParameterUpdates() {
	std::ifstream file("param_updates.json");
	if (!file.is_open()) return;

	try {
		json j;
		file >> j;
		file.close();

		// Delete file immediately to avoid re-applying same updates
		std::remove("param_updates.json");

		if (!j.contains("updates") || !j["updates"].is_array()) return;

		for (const auto& update : j["updates"]) {
			std::string blockPath = update.value("blockPath", "");
			std::string blockName = update.value("blockName", "");
			std::string paramName = update.value("paramName", "");
			double value = update.value("value", 0.0);

			if (paramName.empty()) continue;

			// Helper: extract last path segment (blockName) from a full path like "hashName/Subsys/BlockName"
			auto getLastSegment = [](const std::string& path) -> std::string {
				size_t pos = path.rfind('/');
				return (pos == std::string::npos) ? path : path.substr(pos + 1);
			};

			std::string targetBlockName = blockName.empty() ? getLastSegment(blockPath) : blockName;
			if (targetBlockName.empty()) continue;

			// Match against all parameters in the model
			for (int i = 0; i < mp->parameterNum; i++) {
				PARAMETER* param = mp->parameters[i];
				if (!param || !param->name || !param->path || !param->vp) continue;

				std::string paramBlockName = getLastSegment(param->path);
				bool blockMatches = paramBlockName == targetBlockName;
				bool nameMatches = strcmp(param->name, paramName.c_str()) == 0;

				if (blockMatches && nameMatches) {
					if (param->type == SINGLE) {
						*((REAL*)param->vp) = (REAL)value;
					}
					// MATRIX type updates not supported for real-time tuning
					break;
				}
			}
		}
	} catch (...) {
		// Parse failed or other error - clean up file if it still exists
		std::remove("param_updates.json");
	}
}
