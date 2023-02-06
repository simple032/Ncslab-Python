#include"hardware.h"
#include"malloc.h"
#include"ncs_packet.h"
#include"ncs_udp.h"
void initHardware(){

	//printf("Init hardware\n");
	wiringPiSetup();
}

#define ADDO 2
#define ADSK 3
#define Trig 27
#define Echo 22
#define PIN_FAN 6
#define FIFOLENGTH 10

float height[7]={0,0,0,0,0,0,0};
float speed[5]={0,0,0,0,0};
static ControlFrame SystemControlFrame;
static SampleFrame SystemSampleFrame;  

static WATER_LEVEL *deviceGlobal;
static ALP *deviceGlobalAlp;
static FAN *deviceGlobalFan;

static unsigned long readCount(void)
{
  unsigned long Count;
  unsigned char i;

  digitalWrite(ADSK,0);
  Count=0;
  while(digitalRead(ADDO)){
    usleep(10000);
  };
  
  for (i=0;i<24;i++){
    digitalWrite(ADSK,1);
    Count=Count<<1;
    digitalWrite(ADSK,0);
    if(digitalRead(ADDO)) Count++;
  }
  digitalWrite(ADSK,1);
  Count=Count^0x800000;
  digitalWrite(ADSK,0);
  return(Count);
}

static unsigned long findMiddle(unsigned long *fifo)
{
  unsigned long data[FIFOLENGTH];
  for(int i=0;i<FIFOLENGTH;i++)
  {
    data[i]=fifo[i];
  }

  for(int i=0;i<FIFOLENGTH-1;i++)
  {
    for(int j=0;j<FIFOLENGTH-i-1;j++)
    {
      if(data[j]>data[j+1])
      {
         unsigned long temp=data[j];
         data[j]=data[j+1];
         data[j+1]=temp; 
      }
    }
  }

  return data[FIFOLENGTH/2];
}

long readOffset(){
    FILE *fp;
    long offset=0;
    if((fp=fopen("pumpoffest","r+"))==NULL){
        printf("Can't open file pumpoffest\n");
        exit(1);
    }
    
    
    //rewind(fp);
    int n=fscanf(fp,"%ld",&offset);
    
    printf("Offset=%ld\n",offset);
    
    fclose(fp);
    return offset;
}

void* WaterLevelThreadFunction(void *arg)
{
  unsigned long fifo[FIFOLENGTH]={0};
  double y;
  double prev=0;
  long offset;
  
  offset=readOffset();
  
  printf("Water Level Thread started...Ok\n");
  while(TRUE)
  {
    unsigned long c=readCount();

    //printf("%d\n",c);
    
    for(int i=1;i<FIFOLENGTH;i++){
      fifo[i-1]=fifo[i];
    }
    fifo[FIFOLENGTH-1]=c;
    
    deviceGlobal->level_count=findMiddle(fifo);
   
    y=(deviceGlobal->level_count-offset)/62800.0*30;
    //y=(count-offset)/62800.0*30;
    
    if(y>250)
    {
      y=prev;
    }
    else
    if(y<0)
    {
      y=prev;
    }

    //prev=y;
    
     
    if(y-prev>3)
    {
      y=prev=prev+3;
    }
    else
    if(prev-y>3)
    {
      y=prev=prev-3;
    }  
    else{
     prev=y;
    }
     

    pthread_mutex_lock(&(deviceGlobal->level_timerCritical));
    deviceGlobal->level_out=y;
    pthread_mutex_unlock(&(deviceGlobal->level_timerCritical));
    //printf("%f\n",y);
  }
}

void startWaterLevelThread()
{
    
    printf("Water Level Thread starting...\n");

    pthread_t id;
    int ret;
    ret=pthread_create(&id,NULL,WaterLevelThreadFunction,NULL);


    if(ret!=0)
    {
        printf ("Create pthread error!\n");
        exit (1);
    }

}

static void waterlevel_speed_edgeDetect()
{
  
  pthread_mutex_lock(&(deviceGlobal->speed_timerCritical));

  deviceGlobal->speed_counter++;	
  pthread_mutex_unlock(&(deviceGlobal->speed_timerCritical));
}



static void waterlevel_speed_timer_in_callback(union sigval v){
	unsigned int now,diff;
  	pthread_mutex_lock(&(deviceGlobal->speed_timerCritical));
  	deviceGlobal->speed_counter_out=deviceGlobal->speed_counter;
  	deviceGlobal->speed_counter=0;
  	pthread_mutex_unlock(&(deviceGlobal->speed_timerCritical));
}

static void startWaterLevelTimer(WATER_LEVEL *device){
	struct sigevent evp; 
    memset(&evp, 0, sizeof(evp));
    evp.sigev_value.sival_ptr = NULL; //这里传一个参数进去，在timer的callback回调函数里面可以获得它  
    evp.sigev_notify = SIGEV_THREAD; //定时器到期后内核创建一个线程执行sigev_notify_function函数 
    evp.sigev_notify_function = waterlevel_speed_timer_in_callback; //这个就是指定回调函数

    int ret = 0;
    ret = timer_create(CLOCK_REALTIME, &evp, &(device->speed_main_timer));
    if(ret < 0)
    {
        printf("timer_create() fail, ret:%d", ret);
        exit(0);
    }
    pthread_mutex_init(&(device->speed_timerCritical),NULL);
    struct itimerspec ts;
    ts.it_interval.tv_sec = 0;
    ts.it_interval.tv_nsec = 100000000;
    ts.it_value.tv_sec = ts.it_interval.tv_sec;
    ts.it_value.tv_nsec = ts.it_interval.tv_nsec; 
    ret = timer_settime(device->speed_main_timer, TIMER_ABSTIME, &ts, NULL);
    if(ret < 0){
        printf("main_timer() fail, ret:%d", ret); 
        timer_delete(device->speed_main_timer);
        exit(1);
    } 
}

void initWaterLevel(WATER_LEVEL *device){
    pinMode(1, PWM_OUTPUT);
    pwmSetMode (PWM_MODE_MS) ;	
    pwmSetClock(3);
    pwmSetRange(1000);
    
    pinMode(0, INPUT);
    pullUpDnControl (0,PUD_UP);
    deviceGlobal=device;
    wiringPiISR(0, INT_EDGE_FALLING, waterlevel_speed_edgeDetect);
    startWaterLevelTimer(device);
    
    pthread_mutex_init(&(device->level_timerCritical),NULL);
    pinMode(ADDO, INPUT);
    pinMode(ADSK, OUTPUT);
    startWaterLevelThread();
}



void outputWaterLevel(WATER_LEVEL *device){
	pwmWrite(1,(1-device->pumpPWM)*1000);
	
	pthread_mutex_lock(&(device->speed_timerCritical));
    device->speed_counter_in=100.0*device->speed_counter_out;
    pthread_mutex_unlock(&(device->speed_timerCritical));
    
    pthread_mutex_lock(&(device->level_timerCritical));
    device->level=device->level_out;
    pthread_mutex_unlock(&(device->level_timerCritical));
}

//气浮装置处理函数
static void alp_fanspeed_timer_in_callback(union sigval v){
	unsigned int now,diff;
  	pthread_mutex_lock(&(deviceGlobalAlp->fanspeed_timerCritical));
  	deviceGlobalAlp->fanspeed_counter_out=deviceGlobalAlp->fanspeed_counter;
  	deviceGlobalAlp->fanspeed_counter=0;
  	pthread_mutex_unlock(&(deviceGlobalAlp->fanspeed_timerCritical));
}

static void startAlpTimer(ALP *device){
	struct sigevent evp; 
    memset(&evp, 0, sizeof(evp));
    evp.sigev_value.sival_ptr = NULL; //这里传一个参数进去，在timer的callback回调函数里面可以获得它  
    evp.sigev_notify = SIGEV_THREAD; //定时器到期后内核创建一个线程执行sigev_notify_function函数 
    evp.sigev_notify_function = alp_fanspeed_timer_in_callback; //这个就是指定回调函数

    int ret = 0;
    ret = timer_create(CLOCK_REALTIME, &evp, &(device->fanspeed_main_timer));
    if(ret < 0)
    {
        printf("timer_create() fail, ret:%d", ret);
        exit(0);
    }
    pthread_mutex_init(&(device->fanspeed_timerCritical),NULL);
    struct itimerspec ts;
    ts.it_interval.tv_sec = 0;
    ts.it_interval.tv_nsec = 100000000;
    ts.it_value.tv_sec = ts.it_interval.tv_sec;
    ts.it_value.tv_nsec = ts.it_interval.tv_nsec; 
    ret = timer_settime(device->fanspeed_main_timer, TIMER_ABSTIME, &ts, NULL);
    if(ret < 0){
        printf("main_timer() fail, ret:%d", ret); 
        timer_delete(device->fanspeed_main_timer);
        exit(1);
    } 
}
static void alp_fanspeed_edgeDetect()
{
  pthread_mutex_lock(&(deviceGlobalAlp->fanspeed_timerCritical));
  deviceGlobalAlp->fanspeed_counter++;	
  pthread_mutex_unlock(&(deviceGlobalAlp->fanspeed_timerCritical));
}

float disMeasure()
{
    struct timeval tv1;  //timeval是time.h中的预定义结构体 其中包含两个一个是秒，一个是微秒
    /*
    struct timeval
    {
        time_t tv_sec;  //Seconds.
        suseconds_t tv_usec;  //Microseconds.
    };
    */
    struct timeval tv2;
    long start, stop;
    float dis;
    int e; 
    delayMicroseconds(100);
//     printf("------triggering...-------\n");
    digitalWrite(Trig, 0);
    delayMicroseconds(5);
    digitalWrite(Trig, 1);
    delayMicroseconds(15);      //发出超声波脉冲
    digitalWrite(Trig, 0);
//     printf("------waiting for echo------\n");
    while(!(digitalRead(Echo) == 1));//满足条件会一直阻塞
    gettimeofday(&tv1, NULL);           //获取当前时间 开始接收到返回信号的时候
    while(!(digitalRead(Echo) == 0));
    gettimeofday(&tv2, NULL);           //获取当前时间  最后接收到返回信号的时候
//     printf("------echo recieved------\n");
    
    /*
    int gettimeofday(struct timeval *tv, struct timezone *tz);
    The functions gettimeofday() and settimeofday() can get and set the time as well as a timezone.
    The use of the timezone structure is obsolete; the tz argument should normally be specified as NULL.
    */
    start = tv1.tv_sec * 1000000 + tv1.tv_usec;   //微秒级的时间
    stop  = tv2.tv_sec * 1000000 + tv2.tv_usec;
    dis = (float)(stop - start) / 1000000 * 34000 / 2;  //计算时间差求出距离cm   
//     printf("------dis calculated:%f------\n",dis);
    return dis;
}

void initAlp(ALP *device){
   //PWMout
   if(wiringPiSetupGpio() < 0 )
    {
        printf("wiringPiSetupGpio failed in PWMOut\n");
        exit(1);
    }
    pinMode(13, PWM_OUTPUT);
    pwmSetMode (PWM_MODE_MS) ;	
    pwmSetClock(3);
    pwmSetRange(1000);
   //FanSpeed
    pinMode(PIN_FAN, INPUT);
    pullUpDnControl(PIN_FAN,PUD_UP);
    deviceGlobalAlp=device;
    wiringPiISR(PIN_FAN, INT_EDGE_FALLING, alp_fanspeed_edgeDetect);
    startAlpTimer(device);
    pinMode(Echo, INPUT);  //设置端口为输入
    pullUpDnControl (Echo,PUD_UP);
    pinMode(Trig, OUTPUT);
}

void outputAlp(ALP *device){
    pwmWrite(13,(int)(device->alpPWM*1000));
    //Fanspeed
    int i;
    float v;
    pthread_mutex_lock(&(device->fanspeed_timerCritical));
    device->fanspeed_counter_in=device->fanspeed_counter_out;
    pthread_mutex_unlock(&(device->fanspeed_timerCritical));
    for(i=0;i<4;i++){
       speed[i]=speed[i+1];}
    v=device->fanspeed_counter_in*100.0;
    for(i=0;i<4;i++){
        v=v+speed[i];}
    speed[4]=v/5;
    device->fanspeed_output=speed[4];
    //Getposition
    float h;
    int j;
    for(j=0;j<6;j++){
        height[j]=height[j+1];}
    h=disMeasure();
    for(j=0;j<6;j++){
        h=h+height[j];}
    height[6]=h/7;
    device->position=(real_T)(height[6]);
}

//风扇初始化和输出函数
void initFan(FAN *device,int copyNum,int local_port,int remote_port,char *remote_addr,char *local_addr){
     char* strRemote=(char*)malloc(sizeof(remote_addr));
     strRemote=remote_addr;
     char* strLocal=(char*)malloc(sizeof(local_addr));
     strLocal=local_addr;
     int portRemote=remote_port;
     int portLocal=local_port;
     int nNetTimeout=5;
    {
     SOCKET socketLocal=UDP_OpenClient(strRemote,portRemote,nNetTimeout);
     free(strRemote);
     free(strLocal);
    }
    SystemControlFrame.dir=0x90;
    SystemControlFrame.mode1=PWMO_IDX;
    SystemControlFrame.modenum1=0x01;
    switch(copyNum)
   {
    case 0:SystemControlFrame.chn1=0;
    break;
    case 1:SystemControlFrame.chn1=1;
    break;
    case 2:SystemControlFrame.chn1=2;
    break;
    case 3:SystemControlFrame.chn1=3;
    break;
   }
    SystemControlFrame.mode2=PWMI_IDX;
    SystemControlFrame.modenum2=0x01;
    switch(copyNum)
   {
    case 0:SystemControlFrame.chn2=0;
    break;
    case 1:SystemControlFrame.chn2=1;
    break;
    case 2:SystemControlFrame.chn2=2;
    break;
    case 3:SystemControlFrame.chn2=3;
    break;
   }
  //deviceGlobalFan=device;
}

void outputFan(FAN *device){
    static uint8_t sendData[1024];
    SystemControlFrame.value1=device->fanCMD;
    uint16_t sendLength=ControlFrame2ValidData(sendData,&SystemControlFrame);
    SendFrame(UDP_Send,device,sendData,sendLength);
    utime_t currT=SystemControlFrame.timestamp1;
    static uint8_t recvBuff[1024];
    //int recvLen=UDP_Recv(recvBuff,sizeof(recvBuff));
    int recvLen=UDP_Recv(recvBuff,1024);
    if(recvLen>0){
    Byte2Frame(&SystemSampleFrame,recvBuff,recvLen);
    device->fan_time=SystemSampleFrame.timestamp1;
    device->speed_rpm=SystemSampleFrame.value1;
    SystemControlFrame.value2=SystemSampleFrame.value1;
    SystemControlFrame.timestamp1=SystemSampleFrame.timestamp1;
    SystemControlFrame.timestamp2=SystemSampleFrame.timestamp1;
   }
}
