#ifndef NCSLABCCODE
#define NCSLABCCODE

#include "stdio.h"
#include "stdlib.h"

#include <deque>
#include <list>
#include <string>

// Hardware-specific includes - only include when explicitly enabled for hardware builds
#ifdef _ENABLE_PI
#include "ncs_serialport.h"
#endif // _ENABLE_PI

// Hardware and system includes for testrig blocks
#ifdef __linux__
#include <termios.h>
#include <unistd.h>
#include <sys/ioctl.h>
#ifndef TCIOFLUSH
#define TCIOFLUSH 2
#endif
#endif

// Serial port includes for SerialConfiguration blocks
#ifdef _WIN32
#include <windows.h>
#else
#include <fcntl.h>
#include <termios.h>
#include <unistd.h>
#include <string.h>
#endif

// Constants for testrig blocks
#ifndef M_PI
#define M_PI 3.14159265358979323846
#endif

#ifndef AERO_PI
#define AERO_PI M_PI
#endif

/* Serial Port Handle Structure for centralized serial port management */
/* Following SIMULINK R2024b architecture pattern */
typedef struct {
    const char* portName;
    int baudRate;
    int dataBits;
    const char* parity;
    const char* stopBits;
    const char* byteOrder;
    const char* flowControl;
    double timeout;

#ifdef _WIN32
    HANDLE handle;
#else
    int fd;
#endif

    int isOpen;
} SerialPortHandle;


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

	REAL discreteTime;
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

typedef struct
{
	unsigned int* error_flag = NULL;
	//string error_msg;
}ssError;

typedef struct SimStruct_tag{
	ssSize sizes;
	ssStates states;
	ssStInfo stInfo;
	ssWork work;
  ssError error;

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

	// Solver tolerance parameters (matching MATLAB/Simulink)
	REAL relTol;        // Relative tolerance (default 1e-3)
	REAL absTol;        // Absolute tolerance (default 1e-6)
	REAL minStep;       // Minimum step size (default 1e-10)
	REAL maxStep;       // Maximum step size (default auto)

	// Solver status and diagnostics
	int solverStatus;   // 0=OK, 1=Warning, 2=Error, 3=Diverged
	int stepRejections; // Count of rejected steps
	int totalSteps;     // Total integration steps taken

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

enum PROGRESSTYPE{
	Simulating=1,
	Saving=2,
	Ending=-1
};

void writeSavingInformation(int,int);

#endif

