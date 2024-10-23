#include <cstdlib>
#include <cstdio>
#include "mainccode.hpp"
#include "ncslabdefines.hpp"
#include "onestep.hpp"
#include "results.hpp"
#include "ncslab.hpp"

MODEL* mp;

time_t main_timer;

// argv[0] is the file path of the executable
// argv[1] is the simulation stop time
// argv[2] is the port number
int main(int argc, char* argv[]) {

	double endTime = 10;
	double end = -1;
	NCSLabInit();


	if (argc >= 2) {
		endTime = atof(argv[1]);
	}

	mp = NCSLabGetModelP();

	mp->time = mp->startTime;

	ncslabLoop();

	NCSLabTerminate();
	NCSLabSaveResult();
	NCSLabFinalize();

	fwrite(&end, 1, sizeof(end), stdout);
}

