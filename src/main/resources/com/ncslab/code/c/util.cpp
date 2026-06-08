#include <iostream>
#include <cmath>
#include <cstdio>
#include <cstdlib>
#include <stdarg.h>
#include <vector>
#include <cstdint>
#include <cstring>
#include <fstream>
#include <map>
#include <sstream>
#include <string>
#ifndef EIGEN_DONT_VECTORIZE
#define EIGEN_DONT_VECTORIZE
#endif
#ifndef EIGEN_DONT_ALIGN_STATICALLY
#define EIGEN_DONT_ALIGN_STATICALLY
#endif
#ifndef EIGEN_DONT_PARALLELIZE
#define EIGEN_DONT_PARALLELIZE
#endif
#include <Eigen/Dense>
#ifndef _WIN32
#include <sys/time.h>
#else
#ifndef WIN32_LEAN_AND_MEAN
#define WIN32_LEAN_AND_MEAN
#endif
#include <winsock2.h>
#include <ws2tcpip.h>
#include <windows.h>
#endif // _WIN32
#include "ncslabdefines.hpp"
#include "ncslab.hpp"
#include "Matrix.hpp"
#include "onestep.hpp"
#include "util.hpp"

#include <gsl/gsl_linalg.h>
#include <gsl/gsl_vector.h>
#include <gsl/gsl_blas.h>
#include <gsl/gsl_errno.h>
//#include <octave/oct.h>

extern MODEL* mp;
extern TERMINAL* terminals[];

// Global simulation stream/socket for TCP communication with Java backend
FILE* g_simStream = stdout;
int g_simSocket = -1;
volatile int g_ncsCrashStage = 0;

// Real-time simulation pause/resume control flag
bool g_simulationPaused = false;

static int ncs_env_flag_disabled(const char* name) {
	const char* value = std::getenv(name);
	return value != NULL && value[0] == '1';
}

static int ncs_env_flag_enabled(const char* name) {
	const char* value = std::getenv(name);
	return value != NULL && value[0] != '\0' && value[0] != '0';
}

static std::map<std::string, double> g_runtimeParams;
static int g_runtimeParamsLoaded = 0;

void ncslab_runtime_params_load(const char* path) {
	g_runtimeParams.clear();
	g_runtimeParamsLoaded = 1;
	const char* selected = path;
	if (selected == NULL || selected[0] == '\0') {
		selected = std::getenv("NCSLAB_RUNTIME_PARAMS");
	}
	if (selected == NULL || selected[0] == '\0') {
		selected = "runtime_params.tsv";
	}
	std::ifstream input(selected);
	if (!input.good()) {
		fprintf(stderr, "[ncslab_params] no runtime parameter file: %s\n", selected);
		return;
	}
	std::string line;
	int count = 0;
	while (std::getline(input, line)) {
		if (line.empty() || line[0] == '#') {
			continue;
		}
		std::size_t tab = line.find('\t');
		if (tab == std::string::npos) {
			continue;
		}
		std::string key = line.substr(0, tab);
		std::string valueText = line.substr(tab + 1);
		char* end = NULL;
		double value = std::strtod(valueText.c_str(), &end);
		if (end == valueText.c_str()) {
			continue;
		}
		g_runtimeParams[key] = value;
		count++;
	}
	fprintf(stderr, "[ncslab_params] loaded %d runtime params from %s\n", count, selected);
}

double ncslab_runtime_param(const char* key, double defaultValue) {
	if (!g_runtimeParamsLoaded) {
		ncslab_runtime_params_load(NULL);
	}
	if (key == NULL) {
		return defaultValue;
	}
	std::map<std::string, double>::const_iterator it = g_runtimeParams.find(key);
	if (it == g_runtimeParams.end()) {
		return defaultValue;
	}
	return it->second;
}

typedef struct {
	long long kluFactorUs;
	long long kluSolveUs;
	long long gslLuFactorUs;
	long long gslLuSolveUs;
	long long svdUs;
	long long realtimeSendUs;
	long long circuitOutputUs;
	long long circuitUpdateUs;
	long long partitionsUs;
	long long partitionPrepareUs;
	long long partitionSolveUs;
	long long partitionEquivUs;
	long long kluFactorCount;
	long long kluSolveCount;
	long long gslLuFactorCount;
	long long gslLuSolveCount;
	long long svdCount;
	long long realtimeSendCount;
	long long circuitOutputCount;
	long long circuitUpdateCount;
	long long partitionsCount;
	long long partitionPrepareCount;
	long long partitionSolveCount;
	long long partitionEquivCount;
} NcsPerfStats;

static NcsPerfStats g_ncsPerfStats = {};

static int ncs_prof_enabled() {
	static int initialized = 0;
	static int enabled = 0;
	if (!initialized) {
		const char* value = std::getenv("NCSLAB_PROFILE");
		enabled = value != NULL && value[0] != '\0' && value[0] != '0';
		initialized = 1;
	}
	return enabled;
}

long long ncs_prof_now_us(void) {
	if (!ncs_prof_enabled()) {
		return 0;
	}
#ifdef _WIN32
	LARGE_INTEGER freq;
	LARGE_INTEGER counter;
	QueryPerformanceFrequency(&freq);
	QueryPerformanceCounter(&counter);
	return (long long)((counter.QuadPart * 1000000LL) / freq.QuadPart);
#else
	struct timeval tv;
	gettimeofday(&tv, NULL);
	return (long long)tv.tv_sec * 1000000LL + (long long)tv.tv_usec;
#endif
}

static void ncs_prof_add_pair(long long *total, long long *count, long long us) {
	if (!ncs_prof_enabled() || us < 0) {
		return;
	}
	*total += us;
	*count += 1;
}

void ncs_prof_add_circuit_output(long long us) {
	ncs_prof_add_pair(&g_ncsPerfStats.circuitOutputUs, &g_ncsPerfStats.circuitOutputCount, us);
}

void ncs_prof_add_circuit_update(long long us) {
	ncs_prof_add_pair(&g_ncsPerfStats.circuitUpdateUs, &g_ncsPerfStats.circuitUpdateCount, us);
}

void ncs_prof_add_partitions(long long us) {
	ncs_prof_add_pair(&g_ncsPerfStats.partitionsUs, &g_ncsPerfStats.partitionsCount, us);
}

void ncs_prof_add_partition_prepare(long long us) {
	ncs_prof_add_pair(&g_ncsPerfStats.partitionPrepareUs, &g_ncsPerfStats.partitionPrepareCount, us);
}

void ncs_prof_add_partition_solve(long long us) {
	ncs_prof_add_pair(&g_ncsPerfStats.partitionSolveUs, &g_ncsPerfStats.partitionSolveCount, us);
}

void ncs_prof_add_partition_equiv(long long us) {
	ncs_prof_add_pair(&g_ncsPerfStats.partitionEquivUs, &g_ncsPerfStats.partitionEquivCount, us);
}

static void ncs_prof_print_row(const char* name, long long count, long long totalUs) {
	if (count <= 0) {
		return;
	}
	fprintf(stderr, "[ncslab_prof] %-18s count=%lld total_ms=%.3f avg_us=%.3f\n",
		name, count, (double)totalUs / 1000.0, (double)totalUs / (double)count);
}

void ncs_prof_print_summary(void) {
	if (!ncs_prof_enabled()) {
		return;
	}
	fprintf(stderr, "[ncslab_prof] ---- summary ----\n");
	ncs_prof_print_row("klu_factor", g_ncsPerfStats.kluFactorCount, g_ncsPerfStats.kluFactorUs);
	ncs_prof_print_row("klu_solve", g_ncsPerfStats.kluSolveCount, g_ncsPerfStats.kluSolveUs);
	ncs_prof_print_row("gsl_lu_factor", g_ncsPerfStats.gslLuFactorCount, g_ncsPerfStats.gslLuFactorUs);
	ncs_prof_print_row("gsl_lu_solve", g_ncsPerfStats.gslLuSolveCount, g_ncsPerfStats.gslLuSolveUs);
	ncs_prof_print_row("svd", g_ncsPerfStats.svdCount, g_ncsPerfStats.svdUs);
	ncs_prof_print_row("partition_prepare", g_ncsPerfStats.partitionPrepareCount, g_ncsPerfStats.partitionPrepareUs);
	ncs_prof_print_row("partition_solve", g_ncsPerfStats.partitionSolveCount, g_ncsPerfStats.partitionSolveUs);
	ncs_prof_print_row("partition_equiv", g_ncsPerfStats.partitionEquivCount, g_ncsPerfStats.partitionEquivUs);
	ncs_prof_print_row("partitions", g_ncsPerfStats.partitionsCount, g_ncsPerfStats.partitionsUs);
	ncs_prof_print_row("circuit_output", g_ncsPerfStats.circuitOutputCount, g_ncsPerfStats.circuitOutputUs);
	ncs_prof_print_row("circuit_update", g_ncsPerfStats.circuitUpdateCount, g_ncsPerfStats.circuitUpdateUs);
	ncs_prof_print_row("realtime_send", g_ncsPerfStats.realtimeSendCount, g_ncsPerfStats.realtimeSendUs);
}

static long long ncs_wall_millis() {
#ifdef _WIN32
	return (long long)GetTickCount();
#else
	struct timeval tv;
	gettimeofday(&tv, NULL);
	return (long long)tv.tv_sec * 1000LL + (long long)tv.tv_usec / 1000LL;
#endif
}

int ncsRealtimeNativeIntervalMs() {
	static int initialized = 0;
	static int intervalMs = 50;
	if (!initialized) {
		const char* value = std::getenv("NCSLAB_NATIVE_UI_UPDATE_MS");
		if (value == NULL || value[0] == '\0') {
			value = std::getenv("NCSLAB_UI_UPDATE_MS");
		}
		if (value != NULL && value[0] != '\0') {
			int parsed = atoi(value);
			if (parsed < 0) {
				parsed = 0;
			}
			if (parsed > 1000) {
				parsed = 1000;
			}
			intervalMs = parsed;
		}
		initialized = 1;
	}
	return intervalMs;
}

static int ncs_realtime_scope_points_per_update() {
	static int initialized = 0;
	static int maxPoints = 120;
	if (!initialized) {
		const char* value = std::getenv("NCSLAB_RT_SCOPE_POINTS_PER_UPDATE");
		if (value != NULL && value[0] != '\0') {
			int parsed = atoi(value);
			if (parsed < 10) {
				parsed = 10;
			}
			if (parsed > 2000) {
				parsed = 2000;
			}
			maxPoints = parsed;
		}
		initialized = 1;
	}
	return maxPoints;
}

int ncs_parallel_partitions_enabled(void) {
	static int initialized = 0;
	static int enabled = 0;
	if (!initialized) {
		const char* value = std::getenv("NCSLAB_PARALLEL_PARTITIONS");
		enabled = value != NULL && value[0] != '\0' && value[0] != '0';
		initialized = 1;
	}
	return enabled;
}

// Cross-platform socket write helpers
void simWrite(const void* buf, size_t len) {
	if (g_simSocket >= 0) {
		size_t sent = 0;
		while (sent < len) {
#ifdef _WIN32
			int n = send(g_simSocket, (const char*)buf + sent, (int)(len - sent), 0);
			if (n == SOCKET_ERROR) {
				int err = WSAGetLastError();
				if (err == WSAEWOULDBLOCK) {
					Sleep(1);
					continue;
				}
				break;
			}
			if (n <= 0) break;
#else
			ssize_t n = send(g_simSocket, (const char*)buf + sent, len - sent, 0);
			if (n < 0 && (errno == EAGAIN || errno == EWOULDBLOCK)) {
				usleep(1000);
				continue;
			}
			if (n <= 0) break;
#endif
			sent += n;
		}
	} else {
		fwrite(buf, 1, len, g_simStream);
	}
}

void simPutc(int c) {
	unsigned char ch = (unsigned char)c;
	simWrite(&ch, 1);
}

void simPrintf(const char* fmt, ...) {
	char buf[512];
	va_list args;
	va_start(args, fmt);
	int n = vsnprintf(buf, sizeof(buf), fmt, args);
	va_end(args);
	if (n > 0) {
		simWrite(buf, n);
	}
}

void simFlush() {
	if (g_simSocket < 0) {
		fflush(g_simStream);
	}
}

void sendPreamble() {
	simPutc(0x55);
	simPutc(0xAA);
	simPutc(0x55);
	simPutc(0xAA);
}

void simCloseSocketGracefully() {
	if (g_simSocket < 0) {
		return;
	}
#ifdef _WIN32
	shutdown(g_simSocket, SD_SEND);
	char drain[256];
	for (int i = 0; i < 20; ++i) {
		int n = recv(g_simSocket, drain, sizeof(drain), 0);
		if (n == 0) {
			break;
		}
		if (n == SOCKET_ERROR) {
			int err = WSAGetLastError();
			if (err == WSAEWOULDBLOCK) {
				Sleep(5);
				continue;
			}
			break;
		}
	}
	Sleep(20);
	closesocket(g_simSocket);
	WSACleanup();
#else
	shutdown(g_simSocket, SHUT_WR);
	char drain[256];
	for (int i = 0; i < 20; ++i) {
		ssize_t n = recv(g_simSocket, drain, sizeof(drain), 0);
		if (n == 0) {
			break;
		}
		if (n < 0) {
			if (errno == EAGAIN || errno == EWOULDBLOCK) {
				usleep(5000);
				continue;
			}
			break;
		}
	}
	usleep(20000);
	close(g_simSocket);
#endif
	g_simSocket = -1;
}



double calalpoutput(double inputvalue) {
	double result;
	if (inputvalue < 230) {
		result = inputvalue;
	}
	else if (inputvalue >= 230 && inputvalue < 240) {
		result = 1.5 * (inputvalue - 230) + 230;
	}
	else if (inputvalue >= 240 && inputvalue < 250) {
		result = 2.5 * (inputvalue - 240) + 245;
	}
	else if (inputvalue >= 250 && inputvalue < 270) {
		result = 1.25 * (inputvalue - 250) + 270;
	}
	else if (inputvalue >= 270 && inputvalue < 300) {
		result = 0.66 * (inputvalue - 270) + 295;
	}
	else {
		result = inputvalue + 15;
	}
	return result;
}
extern double sample_time[];

double real_sample_time = 0.0;

void discreteInit() {
	extern int sample_i;
	//extern double sample_time[];

	if (hasdiscrete(sample_time) == 1) {
		real_sample_time = gcd1(sample_time);
	}
	//end
	mp->discreteUpdate = 1;
	if (mp->stepSize > real_sample_time)
	{
		mp->stepSize = real_sample_time;
	}
}

void discreteInitFixed() {
	//xiazhiqiang:Evaluates the greatest common divisor by referencing the sampling time array:(The definition location is at line 31 of codeStructC)
	extern int sample_i;
	//extern double sample_time[];

	if (hasdiscrete(sample_time) == 1) {
		real_sample_time = gcd1(sample_time);
	}
	//end
	mp->discreteUpdate = 1;

}

//xiazhiqiang:Computes the greatest common divisor of the sampling time array
double gcd(double x, double y)
{
	int a = (int)(x * 1000);
	int b = (int)(y * 1000);
	int result;
	for (result = a; result >= 0; result--)
	{
		if (0 == a % result && 0 == b % result)
			return result / 1000.0;
	}
	return 0;
}

double gcd1(double a[]) {
	double d = a[0];
	int i;
	for (i = 0; i < mp->blockNum; i++) {
		if (a[i] == 0) {
			break;
		}else if(a[i] < 0){
            d = STEP_SIZE;
            continue;
        }
		d = gcd(d, a[i]);
	}
	return d;
}

// 函数生成正态分布随机数 (Box-Muller方法)
double generateGaussianNoise(double mean, double stdDev) {
    static int hasSpare = 0;
    static double spare;
    if (hasSpare) {
        hasSpare = 0;
        return mean + stdDev * spare;
    }
    hasSpare = 1;
    double u, v, s;
    do {
        u = (rand() / ((double)RAND_MAX)) * 2.0 - 1.0;
        v = (rand() / ((double)RAND_MAX)) * 2.0 - 1.0;
        s = u * u + v * v;
    } while (s >= 1.0 || s == 0.0);
    s = sqrt(-2.0 * log(s) / s);
    spare = v * s;
    return mean + stdDev * (u * s);
}

// 一阶RC低通滤波器
double lowPassFilter(double input, double alpha) {
  static double prevOutput = 0.0;
    double output = alpha * input + (1.0 - alpha) * (prevOutput);
    prevOutput = output;
    return output;
}
//xiazhiqiang:Check whether there are discrete modules

// int hasdiscrete(double a[]) {
// 	int i;
// 	for (i = 0; i < mp->blockNum; i++)
// 	{
// 		if (a[i] == 0)
// 		{
// 			break;
// 		}
// 	}
// 	if (i == 0)
// 	{
// 		return 0;
// 	}
// 	else
// 	{
// 		return 1;
// 	}
// }

int hasdiscrete(double a[])
{
	return a[0] != 0;
}


//xiazhiqang:Distance from the next sampling point
/*
double distance(double t,double s){
	long a=(long)(t*1000000000000000);
		long b=(long)(s*1000000000000000);
		double distance=s-(a%b)/1000000000000000.0;
		if(distance<0){
			distance=0;
		}
		return distance;
}*/


double distance(double t, double s) {
	long num = t / s;
	double dist = (num + 1) * s - t;
	if (dist < 0) {
		dist = 0;
	}
	return dist;
}

//end

void storeState(int num) {
	/*
	for(int i=0;i<STATE_NUM;i++){mbiguous
	  stateReserve[i]=*((REAL *)(mp->states[i]->vp));
	}*/
	int single = 0;
	int matrix = 0;
	for (int i = 0;i < STATE_NUM;i++) {
		switch (mp->states[i]->type) {
		case SINGLE:
			singleStateReserve[num][single++] = *((REAL*)(mp->states[i]->vp));
			break;
		case MATRIX:
			matrixStateReserve[num][matrix++] = *((Matrix*)(mp->states[i]->vp));
			break;
		}
	}
}

void restoreState(int num) {
	/*
	for(int i=0;i<STATE_NUM;i++){
	  *((REAL *)(mp->states[i]->vp))=stateReserve[i];
	}*/
	int single = 0;
	int matrix = 0;
	for (int i = 0;i < STATE_NUM;i++) {
		switch (mp->states[i]->type) {
		case SINGLE:
			*((REAL*)(mp->states[i]->vp)) = singleStateReserve[num][single++];
			break;
		case MATRIX:
			*((Matrix*)(mp->states[i]->vp)) = matrixStateReserve[num][matrix++];
			break;
		}

	}
}

void storeDerivative(int num) {
	/*
	for(int i=0;i<STATE_NUM;i++){
	  derivativeReserve[num][i]=*((REAL *)(mp->states[i]->dvp));
	}*/

	int single = 0;
	int matrix = 0;
	for (int i = 0;i < STATE_NUM;i++) {
		switch (mp->states[i]->type) {
		case SINGLE:
			singleDerivativeReserve[num][single++] = *((REAL*)(mp->states[i]->dvp));
			break;
		case MATRIX:
			matrixDerivativeReserve[num][matrix++] = *((Matrix*)(mp->states[i]->dvp));
			break;
		}

	}
}

void caculateDerivative(double* weights, int num) {
	/*
	for(int i=0;i<STATE_NUM;i++){
	  *((REAL *)(mp->states[i]->dvp))=0;
	  for(int j=0;j<num;j++){
		*((REAL *)(mp->states[i]->dvp))+=weights[j]*derivativeReserve[j][i];
	  }
	}*/

	int single = 0;
	int matrix = 0;
	for (int i = 0;i < STATE_NUM;i++) {
		switch (mp->states[i]->type) {
		case SINGLE:
			*((REAL*)(mp->states[i]->dvp)) = 0;
			for (int j = 0;j < num;j++) {
				*((REAL*)(mp->states[i]->dvp)) += weights[j] * singleDerivativeReserve[j][single];
			}
			single++;
			break;
		case MATRIX:
			*((Matrix*)(mp->states[i]->dvp)) = Matrix(mp->states[i]->height, mp->states[i]->width);
			for (int j = 0;j < num;j++) {
				*((Matrix*)(mp->states[i]->dvp)) += weights[j] * matrixDerivativeReserve[j][matrix];
			}
			matrix++;
			break;
		}

	}
}

REAL calculateStateDif(int seq1, int seq2) {
	REAL dif = 0;
	for (int i = 0;i < SINGLE_STATE_NUM;i++) {
		REAL difn = fabs(singleStateReserve[seq1][i] - singleStateReserve[seq2][i]);
		if (dif < difn) {
			dif = difn;
		}
	}

	for (int i = 0;i < MATRIX_STATE_NUM;i++) {
		Matrix* pm1 = &(matrixStateReserve[seq1][i]);
		Matrix* pm2 = &(matrixStateReserve[seq2][i]);
		for (int h = 0;h < pm1->rows();h++) {
			for (int w = 0;w < pm1->cols();w++) {
				REAL difn = fabs((*pm1)(h, w) - (*pm2)(h, w));
				if (dif < difn) {
					dif = difn;
				}
			}
		}
	}

	return dif;
}

static long oldSec = 0;

/*
#ifndef _WIN32
void writeInformation() {
	struct timeval tv;
	gettimeofday(&tv, NULL);
	if (tv.tv_sec != oldSec) {
		fwrite(&(mp->time), 1, sizeof(mp->time), stdout);
		fflush(stdout);
		oldSec = tv.tv_sec;
	}

}
#else
void writeInformation() {
    // 获取当前系统时间
    FILETIME ft;
    SYSTEMTIME st;
    __int64 newSec;

    GetSystemTime(&st);
    SystemTimeToFileTime(&st, &ft);
    newSec = (static_cast<uint64_t>(ft.dwHighDateTime) << 32) | ft.dwLowDateTime;

    // 将 FILETIME 转换为秒
    newSec /= 10000000; // 10000000 微秒 = 1 秒

    if (newSec != oldSec) {
        // 假设 mp->time 是一个可以写入的变量
        // 这里需要根据实际情况来定义 mp->time 的类型和如何写入
        // 例如，如果 mp->time 是一个时间戳，可以这样写：
        fwrite(&(mp->time), 1, sizeof(mp->time), stdout);
        fflush(stdout);
        oldSec = newSec;
    }
}

#endif*/

void writeInformation(){
#ifdef _WIN32_WINNT
	struct timeval tv;
	PROGRESSTYPE progressType=Simulating;
	DWORD sec=(GetTickCount()/1000)%60;
	//gettimeofday(&tv,NULL);
	if(sec!=oldSec){
		sendPreamble();
		simWrite(&progressType, sizeof(progressType));
		simPrintf("%f\n", mp->time);
		simFlush();

		// ===== Display 模块实时数据输出 =====
		// 遍历所有 Terminal，找出 Display 模块（maxDataLength == 1）
		// 通过 g_simStream 发送 Display 的 UUID 和当前值到 Java 后端
		int displayCount = 0;
		for (int i = 0; i < mp->terminalNum; i++) {
			TERMINAL* terminal = terminals[i];
			SCOPE* scope = (SCOPE*)terminal->terminal;
			if (scope->maxDataLength == 1) {
				displayCount++;
			}
		}
		if (displayCount > 0) {
			PROGRESSTYPE displayType = DisplayUpdate;
			sendPreamble();
			simWrite(&displayType, sizeof(displayType));
			simWrite(&displayCount, sizeof(displayCount));
			for (int i = 0; i < mp->terminalNum; i++) {
				TERMINAL* terminal = terminals[i];
				SCOPE* scope = (SCOPE*)terminal->terminal;
				if (scope->maxDataLength == 1) {
					int uuidLen = strlen(scope->uuid);
					simWrite(&uuidLen, sizeof(uuidLen));
					simWrite(scope->uuid, uuidLen);
					double value = scope->dataList.empty() ? 0.0 : scope->dataList.back();
					simWrite(&value, sizeof(value));
				}
			}
			simFlush();
		}
		// ===== Display 模块实时数据输出结束 =====

		oldSec=sec;
	}
#else
	struct timeval tv;
	PROGRESSTYPE progressType=Simulating;
	gettimeofday(&tv,NULL);
	if(tv.tv_sec!=oldSec){
		sendPreamble();
		simWrite(&progressType, sizeof(progressType));
		simPrintf("%f\n", mp->time);
		simFlush();
		oldSec=tv.tv_sec;
	}
#endif	
}

/**
 * Force send Display block update without time-based throttling.
 * Used to send initial and final display values at simulation start/end.
 */
void sendDisplayUpdateForce() {
	int displayCount = 0;
	for (int i = 0; i < mp->terminalNum; i++) {
		TERMINAL* terminal = terminals[i];
		SCOPE* scope = (SCOPE*)terminal->terminal;
		if (scope->maxDataLength == 1) {
			displayCount++;
		}
	}
	if (displayCount > 0) {
		PROGRESSTYPE displayType = DisplayUpdate;
		sendPreamble();
		simWrite(&displayType, sizeof(displayType));
		simWrite(&displayCount, sizeof(displayCount));
		for (int i = 0; i < mp->terminalNum; i++) {
			TERMINAL* terminal = terminals[i];
			SCOPE* scope = (SCOPE*)terminal->terminal;
			if (scope->maxDataLength == 1) {
				int uuidLen = strlen(scope->uuid);
				simWrite(&uuidLen, sizeof(uuidLen));
				simWrite(scope->uuid, uuidLen);
				double value = scope->dataList.empty() ? 0.0 : scope->dataList.back();
				simWrite(&value, sizeof(value));
			}
		}
		simFlush();
	}
}

/**
 * Send real-time Display + Scope data update to Java backend via stdout.
 * Called every 10000 steps or every 1 second during real-time simulation.
 * Each scope sends at most 10000 new data points (latest points if exceeded).
 */
void sendRealtimeDataUpdate() {
	if (mp->terminalNum <= 0) return;

	static long long lastSendMs = 0;
	int intervalMs = ncsRealtimeNativeIntervalMs();
	long long nowMs = ncs_wall_millis();
	if (intervalMs > 0 && lastSendMs != 0 && nowMs - lastSendMs < intervalMs) {
		return;
	}

	int displayCount = 0;
	int scopeCount = 0;
	for (int i = 0; i < mp->terminalNum; i++) {
		TERMINAL* terminal = terminals[i];
		if (terminal->type != Scope) continue;
		SCOPE* scope = (SCOPE*)terminal->terminal;
		if (scope->maxDataLength == 1) {
			displayCount++;
		} else {
			scopeCount++;
		}
	}
	if (displayCount == 0 && scopeCount == 0) return;

	long long profStartUs = ncs_prof_now_us();
	PROGRESSTYPE type = RealtimeDataUpdate;
	sendPreamble();
	simWrite(&type, sizeof(type));

	simWrite(&displayCount, sizeof(displayCount));
	for (int i = 0; i < mp->terminalNum; i++) {
		TERMINAL* terminal = terminals[i];
		if (terminal->type != Scope) continue;
		SCOPE* scope = (SCOPE*)terminal->terminal;
		if (scope->maxDataLength == 1) {
			int uuidLen = strlen(scope->uuid);
			simWrite(&uuidLen, sizeof(uuidLen));
			simWrite(scope->uuid, uuidLen);
			double value = scope->dataList.empty() ? 0.0 : scope->dataList.back();
			simWrite(&value, sizeof(value));
		}
	}

	simWrite(&scopeCount, sizeof(scopeCount));
	for (int i = 0; i < mp->terminalNum; i++) {
		TERMINAL* terminal = terminals[i];
		if (terminal->type != Scope) continue;
		SCOPE* scope = (SCOPE*)terminal->terminal;
		if (scope->maxDataLength == 1) continue;

		int uuidLen = strlen(scope->uuid);
		simWrite(&uuidLen, sizeof(uuidLen));
		simWrite(scope->uuid, uuidLen);
		simWrite(&(scope->width), sizeof(scope->width));
		simWrite(&(scope->height), sizeof(scope->height));

		int totalSize = (int)scope->timeList.size();
		int newPoints = totalSize - scope->sentCount;
		if (newPoints < 0) {
			scope->sentCount = 0;
			newPoints = totalSize;
		}

		// Calculate target points per upload based on Scope config.
		// Example: maxDataLength=10000, stopTime=10s -> 1000 points/second/upload
		int maxLen = scope->maxDataLength > 0 ? scope->maxDataLength : 1000;
		int stopTimeSec = (int)(mp->stopTime + 0.5);
		if (stopTimeSec < 1) stopTimeSec = 1;
		int targetPoints = maxLen / stopTimeSec;
		if (targetPoints < 50) targetPoints = 50;
		if (targetPoints > maxLen) targetPoints = maxLen;
		int realtimePointCap = ncs_realtime_scope_points_per_update();
		if (targetPoints > realtimePointCap) targetPoints = realtimePointCap;

		int step = 1;
		int sendPoints = newPoints;
		if (newPoints > targetPoints) {
			step = newPoints / targetPoints;
			if (step < 2) step = 2;
			sendPoints = 0;
			for (int i = 0; i < newPoints; i += step) sendPoints++;
		}
		simWrite(&sendPoints, sizeof(sendPoints));

		size_t wh = (size_t)scope->width * scope->height;

		for (int p = 0; p < newPoints; p += step) {
			size_t idx = (size_t)scope->sentCount + p;
			if (idx >= scope->timeList.size()) break;

			REAL t = scope->timeList[idx];
			simWrite(&t, sizeof(t));

			size_t dataOffset = idx * wh;
			for (size_t v = 0; v < wh; v++) {
				if (dataOffset + v < scope->dataList.size()) {
					REAL val = scope->dataList[dataOffset + v];
					simWrite(&val, sizeof(val));
				} else {
					REAL zero = 0.0;
					simWrite(&zero, sizeof(zero));
				}
			}
		}

		scope->sentCount = totalSize;
	}

	simWrite(&(mp->time), sizeof(mp->time));
	simFlush();
	lastSendMs = nowMs;
	ncs_prof_add_pair(&g_ncsPerfStats.realtimeSendUs, &g_ncsPerfStats.realtimeSendCount,
		ncs_prof_now_us() - profStartUs);
}

/**
 * Send Stateflow state change update to Java backend via stdout.
 * Called from generated C code when a state transition occurs.
 */
void sendStateflowStateUpdate(const char* chartUUID, const char* stateId, const char* stateName) {
	if (chartUUID == NULL || stateId == NULL) return;
	
	PROGRESSTYPE type = StateflowStateUpdate;
	sendPreamble();
	simWrite(&type, sizeof(type));
	
	int chartUuidLen = strlen(chartUUID);
	simWrite(&chartUuidLen, sizeof(chartUuidLen));
	simWrite(chartUUID, chartUuidLen);
	
	int stateIdLen = strlen(stateId);
	simWrite(&stateIdLen, sizeof(stateIdLen));
	simWrite(stateId, stateIdLen);
	
	int stateNameLen = stateName ? strlen(stateName) : 0;
	simWrite(&stateNameLen, sizeof(stateNameLen));
	if (stateNameLen > 0) {
		simWrite(stateName, stateNameLen);
	}
	
	simFlush();
}

// 初始化缓冲区
void init_buffer(Buffer *buf, int size, double init_value) {
    buf->times = (double *)calloc(size, sizeof(double));
    buf->values = (double *)calloc(size, sizeof(double));
    buf->size = size;
    buf->head = 0;
    buf->tail = 0;
    buf->count = 0;
	for(int i = 0; i < size; i++){
		buf->values[i] = init_value;
	}
}

// 释放缓冲区内存
void free_buffer(Buffer *buf) {
    free(buf->times);
    free(buf->values);
}

// 插入新数据到缓冲区
void insert_to_buffer(Buffer *buf, double time, double value) {
    if (buf->count == buf->size) {
        // 队列已满，移除队头元素
        buf->head = (buf->head + 1) % buf->size;
        buf->count--;
    }
    buf->times[buf->tail] = time;
    buf->values[buf->tail] = value;
    buf->tail = (buf->tail + 1) % buf->size;
    buf->count++;
}


// 线性插值函数
double linear_interpolation(Buffer *buf, double target_time) {
    if (target_time < 0) {
        // 元素数量不足，返回初始值
        return buf->values[buf->size-1];
    }

    int i = buf->head;
    for (int j = 0; j < buf->count - 1; j++) {
        int next_i = (i + 1) % buf->size;
        if (buf->times[i] <= target_time && target_time < buf->times[next_i]) {
            double t1 = buf->times[i];
            double y1 = buf->values[i];
            double t2 = buf->times[next_i];
            double y2 = buf->values[next_i];
            return y1 + (target_time - t1) * (y2 - y1) / (t2 - t1);
        }
        i = next_i;
    }
    // 如果没有找到合适的区间，返回最后一个值
    return buf->values[i];
}


// 初始化一维插值表
void init_interpolation_table_1d(InterpolationTable1D *table, int size, int extrapolation_strategy) {
    table->size = size;
    table->extrapolation_strategy = extrapolation_strategy;

    // 分配内存
    table->x = (double *)malloc(size * sizeof(double));
    table->y = (double *)malloc(size * sizeof(double));
}

// 释放一维插值表内存
void free_interpolation_table_1d(InterpolationTable1D *table) {
    free(table->x);
    free(table->y);
}

// 一维线性插值函数，包含外插策略
double linear_interpolation_1d(InterpolationTable1D *table, double target_x) {
    int x1, x2;
    double dx;

    // 检查目标点是否在x范围内
    if (target_x < table->x[0]) {
        // 在x范围外
        if (table->extrapolation_strategy == 0) {
            return table->y[0]; // 最近边界值
        } else {
            x1 = 0;
            x2 = 1;
            dx = (target_x - table->x[x1]) / (table->x[x2] - table->x[x1]);
            return table->y[x1] + dx * (table->y[x2] - table->y[x1]); // 线性外插
        }
    } else if (target_x > table->x[table->size - 1]) {
        // 在x范围外
        if (table->extrapolation_strategy == 0) {
            return table->y[table->size - 1]; // 最近边界值
        } else {
            x1 = table->size - 2;
            x2 = table->size - 1;
            dx = (target_x - table->x[x1]) / (table->x[x2] - table->x[x1]);
            return table->y[x1] + dx * (table->y[x2] - table->y[x1]); // 线性外插
        }
    } else {
        // 在x范围内
        for (x1 = 0; x1 < table->size - 1; x1++) {
            if (table->x[x1] <= target_x && target_x <= table->x[x1 + 1]) {
                break;
            }
        }
        x2 = x1 + 1;
        dx = (target_x - table->x[x1]) / (table->x[x2] - table->x[x1]);
    }

    // 线性插值
    return table->y[x1] * (1 - dx) + table->y[x2] * dx;
}

void insert_to_x_table1d(InterpolationTable1D *table, int index, double value)
{
    if (index < 0 || index >= table->size) {
        printf("Error: Index out of bounds.\n");
        return;
    }
    table->x[index] = value;
}
void insert_to_y_table1d(InterpolationTable1D *table, int index, double value)
{
    if (index < 0 || index >= table->size) {
        printf("Error: Index out of bounds.\n");
        return;
    }
    table->y[index] = value;
}


// 初始化二维插值表
void init_interpolation_table(InterpolationTable *table, int width, int height, int extrapolation_strategy) {
    table->width = width;
    table->height = height;
    table->extrapolation_strategy = extrapolation_strategy;

    // 分配内存
    table->x = (double **)malloc(width * sizeof(double *));
    table->y = (double **)malloc(height * sizeof(double *));
    table->z = (double **)malloc(width * sizeof(double *));

    for (int i = 0; i < width; i++) {
        table->x[i] = (double *)malloc(width * sizeof(double));
        table->z[i] = (double *)malloc(height * sizeof(double));
    }

    for (int i = 0; i < height; i++) {
        table->y[i] = (double *)malloc(height * sizeof(double));
    }
}

// 释放二维插值表内存
void free_interpolation_table(InterpolationTable *table) {
    for (int i = 0; i < table->width; i++) {
        free(table->x[i]);
        free(table->z[i]);
    }

    for (int i = 0; i < table->height; i++) {
        free(table->y[i]);
    }

    free(table->x);
    free(table->y);
    free(table->z);
}

// 双线性插值函数，包含外推策略
double bilinear_interpolation(InterpolationTable *table, double target_x, double target_y) {
    int x1, x2, y1, y2;
    double dx, dy;

    // 检查目标点是否在x范围内
    if (target_x < table->x[0][0]) {
        // 在x范围外
        if (table->extrapolation_strategy == 0) {
            x1 = x2 = 0;
            dx = 0.0;
        } else {
            x1 = 0;
            x2 = 1;
            dx = (target_x - table->x[x1][0]) / (table->x[x2][0] - table->x[x1][0]);
        }
    } else if (target_x > table->x[table->width - 1][0]) {
        // 在x范围外
        if (table->extrapolation_strategy == 0) {
            x1 = x2 = table->width - 1;
            dx = 0.0;
        } else {
            x1 = table->width - 2;
            x2 = table->width - 1;
            dx = (target_x - table->x[x1][0]) / (table->x[x2][0] - table->x[x1][0]);
        }
    } else {
        // 在x范围内
        for (x1 = 0; x1 < table->width - 1; x1++) {
            if (table->x[x1][0] <= target_x && target_x <= table->x[x1 + 1][0]) {
                break;
            }
        }
        x2 = x1 + 1;
        dx = (target_x - table->x[x1][0]) / (table->x[x2][0] - table->x[x1][0]);
    }

    // 检查目标点是否在y范围内
    if (target_y < table->y[0][0]) {
        // 在y范围外
        if (table->extrapolation_strategy == 0) {
            y1 = y2 = 0;
            dy = 0.0;
        } else {
            y1 = 0;
            y2 = 1;
            dy = (target_y - table->y[y1][0]) / (table->y[y2][0] - table->y[y1][0]);
        }
    } else if (target_y > table->y[table->height - 1][0]) {
        // 在y范围外
        if (table->extrapolation_strategy == 0) {
            y1 = y2 = table->height - 1;
            dy = 0.0;
        } else {
            y1 = table->height - 2;
            y2 = table->height - 1;
            dy = (target_y - table->y[y1][0]) / (table->y[y2][0] - table->y[y1][0]);
        }
    } else {
        // 在y范围内
        for (y1 = 0; y1 < table->height - 1; y1++) {
            if (table->y[y1][0] <= target_y && target_y <= table->y[y1 + 1][0]) {
                break;
            }
        }
        y2 = y1 + 1;
        dy = (target_y - table->y[y1][0]) / (table->y[y2][0] - table->y[y1][0]);
    }

    // 双线性插值
    double z1 = table->z[x1][y1] * (1 - dx) + table->z[x2][y1] * dx;
    double z2 = table->z[x1][y2] * (1 - dx) + table->z[x2][y2] * dx;
    double z = z1 * (1 - dy) + z2 * dy;

    return z;
}

void insert_to_x_table(InterpolationTable *table, int index, double value) {
    if (index < 0 || index >= table->width) {
        printf("Error: Index out of bounds.\n");
        return;
    }
    table->x[index][0] = value;
}

void insert_to_y_table(InterpolationTable *table, int index, double value) {
    if (index < 0 || index >= table->height) {
        printf("Error: Index out of bounds.\n");
        return;
    }
    table->y[index][0] = value;
}

void insert_to_z_table(InterpolationTable *table, int x_index, int y_index, double value) {
    if (x_index < 0 || x_index >= table->width || y_index < 0 || y_index >= table->height) {
        printf("Error: Index out of bounds.\n");
        return;
    }
    table->z[x_index][y_index] = value;
}

void writeBuf(unsigned char *buf,int size){
	for(int i=0;i<size;i++){
		simPutc(buf[i]);
		//simFlush(); 
	}
}

void writeSavingInformation(int currentTerminal,int terminalNum){
	PROGRESSTYPE progressType=Saving;
	
	sendPreamble();
	
	writeBuf((unsigned char *)(&progressType),sizeof(progressType));
	writeBuf((unsigned char *)(&currentTerminal),sizeof(currentTerminal));
	writeBuf((unsigned char *)(&terminalNum),sizeof(terminalNum));
	simFlush();
}

void copyCircuitMartrix(REAL *gAA,REAL *gAAc,REAL *iA,REAL *iAc,int size){
	memcpy(gAA,gAAc,sizeof(REAL)*size*size);
	memcpy(iA,iAc,sizeof(REAL)*size);
}

void copyCircuitVector(REAL *iA,REAL *iAc,int size){
	memcpy(iA,iAc,sizeof(REAL)*size);
}

bool isRef(int n,int* ref,int refSize){
	for(int i = 0;i<refSize;++i){
		if(n == ref[i])
			return true;
	}
	return false;
}
/*用来进行电路开关的矩阵合并*/
/*Vindex记录了原始节点与合并之后节点的对应关系*/
/*size记录了合并前的矩阵大小，合并之后size变化，需要通过指针回传*/
/*ref记录了参考节点的位置*/
/*n和m记录了要合并的两个节点*/
/**/
void CircuitCombine(REAL *gAA,REAL *iA,int *vIndex,int *size_p,int* ref,int n,int m,int indexSize,int refSize){
	int size=*size_p;
	/*
	for(int i=0;i<size;i++){
		for(int j=0;j<size;j++){
			printf("%f\t",gAA[i*size+j]);
		}
		printf("\n");
	}
	printf("\n");
	for(int i=0;i<size;i++){
		printf("%f\t",iA[i]);
	}
	printf("\n\n");

	printf("%d\t%d\n",n,m);*/

	//如果合并的节点中有参考节点，电压方程由参考节点决定，那么就只需要消去非参考节点的行列即可
	if(isRef(n,ref,refSize)||isRef(m,ref,refSize)){
		int nRef,nn;
		const int originalSize = size;
		std::vector<REAL> gAAc((size_t)originalSize * (size_t)originalSize);
		auto at = [&gAAc, originalSize](int row, int col) -> REAL& {
			return gAAc[(size_t)row * (size_t)originalSize + (size_t)col];
		};
		//复制原始数据到二维数组，便于进行计算，以后可以优化掉
		for(int i=0;i<size;i++){
			for(int j=0;j<size;j++){
				at(i,j)=gAA[i*size+j];
			}
		}

		//让n成为消去的节点
		int refNode = m;
		if(isRef(n,ref,refSize)){
			refNode=n;
			n=m;
		}

		//nn是变换后的消去节点
		nn=vIndex[n];
		nRef=vIndex[refNode];
		
		//让消去的节点指向参考节点
		vIndex[n]=vIndex[refNode];
		
		//printf("%d\t%d\n",nRef,nn);

		//分别消去nn的节点行与列
		for(int i=nn;i<size-1;i++){
			for(int j=0;j<size;j++){
				at(i,j)=at(i+1,j);
			}
		}
		for(int i=nn;i<size-1;i++){
			for(int j=0;j<size-1;j++){
				at(j,i)=at(j,i+1);
			}
		}
		
		//消去nn在iA中的节点
		for(int i=nn;i<size-1;i++){
			iA[i]=iA[i+1];
		}
		
		/*
		for(int i=nn+1;i<size;i++){
			for(int j=0;j<size;j++){
				if(vIndex[j]==i){
					vIndex[j]-=1;
				}
			}
		}*/
		
		//重新更新vIndex，让nn后面的节点都减一
		for(int i=0;i<indexSize;i++){
			if(vIndex[i]>nn){
				vIndex[i]-=1;
			}
		}

		//矩阵的大小减一
		size--;
		//重新放回到一维数组中
		for(int i=0;i<size;i++){
			for(int j=0;j<size;j++){
				gAA[i*size+j]=at(i,j);
			}
		}
	}
	else{
		//解析合并的两个点在新矩阵中的位置
		int nn=vIndex[n];
		int mm=vIndex[m];
		//如果不是一个点，才需要合并，如果是一个点，就忽略
		if(nn!=mm){
			const int originalSize = size;
			std::vector<REAL> gAAc((size_t)originalSize * (size_t)originalSize);
			auto at = [&gAAc, originalSize](int row, int col) -> REAL& {
				return gAAc[(size_t)row * (size_t)originalSize + (size_t)col];
			};
			for(int i=0;i<size;i++){
				for(int j=0;j<size;j++){
					at(i,j)=gAA[i*size+j];
				}
			}
			//重新排布，让nn在前，mm在后
			if(nn>mm){
				int temp=nn,temp1=n;
				nn=mm;
				n=m;
				mm=temp;
				m=temp1;
			}
			//printf("%d\t%d\n",nn,mm);

			//分别把mm的行与列合并到nn里面去
			for(int i=0;i<size;i++){
				at(nn,i)+=at(mm,i);
			}
			for(int i=0;i<size;i++){
				at(i,nn)+=at(i,mm);
			}
			//iA也进行相应的合并
			iA[nn]+=iA[mm];

			//合并之后，要把mm后面的行和列依次向前
			for(int i=mm;i<size-1;i++){
				for(int j=0;j<size;j++){
					at(i,j)=at(i+1,j);
				}
			}
			for(int i=mm;i<size-1;i++){
				for(int j=0;j<size-1;j++){
					at(j,i)=at(j,i+1);
				}
			}
			//iA也是要把mm后面的元素依次向前
			for(int i=mm;i<size-1;i++){
				iA[i]=iA[i+1];
			}
			/*
			for(int i=0;i<size;i++){
				printf("%d\t",vIndex[i]);
			}
			printf("\n");*/

			//m的索引合并到n里面
			vIndex[m]=vIndex[n];
			
			/*
			for(int i=0;i<size;i++){
				printf("%d\t",vIndex[i]);
			}
			printf("\n");*/
			
			/*
			for(int i=mm+1;i<size;i++){
				for(int j=0;j<size;j++){
					if(vIndex[j]==i){
						vIndex[j]-=1;
					}
				}
			}*/

			//重新更新vIndex，让mm后面的节点都减一
			for(int i=0;i<indexSize;i++){
				if(vIndex[i]>mm){
					vIndex[i]-=1;
				}
			}

			//矩阵的大小减一
			size--;
			//重新放回到一维数组中
			for(int i=0;i<size;i++){
				for(int j=0;j<size;j++){
					gAA[i*size+j]=at(i,j);
				}
			}
			/*
			for(int i=0;i<size;i++){
				printf("%d\t",vIndex[i]);
			}
			printf("\n");*/
			
		}
	
		
	}
	/*
	for(int i=0;i<size;i++){
		for(int j=0;j<size;j++){
			printf("%f\t",gAA[i*size+j]);
		}
		printf("\n");
	}
	printf("\n");
	for(int i=0;i<size;i++){
		printf("%f\t",iA[i]);
	}
	printf("\n\n");*/
	//exit(0);

	*size_p=size;
}


//电路仿真方面的函数

static int ncs_try_lu_inverse(const gsl_matrix *Ain, gsl_matrix *invOut, int n) {
	if (Ain == NULL || invOut == NULL || n <= 0 || ncs_env_flag_disabled("NCSLAB_FORCE_SVD")) {
		return 0;
	}

	gsl_matrix *LU = gsl_matrix_alloc((size_t)n, (size_t)n);
	gsl_permutation *perm = gsl_permutation_alloc((size_t)n);
	if (LU == NULL || perm == NULL) {
		if (perm != NULL) {
			gsl_permutation_free(perm);
		}
		if (LU != NULL) {
			gsl_matrix_free(LU);
		}
		return 0;
	}

	gsl_matrix_memcpy(LU, Ain);
	int signum = 0;
	gsl_error_handler_t *oldHandler = gsl_set_error_handler_off();
	int status = gsl_linalg_LU_decomp(LU, perm, &signum);
	if (status == GSL_SUCCESS) {
		status = gsl_linalg_LU_invert(LU, perm, invOut);
	}
	gsl_set_error_handler(oldHandler);

	gsl_permutation_free(perm);
	gsl_matrix_free(LU);
	return status == GSL_SUCCESS;
}

static int ncs_prepare_store_lu(StoreGAA *store, const gsl_matrix *Ain, int n) {
	if (store == NULL || Ain == NULL || n <= 0) {
		return 0;
	}
	if (n > 16) {
		store->luReady = 0;
		return 0;
	}
	long long profStartUs = ncs_prof_now_us();
	if (store->lu != NULL) {
		gsl_matrix_free(store->lu);
		store->lu = NULL;
	}
	if (store->perm != NULL) {
		gsl_permutation_free(store->perm);
		store->perm = NULL;
	}
	store->luReady = 0;
	store->luSignum = 0;

	gsl_matrix *LU = gsl_matrix_alloc((size_t)n, (size_t)n);
	gsl_permutation *perm = gsl_permutation_alloc((size_t)n);
	if (LU == NULL || perm == NULL) {
		if (LU != NULL) {
			gsl_matrix_free(LU);
		}
		if (perm != NULL) {
			gsl_permutation_free(perm);
		}
		return 0;
	}

	gsl_matrix_memcpy(LU, Ain);
	gsl_error_handler_t *oldHandler = gsl_set_error_handler_off();
	int status = gsl_linalg_LU_decomp(LU, perm, &store->luSignum);
	gsl_set_error_handler(oldHandler);
	if (status != GSL_SUCCESS) {
		gsl_matrix_free(LU);
		gsl_permutation_free(perm);
		return 0;
	}

	store->lu = LU;
	store->perm = perm;
	store->luReady = 1;
	ncs_prof_add_pair(&g_ncsPerfStats.gslLuFactorUs, &g_ncsPerfStats.gslLuFactorCount,
		ncs_prof_now_us() - profStartUs);
	return 1;
}

static void ncs_free_store_lu(StoreGAA *store) {
	if (store == NULL) {
		return;
	}
	if (store->lu != NULL) {
		gsl_matrix_free(store->lu);
		store->lu = NULL;
	}
	if (store->perm != NULL) {
		gsl_permutation_free(store->perm);
		store->perm = NULL;
	}
	store->luReady = 0;
}

static void ncs_free_store_cuda(StoreGAA *store) {
	if (store == NULL) {
		return;
	}
	ncslab_cuda_free_device_cache(&store->cudaInv);
}

#ifdef NCSLAB_USE_KLU
static void ncs_free_store_klu(StoreGAA *store) {
	if (store == NULL || !store->kluReady) {
		return;
	}
	klu_free_numeric(&store->kluNumeric, &store->kluCommon);
	klu_free_symbolic(&store->kluSymbolic, &store->kluCommon);
	store->kluNumeric = NULL;
	store->kluSymbolic = NULL;
	store->kluReady = 0;
}

static int ncs_prepare_store_klu(StoreGAA *store, const gsl_matrix *Ain, int n) {
	if (store == NULL || Ain == NULL || n <= 0) {
		return 0;
	}
	long long profStartUs = ncs_prof_now_us();
	ncs_free_store_klu(store);

	int minN = 12;
	const char* minValue = std::getenv("NCSLAB_KLU_MIN_N");
	if (minValue != NULL && minValue[0] != '\0') {
		minN = atoi(minValue);
		if (minN < 1) {
			minN = 1;
		}
	}
	if (n < minN) {
		return 0;
	}

	std::vector<int32_t> Ap((size_t)n + 1U, 0);
	std::vector<int32_t> Ai;
	std::vector<double> Ax;
	Ai.reserve((size_t)n * 4U);
	Ax.reserve((size_t)n * 4U);
	for (int col = 0; col < n; ++col) {
		Ap[(size_t)col] = (int32_t)Ai.size();
		for (int row = 0; row < n; ++row) {
			double value = gsl_matrix_get(Ain, (size_t)row, (size_t)col);
			if (value != 0.0) {
				Ai.push_back((int32_t)row);
				Ax.push_back(value);
			}
		}
	}
	Ap[(size_t)n] = (int32_t)Ai.size();
	if (Ai.empty()) {
		return 0;
	}

	klu_defaults(&store->kluCommon);
	store->kluCommon.halt_if_singular = 0;
	store->kluSymbolic = klu_analyze((int32_t)n, Ap.data(), Ai.data(), &store->kluCommon);
	if (store->kluSymbolic == NULL) {
		return 0;
	}
	store->kluNumeric = klu_factor(Ap.data(), Ai.data(), Ax.data(), store->kluSymbolic, &store->kluCommon);
	if (store->kluNumeric == NULL) {
		klu_free_symbolic(&store->kluSymbolic, &store->kluCommon);
		store->kluSymbolic = NULL;
		return 0;
	}
	store->kluReady = 1;
	ncs_prof_add_pair(&g_ncsPerfStats.kluFactorUs, &g_ncsPerfStats.kluFactorCount,
		ncs_prof_now_us() - profStartUs);
	return 1;
}
#endif

/* Moore-Penrose pseudoinverse via SVD when the fast LU inverse path is not usable. */
static void ncs_invert_square_svd_pinv(const gsl_matrix *Ain, gsl_matrix *invOut, int n) {
	g_ncsCrashStage = 1000 + n;
	if (Ain == NULL || invOut == NULL || n <= 0) {
		return;
	}

	const int forceGpuSvd = ncs_env_flag_enabled("NCSLAB_CUDA_FORCE_SVD");
	const int useGpuSvd = ncs_env_flag_enabled("NCSLAB_CUDA_SVD");
	if (useGpuSvd && forceGpuSvd) {
		g_ncsCrashStage = 1150 + n;
		if (ncslab_cuda_svd_pinv_colmajor(n, Ain->data, invOut->data) == 0) {
			g_ncsCrashStage = 1195 + n;
			return;
		}
	}

	if (n <= 16) {
		g_ncsCrashStage = 1100 + n;
		if (ncs_try_lu_inverse(Ain, invOut, n)) {
			g_ncsCrashStage = 1190 + n;
			return;
		}
	}

	g_ncsCrashStage = 1200 + n;
	if (useGpuSvd && !forceGpuSvd) {
		if (ncslab_cuda_svd_pinv_colmajor(n, Ain->data, invOut->data) == 0) {
			g_ncsCrashStage = 1290 + n;
			return;
		}
	}

	g_ncsCrashStage = 1300 + n;
	long long profStartUs = ncs_prof_now_us();
	Eigen::MatrixXd A((Eigen::Index)n, (Eigen::Index)n);
	for (int i = 0; i < n; ++i) {
		for (int j = 0; j < n; ++j) {
			double value = gsl_matrix_get(Ain, (size_t)i, (size_t)j);
			A((Eigen::Index)i, (Eigen::Index)j) = std::isfinite(value) ? value : 0.0;
		}
	}
	g_ncsCrashStage = 1400 + n;
	Eigen::JacobiSVD<Eigen::MatrixXd> svd(A, Eigen::ComputeFullU | Eigen::ComputeFullV);
	g_ncsCrashStage = 1500 + n;
	const Eigen::VectorXd singular = svd.singularValues();
	double smax = singular.size() > 0 ? singular.maxCoeff() : 0.0;
	double nscale = (double)((n < 2) ? 1 : n);
	const double tol = (smax > 0.0) ? (smax * 1.0e-14 * nscale) : 0.0;
	Eigen::VectorXd invSingular(singular.size());
	for (Eigen::Index i = 0; i < singular.size(); ++i) {
		double si = singular(i);
		invSingular(i) = (si > tol) ? (1.0 / si) : 0.0;
	}
	g_ncsCrashStage = 1600 + n;
	Eigen::MatrixXd pinv = svd.matrixV() * invSingular.asDiagonal() * svd.matrixU().transpose();
	g_ncsCrashStage = 1700 + n;
	for (int i = 0; i < n; ++i) {
		for (int j = 0; j < n; ++j) {
			gsl_matrix_set(invOut, (size_t)i, (size_t)j, pinv((Eigen::Index)i, (Eigen::Index)j));
		}
	}
	ncs_prof_add_pair(&g_ncsPerfStats.svdUs, &g_ncsPerfStats.svdCount,
		ncs_prof_now_us() - profStartUs);
	g_ncsCrashStage = 0;
}

/*计算一种组合的Inv矩阵*/
//SwitchGAA *psGaa指向SwitchGaa表的指针
//REAL *gAA, 经过开关状态合并之后的gAA矩阵
//int switchNum 开关的个数
//int size,经过开关状态合并之后的gAA矩阵大小
//uint32_T *switchStatus，指向现在开关状态的指针
gsl_matrix *addSwitchCombine(SwitchGAA *psGaa,REAL *gAA,int switchNum,int size,uint32_T *switchStatus){
	int pos;
	g_ncsCrashStage = 2000 + size;
	
	if (psGaa->storeGAASize >= psGaa->storeGAACapacity) {
		uint32_T newCapacity = (psGaa->storeGAACapacity == 0) ? 4 : psGaa->storeGAACapacity * 2;
		StoreGAA *newStore = (StoreGAA *)realloc(psGaa->storeGAA, sizeof(StoreGAA) * newCapacity);
		if (newStore == NULL) {
			return NULL;
		}
		psGaa->storeGAA = newStore;
		psGaa->storeGAACapacity = newCapacity;
	}
	//添加的Inv矩阵表格的位置Pos
	pos=psGaa->storeGAASize++;
	//为新的inv表格对应的开关状态表分配内存，并且在数据结构中复制一份
	psGaa->storeGAA[pos].switchStatus=(uint32_T *)malloc((switchNum/32+1)*sizeof(uint32_T));
	memcpy(psGaa->storeGAA[pos].switchStatus,switchStatus,(switchNum/32+1)*sizeof(uint32_T));
	
	//Inv表格中记录矩阵大小
	psGaa->storeGAA[pos].size=size;
	//都是根据最新参数进行的计算，因此有无参数改变的标志为0
	psGaa->storeGAA[pos].isVariableChanged=0;
	psGaa->storeGAA[pos].lu=NULL;
	psGaa->storeGAA[pos].perm=NULL;
	psGaa->storeGAA[pos].luSignum=0;
	psGaa->storeGAA[pos].luReady=0;
	psGaa->storeGAA[pos].cudaInv=NULL;
#ifdef NCSLAB_USE_KLU
	psGaa->storeGAA[pos].kluSymbolic=NULL;
	psGaa->storeGAA[pos].kluNumeric=NULL;
	psGaa->storeGAA[pos].kluReady=0;
#endif
	//psGaa->storeGAA[pos].rAA=(REAL *)malloc(sizeof(REAL)*size*size);
	
	/*
	printf("%d\t",psGaa->storeGAASize);
	for(int i=0;i<psGaa->switchNum/32+1;i++){
		printf("swtich Status:%4x\t",switchStatus[i]);
	}
	
	printf("size:%d\tSwitchNum:%d\n",size,psGaa->switchNum);
	*/
	
	//gsl_matrix_view A = gsl_matrix_view_array(gAA,size, size);
	
	//计算逆阵inv
	gsl_matrix * inv = gsl_matrix_alloc(size,size);
    gsl_matrix *matrixA=gsl_matrix_alloc(size,size);
    for(size_t i=0;i<(size_t)size;++i){
        for(size_t j=0;j<(size_t)size;++j){
			gsl_matrix_set(matrixA,i,j,gAA[i*size+j]);
		}
    }

    ncs_invert_square_svd_pinv(matrixA, inv, size);
#ifdef NCSLAB_USE_KLU
	g_ncsCrashStage = 3000 + size;
    ncs_prepare_store_klu(&(psGaa->storeGAA[pos]), matrixA, size);
#endif
	g_ncsCrashStage = 4000 + size;
    ncs_prepare_store_lu(&(psGaa->storeGAA[pos]), matrixA, size);
	g_ncsCrashStage = 5000 + size;
    
    //在表格中保存逆阵inv
    psGaa->storeGAA[pos].inv=inv;
    psGaa->lastStoreGAA=&(psGaa->storeGAA[pos]);
    
    /*
    for(size_t i=0;i<size;++i){
        for(size_t j=0;j<size;++j){
			printf("%8.4f", gsl_matrix_get(inv,i,j));
		}
		putchar('\n');
    }*/
    
    gsl_matrix_free(matrixA);
    
    //返回计算的逆阵指针，可以用来进行状态计算
    return inv;
    
}

//如果有无参数改变的标志为1,说明矩阵中有些参数发生变化，因此需要重新计算
//StoreGAA *pStoreGaa，inv矩阵表项的指针
//REAL *gAA, 经过开关状态合并之后的gAA矩阵
//int size,经过开关状态合并之后的gAA矩阵大小
gsl_matrix *caclulateInv(StoreGAA *pStoreGaa,REAL *gAA,int size){
	if (pStoreGaa == NULL) {
		return NULL;
	}
	//释放原有的inv矩阵
	ncs_free_store_cuda(pStoreGaa);
	gsl_matrix_free(pStoreGaa->inv);
	
	//根据Gaa中的数值，计算新的Inv矩阵
	gsl_matrix * inv = gsl_matrix_alloc(size,size);
    gsl_matrix *matrixA=gsl_matrix_alloc(size,size);
    for(size_t i=0;i<(size_t)size;++i){
        for(size_t j=0;j<(size_t)size;++j){
			gsl_matrix_set(matrixA,i,j,gAA[i*size+j]);
		}
    }

    ncs_invert_square_svd_pinv(matrixA, inv, size);
#ifdef NCSLAB_USE_KLU
    ncs_prepare_store_klu(pStoreGaa, matrixA, size);
#endif
    ncs_prepare_store_lu(pStoreGaa, matrixA, size);
    
    //保存计算的inv矩阵
    pStoreGaa->inv=inv;

    gsl_matrix_free(matrixA);
    
    //返回inv矩阵指针
    return inv;
}

int solveStoreGAA(StoreGAA *pStoreGaa,gsl_vector *b,gsl_vector *x){
	if (pStoreGaa == NULL || b == NULL || x == NULL) {
		return 0;
	}
	if (ncs_env_flag_enabled("NCSLAB_CUDA_SOLVE_DGEMV_FORCE") && pStoreGaa->inv != NULL &&
			b->stride == 1 && x->stride == 1 &&
			ncslab_cuda_dgemv_rowmajor_cached_nxn((int)pStoreGaa->inv->size1, pStoreGaa->inv->data,
				b->data, x->data, &pStoreGaa->cudaInv) == 0) {
		return 1;
	}
#ifdef NCSLAB_USE_KLU
	if (pStoreGaa->kluReady && pStoreGaa->kluSymbolic != NULL && pStoreGaa->kluNumeric != NULL
			&& x->stride == 1 && b->stride == 1) {
		long long profStartUs = ncs_prof_now_us();
		for (size_t i = 0; i < b->size; ++i) {
			gsl_vector_set(x, i, gsl_vector_get(b, i));
		}
		int status = klu_solve(pStoreGaa->kluSymbolic, pStoreGaa->kluNumeric,
				(int32_t)x->size, 1, x->data, &pStoreGaa->kluCommon);
		if (status) {
			ncs_prof_add_pair(&g_ncsPerfStats.kluSolveUs, &g_ncsPerfStats.kluSolveCount,
				ncs_prof_now_us() - profStartUs);
			return 1;
		}
	}
#endif
	if (pStoreGaa->luReady && pStoreGaa->lu != NULL && pStoreGaa->perm != NULL) {
		long long profStartUs = ncs_prof_now_us();
		gsl_error_handler_t *oldHandler = gsl_set_error_handler_off();
		int status = gsl_linalg_LU_solve(pStoreGaa->lu, pStoreGaa->perm, b, x);
		gsl_set_error_handler(oldHandler);
		if (status == GSL_SUCCESS) {
			ncs_prof_add_pair(&g_ncsPerfStats.gslLuSolveUs, &g_ncsPerfStats.gslLuSolveCount,
				ncs_prof_now_us() - profStartUs);
			return 1;
		}
	}
	if (pStoreGaa->inv != NULL) {
		if (b->stride == 1 && x->stride == 1 &&
				ncslab_cuda_dgemv_rowmajor_cached_nxn((int)pStoreGaa->inv->size1, pStoreGaa->inv->data,
					b->data, x->data, &pStoreGaa->cudaInv) == 0) {
			return 1;
		}
		gsl_blas_dgemv(CblasNoTrans, 1.0, pStoreGaa->inv, b, 0.0, x);
		return 1;
	}
	return 0;
}

int solveDenseLinearSystem(REAL *gAA, REAL *iA, int size, gsl_vector *x) {
	if (gAA == NULL || iA == NULL || x == NULL || size <= 0) {
		return 0;
	}
	static StoreGAA cachedStore;
	static std::vector<REAL> cachedA;
	static int cachedInitialized = 0;
	static int cachedSize = 0;
	const size_t matrixLen = (size_t)size * (size_t)size;
	gsl_vector_view b = gsl_vector_view_array(iA, (size_t)size);
	if (cachedInitialized && cachedSize == size && cachedA.size() == matrixLen
			&& std::memcmp(cachedA.data(), gAA, matrixLen * sizeof(REAL)) == 0
			&& solveStoreGAA(&cachedStore, &b.vector, x)) {
		return 1;
	}
	if (cachedInitialized) {
#ifdef NCSLAB_USE_KLU
		ncs_free_store_klu(&cachedStore);
#endif
		ncs_free_store_lu(&cachedStore);
		ncs_free_store_cuda(&cachedStore);
		std::memset(&cachedStore, 0, sizeof(cachedStore));
		cachedInitialized = 0;
		cachedSize = 0;
		cachedA.clear();
	}
	gsl_matrix *matrixA = gsl_matrix_alloc((size_t)size, (size_t)size);
	if (matrixA == NULL) {
		return 0;
	}
	for(size_t i=0;i<(size_t)size;++i){
		for(size_t j=0;j<(size_t)size;++j){
			gsl_matrix_set(matrixA,i,j,gAA[i*(size_t)size+j]);
		}
	}

	StoreGAA tempStore;
	std::memset(&tempStore, 0, sizeof(tempStore));
	tempStore.size = (uint32_T)size;
#ifdef NCSLAB_USE_KLU
	ncs_prepare_store_klu(&tempStore, matrixA, size);
	if (!tempStore.kluReady) {
		ncs_prepare_store_lu(&tempStore, matrixA, size);
	}
#else
	ncs_prepare_store_lu(&tempStore, matrixA, size);
#endif
	if (solveStoreGAA(&tempStore, &b.vector, x)) {
		gsl_matrix_free(matrixA);
		cachedStore = tempStore;
		cachedA.assign(gAA, gAA + matrixLen);
		cachedSize = size;
		cachedInitialized = 1;
		return 1;
	}

	long long profStartUs = ncs_prof_now_us();
	gsl_matrix *ncs_A = matrixA;
	gsl_matrix *ncs_V = gsl_matrix_alloc((size_t)size, (size_t)size);
	gsl_vector *ncs_S = gsl_vector_alloc((size_t)size);
	gsl_vector *ncs_work = gsl_vector_alloc((size_t)size);
	gsl_vector *ncs_utb = gsl_vector_alloc((size_t)size);
	gsl_linalg_SV_decomp(ncs_A, ncs_V, ncs_S, ncs_work);
	double ncs_smax = 0.0;
	for (int ncs_k = 0; ncs_k < size; ncs_k++) {
		double ncs_si = gsl_vector_get(ncs_S, (size_t)ncs_k);
		if (ncs_si > ncs_smax) ncs_smax = ncs_si;
	}
	const double ncs_tol = (ncs_smax > 0.0) ? (ncs_smax * 1.0e-14 * (double)size) : 0.0;
	gsl_blas_dgemv(CblasTrans, 1.0, ncs_A, &b.vector, 0.0, ncs_utb);
	for (int ncs_k = 0; ncs_k < size; ncs_k++) {
		double ncs_sj = gsl_vector_get(ncs_S, (size_t)ncs_k);
		double ncs_y = gsl_vector_get(ncs_utb, (size_t)ncs_k);
		gsl_vector_set(ncs_utb, (size_t)ncs_k, (ncs_sj > ncs_tol) ? (ncs_y / ncs_sj) : 0.0);
	}
	gsl_blas_dgemv(CblasNoTrans, 1.0, ncs_V, ncs_utb, 0.0, x);
	gsl_vector_free(ncs_utb);
	gsl_vector_free(ncs_work);
	gsl_vector_free(ncs_S);
	gsl_matrix_free(ncs_V);
#ifdef NCSLAB_USE_KLU
	ncs_free_store_klu(&tempStore);
#endif
	ncs_free_store_lu(&tempStore);
	gsl_matrix_free(matrixA);
	ncs_prof_add_pair(&g_ncsPerfStats.svdUs, &g_ncsPerfStats.svdCount,
		ncs_prof_now_us() - profStartUs);
	return 1;
}

//设定指定位置的开关状态
//pos，开关的编号
//status 开关状态
void setSwitchStatus(SwitchGAA *psGaa,int pos,int status){
	int word=pos/32;
	int bit=pos%32;
	if(status){
		psGaa->switchStatus[word]|=(0x01)<<bit;	
	}
	else{
		psGaa->switchStatus[word]&=(~(0x01<<bit));		
	}
	//printf("%d\t%d\n",pos,status);
	//printf("%x\n",psGaa->switchStatus[0]);
}

//匹配开关状态表，寻找匹配的inv矩阵，如果找到，就返回找到矩阵，如果没有，就返回NULL
StoreGAA *findStoreGAA(SwitchGAA *psGaa,uint32_T *switchStatus){
	if (psGaa->lastStoreGAA != NULL) {
		int same=1;
		for(int j=0;j<psGaa->switchNum/32+1;j++){
			if(psGaa->lastStoreGAA->switchStatus[j]!=switchStatus[j]){
				same=0;
			}
		}
		if(same){
			psGaa->cacheHits++;
			return psGaa->lastStoreGAA;
		}
	}
	for(int i=0;i<psGaa->storeGAASize;i++){
		int same=1;
		for(int j=0;j<psGaa->switchNum/32+1;j++){
			if(psGaa->storeGAA[i].switchStatus[j]!=switchStatus[j]){
				same=0;
			}
		}
		if(same){
			psGaa->cacheHits++;
			psGaa->lastStoreGAA=&(psGaa->storeGAA[i]);
			return &(psGaa->storeGAA[i]);
		}
	}
	psGaa->cacheMisses++;
	return NULL;
}

//获取pos位置的开关状态
int getSwtichStatus(SwitchGAA *psGaa,int pos){
	int word=pos/32;
	int bit=pos%32;
	
	return (psGaa->switchStatus[word])&(0x01<<bit);
}


//与circuitCombine类似的功能，但是只合并iA和索引表
void CircuitCombineIA(REAL *iA,int *vIndex,int *size_p,int* ref,int n,int m,int indexSize,int refSize){
	int size=*size_p;

	//如果合并的节点中有参考节点，电压方程由参考节点决定，那么就只需要消去非参考节点的行列即可
	if(isRef(n,ref,refSize)||isRef(m,ref,refSize)){
		int nRef,nn;
		

		//让n成为消去的节点
		int refNode = m;
		if(isRef(n,ref,refSize)){
			refNode=n;
			n=m;
		}

		//nn是变换后的消去节点
		nn=vIndex[n];
		nRef=vIndex[refNode];
		
		//让消去的节点指向参考节点
		vIndex[n]=vIndex[refNode];
		
		//消去nn在iA中的节点
		for(int i=nn;i<size-1;i++){
			iA[i]=iA[i+1];
		}
				
		//重新更新vIndex，让nn后面的节点都减一
		for(int i=0;i<indexSize;i++){
			if(vIndex[i]>nn){
				vIndex[i]-=1;
			}
		}

		//矩阵的大小减一
		size--;
	}
	else{
		//解析合并的两个点在新矩阵中的位置
		int nn=vIndex[n];
		int mm=vIndex[m];
		//如果不是一个点，才需要合并，如果是一个点，就忽略
		if(nn!=mm){
			
			//重新排布，让nn在前，mm在后
			if(nn>mm){
				int temp=nn,temp1=n;
				nn=mm;
				n=m;
				mm=temp;
				m=temp1;
			}
			//printf("%d\t%d\n",nn,mm);

			
			//iA也进行相应的合并
			iA[nn]+=iA[mm];

			
			//iA也是要把mm后面的元素依次向前
			for(int i=mm;i<size-1;i++){
				iA[i]=iA[i+1];
			}
			/*
			for(int i=0;i<size;i++){
				printf("%d\t",vIndex[i]);
			}
			printf("\n");*/

			//m的索引合并到n里面
			vIndex[m]=vIndex[n];
			
			

			//重新更新vIndex，让mm后面的节点都减一
			for(int i=0;i<indexSize;i++){
				if(vIndex[i]>mm){
					vIndex[i]-=1;
				}
			}

			//矩阵的大小减一
			size--;
			
		}
	
		
	}

	*size_p=size;
}

int isPartRef(int nodeId,int *ref,int refSize,int *vIndex){

	for(int i=0;i<refSize;i++){
		if(vIndex[ref[i]]==vIndex[nodeId]){
			return 1;
		}
	}

	return 0;
}

#include <fstream>
#include "nlohmann/json.hpp"
using json = nlohmann::json;

/**
 * Check for parameter updates from Java backend and apply changes.
 * TCP mode: reads length-prefixed JSON from g_simSocket (non-blocking).
 * Fallback mode: reads param_updates.json file.
 * Called periodically during real-time simulation (before each data send).
 */
void checkParameterUpdates() {
	// TCP mode: receive param updates via socket
	if (g_simSocket >= 0) {
		static std::string recvBuffer;
		static int expectedLen = -1;
		static char headerBuffer[4];
		static int headerBytesReceived = 0;

		while (true) {
			if (expectedLen < 0) {
				// Try to read 4-byte little-endian length header
				if (headerBytesReceived < 4) {
					int n = recv(g_simSocket, headerBuffer + headerBytesReceived, 4 - headerBytesReceived, 0);
					if (n > 0) {
						headerBytesReceived += n;
					} else {
						break; // no more data available right now
					}
				}
				if (headerBytesReceived == 4) {
					expectedLen = *(int*)headerBuffer;
					headerBytesReceived = 0;
				} else {
					break; // incomplete header
				}
			}

			if (expectedLen >= 0) {
				size_t current = recvBuffer.size();
				int need = expectedLen - (int)current;
				if (need > 0) {
					char buf[1024];
					int toRead = need < 1024 ? need : 1024;
					int n = recv(g_simSocket, buf, toRead, 0);
					if (n > 0) {
						recvBuffer.append(buf, n);
					} else {
						break; // no more data available right now
					}
				}

				if ((int)recvBuffer.size() == expectedLen) {
					// Complete JSON received
					try {
						json j = json::parse(recvBuffer);
						recvBuffer.clear();
						expectedLen = -1;

						// Handle pause/resume commands
						if (j.contains("command") && j["command"].is_string()) {
							std::string cmd = j["command"];
							if (cmd == "pause") {
								g_simulationPaused = true;
							} else if (cmd == "resume") {
								g_simulationPaused = false;
							}
						}

						// Handle parameter updates
						if (j.contains("updates") && j["updates"].is_array()) {
							for (const auto& update : j["updates"]) {
								std::string blockPath = update.value("blockPath", "");
								std::string blockName = update.value("blockName", "");
								std::string paramName = update.value("paramName", "");
								double value = update.value("value", 0.0);

								if (paramName.empty()) continue;

								// 使用完整路径 blockPath/blockName 匹配 param->path，
				// 以区分不同子系统内部的同名模块
				std::string targetPath = blockPath;
if (!blockName.empty()) {
	if (!targetPath.empty()) targetPath += "/";
	targetPath += blockName;
}
if (targetPath.empty()) continue;

for (int i = 0; i < mp->parameterNum; i++) {
	PARAMETER* param = mp->parameters[i];
	if (!param || !param->name || !param->path || !param->vp) continue;

	std::string paramPath = param->path;
	bool blockMatches = paramPath == targetPath;
	bool nameMatches = strcmp(param->name, paramName.c_str()) == 0;

	if (blockMatches && nameMatches) {
		if (param->type == SINGLE) {
			*((REAL*)param->vp) = (REAL)value;
		}
		break;
	}
}
							}
						}
					} catch (...) {
						recvBuffer.clear();
						expectedLen = -1;
					}
				} else if ((int)recvBuffer.size() > expectedLen) {
					recvBuffer.clear();
					expectedLen = -1;
				}
			}
		}
		return;
	}

	// Fallback: file-based for non-TCP mode
	std::ifstream file("param_updates.json");
	if (!file.is_open()) return;

	try {
		json j;
		file >> j;
		file.close();

		std::remove("param_updates.json");

		if (!j.contains("updates") || !j["updates"].is_array()) return;

		for (const auto& update : j["updates"]) {
			std::string blockPath = update.value("blockPath", "");
			std::string blockName = update.value("blockName", "");
			std::string paramName = update.value("paramName", "");
			double value = update.value("value", 0.0);

			if (paramName.empty()) continue;

			// 使用完整路径 blockPath/blockName 匹配 param->path，
// 以区分不同子系统内部的同名模块
std::string targetPath = blockPath;
if (!blockName.empty()) {
	if (!targetPath.empty()) targetPath += "/";
	targetPath += blockName;
}
if (targetPath.empty()) continue;

for (int i = 0; i < mp->parameterNum; i++) {
	PARAMETER* param = mp->parameters[i];
	if (!param || !param->name || !param->path || !param->vp) continue;

	std::string paramPath = param->path;
	bool blockMatches = paramPath == targetPath;
	bool nameMatches = strcmp(param->name, paramName.c_str()) == 0;

	if (blockMatches && nameMatches) {
		if (param->type == SINGLE) {
			*((REAL*)param->vp) = (REAL)value;
		}
		break;
	}
}
		}
	} catch (...) {
		std::remove("param_updates.json");
	}
}
