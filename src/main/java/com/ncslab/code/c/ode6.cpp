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

//double derivativeReserve[4][STATE_NUM];

double singleDerivativeReserve[8][SINGLE_STATE_NUM];
Matrix matrixDerivativeReserve[8][MATRIX_STATE_NUM];

double weight1[]={41.0/840.0, 0.0, 216.0 / 840.0, 27.0/840.0, 272.0/840.0, 27.0/840.0, 216/840, 41/840};

double weights3[] = {1.0 / 4.0, 3.0 / 4.0};
double weights4[] = {1.0 / 2.0, 3.0 / 2.0, 2.0};
double weights5[] = {-5.0 / 4.0, 27.0 / 4.0, -6.0, 3.0 / 2.0};
double weights6[] = {221.0 / 6.0, -981.0 / 6.0, 867.0 / 6.0, -102.0 / 6.0, 1.0 / 6.0};
double weights7[] = {-183.0 / 40.0, 678.0 / 40.0, -472.0 / 40.0, -66.0 / 40.0, 2.0, 3.0 / 40.0 };
double weights8[] = {716.0 / 82.0 , -2079.0 / 82.0 , 1002.0 / 82.0 , 834.0 / 82.0 , -454.0 / 82.0 , -9 / 82.0, 72.0 / 82.0};

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
  mp->stepSize=STEP_SIZE/9.0;
  NCSLabUpdate();
  mp->offset=STEP_SIZE/9.0;
  NCSLabOutput();
  NCSLabDerivative();
  storeDerivative(1);

  //Calculate K3
  restoreState(0);
  mp->stepSize=STEP_SIZE/6.0;
  caculateDerivative(weights3, 2);
  NCSLabUpdate();
  mp->offset=STEP_SIZE/6.0;
  NCSLabOutput();
  NCSLabDerivative();
  storeDerivative(2);

  //Calculate K4
  restoreState(0);
  mp->stepSize=STEP_SIZE/3.0;
  caculateDerivative(weights4, 3);
  NCSLabUpdate();
  mp->offset=STEP_SIZE/3.0;
  NCSLabOutput();
  NCSLabDerivative();
  storeDerivative(3);

  //Calculate K5
  restoreState(0);
  mp->stepSize=STEP_SIZE/2.0;
  caculateDerivative(weights5, 4);
  NCSLabUpdate();
  mp->offset=STEP_SIZE/2.0;
  NCSLabOutput();
  NCSLabDerivative();
  storeDerivative(4);

  //Calculate K6
  restoreState(0);
  mp->stepSize=STEP_SIZE*2.0/3.0;
  caculateDerivative(weights6, 5);
  NCSLabUpdate();
  mp->offset=STEP_SIZE*2.0/3.0;
  NCSLabOutput();
  NCSLabDerivative();
  storeDerivative(5);

  //Calculate K7
  restoreState(0);
  mp->stepSize=STEP_SIZE*5.0/6.0;
  caculateDerivative(weights7, 6);
  NCSLabUpdate();
  mp->offset=STEP_SIZE*5.0/6.0;
  NCSLabOutput();
  NCSLabDerivative();
  storeDerivative(6);

  //Calculate K8
  restoreState(0);
  mp->stepSize=STEP_SIZE;
  caculateDerivative(weights8, 7);
  NCSLabUpdate();
  mp->offset=STEP_SIZE;
  NCSLabOutput();
  NCSLabDerivative();
  storeDerivative(7);

  //Update
  restoreState(0);
  mp->stepSize=STEP_SIZE;
  caculateDerivative(weight1,8);
  NCSLabUpdate();
}
