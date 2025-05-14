# 交叉编译工具设置
CROSS_TOOLS ?= /opt/cross-tools
CROSS_PREFIX = loongarch64-unknown-linux-gnu-
CC = $(CROSS_TOOLS)/bin/$(CROSS_PREFIX)gcc
CXX = $(CROSS_TOOLS)/bin/$(CROSS_PREFIX)g++
AR = $(CROSS_TOOLS)/bin/$(CROSS_PREFIX)ar
RANLIB = $(CROSS_TOOLS)/bin/$(CROSS_PREFIX)ranlib
STRIP = $(CROSS_TOOLS)/bin/$(CROSS_PREFIX)strip

# 项目目录结构
BUILD_DIR = build
DEPDIR = .deps
$(shell mkdir -p $(BUILD_DIR) $(DEPDIR) >/dev/null)

# SOEM 源文件
SOEM_SRCS = \
    ethercatdc.c \
    ethercatbase.c \
    ethercatconfig.c \
    ethercatprint.c \
    ethercatfoe.c \
    ethercatmain.c \
    ethercatsoe.c \
    ethercateoe.c \
    ethercatcoe.c

SOEM_OBJS = $(SOEM_SRCS:.c=.o)

# OSAL 和 OSHW 源文件
OSAL_SRCS = osal.c
OSHW_SRCS = nicdrv.c \
            oshw.c

OSAL_OBJS = $(OSAL_SRCS:.c=.o)
OSHW_OBJS = $(OSHW_SRCS:.c=.o)

# 所有 SOEM 相关的对象文件
ALL_SOEM_OBJS = $(SOEM_OBJS) $(OSAL_OBJS) $(OSHW_OBJS)

# 源文件和目标文件
SOURCES = mainccode.cpp ServerThread.cpp ClientThread.cpp UploadThread.cpp DataApi.cpp util.cpp onestep.cpp ncslabmain.cpp EtherCAT.cpp
OBJECTS = $(SOURCES:.cpp=.o)

# Eigen 库路径
EIGEN_PATH = /usr/include/eigen3

# 添加数组相关定义
ARRAY_DEFINES = \
    -DMAX_SAMPLES=1000

# 预处理器定义
DEFINES = \
    -Dchar_T=char \
    -DMODEL=NCSLab \
    -DREAL_T=double \
    -DBYTE_T=char \
    -DUINT_T="unsigned int" \
    -D_REENTRANT \
    -D__GLIBC_USE\(F\)=0 \
    -D_GNU_SOURCE \
    -D_RT \
    $(ARRAY_DEFINES)

# 调整包含路径顺序和内容
INCLUDE_FLAGS = \
    -I. \
    -I.. \
    -I../.. \
    -I$(CROSS_TOOLS)/target/usr/include \
    -I$(EIGEN_PATH) \
    -isystem $(CROSS_TOOLS)/loongarch64-unknown-linux-gnu/include/c++/13.0.1 \
    -isystem $(CROSS_TOOLS)/loongarch64-unknown-linux-gnu/include/c++/13.0.1/loongarch64-unknown-linux-gnu \
    -isystem $(CROSS_TOOLS)/loongarch64-unknown-linux-gnu/include

CXX_WARNING_FLAGS = \
    -Wno-write-strings \
    -Wno-enum-compare \
    -Wno-narrowing \
    -Wno-unused-variable \
    -Wno-unused-but-set-variable \
    -Wno-sign-compare \
    -Wno-unused-parameter \
    -Wno-deprecated-enum-enum-conversion \
    -Wno-array-bounds

C_WARNING_FLAGS = \
    -Wno-write-strings \
    -Wno-enum-compare \
    -Wno-narrowing \
    -Wno-unused-variable \
    -Wno-unused-but-set-variable \
    -Wno-sign-compare \
    -Wno-unused-parameter \
    -Wno-array-bounds

# 基本编译标志
COMMON_FLAGS = \
    -Wall \
    $(INCLUDE_FLAGS) \
    $(DEFINES) \
    -mabi=lp64d \
    -march=loongarch64 \
    -mfpu=64 \
    -fPIC \
    -g \
    -O2 \
    -fdiagnostics-color=always

CXXFLAGS = $(COMMON_FLAGS) \
    -std=c++11 \
    -fpermissive \
    -fno-strict-enums \
    $(CXX_WARNING_FLAGS)

CFLAGS = $(COMMON_FLAGS) $(C_WARNING_FLAGS)

# 链接标志
LDFLAGS = -L. \
    -L$(CROSS_TOOLS)/lib \
    -L$(CROSS_TOOLS)/loongarch64-unknown-linux-gnu/lib \
    -L$(CROSS_TOOLS)/target/usr/lib64 \
    -L$(CROSS_TOOLS)/target/lib64 \
    -L$(CROSS_TOOLS)/loongarch64-unknown-linux-gnu/lib64 \
    -Wl,-rpath-link,$(CROSS_TOOLS)/target/lib64 \
    -Wl,-rpath-link,$(CROSS_TOOLS)/loongarch64-unknown-linux-gnu/lib64 \
    -static \
    -lm -lpthread -lrt -lstdc++

# 目标文件和库
EXEC = ncslab
SOEM_LIB = libsoem.a

# 依赖文件生成标志
DEPFLAGS = -MT $@ -MMD -MP -MF $(DEPDIR)/$*.d

# 默认目标
.PHONY: all clean headers soem show-flags debug
all: headers soem $(EXEC)

# 创建依赖文件目录
$(shell mkdir -p $(DEPDIR))

# SOEM 库目标
soem: $(SOEM_LIB)

# 编译规则
$(SOEM_OBJS): %.o: %.c
	@echo "Compiling SOEM $<..."
	$(CC) $(CFLAGS) $(DEPFLAGS) -c $< -o $@

$(OSAL_OBJS): %.o: %.c
	@echo "Compiling OSAL $<..."
	$(CC) $(CFLAGS) $(DEPFLAGS) -c $< -o $@

$(OSHW_OBJS): %.o: %.c
	@echo "Compiling OSHW $<..."
	$(CC) $(CFLAGS) $(DEPFLAGS) -c $< -o $@

# 创建 SOEM 静态库
$(SOEM_LIB): $(ALL_SOEM_OBJS)
	@echo "Creating static library $(SOEM_LIB)..."
	$(AR) rcs $@ $^
	$(RANLIB) $@

# C++ 源文件编译规则
$(OBJECTS): %.o: %.cpp $(DEPDIR)/%.d
	@echo "Compiling C++ $<..."
	$(CXX) $(CXXFLAGS) $(DEPFLAGS) -c $< -o $@

# 链接最终可执行文件
$(EXEC): $(OBJECTS) $(SOEM_LIB)
	@echo "Linking $@..."
	$(CXX) -o $@ $^ $(LDFLAGS)

# 创建依赖文件
$(DEPDIR)/%.d: ;
.PRECIOUS: $(DEPDIR)/%.d

# 包含依赖文件
-include $(patsubst %,$(DEPDIR)/%.d,$(basename $(SOURCES)))
-include $(patsubst %,$(DEPDIR)/%.d,$(basename $(ALL_SOEM_OBJS)))

# 清理编译文件
clean:
	@echo "Cleaning..."
	rm -f $(OBJECTS) $(EXEC) $(ALL_SOEM_OBJS) $(SOEM_LIB) *.hpp
	rm -rf $(DEPDIR)
	rm -rf $(BUILD_DIR)

# 显示编译标志
show-flags:
	@echo "Compiler: $(CXX)"
	@echo "CXXFLAGS: $(CXXFLAGS)"
	@echo "INCLUDE_FLAGS: $(INCLUDE_FLAGS)"
	@echo "DEFINES: $(DEFINES)"
	@echo "LDFLAGS: $(LDFLAGS)"

# 调试目标
debug:
	@echo "Source files: $(SOURCES)"
	@echo "Object files: $(OBJECTS)"
	@echo "SOEM objects: $(SOEM_OBJS)"
	@echo "OSAL objects: $(OSAL_OBJS)"
	@echo "OSHW objects: $(OSHW_OBJS)"
	@echo "Include flags: $(INCLUDE_FLAGS)"
