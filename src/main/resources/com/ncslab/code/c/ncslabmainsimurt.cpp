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
#endif

MODEL* mp;

extern int g_realtimeMode;

time_t main_timer;

// argv[0] is the file path of the executable
// argv[1] is the simulation stop time
int main(int argc, char* argv[]) {

#ifdef _WIN32
	// Set stdout to binary mode on Windows to prevent \n -> \r\n conversion,
	// which corrupts the binary protocol between ncslab.exe and Java backend.
	_setmode(_fileno(stdout), _O_BINARY);
#endif

	double endTime = 10;
	double end = -1;
	PROGRESSTYPE progressType=Ending;
	NCSLabInit();
	g_realtimeMode = 1;  // Enable real-time mode: skip disk writes in flushScopeChunk

	if (argc >= 2) {
		endTime = atof(argv[1]);
	}

	mp = NCSLabGetModelP();

	mp->time = mp->startTime;
	mp->stopTime = endTime;

	// Send initial display values after initialization
	NCSLabOutput();
	NCSLabSinkOutput();
	sendDisplayUpdateForce();

	// Run real-time simulation loop with periodic data updates
	ncslabLoopRealtime();

	// Send final display values before termination
	sendDisplayUpdateForce();

	NCSLabTerminate();
	// NCSLabSaveResultBin() skipped for real-time mode — data is streamed live via WebSocket
	NCSLabFinalize();

#ifdef _WIN32_WINNT
    fputc(0x55,stdout);
    fputc(0x55,stdout);
#endif
    fwrite(&progressType,1,sizeof(progressType),stdout);
}
