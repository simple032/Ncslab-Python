#include "EtherCATAnalog.hpp"
#include "EtherCATDigital.hpp"
#include "EtherCATServo.hpp"

// 全局初始化标志
int ECAT_init_Flag = 0;

// 模拟量IO管理器的静态成员初始化
std::map<int, std::unique_ptr<EtherCATAnalogOutput>> AnalogIOManager::g_analogOutputs;
std::map<int, std::unique_ptr<EtherCATAnalogInput>> AnalogIOManager::g_analogInputs;

// 数字量IO管理器的静态成员初始化
std::map<std::string, std::unique_ptr<EtherCATDigitalOutput>> DigitalIOManager::g_digitalOutputs;
std::map<std::string, std::unique_ptr<EtherCATDigitalInput>> DigitalIOManager::g_digitalInputs;

// servoIO管理器的静态成员初始化
std::map<int, std::unique_ptr<EtherCATServo>> ServoManager::g_servos;
