#include"ncslabccode.h"
#include"ServerThread.h"
#include"ncslab.h"

#include <iostream>
#include <octave/oct.h>

extern MODEL *mp;

extern double singleStateReserve[][SINGLE_STATE_NUM];
extern Matrix matrixStateReserve[][MATRIX_STATE_NUM];

extern double singleDerivativeReserve[][SINGLE_STATE_NUM];
extern Matrix matrixDerivativeReserve[][MATRIX_STATE_NUM];

void storeState(int num){
  /*
  for(int i=0;i<STATE_NUM;i++){
    stateReserve[i]=*((REAL *)(mp->states[i]->vp));
  }*/
  int single=0;
  int matrix=0;
  for(int i=0;i<STATE_NUM;i++){
  	switch(mp->states[i]->type){
  	case SINGLE:
  		singleStateReserve[num][single++]=*((REAL *)(mp->states[i]->vp));
  		break;
  	case MATRIX:
  		matrixStateReserve[num][matrix++]=*((Matrix *)(mp->states[i]->vp));
  		break;
  	}
  }
}

void restoreState(int num){
  /*	
  for(int i=0;i<STATE_NUM;i++){
    *((REAL *)(mp->states[i]->vp))=stateReserve[i];
  }*/
  int single=0;
  int matrix=0;
  for(int i=0;i<STATE_NUM;i++){
  	switch(mp->states[i]->type){
  	case SINGLE:
  		*((REAL *)(mp->states[i]->vp))=singleStateReserve[num][single++];
  		break;
  	case MATRIX:
  		*((Matrix *)(mp->states[i]->vp))=matrixStateReserve[num][matrix++];
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

REAL calculateStateDif(int seq1,int seq2){
	REAL dif=0;
	for(int i=0;i<SINGLE_STATE_NUM;i++){
		REAL difn=abs(singleStateReserve[seq1][i]-singleStateReserve[seq2][i]);
		if(dif<difn){
			dif=difn;
		}
	}
	
	for(int i=0;i<MATRIX_STATE_NUM;i++){
		Matrix *pm1=&(matrixStateReserve[seq1][i]);
		Matrix *pm2=&(matrixStateReserve[seq2][i]);
		for(int h=0;h<pm1->rows();h++){
			for(int w=0;w<pm1->cols();w++){
				REAL difn=abs((*pm1)(h,w)-(*pm2)(h,w));
				if(dif<difn){
					dif=difn;
				}
			}
		}
	}
	
	return dif;
}
