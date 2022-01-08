#include "UploadThread.h"
#include "ncslabccode.h"
#include "DataApi.h"

#ifndef MSG_WAITALL
	#define MSG_WAITALL 0x08
#endif

void * UploadThreadFunction( void * lpParam );
void onUploadTermination(CLIENT_STRUCT *p);
void readRealTimeData(CLIENT_STRUCT *p);
bool uploadRealTimeData(CLIENT_STRUCT *p);

void createUploadThread(CLIENT_STRUCT *p)
{
	int ret;
	pthread_t id;

	printf("Creating client thread...\n");

	ret=pthread_create(&id,NULL,UploadThreadFunction,p);


    if(ret!=0)
    {
        printf ("Create pthread error!\n");
		return;
        //exit (1);
    }

	p->upload.hUploadThread=id;
	p->upload.loop=true;
}

bool allocateMemory(CLIENT_STRUCT *p)
{
	uint_T num=(p->packetSize)*(p->selectNum+1)*sizeof(real_T);
	printf("Allocating %d values for uploading buffer\n",num);
	p->upload.data=(double *)malloc(num+100);

	p->upload.totalSize=num;

	if(p->upload.data==NULL)
	{
		return false;
	}
	return true;
}

bool packagingData(CLIENT_STRUCT *p,uint_T n)
{
	real_T *pos;
	real_T *sec;

	uint_T i;
	uint_T subPos;

	MODEL *mp=p->pExtModeData->mp;
	
	//printf("Packaging...\n");
	
	SIGNAL **signals=dataApiGetSignals(mp);
	PARAMETER **parameters=dataApiGetParameters(mp);
	
	//printf("Packaging1...\n");

	//uint_T signalNum=dataApiGetNumSignals(mp);

	pos=p->upload.data+n*(p->selectNum+1);
	
	//printf("Packaging2: %d %d...\n",p->selectNum,n);

	*pos++=mp->time;

	for(i=0;i<p->selectNum;i++)
	{
        REAL value=0;
        //printf("%d\n",i);
		if(p->select[i].type==1)
		{
			SIGNAL *signal=dataApiGetSignal(signals,p->select[i].pos);
			//printf("%d\t%d\t%d\t%d\t",p->select[i].row,p->select[i].col,signal->height,signal->width);
            value=*((REAL *)(signal->vp));
		}
		else
		if(p->select[i].type==2)
		{
			PARAMETER *parameter=dataApiGetParameter(parameters,p->select[i].pos);
			//printf("%d\t%d\t%d\t%d\t",p->select[i].row,p->select[i].col,parameter->height,parameter->width);
            value=*((REAL *)(parameter->vp)+parameter->width*p->select[i].row+p->select[i].col);
		}
		else
		{
			return false;
		}

        *pos=value;
		//printf("%f\t",*pos);

		pos++;
	}

	//printf("\n");

	return true;
}

bool responseEXT_UPLOAD_DATA(CLIENT_STRUCT *p)
{
	int socket=p->socket;
	MODEL *mp=p->pExtModeData->mp;

	int ret;
	uint_T reponseCommand=EXT_UPLOAD_DATA;
	uint_T reponseSize=p->upload.totalSize;

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

	ret=send(socket,(char *)p->upload.data,p->upload.totalSize,0);
	if(ret==-1)
	{
		return false;
	}
	pthread_mutex_unlock(&(p->socketCritical));

	return true;
}

void * UploadThreadFunction( void * lpParam )
{
	CLIENT_STRUCT *p;
	MODEL *mp;

	uint_T fetch;

	p=(CLIENT_STRUCT *)lpParam;
	mp=p->pExtModeData->mp;

	printf("\nUpload thread started\n");
	printf("Packet size=%d\n",p->packetSize);

    if(allocateMemory(p)==false)
	{
		printf("Allocating memory for upload data failed.\n");
		return 0;
	}
	
	fetch=0;
	while(p->upload.loop)
	{
		pthread_mutex_lock(&(p->upload.mutex));
		pthread_cond_wait(&(p->upload.cond),&(p->upload.mutex));
		pthread_mutex_unlock(&(p->upload.mutex));

		//printf("Uploading...%f\n",mp->time);

		packagingData(p,fetch);
		
		//printf("Packaged...%f\n",mp->time);

		fetch++;

		if(fetch==p->packetSize)
		{
			responseEXT_UPLOAD_DATA(p);
			fetch=0;
		}

	}

    onUploadTermination(p);
}

void onUploadTermination(CLIENT_STRUCT *p)
{
	printf("\nUpload termination!\n");

	if(p->upload.data)
	{
		free(p->upload.data);
	}
}