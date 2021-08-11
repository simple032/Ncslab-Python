#include"ncslabccode.h"

main(){
	
	MODEL *mp;
	
	NCSLabInit();
	
	mp=NCSLabGetModelP();
	
	while(mp->time<mp->stopTime){
		printf("%f\n",mp->time);
		NCSLabOneStep();
		mp->time+=mp->stepSize;
		
	}
}