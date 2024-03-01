#include <iostream>
#include <cmath>

#include "ncslabdefines.hpp"
#include "util.hpp"
#include "mainccode.hpp"
#include "onestep.hpp"

extern MODEL *mp;

//double stateReserve[STATE_NUM];

double singleStateReserve[SINGLE_STATE_NUM];
Matrix matrixStateReserve[MATRIX_STATE_NUM];

extern double sample_time[];

//double derivativeReserve[3][STATE_NUM];
double singleDerivativeReserve[2][SINGLE_STATE_NUM];
Matrix matrixDerivativeReserve[2][MATRIX_STATE_NUM];

extern double real_sample_time;

#ifdef _SIMU
void ncslabLoop(){
	discreteInitFixed();
  	
	while(mp->time<mp->stopTime){
        //mp->time+=mp->stepSize;
        //fwrite(&(mp->time),1,sizeof(mp->time),stdout);
        writeInformation();
        NCSLabOneStep();
       mp->time+=mp->stepSize;
        //printf("time:%f\n",mp->time);
    }
}
#endif

void NCSLabOneStep(){

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
  mp->majorStep=0;
  //Calculate K1
  storeState(0);
  mp->stepSize=STEP_SIZE/2;
	NCSLabUpdate();
  mp->offset=STEP_SIZE/2;
  NCSLabOutput();
  NCSLabDerivative();

  //Update
  restoreState(0);
  mp->stepSize=STEP_SIZE;
  NCSLabUpdate();
}
