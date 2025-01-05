OBJS=$(SFCNOBJS) mainccode.o hardware.o ServerThread.o ClientThread.o DataApi.o UploadThread.o ncs_serialport_pi.o onestep.o util.o ncslabmain.o Matrix.o DAC8532.o DEV_Config.o  ADS1256.o

CC=/opt/pi/tools/arm-bcm2708/arm-rpi-4.9.3-linux-gnueabihf/bin/arm-linux-gnueabihf-g++
//CC=gcc

LD=/opt/pi/tools/arm-bcm2708/arm-rpi-4.9.3-linux-gnueabihf/bin/arm-linux-gnueabihf-g++
//LD=gcc

CCFlag=-D_RT -fpermissive -Wwrite-strings -I /opt/pi/include -I /home/wenshan/NCSLabLink/ncslablink/src/code/c -Wdate-time -D_FORTIFY_SOURCE=2 -fPIC -I/usr/include/octave-4.2.2/octave/.. -I/usr/include/octave-4.2.2/octave  -pthread -fopenmp -g -O2 -fdebug-prefix-map=/build/octave-QiEri5/octave-4.2.2=. -std=gnu++11 -fstack-protector-strong -Wformat -Werror=format-security
CCFlag += -D_ENABLE_PI -I/usr/include/eigen3
LDFLAG=-lm -lpthread -lrt -lwiringPi -L /opt/pi/lib -L /opt/cross-pi-gcc/arm-linux-gnueabihf/lib -Wdate-time -D_FORTIFY_SOURCE=2 -I/usr/include/octave-4.4.2/octave/.. -I/usr/include/octave-4.4.2/octave  -pthread -fopenmp -g -O2 -fdebug-prefix-map=/build/octave-QiEri5/octave-4.4.1=. -fstack-protector-strong -Wformat -Werror=format-security -rdynamic  -fPIC -Wl,-z,relro -L/usr/lib/arm-linux-gnueabihf/octave/4.4.1 -L/usr/lib/arm-linux-gnueabihf  -Wl,-rpath=/usr/lib/arm-linux-gnueabihf -Wl,-rpath=usr/lib/arm-linux-gnueabihf/octave/4.4.1 -Wl,-rpath=/usr/lib/arm-linux-gnueabihf/lapack -Wl,-rpath=/usr/lib/arm-linux-gnueabihf/blas -L/opt/pi/lib -L/opt/cross-pi-gcc/arm-linux-gnueabihf/lib

ncslab: $(OBJS)
	$(LD) -o ncslab $(OBJS) $(LDFLAG)

mainccode.o: mainccode.cpp ncslab.hpp
	$(CC) $(CCFlag)  -c mainccode.cpp

onestep.o: onestep.cpp ncslab.hpp
	$(CC) $(CCFlag)  -c onestep.cpp

util.o: util.cpp ncslab.hpp
	$(CC) $(CCFlag)  -c util.cpp

ServerThread.o: ServerThread.cpp
	$(CC) $(CCFlag)  -c ServerThread.cpp

ClientThread.o: ClientThread.cpp
	$(CC) $(CCFlag)  -c ClientThread.cpp

DataApi.o: DataApi.cpp
	$(CC) $(CCFlag)  -c DataApi.cpp

UploadThread.o: UploadThread.cpp
	$(CC) $(CCFlag)  -c UploadThread.cpp

Matrix.o: Matrix.cpp
	$(CC) $(CCFlag)  -c Matrix.cpp

ncslabmain.o: ncslabmain.cpp
	$(CC) $(CCFlag)  -c ncslabmain.cpp

%.o : %.c
	$(CC) $(CCFlag)  -c "$<"

clean:
	rm ncslab $(OBJS)
