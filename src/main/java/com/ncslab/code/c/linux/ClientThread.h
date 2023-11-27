#ifndef CLIENTTHREAD
#define CLIENTTHREAD
#include <float.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdbool.h>

#include "ncslabccode.h"
#include "ServerThread.h"

#define UPLOAD_SIG_PARAM 0x01
#define SELECT_SIG_PARAM 0x02
#define SET_PARAM 0x03

#define EXT_CONNECT 0
#define EXT_CONNECT_RESPONSE 19
#define EXT_GET_PARAMSIGNAL 16
#define EXT_GET_PARAMSIGNAL_RESPONSE 32
#define EXT_SETSAMPLETIME 101
#define EXT_SETSAMPLETIME_RESPONSE 102
#define EXT_SELECT_SIGNALS 5
#define EXT_SELECT_SIGNALS_RESPONSE 6
#define EXT_MODEL_START 10
#define EXT_MODEL_START_RESPONSE 26
#define EXT_SETPARAM 3
#define EXT_SETPARAM_RESPONSE 21
#define EXT_UPLOAD_DATA 103
#define EXT_STOP_CONNECT 105

#define NAME_LENGTH 80
#define PATH_LENGTH 80

typedef struct 
{
	uint_T type;
	uint_T pos;
	uint_T row;
	uint_T col;
}SELECT;

typedef struct
{
	bool isEmpty;
	int socket;
	pthread_t  clientThread;
	uint_T currentCommand;

	pthread_mutex_t  socketCritical;

	SELECT *select;
	uint_T selectNum;

	real_T sampleTime;
	int_T packetSize;

	struct
	{
		int  hUploadThread;
		real_T *data;
		bool loop;
		pthread_mutex_t mutex;
		pthread_cond_t cond;
		uint_T totalSize;
	}upload;

	pthread_mutex_t  listCritical;

	ExtModeData *pExtModeData;
}CLIENT_STRUCT;

typedef struct
{
	char_T *blockPath; /* block's full path name (RTW mangled version)   */
	char_T *signalName;/* signal label (unmangled, NULL if not present)  */
	uint_T blockPathLen;
	uint_T signalNameLen;
	uint_T addrMapIndex;
	uint_T dimension;
}Signal;

typedef struct
{
	char_T *blockPath; /* block's full path name (RTW mangled version)   */
	char_T *paramName;
	uint_T blockPathLen;
	uint_T paramNameLen;
	uint_T addrMapIndex;
	uint_T dimension;
}BlockParameter;

typedef struct
{
	char_T *varName;
	uint_T varNameLen;
	uint_T addrMapIndex;
	uint_T dimension;
	uint_T reserved0;
}ModelParameter;

void createClientThread(CLIENT_STRUCT *p);

#endif