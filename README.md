# NCSLabLink
此项目是NCSLab/M2PLab工程下负责提供虚拟仿真、代码生成的后台服务，用于替代matlab/simulink。

## Requirements
此项目基于maven框架，需要安装如下组件，推荐使用idea进行开发部署。
1. Java 1.8
2. maven 3.1
3. lombok.jar 1.18.4
4. mysql-connector-java
5. json 20230227

## Installment
采用tomcat部署服务

## Development
目前

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
        - `java/` - Java源代码
            - `com.example/` - 包结构
                - `App.java` - 主应用程序类
                - `Config.java` - 配置类
        - `resources/` - 资源文件
            - `application.properties` - 应用配置文件
    - `test/` - 包含测试代码
        - `java/` - 测试源代码
            - `com.example/` - 测试包结构
                - `AppTest.java` - 应用程序的单元测试类
        - `resources/` - 测试资源文件

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