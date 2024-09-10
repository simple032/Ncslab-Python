#ifndef SERVERTHREAD
#define SERVERTHREAD
#include <float.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
//#define _WIN32_WINNT 0x501
#ifdef __WIN32
#include <winsock2.h>
#include <ws2tcpip.h>
#endif // __WIN32
#include "ncslabccode.hpp"

#ifdef __cplusplus
extern "C"{
#endif // __cplusplus
typedef struct
{
	char *port;
    MODEL *mp;

}ExtModeData;

#ifdef __cplusplus
}
#endif // __cplusplus
void startMyServerThread(ExtModeData);
void setAllClientUploadEvents();

#endif
