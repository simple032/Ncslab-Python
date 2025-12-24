# Copyright 2014-2025 NCSLab - Network Control Systems Laboratory
#
# File    : makefile (Android ARM Cross-Compilation)
# Revision: 2.0
# Date    : 2025-01-26
#
# Abstract:
#       Professional makefile for building Android ARM-based real-time
#       applications using NCSLabLink generated C++ code and ARM cross-compiler.
#       Designed for deployment on Android devices with ARM processors.
#
#       This makefile supports:
#       - Cross-compilation for ARM Android targets
#       - Real-time control applications
#       - Hardware abstraction (DAC, ADC, Serial, etc.)
#       - Network communication (TCP/IP, UDP)
#       - Multi-threading support (OpenMP, pthread)
#
#       Build options:
#         OPT_OPTS       - Optimization flags (default: -O2)
#         USER_SRCS      - Additional user source files
#         USER_INCLUDES  - Additional include paths
#         USER_LIBS      - Additional libraries to link
#         SYSROOT        - Android system root directory
#         ANDROIDDIR     - Android NDK toolchain directory

#======================== Tool Chain Specifications ===========================

# Cross-Compiler Configuration
# 1. 工具链配置（Clang，Windows 路径）
ANDROID_NDK := D:/Software/android-ndk-r27d
# 64位 ARM（arm64-v8a，Android 11 主流架构），API 30
CROSS_PREFIX := $(ANDROID_NDK)/toolchains/llvm/prebuilt/windows-x86_64/bin/aarch64-linux-android30
# CROSS_PREFIX := arm-linux-androideabi
CC           := $(CROSS_PREFIX)-clang
CXX          := $(CROSS_PREFIX)-clang++
LD           := $(CXX)
RANLIB       := $(CROSS_PREFIX)-ranlib
# RM           := rm -f
RM           := del /f /q
CHMOD        := chmod

# System Root Directory (Android environment)
ifndef SYSROOT
  SYSROOT := $(ANDROID_NDK)/toolchains/llvm/prebuilt/windows-x86_64/sysroot
endif

# Android NDK Directory (if different from default cross-compiler)
ifndef ANDROIDDIR
  ANDROIDDIR := /opt/android-ndk
endif

#======================== Target Configuration =================================

# Target executable name
TARGET := ncslab.bin

# Build mode: RT (Real-Time), DEBUG, or RELEASE
BUILD_MODE := RT

#======================== Source Files Configuration ===========================

# Core application modules
CORE_SRCS := \
	mainccode.cpp \
	ncslabmain.cpp \
	onestep.cpp \
	util.cpp

# Thread and communication modules
COMM_SRCS := \
	ServerThread.cpp \
	ClientThread.cpp \
	UploadThread.cpp \
	DataApi.cpp

# Hardware abstraction layer (HAL) modules


# Matrix and math library modules
# MATH_SRCS removed - Matrix.hpp is now header-only template
MATH_SRCS :=

# S-Function modules (dynamically generated)
SFCN_SRCS :=

# User-defined source files (can be specified externally)
ifndef USER_SRCS
  USER_SRCS :=
endif

# Combine all source files
ALL_SRCS := $(CORE_SRCS) $(COMM_SRCS) $(MATH_SRCS) $(SFCN_SRCS) $(USER_SRCS)

# Generate object file list
CORE_OBJS := $(CORE_SRCS:.cpp=.o)
COMM_OBJS := $(COMM_SRCS:.cpp=.o)
# HAL_OBJS  := $(HAL_SRCS:.c=.o)
MATH_OBJS := $(MATH_SRCS:.cpp=.o)
SFCN_OBJS := $(SFCN_SRCS:.cpp=.o)
USER_OBJS := $(USER_SRCS:.cpp=.o)

OBJS := $(CORE_OBJS) $(COMM_OBJS) $(MATH_OBJS) $(SFCN_OBJS) $(USER_OBJS)

#======================== Include Path Configuration ===========================

# System include paths
SYSTEM_INCLUDES := -I$(SYSROOT)/include

# NCSLab runtime includes
NCSLAB_INCLUDES := -I${M2PLAB_ROOT}/server/cruntime/include

# User-defined includes (can be specified externally)
ifndef USER_INCLUDES
  USER_INCLUDES :=
endif

# Combined include paths
INCLUDES := -I. -I.. $(SYSTEM_INCLUDES) $(NCSLAB_INCLUDES) $(USER_INCLUDES)

#======================== Compiler Flags Configuration =========================

# Optimization flags
ifndef OPT_OPTS
  OPT_OPTS := -O2
endif

# Architecture and platform definitions
ARCH_FLAGS := \
	-march=armv8-a \
# 	-mfpu=neon-vfpv4 \
# 	-mfloat-abi=hard

# Platform-specific definitions
PLATFORM_DEFS := \
	-D_RT \
	-D_FORTIFY_SOURCE=2 \
	-DANDROID 

# Code generation and compatibility flags
CODEGEN_FLAGS := \
	-std=gnu++11 \
	-fpermissive \
	-fPIC \
	-pthread \
	-fPIE

# Warning flags
WARNING_FLAGS := \
	-Wall \
	-Wformat \
	-Wwrite-strings \
	-Wdate-time \
	-Werror=format-security

# Security and hardening flags
SECURITY_FLAGS := \
	-fstack-protector-strong \
	-D_FORTIFY_SOURCE=2

# Debugging flags
DEBUG_FLAGS := -g


# User-defined compiler options
ifndef USER_OPTS
  USER_OPTS :=
endif

# Combined compiler flags
CFLAGS := $(OPT_OPTS) $(ARCH_FLAGS) $(PLATFORM_DEFS) $(CODEGEN_FLAGS) \
          $(WARNING_FLAGS) $(SECURITY_FLAGS) $(DEBUG_FLAGS) $(USER_OPTS)
          

CXXFLAGS := $(CFLAGS)

#======================== Linker Flags Configuration ===========================

# Standard libraries
SYSLIBS := \
	-lm 

# User-defined libraries
ifndef USER_LIBS
  USER_LIBS :=
endif

# Library search paths
LIBPATH := \
	-L$(SYSROOT)/usr/lib/aarch64-linux-android  	

# Runtime library paths (RPATH for deployment)
RPATH_FLAGS := \
	-Wl,-rpath=/data/local/tmp \
	-Wl,-rpath=/system/lib64 \
	-Wl,-rpath=/vendor/lib64 \
	-Wl,-rpath=/usr/lib/arm-linux-gnueabihf \
	-Wl,-rpath=/usr/lib/arm-linux-gnueabihf/octave/4.4.1 \
	-Wl,-rpath=/usr/lib/arm-linux-gnueabihf/lapack \
	-Wl,-rpath=/usr/lib/arm-linux-gnueabihf/blas

# Security linker flags
SECURITY_LDFLAGS := \
	-Wl,-z,relro \
	-Wl,-z,now \
	-pie \
	-lc++_static

# Combined linker flags
LDFLAGS := $(OPENMP_FLAGS) $(DEBUG_FLAGS) $(SECURITY_LDFLAGS) \
           $(LIBPATH) $(RPATH_FLAGS) $(SYSLIBS) $(USER_LIBS)

#======================== Build Rules ==========================================

# Default target
.PHONY: all
all: banner $(TARGET) finish #clean 这些会并行执行，导致部分.o文件在生成后又马上删除

# Display build information
.PHONY: banner
banner:
	@echo "========================================================"
	@echo " NCSLabLink - Android ARM Cross-Compilation"
	@echo "========================================================"
	@echo " Target:       $(TARGET)"
	@echo " Build Mode:   $(BUILD_MODE)"
	@echo " Compiler:     $(CC)"
	@echo " Optimization: $(OPT_OPTS)"
	@echo " SYSROOT:      $(SYSROOT)"
	@echo "========================================================"
	@echo

# Link final executable
$(TARGET): $(OBJS)
	@echo ">>> Linking executable: $(TARGET)"
	$(LD) -o $@ $(OBJS) $(LDFLAGS)
# 	@$(CHMOD) 755 $@
	@echo ">>> Successfully created: $(TARGET)"

# Compile C++ source files
%.o: %.cpp
	@echo "[CXX] Compiling $<"
	@$(CXX) $(CXXFLAGS) $(INCLUDES) -c $< -o $@

# Compile C source files
%.o: %.c
	@echo "[CC]  Compiling $<"
	@$(CC) $(CFLAGS) $(INCLUDES) -c $< -o $@

#======================== Utility Targets ======================================

# Display completion message
.PHONY: finish
finish:
	@echo
	@echo "========================================================"
	@echo " Build completed successfully!"
	@echo " Executable: $(TARGET)"
	@echo " Ready for deployment to Android ARM target"
	@echo "========================================================"

# Clean build artifacts
.PHONY: clean
clean:
	@echo ">>> Cleaning build artifacts..."
	@$(RM) $(OBJS)
	@echo ">>> Clean completed"

# Clean all (including executable)
.PHONY: distclean
distclean: clean
	@echo ">>> Removing executable..."
	@$(RM) $(TARGET)
	@echo ">>> Distclean completed"

# Rebuild everything from scratch
.PHONY: rebuild
rebuild: distclean all

# Display makefile variables (for debugging)
.PHONY: debug-vars
debug-vars:
	@echo "=== Makefile Variables ==="
	@echo "TARGET:     $(TARGET)"
	@echo "CC:         $(CC)"
	@echo "CXX:        $(CXX)"
	@echo "LD:         $(LD)"
	@echo "SYSROOT:    $(SYSROOT)"
	@echo "CFLAGS:     $(CFLAGS)"
	@echo "LDFLAGS:    $(LDFLAGS)"
	@echo "INCLUDES:   $(INCLUDES)"
	@echo "OBJS:       $(OBJS)"
	@echo "=========================="

# Show help information
.PHONY: help
help:
	@echo "NCSLabLink Android ARM Makefile - Available Targets:"
	@echo ""
	@echo "  all         - Build the target executable (default)"
	@echo "  clean       - Remove object files"
	@echo "  distclean   - Remove all build artifacts"
	@echo "  rebuild     - Clean and rebuild from scratch"
	@echo "  debug-vars  - Display makefile variables"
	@echo "  help        - Show this help message"
	@echo ""
	@echo "Build Options:"
	@echo "  OPT_OPTS=<flags>       - Optimization flags (default: -O2)"
	@echo "  USER_SRCS=<files>      - Additional source files"
	@echo "  USER_INCLUDES=<paths>  - Additional include paths"
	@echo "  USER_LIBS=<libs>       - Additional libraries"
	@echo "  SYSROOT=<path>         - Android system root"
	@echo ""
	@echo "Examples:"
	@echo "  make                          # Build with defaults"
	@echo "  make clean                    # Clean build artifacts"
	@echo "  make OPT_OPTS=-O3            # Build with O3 optimization"
	@echo "  make USER_SRCS='custom.cpp'  # Add custom source file"

#======================== Dependency Tracking ==================================

# Automatic dependency generation (optional, for advanced builds)
# -include $(OBJS:.o=.d)

# %.d: %.cpp
# 	@$(CXX) -MM $(CXXFLAGS) $(INCLUDES) $< > $@.tmp
# 	@sed 's,\($*\)\.o[ :]*,\1.o $@ : ,g' < $@.tmp > $@
# 	@$(RM) $@.tmp

# %.d: %.c
# 	@$(CC) -MM $(CFLAGS) $(INCLUDES) $< > $@.tmp
# 	@sed 's,\($*\)\.o[ :]*,\1.o $@ : ,g' < $@.tmp > $@
# 	@$(RM) $@.tmp

#======================== End of Makefile ======================================
# vim: set noet ts=4 sw=4:
