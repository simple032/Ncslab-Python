#include"ncslabccode.h"
#include"ServerThread.h"

#include "ncs_serialport.h"
#include"ncslab.h"

MODEL *mp;

ExtModeData extModeData;

timer_t main_timer;


int main(int argc, char *argv[]){

	double endTime=10;
    NCSLabInit();

	extModeData.acc=1;

    if(argc>=2){
        endTime=atof(argv[1]);
    }

    if(argc==3){
      extModeData.port=atoi(argv[2]);
    }
    else{
      extModeData.port=0;
    }

	mp=NCSLabGetModelP();
	extModeData.mp=mp;

	startMyServerThread(&extModeData);

    while(mp->time<endTime){
        mp->time+=mp->stepSize;
        NCSLabOneStep();

        //printf("time:%f\n",mp->time);
    }
    
    NCSLabTerminate();
    NCSLabSaveResult();
}

