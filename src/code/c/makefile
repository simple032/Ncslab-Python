

CC=gcc

ncslab.exe: ncslabccode.o
	$(CC) -o ncslab.exe ncslabccode.o

ncslabccode.o: ncslabccode.c ncslabccode.h
	$(CC) -c ncslabccode.c

clean:
	rm ncslab.exe ncslabccode.o 