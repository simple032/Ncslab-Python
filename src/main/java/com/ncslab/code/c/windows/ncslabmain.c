#include"winsock2.h"
#include"ncslabccode.h"
#include"ServerThread.hpp"
#include"ncslab.h"
MODEL *mp;

void  CALLBACK TimeEvent(UINT uTimerID, UINT uMsg, DWORD_PTR dwUser, DWORD_PTR dw1, DWORD_PTR dw2){
	//printf("%f\n",mp->time);

	setAllClientUploadEvents();

	NCSLabOneStep();
	mp->time+=STEP_SIZE;
}

int main(int argc, char *argv[]){
	ExtModeData extModeData;

	NCSLabInit();

	if(argc==2){
      extModeData.port=argv[1];
    }
    else{
      extModeData.port=NULL;
    }

	mp=NCSLabGetModelP();
	extModeData.mp=mp;

	startMyServerThread(extModeData);
	
	timeSetEvent(STEP_SIZE*1000,1,(LPTIMECALLBACK)TimeEvent,0,TIME_PERIODIC);
	
	WaitForSingleObject(CreateEvent(NULL,FALSE,FALSE,NULL),INFINITE);
}
