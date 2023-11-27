#include"ncslabccode.h"
#include"ServerThread.h"

#include "ncs_serialport.h"
#include"ncslab.h"

//#include <iostream>
//#include <octave/oct.h>

MODEL *mp;

ExtModeData extModeData;

timer_t main_timer;

unsigned char calcSum(unsigned char bytes[])
{
    int i = 0;  
    unsigned char res = 0x00;
    for(i=0; i<6; i++){
 	res += bytes[i];
    }
    return res;
}

/*static*/void timer_in_callback(union sigval v)
{ 

    pthread_mutex_lock(&(extModeData.timerCritical));
    NCSLabOneStep();
    mp->time+=mp->stepSize;
   // NCSLabOneStep();
    
	gettimeofday(&(mp->tv), NULL);
	//printf("%f\n",mp->time);
	setAllClientUploadEvents();
    pthread_mutex_unlock(&(extModeData.timerCritical));
    
}

void startTimer(real_T stepSize)
{
    struct sigevent evp; 

    memset(&evp, 0, sizeof(evp));

    evp.sigev_value.sival_ptr = NULL; //这里传一个参数进去，在timer的callback回调函数里面可以获得它  

    evp.sigev_notify = SIGEV_THREAD; //定时器到期后内核创建一个线程执行sigev_notify_function函数 

    evp.sigev_notify_function = timer_in_callback; //这个就是指定回调函数

 

    int ret = 0;

    ret = timer_create(CLOCK_REALTIME, &evp, &main_timer);

    if(ret < 0)

    {

        printf("timer_create() fail, ret:%d", ret);

        exit(0);


    }

    pthread_mutex_init(&(extModeData.timerCritical),NULL);


     struct itimerspec ts;

    ts.it_interval.tv_sec = (int)stepSize;

    ts.it_interval.tv_nsec = (stepSize-ts.it_interval.tv_sec)*1000000000;

    ts.it_value.tv_sec = ts.it_interval.tv_sec;

    ts.it_value.tv_nsec = ts.it_interval.tv_nsec; 

    ret = timer_settime(main_timer, TIMER_ABSTIME, &ts, NULL);

    if(ret < 0)

    {

        printf("main_timer() fail, ret:%d", ret); 

        timer_delete(main_timer);

        //timer_created = false;

        exit(1);

    } 

	//NCSLabOneStep();
	//mp->time+=STEP_SIZE;

}


int main(int argc, char *argv[]){
	
	initHardware();
	NCSLabInit();

	extModeData.acc=1;

    if(argc==2){
      extModeData.port=atoi(argv[1]);
    }
    else{
      extModeData.port=0;
    }

	mp=NCSLabGetModelP();
	extModeData.mp=mp;
	
	startMyServerThread(&extModeData);
	
	discreteInitFixed();

	startTimer(mp->stepSize);
	pthread_join(extModeData.servetThread,NULL);

	
	//WaitForSingleObject(CreateEvent(NULL,FALSE,FALSE,NULL),INFINITE);
}
