#include"ncslabccode.h"
#include"ServerThread.h"
#include"ncslab.h"

#include <iostream>
#include <octave/oct.h>

extern MODEL *mp;

//extern double stateReserve[STATE_NUM];

extern double singleStateReserve[SINGLE_STATE_NUM];
extern Matrix matrixStateReserve[MATRIX_STATE_NUM];

//extern double derivativeReserve[][STATE_NUM];

extern double singleDerivativeReserve[4][SINGLE_STATE_NUM];
extern Matrix matrixDerivativeReserve[4][SINGLE_STATE_NUM];

void storeState(){
  /*
  for(int i=0;i<STATE_NUM;i++){
    stateReserve[i]=*((REAL *)(mp->states[i]->vp));
  }*/
  int single=0;
  int matrix=0;
  for(int i=0;i<STATE_NUM;i++){
  	switch(mp->states[i]->type){
  	case SINGLE:
  		singleStateReserve[single++]=*((REAL *)(mp->states[i]->vp));
  		break;
  	case MATRIX:
  		matrixStateReserve[matrix++]=*((Matrix *)(mp->states[i]->vp));
  		break;
  	}
  }
}

void restoreState(){
  /*	
  for(int i=0;i<STATE_NUM;i++){
    *((REAL *)(mp->states[i]->vp))=stateReserve[i];
  }*/
  int single=0;
  int matrix=0;
  for(int i=0;i<STATE_NUM;i++){
  	switch(mp->states[i]->type){
  	case SINGLE:
  		*((REAL *)(mp->states[i]->vp))=singleStateReserve[single++];
  		break;
  	case MATRIX:
  		*((Matrix *)(mp->states[i]->vp))=matrixStateReserve[matrix++];
  		break;
  	}
    
  }
}

void storeDerivative(int num){
  /*
  for(int i=0;i<STATE_NUM;i++){
    derivativeReserve[num][i]=*((REAL *)(mp->states[i]->dvp));
  }*/
  
  int single=0;
  int matrix=0;
  for(int i=0;i<STATE_NUM;i++){
    switch(mp->states[i]->type){
  	case SINGLE:
  		singleDerivativeReserve[num][single++]=*((REAL *)(mp->states[i]->dvp));
  		break;
  	case MATRIX:
  		matrixDerivativeReserve[num][matrix++]=*((Matrix *)(mp->states[i]->dvp));
  		break;
  	}
    
  }
}

void caculateDerivative(double *weights,int num){
  /*
  for(int i=0;i<STATE_NUM;i++){
    *((REAL *)(mp->states[i]->dvp))=0;
    for(int j=0;j<num;j++){
      *((REAL *)(mp->states[i]->dvp))+=weights[j]*derivativeReserve[j][i];
    }
  }*/
  
  int single=0;
  int matrix=0;
  for(int i=0;i<STATE_NUM;i++){
  	switch(mp->states[i]->type){
  	case SINGLE:
  		*((REAL *)(mp->states[i]->dvp))=0;
    	for(int j=0;j<num;j++){
      		*((REAL *)(mp->states[i]->dvp))+=weights[j]*singleDerivativeReserve[j][single];
    	}
    	single++;
  		break;
  	case MATRIX:
  		*((Matrix *)(mp->states[i]->dvp))=Matrix(mp->states[i]->height,mp->states[i]->width);
    	for(int j=0;j<num;j++){
      		*((Matrix *)(mp->states[i]->dvp))+=weights[j]*matrixDerivativeReserve[j][matrix];
    	}
    	matrix++;
  		break;
  	}
    
  }
}
