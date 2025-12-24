# Android Makefile - Quick Reference Card

## Quick Start

```bash
# Build (default)
make

# Clean and rebuild
make rebuild

# Show help
make help
```

---

## Common Build Commands

| Command | Description |
|---------|-------------|
| `make` or `make all` | Build the executable |
| `make clean` | Remove object files only |
| `make distclean` | Remove all build artifacts |
| `make rebuild` | Clean + build from scratch |
| `make help` | Show detailed help |
| `make debug-vars` | Display makefile variables |

---

## Build Options

### Optimization Levels

```bash
make OPT_OPTS=-O0    # No optimization (debugging)
make OPT_OPTS=-O1    # Basic optimization
make OPT_OPTS=-O2    # Default optimization
make OPT_OPTS=-O3    # Aggressive optimization
make OPT_OPTS=-Os    # Size optimization
```

### Custom Source Files

```bash
# Single file
make USER_SRCS="custom.cpp"

# Multiple files
make USER_SRCS="file1.cpp file2.cpp file3.cpp"
```

### Custom Include Paths

```bash
make USER_INCLUDES="-I/path/to/includes"
```

### Custom Libraries

```bash
make USER_LIBS="-lcustom -lspecial"
```

### Combine Options

```bash
make OPT_OPTS=-O3 USER_SRCS="custom.cpp" USER_INCLUDES="-I/opt/inc"
```

---

## File Organization

### Source Files Structure

```
CORE_SRCS    - Main application (mainccode.cpp, ncslabmain.cpp, etc.)
COMM_SRCS    - Communication (ServerThread.cpp, ClientThread.cpp, etc.)
HAL_SRCS     - Hardware (DAC8532.c, ADS1256.c, etc.)
MATH_SRCS    - Math libraries (Matrix.cpp)
SFCN_SRCS    - S-Functions (user-defined)
USER_SRCS    - Additional user files
```

### Generated Files

```
*.o          - Object files (intermediate)
*.d          - Dependency files (automatic)
ncslab.exe   - Final executable
```

---

## Configuration Variables

### Toolchain

| Variable | Default | Description |
|----------|---------|-------------|
| `CROSS_PREFIX` | `arm-none-linux-gnueabihf` | Cross-compiler prefix |
| `CC` | `${CROSS_PREFIX}-g++` | C compiler |
| `CXX` | `${CROSS_PREFIX}-g++` | C++ compiler |
| `LD` | `${CROSS_PREFIX}-g++` | Linker |

### Paths

| Variable | Default | Description |
|----------|---------|-------------|
| `SYSROOT` | `D:/NCSLab/pi` | Android system root |
| `ANDROIDDIR` | `/opt/android-ndk` | Android NDK directory |

### Build Options

| Variable | Default | Description |
|----------|---------|-------------|
| `OPT_OPTS` | `-O2` | Optimization flags |
| `USER_SRCS` | _(empty)_ | Additional source files |
| `USER_INCLUDES` | _(empty)_ | Additional include paths |
| `USER_LIBS` | _(empty)_ | Additional libraries |

---

## Compiler Flags Reference

### Architecture Flags
```
-march=armv7-a              # ARM v7 architecture
-mfpu=neon-vfpv4           # NEON SIMD + VFPv4
-mfloat-abi=hard           # Hardware floating-point
```

### Platform Definitions
```
-D_RT                       # Real-time mode
-D_ENABLE_PI               # Raspberry Pi features
-Dlinux -D__linux__ -Dunix # Linux platform
```

### Code Generation
```
-std=gnu++11               # C++11 with GNU extensions
-fpermissive               # Permissive mode
-fPIC                      # Position-independent code
-pthread                   # POSIX threads
```

### Warnings
```
-Wall                      # All warnings
-Wformat                   # Format string warnings
-Wwrite-strings            # String literal warnings
-Werror=format-security    # Format security errors
```

### Security
```
-fstack-protector-strong   # Stack overflow protection
-D_FORTIFY_SOURCE=2        # Buffer overflow detection
```

### Debugging
```
-g                         # Debug symbols
```

### Optimization
```
-fopenmp                   # OpenMP parallel processing
```

---

## Linker Flags Reference

### Standard Libraries
```
-lm                        # Math library
-lpthread                  # POSIX threads
-lrt                       # Real-time extensions
```

### Hardware Libraries
```
-lwiringPi                 # GPIO/Hardware control
```

### Library Paths
```
-L$(SYSROOT)/lib
-L/usr/lib/arm-linux-gnueabihf
-L/opt/pi/lib
```

### Runtime Paths (RPATH)
```
-Wl,-rpath=/usr/lib/arm-linux-gnueabihf
```

### Security
```
-Wl,-z,relro               # Relocation read-only
-Wl,-z,now                 # Immediate binding (FULL RELRO)
```

---

## Parallel Builds

```bash
# Use 2 jobs
make -j2

# Use 4 jobs
make -j4

# Auto-detect cores (Linux)
make -j$(nproc)

# Auto-detect cores (macOS)
make -j$(sysctl -n hw.ncpu)
```

---

## Troubleshooting

### Check Compiler
```bash
which arm-none-linux-gnueabihf-g++
arm-none-linux-gnueabihf-g++ --version
```

### Debug Build Configuration
```bash
make debug-vars
```

### Verbose Build
```bash
make V=1
```

### Check Dependencies
```bash
# View executable dependencies
arm-none-linux-gnueabihf-readelf -d ncslab.exe

# View symbols
arm-none-linux-gnueabihf-nm ncslab.exe
```

---

## Common Scenarios

### Development Build (Fast Compile)
```bash
make OPT_OPTS=-O0 -j4
```

### Production Build (Optimized)
```bash
make distclean
make OPT_OPTS=-O3
```

### Debug Build (With Symbols)
```bash
make OPT_OPTS="-O0 -ggdb3"
```

### Size-Optimized Build
```bash
make OPT_OPTS="-Os -flto"
```

### Add Custom Module
```bash
make USER_SRCS="mymodule.cpp" USER_INCLUDES="-I./mymodule"
```

---

## Target Platform Info

- **Architecture**: ARMv7-A
- **FPU**: NEON SIMD + VFPv4
- **Float ABI**: Hard float
- **Threading**: POSIX threads + OpenMP
- **Target OS**: Linux (Android)

---

## Build Output Example

```
========================================================
 NCSLabLink - Android ARM Cross-Compilation
========================================================
 Target:       ncslab.exe
 Build Mode:   RT
 Compiler:     arm-none-linux-gnueabihf-g++
 Optimization: -O2
 SYSROOT:      D:/NCSLab/pi
========================================================

[CXX] Compiling mainccode.cpp
[CXX] Compiling ncslabmain.cpp
[CXX] Compiling onestep.cpp
[CXX] Compiling util.cpp
[CXX] Compiling ServerThread.cpp
[CXX] Compiling ClientThread.cpp
[CXX] Compiling UploadThread.cpp
[CXX] Compiling DataApi.cpp
[CC]  Compiling hardware.c
[CC]  Compiling ncs_serialport_pi.c
[CC]  Compiling DAC8532.c
[CC]  Compiling DEV_Config.c
[CC]  Compiling ADS1256.c
[CXX] Compiling Matrix.cpp
>>> Linking executable: ncslab.exe
>>> Successfully created: ncslab.exe

========================================================
 Build completed successfully!
 Executable: ncslab.exe
 Ready for deployment to Android ARM target
========================================================
```

---

## Tips

1. **Use parallel builds** (`-j`) for faster compilation
2. **Use `make rebuild`** instead of `make clean && make`
3. **Check `make debug-vars`** when troubleshooting
4. **Use `make help`** for comprehensive documentation
5. **Keep custom settings** in a separate `config.mk` file
6. **Test incrementally** - dependency tracking speeds up rebuilds

---

**Quick Help**: `make help`
**Debug Variables**: `make debug-vars`
**Clean Build**: `make rebuild`
