OBJS=$(SFCNOBJS) mainccode.o ServerThread.o ClientThread.o DataApi.o UploadThread.o ncs_serialport_pi.o onestep.o util.o ncslabmain.o 

CC=/opt/pi/tools/arm-bcm2708/arm-rpi-4.9.3-linux-gnueabihf/bin/arm-linux-gnueabihf-gcc
//CC=gcc

LD=/opt/pi/tools/arm-bcm2708/arm-rpi-4.9.3-linux-gnueabihf/bin/arm-linux-gnueabihf-gcc
//LD=gcc

CCFlag= -I /opt/pi/include -I /home/wenshan/NCSLabLink/ncslablink/src/code/c

LDFLAG=-lm -lpthread -lrt -lwiringPi -L /opt/pi/lib -L /opt/cross-pi-gcc/arm-linux-gnueabihf/lib

ncslab: $(OBJS)
	$(LD) -o ncslab $(OBJS) $(LDFLAG)

ncslabmain.o: ncslabmain.c
	$(CC) $(CCFlag) -c ncslabmain.c

%.o : %.c
	$(CC) $(CCFlag) -std=c99 -c "$<"

clean:
	rm ncslab $(OBJS)