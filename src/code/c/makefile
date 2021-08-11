

CC=gcc

ncslab.exe: ncslabmain.o mainccode.o
	$(CC) -o ncslab.exe ncslabmain.o mainccode.o
	
mainccode.o: mainccode.c
	$(CC) -c mainccode.c

ncslabmain.o: ncslabmain.c ncslabccode.h
	$(CC) -c ncslabmain.c

clean:
	rm ncslab.exe ncslabccode.o 