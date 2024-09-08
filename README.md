# NCSLabLink
此项目是NCSLab/M2PLab工程下负责提供虚拟仿真、代码生成的后台服务，用于替代matlab/simulink。

## Requirements
此项目基于maven框架，需要安装如下组件，推荐使用`idea`进行开发部署。
1. Java 1.8
2. maven 3.1
3. lombok.jar 1.18.4
4. mysql-connector-java
5. json 20230227
6. JUnit 4.13.2 单元测试用

## Installment
采用tomcat部署服务

## Development


## Deployment

## Features
目前支持的平台有
- Windows
- Linux (Ubuntu)
- Embeded Linux (Raspberry、Loongarch)

## 根目录
- `pom.xml` - Maven项目的配置文件
- `README.md` - 项目说明文件
- `src/` - 源代码目录
    - `block/` - 包含模块代码
    - `line/` - 包含连线代码
    - `main/` - 包含主程序代码
        - `database/` - 数据库接口代码
            - `Algorithms` 算法表接口
        - `resources/` - 资源文件
            - `application.properties` - 应用配置文件
    - `test/` - 包含测试代码
        - `java/` - 测试源代码
            - `code.c/` - 测试包结构
            - `servlet` - servlet测试结构
                - `CompileTest.java` - 测试编译器配置
        - `resources/` - 测试资源文件
            - `mlsCompile.json` - 磁悬浮系统的编译资源文件

## 构建目录
- `target/` - 构建输出目录
    - `classes/` - 编译后的类文件
    - `test-classes/` - 编译后的测试类文件
    - `surefire-report/` - 测试报告
    - `original-*.jar` - 未打包的JAR文件
    - `*.war` - 如果项目打包为WAR

## 文档和报告
- `docs/` - 项目文档
- `reports/` - 项目报告，例如代码覆盖率报告

## 其他文件和目录
- `.gitignore` - 指定Git要忽略的文件和文件夹
- `LICENSE` - 项目许可证
- `scripts/` - 存放脚本文件，如部署脚本

## Windows部署须知
### C语言实时程序编译
1. 需修改文件`src/utils/config/properties`中属性`CCodePathWin`，且保证文件夹存在
2. 在网上下载[MinGW](https://github.com/niXman/mingw-builds-binaries/releases)
3. 下载文件为压缩包，需要解压缩，并将mingw-w64所在路径(mingw64\bin)加入环境变量Path中，
4. 在命令行输入命令`mingw32-make --version`，显示mingw的版本号即说明环境配置成功
5. 安装octave包（可选项）
6. 在文件`src/META-INF/persistence.xml`中配置数据库的账号密码
7. 重启idea或其他ide即可顺利编译

## Linux部署须知
### C语言实时程序编译
#### Loongarch
1. 需修改文件`src/utils/config/properties`中属性`CCodePathLoong`，且保证文件夹存在
2. 在网上下载[loongarch64-clfs-8.0-cross-tools-gcc-full]
3. 下载文件为压缩包，需要解压缩，并将所在路径(cross-tools\bin)加入环境变量`Path`中，
同时添加环境变量`CROSS_COMPILE`，如下所示,修改完输入`source .bashrc`或者重启电脑生效
# M2PLink
[TOC]
## Introduction
NCSLabLink, or M2PLink, is a tool that compile block diagram to executable c++ code which can be used to simulate or run on the Raspberry Pi.

## Usage
```shell
$ maven clean
$ maven compile
$ maven package
```

deploy packaged war on Tomcat 8.5

## Environment
### M2PLink Environment Configuration for Linux
> Based on Ubuntu 22.04 and VS com.ncslab.code.

Configuration in deepin will be similar, because both of them are based on *Debian* and use the same package manager "apt".

Please follow basic development principle and code style.

**Encounter some problems? Restart your computer first...**
#### 0. Preparation
```shell
sudo apt install git
```

#### 1. Java
```shell
$ sudo apt install openjdk-8-jdk
$ sudo apt install maven
```
If you can normally reach google.com, twitter.com .etc, then everything is OK. If not, please modify the "settings.xml" of maven.

Here is a reference:
```xml
<mirrors>
<!-- alicloud -->
<mirror>
    <id>alimaven</id>
    <mirrorOf>central</mirrorOf>
    <name>aliyun maven</name>
    <url>http://maven.aliyun.com/nexus/content/repositories/central/</url>
</mirror>
</mirrors>

<profiles>
<profile>
    <id>jdk-1.8</id>
    <activation>
	    <activeByDefault>true</activeByDefault>
        <jdk>1.8</jdk>
    </activation>

    <properties>
        <maven.compiler.source>1.8</maven.compiler.source>
        <maven.compiler.target>1.8</maven.compiler.target>
        <maven.compiler.compilerVersion>1.8</maven.compiler.compilerVersion>
    </properties>
</profile>
</profiles>
```

If you use vs code as your IDE, and choose plugin "Community Server Connector" to deploy your tomcat, then you must also install a higher version JDK.

e.g. `$ sudo apt install openjdk-17-jdk`

then use `$ sudo update-alternative --config java` to select openjdk8 as your default jdk.

#### 2. C++
2.1 Install toolchains
```shell
$ sudo apt install build-essential
# snap, not apt!
$ sudo snap install cmake
# if you like vs code
$ sudo snap install code
```

p.s. out-of-source build is highly recommended for cmake, i.e. create a build folder in the root directory and run cmake in the build folder.

2.2 install vcpkg

- vcpkg
We use vcpkg to manage the dependencies. Refer to the [official website](https://github.com/microsoft/vcpkg).

```shell
git clone https://github.com/microsoft/vcpkg
./vcpkg/bootstrap-vcpkg.sh
```

(Optional) You can create an alias to use vcpkg more conveniently.
```sh
# Assume that you have installed vcpkg in the home directory
# bash
echo "alias vcpkg='~/vcpkg/vcpkg'" >> ~/.bashrc
# zsh (default shell in macOS)
echo "alias vcpkg='sudo ~/vcpkg/vcpkg'" >> ~/.zshrc

source ~/.bashrc
# source ~/.zshrc
```

2.3 Install libraries

- nlohmann/json
If you have install nlohmann-json with "cmake install", you can skip this step.
```sh
vcpkg install nlohmann-json
```

- eigen3
A C++ template library for linear algebra.
```sh
vcpkg install eigen3
export PATH=cross-tools/bin:$PATH
export LD_LIBRARY_PATH=cross-tools/lib:$LD_LIBRARY_PATH
export CROSS_COMPILE=loongarch64-linux-
```
4. 在命令行输入命令`loongarch64-unknown-linux-gnu-gcc --version`，显示编译器的版本号即说明环境配置成功
5. 安装octave包（可选项）
6. 在文件`src/META-INF/persistence.xml`中配置数据库的账号密码
7. 重启idea或其他ide即可顺利编译

#### Raspberry
#### 3. Nginx
Reverse proxy

## 单元测试
### Windows
1. 运行`test.java.servelt.CompileTest`的测试用例`testCompileWithJSONWindows`
3.0 Installation

### Loong
1. 运行`test.java.servelt.CompileTest`的测试用例`testCompileWithJSONLinuxLoong`
```shell
$ sudo apt install nginx
# Verify that the installation was successful
$ nginx -v
```


3.1

Edit nginx config file

```shell
$ sudo vim /etc/nginx/nginx.conf
# you can also use vs code to edit the file
$ code /etc/nginx/nginx.conf
```

Add such content in http block in "nginx.conf".

change the root path to actual path of folder in your computer.

```shell
server {
		listen 8100;
		location ^~/CCode/{
			root /home/square/ncslablink/;
		}
	}
```

3.2

Try to visit a file in the proxy folder

For example, create a "test.txt" in "CCode" folder, then type `IP:port/CCode/test.txt` in your browser to visit the test file. If you can see its content, CONGRATULATIONS!

If you encounter **"403 Forbidden"**, there are two ways to solve it (The first one is strongly **not** recommended. It is unsafe and make you omit some permission problems):
- directly change the first line of "nginx.conf" from `user xxx` to `user root`

- modify the permission of target folder and all its father folder to "755"

```shell
sudo chmod -R 755 <TARGET_FOLDER>
```

3.3

If you use "ufw" to manage the firewall, you need to open corresponding port. Otherwise just ignore following commands.
```shell
$ sudo ufw status
// open port 8100 & 8060
$ sudo ufw allow 8100
$ sudo ufw allow 8060
// restart nginx, you can also restart your computer, then omit follow operations
// check nginx, by default, nginx is active and will auto-start at login
$ sudo systemctl status nginx
$ sudo systemctl stop nginx
$ sudo systemctl start nginx
```

#### 4. Tomcat
change the port of Tomcat to 8060

### M2PLink Environment Configuration for MacOS
Do it your self.

I am a macOS user, but I dont't want to write anything about it. When choosing Mac, presumably you are familar with environment configuration, otherwise you can't survive in SUSTech.

Homebrew can solve most of your problems.

If you use homebrew to install nginx, its config file is located in "/opt/homebrew/etc/nginx/nginx.conf"

### M2PLink Environment Configuration for Windows
Do it your self, ditto.

## Code

### Java Part

Use Maven to clean, compile and package the project. Then right click "ncslablink-1.0-SNAPSHOT" (in target folder) and select "run on server".


## Contributing
- HU Wenshan
- XIA Zhiqiang
- ZHONG Wuzizheng
- DONG Jinda
- JU Xinyan
