#ifndef NCSLABCCODE
#define NCSLABCCODE

#include "stdio.h"
#include "stdlib.h"

#include <list>

// Hardware-specific includes - only include when explicitly enabled for hardware builds
#ifdef _ENABLE_PI
#include "ncs_serialport.h"
#endif // _ENABLE_PI

// Hardware and system includes for testrig blocks
#ifdef _WIN32
#include <Windows.h>
#endif

// Constants for testrig blocks
#ifndef M_PI
#define M_PI 3.14159265358979323846
#endif

#ifndef AERO_PI
#define AERO_PI M_PI
#endif
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
	char *path;
	char *uuid;
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
#ifndef _WIN32
	struct timeval tv;
#endif // _WIN32
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
	char *path;
	char *uuid;
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

