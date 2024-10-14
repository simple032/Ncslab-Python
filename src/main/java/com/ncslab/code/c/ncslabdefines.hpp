#ifndef NCSLABDEFINES_HPP
#define NCSLABDEFINES_HPP

#include <list>
#ifndef __WIN32
#include <sys/time.h>
#endif // __WIN32
//Input and Output
#define ssSetNumInputPorts(S, num)
#define ssGetNumInputPorts(S) (S->parentBlock->inputPortNum)
#define ssSetInputPortWidth(S, idx, width) blk->inputPorts[idx]=(INPUT_PORT*)malloc(sizeof(INPUT_PORT)*width)
#define ssSetInputPortRequiredContiguous(S, idx, value) /*direct input signal access*/
#define ssSetInputPortDirectFeedThrough(S, idx, value) ;

#define ssSetNumOutputPorts(S, num) 0
#define ssGetNumOutputPorts(S) (S->parentBlock->outputPortNum)
#define ssSetOutputPortWidth(S, idx, width) ;

#define ssGetInputPortSignal(S, idx) (S->parentBlock->inputPorts[idx]->vp)
#define ssGetOutputPortSignal(S, idx) (S->parentBlock->outputPorts[idx]->vp)

#define ssGetGlobalT() (mp->tv.tv_sec * 1000000.0 + mp->tv.tv_usec)


#define sfcnStart(S) if(S.start){S.start((struct SimStruct_tag *)&S);}

#define sfcnInitializeSizes(S) if(S.initializeSize){S.initializeSize((struct SimStruct_tag *)&S); }
#define sfcnUpdate(S) if(S.update){S.update((struct SimStruct_tag *)&S);}
#define sfcnOutputs(S,tid) if(S.outputs){S.outputs((struct SimStruct_tag *)&S, tid);}
#define sfcnDerivatives(S) if(S.derivatives){S.derivatives((struct SimStruct_tag *)&S); }

#define sfcnGetT() (mp->time)

#define sfcnIsMajorStep() (mp->majorStep)

#define REAL double
#define real_T REAL
#define uint_T unsigned int
#define int_T int
#define char_T char

enum DATA_TYPE {SINGLE, MATRIX};

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


typedef void(*MdlUpdateFcn)(void* S);
typedef void(*MdlOutputsFcn)(void* S, int tid);
typedef void(*MdlStartFcn)(void* S);
typedef void(*MdlDerivativesFcn)(void* S);
typedef void(*MdlTerminateFcn)(void* S);

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

struct SimStruct
{
	MdlUpdateFcn update;
	MdlOutputsFcn outputs;
	MdlStartFcn start;
	MdlDerivativesFcn derivatives;
	MdlTerminateFcn terminate;
	BLOCK* parentBlock;
};

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
	int cursor;
	//REAL *buffer;
	//REAL *timeBuffer;

	std::list<REAL> dataList;
	std::list<REAL> timeList;

	int isFull;
};

#endif

