#ifndef NCSLABCCODE
#define NCSLABCCODE

#include "stdio.h"
#include "stdlib.h"

#include <list>

enum DATA_TYPE {SINGLE,MATRIX};

#define REAL double
#define real_T REAL
#define uint_T unsigned int
#define int_T int
#define char_T char

typedef struct {
	char *name;
	int width;
	int height;
	void *vp;
	DATA_TYPE type;
}INPUT_PORT;

typedef struct {
	char *name;
	int width;
	int height;
	void *vp;
	DATA_TYPE type;
}OUTPUT_PORT;

typedef struct {
	char *name;
	char *path;
	int width;
	int height;
	void *vp;
	DATA_TYPE type;
}PARAMETER;

typedef struct {
	char *name;
	int width;
	int height;
	void *vp;
	void *dvp;
	DATA_TYPE type;
}STATE;

typedef struct {
	char *name;
	char *path;
	int width;
	int height;
	void *vp;
	DATA_TYPE type;
}SIGNAL;

typedef struct {
	char *type;
	char *name;
	int inputPortNum;
	int outputPortNum;
	int parameterNum;
	int stateNum;
	int signalNum;
	int discreteUpdated;

	INPUT_PORT **inputPorts;
	OUTPUT_PORT **outputPorts;
	PARAMETER **parameters;
	STATE **states;
	SIGNAL **signals;

}BLOCK;


typedef void(*MdlUpdateFcn)(void*);
typedef void(*MdlOutputsFcn)(void*,int);
typedef void(*MdlStartFcn)(void*);
typedef void(*MdlDerivativesFcn)(void*);
typedef void(*MdlTerminateFcn)(void*);
typedef void(*MdlInitializeSizesFcn)(void*);
typedef void(*MdlInitializeSampleTimesFcn)(void*);
typedef void(*MdlInitializeConditionsFcn)(void*);

typedef struct {

	int inputPortNum;
	int outputPortNum;
	int parameterNum;
	int stateNum;
	int signalNum;

	int* inputPortWidth;
	int* outputPortWidth;

	int numContStates;
	int numDiscStates;
	int numSampleTimes;

	int numRWork;
	int numIWork;
	int numPWork;
	int numModes;
}ssSize;

typedef struct {
	
	void* contStates;
	void* discStates;

	void* derivative;

}ssStates;

typedef struct {

	REAL *rWork;
	int *iWork;
	void **pWork;

}ssWork;

typedef struct 
{
	REAL* sampleTimes;
	REAL* offsetTimes;
}ssStInfo;


typedef struct SimStruct_tag{
	ssSize sizes;
	ssStates states;
	ssStInfo stInfo;
	ssWork work;

	MdlInitializeSizesFcn initializeSizes;
	MdlInitializeSampleTimesFcn initializeSampleTimes;
	MdlInitializeConditionsFcn initializeConditions;
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
#ifndef __WIN32
	struct timeval tv;
#endif // __WIN32
  int terminalNum;
}MODEL;

enum TERMINALTYPE{Scope};

struct TERMINAL
{
	enum TERMINALTYPE type;
	void *terminal;
};


struct SCOPE
{
	char *name;
	int maxDataLength;
	int width;
	int height;
	int cursor;
	//REAL *buffer;
	//REAL *timeBuffer;

	std::list<REAL> dataList;
	std::list<REAL> timeList;

	int isFull;
};

void NCSLabInit();
void NCSLabOneStep();
void NCSLabOutput();
void NCSLabDerivative();
void NCSLabUpdate();
void NCSLabDiscreteUpdate();
void NCSLabSinkOutput();
void storeState();
void restoreState();
void storeDerivative(int);
void caculateDerivative(double *,int);
MODEL * NCSLabGetModelP();

#endif

