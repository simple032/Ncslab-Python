BASE_OBJS = $(SFCNOBJS) mainccode.o onestep.o util.o ncslabmain.o results.o ncslab_cuda_probe.o ncslab_cuda_cublas.o ncslab_cuda_cusolver_stub.o
COMMON_HEADERS = ncslabccode.hpp ncslab.hpp ncslabdefines.hpp mainccode.hpp util.hpp onestep.hpp Matrix.hpp results.hpp ncslabsfun.hpp

CC=cl.exe

LD=link.exe

# Optional CUDA build flag (set by Java makeExeFile when client requests CUDA simulation).
# USE_CUDA=1 compiles a linked CUDA device probe with nvcc and links cudart.lib.
USE_CUDA ?= 0
CUDA_ROOT ?= $(CUDA_PATH)
ifeq ($(strip $(CUDA_ROOT)),)
  CUDA_ROOT := C:/Program Files/NVIDIA GPU Computing Toolkit/CUDA/v13.2
endif
NVCC ?= $(CUDA_ROOT)/bin/nvcc.exe

# ---- Third-party dependency locations (override via env if needed) ----
# VsDevCmd may set VCPKG_ROOT to Visual Studio's bundled vcpkg. NCSLab uses the
# project vcpkg tree below; override with NCSLAB_VCPKG_ROOT if needed.
NCSLAB_VCPKG_ROOT ?= D:/vcpkg
VCPKG_TRIPLET ?= x64-windows
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

THIRD_PARTY_INCLUDES = \
		/I"$(VCPKG_INSTALLED)/include" \
		/I"$(VCPKG_INSTALLED)/include/eigen3" \
		/I"$(GSL_ROOT)" \
		/I"$(GSL_ROOT)/include"

THIRD_PARTY_LIBS = \
		/LIBPATH:"$(VCPKG_INSTALLED)/lib" \
		/LIBPATH:"$(GSL_ROOT)/lib" \
		/LIBPATH:"$(GSL_ROOT)/lib64"

CCFlag=/nologo /EHsc /MD /O2 /Z7 /std:c++14 /D_SIMU \
		/D_WIN32_WINNT=0x0501 /DPSAPI_VERSION=1 \
		/I"$(M2PLAB_ROOT)/server/cruntime/include" \
		$(THIRD_PARTY_INCLUDES) \
		$(KLU_CPPFLAGS) \
		/openmp

LDFLAG=/NOLOGO /DEBUG /INCREMENTAL:NO \
		$(THIRD_PARTY_LIBS) \
		/LIBPATH:"$(M2PLAB_ROOT)/server/cruntime/lib" \
		winmm.lib ws2_32.lib gsl.lib gslcblas.lib $(KLU_LIBS)

ifeq ($(USE_CUDA),1)
  CUDA_OBJS = ncslab_cuda_device.o
  CUDA_LIBS = /LIBPATH:"$(CUDA_ROOT)/lib/x64" cudart.lib
  CUDA_DLL_COPY = powershell -NoProfile -Command "$$roots=@('$(CUDA_ROOT)/bin/x64','$(CUDA_ROOT)/bin'); foreach ($$root in $$roots) { if (Test-Path $$root) { Get-ChildItem -LiteralPath $$root -Filter 'cudart64*.dll' -ErrorAction SilentlyContinue | ForEach-Object { Copy-Item -LiteralPath $$_.FullName -Destination (Join-Path (Get-Location) $$_.Name) -Force } } }"
  $(info NCSLab: USE_CUDA=1 - compiling ncslab_cuda_device.cu with nvcc and linking cudart.lib.)
else
  CUDA_OBJS = ncslab_cuda_device_stub.o
  CUDA_LIBS =
  CUDA_DLL_COPY = cmd /C exit 0
endif

OBJS = $(BASE_OBJS) $(CUDA_OBJS)
CUDA_CLEAN_OBJS = ncslab_cuda_device.o ncslab_cuda_device_stub.o

ncslab.exe: $(OBJS)
	$(LD) /OUT:ncslab.exe $(OBJS) $(LDFLAG) $(CUDA_LIBS)
	powershell -NoProfile -Command "$$dlls=@('$(GSL_ROOT)/bin/gsl.dll','$(GSL_ROOT)/bin/gslcblas.dll'); foreach ($$n in 'klu.dll','btf.dll','amd.dll','colamd.dll','suitesparseconfig.dll','openblas.dll') { $$dlls += (Join-Path '$(VCPKG_INSTALLED)/bin' $$n) }; foreach ($$src in $$dlls) { if (Test-Path $$src) { $$dst = Join-Path (Get-Location) (Split-Path $$src -Leaf); if (!(Test-Path $$dst) -or (Get-Item $$src).Length -ne (Get-Item $$dst).Length -or (Get-Item $$src).LastWriteTimeUtc -gt (Get-Item $$dst).LastWriteTimeUtc) { Copy-Item -LiteralPath $$src -Destination $$dst -Force } } }"
	$(CUDA_DLL_COPY)

mainccode.o: mainccode.cpp $(COMMON_HEADERS)
	$(CC) $(CCFlag) /Fo$@ /c mainccode.cpp

onestep.o: onestep.cpp $(COMMON_HEADERS)
	$(CC) $(CCFlag) /Fo$@ /c onestep.cpp

results.o: results.cpp $(COMMON_HEADERS)
	$(CC) $(CCFlag) /Fo$@ /c results.cpp

util.o: util.cpp $(COMMON_HEADERS)
	$(CC) $(CCFlag) /Fo$@ /c util.cpp

ncslabmain.o: ncslabmain.cpp $(COMMON_HEADERS)
	$(CC) $(CCFlag) /Fo$@ /c ncslabmain.cpp

ncslab_cuda_probe.o: ncslab_cuda_probe.cpp $(COMMON_HEADERS)
	$(CC) $(CCFlag) /Fo$@ /c ncslab_cuda_probe.cpp

ncslab_cuda_cublas.o: ncslab_cuda_cublas.cpp $(COMMON_HEADERS)
	$(CC) $(CCFlag) /Fo$@ /c ncslab_cuda_cublas.cpp

ncslab_cuda_cusolver_stub.o: ncslab_cuda_cusolver_stub.cpp $(COMMON_HEADERS)
	$(CC) $(CCFlag) /Fo$@ /c ncslab_cuda_cusolver_stub.cpp

ncslab_cuda_device.o: ncslab_cuda_device.cu
	"$(NVCC)" -std=c++14 -O2 -c ncslab_cuda_device.cu -o $@

ncslab_cuda_device_stub.o: ncslab_cuda_device_stub.cpp
	$(CC) $(CCFlag) /Fo$@ /c ncslab_cuda_device_stub.cpp

# Matrix.o build rule removed - Matrix.hpp is now header-only template

%.o: %.cpp $(COMMON_HEADERS)
	$(CC) $(CCFlag) /Fo$@ /c "$<"

clean:
	cmd /C del /Q ncslab.exe $(BASE_OBJS) $(CUDA_CLEAN_OBJS) 2>NUL || exit 0
