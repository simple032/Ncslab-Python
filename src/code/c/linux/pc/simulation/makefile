OBJS=$(SFCNOBJS) mainccode.o ServerThread.o ClientThread.o DataApi.o UploadThread.o onestep.o util.o ncslabmain.o results.o

//CC=/opt/pi/tools/arm-bcm2708/arm-rpi-4.9.3-linux-gnueabihf/bin/arm-linux-gnueabihf-g++
CC=g++

//LD=/opt/pi/tools/arm-bcm2708/arm-rpi-4.9.3-linux-gnueabihf/bin/arm-linux-gnueabihf-g++
LD=g++

CCFlag=-D_SIMU -fpermissive -Wwrite-strings -I /usr/include/jsoncpp -I /opt/pi/include -I /home/wenshan/NCSLabLink/ncslablink/src/code/c -Wdate-time -D_FORTIFY_SOURCE=2 -fPIC -I/usr/include/octave-4.4.1/octave/.. -I/usr/include/octave-4.4.1/octave  -pthread -fopenmp -g -O2 -fdebug-prefix-map=/build/octave-QiEri5/octave-4.4.1=. -std=gnu++11 -fstack-protector-strong -Wformat -Werror=format-security

//LDFLAG=-lm -lpthread -lrt -lwiringPi -L /opt/pi/lib -L /opt/cross-pi-gcc/arm-linux-gnueabihf/lib -Wdate-time -D_FORTIFY_SOURCE=2 -I/usr/include/octave-4.4.2/octave/.. -I/usr/include/octave-4.4.2/octave  -pthread -fopenmp -g -O2 -fdebug-prefix-map=/build/octave-QiEri5/octave-4.4.1=. -fstack-protector-strong -Wformat -Werror=format-security -rdynamic  -fPIC -Wl,-z,relro -L/usr/lib/arm-linux-gnueabihf/octave/4.4.1 -L/usr/lib/arm-linux-gnueabihf  -loctinterp -loctave -Wl,-rpath=/usr/lib/arm-linux-gnueabihf -Wl,-rpath=usr/lib/arm-linux-gnueabihf/octave/4.4.1 -Wl,-rpath=/usr/lib/arm-linux-gnueabihf/lapack -Wl,-rpath=/usr/lib/arm-linux-gnueabihf/blas -L/opt/pi/lib -L/opt/cross-pi-gcc/arm-linux-gnueabihf/lib
LDFLAG=-lm -lpthread -lrt -ljsoncpp -Wdate-time -D_FORTIFY_SOURCE=2 -I/usr/include/octave-4.2.2/octave/.. -I/usr/include/octave-4.2.2/octave -I/usr/include/hdf5/serial -I/usr/include/mpi  -pthread -fopenmp -g -O2 -fdebug-prefix-map=/build/octave-DtqyIg/octave-4.2.2=. -fstack-protector-strong -Wformat -Werror=format-security -rdynamic  -fPIC -Wl,-Bsymbolic-functions -Wl,-z,relro  -L/usr/lib/x86_64-linux-gnu/octave/4.2.2 -L/usr/lib/x86_64-linux-gnu -loctinterp -loctave 

ncslab: $(OBJS)
	$(LD) -o ncslab $(OBJS) $(LDFLAG)
	
mainccode.o: mainccode.c ncslab.h
	$(CC) $(CCFlag)  -c mainccode.c
	
onestep.o: onestep.c ncslab.h
	$(CC) $(CCFlag)  -c onestep.c

results.o: results.c ncslab.h
	$(CC) $(CCFlag)  -c results.c
	
util.o: util.c ncslab.h
	$(CC) $(CCFlag)  -c util.c
	
%.o : %.c
	$(CC) $(CCFlag)  -c "$<"

clean:
	rm ncslab $(OBJS)