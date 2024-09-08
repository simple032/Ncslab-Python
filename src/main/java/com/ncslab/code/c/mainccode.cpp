/**
 * @file mainccode.cpp
 * @author 
 * @brief just for test 
 * @version 0.1
 * @date 2024-03-01
 * 
 * @copyright Copyright (c) 2024
 * 
 */
#include <iostream>
#include <cmath>
#include "mainccode.hpp"
#include "ncs_serialport.h"
#include "ncslab.h"
#include "Matrix.hpp"

#ifdef _RT
#include"hardware.h"
#include"ADS1256.h"
#include"DAC8532.h"
#include"Debug.h"
#include"wiringPi.h"
#include"wiringPiSPI.h"
#endif


double sample_time[5]={};
int sample_i=0;

/*Define arrays for block discrete_Delay:(2)PID Controller1*/
double Block2save_data[5];

extern MODEL* mp;


/*Define variables for parameters*/
REAL PID_Controller1_P;
REAL PID_Controller1_I;
REAL PID_Controller1_D;
REAL PID_Controller1_N;
REAL Set_Point_value;

/*Define variables for states*/
REAL Block1_State_speedState;
REAL Block1_State_speedState_Derivative;
REAL Block2_State_stateIntegral;
REAL Block2_State_stateIntegral_Derivative;
REAL Block2_State_stateFilter;
REAL Block2_State_stateFilter_Derivative;

/*Define variables for output signals*/
REAL Block1_Output1;
REAL Block2_Output1;
REAL Block3_Output1;
REAL Block4_Output1;


/*Define data structures*/
/*Define terminal structures*/
SCOPE Block5_Scope_Scope1={(char *)"Scope1",3000,1,1};
TERMINAL Block5_Terminal_Scope1={Scope,&Block5_Scope_Scope1};
TERMINAL *terminals[1];
/*Define inputPort structures*/
INPUT_PORT inputPort1_1={(char *)"newMotor1_in1",1};
INPUT_PORT *inputPorts1[1];
INPUT_PORT inputPort2_1={(char *)"PID_Controller1_in1",1};
INPUT_PORT *inputPorts2[1];
INPUT_PORT **inputPorts3=NULL;
INPUT_PORT inputPort4_1={(char *)"Sum1_in1",1};
INPUT_PORT inputPort4_2={(char *)"Sum1_in2",1};
INPUT_PORT *inputPorts4[2];
INPUT_PORT inputPort5_1={(char *)"Scope1",1};
INPUT_PORT *inputPorts5[1];
/*Define outputPort structures*/
OUTPUT_PORT outputPort1_1={(char *)"newMotor1_Speed",1};
OUTPUT_PORT *outputPorts1[1];
OUTPUT_PORT outputPort2_1={(char *)"PID_Controller1_out1",1};
OUTPUT_PORT *outputPorts2[1];
OUTPUT_PORT outputPort3_1={(char *)"Set_Point_out1",1};
OUTPUT_PORT *outputPorts3[1];
OUTPUT_PORT outputPort4_1={(char *)"Sum1_out1",1};
OUTPUT_PORT *outputPorts4[1];
OUTPUT_PORT **outputPorts5=NULL;
/*Define parameter structures*/
PARAMETER **parameters1=NULL;
PARAMETER parameter2_1;
PARAMETER parameter2_2;
PARAMETER parameter2_3;
PARAMETER parameter2_4;
PARAMETER *parameters2[4];
PARAMETER parameter3_1;
PARAMETER *parameters3[1];
PARAMETER **parameters4=NULL;
PARAMETER **parameters5=NULL;
PARAMETER *parameters[5];
/*Define state structures*/
STATE state1_1={(char *)"speedState",1};
STATE *states1[1];
STATE state2_1={(char *)"stateIntegral",1};
STATE state2_2={(char *)"stateFilter",1};
STATE *states2[2];
STATE **states3=NULL;
STATE **states4=NULL;
STATE **states5=NULL;
STATE *states[3];
/*Define signal structures*/
SIGNAL signal1_In1;
SIGNAL signal1_Out1;
SIGNAL *signals1[2];
SIGNAL signal2_In1;
SIGNAL signal2_Out1;
SIGNAL *signals2[2];
SIGNAL signal3_Out1;
SIGNAL *signals3[1];
SIGNAL signal4_In1;
SIGNAL signal4_In2;
SIGNAL signal4_Out1;
SIGNAL *signals4[3];
SIGNAL signal5_In1;
SIGNAL *signals5[1];
SIGNAL *signals[9];
/*Define block structures*/
BLOCK block1={(char *)"newMotor",(char *)"newMotor1",1,1,0,1,2,0.0,0};
BLOCK block2={(char *)"PID Controller",(char *)"PID Controller1",1,1,4,2,2,0.0,0};
BLOCK block3={(char *)"Constant",(char *)"Set_Point",0,1,1,0,1,0.0,0};
BLOCK block4={(char *)"Sum",(char *)"Sum1",2,1,0,0,3,0.0,0};
BLOCK block5={(char *)"Scope",(char *)"Scope1",1,0,0,0,1,0.0,0};
BLOCK *blocks[5];
MODEL model={(char *)"PI Control",5,0.01,0.0,20.0};

void NCSLabInit(){
/*Initialize data structure*/
/*Initialize terminals*/
terminals[0]=&Block5_Terminal_Scope1;
model.terminalNum=1;
/*Initialize inputs*/
/*Initialize inputs for block (1)newMotor1*/
inputPort1_1.vp=&Block2_Output1;
/*Initialize inputs for block (2)PID Controller1*/
inputPort2_1.vp=&Block4_Output1;
/*Initialize inputs for block (3)Set_Point*/
/*Initialize inputs for block (4)Sum1*/
inputPort4_1.vp=&Block3_Output1;
inputPort4_2.vp=&Block1_Output1;
/*Initialize inputs for block (5)Scope1*/
inputPort5_1.vp=&Block1_Output1;
/*Initialize outputs*/
/*Initialize outputs for block (1)newMotor1*/
outputPort1_1.vp=&Block1_Output1;
/*Initialize outputs for block (2)PID Controller1*/
outputPort2_1.vp=&Block2_Output1;
/*Initialize outputs for block (3)Set_Point*/
outputPort3_1.vp=&Block3_Output1;
/*Initialize outputs for block (4)Sum1*/
outputPort4_1.vp=&Block4_Output1;
/*Initialize outputs for block (5)Scope1*/
/*Initialize parameters*/
/*Initialize parameters for block (1)newMotor1*/
/*Initialize parameters for block (2)PID Controller1*/
parameter2_1.name=(char *)"PID_Controller1_P";
parameter2_1.width=1;
parameter2_1.height=1;
parameter2_1.type=SINGLE;
parameter2_1.vp=&PID_Controller1_P;
parameter2_1.path=(char *)"s207744/PID Controller1";
parameter2_2.name=(char *)"PID_Controller1_I";
parameter2_2.width=1;
parameter2_2.height=1;
parameter2_2.type=SINGLE;
parameter2_2.vp=&PID_Controller1_I;
parameter2_2.path=(char *)"s207744/PID Controller1";
parameter2_3.name=(char *)"PID_Controller1_D";
parameter2_3.width=1;
parameter2_3.height=1;
parameter2_3.type=SINGLE;
parameter2_3.vp=&PID_Controller1_D;
parameter2_3.path=(char *)"s207744/PID Controller1";
parameter2_4.name=(char *)"PID_Controller1_N";
parameter2_4.width=1;
parameter2_4.height=1;
parameter2_4.type=SINGLE;
parameter2_4.vp=&PID_Controller1_N;
parameter2_4.path=(char *)"s207744/PID Controller1";
/*Initialize parameters for block (3)Set_Point*/
parameter3_1.name=(char *)"Set_Point_value";
parameter3_1.width=1;
parameter3_1.height=1;
parameter3_1.type=SINGLE;
parameter3_1.vp=&Set_Point_value;
parameter3_1.path=(char *)"s207744/Set_Point";
/*Initialize parameters for block (4)Sum1*/
/*Initialize parameters for block (5)Scope1*/
/*Initialize states*/
/*Initialize states for block (1)newMotor1*/
state1_1.height=1;
state1_1.width=1;
state1_1.type=SINGLE;
state1_1.vp=&Block1_State_speedState;
state1_1.dvp=&Block1_State_speedState_Derivative;
/*Initialize states for block (2)PID Controller1*/
state2_1.height=1;
state2_1.width=1;
state2_1.type=SINGLE;
state2_1.vp=&Block2_State_stateIntegral;
state2_1.dvp=&Block2_State_stateIntegral_Derivative;
state2_2.height=1;
state2_2.width=1;
state2_2.type=SINGLE;
state2_2.vp=&Block2_State_stateFilter;
state2_2.dvp=&Block2_State_stateFilter_Derivative;
/*Initialize states for block (3)Set_Point*/
/*Initialize states for block (4)Sum1*/
/*Initialize states for block (5)Scope1*/
/*Initialize signals*/
/*Initialize signals for block (1)newMotor1*/
signal1_In1.vp=&Block2_Output1;
signal1_In1.width=1;
signal1_In1.height=1;
signal1_In1.name=(char *)"newMotor1_in1";
signal1_In1.path=(char *)"s207744/newMotor1/newMotor1_in1";
signal1_In1.type=SINGLE;
signal1_Out1.vp=&Block1_Output1;
signal1_Out1.width=1;
signal1_Out1.height=1;
signal1_Out1.name=(char *)"newMotor1_Speed";
signal1_Out1.path=(char *)"s207744/newMotor1/newMotor1_Speed";
signal1_Out1.type=SINGLE;
/*Initialize signals for block (2)PID Controller1*/
signal2_In1.vp=&Block4_Output1;
signal2_In1.width=1;
signal2_In1.height=1;
signal2_In1.name=(char *)"PID_Controller1_in1";
signal2_In1.path=(char *)"s207744/PID Controller1/PID_Controller1_in1";
signal2_In1.type=SINGLE;
signal2_Out1.vp=&Block2_Output1;
signal2_Out1.width=1;
signal2_Out1.height=1;
signal2_Out1.name=(char *)"PID_Controller1_out1";
signal2_Out1.path=(char *)"s207744/PID Controller1/PID_Controller1_out1";
signal2_Out1.type=SINGLE;
/*Initialize signals for block (3)Set_Point*/
signal3_Out1.vp=&Block3_Output1;
signal3_Out1.width=1;
signal3_Out1.height=1;
signal3_Out1.name=(char *)"Set_Point_out1";
signal3_Out1.path=(char *)"s207744/Set_Point/Set_Point_out1";
signal3_Out1.type=SINGLE;
/*Initialize signals for block (4)Sum1*/
signal4_In1.vp=&Block3_Output1;
signal4_In1.width=1;
signal4_In1.height=1;
signal4_In1.name=(char *)"Sum1_in1";
signal4_In1.path=(char *)"s207744/Sum1/Sum1_in1";
signal4_In1.type=SINGLE;
signal4_In2.vp=&Block1_Output1;
signal4_In2.width=1;
signal4_In2.height=1;
signal4_In2.name=(char *)"Sum1_in2";
signal4_In2.path=(char *)"s207744/Sum1/Sum1_in2";
signal4_In2.type=SINGLE;
signal4_Out1.vp=&Block4_Output1;
signal4_Out1.width=1;
signal4_Out1.height=1;
signal4_Out1.name=(char *)"Sum1_out1";
signal4_Out1.path=(char *)"s207744/Sum1/Sum1_out1";
signal4_Out1.type=SINGLE;
/*Initialize signals for block (5)Scope1*/
signal5_In1.vp=&Block1_Output1;
signal5_In1.width=1;
signal5_In1.height=1;
signal5_In1.name=(char *)"Scope1";
signal5_In1.path=(char *)"s207744/Scope1/Scope1";
signal5_In1.type=SINGLE;
/*Initialize blocks*/
/*Initialize block (1)newMotor1*/
inputPorts1[0]=&inputPort1_1;
block1.inputPorts=inputPorts1;
outputPorts1[0]=&outputPort1_1;
block1.outputPorts=outputPorts1;
block1.parameters=parameters1;
states1[0]=&state1_1;
states[0]=&state1_1;
block1.states=states1;
signals1[0]=&signal1_In1;
signals[0]=&signal1_In1;
signals1[1]=&signal1_Out1;
signals[1]=&signal1_Out1;
block1.signals=signals1;
/*Initialize block (2)PID Controller1*/
inputPorts2[0]=&inputPort2_1;
block2.inputPorts=inputPorts2;
outputPorts2[0]=&outputPort2_1;
block2.outputPorts=outputPorts2;
parameters2[0]=&parameter2_1;
parameters[0]=&parameter2_1;
parameters2[1]=&parameter2_2;
parameters[1]=&parameter2_2;
parameters2[2]=&parameter2_3;
parameters[2]=&parameter2_3;
parameters2[3]=&parameter2_4;
parameters[3]=&parameter2_4;
block2.parameters=parameters2;
states2[0]=&state2_1;
states[1]=&state2_1;
states2[1]=&state2_2;
states[2]=&state2_2;
block2.states=states2;
signals2[0]=&signal2_In1;
signals[2]=&signal2_In1;
signals2[1]=&signal2_Out1;
signals[3]=&signal2_Out1;
block2.signals=signals2;
/*Initialize block (3)Set_Point*/
block3.inputPorts=inputPorts3;
outputPorts3[0]=&outputPort3_1;
block3.outputPorts=outputPorts3;
parameters3[0]=&parameter3_1;
parameters[4]=&parameter3_1;
block3.parameters=parameters3;
block3.states=states3;
signals3[0]=&signal3_Out1;
signals[4]=&signal3_Out1;
block3.signals=signals3;
/*Initialize block (4)Sum1*/
inputPorts4[0]=&inputPort4_1;
inputPorts4[1]=&inputPort4_2;
block4.inputPorts=inputPorts4;
outputPorts4[0]=&outputPort4_1;
block4.outputPorts=outputPorts4;
block4.parameters=parameters4;
block4.states=states4;
signals4[0]=&signal4_In1;
signals[5]=&signal4_In1;
signals4[1]=&signal4_In2;
signals[6]=&signal4_In2;
signals4[2]=&signal4_Out1;
signals[7]=&signal4_Out1;
block4.signals=signals4;
/*Initialize block (5)Scope1*/
inputPorts5[0]=&inputPort5_1;
block5.inputPorts=inputPorts5;
block5.outputPorts=outputPorts5;
block5.parameters=parameters5;
block5.states=states5;
signals5[0]=&signal5_In1;
signals[8]=&signal5_In1;
block5.signals=signals5;
/*Initialize model*/
blocks[0]=&block1;
blocks[1]=&block2;
blocks[2]=&block3;
blocks[3]=&block4;
blocks[4]=&block5;
model.blocks=blocks;
model.time=model.startTime;
model.offset=0;
model.discreteTime=model.startTime;
model.signalNum=9;
model.signals=signals;
model.parameterNum=5;
model.parameters=parameters;
model.stateNum=3;
model.states=states;

/*Code for initialization of block NewMotor:(1)newMotor1*/
Block1_State_speedState=0;
/*Code for initialization of block PID Controller:(2)PID Controller1*/
PID_Controller1_P=0.004;
PID_Controller1_I=0.05;
PID_Controller1_D=0.0;
PID_Controller1_N=100.0;
Block2_State_stateIntegral=0;
Block2_State_stateFilter=0;
/*Code for initialization of block Contant:(3)Set_Point*/
Set_Point_value=10.0;
/*Code for initialization of block Scope:(5)Scope1*/

}
void NCSLabOutput(){
/*Code for output of block NewMotor:(1)newMotor1*/
Block1_Output1=10000*Block1_State_speedState;
/*Code for output of block Constant:(3)Set_Point*/
Block3_Output1=Set_Point_value;
/*Code for output of block Sum:(4)Sum1*/
Block4_Output1=0+Block3_Output1-Block1_Output1;
/*Code for output of block PID Controller:(2)PID Controller1*/
if(mp->majorStep>0) {
Block2save_data[0]=PID_Controller1_P*Block4_Output1;
Block2save_data[1]=PID_Controller1_D*Block4_Output1;
Block2save_data[2]=PID_Controller1_I*Block4_Output1;
Block2save_data[3]=(Block2save_data[1]-Block2_State_stateFilter)*PID_Controller1_N;
Block2save_data[4]=Block2save_data[0]+Block2_State_stateIntegral+Block2save_data[3];
Block2_Output1=Block2save_data[4];}

}
void NCSLabDerivative(){
/*Code for Derivative of NewMotor:(1)newMotor1*/
Block1_State_speedState_Derivative=(Block2_Output1*0.01-Block1_State_speedState)*11.11111111111111;
/*Code for Derivative of PID Controller:(2)PID Controller1*/
Block2_State_stateIntegral_Derivative=Block2save_data[2];
Block2_State_stateFilter_Derivative=Block2save_data[3];

}
void NCSLabUpdate(){
/*Code for update of block newMotor:(1)newMotor1*/
Block1_State_speedState=Block1_State_speedState+Block1_State_speedState_Derivative*model.stepSize;
/*Code for Update of :(2)PID Controller1*/
Block2_State_stateIntegral=Block2_State_stateIntegral+Block2_State_stateIntegral_Derivative*model.stepSize;
Block2_State_stateFilter=Block2_State_stateFilter+Block2_State_stateFilter_Derivative*model.stepSize;
/*Code for update of block Constant:(3)Set_Point*/
/*Code for update of block Sum:(4)Sum1*/
/*Code for update of block Scope:(5)Scope1*/

}
void NCSLabDiscreteUpdate(){
double dist;

}
void NCSLabSinkOutput(){
/*Code for output of block Scope:(5)Scope1*/
if(sfcnIsMajorStep()){
Block5_Scope_Scope1.timeList.push_back(sfcnGetT());
Block5_Scope_Scope1.dataList.push_back(Block1_Output1);
}
block1.discreteUpdated=0;


}
void NCSLabTerminate(){
/*Code for terminate code of block Scope:(5)Scope1*/
}

MODEL * NCSLabGetModelP(){
return &model;
}

