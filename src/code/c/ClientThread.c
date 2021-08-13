#include "ClientThread.h"
#include "ncslabccode.h"
#include "DataApi.h"

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
        }
    }

    closeClient(p);

}

BOOL responseEXT_CONNECT(CLIENT_STRUCT *p)
{
	SOCKET socket=p->socket;
	//rtwCAPI_ModelMappingInfo *mmi=&(p->pExtModeData->mmi);

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

void closeClient(CLIENT_STRUCT *p)
{
	//terminateUploadThread(p);
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
	//rtwCAPI_ModelMappingInfo *mmi=&(p->pExtModeData->mmi);

	ret=recv(socket,(char *)&(p->currentCommand),sizeof(p->currentCommand),MSG_WAITALL);
	if(ret==SOCKET_ERROR||ret==0)
	{
		return FALSE;
	}
	return TRUE;
}