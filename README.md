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
> Based on Ubuntu 22.04 and VS Code.

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
```

#### 3. Nginx
Reverse proxy

3.0 Installation

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