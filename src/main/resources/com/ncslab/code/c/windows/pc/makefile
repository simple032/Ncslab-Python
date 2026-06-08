
CC=cl.exe

LD=link.exe
COMMON_HEADERS = ncslabccode.hpp ncslab.hpp ncslabdefines.hpp mainccode.hpp util.hpp onestep.hpp Matrix.hpp ncslabsfun.hpp DataApi.hpp ServerThread.hpp ClientThread.hpp UploadThread.hpp

#CCFlag=-mwindows -D_WIN32_WINNT=0x0501
NCSLAB_VCPKG_ROOT ?= D:/vcpkg
VCPKG_TRIPLET = x64-windows
VCPKG_INSTALLED := $(NCSLAB_VCPKG_ROOT)/installed/$(VCPKG_TRIPLET)
GSL_ROOT ?= $(NCSLAB_VCPKG_ROOT)/packages/gsl_$(VCPKG_TRIPLET)
KLU_LIB := $(wildcard $(VCPKG_INSTALLED)/lib/klu.lib)

ifneq ($(KLU_LIB),)
  KLU_CPPFLAGS = /DNCSLAB_USE_KLU /I"$(VCPKG_INSTALLED)/include/suitesparse"
  KLU_LIBS = klu.lib btf.lib amd.lib colamd.lib suitesparseconfig.lib openblas.lib
  $(info NCSLab: SuiteSparse KLU enabled for sparse circuit solves.)
else
  KLU_CPPFLAGS =
  KLU_LIBS =
endif

CCFlag = /nologo /EHsc /MD /O2 /Z7 /std:c++14 \
         /D_WIN32_WINNT=0x0501 /DPSAPI_VERSION=1 \
         /I"$(M2PLAB_ROOT)/server/cruntime/include" \
         /I"$(VCPKG_INSTALLED)/include" \
         /I"$(VCPKG_INSTALLED)/include/eigen3" \
         /I"$(GSL_ROOT)/include" \
         $(KLU_CPPFLAGS) \
         /openmp

LDFlag = /NOLOGO /DEBUG /INCREMENTAL:NO \
         /LIBPATH:"$(M2PLAB_ROOT)/server/cruntime/lib" \
         /LIBPATH:"$(VCPKG_INSTALLED)/lib" \
         /LIBPATH:"$(GSL_ROOT)/lib" \
         winmm.lib ws2_32.lib gsl.lib gslcblas.lib $(KLU_LIBS)

ncslab.exe: ncslabmain.o mainccode.o ServerThread.o ClientThread.o DataApi.o UploadThread.o onestep.o util.o
	$(LD) /OUT:ncslab.exe ncslabmain.o mainccode.o ServerThread.o ClientThread.o DataApi.o UploadThread.o onestep.o util.o $(LDFlag)
	powershell -NoProfile -Command "$$dlls=@('$(GSL_ROOT)/bin/gsl.dll','$(GSL_ROOT)/bin/gslcblas.dll'); foreach ($$n in 'klu.dll','btf.dll','amd.dll','colamd.dll','suitesparseconfig.dll','openblas.dll') { $$dlls += (Join-Path '$(VCPKG_INSTALLED)/bin' $$n) }; foreach ($$src in $$dlls) { if (Test-Path $$src) { $$dst = Join-Path (Get-Location) (Split-Path $$src -Leaf); if (!(Test-Path $$dst) -or (Get-Item $$src).Length -ne (Get-Item $$dst).Length -or (Get-Item $$src).LastWriteTimeUtc -gt (Get-Item $$dst).LastWriteTimeUtc) { Copy-Item -LiteralPath $$src -Destination $$dst -Force } } }"

mainccode.o: mainccode.cpp $(COMMON_HEADERS)
	$(CC) $(CCFlag) /Fo$@ /c mainccode.cpp

ncslabmain.o: ncslabmain.cpp $(COMMON_HEADERS)
	$(CC) $(CCFlag) /Fo$@ /c ncslabmain.cpp

ServerThread.o: ServerThread.cpp $(COMMON_HEADERS)
	$(CC) $(CCFlag) /Fo$@ /c ServerThread.cpp

ClientThread.o: ClientThread.cpp $(COMMON_HEADERS)
	$(CC) $(CCFlag) /Fo$@ /c ClientThread.cpp

DataApi.o: DataApi.cpp $(COMMON_HEADERS)
	$(CC) $(CCFlag) /Fo$@ /c DataApi.cpp

UploadThread.o: UploadThread.cpp $(COMMON_HEADERS)
	$(CC) $(CCFlag) /Fo$@ /c UploadThread.cpp

onestep.o: onestep.cpp $(COMMON_HEADERS)
	$(CC) $(CCFlag) /Fo$@ /c onestep.cpp

util.o: util.cpp $(COMMON_HEADERS)
	$(CC) $(CCFlag) /Fo$@ /c util.cpp

clean:
	del ncslab.exe *.o
