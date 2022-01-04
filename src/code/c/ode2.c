#include"ncslabccode.h"
#include"ServerThread.h"
#include"ncslab.h"

extern MODEL *mp;

double stateReserve[STATE_NUM];
double **derivativeReserve;

void NCSLabOneStep(){

  mp->offset=0;
  NCSLabOutput();
  mp->majorStep=1;
  //Calculate K0
	NCSLabDerivative();
  mp->majorStep=0;
  //Calculate K1
  storeState();
  mp->stepSize=STEP_SIZE/2;
	NCSLabUpdate();
  mp->offset=STEP_SIZE/2;
  NCSLabOutput();
  NCSLabDerivative();

  //Update
  restoreState();
  mp->stepSize=STEP_SIZE;
  NCSLabUpdate();
}
