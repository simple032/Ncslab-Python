# NCSLabLink (M2PLink)

## Project Overview

NCSLabLink (also known as M2PLink) is a Java-based web application that provides virtual simulation and code generation services, designed to replace MATLAB/Simulink functionality. It compiles block diagrams to executable code for simulation or deployment on various platforms including embedded systems.

## Key Features

- **Block-based Modeling System**: 150+ predefined simulation blocks covering continuous/discrete systems, math operations, logic control, hardware drivers, etc.
- **Multi-language Code Generation**: Supports C++, MATLAB, Structured Text (ST), RAPID (ABB robotics), and more
- **Cross-platform Support**: Windows, Linux, Raspberry Pi, Loongarch cross-compilation
- **Template-driven Architecture**: Uses Apache Velocity template engine for improved maintainability
- **Real-time Simulation**: WebSocket-based real-time data transmission and hardware-in-the-loop testing
- **License Management**: Integrated license key validation system
- **External Tool Integration**: Supports Python, Octave, MFCalc simulation engines

## System Architecture

### Core Components

1. **Block System** (`com.ncslab.block`)
   - Modular block-based modeling system
   - Supports continuous, discrete, math, logic, hardware driver blocks
   - Each block uses Velocity templates for code generation

2. **Model Processing** (`com.ncslab.ncslablink`)
   - Parses JSON block diagrams
   - Handles simulation execution and S-Function compilation

3. **Code Generation** (`com.ncslab.code`)
   - Multi-language code generation support
   - Platform-specific implementation optimizations
   - Template-based generation pipeline

4. **Circuit Simulation** (`com.ncslab.circuit`)
   - Electrical circuit modeling and simulation
   - Algebraic loop detection and resolution

5. **Web Interface** (`com.ncslab.servlet`, `com.ncslab.websocket`)
   - RESTful API endpoints
   - WebSocket real-time communication
   - External server integration

## Supported Platforms

| Platform       | Status |
|----------------|--------|
| Windows        | ![Windows](https://img.shields.io/badge/build-passing-brightgreen.svg) |
| Ubuntu 22.04   | ![Linux](https://img.shields.io/badge/build-passing-brightgreen.svg) |
| Raspberry Pi   | ![Raspberry](https://img.shields.io/badge/build-passing-brightgreen.svg) |
| Loongarch      | ![Loongarch](https://img.shields.io/badge/build-passing-brightgreen.svg) |

## Installation and Configuration

### System Requirements

- **Java**: OpenJDK 17 or higher
- **Database**: MySQL 8.0.33 or SQLite 3.44.1.0
- **Web Server**: Apache Tomcat 10.1.16
- **Build Tool**: Maven 3.6.3+

### Dependencies

- C++ Compiler: MinGW for Windows, GCC for Linux
- vcpkg: For managing C++ dependencies (Eigen3, nlohmann/json)
- See `pom.xml` for detailed dependencies

### Build Steps

```bash
# Clean project
mvn clean

# Compile project
mvn compile

# Package project (creates WAR file)
mvn package

# Run tests
mvn test
```

### Security Configuration (REQUIRED)

⚠️ **CRITICAL**: Before deployment, you MUST set the following environment variables for security:

```bash
# REQUIRED - NCSLab database password (no default for security)
export NCSLAB_DB_PASSWORD="your_secure_database_password"

# OPTIONAL - NCSLab database connection details (have secure defaults)
export NCSLAB_DB_URL="jdbc:mysql://localhost:3306/ncslab?useSSL=true&characterEncoding=utf8&serverTimezone=UTC"
export NCSLAB_DB_USERNAME="ncslab_user"
```

**Windows Command Prompt:**
```cmd
set NCSLAB_DB_PASSWORD=your_secure_database_password
set NCSLAB_DB_URL=jdbc:mysql://localhost:3306/ncslab?useSSL=true^&characterEncoding=utf8^&serverTimezone=UTC
set NCSLAB_DB_USERNAME=ncslab_user
```

**Windows PowerShell:**
```powershell
$env:NCSLAB_DB_PASSWORD="your_secure_database_password"
$env:NCSLAB_DB_URL="jdbc:mysql://localhost:3306/ncslab?useSSL=true&characterEncoding=utf8&serverTimezone=UTC"
$env:NCSLAB_DB_USERNAME="ncslab_user"
```

### Deployment

1. **Set required environment variables** (see Security Configuration above)
2. Deploy generated WAR file to Tomcat webapps directory
3. Configure path settings in `config.properties`
4. ~~Configure database connection in `mybatis-config.xml`~~ (Now uses environment variables)
5. Start Tomcat server

## Security Features

This application includes comprehensive security enhancements:

- **Database Security**: Environment variable-based configuration (no hardcoded credentials)
- **WebSocket Security**: Input validation, rate limiting, malicious content detection
- **Error Handling**: Secure error messages without internal details exposure
- **Session Management**: Proper WebSocket session lifecycle management

For detailed security information, see `SECURITY_IMPROVEMENTS.md`.

## Usage Guide

### API Endpoints

- **Compile**: `/servlet/compile` - Compile model to target code
- **Simulate**: `/servlet/simulate` - Run simulation
- **Real-time Simulation**: WebSocket endpoints for real-time data
- **External Tools**: `/servlet/python`, `/servlet/octave`, `/servlet/mfcalc`

### Supported Block Types

- **Continuous Systems**: Integrator, Derivative, PID Controller, Transport Delay
- **Discrete Systems**: Unit Delay, Discrete Integrator, Delay blocks
- **Math Operations**: Add, Multiply, Gain, Trigonometric, Matrix operations
- **Logic and Bit Operations**: Relational operators, Logic operations, Bit operations
- **Signal Sources**: Constant, Clock, Ramp, Step, Repeating Sequence
- **Signal Sinks**: Scope, Output ports
- **Hardware Drivers**: DC Motor, Magnetic Levitation System

## Development Guide

### Adding New Blocks

1. Create new block class in appropriate `com.ncslab.block` package
2. Register new block type in `BlockType` enum
3. Create corresponding Velocity template files
4. Implement necessary code generation logic
5. Add unit tests to verify functionality

### Template Development

- All blocks should use Velocity templates instead of string concatenation
- Templates stored in `src/main/resources/templates/`
- Use `TemplateUtils` for context management
- Follow best practices in `TEMPLATE_IMPROVEMENTS.md`

### Testing Strategy

- **Unit Tests**: Validate individual block behavior and mathematical accuracy
- **Integration Tests**: Verify complete compilation pipeline
- **Platform-specific Tests**: Ensure cross-compilation works correctly
- **Performance Tests**: Measure compilation speed and simulation accuracy

## Unit Testing

Run tests to verify the project is working correctly:

### Windows Tests
```bash
mvn test -Dtest=com.ncslab.WindowsTest
```

### Loongarch Tests
```bash
mvn test -Dtest=com.ncslab.LinuxLoongarchTest
```

### Common Module Tests
```bash
mvn test -Dtest=com.ncslab.PublicTest
```

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Create a Pull Request

## License

This project uses a proprietary license. Please contact the project maintainers for licensing information.

## Contact

- Project Homepage: [NCSLabLink](https://github.com/ncslab/ncslablink)
- Issue Tracking: Please report issues on GitHub Issues

## Contributors

The implementation of this project is inseparable from the contributions of the following contributors:

- Wuhan University: HU Wenshan, XIA Zhiqiang, ZHOU Xingwei, YE Shengwang
- Southern University of Science and Technology: ZHONG Wuzizheng, DONG Jinda, JU Xinyan, WANG Xiangxian, WANG Jingxu
- North China University of Technology: ZHOU Keying

and others.

Please refer to our [maintainer guide](https://docs.qq.com/doc/DQXpFS1BwUFFIdmll) and packaging tutorial for more details.