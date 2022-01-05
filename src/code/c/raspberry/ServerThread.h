#ifndef SERVERTHREAD
#define SERVERTHREAD
#include <float.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "ncslabccode.h"


typedef struct
{
	int port;

    uint_T acc;
    MODEL *mp;
    
    pthread_t  servetThread;

    pthread_mutex_t  timerCritical;

}ExtModeData;


void startMyServerThread(ExtModeData);
void setAllClientUploadEvents();

#endif