
CC=g++

LD=g++

#CCFlag=-mwindows -D_WIN32_WINNT=0x0501
VCPKG_TRIPLET = x64-windows
VCPKG_PATH = $(VCPKG_ROOT)\$(VCPKG_TRIPLET)

CCFlag = -mwindows -D_WIN32_WINNT=0x0501 -DPSAPI_VERSION=1 \
         -I${M2PLAB_ROOT}/server/cruntime/include \
         -fopenmp -g -O2

LDFlag = -lwinmm -lws2_32 \
         -L${M2PLAB_ROOT}/server/cruntime/lib

ncslab.exe: ncslabmain.o mainccode.o ServerThread.o ClientThread.o DataApi.o UploadThread.o onestep.o util.o
	$(LD) -o ncslab.exe ncslabmain.o mainccode.o ServerThread.o ClientThread.o DataApi.o UploadThread.o onestep.o util.o $(LDFlag)

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
