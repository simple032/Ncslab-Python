#include"ncslabccode.h"
#include"ServerThread.h"
#include"ncslab.h"

extern MODEL *mp;

extern double stateReserve[STATE_NUM];
extern double derivativeReserve[][STATE_NUM];

void storeState(){
  int i = 0;
  for(i=0;i<STATE_NUM;i++){
    stateReserve[i]=*((REAL *)(mp->states[i]->vp));
  }
}

void restoreState(){
  int i = 0;
  for(i=0;i<STATE_NUM;i++){
    *((REAL *)(mp->states[i]->vp))=stateReserve[i];
  }
}

void storeDerivative(int num){
  int i = 0;  
  for(i=0;i<STATE_NUM;i++){
    derivativeReserve[num][i]=*((REAL *)(mp->states[i]->dvp));
  }
}

void caculateDerivative(double *weights,int num){
  int i = 0, j = 0;
  for(i=0;i<STATE_NUM;i++){
    *((REAL *)(mp->states[i]->dvp))=0;
    for(j=0;j<num;j++){
      *((REAL *)(mp->states[i]->dvp))+=weights[j]*derivativeReserve[j][i];
    }
  }
}
