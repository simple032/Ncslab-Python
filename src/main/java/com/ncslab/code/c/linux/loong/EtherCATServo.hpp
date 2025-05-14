#ifndef ETHERCAT_SERVO_HPP
#define ETHERCAT_SERVO_HPP

#include <memory>
#include <map>
#include "EtherCATSh.hpp"

class EtherCATServo {
public:
    explicit EtherCATServo(int slave_id) : slave_id_(slave_id) {
        printf("Creating EtherCAT Servo for slave %d\n", slave_id);
    }

    bool init() {
        printf("Initializing servo %d\n", slave_id_);

        // 检查伺服驱动器的状态
        uint16_t statusword = 0;
        if(!readStatusWord(&statusword)) {
            printf("Failed to read initial status word\n");
            return false;
        }
        printf("Initial statusword: 0x%04x\n", statusword);
        printDetailedStatus(statusword);

        return true;
    }

    bool enableServo() {
        uint16_t statusword = 0;

        // 检查是否有故障
        if(!readStatusWord(&statusword)) {
            return false;
        }

        // 如果有故障，先清除故障
        if(statusword & 0x0008) {
            printf("Fault detected (0x%04x), attempting to clear...\n", statusword);
            uint16_t* ctrl_word = (uint16_t*)&ec_slave[slave_id_].outputs[0];

            // 发送故障复位命令
            *ctrl_word = 0x0080;  // Fault Reset命令
            auto* ethercat = EtherCATSE::getInstance();
            ethercat->send_receive_data();
            osal_usleep(100000);  // 等待100ms

            // 重新读取状态
            readStatusWord(&statusword);
            if(statusword & 0x0008) {
                printf("Failed to clear fault\n");
                return false;
            }
            printf("Fault cleared successfully\n");
        }

        uint16_t *ctrl_word = (uint16_t*)&ec_slave[slave_id_].outputs[0];
        int wait_time = 0;
        const int TIMEOUT_CYCLES = 20;

        auto* ethercat = EtherCATSE::getInstance();

        printf("Starting servo enable sequence...\n");

        // 1. 读取初始状态
        if(!readStatusWord(&statusword)) {
            printf("Failed to read initial status\n");
            return false;
        }
        printf("Initial status: 0x%04x\n", statusword);

        // 2. 复位所有控制位
        *ctrl_word = 0x0000;
        ethercat->send_receive_data();
        osal_usleep(50000);
        readStatusWord(&statusword);
        printf("Status after reset: 0x%04x\n", statusword);

        // 3. 清除Quick Stop和Switch On Disabled
        *ctrl_word = 0x0006;  // Shutdown command
        for(int i = 0; i < 10; i++) {
            ethercat->send_receive_data();
            readStatusWord(&statusword);
            printf("Status during shutdown: 0x%04x\n", statusword);
            if((statusword & 0x004F) == 0x0040) break;  // 期望状态
            osal_usleep(50000);
        }

        // 4. Switch On
        *ctrl_word = 0x0007;  // Switch On command
        for(int i = 0; i < 10; i++) {
            ethercat->send_receive_data();
            readStatusWord(&statusword);
            printf("Status during switch on: 0x%04x\n", statusword);
            if((statusword & 0x006F) == 0x0023) break;  // 期望状态
            osal_usleep(50000);
        }

        // 5. Enable Operation
        *ctrl_word = 0x000F;  // Enable Operation command
        for(int i = 0; i < 10; i++) {
            ethercat->send_receive_data();
            readStatusWord(&statusword);
            printf("Status during enable: 0x%04x\n", statusword);
            if((statusword & 0x006F) == 0x0027) {
                printf("Servo enabled successfully\n");
                return true;
            }
            osal_usleep(50000);
        }

        printf("Failed to enable servo, final status: 0x%04x\n", statusword);
        return false;
    }

    // 不同模式下的控制函数
    // Position
    bool setPosition(int32_t position) {
        printf("Position mode not implemented yet\n");
        return false;
    }

    bool readActualPosition(int32_t* position) {
        if (!position) return false;

        printf("Position mode not implemented yet\n");
        return false;
    }

    // Velocity
    bool setVelocity(int32_t velocity) {
        auto* ethercat = EtherCATSE::getInstance();
        if (!ethercat->isOperational()) {
            printf("EtherCAT not operational\n");
            return false;
        }

        // 检查并保持使能状态
        uint16_t statusword = 0;
        readStatusWord(&statusword);

        // 检查故障状态
        if(statusword & 0x0008) {
            printf("Fault detected (0x%04x), attempting to re-enable...\n", statusword);
            if(!enableServo()) return false;
        }
        else if((statusword & 0x006F) != 0x0027) {
            if(!enableServo()) return false;
        }

        // 确保控制字保持使能状态
        uint16_t* ctrl_word = (uint16_t*)&ec_slave[slave_id_].outputs[0];
        *ctrl_word = 0x000F;  // 保持Enable Operation

        // 写入速度值
        int32_t* target_vel = (int32_t*)&ec_slave[slave_id_].outputs[2];
        *target_vel = velocity;

        // 发送数据
        if (!ethercat->send_receive_data()) {
            printf("Failed to send velocity command\n");
            return false;
        }

        //printf("Set velocity to %d\n", velocity);
        return true;
    }

    bool readActualVelocity(int32_t* velocity) {
        if (!velocity) return false;

        auto* ethercat = EtherCATSE::getInstance();
        if (!ethercat->isOperational()) {
            printf("EtherCAT not operational\n");
            return false;
        }

        // 读取实际速度
        if (!ethercat->send_receive_data()) {
            printf("Failed to receive velocity data\n");
            return false;
        }

        //*velocity = *((int32_t*)&ec_slave[slave_id_].inputs[2]);  // 2字节偏移
        printf("Actual Speed: %d\n", *((int32_t*)&ec_slave[slave_id_].inputs[2]));


        // --------------------- 处理实际位置数据 ---------------------
        static int32 initial_position = 0;
        static bool position_initialized = false; // 是否初始化
        static int32 last_position = 0;

        // 获取当前实际位置
        int32 *actualpos = (int32 *)&ec_slave[slave_id_].inputs[4];

        // 初始化初始位置和上一次位置（新增逻辑）
        if (!position_initialized) {
            initial_position = *actualpos;
            last_position = *actualpos;
            position_initialized = true;
        }
        int32 relative_position = *actualpos - initial_position;

        if (abs(*actualpos - last_position) == 65536) {
            // 如果差值为 65536，修正相对位置
            if (*actualpos > last_position) {
                relative_position -= 65536;  // 如果当前位置大于上次位置，减少 65536
                initial_position += 65536;
            } else {
                relative_position += 65536;  // 如果当前位置小于上次位置，增加 65536
                initial_position -= 65536;
            }

        }
        relative_position = relative_position / 65536;
        last_position = *actualpos;
        printf("Actual Position: %d, Relative Position: %d\n", *actualpos, relative_position);
        printf("last_position: %d\n", last_position);

        double angle_change = (double)relative_position / 8192 * 360.0;
        printf("Angle: %.2f degrees\n", angle_change);
        *velocity = angle_change;


        return true;
    }

    // Torque
    bool setTorque(int32_t torque) {
        auto* ethercat = EtherCATSE::getInstance();
        if (!ethercat->isOperational()) {
            printf("EtherCAT not operational\n");
            return false;
        }

        // 检查并保持使能状态
        uint16_t statusword = 0;
        readStatusWord(&statusword);

        // 检查故障状态
        if(statusword & 0x0008) {
            printf("Fault detected (0x%04x), attempting to re-enable...\n", statusword);
            if(!enableServo()) return false;
        }
        else if((statusword & 0x006F) != 0x0027) {
            if(!enableServo()) return false;
        }

        // 确保控制字保持使能状态
        uint16_t* ctrl_word = (uint16_t*)&ec_slave[slave_id_].outputs[0];
        *ctrl_word = 0x000F;  // 保持Enable Operation

        // 写入转矩值
        int16_t* target_trq = (int16_t*)&ec_slave[slave_id_].outputs[2];
        *target_trq = (int16_t)torque;

        // 发送数据
        if (!ethercat->send_receive_data()) {
            printf("Failed to send torque command\n");
            return false;
        }

        return true;
    }

    bool readActualTorque(int32_t* torque) {
        if (!torque) return false;

        auto* ethercat = EtherCATSE::getInstance();
        if (!ethercat->isOperational()) {
            printf("EtherCAT not operational\n");
            return false;
        }

        // 读取实际数据
        if (!ethercat->send_receive_data()) {
            printf("Failed to receive torque data\n");
            return false;
        }

        // 读取转矩值（从偏移量2开始的2字节）
        int16_t* actual_trq = (int16_t*)&ec_slave[slave_id_].inputs[2];
        //*torque = (int32_t)*actual_trq;

        // 读取速度值（从偏移量4开始的4字节）
        int32_t* actual_vel = (int32_t*)&ec_slave[slave_id_].inputs[4];
        //*velocity = *actual_vel;
        *torque = *actual_vel;

        return true;
    }

private:
    bool readStatusWord(uint16_t* statusword) {
        if (!statusword) return false;
        auto* ethercat = EtherCATSE::getInstance();
        *statusword = *((uint16_t*)&ec_slave[slave_id_].inputs[0]);
        //printDetailedStatus(*statusword);
        return true;
    }

    bool writeControlWord(uint16_t controlword) {
        auto* ethercat = EtherCATSE::getInstance();
        uint16_t* ctrl_word = (uint16_t*)&ec_slave[slave_id_].outputs[0];
        *ctrl_word = controlword;
        return ethercat->send_receive_data();
    }

    void printDetailedStatus(uint16_t statusword) {
        printf("Status Analysis:\n");
        printf("  Ready to switch on: %d\n", (statusword & 0x0001) != 0);
        printf("  Switched on: %d\n", (statusword & 0x0002) != 0);
        printf("  Operation enabled: %d\n", (statusword & 0x0004) != 0);
        printf("  Fault: %d\n", (statusword & 0x0008) != 0);
        printf("  Voltage enabled: %d\n", (statusword & 0x0010) != 0);
        printf("  Quick stop: %d\n", (statusword & 0x0020) != 0);
        printf("  Switch on disabled: %d\n", (statusword & 0x0040) != 0);
        printf("  Warning: %d\n", (statusword & 0x0080) != 0);
        printf("  Remote: %d\n", (statusword & 0x0200) != 0);
        printf("  Target reached: %d\n", (statusword & 0x0400) != 0);
    }

    int slave_id_;
};

// Manager class for servos
class ServoManager {
public:
    static void createServo(int slave_id) {
        g_servos[slave_id].reset(new EtherCATServo(slave_id));
    }

    static EtherCATServo* getServo(int slave_id) {
        auto it = g_servos.find(slave_id);
        return (it != g_servos.end()) ? it->second.get() : nullptr;
    }

private:
    static std::map<int, std::unique_ptr<EtherCATServo>> g_servos;
};

#endif
