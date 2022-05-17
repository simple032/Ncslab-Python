/*
 * ncs_serialport.c: Basic 'C' template for a level 2 S-function.
 *
 * Copyright 1990-2013 The MathWorks, Inc.
 */

#define _CRT_SECURE_NO_WARNINGS


#include <stdint.h>
#include <stdio.h>

#ifndef __WIN32
#include <wiringPi.h>
#include <wiringSerial.h>
#include <unistd.h>

#endif


#include "ncs_serialport.h"

/*****************************serialport***************************************/

HANDLE hComm;
/* Function: Serialport_Open =====================================================
 * Abstract:
 *    Open the serialport
 */
HANDLE Serialport_Open(char* port, uint32_t baudrate, char* msg)
{  
    DWORD err;
    HANDLE	hComm = serialOpen(port, baudrate);
	if(hComm == INVALID_HANDLE_VALUE)
    {
        #ifdef __WIN32
        err = GetLastError();
        #endif // __WIN32
        sprintf(msg, "%s Open Error. Error code:%d.\r\n", port, err);
        return INVALID_HANDLE_VALUE;
    }
    
	sprintf(msg, "%s Open Success.\n", port);
    return hComm;
}

/* Function: Serialport_Close =====================================================
 * Abstract:
 *    Close the serialport
 */
void Serialport_Close(HANDLE handle)
{
	if(handle!=INVALID_HANDLE_VALUE){
        #ifdef __WIN32
		CloseHandle(handle);
        #else
        serialClose(handle);
        #endif
    }
}

/* Function: Serialport_Send =====================================================
 * Abstract:
 *    Send data through the serialport
 */
BOOL Serialport_Send(HANDLE hComm, uint8_t* sendBuff,DWORD bytesToSend)
{

	DWORD bytesSend = 0;

	DWORD dwError;
	//variable

	//clear buffer
	bytesSend= write(hComm, sendBuff, bytesToSend);

    //Verify that the data size send equals what we tried to send
	if (bytesSend != bytesToSend)
	{			
        //sprintf(msg, ("WARNING: WriteFile() error.. Bytes Sent: %ld; MessageLength: %zd\n"), bytesSend, strlen((char*)sendBuff));
        return FALSE;
	}
    return TRUE;
}

/* Function: Serialport_Receive =====================================================
 * Abstract:
 *    Receive data from the serialport
 */
DWORD Serialport_Recv(HANDLE hComm, uint8_t* recvBuff, DWORD bytesToRead)
{
	BOOL bRead   = TRUE;
	BOOL bResult = TRUE;
	DWORD dwError=0;
	DWORD bytesRead=0;

	bytesRead = read(hComm, recvBuff, bytesToRead);

    return bytesRead;
	
}
