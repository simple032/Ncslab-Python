#ifndef __NCS_SERIALPORT_H
#define __NCS_SERIALPORT_H

#ifdef __WIN32

#include <Windows.h>

#else

typedef int HANDLE;
typedef int BOOL;
typedef unsigned int DWORD;

#ifndef INVALID_HANDLE_VALUE
#define INVALID_HANDLE_VALUE (-1)
#endif

#endif // __WIN32

#include <stdint.h>

HANDLE Serialport_Open(char* port, uint32_t baudrate, char* msg);
void Serialport_Close(HANDLE handle);
BOOL Serialport_Send(HANDLE hComm, uint8_t* sendBuff, DWORD bytesToSend);
DWORD Serialport_Recv(HANDLE hComm, uint8_t* recvBuff, DWORD bytesToRead);


#endif // __NCS_SERIALPORT_H