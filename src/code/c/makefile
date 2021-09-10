OBJS=mainccode.o ServerThread.o ClientThread.o DataApi.o UploadThread.o onestep.o util.o ncs_serialport_pi.o ncslabmain.o 

CC=/opt/pi/tools/arm-bcm2708/arm-rpi-4.9.3-linux-gnueabihf/bin/arm-linux-gnueabihf-gcc
//CC=gcc

LD=/opt/pi/tools/arm-bcm2708/arm-rpi-4.9.3-linux-gnueabihf/bin/arm-linux-gnueabihf-gcc
//LD=gcc

CCFlag= -I /opt/pi/include 

LDFLAG=-lm -lpthread -lrt -lwiringPi -L /opt/pi/lib -L /opt/cross-pi-gcc/arm-linux-gnueabihf/lib

ncslab: $(OBJS)
	$(LD) -o ncslab $(OBJS) $(LDFLAG)
	
%.o : %.c
	$(CC) $(CCFlag) -c "$<"

clean:
	rm ncslab $(OBJS)