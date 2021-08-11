#include"ncslabccode.h"
#include"ServerThread.h"

MODEL *mp;

void  CALLBACK TimeEvent(UINT uTimerID, UINT uMsg, DWORD_PTR dwUser, DWORD_PTR dw1, DWORD_PTR dw2){
	//printf("%f\n",mp->time);
	NCSLabOneStep();
	mp->time+=mp->stepSize;
}

main(int argc, const char *argv[]){
	ExtModeData extModeData;

	NCSLabInit();

	if(argc==2){
      extModeData.port=argv[1];
    }
    else{
      extModeData.port=NULL;
    }

	startMyServerThread(extModeData);
	
	mp=NCSLabGetModelP();
	
	timeSetEvent(mp->stepSize*1000,1,(LPTIMECALLBACK)TimeEvent,0,TIME_PERIODIC);
	
	WaitForSingleObject(CreateEvent(NULL,FALSE,FALSE,NULL),INFINITE);
}
