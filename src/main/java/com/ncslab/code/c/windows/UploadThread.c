#include "winsock2.h"
#include "UploadThread.hpp"
#include "ncslabccode.h"
#include "DataApi.h"

#ifndef MSG_WAITALL
	#define MSG_WAITALL 0x08
#endif

DWORD WINAPI UploadThreadFunction( LPVOID lpParam );
void onUploadTermination(CLIENT_STRUCT *p);
void readRealTimeData(CLIENT_STRUCT *p);
BOOL uploadRealTimeData(CLIENT_STRUCT *p);

void createUploadThread(CLIENT_STRUCT *p)
{
	HANDLE  hUploadThread;
    DWORD  hUploadThreadId;

	printf("Creating upload thread...\n");

	hUploadThread = CreateThread(
            NULL,                   // default security attributes
            0,                      // use default stack size
            UploadThreadFunction,       // thread function name
            p,          // argument to thread function
            0,                      // use default creation flags
            &hUploadThreadId);   // returns the thread identifier

	p->upload.hUploadThread=hUploadThread;
	p->upload.loop=TRUE;
}

BOOL allocateMemory(CLIENT_STRUCT *p)
{
	uint_T num=(p->packetSize)*(p->selectNum+1)*sizeof(real_T);
	printf("Allocating %d values for uploading buffer\n",num);
	p->upload.data=(double *)malloc(num+100);

	p->upload.totalSize=num;

	if(p->upload.data==NULL)
	{
		return FALSE;
	}
	return TRUE;
}

BOOL packagingData(CLIENT_STRUCT *p,uint_T n)
{
	real_T *pos;
	real_T *sec;

	uint_T i;
	uint_T subPos;

	MODEL *mp=p->pExtModeData->mp;
	
	SIGNAL **signals=dataApiGetSignals(mp);
	PARAMETER **parameters=dataApiGetParameters(mp);

	//uint_T signalNum=dataApiGetNumSignals(mp);

	pos=p->upload.data+n*(p->selectNum+1);

	*pos++=mp->time;

	for(i=0;i<p->selectNum;i++)
	{
        REAL value=0;
		if(p->select[i].type==1)
		{
			SIGNAL *signal=dataApiGetSignal(signals,p->select[i].pos);
            value=*((REAL *)(signal->vp));
		}
		else
		if(p->select[i].type==2)
		{
			PARAMETER *parameter=dataApiGetParameter(parameters,p->select[i].pos);
            value=*((REAL *)(parameter->vp));
		}
		else
		{
			return FALSE;
		}

        *pos=value;
		//printf("%f\t",*pos);

		pos++;
	}

	//printf("\n");

	return TRUE;
}

BOOL responseEXT_UPLOAD_DATA(CLIENT_STRUCT *p)
{
	SOCKET socket=p->socket;
	MODEL *mp=p->pExtModeData->mp;

	int ret;
	uint_T reponseCommand=EXT_UPLOAD_DATA;
	uint_T reponseSize=p->upload.totalSize;

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

	ret=send(socket,(char *)p->upload.data,p->upload.totalSize,0);
	if(ret==SOCKET_ERROR)
	{
		return FALSE;
	}
	LeaveCriticalSection(&(p->socketCritical));

	return TRUE;
}

DWORD WINAPI UploadThreadFunction( LPVOID lpParam )
{
	CLIENT_STRUCT *p;
	MODEL *mp;

	uint_T fetch;

	p=(CLIENT_STRUCT *)lpParam;
	mp=p->pExtModeData->mp;

	printf("\nUpload thread started\n");
	printf("Packet size=%d\n",p->packetSize);

    if(allocateMemory(p)==FALSE)
	{
		printf("Allocating memory for upload data failed.\n");
		return 0;
	}
	
	while(p->upload.loop)
	{
		WaitForSingleObject(p->upload.hEvent,INFINITE);

		//printf("Uploading...%f\n",mp->time);

		packagingData(p,fetch);

		fetch++;

		if(fetch==p->packetSize)
		{
			responseEXT_UPLOAD_DATA(p);
			fetch=0;
		}

	}

    onUploadTermination(p);
    
    return 0;
}

void onUploadTermination(CLIENT_STRUCT *p)
{
	printf("\nUpload termination!\n");

	if(p->upload.data)
	{
		free(p->upload.data);
	}
}