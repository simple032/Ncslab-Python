#include <iostream>
#include <cmath>
#include <cstdio>
#ifndef __WIN32
#include <sys/time.h>
#else
#include <windows.h>
#endif // __WIN32
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
	// Handle negative or zero sample times (inherited/auto sample time)
	// Negative sample times like -1 indicate "inherited" timing
	if (x <= 0 || y <= 0) {
		// Return the positive one, or 0 if both are non-positive
		if (x > 0) return x;
		if (y > 0) return y;
		return 0;
	}

	int a = (int)(x * 1000);
	int b = (int)(y * 1000);

	// Handle edge cases
	if (a == 0) return y;
	if (b == 0) return x;

	int result;
	// CRITICAL FIX: Loop must stop at 1, not 0, to avoid division by zero
	for (result = (a < b ? a : b); result > 0; result--)
	{
		if (0 == a % result && 0 == b % result)
			return result / 1000.0;
	}
	return 0.001; // Return 1ms as fallback instead of 0
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

#ifndef __WIN32
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
