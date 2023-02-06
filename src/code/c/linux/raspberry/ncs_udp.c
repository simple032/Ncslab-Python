///
/// @file: ncs_udp.c
/// @author: ysw
/// @date: 2022.05.27
/// @note: linux version for fan drive and sample interface
///

#include "ncs_udp.h"
#include "string.h"
#include "unistd.h"
#include "errno.h"
#include "sys/types.h"
#include "sys/socket.h"
#include "netinet/in.h"
#include "arpa/inet.h"
#include "fcntl.h"
#include "sys/time.h"



static struct sockaddr_in adr_serv_netsend;
static SOCKET sockfd;

SOCKET UDP_OpenServer(char* strLocal, int portLocal, int nNetTimeout)
{
    printf("Error in UDP_OpenServer");
	return -1;
}
SOCKET UDP_OpenClient(char* strRemote, int portRemote, int nNetTimeout)
{
	
	adr_serv_netsend.sin_family = AF_INET;
	adr_serv_netsend.sin_port = htons(portRemote);
	int status = inet_aton(strRemote,&adr_serv_netsend.sin_addr);
	if (status != 1)
	{
		printf("Wrong IP Address.\n");
		//ssSetErrorStatus(S, errMsg);
		return -1;
	}
	bzero(&(adr_serv_netsend.sin_zero), 8);
	
	sockfd = socket(AF_INET, SOCK_DGRAM, 0);
	if (sockfd == -1)
	{
		printf("Error in socket.\n");
		return sockfd;
	}

	int sock_buf_size=1024;
	int len=sizeof(sock_buf_size);
	setsockopt( sockfd, SOL_SOCKET, SO_SNDBUF,(char *)&sock_buf_size, len);
	setsockopt( sockfd, SOL_SOCKET, SO_RCVBUF,(char *)&sock_buf_size, len);
 
    struct timeval timeout;
    timeout.tv_sec = 0;
    timeout.tv_usec = 1000;

   	setsockopt( sockfd, SOL_SOCKET, SO_SNDTIMEO, (char *)&timeout, sizeof(timeout));
	setsockopt( sockfd, SOL_SOCKET, SO_RCVTIMEO, (char *)&timeout, sizeof(timeout));
  

	//printf("Creat socket success...\n"); 
    return sockfd;
}
void UDP_Close(SOCKET handle)
{
    close(handle);
}
void UDP_Send(uint8_t* sendData, int sendLength)
{
	ssize_t ret = sendto(sockfd, sendData, sendLength, 0, 
        (struct sockaddr*)&adr_serv_netsend, sizeof(adr_serv_netsend));
    if(ret <= 0){
        printf("sendto error\n");
    }
}
int UDP_Recv(void* recvData, int buffLength)
{
    int len = sizeof(adr_serv_netsend);
    ssize_t ret = 
        recvfrom(sockfd, recvData, buffLength, 0, 
        (struct sockaddr*)&adr_serv_netsend, (socklen_t *)&len);  //接收来自server的信息
    return (int)len;
}