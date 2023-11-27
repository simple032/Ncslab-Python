#include "ServerThread.h"
#include "clientThread.h"

#define DEFAULT_PORT "27015"

#define MAX_CLIENT_NUM 16

static HANDLE hServerThread;
static DWORD hServerThreadId;

ExtModeData extModeData;

DWORD WINAPI ServerThreadFunction( LPVOID lpParam );

static CLIENT_STRUCT clients[MAX_CLIENT_NUM];

void startMyServerThread(ExtModeData pExtModeData)
{
	extModeData=pExtModeData;

	printf("Server Thread starting...\n");


	hServerThread = CreateThread(
            NULL,                   // default security attributes
            0,                      // use default stack size
            ServerThreadFunction,       // thread function name
            &extModeData,          // argument to thread function
            0,                      // use default creation flags
            &hServerThreadId);   // returns the thread identifier


}

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
}