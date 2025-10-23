/*
 * ncs_serialport.c: Basic 'C' template for a level 2 S-function.
 *
 * Copyright 1990-2013 The MathWorks, Inc.
 */

#define _CRT_SECURE_NO_WARNINGS


#include <stdint.h>
#include <stdio.h>
#include <string.h>
#include <termios.h>

#ifndef _WIN32
#include <wiringPi.h>
#include <wiringSerial.h>
#include <unistd.h>
#include <sys/select.h>
#include <sys/time.h>
#endif

#if _ENABLE_PI
#include "ncs_serialport.h"
#endif // _ENABLE_PI
/*****************************serialport***************************************/

//HANDLE hComm;
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
        #ifdef _WIN32
        err = GetLastError();
        #endif // _WIN32
        sprintf(msg, "%s Open Error. Error code:%d.\r\n", port, err);
        return INVALID_HANDLE_VALUE;
    }else if(hComm < 0){
        sprintf(msg, "%s Open Error.\r\n", port);
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
        #ifdef _WIN32
		CloseHandle(handle);
        #else
        serialClose(handle);
        #endif
    }
}

/* Function: Serialport_Flush =====================================================
 * Abstract:
 *    Clear the serialport
 */
void Serialport_Flush(HANDLE handle)
{
	if(handle!=INVALID_HANDLE_VALUE){
        #ifdef _WIN32
        err = GetLastError();
        #else
        serialFlush(handle);
        #endif
    }
}


/* Function: Serialport_Send =====================================================
 * Abstract:
 *    Send data through the serialport
 */
BOOL Serialport_Send(HANDLE hComm, uint8_t* sendBuff,DWORD bytesToSend)
{
    if (hComm == INVALID_HANDLE_VALUE)
    {
        return FALSE;
    }
    
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
// DWORD Serialport_Recv(HANDLE hComm, uint8_t* recvBuff, DWORD bytesToRead)
// {
// 	BOOL bRead   = TRUE;
// 	BOOL bResult = TRUE;
// 	DWORD dwError=0;
// 	DWORD bytesRead=0;

// 	bytesRead = read(hComm, recvBuff, bytesToRead);

//     return bytesRead;

// }


DWORD Serialport_Recv(HANDLE hComm, uint8_t* recvBuff, DWORD bytesToRead) {
    int fd = (int)hComm;  // 将 HANDLE 强制转换为 int

    fd_set readfds;
    FD_ZERO(&readfds);
    FD_SET(fd, &readfds);

    // 设置超时时间
    struct timeval timeout;
    timeout.tv_sec = 30 / 1000;
    timeout.tv_usec = (30 % 1000) * 1000;

    int ready = select(fd + 1, &readfds, NULL, NULL, &timeout);

    if (ready == -1) {
        perror("select 失败");
        return 0;
    } else if (ready > 0) {
        // 串口有数据可读
        DWORD bytesRead = read(fd, recvBuff, bytesToRead);

        if (bytesRead > 0) {
            // 处理读取到的数据
            // printf("读取到数据：%.*s", bytesRead, recvBuff);
        } else if (bytesRead == 0) {
            // 读到文件末尾，串口可能已关闭
            return 0;
        } else {
            perror("读取失败");
            return 0;
        }

        return bytesRead;
    } else {
        // 超时，可以在这里添加超时处理逻辑
        printf("读取超时\n");
        return 0;
    }
}
