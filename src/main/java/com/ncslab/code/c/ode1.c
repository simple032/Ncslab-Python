#include"ncslabccode.h"
#include"ncslab.h"

#include <iostream>
//#include <octave/oct.h>
#include "math.h"
#include "Matrix.h"

extern MODEL *mp;

double *stateReserve;
double **derivativeReserve;

extern double sample_time[];

double singleStateReserve[SINGLE_STATE_NUM];
Matrix matrixStateReserve[MATRIX_STATE_NUM];

double singleDerivativeReserve[1][SINGLE_STATE_NUM];
Matrix matrixDerivativeReserve[1][MATRIX_STATE_NUM];

void NCSLabOneStep1(double);

#ifdef _SIMU
void ncslabLoop(){
	//xiazhiqiang:Evaluates the greatest common divisor by referencing the sampling time array:(The definition location is at line 31 of codeStructC)
    extern int sample_i;
    //extern double sample_time[];
    double real_sample_time=0.0;
    if(hasdiscrete(sample_time)==1){
      real_sample_time=gcd1(sample_time);
    }
    //end
  	mp->discreteUpdate=1;

	while(mp->time<mp->stopTime){
        //mp->time+=mp->stepSize;
        //fwrite(&(mp->time),1,sizeof(mp->time),stdout);
        writeInformation();
        NCSLabOneStep1(real_sample_time);
       mp->time+=mp->stepSize;
        //printf("time:%f\n",mp->time);
    }
}
#endif

void NCSLabOneStep1(double real_sample_time){
	mp->offset=0;
	
	if (hasdiscrete(sample_time)){

    while (mp->discreteTime <= mp->time){
      mp->discreteTime += real_sample_time;
      // mp->offset = 0;
      // NCSLabOutput();
      // NCSLabDiscreteUpdate();
      mp->discreteUpdate = 1;
      }
    }
	
	mp->majorStep=1;
	NCSLabOutput();
	
	if(mp->discreteUpdate){
  	
  		NCSLabDiscreteUpdate();
  		mp->discreteUpdate=0;
  	}
  	NCSLabSinkOutput();
	
	NCSLabDerivative();
	NCSLabUpdate();
	mp->majorStep=0;
}
