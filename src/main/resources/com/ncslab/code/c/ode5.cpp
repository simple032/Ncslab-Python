#include <iostream>
#include <cmath>

#include "ncslabdefines.hpp"
#include "util.hpp"
#include "mainccode.hpp"
#include "onestep.hpp"

extern MODEL *mp;

//double stateReserve[STATE_NUM];

double singleStateReserve[5][SINGLE_STATE_NUM];
Matrix matrixStateReserve[5][MATRIX_STATE_NUM];

extern double sample_time[];

//double derivativeReserve[4][STATE_NUM];

double singleDerivativeReserve[6][SINGLE_STATE_NUM];
Matrix matrixDerivativeReserve[6][MATRIX_STATE_NUM];

double weight1[]={1.0/24.0, 0.0, 0.0, 5.0/48.0, 27.0/56.0, 125.0/336.0};

double weights3[] = {1.0 / 2.0, 1.0 / 2.0};
double weights4[] = {0.0, -1.0, 2.0};
double weights5[] = {7.0 / 18.0, 5.0 / 9.0, 0.0, 1.0 / 18.0};
double weights6[] = {28.0 / 125.0 , -1.0 , 546.0 / 125.0 , 54.0 / 125.0 , -378.0 / 125.0};

extern double real_sample_time;

#ifdef _SIMU
void ncslabLoop(){

	discreteInitFixed();

	while(mp->time<mp->stopTime){
        //mp->time+=mp->stepSize;
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

  while (mp->time < mp->stopTime) {
    NCSLabOneStep();
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

  //Calculate K1
  NCSLabDerivative();
  storeDerivative(0);
  mp->majorStep=0;
  //Calculate K2
  storeState(0);
  mp->stepSize=STEP_SIZE/2;
  NCSLabUpdate();
  mp->offset=STEP_SIZE/2;
  NCSLabOutput();
  NCSLabDerivative();
  storeDerivative(1);

  //Calculate K3
  restoreState(0);
  mp->stepSize=STEP_SIZE/2;
  caculateDerivative(weights3, 2);
  NCSLabUpdate();
  mp->offset=STEP_SIZE/2;
  NCSLabOutput();
  NCSLabDerivative();
  storeDerivative(2);

  //Calculate K4
  restoreState(0);
  mp->stepSize=STEP_SIZE;
  caculateDerivative(weights4, 3);
  NCSLabUpdate();
  mp->offset=STEP_SIZE;
  NCSLabOutput();
  NCSLabDerivative();
  storeDerivative(3);

  //Calculate K5
  restoreState(0);
  mp->stepSize=STEP_SIZE*2.0/3.0;
  caculateDerivative(weights5, 4);
  NCSLabUpdate();
  mp->offset=STEP_SIZE*2.0/3.0;
  NCSLabOutput();
  NCSLabDerivative();
  storeDerivative(4);

  //Calculate K6
  restoreState(0);
  mp->stepSize=STEP_SIZE*1.0/5.0;
  caculateDerivative(weights6, 5);
  NCSLabUpdate();
  mp->offset=STEP_SIZE*1.0/5.0;
  NCSLabOutput();
  NCSLabDerivative();
  storeDerivative(5);

  //Update
  restoreState(0);
  mp->stepSize=STEP_SIZE;
  caculateDerivative(weight1,6);
  NCSLabUpdate();
}
