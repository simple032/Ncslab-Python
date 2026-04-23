#include <iostream>
#include <cmath>

#include "ncslabdefines.hpp"
#include "util.hpp"
#include "mainccode.hpp"
#include "onestep.hpp"

extern MODEL *mp;

double *stateReserve;
double **derivativeReserve;

extern double sample_time[];

double singleStateReserve[SINGLE_STATE_NUM];
Matrix matrixStateReserve[MATRIX_STATE_NUM];

double singleDerivativeReserve[1][SINGLE_STATE_NUM];
Matrix matrixDerivativeReserve[1][MATRIX_STATE_NUM];

void NCSLabOneStep1(double);

#ifdef _SIMU
void ncslabLoop(){
	//xiazhiqiang:Evaluates the greatest common divisor by referencing the sampling time array:(The definition location is at line 31 of codeStructC)
    extern int sample_i;
    //extern double sample_time[];
    double real_sample_time=0.0;
    if(hasdiscrete(sample_time)==1){
      real_sample_time=gcd1(sample_time);
    }
    //end
  	mp->discreteUpdate=1;

	while(mp->time<mp->stopTime){
        //mp->time+=mp->stepSize;
        //fwrite(&(mp->time),1,sizeof(mp->time),stdout);
        writeInformation();
        NCSLabOneStep1(real_sample_time);
       mp->time+=mp->stepSize;
        //printf("time:%f\n",mp->time);
    }
}
#endif


void ncslabLoopRealtime()
{
//xiazhiqiang:Evaluates the greatest common divisor by referencing the sampling time array:(The definition location is at line 31 of codeStructC)
    extern int sample_i;
    //extern double sample_time[];
    double real_sample_time=0.0;
    if(hasdiscrete(sample_time)==1){
      real_sample_time=gcd1(sample_time);
    }
    //end
  	mp->discreteUpdate=1;
  NCSLabOutput();
  NCSLabSinkOutput();
  sendDisplayUpdateForce();

int stepCountSinceLastSend = 0;
#ifdef _WIN32
  DWORD startTick = GetTickCount();
  DWORD lastSendTick = startTick;
#else
  struct timeval startTv;
  gettimeofday(&startTv, NULL);
  long long startMs = startTv.tv_sec * 1000LL + startTv.tv_usec / 1000;
  long long lastSendMs = startMs;
#endif

  while (mp->time < mp->stopTime) {
    NCSLabOneStep1(real_sample_time);
    mp->time += mp->stepSize;
    stepCountSinceLastSend++;

    bool shouldSend = false;
#ifdef _WIN32
    DWORD nowTick = GetTickCount();
    if (stepCountSinceLastSend >= 10000 || (int)(nowTick - lastSendTick) >= 1000) {
      shouldSend = true;
    }
#else
    struct timeval nowTv;
    gettimeofday(&nowTv, NULL);
    long long nowMs = nowTv.tv_sec * 1000LL + nowTv.tv_usec / 1000;
    if (stepCountSinceLastSend >= 10000 || (int)(nowMs - lastSendMs) >= 1000) {
      shouldSend = true;
    }
#endif

    if (shouldSend) {
      sendRealtimeDataUpdate();
      stepCountSinceLastSend = 0;
#ifdef _WIN32
      lastSendTick = nowTick;
#else
      lastSendMs = nowMs;
#endif
    }

    double simulatedElapsed = mp->time - mp->startTime;
#ifdef _WIN32
    DWORD realElapsedMs = GetTickCount() - startTick;
    double realElapsedSec = realElapsedMs / 1000.0;
    if (simulatedElapsed > realElapsedSec) {
      DWORD sleepMs = (DWORD)((simulatedElapsed - realElapsedSec) * 1000);
      if (sleepMs > 0) Sleep(sleepMs);
    }
#else
    struct timeval syncTv;
    gettimeofday(&syncTv, NULL);
    long long realElapsedMs = (syncTv.tv_sec * 1000LL + syncTv.tv_usec / 1000) - startMs;
    double realElapsedSec = realElapsedMs / 1000.0;
    if (simulatedElapsed > realElapsedSec) {
      long sleepUs = (long)((simulatedElapsed - realElapsedSec) * 1000000);
      if (sleepUs > 0) usleep(sleepUs);
    }
#endif
  }

}
void NCSLabOneStep1(double real_sample_time){
	mp->offset=0;
	
	if (hasdiscrete(sample_time)){

    while (mp->discreteTime <= mp->time){
      mp->discreteTime += real_sample_time;
      // mp->offset = 0;
      // NCSLabOutput();
      // NCSLabDiscreteUpdate();
      mp->discreteUpdate = 1;
      }
    }
	
	mp->majorStep=1;
	NCSLabOutput();
	
	if(mp->discreteUpdate){
  	
  		NCSLabDiscreteUpdate();
  		mp->discreteUpdate=0;
  	}
  	NCSLabSinkOutput();
	
	NCSLabDerivative();
	NCSLabUpdate();
	mp->majorStep=0;
}
