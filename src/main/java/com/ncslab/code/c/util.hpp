#ifndef UTIL_HPP
#define UTIL_HPP

#include "ncslabdefines.hpp"

extern double real_sample_time;

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

#endif
