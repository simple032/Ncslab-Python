# TODO: this file need modification

OBJS=$(SFCNOBJS) mainccode.o ServerThread.o ClientThread.o DataApi.o UploadThread.o onestep.o util.o ncslabmain.o results.o Matrix.o

# CC=g++
CC=/opt/homebrew/Cellar/llvm/17.0.5/bin/clang++

# LD=g++
LD=/opt/homebrew/Cellar/llvm/17.0.5/bin/clang++

CCFlag=-D_SIMU -fpermissive -Wwrite-strings -I /Users/square/Documents/Code/lib/jsoncpp	-I /Users/square/Documents/Code/lib/pi -I /Users/square/Documents/Code/ncslablink/src/code/c -Wdate-time -D_FORTIFY_SOURCE=2 -fPIC -pthread -fopenmp -g -O2 -std=gnu++11 -fstack-protector-strong -Wformat -Werror=format-security	

# -ljsoncpp
# -Wl,-Bsymbolic-functions -Wl,-z,relro
LDFLAG=-lm -lpthread -L /Users/square/vcpkg/packages/jsoncpp_arm64-osx/lib -Wdate-time -D_FORTIFY_SOURCE=2 -pthread -fopenmp -g -O2 -fstack-protector-strong -Wformat -Werror=format-security -rdynamic  -fPIC

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
