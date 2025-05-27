# NCSLabLink
此项目是NCSLab/M2PLab工程下负责提供虚拟仿真、代码生成的后台服务，用于替代matlab/simulink。

NCSLabLink, or M2PLink, is a tool that compile block diagram to executable c++ code which can be used to simulate or run on the Raspberry Pi.

**支持平台**

| 平台             | 状态  |
|----------------|-------|
| Windows        | ![Windows](https://img.shields.io/badge/build-passing-brightgreen.svg) |
| Ubuntu 22.04   | ![Linux](https://img.shields.io/badge/build-passing-brightgreen.svg) |
| Raspberry Pi   | ![Raspberry](https://img.shields.io/badge/build-passing-brightgreen.svg) |
| Loongarch-华龙定制 | ![Loongarch](https://img.shields.io/badge/build-passing-brightgreen.svg) |


## Usage
```shell
$ maven clean
$ maven compile
$ maven package
```
deploy packaged war on Tomcat 8.5

## Installment(Ubuntu22.04-based)
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
##### 2.1 Install toolchains
```shell
$ sudo apt install build-essential
# snap, not apt!
$ sudo snap install cmake
# if you like vs code
$ sudo snap install code
```

p.s. out-of-source build is highly recommended for cmake, i.e. create a build folder in the root directory and run cmake in the build folder.

##### 2.2 install vcpkg

- vcpkg
  We use vcpkg to manage the dependencies. Refer to the [official website](https://github.com/microsoft/vcpkg).

```shell
git clone https://github.com/microsoft/vcpkg
cd vcpkg
./bootstrap-vcpkg.sh #(or ./bootstrap.vcpkg.bat instead in Windows)
```

(Optional in Linux) You can create an alias to use vcpkg more conveniently.
```sh
# Assume that you have installed vcpkg in the home directory
# bash
echo "alias vcpkg='~/vcpkg/vcpkg'" >> ~/.bashrc
# zsh (default shell in macOS)
echo "alias vcpkg='sudo ~/vcpkg/vcpkg'" >> ~/.zshrc

source ~/.bashrc
# source ~/.zshrc
```

##### 2.3 Install libraries

- nlohmann/json
  If you have install nlohmann-json with "cmake install", you can skip this step.
```shell
vcpkg install nlohmann-json
```

- eigen3
  A C++ template library for linear algebra.
```shell
vcpkg install eigen3
```

#### 3. Nginx
Reverse proxy

```shell
$ sudo apt install nginx
# Verify that the installation was successful
$ nginx -v
```
##### 3.1 Edit nginx config file

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

##### 3.2 Try to visit a file in the proxy folder

For example, create a "test.txt" in "CCode" folder, then type `IP:port/CCode/test.txt` in your browser to visit the test file. If you can see its content, CONGRATULATIONS!

If you encounter **"403 Forbidden"**, there are two ways to solve it (The first one is strongly **not** recommended. It is unsafe and make you omit some permission problems):
- directly change the first line of "nginx.conf" from `user xxx` to `user root`

- modify the permission of target folder and all its father folder to "755"

```shell
sudo chmod -R 755 <TARGET_FOLDER>
```

##### 3.3 Remove ufw restriction

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

## 根目录
- `pom.xml` - Maven项目的配置文件
- `README.md` - 项目说明文件
- `src/` - 源代码目录
    - `main/` - 包含主程序代码
        - `java/` - 主程序代码
            - `com.ncslab` ncslab所有代码
        - `resources/` - 资源文件
            - `log4j.properties` - logger应用配置文件
            - `mybatis-config.xml` - Mybatis数据库配置
    - `test/` - 包含测试代码
        - `java/` - 测试源代码
            - `code.c/` - 测试包结构
            - `servlet` - servlet测试结构
                - `CompileTest.java` - 测试网络请求编译
        - `resources/` - 测试资源文件
            - `mlsCompile.json` - 磁悬浮系统的编译资源文件
    - `others/` - 包含其他代码

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

## 注意事项
### C语言实时程序编译
Link统一采用`make`作为编译工具，如果要采用其他的，需覆写方法`makeExeFile`。由于存在交叉编译的需求，区分编译环境（`host`）和运行环境（`target`），
- Link运行的操作系统定义为`host`，可选值包括`Windows`和`Linux`
- 目标程序的运行的操作系统定义为`target`，可选值包括`PC`（表示和Link运行环境一致）,`Raspberry`,`Loongarch`

如果需要交叉编译新架构的程序，自行增加符合命名规则的类即可。编译依赖项包括
- Eigen
- Nolhman/json
#### Windows
目前Windows支持的目标机环境包括`PC`和`Raspberry`。
##### PC
1. 需修改文件`src/main/java/utils/config/properties`中属性`CCodePathWin`，且保证文件夹存在
2. 在网上下载[MinGW](https://github.com/niXman/mingw-builds-binaries/releases)
3. 下载文件为压缩包，需要解压缩，并将mingw-w64所在路径(mingw64\bin)加入环境变量Path中，
4. 在命令行输入命令`mingw32-make --version`，显示mingw的版本号即说明环境配置成功
5. 安装octave包（可选项）
6. 在文件`src/main/resources/mybatis-config.xml`中配置数据库的账号密码
7. 重启`idea`或其他`ide`即可顺利编译

##### Raspberry Pi(TODO)
编译树莓派程序需要使用交叉编译工具链，但工具链最低版本9.2对应的`glibc`版本为2.29，而现有设备支持的`glibc`版本为2.28，因此需进行升级。

1. 下载交叉编译工具链
2.

#### Ubuntu(TODO)

##### PC


##### Raspberry Pi(TODO)

##### Loongarch
1. 需修改文件`src/main/java/utils/config/properties`中属性`CCodePathLoong`，且保证文件夹存在
2. 在网上下载[loongarch64-clfs-8.0-cross-tools-gcc-full]
3. 下载文件为压缩包，需要解压缩，并将所在路径(cross-tools\bin)加入环境变量`Path`中，
同时添加环境变量`CROSS_COMPILE`，如下所示,修改完输入`source .bashrc`或者重启电脑生效
```shell
export PATH=cross-tools/bin:$PATH
export LD_LIBRARY_PATH=cross-tools/lib:$LD_LIBRARY_PATH
export CROSS_COMPILE=loongarch64-linux-
```
4. 在命令行输入命令`loongarch64-unknown-linux-gnu-gcc --version`，显示编译器的版本号即说明环境配置成功
5. 安装octave包（可选项）
6. 在文件`src/main/resources/mybatis-config.xml`中配置数据库的账号密码
7. 重启`idea`或其他`ide`即可顺利编译

## 单元测试[TODO]
若成功通过所有测试，则表示版本merge没有问题。

以代码生成为例，若生成可执行文件`CCode\35\8078\ncslab`，并插入到数据库中，则表示测试通过。
### Windows
```shell
maven test -Dtest=com.ncslab.WindowsTest
```

### Loongarch
```shell
maven test -Dtest=com.ncslab.LinuxLoongarchTest
```

### 公共模块
```shell
maven test -Dtest=com.ncslab.PublicTest
```

## Contributing
The implementation of this project is inseparable from the contributions of the following contributors.

- Wuhan University: HU Wenshan, XIA Zhiqiang, ZHOU Xingwei, YE Shengwang
- Southern University of Science and Technology: ZHONG Wuzizheng, DONG Jinda, JU Xinyan, WANG Xiangxian, WANG Jingxu
- North China University of Technology: ZHOU keying

and others.

Please refer to our [maintainer guide](https://docs.qq.com/doc/DQXpFS1BwUFFIdmll) and packaging tutorial for more details.
