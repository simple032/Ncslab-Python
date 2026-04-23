#include"ncslabccode.hpp"
// #include"ServerThread.hpp"
#include"ncslab.hpp"

#include <iostream>
#include <cmath>

#include "ncslabdefines.hpp"
#include "util.hpp"
//#include "mainccode.hpp"
#include "onestep.hpp"
//#include <octave/oct.h>

extern MODEL *mp;

//double stateReserve[STATE_NUM];

extern double sample_time[];

double singleStateReserve[4][SINGLE_STATE_NUM];
Matrix matrixStateReserve[4][MATRIX_STATE_NUM];

//double derivativeReserve[4][STATE_NUM];

double singleDerivativeReserve[4][SINGLE_STATE_NUM];
Matrix matrixDerivativeReserve[4][MATRIX_STATE_NUM];

double weight1[4]={1.0/6, 2.0/6, 2.0/6, 1.0/6};

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
    NCSLabOneStep();
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
void NCSLabOneStep(){

  mp->offset=0;

  if (hasdiscrete(sample_time)){

    while (mp->discreteTime - mp->time <= 1E-7){
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
  mp->stepSize=STEP_SIZE/2;
  NCSLabUpdate();
  mp->offset=STEP_SIZE/2;
  NCSLabOutput();
  NCSLabDerivative();
  storeDerivative(2);

  //Calculate K3
  restoreState(0);
  mp->stepSize=STEP_SIZE;
  NCSLabUpdate();
  mp->offset=STEP_SIZE;
  NCSLabOutput();
  NCSLabDerivative();
  storeDerivative(3);

  //Update
  restoreState(0);
  mp->stepSize=STEP_SIZE;
  caculateDerivative(weight1,4);
  NCSLabUpdate();

  //mp->time+=mp->stepSize;

  #ifdef _CIRCUIT
  
  #endif

}
