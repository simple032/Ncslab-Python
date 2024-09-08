#include "ServerThread.hpp"
#include "ClientThread.hpp"

#define DEFAULT_PORT 27015

#define MAX_CLIENT_NUM 16

ExtModeData *pExtModeDataG;

void* ServerThreadFunction(void *arg);

static CLIENT_STRUCT clients[MAX_CLIENT_NUM];

void startMyServerThread(ExtModeData *pExtModeData)
{
	pExtModeDataG=pExtModeData;

	printf("Server Thread starting...\n");

    pthread_t id;
    int ret;
    
    ret=pthread_create(&id,NULL,ServerThreadFunction,pExtModeDataG);


    if(ret!=0)
    {
        printf ("Create pthread error!\n");
        exit (1);
    }

    pExtModeDataG->servetThread=id;


}

void initClients()
{
	int i;
	for(i=0;i<MAX_CLIENT_NUM;i++)
	{
		clients[i].isEmpty=true;
	}
}

bool startClientThread(int socket)
{
	int i;
	printf("Start client thread\n");

	for(i=0;i<MAX_CLIENT_NUM;i++)
	{
		if(clients[i].isEmpty==true)
			break;
	}

	if(i==MAX_CLIENT_NUM)
	{
		printf("To many connections!\n");
		return false;
	}

	printf("Connection %d is available. Starting the client thread...\n",i);

	clients[i].socket=socket;
	clients[i].pExtModeData=pExtModeDataG;
	clients[i].currentCommand=0;
	//clients[i].upload.hUploadThread=0;
	clients[i].upload.data=NULL;
    pthread_cond_init(&(clients[i].upload.cond),NULL);
    pthread_mutex_init(&(clients[i].upload.mutex),NULL);
	clients[i].select=NULL;

	clients[i].isEmpty=false;

	createClientThread(&clients[i]);

	return true;
}

void* ServerThreadFunction(void *arg)
{
    int    listenfd, connfd;
    struct sockaddr_in     servaddr;
    int     n;
    int port;

    printf("Server Thread started...Ok\n");

    ExtModeData *pExtModeData=(ExtModeData *)arg;

    port=pExtModeData->port;

    if(port==0){
        port=DEFAULT_PORT;
    }

    //rtwCAPI_ModelMappingInfo *mmi=&(pExtModeData->mmi);

    if( (listenfd = socket(AF_INET, SOCK_STREAM, 0)) == -1 ){
        printf("create socket error: %s(errno: %d)\n",strerror(errno),errno);
        exit(0);
    }

    memset(&servaddr, 0, sizeof(servaddr));
    servaddr.sin_family = AF_INET;
    servaddr.sin_addr.s_addr = htonl(INADDR_ANY);
    servaddr.sin_port = htons(port);

    if( bind(listenfd, (struct sockaddr*)&servaddr, sizeof(servaddr)) == -1){
        printf("bind socket error: %s(errno: %d)\n",strerror(errno),errno);
        exit(0);
    }

    if( listen(listenfd, 10) == -1){
        printf("listen socket error: %s(errno: %d)\n",strerror(errno),errno);
        exit(0);
    }

    initClients();

    printf("======waiting for client's request======\n");
    while(1)
    {
        if( (connfd = accept(listenfd, (struct sockaddr*)NULL, NULL)) == -1){
            printf("accept socket error: %s(errno: %d)",strerror(errno),errno);
            continue;
        }

        printf("Accepted...\n");

        if(startClientThread(connfd)==false)
		{
			close(connfd);
			printf("Connection rejected!\n");
		}
    }

    return NULL;

}

void setAllClientUploadEvents()
{
	int_T i;
	for(i=0;i<MAX_CLIENT_NUM;i++)
	{
		if(clients[i].isEmpty==false)
		{
			//SetEvent(clients[i].upload.hEvent);
            pthread_cond_signal(&(clients[i].upload.cond));
		}
	}
}

/*
void initClients()
{
	int i;
	for(i=0;i<MAX_CLIENT_NUM;i++)
	{
		clients[i].isEmpty=TRUE;
	}
}


BOOL startClientThread(SOCKET socket)
{
	int i;
	printf("Start client thread\n");

	for(i=0;i<MAX_CLIENT_NUM;i++)
	{
		if(clients[i].isEmpty==TRUE)
			break;
	}

	if(i==MAX_CLIENT_NUM)
	{
		printf("To many connections!\n");
		return FALSE;
	}

	printf("Connection %d is available. Starting the client thread...\n",i);

	clients[i].socket=socket;
	clients[i].pExtModeData=&extModeData;
	clients[i].currentCommand=0;
	clients[i].upload.hUploadThread=0;
	clients[i].upload.data=NULL;
	clients[i].upload.hEvent=CreateEvent(NULL,FALSE,FALSE,NULL);
	clients[i].select=NULL;

	clients[i].isEmpty=FALSE;

	createClientThread(&clients[i]);

	return TRUE;
}

void setAllClientUploadEvents()
{
	int_T i;
	for(i=0;i<MAX_CLIENT_NUM;i++)
	{
		if(clients[i].isEmpty==FALSE)
		{
			SetEvent(clients[i].upload.hEvent);
		}
	}
}

DWORD WINAPI ServerThreadFunction( LPVOID lpParam )
{
	struct addrinfo *result = NULL, *ptr = NULL;
    struct addrinfo hints;

	WSADATA wsaData;

	int iResult;

	ExtModeData *pExtModeData=(ExtModeData *)lpParam;

	SOCKET ListenSocket = INVALID_SOCKET;

	SOCKET ClientSocket = INVALID_SOCKET;

	printf("Server Thread started...\n");

    iResult = WSAStartup(MAKEWORD(2, 2), &wsaData);
    if (iResult != 0) {
        printf("WSAStartup failed: %d\n", iResult);
        return 1;
    }


	ZeroMemory(&hints, sizeof (hints));
	hints.ai_family = AF_INET;
	hints.ai_socktype = SOCK_STREAM;
	hints.ai_protocol = IPPROTO_TCP;
	hints.ai_flags = AI_PASSIVE;

	// Resolve the local address and port to be used by the server
	if(pExtModeData->port==NULL){
		iResult = getaddrinfo(NULL, DEFAULT_PORT, &hints, &result);
		printf("Default Port %s \n",DEFAULT_PORT);
	}
	else{
		iResult = getaddrinfo(NULL, pExtModeData->port, &hints, &result);
		printf("customized Port %s \n",pExtModeData->port);
	}

	if (iResult != 0) {
		printf("getaddrinfo failed: %d\n", iResult);
		WSACleanup();
		return 1;
	}

    ListenSocket = socket(result->ai_family, result->ai_socktype, result->ai_protocol);

	if (ListenSocket == INVALID_SOCKET) {
		printf("Error at socket(): %ld\n", WSAGetLastError());
		freeaddrinfo(result);
		WSACleanup();
		return 1;
	}

	iResult = bind( ListenSocket, result->ai_addr, (int)result->ai_addrlen);
    if (iResult == SOCKET_ERROR) {
        printf("bind failed with error: %d\n", WSAGetLastError());
        freeaddrinfo(result);
        closesocket(ListenSocket);
        WSACleanup();
        return 1;
    }

	freeaddrinfo(result);

	if ( listen( ListenSocket, SOMAXCONN ) == SOCKET_ERROR ) {
		printf( "Listen failed with error: %ld\n", WSAGetLastError() );
		closesocket(ListenSocket);
		WSACleanup();
		return 1;
	}

	initClients();

	while(TRUE)
	{
		ClientSocket = accept(ListenSocket, NULL, NULL);
		if (ClientSocket == INVALID_SOCKET) {
			printf("accept failed: %d\n", WSAGetLastError());
			closesocket(ListenSocket);
			WSACleanup();
			return 1;

		}
		printf("Accepted...\n");

        if(startClientThread(ClientSocket)==FALSE)
		{
			closesocket(ClientSocket);
			printf("Connection rejected!\n");
		}
    }
}*/
