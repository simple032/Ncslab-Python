/**
 ******************************************************************************
 * @file    	udp_packet.c
 * @author  	ysw
 * @date    	2019-10-14
 * @brief   	v1.0.0: packet and bytes array transform interface
 		   	v1.1.0: 2020-06-09 add frame parse.
 		   	v1.2.0: 2020-08-12 considering control period is not equal to sample period,
 		   			update length 1byte(up to 255)->2bytes(up to 65535)	
			v1.3.0: 2022-05-27 simplify communication interface			   
 		   	
 ******************************************************************************
 */

#include "ncs_packet.h"

static NCS_Frame _systemFrame; 
void SetSystemFrame(const NCS_Frame* pframe)
{	
	memcpy(&_systemFrame, pframe, sizeof(NCS_Frame));
}

NCS_Frame GetSystemFrame(void)
{
	return _systemFrame;
}

uint8_t bit_count(uint32_t n)
{
	uint8_t count;
	for (count = 0; n; n &= n - 1)
	{
		count++;
	}
	return count;
}

uint16_t CRC16_XMODEM(const uint8_t *ptr, uint16_t length)
{
	uint16_t wCRCin = 0x0000;
	uint16_t wCPoly = 0x1021;
	uint8_t wChar = 0;

	while (length--)
	{
		wChar = *(ptr++);
		wCRCin ^= (wChar << 8);

		for (int i = 0; i < 8; i++)
		{
			if (wCRCin & 0x8000)
			{
				wCRCin = (wCRCin << 1) ^ wCPoly;
			}
			else
			{
				wCRCin = wCRCin << 1;
			}
		}
	}
	return (wCRCin);
}

uint8_t MyGetChecksum(const uint8_t* data, uint8_t length)
{
	uint16_t i = 0;
	uint8_t crcResult = 0x00;
	for (i = 0; i < length; i++)
	{
		crcResult += data[i];
	}
	return crcResult;
}

void SendFrame(void(*send)(uint8_t* data, int length),
	void* handler, unsigned char* data, int length)
{
	uint16_t sendLength = 0;
	//uint8_t u8length = (uint8_t)length;
	uint16_t antilength = (uint16_t)(~length);
	uint16_t checksum = 0x0000;
	static uint8_t sendData[1024];
	//frame head
	sendData[sendLength++] = 0xFF;
	sendData[sendLength++] = 0xFF;
	//1byte
	//sendData[sendLength++] = (uint8_t)(u8length);
	//sendData[sendLength++] = (uint8_t)(~u8length);
	//2bytes
	memcpy(sendData + sendLength, &length, sizeof(length));
	sendLength += sizeof(length);
	memcpy(sendData + sendLength, &antilength, sizeof(length));
	sendLength += sizeof(length);
	//version
	sendData[sendLength++] = UDP_VERSION;

	memcpy(sendData + sendLength, data, length);
	sendLength += length;

	//calculate the checksum
	checksum = CRC16_XMODEM(data, length);
	memcpy(sendData + sendLength, &checksum, sizeof(checksum));
	sendLength += sizeof(checksum);

	send(sendData, sendLength);

	uint16_t i = 0;
	printf("Data in SendFrame(%d):", sendLength);
	for(i=0; i<sendLength; i++)
	{
		printf("%02x ", sendData[i]);
	}
	printf("\n");
}


#if UDP_VERSION == 1
void PacketInit(UDP_Packet_t* ppacket)
{
	ppacket->timestamp = 0;
	ppacket->total = 0;
	ppacket->inputWidth = 0;
	ppacket->inputLength = 0;
	ppacket->data = NULL;
}

uint16_t Packet2Bytes(uint8_t* data, const UDP_Packet_t* ppacket)
{
	uint8_t i = 0, j = 0;
	uint16_t idx = 0;
	memcpy(data+idx, &ppacket->timestamp, sizeof(ppacket->timestamp));
	idx += sizeof(ppacket->timestamp);
	
	memcpy(data+idx, &ppacket->inputWidth, sizeof(ppacket->inputWidth));
	idx += sizeof(ppacket->inputWidth);
	memcpy(data+idx, &ppacket->inputLength, sizeof(ppacket->inputLength));
	idx += sizeof(ppacket->inputLength);		
	
	for(i=0; i<ppacket->inputWidth; i++)
	{
		for(j=0; j<ppacket->inputLength; j++)
		{
			memcpy(data+idx, &(ppacket->data[i][j]), sizeof(ppacket->data[i][j]));
			idx += sizeof(ppacket->data[i][j]);
		}
	}
	return idx;
}

uint16_t Bytes2Packet(UDP_Packet_t* ppacket, const uint8_t* data)
{
	uint8_t i = 0 , j=0;
	uint16_t idx = 0;
	memcpy(&ppacket->timestamp, data+idx, sizeof(ppacket->timestamp));
	idx += sizeof(ppacket->timestamp);
	
	memcpy(&ppacket->inputWidth, data+idx, sizeof(ppacket->inputWidth));
	idx += sizeof(ppacket->inputWidth);
	memcpy(&ppacket->inputLength, data+idx, sizeof(ppacket->inputLength));
	idx += sizeof(ppacket->inputLength);		
	
	for(i=0; i<ppacket->inputWidth; i++)
	{
		for(j=0; j<ppacket->inputLength; j++)
		{
			memcpy(&(ppacket->data[i][j]), data+idx, sizeof(ppacket->data[i][j]));
			idx += sizeof(ppacket->data[i][j]);
		}
	}
	return idx;
}

#elif UDP_VERSION == 2


static NCS_UDP_Payload_t* PayloadInit( uint8_t mode, uint8_t channel, uint8_t length)
{
	NCS_UDP_Payload_t* ppayload = NULL;
	if (channel != 0x00)
	{
		//allocate memory for digit
		ppayload = (NCS_UDP_Payload_t*)malloc(sizeof(NCS_UDP_Payload_t));
		if (ppayload == NULL)
		{
			printf("malloc mode=%d payload failure.\n", mode);			
			return NULL;
		}
		ppayload->mode = mode;
		ppayload->channel = channel;
		uint8_t channelbits = bit_count(channel);
		ppayload->buf = (data_t**)malloc(sizeof(data_t*)*channelbits);
		if (ppayload->buf == NULL)
		{
			free(ppayload);
			printf("malloc mode=%d ppayload->buf failure.\n", mode);		
			return NULL;			
		}
		{
			uint8_t i = 0, j = 0;
			for (i = 0; i < bit_count(channel); i++)
			{
				ppayload->buf[i] = (data_t*)malloc(sizeof(data_t)*length);
				if (ppayload->buf[i] == NULL)
				{
					printf("malloc mode=%d ppayload->buf[%d] failure.\n", mode, i);
					for (j = 0; j < i; j++)
					{
						free(ppayload->buf[j]);						
					}
					free(ppayload->buf);
					free(ppayload);
					return NULL;
				}
				memset(ppayload->buf[i], 0x00, sizeof(data_t)*length);
			}
		}
	}
	return ppayload;
}


NCS_UDP_Packet_t* PacketConstructor(uint8_t outputFlag, uint8_t DigitChannel, uint8_t AnalogChannel, uint8_t PWMChannel, uint8_t length)
{
	NCS_UDP_Packet_t* ppacket = (NCS_UDP_Packet_t*)malloc(sizeof(NCS_UDP_Packet_t));
	if (ppacket == NULL)
	{
		printf("malloc Packet failure.\n");
		return NULL;
	}
	ppacket->timestamp = 0;
	//ppacket->inputWidth = bit_count(DigitChannel) + bit_count(AnalogChannel) + bit_count(PWMChannel);
	ppacket->inputLength = length;	
	
	//allocate memory for digit
	ppacket->payload[0] = PayloadInit(0x01 + outputFlag * 0x03, DigitChannel, length);
	
	//allocate memory for analog
	ppacket->payload[1] = PayloadInit(0x02 + outputFlag * 0x03, AnalogChannel, length);

	//allocate memory for pwm
	ppacket->payload[2] = PayloadInit(0x03 + outputFlag * 0x03, PWMChannel, length);
	
	{
		
		if( (DigitChannel != 0x00 && ppacket->payload[0] == NULL)
			|| (AnalogChannel != 0x00 && ppacket->payload[1] == NULL)
			|| (PWMChannel != 0x00 && ppacket->payload[2] == NULL)		)

		{
			PacketDestructor(ppacket);
			ppacket = NULL;
		}		
	}	
	return ppacket;
}


static void PayloadDeInit(NCS_UDP_Payload_t* ppayload)
{
	if (ppayload != NULL)
	{
		uint8_t i = 0;
		for (i = 0; i < bit_count(ppayload->channel); i++)
		{
			free(ppayload->buf[i]);
			ppayload->buf[i] = NULL;
		}
		free(ppayload->buf);
		ppayload->buf = NULL;
		ppayload->mode = 0x00;
		ppayload->channel = 0x00;
		free(ppayload);
	}
}

void PacketDestructor(NCS_UDP_Packet_t* ppacket)
{
	//free memory 
	uint8_t i = 0;
	for (i = 0; i < 3; i++)
	{
		if (ppacket->payload[i] != NULL)
		{
			PayloadDeInit(ppacket->payload[i]);
			ppacket->payload[i] = NULL;
		}
	}
	free(ppacket);
}

//MCU->PC mode:4-6
uint16_t Packet2Bytes(uint8_t* data, const NCS_UDP_Packet_t* ppacket)
{
	uint8_t i = 0, j = 0;
	uint16_t idx = 0;
	
	if(ppacket == NULL)
		return 0;
	
	memcpy(data + idx, &ppacket->timestamp, sizeof(ppacket->timestamp));
	idx += sizeof(ppacket->timestamp);

	//memcpy(data + idx, &ppacket->inputWidth, sizeof(ppacket->inputWidth));
	//idx += sizeof(ppacket->inputWidth);
	memcpy(data + idx, &ppacket->inputLength, sizeof(ppacket->inputLength));
	idx += sizeof(ppacket->inputLength);

	for (i = 0; i<sizeof(ppacket->payload)/sizeof(ppacket->payload[0]); i++)
	{
		if (ppacket->payload[i] != NULL)
		{			
			data[idx++] = ppacket->payload[i]->mode;
			data[idx++] = ppacket->payload[i]->channel;
			for (j = 0; j<bit_count(ppacket->payload[i]->channel); j++)
			{
				memcpy(data + idx, ppacket->payload[i]->buf[j], sizeof(data_t) * ppacket->inputLength);
				idx += sizeof(data_t) * ppacket->inputLength;
			}
		}
	}
	return idx;
}

//PC->MCU mode:1-3
uint16_t Bytes2Packet(NCS_UDP_Packet_t* ppacket, const uint8_t* data, uint16_t cmdLength)
{
	uint8_t i = 0, j = 0;
	uint16_t idx = 0;
	
	if(ppacket == NULL)
		return 0;
	
	memcpy(&ppacket->timestamp, data + idx, sizeof(ppacket->timestamp));
	idx += sizeof(ppacket->timestamp);

	memcpy(&ppacket->inputLength, data + idx, sizeof(ppacket->inputLength));
	idx += sizeof(ppacket->inputLength);

	while (idx < cmdLength)
	{			
		uint8_t mode = data[idx];
		ppacket->payload[mode-1]->mode = data[idx];
		idx++;
		ppacket->payload[mode-1]->channel = data[idx];
		idx++;
		for (j = 0; j<bit_count(ppacket->payload[mode-1]->channel); j++)
		{
			memcpy(ppacket->payload[mode-1]->buf[j], data + idx, sizeof(data_t)*ppacket->inputLength);
			idx += sizeof(data_t)*ppacket->inputLength;
		}
	}
	return idx;
}

/**
	* @brief: 	validate the frame
	* @param:	buf: receive buffer
	* @param: 	length: pointer to the buffer length
	* @retval:	0-SUCCESS others-fail
	*/
uint16_t ValidateNode(uint8_t* buf, uint16_t* pLength, uint8_t getchecksum(const uint8_t *ptr, uint8_t len))
{
	uint16_t result = 4;
	uint8_t frameLen = 1;

	uint16_t offset = 0;

	while (*pLength >= 6)
	{
		while (buf[offset] != 0xFF || buf[offset + 1] != 0xFF)
		{
			offset++;
			if (*pLength - offset < 5)
			{
				break;
			}
		}
		memset(buf, 0x00, offset);
		(*pLength) -= offset;
		memmove(buf, buf + offset, *pLength);
		if (*pLength < 5)
		{
			result = 3;
			printf("Frame head Error.\n");
			break;
		}

		if ((uint8_t)(~buf[2]) != buf[3])
		{
			printf("Frame len Error.\n");
			offset = 1;
			continue;
		}

		memcpy(&frameLen, buf + 2, 1);			
		
		if (buf[4 + frameLen] == getchecksum(buf + 4, frameLen)) //satisfy all requset.
		{
			result = 0;
			*pLength -= frameLen + 5;
			break;
		}
		else
		{
			result = 1;
			memset(buf, 0x00, frameLen + 5);
			*pLength -= (frameLen + 5);
			memmove(buf, buf + frameLen + 5, *pLength);
			printf("checksum Error.\n");
		}		
	}
	return result;
}

NCS_Node* ParseNodes(uint8_t* buf, int32_t length)
{
	NCS_Node* head = (NCS_Node*)malloc(sizeof(NCS_Node));
	NCS_Node *node,*newnode ;
	uint16_t len = (uint16_t)length;
	node = head;
	head->data = NULL;
	head->datalength = 0;
	while (ValidateNode(buf, &len, CRC8) == 0)
	{				
		newnode = (NCS_Node*)malloc(sizeof(NCS_Node));
		memset(newnode, 0x00, sizeof(NCS_Node));
		newnode->datalength = buf[2];
		newnode->data = (uint8_t*)malloc(newnode->datalength);
		memcpy(newnode->data, buf + 4, newnode->datalength);
		newnode->next = NULL;

		node->next = newnode;	
		node = newnode;
	}
	return head;
}

void ReleaseNodes(NCS_Node* head)
{
	NCS_Node* node= head;
	NCS_Node* nextnode = node->next;
	while (node)
	{
		nextnode= node->next;
		if (node->data)
		{
			free(node->data);
			node->data = NULL;
		}
		free(node);		
		node = nextnode;
	}
}

#elif UDP_VERSION == 4

/**
	* @brief: 	validate the frame
	* @param:	buf: receive buffer
	* @param: 	length: pointer to the buffer length
	* @retval:	0-SUCCESS others-fail
	*/
uint16_t ValidateNode(uint8_t* buf, int32_t* pLength, uint16_t getchecksum(const uint8_t *ptr, uint16_t len))
{
	uint16_t result = 4;
	
	uint16_t frameLen = 0;
	uint16_t antiframeLen = 0;
	uint16_t checksum = 0x0000;
	uint8_t version = 0;

	uint16_t idx = 0;	
	
	uint16_t offset = 0;
	while (*pLength > NCS_FRAME_MIN_LEN )
	{
		idx = 0;
		while (buf[offset] != 0xFF || buf[offset + 1] != 0xFF)
		{
			offset++;
			if (*pLength - offset < NCS_FRAME_MIN_LEN)
			{
				break;
			}
		}
		memset(buf, 0x00, offset);
		(*pLength) -= offset;		
		if (*pLength > 0)
			memmove(buf, buf + offset, *pLength);
		// There are 2 situations in here.
		if (*pLength < 	NCS_FRAME_MIN_LEN)
		{
			result = 3;
			printf("Frame head Error.\n");
			break;
		}	

		idx += NCS_FRAME_FUN_HEAD;

		memcpy(&frameLen, buf + idx, NCS_FRAME_FUN_LENGTH);
		idx += NCS_FRAME_FUN_LENGTH;
		
		memcpy(&antiframeLen, buf + idx, NCS_FRAME_FUN_LENGTH);
		idx += NCS_FRAME_FUN_LENGTH;
		if ((uint16_t)(~frameLen) != antiframeLen)
		{
			printf("Frame len Error.\n");
			offset = 1;
			continue;
		}					
		
		memcpy(&version, buf + idx, NCS_FRAME_FUN_VERSION);
		idx++;
		if(version != UDP_VERSION)
		{
			printf("Communication version not match: this: %d - recv:%d", UDP_VERSION, version);
		}

		memcpy(&checksum, buf + idx + frameLen, NCS_FRAME_FUN_CHECKSUM);

		if (checksum == getchecksum(buf + idx, frameLen)) //satisfy all requset.
		{
			result = 0;
			*pLength -= frameLen + NCS_FRAME_MIN_LEN;
			break;
		}
		else
		{
			result = 1;
			memset(buf, 0x00, frameLen + NCS_FRAME_MIN_LEN);
			*pLength -= (frameLen + NCS_FRAME_MIN_LEN);
			if (*pLength > 0)
				memmove(buf, buf + frameLen + NCS_FRAME_MIN_LEN, *pLength);
			printf("checksum Error.\n");
		}		
	}
	return result;
}

NCS_Node* ParseNodes(uint8_t* buf, int32_t length)
{
	NCS_Node *head = (NCS_Node*)malloc(sizeof(NCS_Node));
	NCS_Node *node,*newnode ;
	node = head;
	head->data = NULL;
	head->datalength = 0;
	head->next = NULL;
	while (ValidateNode(buf, &length, CRC16_XMODEM) == 0)
	{				
		newnode = (NCS_Node*)malloc(sizeof(NCS_Node));
		memset(newnode, 0x00, sizeof(NCS_Node));
		
		memcpy(&newnode->datalength, buf+NCS_FRAME_FUN_HEAD, 
			NCS_FRAME_FUN_LENGTH);

		newnode->data = (uint8_t*)malloc(newnode->datalength);
		
		memcpy(newnode->data, buf + NCS_FRAME_FUN_HEAD + 2*NCS_FRAME_FUN_LENGTH + NCS_FRAME_FUN_VERSION, 
			newnode->datalength);
		newnode->next = NULL;

		node->next = newnode;	
		node = newnode;
	}
	return head;
}

void ReleaseNodes(NCS_Node* head)
{
	NCS_Node* node= head;
	NCS_Node* nextnode = node->next;
	while (node)
	{
		nextnode= node->next;
		if (node->data)
		{
			free(node->data);
			node->data = NULL;
		}
		free(node);		
		node = nextnode;
	}
}


#if UDP_VERSION == 4
/**
  * @brief: 	add new data to sequence.
  *	@param:		null
  * @retval:	null
  */
int AddNewData(NCS_UDP_Channel_t* chn , const data_t* src, uint8_t len)
{
	int res = 0;
	if(chn->length < len)
	{
		res = 1;
	}
	else
	{
		//multibytes pointer add : 0x00000002 + 1 = 0x00000004
		memmove( chn->buf[0] + len, chn->buf[0],
			sizeof(data_t) * (chn->length- len));
		memcpy(chn->buf[0], src, sizeof(data_t) * len);	
	}
		 
	return res;
}

static void PayloadDeInit(NCS_UDP_Payload_t** ppPayload)
{
	NCS_UDP_Payload_t* pPayload = *ppPayload;
	if (pPayload)
	{
		uint8_t i = 0;
		uint8_t chn = 0;
		for(chn=0; chn<8; chn++)
		{
			if(pPayload->channels[chn])
			{				
				if(pPayload->channels[chn]->buf)
				{
					for(i=0; i<pPayload->channels[chn]->width; i++)
					{
						if(pPayload->channels[chn]->buf[i])
							free(pPayload->channels[chn]->buf[i]);
					}
					free(pPayload->channels[chn]->buf);
				}
				free(pPayload->channels[chn]);
			}
		}	
		free(pPayload);
	}
}

static NCS_UDP_Payload_t* PayloadInit(const NCS_InitStructure_t* ncs_initstruct)
{
	NCS_UDP_Payload_t* ppayload = NULL;
	int8_t chn = 0;
	int8_t err = 0;
	uint8_t i = 0;
	if (ncs_initstruct->channel != 0x00)
	{
		//allocate payload
		ppayload = (NCS_UDP_Payload_t*)malloc(sizeof(NCS_UDP_Payload_t));
		if (ppayload == NULL)
		{
			printf("malloc mode=%d payload failure.\n", ncs_initstruct->mode);			
			err = 1;
			goto End;
		}
		ppayload->mode = ncs_initstruct->mode;
		for(chn=0; chn<8; chn++)
		{
			//uint8_t channelbits = bit_count(channel);
			ppayload->channels[chn] = NULL;
			if( ncs_initstruct->channel & (0x01<<chn) )
			{
				ppayload->channels[chn] = 
					(NCS_UDP_Channel_t*)malloc(sizeof(NCS_UDP_Channel_t));
				if (!ppayload->channels[chn])
				{					
					printf("malloc mode=%d ppayload->channels[%d] failure.\n", ncs_initstruct->mode, chn);		
					err = 3;
					goto End;	
				}
				{
					ppayload->channels[chn]->width = ncs_initstruct->width;
					ppayload->channels[chn]->length = ncs_initstruct->length;
					ppayload->channels[chn]->buf = 
						(data_t**) malloc( sizeof(data_t*) * ncs_initstruct->width );
					if(!ppayload->channels[chn]->buf)
					{
						printf("malloc mode=%d ppayload->channels[%d]->buf failure.\n", ncs_initstruct->mode, chn);		
						err = 4;
						goto End;
					}					
					for (i = 0; i < ncs_initstruct->width; i++)
					{
						ppayload->channels[chn]->buf[i] = 
							(data_t*) malloc( sizeof(data_t) * ncs_initstruct->length );
						if (ppayload->channels[chn]->buf[i] == NULL)
						{
							printf("malloc mode=%d ppayload->channels[%d]->buf[%d] failure.\n", ncs_initstruct->mode, chn, i);
							err = 5;
							goto End;
						}
						memset(ppayload->channels[chn]->buf[i], 0x00, 
							sizeof(data_t) * ncs_initstruct->length);
					}
				}
			}									
		}		
	}
End:
	if(err)
	{
		PayloadDeInit(&ppayload);
		ppayload = NULL;
	}
	return ppayload;
}

NCS_UDP_Packet_t* PacketConstructor(NCS_Direction dir, const NCS_Frame* pFrame)
{
	NCS_UDP_Packet_t* ppacket = (NCS_UDP_Packet_t*)malloc(sizeof(NCS_UDP_Packet_t));
	if (!ppacket)
	{
		printf("malloc Packet failure, outputFlag=%d.\n", dir);
	}
	else
	{
		uint8_t i = 0;
		ppacket->timestamp = 0;
		//ppacket->inputWidth = bit_count(DigitChannel) + bit_count(AnalogChannel) + bit_count(PWMChannel);					
		
		ppacket->inputLength = (dir == Output)?
			pFrame->communicatePeriod/pFrame->controlPeriod : pFrame->communicatePeriod/pFrame->samplePeriod;

		ppacket->maxStep = pFrame->maxStep;
		
		//allocate memory for digit/analog/pwm
		NCS_InitStructure_t ncs_initstruct;
		for(i=0;i<3;i++)
		{
			ncs_initstruct.mode = i + dir * 3;
			ncs_initstruct.channel = pFrame->iochannels[ncs_initstruct.mode];
			ncs_initstruct.width = ppacket->maxStep;
			ncs_initstruct.length = ppacket->inputLength;
			ppacket->payload[i] = PayloadInit(&ncs_initstruct);
			if( (pFrame->iochannels[ncs_initstruct.mode] != 0x00 
				&& ppacket->payload[i] == NULL) )
			{
				PacketDestructor(ppacket);
				ppacket = NULL;
				break;
			}
		}		
	}	
	return ppacket;
}

void PacketDestructor(NCS_UDP_Packet_t* ppacket)
{
	//free memory 
	uint8_t i = 0;
	for (i = 0; i < 3; i++)
	{
		if (ppacket->payload[i] != NULL)
		{
			PayloadDeInit(&ppacket->payload[i]);
			ppacket->payload[i] = NULL;
		}
	}
	free(ppacket);
}

//MCU->PC mode:4-6
uint16_t Packet2Bytes(uint8_t* data, const NCS_UDP_Packet_t* ppacket)
{
	uint8_t i = 0;
	uint16_t idx = 0;
	uint8_t chn = 0;
	uint8_t payloadNum;
	if(ppacket == NULL)
		return 0;
	
	memcpy(data + idx, &ppacket->timestamp, sizeof(ppacket->timestamp));
	idx += sizeof(ppacket->timestamp);

	//memcpy(data + idx, &ppacket->inputWidth, sizeof(ppacket->inputWidth));
	//idx += sizeof(ppacket->inputWidth);
	memcpy(data + idx, &ppacket->inputLength, sizeof(ppacket->inputLength));
	idx += sizeof(ppacket->inputLength);

	for (payloadNum = 0; payloadNum<sizeof(ppacket->payload)/sizeof(ppacket->payload[0]); payloadNum++)
	{
		if (ppacket->payload[payloadNum] != NULL)
		{			
			data[idx++] = ppacket->payload[payloadNum]->mode;
			for(chn=0 ; chn<8; chn++)
			{
				if(ppacket->payload[payloadNum]->channels[chn])
				{					
					for(i=0; i<ppacket->payload[payloadNum]->channels[chn]->width; i++)
					{						
						memcpy(data + idx, ppacket->payload[payloadNum]->channels[chn]->buf[i], 
							sizeof(data_t) * ppacket->inputLength);
						idx += sizeof(data_t) * ppacket->inputLength;						
					}					
				}
			}	
		}
	}
	return idx;
}

//PC->MCU mode:1-3
uint16_t Bytes2Packet(NCS_UDP_Packet_t* ppacket, const uint8_t* data, uint16_t cmdLength)
{
	uint8_t i = 0;
	uint16_t idx = 0;
	uint8_t chn = 0;
	if(ppacket == NULL)
		return 0;
	
	memcpy(&ppacket->timestamp, data + idx, sizeof(ppacket->timestamp));
	idx += sizeof(ppacket->timestamp);

	memcpy(&ppacket->inputLength, data + idx, sizeof(ppacket->inputLength));
	idx += sizeof(ppacket->inputLength);

	while (idx < cmdLength)
	{			
		uint8_t mode = data[idx]%3;
		ppacket->payload[mode]->mode = data[idx++];
		
		for(chn=0 ; chn<8; chn++)
		{			
			if(ppacket->payload[mode]->channels[chn])
			{				
				for(i=0; i<ppacket->payload[mode]->channels[chn]->width; i++)
				{						
					memcpy(ppacket->payload[mode]->channels[chn]->buf[i], data + idx, 
						sizeof(data_t) * ppacket->inputLength);
					idx += sizeof(data_t) * ppacket->inputLength;						
				}					
			}
		}	
	}
	return idx;
}

#elif UDP_VERSION == 5


uint16_t Packet2Bytes(uint8_t* data, const NCS_Packet_t* ppacket)
{
	uint16_t idx = 0;
	uint8_t i = 0;

	//memcpy(data + idx, &ppacket->inputWidth, sizeof(ppacket->inputWidth));
	//idx += sizeof(ppacket->inputWidth);
	memcpy(data + idx, &ppacket->step, sizeof(ppacket->step));
	idx += sizeof(ppacket->step);

	NCS_Channel_t* pchn = ppacket->channel;

	while(pchn->next)
	{		
		pchn = pchn->next;

		memcpy(data+idx, &pchn->mode, sizeof(pchn->mode));
		idx += sizeof(pchn->mode);

		memcpy(data+idx, &pchn->chnNum, sizeof(pchn->chnNum));
		idx += sizeof(pchn->chnNum);

		NCS_Data_t* pdata = pchn->data;

		for(i=0; i<pchn->chnNum; i++)
		{
			memcpy(data+idx, &pdata[i].chn, sizeof(pdata[i].chn));
			idx += sizeof(pdata[i].chn);

			memcpy(data+idx, pdata[i].buf.values, ppacket->step*sizeof(data_t));
			idx += ppacket->step*sizeof(data_t);

			memcpy(data+idx, &pdata[i].buf.time, sizeof(pdata[i].buf.time));
			idx += sizeof(pdata[i].buf.time);
		}
	}
	return idx;
}

uint16_t Bytes2Packet(NCS_Packet_t* ppacket, const uint8_t* data, uint16_t cmdLength)
{
	uint16_t idx = 0;
	uint8_t i = 0;

	//memcpy(data + idx, &ppacket->inputWidth, sizeof(ppacket->inputWidth));
	//idx += sizeof(ppacket->inputWidth);
	memcpy(&ppacket->step, data + idx, sizeof(ppacket->step));
	idx += sizeof(ppacket->step);

	NCS_Channel_t* pchn = malloc(sizeof(NCS_Channel_t));
	pchn->next = NULL;
	pchn->mode = 0xFF;
	pchn->data = NULL;

	ppacket->channel = pchn;

	while(idx < cmdLength)
	{		
		NCS_Channel_t* newchn = (NCS_Channel_t*)malloc(sizeof(NCS_Channel_t));
		newchn->next = NULL;
		newchn->mode = 0xFF;
		newchn->data = NULL;
		newchn->chnNum = 0;

		pchn->next = newchn;
		pchn = pchn->next;

		memcpy(&pchn->mode, data+idx, sizeof(pchn->mode));
		idx += sizeof(pchn->mode);

		memcpy(&pchn->chnNum, data+idx, sizeof(pchn->chnNum));
		idx += sizeof(pchn->chnNum);
		
		pchn->data = (NCS_Data_t*)malloc(sizeof(NCS_Data_t)*pchn->chnNum);;
		NCS_Data_t* pdata = pchn->data;
		
		for(i=0; i<pchn->chnNum; i++)
		{
			memcpy(&(pdata[i].chn), data+idx, sizeof(pdata[i].chn));
			idx += sizeof(pdata[i].chn);

			memcpy(pdata[i].buf.values, data+idx, ppacket->step*sizeof(data_t));
			idx += ppacket->step*sizeof(data_t);

			memcpy(&(pdata[i].buf.time), data+idx, sizeof(pdata[i].buf.time));
			idx += sizeof(pdata[i].buf.time);
		}				
	}
	return idx;
}

void InitPacket(NCS_Packet_t* ppacket)
{
	if(!ppacket) return;
	ppacket->channel = NULL;
	ppacket->step = 0;
}

void ReleasePacket(NCS_Packet_t* ppacket)
{
	NCS_Channel_t* pchn = ppacket->channel;
	NCS_Channel_t* nextchn = NULL;
	if(!ppacket) return;
	while(pchn)
	{
		nextchn = pchn->next;
		if(pchn->data)
		{
			free(pchn->data);
		}
		free(pchn);
		pchn = nextchn;		
	}
	ppacket->channel = NULL;
	ppacket->step = 0;
}

int AddNewData(NCS_Channel_t* chn , const data_t* src, uint8_t srcLength)
{
	return 0x01;
}

#endif 

#elif UDP_VERSION == 5

void Byte2Frame(void* frame, uint8_t* buff, int len)
{
	int idx = 0;
	switch (len){
		case SimplySampleFrameLen: {
			SimplySampleFrame* sf = (SimplySampleFrame*)frame;
			memcpy(&sf->head, buff + idx, 2); idx += 2;
			memcpy(&sf->len, buff + idx, 2); idx += 2;
			memcpy(&sf->antilen, buff + idx, 2); idx += 2;
			memcpy(&sf->version, buff + idx, 1); idx += 1;
			memcpy(&sf->dir, buff + idx, 1); idx += 1;
			memcpy(&sf->datalen, buff + idx, 1); idx += 1;
			memcpy(&sf->mode1, buff + idx, 1); idx += 1;
			memcpy(&sf->modenum1, buff + idx, 1); idx += 1;
			memcpy(&sf->chn1, buff + idx, 1); idx += 1;
			memcpy(&sf->value1, buff + idx, 2); idx += 2;
			memcpy(&sf->checksum, buff + idx, 2); idx += 2;
		}break;

		case SampleFrameLen: {
			SampleFrame* sf = (SampleFrame*)frame;
			memcpy(&sf->head, buff + idx, 2); idx += 2;
			memcpy(&sf->len, buff + idx, 2); idx += 2;
			memcpy(&sf->antilen, buff + idx, 2); idx += 2;
			memcpy(&sf->version, buff + idx, 1); idx += 1;
			memcpy(&sf->dir, buff + idx, 1); idx += 1;
			memcpy(&sf->datalen, buff + idx, 1); idx += 1;
			memcpy(&sf->mode1, buff + idx, 1); idx += 1;
			memcpy(&sf->modenum1, buff + idx, 1); idx += 1;
			memcpy(&sf->chn1, buff + idx, 1); idx += 1;
			memcpy(&sf->value1, buff + idx, 2); idx += 2;
			memcpy(&sf->timestamp1, buff + idx, 4); idx += 4;
			memcpy(&sf->checksum, buff + idx, 2); idx += 2;
		}break;
		case ControlFrameLen: {
			ControlFrame* sf = (ControlFrame*)frame;
			memcpy(&sf->head, buff + idx, 2); idx += 2;
			memcpy(&sf->len, buff + idx, 2); idx += 2;
			memcpy(&sf->antilen, buff + idx, 2); idx += 2;
			memcpy(&sf->version, buff + idx, 1); idx += 1;
			memcpy(&sf->dir, buff + idx, 1); idx += 1;
			memcpy(&sf->datalen, buff + idx, 1); idx += 1;
			memcpy(&sf->mode1, buff + idx, 1); idx += 1;
			memcpy(&sf->modenum1, buff + idx, 1); idx += 1;
			memcpy(&sf->chn1, buff + idx, 1); idx += 1;
			memcpy(&sf->value1, buff + idx, 2); idx += 2;
			memcpy(&sf->timestamp1, buff + idx, 4); idx += 4;
			memcpy(&sf->mode2, buff + idx, 1); idx += 1;
			memcpy(&sf->modenum2, buff + idx, 1); idx += 1;
			memcpy(&sf->chn2, buff + idx, 1); idx += 1;
			memcpy(&sf->value2, buff + idx, 2); idx += 2;
			memcpy(&sf->timestamp2, buff + idx, 4); idx += 4;
			memcpy(&sf->checksum, buff + idx, 2); idx += 2;
		}break;
		default:
			printf("The len=%d is unregconized\n", len);
			break;
	}
}

void Frame2Byte(uint8_t* buff, int len, void* frame)
{
	int idx = 0;
	switch (len) {
		case SampleFrameLen: {
			SampleFrame* sf = (SampleFrame*)frame;
			memcpy(buff + idx, &sf->head, 2); idx += 2;
			memcpy(buff + idx, &sf->len, 2); idx += 2;
			memcpy(buff + idx, &sf->antilen, 2); idx += 2;
			memcpy(buff + idx, &sf->version, 1); idx += 1;
			memcpy(buff + idx, &sf->dir, 1); idx += 1;
			memcpy(buff + idx, &sf->datalen, 1); idx += 1;
			memcpy(buff + idx, &sf->mode1, 1); idx += 1;
			memcpy(buff + idx, &sf->modenum1, 1); idx += 1;
			memcpy(buff + idx, &sf->chn1, 1); idx += 1;
			memcpy(buff + idx, &sf->value1, 2); idx += 2;
			memcpy(buff + idx, &sf->timestamp1, 4); idx += 4;
			memcpy(buff + idx, &sf->checksum, 2); idx += 2;
		}break;
		case ControlFrameLen: {
			ControlFrame* sf = (ControlFrame*)frame;
			memcpy(buff + idx, &sf->head, 2); idx += 2;
			memcpy(buff + idx, &sf->len, 2); idx += 2;
			memcpy(buff + idx, &sf->antilen, 2); idx += 2;
			memcpy(buff + idx, &sf->version, 1); idx += 1;
			memcpy(buff + idx, &sf->dir, 1); idx += 1;
			memcpy(buff + idx, &sf->datalen, 1); idx += 1;
			memcpy(buff + idx, &sf->mode1, 1); idx += 1;
			memcpy(buff + idx, &sf->modenum1, 1); idx += 1;
			memcpy(buff + idx, &sf->chn1, 1); idx += 1;
			memcpy(buff + idx, &sf->value1, 2); idx += 2;
			memcpy(buff + idx, &sf->timestamp1, 4); idx += 4;
			memcpy(buff + idx, &sf->mode2, 1); idx += 1;
			memcpy(buff + idx, &sf->modenum2, 1); idx += 1;
			memcpy(buff + idx, &sf->chn2, 1); idx += 1;
			memcpy(buff + idx, &sf->value2, 2); idx += 2;
			memcpy(buff + idx, &sf->timestamp2, 4); idx += 4;
			memcpy(buff + idx, &sf->checksum, 2); idx += 2;
		}break;
		default:
			printf("The len=%d is unregconized\n", len);
			break;
	}
}

uint16_t SimplySampleFrame2ValidData(uint8_t *buff, SimplySampleFrame* sf)
{
	int idx = 0;
	memcpy(buff + idx, &sf->dir, 1); idx += 1;
	memcpy(buff + idx, &sf->datalen, 1); idx += 1;
	memcpy(buff + idx, &sf->mode1, 1); idx += 1;
	memcpy(buff + idx, &sf->modenum1, 1); idx += 1;
	memcpy(buff + idx, &sf->chn1, 1); idx += 1;
	memcpy(buff + idx, &sf->value1, 2); idx += 2;
	return idx;
}

uint16_t SampleFrame2ValidData(uint8_t *buff, SampleFrame* sf)
{
	int idx = 0;
	memcpy(buff + idx, &sf->dir, 1); idx += 1;
	memcpy(buff + idx, &sf->datalen, 1); idx += 1;
	memcpy(buff + idx, &sf->mode1, 1); idx += 1;
	memcpy(buff + idx, &sf->modenum1, 1); idx += 1;
	memcpy(buff + idx, &sf->chn1, 1); idx += 1;
	memcpy(buff + idx, &sf->value1, 2); idx += 2;
	memcpy(buff + idx, &sf->timestamp1, 4); idx += 4;
	return idx;
}

uint16_t ControlFrame2ValidData(uint8_t *buff, ControlFrame* sf)
{
	int idx = 0;
	memcpy(buff + idx, &sf->dir, 1); idx += 1;
	memcpy(buff + idx, &sf->datalen, 1); idx += 1;
	memcpy(buff + idx, &sf->mode1, 1); idx += 1;
	memcpy(buff + idx, &sf->modenum1, 1); idx += 1;
	memcpy(buff + idx, &sf->chn1, 1); idx += 1;
	memcpy(buff + idx, &sf->value1, 2); idx += 2;
	memcpy(buff + idx, &sf->timestamp1, 4); idx += 4;
	memcpy(buff + idx, &sf->mode2, 1); idx += 1;
	memcpy(buff + idx, &sf->modenum2, 1); idx += 1;
	memcpy(buff + idx, &sf->chn2, 1); idx += 1;
	memcpy(buff + idx, &sf->value2, 2); idx += 2;
	memcpy(buff + idx, &sf->timestamp2, 4); idx += 4;
	return idx;
}




#else
#error "UDP Version mismatch."

#endif // UDP_VERSION
