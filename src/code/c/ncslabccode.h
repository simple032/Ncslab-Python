#ifndef NCSLABCCODE
#define NCSLABCCODE

#include "stdlib.h"
#define REAL double

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
	int width;
	void *vp;
}PARAMETER;

typedef struct {
	char *name;
	int width;
	void *vp;
}STATE;

typedef struct {
	char *type;
	char *name;
	int inputPortNum;
	int outputPortNum;
	int parameterNum;
	int stateNum;
	
	INPUT_PORT **inputPorts;
	OUTPUT_PORT **outputPorts;
	PARAMETER **parameters;
	STATE **states;

}BLOCK;

typedef struct {
	char *name;
	int blockNum;
	BLOCK **blocks;
}MODEL;



#endif

