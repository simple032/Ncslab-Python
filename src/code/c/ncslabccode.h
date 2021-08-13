#ifndef NCSLABCCODE
#define NCSLABCCODE

#include "stdio.h"
#include "stdlib.h"

#include <windows.h>

#define REAL double
#define real_T REAL
#define uint_T unsigned int
#define int_T int
#define char_T char

typedef struct {
	char *name;
	int width;
	void *vp;
}INPUT_PORT;

typedef struct {
	char *name;
	int width;
	void *vp;
}OUTPUT_PORT;

typedef struct {
	char *name;
	char *path;
	int width;
	void *vp;
}PARAMETER;

typedef struct {
	char *name;
	int width;
	void *vp;
}STATE;

typedef struct {
	char *name;
	char *path;
	int width;
	void *vp;
}SIGNAL;

typedef struct {
	char *type;
	char *name;
	int inputPortNum;
	int outputPortNum;
	int parameterNum;
	int stateNum;
	int signalNum;
	
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
	
	int signalNum;
	int parameterNum;
	SIGNAL **signals;
	PARAMETER **parameters;
	
	BLOCK **blocks;
}MODEL;

void NCSLabInit();
void NCSLabOneStep();
MODEL * NCSLabGetModelP();

#endif

