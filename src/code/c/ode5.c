#include"ncslabccode.h"
#include"ServerThread.h"
#include"ncslab.h"

#include <iostream>
//#include <octave/oct.h>
#include "math.h"
#include "Matrix.h"
extern MODEL *mp;

//double stateReserve[STATE_NUM];

double singleStateReserve[SINGLE_STATE_NUM];
Matrix matrixStateReserve[MATRIX_STATE_NUM];

//double derivativeReserve[4][STATE_NUM];

double singleDerivativeReserve[6][SINGLE_STATE_NUM];
Matrix matrixDerivativeReserve[6][MATRIX_STATE_NUM];

double weight1[]={1.0/24.0, 0.0, 0.0, 5.0/48.0, 27.0/56.0, 125.0/336.0};

double weights3[] = {1.0 / 2.0, 1.0 / 2.0};
double weights4[] = {0.0, -1.0, 2.0};
double weights5[] = {7.0 / 18.0, 5.0 / 9.0, 0.0, 1.0 / 18.0};
double weights6[] = {28.0 / 125.0 , -1.0 , 546.0 / 125.0 , 54.0 / 125.0 , -378.0 / 125.0};

#ifdef _SIMU
void ncslabLoop(){
	while(mp->time<mp->stopTime){
        //mp->time+=mp->stepSize;
        writeInformation();
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

  //Calculate K1
  NCSLabDerivative();
  storeDerivative(0);
  mp->majorStep=0;
  //Calculate K2
  storeState(0);
  mp->stepSize=STEP_SIZE/2;
  NCSLabUpdate();
  mp->offset=STEP_SIZE/2;
  NCSLabOutput();
  NCSLabDerivative();
  storeDerivative(1);

  //Calculate K3
  restoreState(0);
  mp->stepSize=STEP_SIZE/2;
  caculateDerivative(weights3, 2);
  NCSLabUpdate();
  mp->offset=STEP_SIZE/2;
  NCSLabOutput();
  NCSLabDerivative();
  storeDerivative(2);

  //Calculate K4
  restoreState(0);
  mp->stepSize=STEP_SIZE;
  caculateDerivative(weights4, 3);
  NCSLabUpdate();
  mp->offset=STEP_SIZE;
  NCSLabOutput();
  NCSLabDerivative();
  storeDerivative(3);

  //Calculate K5
  restoreState(0);
  mp->stepSize=STEP_SIZE*2.0/3.0;
  caculateDerivative(weights5, 4);
  NCSLabUpdate();
  mp->offset=STEP_SIZE*2.0/3.0;
  NCSLabOutput();
  NCSLabDerivative();
  storeDerivative(4);

  //Calculate K6
  restoreState(0);
  mp->stepSize=STEP_SIZE*1.0/5.0;
  caculateDerivative(weights6, 5);
  NCSLabUpdate();
  mp->offset=STEP_SIZE*1.0/5.0;
  NCSLabOutput();
  NCSLabDerivative();
  storeDerivative(5);

  //Update
  restoreState(0);
  mp->stepSize=STEP_SIZE;
  caculateDerivative(weight1,6);
  NCSLabUpdate();
}
