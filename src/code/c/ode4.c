#include"ncslabccode.h"
#include"ServerThread.h"
#include"ncslab.h"

#include <iostream>
#include <octave/oct.h>

extern MODEL *mp;

//double stateReserve[STATE_NUM];

double singleStateReserve[SINGLE_STATE_NUM];
Matrix matrixStateReserve[MATRIX_STATE_NUM];

//double derivativeReserve[4][STATE_NUM];

double singleDerivativeReserve[4][SINGLE_STATE_NUM];
Matrix matrixDerivativeReserve[4][MATRIX_STATE_NUM];

double weight1[4]={1.0/6, 2.0/6, 2.0/6, 1.0/6};

#ifdef _SIMU
void ncslabLoop(){
	while(mp->time<mp->stopTime){
        //mp->time+=mp->stepSize;
        fwrite(&(mp->time),1,sizeof(mp->time),stdout);
        NCSLabOneStep();
       mp->time+=mp->stepSize;
        //printf("time:%f\n",mp->time);
    }
}
#endif


void NCSLabOneStep(){

  mp->offset=0;
  mp->majorStep=1;
  NCSLabOutput();

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
}
