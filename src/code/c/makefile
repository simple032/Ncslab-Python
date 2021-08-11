

CC=gcc

LD=gcc

CCFlag=-mwindows -D_WIN32_WINNT=0x0501

ncslab.exe: ncslabmain.o mainccode.o ServerThread.o
	$(LD) -o ncslab.exe ncslabmain.o mainccode.o ServerThread.o -lwinmm -lws2_32
	
mainccode.o: mainccode.c
	$(CC) $(CCFlag) -c mainccode.c

ncslabmain.o: ncslabmain.c ncslabccode.h
	$(CC) $(CCFlag) -c ncslabmain.c

ServerThread.o: ServerThread.c ServerThread.h 
	$(CC) $(CCFlag) -c ServerThread.c

clean:
	rm ncslab.exe mainccode.o ncslabmain.o