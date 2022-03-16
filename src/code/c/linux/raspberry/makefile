OBJS=$(SFCNOBJS) mainccode.o ServerThread.o ClientThread.o DataApi.o UploadThread.o ncs_serialport_pi.o onestep.o util.o ncslabmain.o 

CC=/opt/pi/tools/arm-bcm2708/arm-rpi-4.9.3-linux-gnueabihf/bin/arm-linux-gnueabihf-g++
//CC=gcc

LD=/opt/pi/tools/arm-bcm2708/arm-rpi-4.9.3-linux-gnueabihf/bin/arm-linux-gnueabihf-g++
//LD=gcc

CCFlag=-fpermissive -Wwrite-strings -I /opt/pi/include -I /home/wenshan/NCSLabLink/ncslablink/src/code/c -Wdate-time -D_FORTIFY_SOURCE=2 -fPIC -I/usr/include/octave-4.2.2/octave/.. -I/usr/include/octave-4.2.2/octave  -pthread -fopenmp -g -O2 -fdebug-prefix-map=/build/octave-QiEri5/octave-4.2.2=. -std=gnu++11 -fstack-protector-strong -Wformat -Werror=format-security

LDFLAG=-lm -lpthread -lrt -lwiringPi -L /opt/pi/lib -L /opt/cross-pi-gcc/arm-linux-gnueabihf/lib -Wdate-time -D_FORTIFY_SOURCE=2 -I/usr/include/octave-4.4.2/octave/.. -I/usr/include/octave-4.4.2/octave  -pthread -fopenmp -g -O2 -fdebug-prefix-map=/build/octave-QiEri5/octave-4.4.1=. -fstack-protector-strong -Wformat -Werror=format-security -rdynamic  -fPIC -Wl,-z,relro -L/usr/lib/arm-linux-gnueabihf/octave/4.4.1 -L/usr/lib/arm-linux-gnueabihf  -loctinterp -loctave -Wl,-rpath=/usr/lib/arm-linux-gnueabihf -Wl,-rpath=usr/lib/arm-linux-gnueabihf/octave/4.4.1 -Wl,-rpath=/usr/lib/arm-linux-gnueabihf/lapack -Wl,-rpath=/usr/lib/arm-linux-gnueabihf/blas -L/opt/pi/lib -L/opt/cross-pi-gcc/arm-linux-gnueabihf/lib

ncslab: $(OBJS)
	$(LD) -o ncslab $(OBJS) $(LDFLAG)

ncslabmain.o: ncslabmain.c
	$(CC) $(CCFlag) -c ncslabmain.c

%.o : %.c
	$(CC) $(CCFlag)  -c "$<"

clean:
	rm ncslab $(OBJS)