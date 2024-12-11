#ifndef __HARDWARE_H
#define __HARDWARE_H

#include "ncslabdefines.hpp"

#include "ncslabccode.hpp"

#include "ncs_serialport.h"
#include "ncslab.hpp"
#include <sys/time.h>
#include <sys/select.h>
#include <time.h>
#include <stdio.h>
#include "wiringPi.h"
#ifdef __linux
#include <unistd.h>
#include <pthread.h>        // 对于 pthread 相关函数和类型
#endif
#include <signal.h>         // 对于 union sigval
#include <string.h>

extern "C"{
typedef struct {
	REAL pumpPWM;
	unsigned int speed_lastEdge = 0;

	double speed_frequency=0;

	pthread_mutex_t  speed_timerCritical;
	timer_t speed_main_timer;

	unsigned int speed_lastTimer = 0;

	unsigned int speed_counter=0;
	unsigned int speed_counter_out=0;
	unsigned int speed_counter_in;

	unsigned int level_lastEdge = 0;
	double level_out;
	double level;
	unsigned long level_count;
	pthread_mutex_t  level_timerCritical;

}WATER_LEVEL;

typedef struct {
	REAL alpPWM;
	unsigned int fanspeed_lastEdge = 0;

	double fanspeed_frequency=0;

	pthread_mutex_t  fanspeed_timerCritical;
	timer_t fanspeed_main_timer;

	unsigned int fanspeed_lastTimer = 0;

	unsigned int fanspeed_counter=0;
	unsigned int fanspeed_counter_out=0;
	unsigned int fanspeed_counter_in;
       unsigned int fanspeed_output;
	double position;
}ALP;

typedef struct {
	REAL raspFanPWM;
	unsigned int fanspeed_lastEdge = 0;

	double fanspeed_frequency=0;

	pthread_mutex_t  fanspeed_timerCritical;
	timer_t fanspeed_main_timer;

	unsigned int fanspeed_lastTimer = 0;

	unsigned int fanspeed_counter=0;
	unsigned int fanspeed_counter_out=0;
	unsigned int fanspeed_counter_in;
       unsigned int fanspeed_output;
}RASPFAN;



void initWaterLevel(WATER_LEVEL *);
void outputWaterLevel(WATER_LEVEL *);

void initAlp(ALP *);
void outputAlp(ALP *);


void initRaspFan(RASPFAN *);
void outputRaspFan(RASPFAN *);
}
#endif // __HARDWARE_H
