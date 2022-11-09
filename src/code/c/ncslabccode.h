#ifndef NCSLABCCODE
#define NCSLABCCODE

#include <float.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <pthread.h>
#include<errno.h>
#include<sys/types.h>
#include<sys/socket.h>
#include<netinet/in.h>
#include <sys/time.h>
#include<signal.h>
#include<time.h>
#include "arpa/inet.h"
#include "fcntl.h"

#include<list>

using namespace std;

#define REAL double
#define real_T REAL
#define uint_T unsigned int
#define int_T int
#define char_T char

enum DATA_TYPE {SINGLE,MATRIX};

typedef struct {
	char *name;
	int width;
	int height;
	DATA_TYPE type;
	void *vp;
}INPUT_PORT;

typedef struct {
	char *name;
	int width;
	int height;
	DATA_TYPE type;
	void *vp;
}OUTPUT_PORT;

typedef struct {
	char *name;
	char *path;
	int width;
	int height;
	DATA_TYPE type;
	void *vp;
}PARAMETER;

typedef struct {
	char *name;
	int width;
	int height;
	DATA_TYPE type;
	void *vp;
	void *dvp;
}STATE;

typedef struct {
	char *name;
	char *path;
	int width;
	int height;
	DATA_TYPE type;
	void *vp;
}SIGNAL;


typedef void(*MdlUpdateFcn)(void* S);
typedef void(*MdlOutputsFcn)(void* S, int tid);
typedef void(*MdlStartFcn)(void* S);
typedef void(*MdlDerivativesFcn)(void* S);
typedef void(*MdlTerminateFcn)(void* S);

typedef struct {
	char *type;
	char *name;
	int inputPortNum;
	int outputPortNum;
	int parameterNum;
	int stateNum;
	int signalNum;
	
	REAL discreteTime;
	int discreteUpdated;
	
	INPUT_PORT **inputPorts;
	OUTPUT_PORT **outputPorts;
	PARAMETER **parameters;
	STATE **states;
	SIGNAL **signals;

}BLOCK;

typedef struct SimStruct_tag{
	MdlUpdateFcn update;
	MdlOutputsFcn outputs;
	MdlStartFcn start;
	MdlDerivativesFcn derivatives;
	MdlTerminateFcn terminate;
	BLOCK* parentBlock;
}SimStruct;

typedef struct {
	char *name;
	int blockNum;
	REAL stepSize;
	REAL startTime;
	REAL stopTime;
	REAL time;
	REAL offset;
	REAL discreteTime;
	
	int discreteUpdate;
	
	int signalNum;
	int parameterNum;
	int stateNum;
	SIGNAL **signals;
	PARAMETER **parameters;
	STATE **states;
	
	BLOCK **blocks;
	int majorStep;
	struct timeval tv;
	
	int terminalNum;
	
}MODEL;

enum TERMINALTYPE{Scope};

typedef struct{
	enum TERMINALTYPE type;
	void *terminal;
}TERMINAL;

typedef struct{
	char *name;
	int maxDataLength;
	int width;
	int height;
	//int cursor;
	//REAL *buffer;
	//REAL *timeBuffer;
	
	list<REAL> dataList;
	list<REAL> timeList;
	
	int isFull;
}SCOPE;

void NCSLabInit();
void NCSLabOneStep();
void NCSLabOutput();
void NCSLabDerivative();
void NCSLabUpdate();
void NCSLabDiscreteUpdate();
void NCSLabTerminate();
void storeState(int);
void restoreState(int);
void storeDerivative(int);
REAL calculateStateDif(int,int);
void caculateDerivative(double *,int);
MODEL * NCSLabGetModelP();

//xiazhiqiang:These functions are defined in util.c
double gcd( double, double);
double gcd1(double *);
int hasdiscrete(double *);
void NCSLabOneStep45(double);
double distance(double,double);
void NCSLabOneStep23(double);
//end

void ncslabLoop();

void NCSLabSaveResult();

unsigned char calcSum(unsigned char bytes[]);

void writeInformation();

void initHardware();

#endif

