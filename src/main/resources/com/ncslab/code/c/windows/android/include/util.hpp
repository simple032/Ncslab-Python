#ifndef UTIL_HPP
#define UTIL_HPP

#include "ncslabdefines.hpp"
#include "ncslabccode.hpp"

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
