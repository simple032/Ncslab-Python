#ifndef SERVERTHREAD
#define SERVERTHREAD
#include <float.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
//#define _WIN32_WINNT 0x501
#include <winsock2.h>
#include <ws2tcpip.h>

#include "ncslabccode.h"


typedef struct
{
	char *port;

}ExtModeData;


void startMyServerThread(ExtModeData);
void setAllClientUploadEvents();

#endif