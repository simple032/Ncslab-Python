// ode45.cpp

#include "ncslabccode.h"
#include "ncslab.h"

#include <iostream>
//#include <octave/oct.h>
#include "math.h"
#include "Matrix.h"

#define INIT_POINT_NUM 100
#define TOL 1E-7

extern MODEL *mp;
extern double sample_time[];
double singleStateReserve[3][SINGLE_STATE_NUM];
Matrix matrixStateReserve[3][MATRIX_STATE_NUM];

double singleDerivativeReserve[6][SINGLE_STATE_NUM];
Matrix matrixDerivativeReserve[6][MATRIX_STATE_NUM];

double weights3[] = {1.0 / 4.0, 3.0 / 4.0};

double weights4[] = {161.0 / 169.0, -600.0 / 169.0, 608.0 / 169.0};
double weights5[] = {439.0 / 216.0, -8.0, 3680.0 / 513.0, -845.0 / 4104.0};
double weights6[] = {-16.0 / 27.0 , 4.0, -7088.0 / 2565.0 , 3718.0 / 4104.0 , -22.0 / 40.0};
double weightss1[] = {25.0 / 216.0, 0.0, 1408.0 / 2565.0, 2197.0 / 4104.0, -1.0 / 5.0};
double weightss2[] = {16.0 / 135.0, 0.0, 6656.0 / 12825.0, 28561.0 / 56430.0, -9.0 / 50.0, 2.0 / 55.0};

double stepSize;
double nextStepSize;

double maxStepSize;

extern double real_sample_time;

void ncslabLoop()
{
  maxStepSize=stepSize = (mp->stopTime - mp->startTime) / INIT_POINT_NUM;
  
  discreteInit();
  	 	
  while (mp->time < mp->stopTime)
  {
        //mp->time += stepSize;
      //printf("time:%f\n",mp->time);
    writeInformation();
    NCSLabOneStep();
	//mp->time += stepSize;
    
  }
}

void NCSLabOneStep()
{

  REAL dif;

  mp->offset = 0;
  
  mp->majorStep = 1;
  NCSLabOutput();
  
  if(mp->discreteUpdate){
  	
  	NCSLabDiscreteUpdate();
  	mp->discreteUpdate=0;
  }
  NCSLabSinkOutput();
  
  storeState(0);

  for (int i = 0; i < 2; i++)
  {

    // Calculate K1
    NCSLabDerivative();
    storeDerivative(0);
    mp->majorStep = 0;

    // Calculate K2
    mp->stepSize = stepSize / 4.0;
    NCSLabUpdate();
    mp->offset = stepSize / 4.0;
    NCSLabOutput();
    NCSLabDerivative();
    storeDerivative(1);

    // Calculate K3
    restoreState(0);
    mp->stepSize = stepSize * 3.0 / 8.0;
    caculateDerivative(weights3, 2);
    NCSLabUpdate();
    mp->offset = stepSize * 3.0 / 8.0;
    NCSLabOutput();
    NCSLabDerivative();
    storeDerivative(2);

    // Calculate K4
    restoreState(0);
    mp->stepSize = stepSize * 12.0 / 13.0;
    caculateDerivative(weights4, 3);
    NCSLabUpdate();
    mp->offset = stepSize * 12.0 / 13.0;
    NCSLabOutput();
    NCSLabDerivative();
    storeDerivative(3);

    // Calculate K5
    restoreState(0);
    mp->stepSize = stepSize;
    caculateDerivative(weights5, 4);
    NCSLabUpdate();
    mp->offset = stepSize;
    NCSLabOutput();
    NCSLabDerivative();
    storeDerivative(4);

    // Calculate K6
    restoreState(0);
    mp->stepSize = stepSize * 1.0 / 2.0;
    caculateDerivative(weights6, 5);
    NCSLabUpdate();
    mp->offset = stepSize*1.0/2.0;
    NCSLabOutput();
    NCSLabDerivative();
    storeDerivative(5);

    // Update 1
    restoreState(0);
    mp->stepSize = stepSize;
    caculateDerivative(weightss1, 5);
    NCSLabUpdate();
    storeState(1);

    // Update 2
    restoreState(0);
    mp->stepSize = stepSize;
    caculateDerivative(weightss2, 6);
    NCSLabUpdate();
    storeState(2);

    dif = calculateStateDif(1, 2);

    nextStepSize = sqrt(sqrt((TOL * stepSize) / dif))*0.84*stepSize;
    

    if (nextStepSize > stepSize || i == 1)
    {
      mp->time += stepSize;
      stepSize = nextStepSize;
      
      if(hasdiscrete(sample_time)){
      	
      	while(mp->discreteTime<=mp->time){
      		mp->discreteTime+=real_sample_time;
      		//mp->offset = 0;
  			//NCSLabOutput();
      		//NCSLabDiscreteUpdate();
      		mp->discreteUpdate=1;
      	}
      	
      	double dist=mp->discreteTime-mp->time;
      	if(stepSize>dist){
      		stepSize=dist;
      	}
      	
      	
      }
      
      break;
    }

    stepSize = nextStepSize;
    restoreState(0);
    mp->offset = 0;
    NCSLabOutput();
  }
}
