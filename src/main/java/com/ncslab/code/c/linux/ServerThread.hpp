#ifndef SERVERTHREAD_HPP
#define SERVERTHREAD_HPP

#include “pthread.h”
#include "ncslabdefines.hpp"

typedef struct
{
	int port;

    uint_T acc;
    MODEL *mp;
    
    pthread_t  servetThread;

    pthread_mutex_t  timerCritical;

}ExtModeData;


void startMyServerThread(ExtModeData*);
void setAllClientUploadEvents();

#endif