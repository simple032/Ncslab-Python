# NCSLabLink (M2PLink) - 中文版

## 项目概述

NCSLabLink（也称为M2PLink）是一个基于Java的Web应用程序，提供虚拟仿真和代码生成服务，旨在替代MATLAB/Simulink的功能。它将块图编译为可执行代码，用于仿真或部署到各种平台，包括嵌入式系统。

## 主要特性

- **基于块的建模系统**: 150+种预定义仿真块，涵盖连续/离散系统、数学运算、逻辑控制、硬件驱动等
- **多语言代码生成**: 支持C++、MATLAB、结构化文本(ST)、RAPID(ABB机器人)等多种目标语言
- **跨平台支持**: Windows、Linux、树莓派、龙芯等多平台交叉编译
- **模板驱动架构**: 使用Apache Velocity模板引擎，提高代码生成的可维护性
- **实时仿真**: 通过WebSocket支持实时数据传输和硬件在环测试
- **许可证管理**: 集成的许可证密钥验证系统
- **外部工具集成**: 支持Python、Octave、MFCalc等外部仿真引擎

## 系统架构

### 核心组件

1. **块系统** (`com.ncslab.block`)
   - 模块化的基于块的建模系统
   - 支持连续、离散、数学、逻辑、硬件驱动等多种块类型
   - 每个块使用Velocity模板生成目标代码

2. **模型处理** (`com.ncslab.ncslablink`)
   - 解析JSON格式的块图
   - 处理仿真执行和S-Function编译

3. **代码生成** (`com.ncslab.code`)
   - 支持多种编程语言的代码生成
   - 平台特定的实现优化
   - 基于模板的生成流程

4. **电路仿真** (`com.ncslab.circuit`)
   - 电气电路建模和仿真
   - 代数环检测和解析

5. **Web接口** (`com.ncslab.servlet`, `com.ncslab.websocket`)
   - RESTful API端点
   - WebSocket实时通信
   - 与外部服务器集成

## 支持平台

| 平台             | 状态  |
|----------------|-------|
| Windows        | ![Windows](https://img.shields.io/badge/build-passing-brightgreen.svg) |
| Ubuntu 22.04   | ![Linux](https://img.shields.io/badge/build-passing-brightgreen.svg) |
| Raspberry Pi   | ![Raspberry](https://img.shields.io/badge/build-passing-brightgreen.svg) |
| Loongarch-华龙定制 | ![Loongarch](https://img.shields.io/badge/build-passing-brightgreen.svg) |

## 安装和配置

### 系统要求

- **Java**: OpenJDK 17或更高版本
- **数据库**: MySQL 5.7.33或SQLite 3.44.1.0
- **Web服务器**: Apache Tomcat 10.1.16
- **构建工具**: Maven 3.6.3+

### 依赖库

- C++编译器: Windows需要MinGW，Linux需要GCC
- vcpkg: 用于管理C++依赖(Eigen3, nlohmann/json)
- 具体依赖请参见`pom.xml`

### 构建步骤

```bash
# 清理项目
mvn clean

# 编译项目
mvn compile

# 打包项目（生成WAR文件）
mvn package

# 运行测试
mvn test
```

### 安全配置（必需）

⚠️ **重要**: 部署前，您必须设置以下环境变量以确保安全性：

```bash
# 必需 - NCSLab数据库密码（为安全起见无默认值）
export NCSLAB_DB_PASSWORD="您的安全数据库密码"

# 可选 - NCSLab数据库连接详细信息（有安全的默认值）
export NCSLAB_DB_URL="jdbc:mysql://localhost:3306/ncslab?useSSL=true&characterEncoding=utf8&serverTimezone=UTC"
export NCSLAB_DB_USERNAME="ncslab_user"
```

**Windows 命令提示符:**
```cmd
set NCSLAB_DB_PASSWORD=您的安全数据库密码
set NCSLAB_DB_URL=jdbc:mysql://localhost:3306/ncslab?useSSL=true^&characterEncoding=utf8^&serverTimezone=UTC
set NCSLAB_DB_USERNAME=ncslab_user
```

**Windows PowerShell:**
```powershell
$env:NCSLAB_DB_PASSWORD="您的安全数据库密码"
$env:NCSLAB_DB_URL="jdbc:mysql://localhost:3306/ncslab?useSSL=true&characterEncoding=utf8&serverTimezone=UTC"
$env:NCSLAB_DB_USERNAME="ncslab_user"
```

### 部署

1. **设置必需的环境变量**（见上面的安全配置）
2. 将生成的WAR文件部署到Tomcat的webapps目录
3. 配置`config.properties`中的路径设置
4. ~~配置`mybatis-config.xml`中的数据库连接~~（现在使用环境变量）
5. 启动Tomcat服务器

## 安全特性

此应用程序包含全面的安全增强功能：

- **数据库安全**: 基于环境变量的配置（无硬编码凭据）
- **WebSocket安全**: 输入验证、速率限制、恶意内容检测
- **错误处理**: 安全的错误消息，不暴露内部详细信息
- **会话管理**: 正确的WebSocket会话生命周期管理

有关详细的安全信息，请参阅 `SECURITY_IMPROVEMENTS.md`。

## 使用指南

### API端点

- **编译**: `/servlet/compile` - 编译模型为目标代码
- **仿真**: `/servlet/simulate` - 运行仿真
- **实时仿真**: WebSocket端点用于实时数据传输
- **外部工具**: `/servlet/python`, `/servlet/octave`, `/servlet/mfcalc`

### 支持的块类型

- **连续系统**: 积分器、微分器、PID控制器、传输延迟
- **离散系统**: 单位延迟、离散积分器、延迟块
- **数学运算**: 加法、乘法、增益、三角函数、矩阵运算
- **逻辑和位运算**: 关系运算符、逻辑运算、位操作
- **信号源**: 常数、时钟、斜坡、阶跃、重复序列
- **信号接收**: 示波器、输出端口
- **硬件驱动**: 直流电机、磁悬浮系统

## 开发指南

### 添加新块

1. 在相应的`com.ncslab.block`包中创建新的块类
2. 在`BlockType`枚举中注册新块类型
3. 创建对应的Velocity模板文件
4. 实现必要的代码生成逻辑
5. 添加单元测试验证功能

### 模板开发

- 所有块应使用Velocity模板而非字符串拼接
- 模板存储在`src/main/resources/templates/`
- 使用`TemplateUtils`进行上下文管理
- 遵循`TEMPLATE_IMPROVEMENTS.md`中的最佳实践

### 测试策略

- **单元测试**: 验证单个块的行为和数学精度
- **集成测试**: 验证完整的编译管道
- **平台特定测试**: 确保交叉编译正常工作
- **性能测试**: 测量编译速度和仿真精度

## 详细安装指南

### 0. 准备工作
```shell
sudo apt install git
```

### 1. Java
```shell
$ sudo apt install openjdk-8-jdk
$ sudo apt install maven
```
如果您可以正常访问google.com、twitter.com等网站，那么一切都没问题。如果不能，请修改maven的"settings.xml"。

参考配置：
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

如果您使用VS Code作为IDE，并选择"Community Server Connector"插件来部署Tomcat，那么您还必须安装更高版本的JDK。

例如：`$ sudo apt install openjdk-17-jdk`

然后使用`$ sudo update-alternative --config java`选择openjdk8作为默认jdk。

### 2. C++
#### 2.1 安装工具链
```shell
$ sudo apt install build-essential
# 使用snap，不是apt！
$ sudo snap install cmake
# 如果您喜欢vs code
$ sudo snap install code
```

建议cmake使用out-of-source构建，即在根目录中创建build文件夹并在build文件夹中运行cmake。

#### 2.2 安装vcpkg

- vcpkg
  我们使用vcpkg来管理依赖项。参考[官方网站](https://github.com/microsoft/vcpkg)。

```shell
git clone https://github.com/microsoft/vcpkg
cd vcpkg
./bootstrap-vcpkg.sh #(在Windows中使用./bootstrap.vcpkg.bat)
```

（Linux中可选）您可以创建一个别名来更方便地使用vcpkg。
```sh
# 假设您已在home目录中安装了vcpkg
# bash
echo "alias vcpkg='~/vcpkg/vcpkg'" >> ~/.bashrc
# zsh (macOS中的默认shell)
echo "alias vcpkg='sudo ~/vcpkg/vcpkg'" >> ~/.zshrc

source ~/.bashrc
# source ~/.zshrc
```

#### 2.3 安装库

- nlohmann/json
  如果您已经使用"cmake install"安装了nlohmann-json，可以跳过此步骤。
```shell
vcpkg install nlohmann-json
```

- eigen3
  线性代数的C++模板库。
```shell
vcpkg install eigen3
```

### 3. Nginx
反向代理

```shell
$ sudo apt install nginx
# 验证安装是否成功
$ nginx -v
```
#### 3.1 编辑nginx配置文件

```shell
$ sudo vim /etc/nginx/nginx.conf
# 您也可以使用vs code编辑文件
$ code /etc/nginx/nginx.conf
```

在"nginx.conf"的http块中添加此类内容。

将root路径更改为您计算机中文件夹的实际路径。

```shell
server {
		listen 8100;
		location ^~/CCode/{
			root /home/square/ncslablink/;
		}
	}
```

#### 3.2 尝试访问代理文件夹中的文件

例如，在"CCode"文件夹中创建"test.txt"，然后在浏览器中输入`IP:port/CCode/test.txt`来访问测试文件。如果您能看到内容，恭喜！

如果遇到**"403 Forbidden"**，有两种解决方法（强烈不建议第一种。它不安全，会让您忽略一些权限问题）：
- 直接将"nginx.conf"的第一行从`user xxx`改为`user root`

- 将目标文件夹及其所有父文件夹的权限修改为"755"

```shell
sudo chmod -R 755 <TARGET_FOLDER>
```

#### 3.3 移除ufw限制

如果您使用"ufw"管理防火墙，需要开放相应端口。否则忽略以下命令。
```shell
$ sudo ufw status
// 开放端口8100和8060
$ sudo ufw allow 8100
$ sudo ufw allow 8060
// 重启nginx，您也可以重启计算机，然后忽略后续操作
// 检查nginx，默认情况下，nginx是活动的并会在登录时自动启动
$ sudo systemctl status nginx
$ sudo systemctl stop nginx
$ sudo systemctl start nginx
```

## 项目结构

### 根目录
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

### 构建目录
- `target/` - 构建输出目录
    - `classes/` - 编译后的类文件
    - `test-classes/` - 编译后的测试类文件
    - `surefire-report/` - 测试报告
    - `original-*.jar` - 未打包的JAR文件
    - `*.war` - 如果项目打包为WAR

### 文档和报告
- `docs/` - 项目文档
- `reports/` - 项目报告，例如代码覆盖率报告

### 其他文件和目录
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
6. ~~在文件`src/main/resources/mybatis-config.xml`中配置数据库的账号密码~~（现在使用环境变量）
7. 重启`idea`或其他`ide`即可顺利编译

##### Raspberry Pi(TODO)
编译树莓派程序需要使用交叉编译工具链，但工具链最低版本9.2对应的`glibc`版本为2.29，而现有设备支持的`glibc`版本为2.28，因此需进行升级。

1. 下载交叉编译工具链
2. (待完成)

#### Ubuntu(TODO)

##### PC
(待完成)

##### Raspberry Pi(TODO)
(待完成)

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
6. ~~在文件`src/main/resources/mybatis-config.xml`中配置数据库的账号密码~~（现在使用环境变量）
7. 重启`idea`或其他`ide`即可顺利编译

## 单元测试

若成功通过所有测试，则表示版本merge没有问题。

### Windows测试
```bash
mvn test -Dtest=com.ncslab.WindowsTest
```

### Loongarch测试
```bash
mvn test -Dtest=com.ncslab.LinuxLoongarchTest
```

### 公共模块测试
```bash
mvn test -Dtest=com.ncslab.PublicTest
```

## 贡献指南

1. Fork项目仓库
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 创建Pull Request

## 许可证

本项目使用专有许可证。使用前请联系项目维护者获取许可。

## 联系方式

- 项目主页: [NCSLabLink](https://github.com/ncslab/ncslablink)
- 问题反馈: 请在GitHub Issues中报告

## 贡献者

本项目的实现离不开以下贡献者的努力：

- 武汉大学: 胡文山、夏志强、周兴伟、叶圣旺
- 南方科技大学: 钟五子正、董进达、鞠欣艳、王向贤、王靖旭
- 北方工业大学: 周可莹

以及其他贡献者。

更多详情请参阅我们的[维护者指南](https://docs.qq.com/doc/DQXpFS1BwUFFIdmll)和打包教程。