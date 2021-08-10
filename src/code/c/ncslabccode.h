#ifndef NCSLABCCODE
#define NCSLABCCODE

#include "stdlib.h"
#define REAL double


typedef struct {
	char *name;

}BLOCK;

typedef struct {
	char *name;
	int blockNum;
	BLOCK **blocks;
}MODEL;



#endif

