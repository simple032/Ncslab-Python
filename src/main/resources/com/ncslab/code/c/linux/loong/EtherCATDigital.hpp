#ifndef ETHERCAT_DIGITAL_HPP
#define ETHERCAT_DIGITAL_HPP

#include <memory>
#include <map>
#include <string>
#include <cstdint>
#include <cstring>
#include "EtherCAT.hpp"

extern int ECAT_init_Flag;

class EtherCATDigitalOutput {
public:

    explicit EtherCATDigitalOutput(int slave_id, int channel) : slave_id_(slave_id), channel_(channel) {
        printf("Creating EtherCAT Digital Output for slave %d channel %d\n", slave_id, channel);
    }

    bool init() {
        if (slave_id_ <= 0 || slave_id_ > ec_slavecount) {
            printf("Invalid slave ID %d for digital output\n", slave_id_);
            return false;
        }
        if (channel_ < 0 || channel_ >= 8) {
            printf("Invalid channel %d for digital output\n", channel_);
            return false;
        }

        // 验证从站类型
        if (strcmp(ec_slave[slave_id_].name, "DigOut") != 0) {
            printf("Slave %d is not a Digital Output module\n", slave_id_);
            return false;
        }

        printf("Initialized EtherCAT Digital Output for slave %d channel %d\n", slave_id_, channel_);
        return true;
    }

    bool write(bool value) {
        auto* ethercat = EtherCAT::getInstance();
        if (!ethercat->isOperational()) {
            return false;
        }

        uint8_t* output_ptr = ec_slave[slave_id_].outputs;
        if (!output_ptr) return false;

        uint8_t mask = 1 << channel_;
        if (value) {
            *output_ptr |= mask;
        } else {
            *output_ptr &= ~mask;
        }

        ec_send_processdata();
        return true;
    }

private:
    int slave_id_;
    int channel_;
};


class EtherCATDigitalInput {
public:

    explicit EtherCATDigitalInput(int slave_id, int channel) : slave_id_(slave_id), channel_(channel) {
        printf("Creating EtherCAT Digital Input for slave %d channel %d\n", slave_id, channel);
    }

    bool init() {
        if (slave_id_ <= 0 || slave_id_ > ec_slavecount) {
            printf("Invalid slave ID %d for digital input\n", slave_id_);
            return false;
        }
        if (channel_ < 0 || channel_ >= 2) {
            printf("Invalid channel %d for digital input\n", channel_);
            return false;
        }

        // 验证从站类型
        if (strcmp(ec_slave[slave_id_].name, "DigIn") != 0) {
            printf("Slave %d is not a Digital Input module\n", slave_id_);
            return false;
        }

        printf("Initialized EtherCAT Digital Input for slave %d channel %d\n", slave_id_, channel_);
        return true;
    }

    bool read(bool* value) {
        if (!value) return false;
        auto* ethercat = EtherCAT::getInstance();
        if (!ethercat->isOperational()) {
            return false;
        }
        ec_send_processdata();
        uint8_t* input_ptr = ec_slave[slave_id_].inputs;

        // 打印 input_ptr 的地址以及其指向的数据
//        if (input_ptr) {
//            printf("input_ptr address: %p\n", (void*)input_ptr);
//            printf("input_ptr value at address[0]: %d\n", input_ptr[0]);  // 打印第一个字节的值
//            printf("input_ptr value at address[1]: %d\n", input_ptr[1]);  // 打印第二个字节的值
//            printf("input_ptr value at address[2]: %d\n", input_ptr[2]);  // 打印第三个字节的值
//        } else {
//            printf("Error: input_ptr is NULL\n");
//        }

        if (!input_ptr) return false;

        uint8_t mask = 1 << channel_;
        *value = (*input_ptr & mask) != 0;

        return true;
    }

private:
    int slave_id_;
    int channel_;
};

class DigitalIOManager {
public:
    static void createDigitalOutput(int slave_id, int channel) {
        std::string key = makeKey(slave_id, channel);
        g_digitalOutputs[key].reset(new EtherCATDigitalOutput(slave_id, channel));
    }

    static void createDigitalInput(int slave_id, int channel) {
        std::string key = makeKey(slave_id, channel);
        g_digitalInputs[key].reset(new EtherCATDigitalInput(slave_id, channel));
    }

    static EtherCATDigitalOutput* getDigitalOutput(int slave_id, int channel) {
        std::string key = makeKey(slave_id, channel);
        auto it = g_digitalOutputs.find(key);
        return (it != g_digitalOutputs.end()) ? it->second.get() : nullptr;
    }

    static EtherCATDigitalInput* getDigitalInput(int slave_id, int channel) {
        std::string key = makeKey(slave_id, channel);
        auto it = g_digitalInputs.find(key);
        return (it != g_digitalInputs.end()) ? it->second.get() : nullptr;
    }

private:
    static std::string makeKey(int slave_id, int channel) {
        return std::to_string(slave_id) + "_" + std::to_string(channel);
    }

    static std::map<std::string, std::unique_ptr<EtherCATDigitalOutput>> g_digitalOutputs;
    static std::map<std::string, std::unique_ptr<EtherCATDigitalInput>> g_digitalInputs;
};

#endif
