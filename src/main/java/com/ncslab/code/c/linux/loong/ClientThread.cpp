#include <iostream>
#include <cmath>

#include "ClientThread.hpp"

#include "DataApi.hpp"
#include "UploadThread.hpp"
#include "Matrix.hpp"
#include <unistd.h>
#include <string.h>
#include <arpa/inet.h>

#ifndef MSG_WAITALL
	#define MSG_WAITALL 0x08
#endif

void * ClientThreadFunction( void * lpParam );
bool uploadMMI(CLIENT_STRUCT *p);
void closeClient(CLIENT_STRUCT *p);
bool readCom(CLIENT_STRUCT *p);

bool uploadVersion(CLIENT_STRUCT *p);
bool uploadSignals(CLIENT_STRUCT *p);
bool uploadBlockParameters(CLIENT_STRUCT *p);
bool uploadModelParameters(CLIENT_STRUCT *p);
bool setParameter(CLIENT_STRUCT *p);


bool responseEXT_CONNECT(CLIENT_STRUCT *p);
bool responseEXT_GET_PARAMSIGNAL(CLIENT_STRUCT *p);
bool responseEXT_SELECT_SIGNALS(CLIENT_STRUCT *p);
bool responseEXT_SETSAMPLETIME(CLIENT_STRUCT *p);
bool responseEXT_SETPARAM(CLIENT_STRUCT *p);

void createClientThread(CLIENT_STRUCT *p)
{
	//HANDLE  hClientThread;

    int ret;
    pthread_t id;
	
	printf("Creating client thread...\n");
    pthread_mutex_init(&(p->socketCritical),NULL);
    pthread_mutex_init(&(p->listCritical),NULL);

    ret=pthread_create(&id,NULL,ClientThreadFunction,p);


    if(ret!=0)
    {
        printf ("Create pthread error!\n");
        //exit (1);
    }
	
}

bool terminateUploadThread(CLIENT_STRUCT *p)
{
	if(p->upload.hUploadThread!=0)
	{
		//Wait for the old thread to stop.
		printf("Old upload thread didn't stop. Wait for it to stop\n");
		p->upload.loop=false;
		//WaitForSingleObject(p->upload.hUploadThread,INFINITE);
		printf("Old upload thread stopped\n");
	}

	return true;
}

bool startUpload(CLIENT_STRUCT *p)
{
	//terminateUploadThread(p);
	printf("Starting new upload thread.\n");
	createUploadThread(p);
	return true;
}

void * ClientThreadFunction( void * lpParam )
{


	bool loop=true;

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
		if(readCom(p)==false)
		{
			printf("\nSocket error when reading the command\n");
			break;
		}

        printf("\nCommand=%d\n",p->currentCommand);

		switch(p->currentCommand)
		{
		case EXT_CONNECT:
			printf("\nGet a EXT_CONNECT message...\n");
			if(responseEXT_CONNECT(p)==false)
			{
				printf("\nSocket error when processing EXT_CONNECT\n");
				loop=false;
			}
			break;
        case EXT_GET_PARAMSIGNAL:
			printf("\nGet a EXT_GET_PARAMSIGNAL message...\n");
			if(responseEXT_GET_PARAMSIGNAL(p)==false)
			{
				printf("\nSocket error when processing EXT_GET_PARAMSIGNAL\n");
				loop=false;
			}
			break;
		case EXT_SELECT_SIGNALS:
			printf("\nGet a EXT_SELECT_SIGNALS message...\n");
			if(responseEXT_SELECT_SIGNALS(p)==false)
			{
				printf("\nSocket error when processing EXT_SELECT_SIGNALS\n");
				loop=false;
			}
			break;
		case EXT_SETSAMPLETIME:
			printf("\nGet a EXT_SETSAMPLETIME message...\n");
			if(responseEXT_SETSAMPLETIME(p)==false)
			{
				printf("\nSocket error when processing EXT_SETSAMPLETIME\n");
				loop=false;
				break;
			}
			startUpload(p);
			break;
		case EXT_SETPARAM:
			printf("\nGet a EXT_SETPARAM message...\n");
			if(responseEXT_SETPARAM(p)==false)
			{
				printf("\nSocket error when processing EXT_SETPARAM\n");
				loop=false;
				break;
			}
			break;
        }
    }

    closeClient(p);

    return NULL;

}

bool responseEXT_CONNECT(CLIENT_STRUCT *p)
{
	int socket=p->socket;

	int ret;
	uint_T reponseCommand=EXT_CONNECT_RESPONSE;
	uint_T reponseSize=0;

	uint_T bodySize;

	ret=recv(socket,(char *)&bodySize,sizeof(uint_T),MSG_WAITALL);
	if(ret==-1||ret==0)
	{
		return false;
	}

	pthread_mutex_lock(&(p->socketCritical));

	ret=send(socket,(char *)&reponseCommand,sizeof(uint_T),0);
	if(ret==-1)
	{
		return false;
	}

	ret=send(socket,(char *)&reponseSize,sizeof(uint_T),0);
	if(ret==-1)
	{
		return false;
	}

	pthread_mutex_unlock(&(p->socketCritical));

	return true;
}

bool uploadSignals(CLIENT_STRUCT *p)
{
	int socket=p->socket;
	MODEL *mp=p->pExtModeData->mp;
	int ret;
	uint_T i;

	SIGNAL **signals=dataApiGetSignals(mp);

	uint_T num=dataApiGetNumSignals(mp);

	printf("Singnal Num %d\n",num);

	for(i=0;i<num;i++){
		SIGNAL *signal=dataApiGetSignal(signals,i);

		char_T blockPath[PATH_LENGTH];
		char_T signalName[NAME_LENGTH];

		uint_T nRows;
		uint_T nCols;

		uint_T orientation=0;
		uint_T dataType=0;

		printf("Signal %d:%s\n",i,signal->path);
		printf("%s\n",signal->name);
		printf("Type: %d\n",signal->type);
		printf("Height: %d\n",signal->height);
		printf("Width: %d\n",signal->width);

		strcpy(blockPath,signal->path);
		strcpy(signalName,signal->name);

		nRows=signal->height;
		nCols=signal->width;

		ret=send(socket,signalName,NAME_LENGTH,0);
		if(ret==-1)
		{
			return false;
		}

		ret=send(socket,blockPath,PATH_LENGTH,0);
		if(ret==-1)
		{
			return false;
		}

		ret=send(socket,(char *)&orientation,sizeof(uint_T),0);
		if(ret==-1)
		{
			return false;
		}

		ret=send(socket,(char *)&dataType,sizeof(uint_T),0);
		if(ret==-1)
		{
			return false;
		}

		ret=send(socket,(char *)&nRows,sizeof(uint_T),0);
		if(ret==-1)
		{
			return false;
		}

		ret=send(socket,(char *)&nCols,sizeof(uint_T),0);
		if(ret==-1)
		{
			return false;
		}

	}

	return true;

}

bool uploadParameters(CLIENT_STRUCT *p)
{
	int socket=p->socket;
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

		printf("Parameter %d:%s\n",i,parameter->path);
		printf("%s\n",parameter->name);
		printf("Type: %d\n",parameter->type);
		printf("Height: %d\n",parameter->height);
		printf("Width: %d\n",parameter->width);


		strcpy(blockPath,parameter->path);
		strcpy(parameterName,parameter->name);

		nRows=parameter->height;
		nCols=parameter->width;


		ret=send(socket,parameterName,NAME_LENGTH,0);
		if(ret==-1)
		{
			return false;
		}

		ret=send(socket,blockPath,PATH_LENGTH,0);
		if(ret==-1)
		{
			return false;
		}

		ret=send(socket,(char *)&orientation,sizeof(uint_T),0);
		if(ret==-1)
		{
			return false;
		}

		ret=send(socket,(char *)&dataType,sizeof(uint_T),0);
		if(ret==-1)
		{
			return false;
		}

		ret=send(socket,(char *)&nRows,sizeof(uint_T),0);
		if(ret==-1)
		{
			return false;
		}

		ret=send(socket,(char *)&nCols,sizeof(uint_T),0);
		if(ret==-1)
		{
			return false;
		}

	}

	return true;

}

bool responseEXT_GET_PARAMSIGNAL(CLIENT_STRUCT *p)
{
	int socket=p->socket;
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
	if(ret==-1||ret==0)
	{
		return false;
	}

	pthread_mutex_lock(&(p->socketCritical));

	//Send command
	ret=send(socket,(char *)&responseCommand,sizeof(uint_T),0);
	if(ret==-1)
	{
		return false;
	}

	//Send body size
	responseSize=sizeof(uint_T)*2+(NAME_LENGTH+PATH_LENGTH+sizeof(uint_T)*4)*(signalNum+paramNum);
	//responseSize=512;
	ret=send(socket,(char *)&responseSize,sizeof(uint_T),0);
	if(ret==-1)
	{
		return false;
	}

	ret=send(socket,(char *)&signalNum,sizeof(uint_T),0);
	if(ret==-1)
	{
		return false;
	}

	ret=send(socket,(char *)&paramNum,sizeof(uint_T),0);
	if(ret==-1)
	{
		return false;
	}

	if(uploadSignals(p)==false)
	{
		return false;
	}

	//printf("Signal finished\n");

	if(uploadParameters(p)==false)
	{
		return false;
	}

	pthread_mutex_unlock(&(p->socketCritical));

	return true;
}

bool responseEXT_SELECT_SIGNALS(CLIENT_STRUCT *p)
{
	int socket=p->socket;
	MODEL *mp=p->pExtModeData->mp;

	int ret;
	uint_T reponseCommand=EXT_SELECT_SIGNALS_RESPONSE;
	uint_T reponseSize=0;

	uint_T bodySize;
	uint_T num;

	uint_T i;

	SELECT *select,*oldSelect;

	ret=recv(socket,(char *)&bodySize,sizeof(uint_T),MSG_WAITALL);
	if(ret==-1||ret==0)
	{
		return false;
	}

	ret=recv(socket,(char *)&num,sizeof(uint_T),MSG_WAITALL);
	if(ret==-1||ret==0)
	{
		return false;
	}

	//printf("Body size=%d\n",bodySize);
	printf("Selected signal and parameter number=%d\n",num);
	//printf("Data size=%d",sizeof(SELECT)*num);

	select=(SELECT *)malloc(sizeof(SELECT)*num);

	ret=recv(socket,(char *)select,sizeof(SELECT)*num,MSG_WAITALL);
	if(ret==-1||ret==0)
	{
		return false;
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
	pthread_mutex_lock(&(p->socketCritical));

	ret=send(socket,(char *)&reponseCommand,sizeof(uint_T),0);
	if(ret==-1)
	{
		return false;
	}

	ret=send(socket,(char *)&reponseSize,sizeof(uint_T),0);
	if(ret==-1)
	{
		return false;
	}

	pthread_mutex_unlock(&(p->socketCritical));

	return true;
}

bool responseEXT_SETSAMPLETIME(CLIENT_STRUCT *p)
{
	int socket=p->socket;
	MODEL *mp=p->pExtModeData->mp;

	int ret;
	uint_T reponseCommand=EXT_SETSAMPLETIME_RESPONSE;
	uint_T reponseSize=sizeof(real_T);

	uint_T bodySize;

	uint_T packetSize;
	real_T sampleTime;
	real_T stepSize;

	ret=recv(socket,(char *)&bodySize,sizeof(uint_T),MSG_WAITALL);
	if(ret==-1||ret==0)
	{
		return false;
	}

	ret=recv(socket,(char *)&sampleTime,sizeof(real_T),MSG_WAITALL);
	if(ret==-1||ret==0)
	{
		return false;
	}

	ret=recv(socket,(char *)&packetSize,sizeof(uint_T),MSG_WAITALL);
	if(ret==-1||ret==0)
	{
		return false;
	}

	p->sampleTime=sampleTime;
	p->packetSize=packetSize;

	printf("PacketSize=%d\n",packetSize);
	printf("SampleTime=%f\n",sampleTime);

	//Return EXT_SETSAMPLETIME_RESPONSE
	pthread_mutex_lock(&(p->socketCritical));

	ret=send(socket,(char *)&reponseCommand,sizeof(uint_T),0);
	if(ret==-1)
	{
		return false;
	}

	ret=send(socket,(char *)&reponseSize,sizeof(uint_T),0);
	if(ret==-1)
	{
		return false;
	}

	//stepSize=getStepSize();
	//printf("My SampleTime=%f\n",stepSize);

	ret=send(socket,(char *)&stepSize,sizeof(real_T),0);
	if(ret==-1)
	{
		return false;
	}

	pthread_mutex_unlock(&(p->socketCritical));

	return true;
}

bool responseEXT_SETPARAM(CLIENT_STRUCT *p)
{
	int socket=p->socket;
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
	if(ret==-1||ret==0)
	{
		return false;
	}

	ret=recv(socket,(char *)&pos,sizeof(uint_T),MSG_WAITALL);
	if(ret==-1||ret==0)
	{
		return false;
	}

	ret=recv(socket,(char *)&row,sizeof(uint_T),MSG_WAITALL);
	if(ret==-1||ret==0)
	{
		return false;
	}

	ret=recv(socket,(char *)&col,sizeof(uint_T),MSG_WAITALL);
	if(ret==-1||ret==0)
	{
		return false;
	}

	ret=recv(socket,(char *)&value,sizeof(real_T),MSG_WAITALL);
	if(ret==-1||ret==0)
	{
		return false;
	}


	parameter=dataApiGetParameter(parameters,pos);
	//sec=(REAL *)(parameter->vp)+parameter->width*row+col;
	//*sec=value;

	switch (parameter->type)
	{
	case DATA_TYPE::SINGLE:
		sec=(REAL *)(parameter->vp);
		*sec=value;
		break;
	case DATA_TYPE::MATRIX:
		Matrix *matrix;
		matrix=(Matrix *)(parameter->vp);
		(*matrix)(row,col)=value;
		break;

	default:
		break;
	}

	printf("Pos=%d\n",pos);
	printf("Value=%f\n",value);

	return true;
}

void closeClient(CLIENT_STRUCT *p)
{
	//terminateUploadThread(p);
	close(p->socket);

	//CloseHandle(p->upload.hEvent);
	p->isEmpty=true;


	printf("Client closed\n");
}

bool readCom(CLIENT_STRUCT *p)
{
	uint_T com;

	int ret;

	int socket=p->socket;

	ret=recv(socket,(char *)&(p->currentCommand),sizeof(p->currentCommand),MSG_WAITALL);
	if(ret==-1||ret==0) {
		
		return false;
	}
	return true;
}

