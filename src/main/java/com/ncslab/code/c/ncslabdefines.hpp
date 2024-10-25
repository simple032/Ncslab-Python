#ifndef NCSLABDEFINES_HPP
#define NCSLABDEFINES_HPP

#include <list>
#ifndef __WIN32
#include <sys/time.h>
#endif // __WIN32

#define REAL double
#define real_T REAL
#define uint_T unsigned int
#define int_T int
#define char_T char

enum DATA_TYPE {SINGLE, MATRIX};


//sfuntmpl_basic constant
#define USE_DEFAULT_SIM_STATE 0
#define CONTINUOUS_SAMPLE_TIME 0

//Input and Output
#define ssSetNumInputPorts(S, num)  ((S->sizes.inputPortNum = num) && (S->sizes.inputPortWidth=(int *)calloc(num,sizeof(int))))
    
#define ssGetNumInputPorts(S) (S->parentBlock->inputPortNum)
#define ssSetInputPortWidth(S, idx, width) (S->sizes.inputPortWidth[idx] = width)
// blk->inputPorts[idx]=(INPUT_PORT*)malloc(sizeof(INPUT_PORT)*width)
#define ssSetInputPortRequiredContiguous(S, idx, value) /*direct input signal access*/
#define ssSetInputPortDirectFeedThrough(S, idx, value) ;

#define ssSetNumOutputPorts(S, num) ((S->sizes.outputPortNum = num) && (S->sizes.outputPortWidth=(int *)calloc(num,sizeof(int)))) 
#define ssGetNumOutputPorts(S) (S->parentBlock->outputPortNum) 
#define ssSetOutputPortWidth(S, idx, width) (S->sizes.outputPortWidth[idx] = width)

#define ssGetInputPortSignal(S, idx) (S->parentBlock->inputPorts[idx]->type == SINGLE ? S->parentBlock->inputPorts[idx]->vp : ((Matrix*)S->parentBlock->inputPorts[idx]->vp)->data())
#define ssGetOutputPortSignal(S, idx) (S->parentBlock->outputPorts[idx]->type == SINGLE ? S->parentBlock->outputPorts[idx]->vp : ((Matrix*)S->parentBlock->outputPorts[idx]->vp)->data())

// parameters
#define ssSetNumSFcnParams(S,num) (S->sizes.parameterNum = num)
#define ssGetNumSFcnParams(S) (S->sizes.parameterNum)
#define ssGetSFcnParamsCount(S) ((S->parentBlock->parameterNum)-1)  //有一个参数是采样时间,需减掉

#define mxGetPr(parameter) (parameter->vp)
#define ssGetSFcnParam(S,idx) (S->parentBlock->parameters[idx])

// states
#define ssSetNumContStates(S,num) (S->sizes.numContStates = num)
#define ssSetNumDiscStates(S,num) (S->sizes.numDiscStates = num)
#define ssGetContStates(S) (S->states.contStates)
#define ssGetRealDiscStates(S) (S->states.discStates)

// mdlDerivatives method
#define ssGetdX(S) (S->states.derivative)

// sample time and offset time
#define ssSetNumSampleTimes(S,num) ((S->sizes.numSampleTimes = num ) && (S->stInfo.sampleTimes = (REAL *)calloc(num,sizeof(REAL))) && (S->stInfo.offsetTimes = (REAL *)calloc(num,sizeof(REAL))))

#define ssSetSampleTime(S,idx,sampleTime) (S->stInfo.sampleTimes[idx] = sampleTime)
#define ssSetOffsetTime(S,idx,offsetTime) (S->stInfo.offsetTimes[idx] = offsetTime)

//
#define ssSetNumNonsampledZCs(S, num);

#define ssSetSimStateCompliance(S,num)
#define ssSetOptions(S,num)

// work vector
#define ssSetNumRWork(S, num) ((S->sizes.numRWork = num) && (S->work.rWork = (double *)malloc(num*sizeof(double))))
#define ssSetNumIWork(S, num) ((S->sizes.numIWork = num) && (S->work.iWork = (int *)malloc(num*sizeof(int))))
#define ssSetNumPWork(S, num) ((S->sizes.numPWork = num) && (S->work.pWork = (void **)malloc(num*sizeof(void *))))
#define ssSetNumModes(S, num) (S->sizes.numModes = num)

#define ssGetNumRWork(S) (S->sizes.numRWork)
#define ssGetNumIWork(S) (S->sizes.numIWork)
#define ssGetNumPWork(S) (S->sizes.numPWork)
#define ssGetNumModes(S) (S->sizes.numModes)

#define ssGetRWork(S) (S->work.rWork)
#define ssGetIWork(S) (S->work.iWork)
#define ssGetPWork(S) (S->work.pWork)

//
#define ssGetGlobalT() (mp->tv.tv_sec * 1000000.0 + mp->tv.tv_usec)


#define sfcnStart(S) if(S.start){S.start((struct SimStruct_tag *)&S);}

#define sfcnInitializeSizes(S) if(S.initializeSize){S.initializeSize((struct SimStruct_tag *)&S); }
#define sfcnUpdate(S) if(S.update){S.update((struct SimStruct_tag *)&S);}
#define sfcnOutputs(S,tid) if(S.outputs){S.outputs((struct SimStruct_tag *)&S, tid);}
#define sfcnDerivatives(S) if(S.derivatives){S.derivatives((struct SimStruct_tag *)&S); }

#define sfcnGetT() (mp->time)

#define sfcnIsMajorStep() (mp->majorStep)
//
/*=======================================================================*
 * Fixed width word size data types:                                     *
 *   int8_T, int16_T, int32_T     - signed 8, 16, or 32 bit integers     *
 *   uint8_T, uint16_T, uint32_T  - unsigned 8, 16, or 32 bit integers   *
 *   real32_T, real64_T           - 32 and 64 bit floating point numbers *
 *=======================================================================*/
typedef signed char int8_T;
typedef unsigned char uint8_T;
typedef short int16_T;
typedef unsigned short uint16_T;
typedef int int32_T;
typedef unsigned int uint32_T;
typedef float real32_T;
typedef double real64_T;

/*===========================================================================*
 * Generic type definitions: real_T, time_T, boolean_T, char_T, int_T,       *
 *                           uint_T and byte_T.                              *
 *===========================================================================*/

typedef double time_T;
typedef unsigned char boolean_T;
typedef char_T byte_T;





struct INPUT_PORT
{
	char *name;
	int width;
	int height;
	DATA_TYPE type;
	void *vp;
};

struct OUTPUT_PORT
{
	char *name;
	int width;
	int height;
	DATA_TYPE type;
	void *vp;
};

struct PARAMETER
{
	char *name;
	char *path;
	int width;
	int height;
	DATA_TYPE type;
	void *vp;
};

struct STATE
{
	char *name;
	int width;
	int height;
	DATA_TYPE type;
	void *vp;
	void *dvp;
};

struct SIGNAL
{
	char *name;
	char *path;
	int width;
	int height;
	DATA_TYPE type;
	void *vp;
};


typedef void(*MdlUpdateFcn)(void*);
typedef void(*MdlOutputsFcn)(void*,int);
typedef void(*MdlStartFcn)(void*);
typedef void(*MdlDerivativesFcn)(void*);
typedef void(*MdlTerminateFcn)(void*);
typedef void(*MdlInitializeSizesFcn)(void*);
typedef void(*MdlInitializeSampleTimesFcn)(void*);
typedef void(*MdlInitializeConditionsFcn)(void*);

struct BLOCK
{
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

};

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

struct MODEL
{
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

};

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
	//int cursor;
	//REAL *buffer;
	//REAL *timeBuffer;

	std::list<REAL> dataList;
	std::list<REAL> timeList;

	int isFull;
};

#endif

