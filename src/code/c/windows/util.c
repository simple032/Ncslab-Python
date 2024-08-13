#include "winsock2.h"
#include"ncslabccode.h"
#include"ServerThread.h"
#include"ncslab.h"

extern MODEL *mp;


extern double singleStateReserve[][SINGLE_STATE_NUM];
extern Matrix matrixStateReserve[][MATRIX_STATE_NUM];

extern double singleDerivativeReserve[][SINGLE_STATE_NUM];
extern Matrix matrixDerivativeReserve[][MATRIX_STATE_NUM];

void storeState(){
  for(int i=0;i<STATE_NUM;i++){
    stateReserve[i]=*((REAL *)(mp->states[i]->vp));
  }
}

void restoreState(){
  for(int i=0;i<STATE_NUM;i++){
    *((REAL *)(mp->states[i]->vp))=stateReserve[i];
  }
}

void storeDerivative(int num){
  for(int i=0;i<STATE_NUM;i++){
    derivativeReserve[num][i]=*((REAL *)(mp->states[i]->dvp));
  }
}

void caculateDerivative(double *weights,int num){
  for(int i=0;i<STATE_NUM;i++){
    *((REAL *)(mp->states[i]->dvp))=0;
    for(int j=0;j<num;j++){
      *((REAL *)(mp->states[i]->dvp))+=weights[j]*derivativeReserve[j][i];
    }
  }
}