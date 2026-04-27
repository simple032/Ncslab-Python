#include <cstdlib>
#include <cstdio>
#include "mainccode.hpp"
#include "ncslabdefines.hpp"
#include "onestep.hpp"
#include "results.hpp"
#include "ncslab.hpp"
#include "util.hpp"

#ifdef _WIN32
#include <io.h>
#include <fcntl.h>
#include <winsock2.h>
#include <ws2tcpip.h>
#pragma comment(lib, "ws2_32.lib")
#else
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <unistd.h>
#include <fcntl.h>
#endif

MODEL* mp;

extern int g_simSocket;

time_t main_timer;

// argv[0] is the file path of the executable
// argv[1] is the simulation stop time
// argv[2] is the TCP port number (optional, if provided use TCP instead of stdout)
int main(int argc, char* argv[]) {

	double endTime = 10;
	double end = -1;
	PROGRESSTYPE progressType=Ending;
	NCSLabInit();

	if (argc >= 2) {
		endTime = atof(argv[1]);
	}

	// TCP mode: if port is provided, connect to Java backend
	if (argc >= 3) {
		int port = atoi(argv[2]);
#ifdef _WIN32
		WSADATA wsaData;
		WSAStartup(MAKEWORD(2, 2), &wsaData);
#endif
		g_simSocket = socket(AF_INET, SOCK_STREAM, 0);
		if (g_simSocket >= 0) {
			struct sockaddr_in addr;
			addr.sin_family = AF_INET;
			addr.sin_port = htons(port);
			addr.sin_addr.s_addr = inet_addr("127.0.0.1");
			if (connect(g_simSocket, (struct sockaddr*)&addr, sizeof(addr)) == 0) {
				// Disable Nagle for low latency
				int flag = 1;
				setsockopt(g_simSocket, IPPROTO_TCP, TCP_NODELAY, (char*)&flag, sizeof(int));
#ifdef _WIN32
				u_long mode = 1;
				ioctlsocket(g_simSocket, FIONBIO, &mode);
#else
				int flags = fcntl(g_simSocket, F_GETFL, 0);
				fcntl(g_simSocket, F_SETFL, flags | O_NONBLOCK);
#endif
			} else {
#ifdef _WIN32
				closesocket(g_simSocket);
#else
				close(g_simSocket);
#endif
				g_simSocket = -1;
			}
		}
	}

#ifdef _WIN32
	if (g_simSocket < 0) {
		// Only set stdout to binary when using stdout fallback
		_setmode(_fileno(stdout), _O_BINARY);
	}
#endif

	mp = NCSLabGetModelP();

	mp->time = mp->startTime;
	mp->stopTime = endTime;

	// Send initial display values after initialization
	NCSLabOutput();
	NCSLabSinkOutput();
	sendDisplayUpdateForce();

	ncslabLoop();

	// Send final display values before termination
	sendDisplayUpdateForce();

	NCSLabTerminate();
	//NCSLabSaveResult();
	NCSLabSaveResultBin();
	NCSLabFinalize();

	// Send ending preamble + cmd via unified simWrite (works for both TCP and stdout)
	sendPreamble();
	simWrite(&progressType, sizeof(progressType));
	simFlush();

	if (g_simSocket >= 0) {
#ifdef _WIN32
		closesocket(g_simSocket);
		WSACleanup();
#else
		close(g_simSocket);
#endif
		g_simSocket = -1;
	}
}

