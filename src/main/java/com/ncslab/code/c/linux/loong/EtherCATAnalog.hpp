#ifndef ETHERCAT_ANALOG_HPP
#define ETHERCAT_ANALOG_HPP
#include <memory>
#include <map>
#include <cstdint>
#include <cstring>
#include "EtherCAT.hpp"


class EtherCATAnalogOutput {
public:
    explicit EtherCATAnalogOutput(int slave_id) : slave_id_(slave_id) {
        printf("Creating EtherCAT Analog Output for slave %d\n", slave_id);
    }

    bool init() {
        if (slave_id_ <= 0 || slave_id_ > ec_slavecount) {
            printf("Invalid slave ID %d for analog output\n", slave_id_);
            return false;
        }

        if (strcmp(ec_slave[slave_id_].name, "AnaOut") != 0) {
            printf("Slave %d is not an Analog Output module\n", slave_id_);
            return false;
        }

        printf("Initialized EtherCAT Analog Output for slave %d\n", slave_id_);
        return true;
    }


    bool write(double value) {
        auto* ethercat = EtherCAT::getInstance();
        if (!ethercat->isOperational()) {
            return false;
        }
        ethercat->updateOutput(slave_id_, value);
        return true;
    }

private:
    int slave_id_;
};

class EtherCATAnalogInput {
public:
    explicit EtherCATAnalogInput(int slave_id) : slave_id_(slave_id) {
        printf("Creating EtherCAT Analog Input for slave %d\n", slave_id);
    }

    bool init() {
        if (slave_id_ <= 0 || slave_id_ > ec_slavecount) {
            printf("Invalid slave ID %d for analog input\n", slave_id_);
            return false;
        }

        if (strcmp(ec_slave[slave_id_].name, "AnaIn") != 0) {
            printf("Slave %d is not an Analog Input module\n", slave_id_);
            return false;
        }

        printf("Initialized EtherCAT Analog Input for slave %d\n", slave_id_);
        return true;
    }

    bool read(double* value) {
        if (!value) return false;

        auto* ethercat = EtherCAT::getInstance();
        if (!ethercat->isOperational()) {
            return false;
        }
        *value = ethercat->readInput(slave_id_);
        return true;
    }

private:
    int slave_id_;
};


class AnalogIOManager {
public:
    static void createAnalogOutput(int slave_id) {
        g_analogOutputs[slave_id].reset(new EtherCATAnalogOutput(slave_id));
    }

    static void createAnalogInput(int slave_id) {
        g_analogInputs[slave_id].reset(new EtherCATAnalogInput(slave_id));
    }

    static EtherCATAnalogOutput* getAnalogOutput(int slave_id) {
        auto it = g_analogOutputs.find(slave_id);
        return (it != g_analogOutputs.end()) ? it->second.get() : nullptr;
    }

    static EtherCATAnalogInput* getAnalogInput(int slave_id) {
        auto it = g_analogInputs.find(slave_id);
        return (it != g_analogInputs.end()) ? it->second.get() : nullptr;
    }

private:
    static std::map<int, std::unique_ptr<EtherCATAnalogOutput>> g_analogOutputs;
    static std::map<int, std::unique_ptr<EtherCATAnalogInput>> g_analogInputs;
};

extern int ECAT_init_Flag;

#endif
