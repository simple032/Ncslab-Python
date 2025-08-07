
# OBJS=$(SFCNOBJS) mainccode.o ServerThread.o ClientThread.o DataApi.o UploadThread.o onestep.o util.o ncslabmain.o results.o Matrix.o
OBJS = $(SFCNOBJS) mainccode.o onestep.o util.o ncslabmain.o results.o Matrix.o

CC=g++

LD=g++

CCFlag=-D_SIMU -fPIC -g -O2 -std=gnu++11 -Wformat -Wwrite-strings -fpermissive\
		-mwindows -D_WIN32_WINNT=0x0501 -DPSAPI_VERSION=1 \
		-I$(M2PLAB_ROOT)/server/cruntime/include \
		-IE:\M2PLab\server\cruntime\include \
		-fopenmp

LDFLAG=-lm -g -O2 -Wformat -fPIC -lwinmm -lws2_32 -fpermissive -L$(M2PLAB_ROOT)/server/cruntime/lib -lgsl

ncslab: $(OBJS)
	$(LD) -o ncslab $(OBJS) $(LDFLAG)

mainccode.o: mainccode.cpp
	$(CC) $(CCFlag)  -c mainccode.cpp

onestep.o: onestep.cpp
	$(CC) $(CCFlag)  -c onestep.cpp

results.o: results.cpp
	$(CC) $(CCFlag)  -c results.cpp

util.o: util.cpp
	$(CC) $(CCFlag)  -c util.cpp

ncslabmain.o: ncslabmain.cpp
	$(CC) $(CCFlag)  -c ncslabmain.cpp

Matrix.o: Matrix.cpp
	$(CC) $(CCFlag)  -c Matrix.cpp


%.o: %.cpp
	$(CC) $(CCFlag)  -c "$<"

clean:
	rm ncslab $(OBJS)
