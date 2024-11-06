
# OBJS=$(SFCNOBJS) mainccode.o ServerThread.o ClientThread.o DataApi.o UploadThread.o onestep.o util.o ncslabmain.o results.o Matrix.o
OBJS = mainccode.o onestep.o util.o ncslabmain.o results.o Matrix.o

CC=g++

LD=g++
# LD=/opt/homebrew/Cellar/llvm/17.0.5/bin/clang++

srcdir=/home/wenshan/NetConTop/ncslablink/src/main/java/com/ncslab/code/c
pidir=/home/wenshan/ncslablink/WiringPi-2.61-1/wiringPi
jsondir=/usr/include/jsoncpp
# CCFlag=-D_SIMU -fpermissive -Wwrite-strings -I /Users/square/vcpkg/packages/jsoncpp_arm64-osx/include -I /Users/square/Documents/Code/lib/pi -I /Users/square/Documents/Code/ncslablink/src/code/c -Wdate-time -D_FORTIFY_SOURCE=2 -fPIC -pthread -fopenmp -g -O2 -std=c++11 -fstack-protector-strong -Wformat -Werror=format-security -isysroot /Library/Developer/CommandLineTools/SDKs/MacOSX.sdk
# -fopenmp 
# -pthread -Werror=format-security
# -Wdate-time -D_FORTIFY_SOURCE=2 -fstack-protector-strong
# -fpermissive
CCFlag=-D_SIMU -I $(srcdir) -I $(jsondir) -I $(pidir) -fPIC -g -O2 -std=gnu++11 -Wformat -Wwrite-strings -DPSAPI_VERSION=1 -fopenmp
# -ljsoncpp
# -Wl,-Bsymbolic-functions -Wl,-z,relro
# LDFLAG=-lm -lpthread -L /Users/square/vcpkg/packages/jsoncpp_arm64-osx/lib -Wdate-time -D_FORTIFY_SOURCE=2 -pthread -fopenmp -g -O2 -fstack-protector-strong -Wformat -Werror=format-security -rdynamic  -fPIC -isysroot /Library/Developer/CommandLineTools/SDKs/MacOSX.sdk
# -fopenmp -lpthread =pthread
# -Werror=format-security
# -fstack-protector-strong -D_FORTIFY_SOURCE=2 -Wdate-time 
LDFLAG=-lm -ljsoncpp -g -O2 -Wformat -rdynamic -fPIC

ncslab: $(OBJS)
	$(LD) -o ncslab $(OBJS) $(LDFLAG)

mainccode.o: mainccode.cpp
	$(CC) $(CCFlag)  -c mainccode.cpp

onestep.o: onestep.cpp
	$(CC) $(CCFlag)  -c onestep.cpp

results.o: results.cpp
	$(CC) $(CCFlag)  -c results.cpp

util.o: util.cpp
	$(CC) $(CCFlag)  -c util.cpp

ncslabmain.o: ncslabmain.cpp
	$(CC) $(CCFlag)  -c ncslabmain.cpp

Matrix.o: Matrix.cpp
	$(CC) $(CCFlag)  -c Matrix.cpp


%.o: %.c
	$(CC) $(CCFlag)  -c "$<"

clean:
	rm ncslab $(OBJS)
