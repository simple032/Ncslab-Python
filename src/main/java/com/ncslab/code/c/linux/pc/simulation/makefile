# TODO: this file need modification

# OBJS=$(SFCNOBJS) mainccode.o ServerThread.o ClientThread.o DataApi.o UploadThread.o onestep.o util.o ncslabmain.o results.o Matrix.o
OBJS = mainccode.o onestep.o util.o ncslabmain.o results.o Matrix.o

CC=g++
# CC=/opt/homebrew/Cellar/llvm/17.0.5/bin/clang++

LD=g++
# LD=/opt/homebrew/Cellar/llvm/17.0.5/bin/clang++

srcdir=/home/square/ncslablink/ncslablink/src/main/java/com/ncslab/code/c
pidir=/home/square/ncslablink/WiringPi-2.61-1/wiringPi
# CCFlag=-D_SIMU -fpermissive -Wwrite-strings -I /Users/square/vcpkg/packages/jsoncpp_arm64-osx/include -I /Users/square/Documents/Code/lib/pi -I /Users/square/Documents/Code/ncslablink/src/code/c -Wdate-time -D_FORTIFY_SOURCE=2 -fPIC -pthread -fopenmp -g -O2 -std=c++11 -fstack-protector-strong -Wformat -Werror=format-security -isysroot /Library/Developer/CommandLineTools/SDKs/MacOSX.sdk
# -fopenmp 
# -pthread -Werror=format-security
# -Wdate-time -D_FORTIFY_SOURCE=2 -fstack-protector-strong
# -fpermissive
CCFlag=-D_SIMU -I $(srcdir) -I $(pidir) -fPIC -g -O2 -std=gnu++11 -Wformat -Wwrite-strings
# -ljsoncpp
# -Wl,-Bsymbolic-functions -Wl,-z,relro
# LDFLAG=-lm -lpthread -L /Users/square/vcpkg/packages/jsoncpp_arm64-osx/lib -Wdate-time -D_FORTIFY_SOURCE=2 -pthread -fopenmp -g -O2 -fstack-protector-strong -Wformat -Werror=format-security -rdynamic  -fPIC -isysroot /Library/Developer/CommandLineTools/SDKs/MacOSX.sdk
# -fopenmp -lpthread =pthread
# -Werror=format-security
# -fstack-protector-strong -D_FORTIFY_SOURCE=2 -Wdate-time 
LDFLAG=-lm -ljsoncpp -g -O2 -Wformat -rdynamic -fPIC

ncslab: $(OBJS)
	$(LD) -o ncslab $(OBJS) $(LDFLAG)

mainccode.o: mainccode.c ncslab.h
	$(CC) $(CCFlag)  -c mainccode.c

onestep.o: onestep.c ncslab.h
	$(CC) $(CCFlag)  -c onestep.c

results.o: results.c ncslab.h
	$(CC) $(CCFlag)  -c results.c

util.o: util.c ncslab.h
	$(CC) $(CCFlag)  -c util.c

%.o: %.c
	$(CC) $(CCFlag)  -c "$<"

clean:	
	rm ncslab $(OBJS)
