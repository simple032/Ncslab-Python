#include"ncslabccode.h"
#include"ServerThread.h"
#include"ncslab.h"

#include <iostream>
//#include <octave/oct.h>
#include "math.h"
#include "Matrix.h"

extern MODEL *mp;

//double stateReserve[STATE_NUM];

extern double sample_time[];

double singleStateReserve[SINGLE_STATE_NUM];
Matrix matrixStateReserve[MATRIX_STATE_NUM];

//double derivativeReserve[4][STATE_NUM];

double singleDerivativeReserve[4][SINGLE_STATE_NUM];
Matrix matrixDerivativeReserve[4][MATRIX_STATE_NUM];

double weight1[4]={1.0/6, 2.0/6, 2.0/6, 1.0/6};

void NCSLabOneStep4(double);

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
        NCSLabOneStep4(real_sample_time);
       	mp->time+=mp->stepSize;
        //printf("time:%f\n",mp->time);
    }
}
#endif


void NCSLabOneStep4(double real_sample_time){

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

  //Calculate K0
	NCSLabDerivative();
  storeDerivative(0);
  mp->majorStep=0;
  //Calculate K1
  storeState(0);
  mp->stepSize=STEP_SIZE/2;
	NCSLabUpdate();
  mp->offset=STEP_SIZE/2;
  NCSLabOutput();
  NCSLabDerivative();
  storeDerivative(1);

  //Calculate K2
  restoreState(0);
  mp->stepSize=STEP_SIZE/2;
  NCSLabUpdate();
  mp->offset=STEP_SIZE/2;
  NCSLabOutput();
  NCSLabDerivative();
  storeDerivative(2);

  //Calculate K3
  restoreState(0);
  mp->stepSize=STEP_SIZE;
  NCSLabUpdate();
  mp->offset=STEP_SIZE;
  NCSLabOutput();
  NCSLabDerivative();
  storeDerivative(3);

  //Update
  restoreState(0);
  mp->stepSize=STEP_SIZE;
  caculateDerivative(weight1,4);
  NCSLabUpdate();
  
  //mp->time+=mp->stepSize;
  
  
  
}
