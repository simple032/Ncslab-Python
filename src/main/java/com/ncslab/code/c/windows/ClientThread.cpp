#ifdef _WIN32
#include "winsock2.h"
#endif // _WIN32
#include "ClientThread.hpp"
#include "ncslabccode.hpp"
#include "DataApi.hpp"
#include "UploadThread.hpp"

#ifndef MSG_WAITALL
	#define MSG_WAITALL 0x08
#endif

#define false FALSE;

DWORD WINAPI ClientThreadFunction( LPVOID lpParam );
BOOL uploadMMI(CLIENT_STRUCT *p);
void closeClient(CLIENT_STRUCT *p);
BOOL readCom(CLIENT_STRUCT *p);

BOOL uploadVersion(CLIENT_STRUCT *p);
BOOL uploadSignals(CLIENT_STRUCT *p);
BOOL uploadBlockParameters(CLIENT_STRUCT *p);
BOOL uploadModelParameters(CLIENT_STRUCT *p);
BOOL setParameter(CLIENT_STRUCT *p);


BOOL responseEXT_CONNECT(CLIENT_STRUCT *p);
BOOL responseEXT_GET_PARAMSIGNAL(CLIENT_STRUCT *p);
BOOL responseEXT_SELECT_SIGNALS(CLIENT_STRUCT *p);
BOOL responseEXT_SETSAMPLETIME(CLIENT_STRUCT *p);
BOOL responseEXT_SETPARAM(CLIENT_STRUCT *p);

void createClientThread(CLIENT_STRUCT *p)
{
	HANDLE  hClientThread;
	DWORD  hClientThreadId;

	printf("Creating client thread...\n");

	InitializeCriticalSection(&(p->listCritical));

	InitializeCriticalSection(&(p->socketCritical));

	hClientThread = CreateThread(
            NULL,                   // default security attributes
            0,                      // use default stack size
            ClientThreadFunction,       // thread function name
            p,          // argument to thread function
            0,                      // use default creation flags
            &hClientThreadId);   // returns the thread identifier
}

BOOL terminateUploadThread(CLIENT_STRUCT *p)
{
	if(p->upload.hUploadThread!=0)
	{
		//Wait for the old thread to stop.
		printf("Old upload thread didn't stop. Wait for it to stop\n");
		p->upload.loop=FALSE;
		WaitForSingleObject(p->upload.hUploadThread,INFINITE);
		printf("Old upload thread stopped\n");
	}
	return TRUE;
}

BOOL startUpload(CLIENT_STRUCT *p)
{
	//terminateUploadThread(p);
	printf("Starting new upload thread.\n");
	createUploadThread(p);
	return TRUE;
}

DWORD WINAPI ClientThreadFunction( LPVOID lpParam )
{
	BOOL loop=TRUE;

	CLIENT_STRUCT *p;

	p=(CLIENT_STRUCT *)lpParam;

	MODEL *mp;

	mp=p->pExtModeData->mp;

	printf("\nClient thread started!\n");
	printf("DataApi version number %s\n",dataApiGetVersion(mp));
	printf("Number of blocks %d\n",dataApiGetNumBlocks(mp));
	printf("Number of signals %d\n",dataApiGetNumSignals(mp));
	printf("Number of parameters %d\n",dataApiGetNumParameters(mp));


	while(loop)
	{
		printf("\nWaiting for command!\n");
		if(readCom(p)==FALSE)
		{
			printf("\nSocket error when reading the command\n");
			break;
		}

        printf("\nCommand=%d\n",p->currentCommand);

		switch(p->currentCommand)
		{
		case EXT_CONNECT:
			printf("\nGet a EXT_CONNECT message...\n");
			if(responseEXT_CONNECT(p)==FALSE)
			{
				printf("\nSocket error when processing EXT_CONNECT\n");
				loop=false;
			}
			break;
        case EXT_GET_PARAMSIGNAL:
			printf("\nGet a EXT_GET_PARAMSIGNAL message...\n");
			if(responseEXT_GET_PARAMSIGNAL(p)==FALSE)
			{
				printf("\nSocket error when processing EXT_GET_PARAMSIGNAL\n");
				loop=false;
			}
			break;
		case EXT_SELECT_SIGNALS:
			printf("\nGet a EXT_SELECT_SIGNALS message...\n");
			if(responseEXT_SELECT_SIGNALS(p)==FALSE)
			{
				printf("\nSocket error when processing EXT_SELECT_SIGNALS\n");
				loop=false;
			}
			break;
		case EXT_SETSAMPLETIME:
			printf("\nGet a EXT_SETSAMPLETIME message...\n");
			if(responseEXT_SETSAMPLETIME(p)==FALSE)
			{
				printf("\nSocket error when processing EXT_SETSAMPLETIME\n");
				loop=false;
				break;
			}
			startUpload(p);
			break;
		case EXT_SETPARAM:
			printf("\nGet a EXT_SETPARAM message...\n");
			if(responseEXT_SETPARAM(p)==FALSE)
			{
				printf("\nSocket error when processing EXT_SETPARAM\n");
				loop=false;
				break;
			}
			break;
        }
    }

    closeClient(p);
    return TRUE;

}

BOOL responseEXT_CONNECT(CLIENT_STRUCT *p)
{
	SOCKET socket=p->socket;

	int ret;
	uint_T reponseCommand=EXT_CONNECT_RESPONSE;
	uint_T reponseSize=0;

	uint_T bodySize;

	ret=recv(socket,(char *)&bodySize,sizeof(uint_T),MSG_WAITALL);
	if(ret==SOCKET_ERROR||ret==0)
	{
		return FALSE;
	}

	EnterCriticalSection(&(p->socketCritical));

	ret=send(socket,(char *)&reponseCommand,sizeof(uint_T),0);
	if(ret==SOCKET_ERROR)
	{
		return FALSE;
	}

	ret=send(socket,(char *)&reponseSize,sizeof(uint_T),0);
	if(ret==SOCKET_ERROR)
	{
		return FALSE;
	}

	LeaveCriticalSection(&(p->socketCritical));

	return TRUE;
}

BOOL uploadSignals(CLIENT_STRUCT *p)
{
	SOCKET socket=p->socket;
	MODEL *mp=p->pExtModeData->mp;
	int ret;
	uint_T i;

	SIGNAL **signals=dataApiGetSignals(mp);

	uint_T num=dataApiGetNumSignals(mp);

	for(i=0;i<num;i++){
		SIGNAL *signal=dataApiGetSignal(signals,i);

		char_T blockPath[PATH_LENGTH];
		char_T signalName[NAME_LENGTH];

		uint_T nRows;
		uint_T nCols;

		uint_T orientation=0;
		uint_T dataType=0;

		printf("%s\n",signal->path);
		printf("%s\n",signal->name);

		strcpy(blockPath,signal->path);
		strcpy(signalName,signal->name);

		nRows=signal->width;
		nCols=1;

		ret=send(socket,signalName,NAME_LENGTH,0);
		if(ret==SOCKET_ERROR)
		{
			return FALSE;
		}

		ret=send(socket,blockPath,PATH_LENGTH,0);
		if(ret==SOCKET_ERROR)
		{
			return FALSE;
		}

		ret=send(socket,(char *)&orientation,sizeof(uint_T),0);
		if(ret==SOCKET_ERROR)
		{
			return FALSE;
		}

		ret=send(socket,(char *)&dataType,sizeof(uint_T),0);
		if(ret==SOCKET_ERROR)
		{
			return FALSE;
		}

		ret=send(socket,(char *)&nRows,sizeof(uint_T),0);
		if(ret==SOCKET_ERROR)
		{
			return FALSE;
		}

		ret=send(socket,(char *)&nCols,sizeof(uint_T),0);
		if(ret==SOCKET_ERROR)
		{
			return FALSE;
		}

	}

	return TRUE;

}

BOOL uploadParameters(CLIENT_STRUCT *p)
{
	SOCKET socket=p->socket;
	MODEL *mp=p->pExtModeData->mp;
	int ret;
	uint_T i;

	PARAMETER **parameters=dataApiGetParameters(mp);

	uint_T num=dataApiGetNumParameters(mp);

	for(i=0;i<num;i++){
		PARAMETER *parameter=dataApiGetParameter(parameters,i);


		char_T blockPath[PATH_LENGTH];
		char_T parameterName[NAME_LENGTH];

		uint_T nRows;
		uint_T nCols;

		uint_T orientation=0;
		uint_T dataType=0;

		printf("%s\n",parameter->path);
		printf("%s\n",parameter->name);


		strcpy(blockPath,parameter->path);
		strcpy(parameterName,parameter->name);

		nRows=parameter->width;
		nCols=1;


		ret=send(socket,parameterName,NAME_LENGTH,0);
		if(ret==SOCKET_ERROR)
		{
			return FALSE;
		}

		ret=send(socket,blockPath,PATH_LENGTH,0);
		if(ret==SOCKET_ERROR)
		{
			return FALSE;
		}

		ret=send(socket,(char *)&orientation,sizeof(uint_T),0);
		if(ret==SOCKET_ERROR)
		{
			return FALSE;
		}

		ret=send(socket,(char *)&dataType,sizeof(uint_T),0);
		if(ret==SOCKET_ERROR)
		{
			return FALSE;
		}

		ret=send(socket,(char *)&nRows,sizeof(uint_T),0);
		if(ret==SOCKET_ERROR)
		{
			return FALSE;
		}

		ret=send(socket,(char *)&nCols,sizeof(uint_T),0);
		if(ret==SOCKET_ERROR)
		{
			return FALSE;
		}

	}
	return TRUE;

}

BOOL responseEXT_GET_PARAMSIGNAL(CLIENT_STRUCT *p)
{
	SOCKET socket=p->socket;
	MODEL *mp=p->pExtModeData->mp;

	int ret;
	uint_T responseCommand=EXT_GET_PARAMSIGNAL_RESPONSE;
	uint_T responseSize=0;

	uint_T signalNum=0;
	uint_T paramNum=0;

	uint_T bodySize;

	signalNum=dataApiGetNumSignals(mp);
	paramNum=dataApiGetNumParameters(mp);

	ret=recv(socket,(char *)&bodySize,sizeof(uint_T),MSG_WAITALL);
	if(ret==SOCKET_ERROR||ret==0)
	{
		return FALSE;
	}

	EnterCriticalSection(&(p->socketCritical));

	//Send command
	ret=send(socket,(char *)&responseCommand,sizeof(uint_T),0);
	if(ret==SOCKET_ERROR)
	{
		return FALSE;
	}

	//Send body size
	responseSize=sizeof(uint_T)*2+(NAME_LENGTH+PATH_LENGTH+sizeof(uint_T)*4)*(signalNum+paramNum);
	//responseSize=512;
	ret=send(socket,(char *)&responseSize,sizeof(uint_T),0);
	if(ret==SOCKET_ERROR)
	{
		return FALSE;
	}

	ret=send(socket,(char *)&signalNum,sizeof(uint_T),0);
	if(ret==SOCKET_ERROR)
	{
		return FALSE;
	}

	ret=send(socket,(char *)&paramNum,sizeof(uint_T),0);
	if(ret==SOCKET_ERROR)
	{
		return FALSE;
	}

	if(uploadSignals(p)==FALSE)
	{
		return FALSE;
	}

	if(uploadParameters(p)==FALSE)
	{
		return FALSE;
	}

	LeaveCriticalSection(&(p->socketCritical));

	return TRUE;
}

BOOL responseEXT_SELECT_SIGNALS(CLIENT_STRUCT *p)
{
	SOCKET socket=p->socket;
	MODEL *mp=p->pExtModeData->mp;

	int ret;
	uint_T reponseCommand=EXT_SELECT_SIGNALS_RESPONSE;
	uint_T reponseSize=0;

	uint_T bodySize;
	uint_T num;

	uint_T i;

	SELECT *select,*oldSelect;

	ret=recv(socket,(char *)&bodySize,sizeof(uint_T),MSG_WAITALL);
	if(ret==SOCKET_ERROR||ret==0)
	{
		return FALSE;
	}

	ret=recv(socket,(char *)&num,sizeof(uint_T),MSG_WAITALL);
	if(ret==SOCKET_ERROR||ret==0)
	{
		return FALSE;
	}

	//printf("Body size=%d\n",bodySize);
	printf("Selected signal and parameter number=%d\n",num);
	//printf("Data size=%d",sizeof(SELECT)*num);

	select=(SELECT *)malloc(sizeof(SELECT)*num);

	ret=recv(socket,(char *)select,sizeof(SELECT)*num,MSG_WAITALL);
	if(ret==SOCKET_ERROR||ret==0)
	{
		return FALSE;
	}

	for(i=0;i<num;i++)
	{
		printf("%d_%d_%d_%d, ",select[i].type,select[i].pos,select[i].row,select[i].col);
	}
	printf("\n");

	terminateUploadThread(p);

	oldSelect=p->select;
	p->select=select;
	p->selectNum=num;

	if(oldSelect)
	{
		free(oldSelect);
	}

	//Return EXT_SELECT_SIGNALS_RESPONSE
	EnterCriticalSection(&(p->socketCritical));

	ret=send(socket,(char *)&reponseCommand,sizeof(uint_T),0);
	if(ret==SOCKET_ERROR)
	{
		return FALSE;
	}

	ret=send(socket,(char *)&reponseSize,sizeof(uint_T),0);
	if(ret==SOCKET_ERROR)
	{
		return FALSE;
	}

	LeaveCriticalSection(&(p->socketCritical));

	return TRUE;
}

BOOL responseEXT_SETSAMPLETIME(CLIENT_STRUCT *p)
{
	SOCKET socket=p->socket;
	MODEL *mp=p->pExtModeData->mp;

	int ret;
	uint_T reponseCommand=EXT_SETSAMPLETIME_RESPONSE;
	uint_T reponseSize=sizeof(real_T);

	uint_T bodySize;

	uint_T packetSize;
	real_T sampleTime;
	real_T stepSize;

	ret=recv(socket,(char *)&bodySize,sizeof(uint_T),MSG_WAITALL);
	if(ret==SOCKET_ERROR||ret==0)
	{
		return FALSE;
	}

	ret=recv(socket,(char *)&sampleTime,sizeof(real_T),MSG_WAITALL);
	if(ret==SOCKET_ERROR||ret==0)
	{
		return FALSE;
	}

	ret=recv(socket,(char *)&packetSize,sizeof(uint_T),MSG_WAITALL);
	if(ret==SOCKET_ERROR||ret==0)
	{
		return FALSE;
	}

	p->sampleTime=sampleTime;
	p->packetSize=packetSize;

	printf("PacketSize=%d\n",packetSize);
	printf("SampleTime=%f\n",sampleTime);

	//Return EXT_SETSAMPLETIME_RESPONSE
	EnterCriticalSection(&(p->socketCritical));

	ret=send(socket,(char *)&reponseCommand,sizeof(uint_T),0);
	if(ret==SOCKET_ERROR)
	{
		return FALSE;
	}

	ret=send(socket,(char *)&reponseSize,sizeof(uint_T),0);
	if(ret==SOCKET_ERROR)
	{
		return FALSE;
	}

	//stepSize=getStepSize();
	//printf("My SampleTime=%f\n",stepSize);

	ret=send(socket,(char *)&stepSize,sizeof(real_T),0);
	if(ret==SOCKET_ERROR)
	{
		return FALSE;
	}

	LeaveCriticalSection(&(p->socketCritical));

	return TRUE;
}

BOOL responseEXT_SETPARAM(CLIENT_STRUCT *p)
{
	SOCKET socket=p->socket;
	MODEL *mp=p->pExtModeData->mp;

	real_T *sec;

	int ret;
	uint_T reponseCommand=EXT_SETSAMPLETIME_RESPONSE;
	uint_T reponseSize=sizeof(real_T);

	uint_T bodySize;
	uint_T pos,row,col;
	real_T value;

	PARAMETER **parameters=dataApiGetParameters(mp);
	PARAMETER *parameter;

	ret=recv(socket,(char *)&bodySize,sizeof(uint_T),MSG_WAITALL);
	if(ret==SOCKET_ERROR||ret==0)
	{
		return FALSE;
	}

	ret=recv(socket,(char *)&pos,sizeof(uint_T),MSG_WAITALL);
	if(ret==SOCKET_ERROR||ret==0)
	{
		return FALSE;
	}

	ret=recv(socket,(char *)&row,sizeof(uint_T),MSG_WAITALL);
	if(ret==SOCKET_ERROR||ret==0)
	{
		return FALSE;
	}

	ret=recv(socket,(char *)&col,sizeof(uint_T),MSG_WAITALL);
	if(ret==SOCKET_ERROR||ret==0)
	{
		return FALSE;
	}

	ret=recv(socket,(char *)&value,sizeof(real_T),MSG_WAITALL);
	if(ret==SOCKET_ERROR||ret==0)
	{
		return FALSE;
	}

	parameter=dataApiGetParameter(parameters,pos);
	sec=(REAL *)(parameter->vp);
	*sec=value;

	printf("Pos=%d\n",pos);
	printf("Value=%f\n",value);

	return TRUE;
}

void closeClient(CLIENT_STRUCT *p)
{
	terminateUploadThread(p);
	closesocket(p->socket);

	CloseHandle(p->upload.hEvent);
	p->isEmpty=TRUE;

	DeleteCriticalSection(&(p->socketCritical));
	DeleteCriticalSection(&(p->listCritical));

	printf("Client closed\n");
}

BOOL readCom(CLIENT_STRUCT *p)
{
	uint_T com;

	int ret;

	SOCKET socket=p->socket;

	ret=recv(socket,(char *)&(p->currentCommand),sizeof(p->currentCommand),MSG_WAITALL);
	if(ret==SOCKET_ERROR||ret==0)
	{
		return FALSE;
	}
	return TRUE;
}
