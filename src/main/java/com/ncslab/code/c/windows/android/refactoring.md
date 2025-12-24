# Android适配工作

## 重定向HTTP请求

1. 分别设置两个新的servlet用于仿真和编译 android_compile 和 android_simulate
2. 修改App的请求目标地址到本机

### 目的

获取真实的HTTP请求包内容，以便于编写对应的DTO文件

## 编写DTO

1. 缺少saveInfo和option导致错误
2. Scope模块映射到Out模块导致编译不了
3. 似乎不支持多个Scope

## 迁移MATLAB

### MATLAB测试

1. 配置MATLAB的mex功能

```m
setenv('MW_MINGW64_LOC', 'D:\Software\mingw64');  % 替换为实际路径
mex -setup
```

```md
MEX 配置为使用 'Microsoft Visual C++ 2022 (C)' 以进行 C 语言编译。

要选择不同的 C 编译器，请从以下选项中选择一种命令:
MinGW64 Compiler (C)  mex -setup:'C:\Program Files\MATLAB\R2024a\bin\win64\mexopts\mingw64.xml' C
Microsoft Visual C++ 2019 (C)  mex -setup:'C:\Program Files\MATLAB\R2024a\bin\win64\mexopts\msvc2019.xml' C
Microsoft Visual C++ 2022 (C)  mex -setup:C:\Users\Sheng\AppData\Roaming\MathWorks\MATLAB\R2024a\mex_C_win64.xml C

要选择不同的语言，请从以下选项中选择一种命令:
 mex -setup C++ 
 mex -setup FORTRAN
已将 options 文件 'C:\Users\Sheng\AppData\Roaming\MathWorks\MATLAB\R2024a\mex_C_win64.xml' 重命名为 'C:\Users\Sheng\AppData\Roaming\MathWorks\MATLAB\R2024a\mex_C_win64_backup.xml'。
MEX 配置为使用 'MinGW64 Compiler (C)' 以进行 C 语言编译。
```

3. 配置MATLAB server， 但是无法监听 192.168.131.1的9800端口，其他均正常

测试命令

```sh
curl -X POST "http://127.0.0.1:9800/api/task/cmp" \
  -H "Content-Type: application/json" \
  -d '{
    "userName": "user1",
    "modelName": "Unnamed",
    "config": {
      "FixedStep": "auto",
      "Solver": "FixedStepDiscrete",
      "StartTime": "0.0",
      "StopTime": "20.0",
      "MaxDataPoints": "1000",
      "SystemTargetFile": "grt_Android.tlc",
      "TemplateMakefile": "grt_Android.tmf",
      "NetConIPAddress": "127.0.0.1"
    },
    "canvas": {
      "width": "1000",
      "height": "1000"
    },
    "blocks": [
      {
        "blockType": "Switch",
        "srcBlock": "simulink/Signal Routing/Switch",
        "blockName": "Switch",
        "paramValues": {
          "Threshold": "0",
          "Criteria": "u2 >= Threshold",
          "SaturateOnIntegerOverflow": "on"
        },
        "shape": {
          "x": 249,
          "y": 135,
          "angle": 0,
          "width": 100,
          "height": 100
        },
        "Position": "[249, 135,349,235]",
        "ports": []
      }
    ],
    "lines": [      
    ]
  }'
```

4. 设置NDK环境变量，指向`D:\Documents\Deliverables\matlab_setups\android-toolchain-16_win`

### tlc/tmf

1. 已经编译成功了
2. 但是无法下载成功

失败的消息结构

```json
{"code":4000,"message":"找不到模板联编文件: grt_Android.tmf","modelName":"Unnamed","data":null}
```

成功的消息结构

```json
{"code":2000,"message":"SUCCESS","modelName":"Unnamed","data":{"binaryFileUrl":"files\\user1\\Unnamed.bin"}}
```

### 调试过程

#### 版本问题

1. ndk版本和gcc版本过旧
2. adb版本过旧
3. 缺乏openmp库
4. 没有分配tty导致的缓冲

#### 安卓机调试

- 推送并运行文件

```bash
adb push C:\Users\Sheng\Documents\Projects\NCSLab\NCSLabLink\CCode\0\0\ncslab.exe /data/local/tmp/ncslab.exe
adb shell chmod 755 /data/local/tmp/ncslab.exe
adb shell /data/local/tmp/ncslab.exe
```

强制分配 TTY：

```bash
D:\Software\adb-platform-tools>adb shell -t /data/local/tmp/ncslab.exe
Current sample stepSize is 0.010000.
Server Thread starting...
Server Thread started...Ok
======waiting for client's request======
```

- 简单调试

```bash
adb logcat -c
adb shell /data/local/tmp/ncslab.exe
adb logcat -d | findstr "pc "  # 得到类似 "pc 000000000005e124" 的地址
D:\Software\android-ndk-r27d\toolchains\llvm\prebuilt\windows-x86_64\bin\llvm-addr2line.exe -e ncslab.exe 0x5e124 # 定位代码行
```

使用File命令查看文件类型

```sh
swye@HP-YSW:/mnt/d/Software/adb-platform-tools$ file /mnt/c/Users/Sheng/Documents/Projects/NCSLab/NCSLabLink/CCode/0/0/ncsla
b.exe
/mnt/c/Users/Sheng/Documents/Projects/NCSLab/NCSLabLink/CCode/0/0/ncslab.exe: ELF 64-bit LSB pie executable, ARM aarch64, version 1 (SYSV), dynamically linked, interpreter /system/bin/linker64, with debug_info, not stripped
```

-[ ] 复杂调试

1. 找到 lldb 客户端（电脑端）： D:\Software\android-ndk-r27d\toolchains\llvm\prebuilt\windows-x86_64\bin\lldb.exe
2. 推送 lldb-server 到设备（设备端调试服务）：

```bash
# 推送 arm64 版本的 lldb-server
adb push D:\Software\android-ndk-r27d\toolchains\llvm\prebuilt\windows-x86_64\lib64\clang\17\lib\linux\aarch64\lldb-server /data/local/tmp/
adb shell chmod 755 /data/local/tmp/lldb-server
```

3. 启动 lldb-server：

- 设备端启动 lldb-server：

```bash
adb shell "/data/local/tmp/lldb-server platform --listen *:1234 --server"
```

