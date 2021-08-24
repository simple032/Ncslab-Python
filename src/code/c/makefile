OBJS=ncslabmain.o mainccode.o ServerThread.o DataApi.o ClientThread.o UploadThread.o

//CC=/opt/pi/tools/arm-bcm2708/arm-rpi-4.9.3-linux-gnueabihf/bin/arm-linux-gnueabihf-gcc
CC=gcc

//LD=/opt/pi/tools/arm-bcm2708/arm-rpi-4.9.3-linux-gnueabihf/bin/arm-linux-gnueabihf-gcc
LD=gcc

CCFlag=

LDFLAG=-lm -lpthread -lrt

ncslab: $(OBJS)
	$(LD) -o ncslab $(OBJS) $(LDFLAG)
	
%.o : %.c
	$(CC) $(CCFlag) -c "$<"

clean:
	rm ncslab $(OBJS)