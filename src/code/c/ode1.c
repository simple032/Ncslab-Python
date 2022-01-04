#include"ncslabccode.h"
#include"ServerThread.h"

extern MODEL *mp;

double *stateReserve;
double **derivativeReserve;

void NCSLabOneStep(){
	mp->offset=0;
	mp->majorStep=1;
	NCSLabOutput();
	NCSLabDerivative();
	NCSLabUpdate();
	mp->majorStep=0;
}
