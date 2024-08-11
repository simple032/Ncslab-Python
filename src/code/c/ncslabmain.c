#include"ncslabccode.h"
#include"ServerThread.h"
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <sys/timerfd.h>
#include <sys/epoll.h>
#include <stdint.h>  // for uint64_t
#include <errno.h>
#include"ncslab.h"

MODEL *mp;

ExtModeData extModeData;

timer_t main_timer;

unsigned char calcSum(unsigned char bytes[])
{
    int i = 0;  
    unsigned char res = 0x00;
    for(i=0; i<6; i++){
 	res += bytes[i];
    }
    return res;
}

/*static*/void timer_in_callback(union sigval v)
{ 

    pthread_mutex_lock(&(extModeData.timerCritical));
    setAllClientUploadEvents();
    NCSLabOneStep();
    mp->time+=mp->stepSize;
    //NCSLabOneStep();
    
	gettimeofday(&(mp->tv), NULL);
	//printf("%f\n",mp->time);
	
    pthread_mutex_unlock(&(extModeData.timerCritical));
    
}

// 定时器事件处理函数
void timer_event_handler(int timer_fd) {
    uint64_t exp;
    ssize_t s = read(timer_fd, &exp, sizeof(uint64_t));
    if (s != sizeof(uint64_t)) {
        perror("read");
        exit(EXIT_FAILURE);
    }
    // printf("Timer triggered! Expirations: %llu\n", (unsigned long long) exp);
    pthread_mutex_lock(&(extModeData.timerCritical));
    setAllClientUploadEvents();
    NCSLabOneStep();
    mp->time+=mp->stepSize;
    //NCSLabOneStep();
    
	gettimeofday(&(mp->tv), NULL);
	// printf("current time: %f\n",mp->time);
	
    pthread_mutex_unlock(&(extModeData.timerCritical));
    
}

// 初始化定时器
int init_timer(double initial_interval, double repeat_interval) {
    int timer_fd = timerfd_create(CLOCK_REALTIME, 0);
    if (timer_fd == -1) {
        perror("timerfd_create");
        exit(EXIT_FAILURE);
    }

    struct itimerspec new_value;
    new_value.it_value.tv_sec = (time_t)initial_interval;
    new_value.it_value.tv_nsec = (initial_interval - (time_t)initial_interval) * 1e9;
    new_value.it_interval.tv_sec = (time_t)repeat_interval;
    new_value.it_interval.tv_nsec = (repeat_interval - (time_t)repeat_interval) * 1e9;

    if (timerfd_settime(timer_fd, 0, &new_value, NULL) == -1) {
        perror("timerfd_settime");
        exit(EXIT_FAILURE);
    }

    return timer_fd;
}


// epoll事件处理线程函数
void* epoll_thread_func(void* arg) {
    int epoll_fd = *(int*)arg;
    struct epoll_event events[10];
    int nfds;

    printf("Epoll thread started. Waiting for events...\n");
    while (1) {
        nfds = epoll_wait(epoll_fd, events, 10, -1);
        if (nfds == -1) {
            if (errno == EINTR) {
                continue;  // 如果epoll_wait因信号中断，则继续等待
            }
            perror("epoll_wait");
            exit(EXIT_FAILURE);
        }

        for (int n = 0; n < nfds; ++n) {
            if (events[n].data.fd != -1) {
                timer_event_handler(events[n].data.fd);
            }
        }
    }

    return NULL;
}

// 启动事件驱动定时器
void start_event_driven_timer(double initial_interval) {
    int epoll_fd;
    struct epoll_event ev;
    pthread_t epoll_thread;

    pthread_mutex_init(&(extModeData.timerCritical),NULL);

    // 创建epoll实例
    epoll_fd = epoll_create1(0);
    if (epoll_fd == -1) {
        perror("epoll_create1");
        exit(EXIT_FAILURE);
    }

    // 初始化定时器
    int timer_fd = init_timer(initial_interval, initial_interval);

    // 添加定时器文件描述符到epoll实例中
    ev.events = EPOLLIN;
    ev.data.fd = timer_fd;
    if (epoll_ctl(epoll_fd, EPOLL_CTL_ADD, timer_fd, &ev) == -1) {
        perror("epoll_ctl");
        exit(EXIT_FAILURE);
    }

    // 创建epoll事件处理线程
    if (pthread_create(&epoll_thread, NULL, epoll_thread_func, &epoll_fd) != 0) {
        perror("pthread_create");
        exit(EXIT_FAILURE);
    }



    // // 清理资源
    // close(timer_fd);
    // close(epoll_fd);
    pthread_join(epoll_thread, NULL);
}

// void startTimer(real_T stepSize)
// {
//     struct sigevent evp; 

//     memset(&evp, 0, sizeof(evp));

//     evp.sigev_value.sival_ptr = NULL; //这里传一个参数进去，在timer的callback回调函数里面可以获得它  

//     evp.sigev_notify = SIGEV_THREAD; //定时器到期后内核创建一个线程执行sigev_notify_function函数 

//     evp.sigev_notify_function = timer_in_callback; //这个就是指定回调函数

 

//     int ret = 0;

//     ret = timer_create(CLOCK_REALTIME, &evp, &main_timer);

//     if(ret < 0)

//     {

//         printf("timer_create() fail, ret:%d", ret);

//         exit(0);


//     }

//     pthread_mutex_init(&(extModeData.timerCritical),NULL);


//      struct itimerspec ts;

//     ts.it_interval.tv_sec = (int)stepSize;

//     ts.it_interval.tv_nsec = (stepSize-ts.it_interval.tv_sec)*1000000000;

//     ts.it_value.tv_sec = ts.it_interval.tv_sec;

//     ts.it_value.tv_nsec = ts.it_interval.tv_nsec; 

//     ret = timer_settime(main_timer, TIMER_ABSTIME, &ts, NULL);

//     if(ret < 0)

//     {

//         printf("main_timer() fail, ret:%d", ret); 

//         timer_delete(main_timer);

//         //timer_created = false;

//         exit(1);

//     } 

// 	//NCSLabOneStep();
// 	//mp->time+=STEP_SIZE;

// }


int main(int argc, char *argv[]){

	NCSLabInit();
    

	extModeData.acc=1;

    if(argc==2){
      extModeData.port=atoi(argv[1]);
    }
    else{
      extModeData.port=0;
    }

	mp=NCSLabGetModelP();
	extModeData.mp=mp;

    printf("Current sample stepSize is %lf.\n", mp->stepSize);

	startMyServerThread(&extModeData);

    start_event_driven_timer(mp->stepSize);
    pthread_join(extModeData.servetThread,NULL);
	return 0;
	//WaitForSingleObject(CreateEvent(NULL,FALSE,FALSE,NULL),INFINITE);
}
