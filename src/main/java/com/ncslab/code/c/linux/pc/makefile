
CC=g++

LD=g++

#CCFlag=-mwindows -D_WIN32_WINNT=0x0501

CCFlag = -DPSAPI_VERSION=1 \
         -I$(VCPKG_PATH)/include \
         -I$(M2PLAB_ROOT)/server/cruntime/include \
		 -I/data/M2PLab/server/cruntime/include \
         -fopenmp -g -O2

LDFlag = -L$(M2PLAB_ROOT)/server/cruntime/lib -lpthread -lrt  -pthread

ncslab: ncslabmain.o mainccode.o ServerThread.o ClientThread.o DataApi.o UploadThread.o onestep.o util.o
	$(LD) -o ncslab ncslabmain.o mainccode.o ServerThread.o ClientThread.o DataApi.o UploadThread.o onestep.o util.o $(LDFlag)

mainccode.o: mainccode.cpp
	$(CC) $(CCFlag) -c mainccode.cpp

ncslabmain.o: ncslabmain.cpp ncslabccode.hpp
	$(CC) $(CCFlag) -c ncslabmain.cpp

ServerThread.o: ServerThread.cpp ServerThread.hpp
	$(CC) $(CCFlag) -c ServerThread.cpp

ClientThread.o: ClientThread.cpp ClientThread.hpp
	$(CC) $(CCFlag) -c ClientThread.cpp

DataApi.o: DataApi.cpp DataApi.hpp
	$(CC) $(CCFlag) -c DataApi.cpp

UploadThread.o: UploadThread.cpp UploadThread.hpp
	$(CC) $(CCFlag) -c UploadThread.cpp

onestep.o: onestep.cpp
	$(CC) $(CCFlag) -c onestep.cpp

util.o: util.cpp
	$(CC) $(CCFlag) -c util.cpp

clean:
	del ncslab.exe *.o
