#include"hardware.h"


void initHardware(){

	//printf("Init hardware\n");
	wiringPiSetup();
}

#define ADDO 2
#define ADSK 3

#define FIFOLENGTH 10

static WATER_LEVEL *deviceGlobal;

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