#include"ncslabccode.h"
#include"ServerThread.h"

extern MODEL *mp;

double *stateReserve;
double **derivativeReserve;

#ifdef _SIMU
void ncslabLoop(){
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
	mp->majorStep=1;
	NCSLabOutput();
	NCSLabDerivative();
	NCSLabUpdate();
	mp->majorStep=0;
}
