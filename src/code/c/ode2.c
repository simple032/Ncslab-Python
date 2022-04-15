#include"ncslabccode.h"
#include"ServerThread.h"
#include"ncslab.h"

extern MODEL *mp;

double stateReserve[STATE_NUM];
double **derivativeReserve;

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
