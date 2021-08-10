#ifndef NCSLABCCODE
#define NCSLABCCODE

#include "stdlib.h"
#define REAL double


typedef struct {
	char *type;
	char *name;
	int inputPortNum;
	int outputPortNum;
	int parameterNum;
	int stateNum;

}BLOCK;

typedef struct {
	char *name;
	int blockNum;
	BLOCK **blocks;
}MODEL;



#endif

