#include "ncslabccode.h"
#include "ServerThread.h"
#include "ncslab.h"

#include <iostream>
#include <octave/oct.h>

#define INIT_POINT_NUM 100
#define TOL 1E-6

extern MODEL *mp;

double singleStateReserve[3][SINGLE_STATE_NUM];
Matrix matrixStateReserve[3][MATRIX_STATE_NUM];

double singleDerivativeReserve[6][SINGLE_STATE_NUM];
Matrix matrixDerivativeReserve[6][MATRIX_STATE_NUM];

double weights3[] = {1.0 / 4.0, 3.0 / 4.0};

double weights4[] = {1932.0 / 2028.0, -7200.0 / 2028.0, 7296.0 / 2028.0};
double weights5[] = {439.0 / 216.0, -8.0, 3680.0 / 513.0, -845.0 / 4104.0};
double weights6[] = {-8.0 / 27.0 * 2.0, 2.0 * 2.0, -3544.0 / 2565.0 * 2.0, 1859.0 / 4104.0 * 2.0, -11.0 / 40.0 * 2.0};
double weightss1[] = {25.0 / 216.0, 0.0, 1408.0 / 2565.0, 2197.0 / 4104.0, -1.0 / 5.0};
double weightss2[] = {16.0 / 135.0, 0.0, 6656.0 / 12825.0, 28561.0 / 56430.0, -9.0 / 50.0, 2.0 / 55.0};

double stepSize;
double nextStepSize;

double maxStepSize;

#ifdef _SIMU
void ncslabLoop()
{
  maxStepSize=stepSize = (mp->stopTime - mp->startTime) / INIT_POINT_NUM;
  
  while (mp->time < mp->stopTime)
  {
  	//printf("time:%f\n",mp->time);
  	fwrite(&(mp->time),1,sizeof(mp->time),stdout);
    NCSLabOneStep();
	mp->time += stepSize;
    
  }
}
#endif

void NCSLabOneStep()
{

  REAL dif;

  mp->offset = 0;
  mp->majorStep = 1;
  NCSLabOutput();
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

    // printf("%f\n",dif);

    nextStepSize = sqrt(sqrt((TOL * stepSize) / dif))*0.84*stepSize;
    
    if(nextStepSize>maxStepSize){
    	nextStepSize=maxStepSize;
    }

    // printf("%f\n", nextStepSize);

    if (nextStepSize > stepSize || i == 1)
    {
      stepSize = nextStepSize;
      break;
    }

    stepSize = nextStepSize;
    restoreState(0);
    mp->offset = 0;
    NCSLabOutput();
  }
}
