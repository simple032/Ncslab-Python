#include"ncslabccode.h"
#include"ServerThread.h"
#include"ncslab.h"

extern MODEL *mp;

double stateReserve[STATE_NUM];

double derivativeReserve[3][STATE_NUM];

double weight1[2]={-1.0,2.0};
double weight2[3]={1.0/6,4.0/6,1.0/6};

void NCSLabOneStep(){

  mp->offset=0;
  mp->majorStep=1;
  NCSLabOutput();

  //Calculate K0
	NCSLabDerivative();
  storeDerivative(0);
  mp->majorStep=0;
  //Calculate K1
  storeState();
  mp->stepSize=STEP_SIZE/2;
	NCSLabUpdate();
  mp->offset=STEP_SIZE/2;
  NCSLabOutput();
  NCSLabDerivative();
  storeDerivative(1);

  //Calculate K2
  restoreState();
  mp->stepSize=STEP_SIZE;
	caculateDerivative(weight1,2);
  NCSLabUpdate();
  mp->offset=STEP_SIZE;
  NCSLabOutput();
  NCSLabDerivative();
  storeDerivative(2);

  //Update
  restoreState();
  mp->stepSize=STEP_SIZE;
  caculateDerivative(weight2,3);
  NCSLabUpdate();
}
