#include "ncslabccode.h"
#include "ServerThread.h"
#include "ncslab.h"

#include <iostream>
//#include <octave/oct.h>
#include "math.h"
#include "Matrix.h"

#define INIT_POINT_NUM 100
#define TOL 1E-4

extern MODEL *mp;
extern double sample_time[20];
double singleStateReserve[3][SINGLE_STATE_NUM];
Matrix matrixStateReserve[3][MATRIX_STATE_NUM];

double singleDerivativeReserve[4][SINGLE_STATE_NUM];
Matrix matrixDerivativeReserve[4][MATRIX_STATE_NUM];

double weights3[] = {0.0, 1.0};

double weights4[] = {2.0 / 9.0, 3.0 / 9.0, 4.0 / 9.0};
double weightss1[] = {2.0 / 9.0, 3.0 / 9.0, 4.0 / 9.0};
double weightss2[] = {7.0 / 24.0, 6.0 / 24.0 , 8.0 / 24.0, 3.0 / 24.0};

double stepSize;
double nextStepSize;

double maxStepSize;

extern double real_sample_time;


#ifdef _SIMU
void ncslabLoop()
{
  maxStepSize=stepSize = (mp->stopTime - mp->startTime) / INIT_POINT_NUM;
  
  discreteInit();
  
  while (mp->time < mp->stopTime)
  {
  	//printf("time:%f\n",mp->time);
  	//fwrite(&(mp->time),1,sizeof(mp->time),stdout);
  	writeInformation();
      //printf("time:%f\n",mp->time);
    NCSLabOneStep();
	//mp->time += stepSize;
    
  }
}
#endif

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
    mp->stepSize = stepSize / 2.0;
    NCSLabUpdate();
    mp->offset = stepSize / 2.0;
    NCSLabOutput();
    NCSLabDerivative();
    storeDerivative(1);

    // Calculate K3
    restoreState(0);
    mp->stepSize = stepSize * 3.0 / 4.0;
    caculateDerivative(weights3, 2);
    NCSLabUpdate();
    mp->offset = stepSize * 3.0 / 4.0;
    NCSLabOutput();
    NCSLabDerivative();
    storeDerivative(2);

    // Calculate K4
    restoreState(0);
    mp->stepSize = stepSize;
    caculateDerivative(weights4, 3);
    NCSLabUpdate();
    mp->offset = stepSize;
    NCSLabOutput();
    NCSLabDerivative();
    storeDerivative(3);

    // Update 1
    restoreState(0);
    mp->stepSize = stepSize;
    caculateDerivative(weightss1, 3);
    NCSLabUpdate();
    storeState(1);

    // Update 2
    restoreState(0);
    mp->stepSize = stepSize;
    caculateDerivative(weightss2, 4);
    NCSLabUpdate();
    storeState(2);

    dif = calculateStateDif(1, 2);

    // printf("%f\n",dif);

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
