#ifndef __NCS_PACKET_H
#define __NCS_PACKET_H

#include <stdlib.h>
#include <string.h>
#include <stdint.h>
#include <stdio.h>
#define UDP_VERSION 5
#if UDP_VERSION == 1
typedef int16_t data_t;

typedef struct UDP_PACKET_tag{
    uint32_t timestamp;
    uint16_t total; 
    uint8_t inputWidth; 
    uint8_t inputLength; 
    data_t** data;
}UDP_Packet_t;

void PacketInit(UDP_Packet_t* ppacket);

#elif UDP_VERSION == 2

typedef struct UDP_Payload_tag {
	struct UDP_Payload_tag* next;
	uint8_t mode;
	uint8_t channel;
	data_t** buf;
}NCS_UDP_Payload_t;

typedef struct{
    uint32_t timestamp;    
    //uint16_T total;
    //uint8_T inputWidth; //????
    uint8_t inputLength; //??????
	NCS_UDP_Payload_t* payload[3];
}NCS_UDP_Packet_t;

typedef struct NCS_Node_tag{
	uint8_t* data;
	uint16_t datalength;

    struct NCS_Node_tag* next;
}NCS_Node;

typedef struct NCS_Frame_tag {
	uint8_t cmd;
	uint16_t period;
	uint8_t iochannels[6];
	uint8_t width;
}NCS_Frame;

#elif UDP_VERSION >= 4

typedef int16_t data_t;




#define DO_IDX 0
#define AO_IDX 1
#define PWMO_IDX 2
#define DI_IDX 3
#define AI_IDX 4
#define PWMI_IDX 5

#define NCS_FRAME_FUN_HEAD 2
#define NCS_FRAME_FUN_LENGTH 2
#define NCS_FRAME_FUN_VERSION 1
#define NCS_FRAME_FUN_CHECKSUM 2

#define NCS_FRAME_MIN_LEN  \
	(NCS_FRAME_FUN_HEAD + 2*NCS_FRAME_FUN_LENGTH \
	+ NCS_FRAME_FUN_VERSION + NCS_FRAME_FUN_CHECKSUM) 

#define  	NCS_RESP_ONUNSUPPORT  0x11

#define 	NCS_CMD_WRITE	0x06
#define 	NCS_CMD_READ	0x03

#define 	NCS_CMD_ONINIT  0x80
#define 	NCS_RESP_ONINIT  0x81

#define 	NCS_CMD_ONINPUT  0x90
#define 	NCS_RESP_ONINPUT  0x91

#define 	NCS_CMD_ONTEST  0xFF
#define 	NCS_RESP_ONTEST  0x00

typedef struct NCS_Node_tag{
	uint8_t* data;
	uint16_t datalength;
    struct NCS_Node_tag* next;
}NCS_Node;

typedef struct NCS_Frame_tag {	
	uint8_t cmd;
	uint8_t maxStep;
	//uint16_t communicatePeriod; 
    //uint16_t controlPeriod;
	//uint16_t samplePeriod; 	//sample period = control period
	uint8_t iochannels[6];		
}NCS_Frame;

#if UDP_VERSION == 4
typedef struct NCS_InitStructure_tag{
    uint8_t mode;
    uint8_t channel;
    uint8_t width;
    uint8_t length;
}NCS_InitStructure_t;

typedef struct UDP_Channel_tag {
	struct UDP_Channel_tag* next;	
	uint8_t width; // prediction step
    uint8_t length; // controlPeriod/samplePeriod
	data_t** buf;
	//void (*addData)(data_t* ptr, uint8_t length);
}NCS_UDP_Channel_t;

typedef enum{
	Digit,
	Analog,
	PWM
}NCS_Mode;

typedef enum{
	Output=0,
	Input
}NCS_Direction;

typedef struct UDP_Payload_tag {
	struct UDP_Payload_tag* next;
	uint8_t mode;
	NCS_UDP_Channel_t* channels[8];
}NCS_UDP_Payload_t;

typedef struct{
    uint32_t timestamp;    
    //uint16_T total;
    //uint8_T inputWidth; //
    uint8_t inputLength; //
	uint8_t maxStep; 
	NCS_UDP_Payload_t* payload[3];
}NCS_UDP_Packet_t;

NCS_UDP_Packet_t* PacketConstructor(uint8_t outputFlag, const NCS_Frame* pframe);
void PacketDestructor(NCS_UDP_Packet_t* ppacket);

uint16_t Packet2Bytes(uint8_t* data, const NCS_UDP_Packet_t* ppacket);
uint16_t Bytes2Packet(NCS_UDP_Packet_t* ppacket, const uint8_t* data, uint16_t cmdLength);
int AddNewData(NCS_UDP_Channel_t* chn , const data_t* src, uint8_t srcLength);


#elif UDP_VERSION == 4 //������һ����Ƭ����ͬʱ�����������

typedef uint32_t utime_t;

#define PREDICT_MAXSTEP 10
typedef struct TData_tag{
    data_t values[PREDICT_MAXSTEP];
	uint8_t step;
    utime_t time;
	uint8_t flag;
}TimeValues; //��ʱ���������

typedef struct NCS_Data_tag{	
	uint8_t chn;
	//uint8_t length; // controlPeriod/samplePeriod
	TimeValues buf;	
}NCS_Data_t;
typedef struct NCS_Channel_tag {
	struct NCS_Channel_tag* next;		
	uint8_t mode;
	uint8_t chnNum;
	NCS_Data_t* data;
	//void (*addData)(data_t* ptr, uint8_t length);
}NCS_Channel_t;
typedef struct NCS_Packet_tag {	
	uint8_t step; //prediction step
	NCS_Channel_t* channel;
}NCS_Packet_t;


uint16_t Packet2Bytes(uint8_t* data, const NCS_Packet_t* ppacket);
uint16_t Bytes2Packet(NCS_Packet_t* ppacket, const uint8_t* data, uint16_t cmdLength);
void InitPacket(NCS_Packet_t* ppacket);
void ReleasePacket(NCS_Packet_t* ppacket);
int AddNewData(NCS_Channel_t* chn , const data_t* src, uint8_t srcLength);
NCS_Node* ParseNodes(uint8_t* buf, int32_t length);
void ReleaseNodes(NCS_Node* head);

void SetSystemFrame(const NCS_Frame* frame);
NCS_Frame GetSystemFrame(void);

#elif UDP_VERSION == 5
typedef struct {
	uint16_t head;
	uint16_t len;
	uint16_t antilen;
	uint8_t version;
	uint8_t dir;
	uint8_t datalen;
	uint8_t mode1;
	uint8_t modenum1;
	uint8_t chn1;
	uint16_t value1;
	uint32_t timestamp1;	
	uint8_t mode2;
	uint8_t modenum2;
	uint8_t chn2;
	uint16_t value2;
	uint32_t timestamp2;
	uint16_t checksum;
}ControlFrame;

typedef struct {
	uint16_t head;
	uint16_t len;
	uint16_t antilen;
	uint8_t version;
	uint8_t dir;
	uint8_t datalen;
	uint8_t mode1;
	uint8_t modenum1;
	uint8_t chn1;
	uint16_t value1;
	uint32_t timestamp1;
	uint16_t checksum;
}SampleFrame;

typedef struct {
	uint16_t head;
	uint16_t len;
	uint16_t antilen;
	uint8_t version;
	uint8_t dir;
	uint8_t datalen;
	uint8_t mode1;
	uint8_t modenum1;
	uint8_t chn1;
	uint16_t value1;
	uint16_t checksum;
}SimplySampleFrame;

enum {
	SimplySampleFrameLen = 16,
	SampleFrameLen = 20,
	ControlFrameLen = 29
};


typedef uint32_t utime_t;

void Frame2Byte(uint8_t* buff, int len, void* frame);
void Byte2Frame(void* frame, uint8_t* buff, int len);
uint16_t SimplySampleFrame2ValidData(uint8_t *buff, SimplySampleFrame* sf);
uint16_t SampleFrame2ValidData(uint8_t *buff, SampleFrame* sf);
uint16_t ControlFrame2ValidData(uint8_t *buff, ControlFrame* sf);
//void SendFrame(void(*send)(void* handler, const uint8_t* data, uint16_t length),
	//void* handler, const uint8_t* data, uint16_t length);
void SendFrame(void(*send)(uint8_t* data, int length),
	void* handler, unsigned char* data, int length);

#endif

 
#endif // UDP_VERSION

uint8_t bit_count(uint32_t n);

//void SendFrame(void (*send)(void* handler, const uint8_t* data, uint16_t length),
        //void* handler, const uint8_t* data, uint16_t length );
void SendFrame(void(*send)(uint8_t* data, int length),
	void* handler, unsigned char* data, int length);
#endif // __NCS_PACKET_H
