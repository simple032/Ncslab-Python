

CC=g++

LD=g++

#CCFlag=-mwindows -D_WIN32_WINNT=0x0501 

CCFlag=-mwindows -D_WIN32_WINNT=0x0501 -DPSAPI_VERSION=1 -IC:\Octave-6.3.0\mingw64\/include  -IC:\Octave-6.3.0\mingw64\include\octave-6.3.0\octave\.. -IC:\Octave-6.3.0\mingw64\include\octave-6.3.0\octave -IC:\Octave-6.3.0\mingw64\include   -fopenmp -g -O2    

ncslab.exe: ncslabmain.o mainccode.o ServerThread.o ClientThread.o DataApi.o UploadThread.o onestep.o util.o
	$(LD) -o ncslab.exe ncslabmain.o mainccode.o ServerThread.o ClientThread.o DataApi.o UploadThread.o onestep.o util.o -lwinmm -lws2_32
	
mainccode.o: mainccode.c
	$(CC) $(CCFlag) -c mainccode.c

ncslabmain.o: ncslabmain.c ncslabccode.h
	$(CC) $(CCFlag) -c ncslabmain.c

ServerThread.o: ServerThread.cpp ServerThread.hpp 
	$(CC) $(CCFlag) -c ServerThread.cpp

ClientThread.o: ClientThread.cpp ClientThread.hpp
	$(CC) $(CCFlag) -c ClientThread.cpp
	
DataApi.o: DataApi.c DataApi.h
	$(CC) $(CCFlag) -c DataApi.c
	
UploadThread.o: UploadThread.cpp UploadThread.hpp
	$(CC) $(CCFlag) -c UploadThread.cpp

onestep.o: onestep.c
	$(CC) $(CCFlag) -c onestep.c

util.o: util.cpp
	$(CC) $(CCFlag) -c util.cpp

clean:
	rm ncslab.exe mainccode.o ncslabmain.o