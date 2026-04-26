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
    
    bool shouldSend = false;
#ifdef _WIN32
    DWORD nowTick = GetTickCount();
    if ((int)(nowTick - lastSendTick) >= 1000) {
      shouldSend = true;
    }
#else
    struct timeval nowTv;
    gettimeofday(&nowTv, NULL);
    long long nowMs = nowTv.tv_sec * 1000LL + nowTv.tv_usec / 1000;
    if ((int)(nowMs - lastSendMs) >= 1000) {
      shouldSend = true;
    }
#endif

    if (shouldSend) {
      checkParameterUpdates();
      sendRealtimeDataUpdate();
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
      while (sleepMs > 0) {
        DWORD chunk = sleepMs > 100 ? 100 : sleepMs;
        Sleep(chunk);
        sleepMs -= chunk;
        DWORD nowTick = GetTickCount();
        if ((int)(nowTick - lastSendTick) >= 1000) {
          checkParameterUpdates();
          sendRealtimeDataUpdate();
          lastSendTick = nowTick;
        }
      }
    }
#else
    struct timeval syncTv;
    gettimeofday(&syncTv, NULL);
    long long realElapsedMs = (syncTv.tv_sec * 1000LL + syncTv.tv_usec / 1000) - startMs;
    double realElapsedSec = realElapsedMs / 1000.0;
    if (simulatedElapsed > realElapsedSec) {
      long sleepUs = (long)((simulatedElapsed - realElapsedSec) * 1000000);
      while (sleepUs > 0) {
        long chunk = sleepUs > 100000 ? 100000 : sleepUs;
        usleep(chunk);
        sleepUs -= chunk;
        struct timeval nowTv;
        gettimeofday(&nowTv, NULL);
        long long nowMs = nowTv.tv_sec * 1000LL + nowTv.tv_usec / 1000;
        if ((int)(nowMs - lastSendMs) >= 1000) {
          checkParameterUpdates();
          sendRealtimeDataUpdate();
          lastSendMs = nowMs;
        }
      }
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
