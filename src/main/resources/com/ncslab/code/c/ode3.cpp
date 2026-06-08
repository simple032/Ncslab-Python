#include <iostream>
#include <cmath>

#include "ncslabdefines.hpp"
#include "util.hpp"
#include "mainccode.hpp"
#include "onestep.hpp"

extern MODEL *mp;

//double stateReserve[STATE_NUM];

double singleStateReserve[3][SINGLE_STATE_RESERVE_NUM];
Matrix matrixStateReserve[3][MATRIX_STATE_RESERVE_NUM];

extern double sample_time[];

//double derivativeReserve[3][STATE_NUM];
double singleDerivativeReserve[3][SINGLE_STATE_RESERVE_NUM];
Matrix matrixDerivativeReserve[3][MATRIX_STATE_RESERVE_NUM];

double weight1[2]={-1.0,2.0};
double weight2[3]={1.0/6,4.0/6,1.0/6};

extern double real_sample_time;

#ifdef _SIMU
void ncslabLoop(){
	discreteInitFixed();

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


void ncslabLoopRealtime()
{
discreteInitFixed();
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

  
  int realtimeUpdateMs = ncsRealtimeNativeIntervalMs();
  if (realtimeUpdateMs < 50) {
    realtimeUpdateMs = 50;
  }
while (mp->time < mp->stopTime) {
        // Pause/resume control
        while (g_simulationPaused) {
#ifdef _WIN32
          DWORD pauseTick = GetTickCount();
          Sleep(10);
          DWORD pauseDuration = GetTickCount() - pauseTick;
          startTick += pauseDuration;
          lastSendTick += pauseDuration;
#else
          struct timeval pauseTv;
          gettimeofday(&pauseTv, NULL);
          usleep(10000);
          struct timeval pauseEndTv;
          gettimeofday(&pauseEndTv, NULL);
          long long pauseDurationMs = (pauseEndTv.tv_sec * 1000LL + pauseEndTv.tv_usec / 1000) - (pauseTv.tv_sec * 1000LL + pauseTv.tv_usec / 1000);
          startMs += pauseDurationMs;
          lastSendMs += pauseDurationMs;
#endif
          checkParameterUpdates();
        }
    NCSLabOneStep();
    mp->time += mp->stepSize;
    
    bool shouldSend = false;
#ifdef _WIN32
    DWORD nowTick = GetTickCount();
    if ((int)(nowTick - lastSendTick) >= realtimeUpdateMs) {
      shouldSend = true;
    }
#else
    struct timeval nowTv;
    gettimeofday(&nowTv, NULL);
    long long nowMs = nowTv.tv_sec * 1000LL + nowTv.tv_usec / 1000;
    if ((int)(nowMs - lastSendMs) >= realtimeUpdateMs) {
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
        if ((int)(nowTick - lastSendTick) >= realtimeUpdateMs) {
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
        if ((int)(nowMs - lastSendMs) >= realtimeUpdateMs) {
          checkParameterUpdates();
          sendRealtimeDataUpdate();
          lastSendMs = nowMs;
        }
      }
    }
#endif
  }

}
void NCSLabOneStep(){

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

  //Calculate K0
	NCSLabDerivative();
  storeDerivative(0);
  mp->majorStep=0;
  //Calculate K1
  storeState(0);
  mp->stepSize=STEP_SIZE/2;
	NCSLabUpdate();
  mp->offset=STEP_SIZE/2;
  NCSLabOutput();
  NCSLabDerivative();
  storeDerivative(1);

  //Calculate K2
  restoreState(0);
  mp->stepSize=STEP_SIZE;
	caculateDerivative(weight1,2);
  NCSLabUpdate();
  mp->offset=STEP_SIZE;
  NCSLabOutput();
  NCSLabDerivative();
  storeDerivative(2);

  //Update
  restoreState(0);
  mp->stepSize=STEP_SIZE;
  caculateDerivative(weight2,3);
  NCSLabUpdate();
}

