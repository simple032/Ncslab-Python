#ifndef __NCS_UDP_H
#define __NCS_UDP_H

#include <stdint.h>
#include "stdio.h"



typedef int SOCKET;


SOCKET UDP_OpenServer(char* strLocal, int portLocal, int nNetTimeout);
SOCKET UDP_OpenClient(char* strLocal, int portLocal, int nNetTimeout);
void UDP_Close(SOCKET handle);
void UDP_Send(uint8_t* sendData, int sendLength);
int UDP_Recv(void* recvData, int buffLength);

#endif // __NCS_UDP_H