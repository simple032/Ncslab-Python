

CC=gcc

LD=gcc

CCFlag=-mwindows -D_WIN32_WINNT=0x0501 

ncslab.exe: ncslabmain.o mainccode.o ServerThread.o ClientThread.o DataApi.o UploadThread.o
	$(LD) -o ncslab.exe ncslabmain.o mainccode.o ServerThread.o ClientThread.o DataApi.o UploadThread.o -lwinmm -lws2_32
	
mainccode.o: mainccode.c
	$(CC) $(CCFlag) -c mainccode.c

ncslabmain.o: ncslabmain.c ncslabccode.h
	$(CC) $(CCFlag) -c ncslabmain.c

ServerThread.o: ServerThread.c ServerThread.h 
	$(CC) $(CCFlag) -c ServerThread.c

ClientThread.o: ClientThread.c ClientThread.h
	$(CC) $(CCFlag) -c ClientThread.c
	
DataApi.o: DataApi.c DataApi.h
	$(CC) $(CCFlag) -c DataApi.c
	
UploadThread.o: UploadThread.c UploadThread.h
	$(CC) $(CCFlag) -c UploadThread.c

clean:
	rm ncslab.exe mainccode.o ncslabmain.o