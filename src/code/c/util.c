#include"ncslabccode.h"
#include"ServerThread.h"
#include"ncslab.h"

#include <iostream>
//#include <octave/oct.h>
#include "Matrix.h"
#include "math.h"

extern MODEL *mp;

extern double singleStateReserve[][SINGLE_STATE_NUM];
extern Matrix matrixStateReserve[][MATRIX_STATE_NUM];

extern double singleDerivativeReserve[][SINGLE_STATE_NUM];
extern Matrix matrixDerivativeReserve[][MATRIX_STATE_NUM];

double calalpoutput(double inputvalue) {
     double result;
     if(inputvalue<230) {
	result=inputvalue;
     }else if(inputvalue>=230&&inputvalue<240) {
	result=1.5*(inputvalue-230)+230;
     }else if(inputvalue>=240&&inputvalue<250) {
	result=2.5*(inputvalue-240)+245;
     }else if(inputvalue>=250&&inputvalue<270) {
	result=1.25*(inputvalue-250)+270;
     }else if(inputvalue>=270&&inputvalue<300) {
	result=0.66*(inputvalue-270)+295;
     }else {
	result=inputvalue+15;	
     }
	return result;
}
extern double sample_time[];

double real_sample_time=0.0;

void discreteInit(){
	//xiazhiqiang:Evaluates the greatest common divisor by referencing the sampling time array:(The definition location is at line 31 of codeStructC)
        extern int sample_i;
        //extern double sample_time[];
        
         if(hasdiscrete(sample_time)==1){
          real_sample_time=gcd1(sample_time);
          }
        //end
  mp->discreteUpdate=1;
  if(mp->stepSize>real_sample_time){
  	mp->stepSize=real_sample_time;
  }
}

void discreteInitFixed(){
	//xiazhiqiang:Evaluates the greatest common divisor by referencing the sampling time array:(The definition location is at line 31 of codeStructC)
        extern int sample_i;
        //extern double sample_time[];
        
         if(hasdiscrete(sample_time)==1){
          real_sample_time=gcd1(sample_time);
          }
        //end
  mp->discreteUpdate=1;

}

//xiazhiqiang:Computes the greatest common divisor of the sampling time array
double gcd( double x, double y )
{   int a=(int)(x*1000);
    int b=(int)(y*1000);
    int result;
    for(result=a;result>=0;result-=1)
    {
        if(0==a%result&&0==b%result)
            return result/1000.0;
    }
	return 0;    
}

double gcd1(double a[]){
   double d=a[0];
   int i;
   for(i=0;i<mp->blockNum;i++)	{
   	if(a[i]==0){
   		break;
	   }
   	 d=gcd(d,a[i]);
   }
   return d;
}

//xiazhiqiang:Check whether there are discrete modules
int hasdiscrete(double a[]){
	  int i;
   for(i=0;i<mp->blockNum;i++){
   	if(a[i]==0){
   		break;
	   }
	}
	   if(i==0){
	   	return 0;
	   }
	   else{
	   	return 1;
	   }
	}
//xiazhiqang:Distance from the next sampling point
/*
double distance(double t,double s){
	long a=(long)(t*1000000000000000);
        long b=(long)(s*1000000000000000);
        double distance=s-(a%b)/1000000000000000.0;
        if(distance<0){
        	distance=0;
        }
        return distance;
}*/


double distance(double t,double s){
	long num=t/s;
    double dist=(num+1)*s-t;
    if(dist<0){
    	dist=0;
    }
    return dist;
}

//end

void storeState(int num){
  /*
  for(int i=0;i<STATE_NUM;i++){mbiguous
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
		REAL difn=fabs(singleStateReserve[seq1][i]-singleStateReserve[seq2][i]);
		if(dif<difn){
			dif=difn;
		}
	}
	
	for(int i=0;i<MATRIX_STATE_NUM;i++){
		Matrix *pm1=&(matrixStateReserve[seq1][i]);
		Matrix *pm2=&(matrixStateReserve[seq2][i]);
		for(int h=0;h<pm1->rows();h++){
			for(int w=0;w<pm1->cols();w++){
				REAL difn=fabs((*pm1)(h,w)-(*pm2)(h,w));
				if(dif<difn){
					dif=difn;
				}
			}
		}
	}
	
	return dif;
}

static long oldSec=0;

void writeInformation(){
	struct timeval tv;
	gettimeofday(&tv,NULL);
	if(tv.tv_sec!=oldSec){
		fwrite(&(mp->time),1,sizeof(mp->time),stdout);
		fflush(stdout); 
		oldSec=tv.tv_sec;
	}
	
}

