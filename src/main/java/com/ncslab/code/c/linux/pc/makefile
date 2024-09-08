OBJS=$(SFCNOBJS) mainccode.o ServerThread.o ClientThread.o DataApi.o UploadThread.o onestep.o util.o ncslabmain.o results.o

CC=g++
//CC=/opt/homebrew/Cellar/llvm/17.0.5/bin/clang++

LD=g++
//LD=/opt/homebrew/Cellar/llvm/17.0.5/bin/clang++


CCFlag=-fpermissive -Wwrite-strings -I /usr/include -I /opt/pi/include -I /home/pi/NetConTop/tomcat/apache-tomcat-8.5.32/webapps/ncslablink/WEB-INF/classes/code/c -Wdate-time -D_FORTIFY_SOURCE=2 -fPIC -I/usr/include/octave-4.4.1/octave/.. -I/usr/include/octave-4.4.1/octave  -pthread -fopenmp -g -O2 -fdebug-prefix-map=/build/octave-QiEri5/octave-4.4.1=. -std=gnu++11 -fstack-protector-strong -Wformat -Werror=format-security

LDFLAG=-lm -lpthread -lrt -lwiringPi -ljsoncpp -Wdate-time -D_FORTIFY_SOURCE=2 -I/usr/include/octave-4.4.1/octave.. -I/usr/include/octave-4.4.1/octave -I/usr/include/hdf5/serial -I/usr/include/mpi  -pthread -fopenmp -g -O2 -fdebug-prefix-map=/build/octave-QiEri5/octave-4.4.1=. -fstack-protector-strong -Wformat -Werror=format-security -rdynamic  -fPIC -Wl,-Bsymbolic-functions -Wl,-z,relro  -L/usr/lib/arm-linux-gnueabihf/octave/4.4.1 -L/usr/lib/arm-linux-gnueabihf -loctinterp -loctave

ncslab: $(OBJS)
	$(LD) -o ncslab $(OBJS) $(LDFLAG)


mainccode.o: mainccode.c ncslab.h
	$(CC) $(CCFlag)  -c mainccode.c
    
onestep.o: onestep.c ncslab.h
	$(CC) $(CCFlag)  -c onestep.c
    
util.o: util.c ncslab.h
	$(CC) $(CCFlag)  -c util.c
      
%.o : %.c
	$(CC) $(CCFlag)  -c "$<"

clean:
	rm ncslab $(OBJS)