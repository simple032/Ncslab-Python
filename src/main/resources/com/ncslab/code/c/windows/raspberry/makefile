SFCNOBJS=
OBJS=$(SFCNOBJS) mainccode.o ServerThread.o ClientThread.o DataApi.o UploadThread.o Matrix.o ncslabmain.o  util.o onestep.o \
  DAC8532.o DEV_Config.o  ADS1256.o ncs_serialport_pi.o hardware.o

CC=arm-none-linux-gnueabihf-g++
LD=arm-none-linux-gnueabihf-g++

# 指定系统根目录
SYSROOT := D:/NCSLab/pi

CCFLAGS +=-D_RT -fpermissive -Wwrite-strings  -Wdate-time -D_FORTIFY_SOURCE=2 -fPIC -pthread -fopenmp -g -O2  -std=gnu++11 -fstack-protector-strong -Wformat -Werror=format-security
CCFLAGS += -D_ENABLE_PI
LDFLAGS +=-lm -lpthread -lrt -lwiringPi -Wdate-time -D_FORTIFY_SOURCE=2-pthread -fopenmp -g -O2  -fstack-protector-strong -Wformat -Werror=format-security -rdynamic  -fPIC -Wl,-z,relro -L/usr/lib/arm-linux-gnueabihf  -Wl,-rpath=/usr/lib/arm-linux-gnueabihf -Wl,-rpath=usr/lib/arm-linux-gnueabihf/octave/4.4.1 -Wl,-rpath=/usr/lib/arm-linux-gnueabihf/lapack -Wl,-rpath=/usr/lib/arm-linux-gnueabihf/blas -L/opt/pi/lib -L/opt/cross-pi-gcc/arm-linux-gnueabihf/lib
# 包含目录
CCFLAGS += -I$(SYSROOT)/include -ID:/M2PLab/server/cruntime/include

# 库目录
LDFLAGS += -L$(SYSROOT)/lib

ncslab.exe: $(OBJS)
	$(LD) -o ncslab.exe $(OBJS) $(LDFLAGS)

mainccode.o: mainccode.cpp
	$(CC) $(CCFLAGS)  -c mainccode.cpp

onestep.o: onestep.cpp
	$(CC) $(CCFLAGS)  -c onestep.cpp

util.o: util.cpp
	$(CC) $(CCFLAGS)  -c util.cpp

ServerThread.o: ServerThread.cpp
	$(CC) $(CCFLAGS)  -c ServerThread.cpp

ClientThread.o: ClientThread.cpp
	$(CC) $(CCFLAGS)  -c ClientThread.cpp

DataApi.o: DataApi.cpp
	$(CC) $(CCFLAGS)  -c DataApi.cpp

UploadThread.o: UploadThread.cpp
	$(CC) $(CCFLAGS)  -c UploadThread.cpp

Matrix.o: Matrix.cpp
	$(CC) $(CCFLAGS)  -c Matrix.cpp

ncslabmain.o: ncslabmain.cpp
	$(CC) $(CCFLAGS)  -c ncslabmain.cpp

%.o : %.c
	$(CC) $(CCFLAGS)  -c "$<"

clean:
	rm ncslab $(OBJS)
