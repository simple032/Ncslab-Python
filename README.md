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
```shell
export PATH=cross-tools/bin:$PATH
export LD_LIBRARY_PATH=cross-tools/lib:$LD_LIBRARY_PATH
export CROSS_COMPILE=loongarch64-linux-
```
4. 在命令行输入命令`loongarch64-unknown-linux-gnu-gcc --version`，显示编译器的版本号即说明环境配置成功
5. 安装octave包（可选项）
6. 在文件`src/META-INF/persistence.xml`中配置数据库的账号密码
7. 重启idea或其他ide即可顺利编译

#### Raspberry

## 单元测试
### Windows
1. 运行`test.java.servelt.CompileTest`的测试用例`testCompileWithJSONWindows`

### Loong
1. 运行`test.java.servelt.CompileTest`的测试用例`testCompileWithJSONLinuxLoong`