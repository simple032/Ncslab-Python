#ifndef ETHERCAT_SH_HPP
#define ETHERCAT_SH_HPP

#include <memory>
#include <string>
#include <thread>
#include <atomic>
#include <mutex>
#include <map>
#include <time.h>

#include "osal.h"
#include "oshw.h"
#include "nicdrv.h"
#include "ethercattype.h"
#include "ethercatbase.h"
#include "ethercatmain.h"
#include "ethercatdc.h"
#include "ethercatconfig.h"
#include "ethercatprint.h"
#include "ethercatcoe.h"
#include "ethercatfoe.h"

extern int ECAT_init_Flag;

class EtherCATServo;
class ServoManager;

class EtherCATSE {
public:
    static EtherCATSE* getInstance() {
        static EtherCATSE instance;
        return &instance;
    }

    bool init(const std::string& ifname, uint8_t operation_mode = 3) {
        std::lock_guard<std::mutex> lock(mutex_);
        interface_name = ifname;

        printf("Starting EtherCAT initialization\n");
        clock_gettime(CLOCK_MONOTONIC, &last_cycle_time);

        // 1. 初始化EtherCAT主站
        if (ec_init(interface_name.c_str()) <= 0) {
            printf("Failed to initialize EtherCAT master\n");
            return false;
        }
        printf("ec_init on %s succeeded.\n", interface_name.c_str());

        // 2. 配置从站
        if (ec_config_init(FALSE) <= 0) {
            printf("No slaves found!\n");
            return false;
        }
        printf("%d slaves found and configured.\n", ec_slavecount);

        // 3. 对每个M6_ECAT_CoE从站基础配置
        for(int slave = 1; slave <= ec_slavecount; slave++) {
            if(strcmp(ec_slave[slave].name, "M6_ECAT_CoE") == 0) {
                // 根据不同模式配置
                switch(operation_mode) {
                    case 3: // 速度模式
                        if (!configureVelocityMode(slave)) {
                            printf("Failed to configure velocity mode for slave %d\n", slave);
                            return false;
                        }
                        break;
                    case 4: // 转矩模式
                        if (!configureTorqueMode(slave)) {
                            printf("Failed to configure torque mode for slave %d\n", slave);
                            return false;
                        }
                        break;
                    default:
                        printf("Unsupported operation mode: %d\n", operation_mode);
                        return false;
                }
            }
        }
        // 4. 配置和映射过程数据
        printf("Mapping PDOs to process data...\n");
        if(ec_config_map(&IOmap) <= 0) {
            printf("Failed to map PDOs\n");
            return false;
        }

        // 5. 显示从站配置信息
        printSlaveInfo("Slave info after PDO configuration");

        // PDO映射之后，添加状态切换代码
        printf("\nRequesting SAFE_OP state for all slaves\n");
        ec_slave[0].state = EC_STATE_SAFE_OP;
        ec_writestate(0);

        int chk = 40;
        do {
            ec_send_processdata();
            ec_receive_processdata(EC_TIMEOUTRET);
            ec_statecheck(0, EC_STATE_SAFE_OP, 50000);
        } while (chk-- && (ec_slave[0].state != EC_STATE_SAFE_OP));

        if (ec_slave[0].state != EC_STATE_SAFE_OP) {
            printf("Not all slaves reached safe operational state.\n");
            ec_readstate();
            for(int i = 1; i <= ec_slavecount; i++) {
                printf("Slave %d State=%2d StatusCode=%4.4x : %s\n",
                    i, ec_slave[i].state, ec_slave[i].ALstatuscode,
                    ec_ALstatuscode2string(ec_slave[i].ALstatuscode));
            }
            return false;
        }

        printf("All slaves reached safe operational state.\n");

        // 切换到 OPERATIONAL 状态
        printf("\nRequesting operational state for all slaves\n");
        ec_slave[0].state = EC_STATE_OPERATIONAL;
        ec_writestate(0);

        chk = 40;
        do {
            ec_send_processdata();
            ec_receive_processdata(EC_TIMEOUTRET);
            ec_statecheck(0, EC_STATE_OPERATIONAL, 50000);
        } while (chk-- && (ec_slave[0].state != EC_STATE_OPERATIONAL));

        if (ec_slave[0].state != EC_STATE_OPERATIONAL) {
            printf("Not all slaves reached operational state.\n");
            ec_readstate();
            for(int i = 1; i <= ec_slavecount; i++) {
                printf("Slave %d State=%2d StatusCode=%4.4x : %s\n",
                    i, ec_slave[i].state, ec_slave[i].ALstatuscode,
                    ec_ALstatuscode2string(ec_slave[i].ALstatuscode));
            }
            return false;
        }

        printf("Operational state reached for all slaves.\n");
        expected_wkc = (ec_group[0].outputsWKC * 2) + ec_group[0].inputsWKC;
        printf("Calculated workcounter %d\n", expected_wkc);

        inited = true;
        return true;
    }
/*
    bool send_receive_data() {
        struct timespec current_time;
        clock_gettime(CLOCK_MONOTONIC, &current_time);

        // Calculate actual cycle time
        double actual_cycle_ms = (current_time.tv_sec - last_cycle_time.tv_sec) * 1000.0 +
                               (current_time.tv_nsec - last_cycle_time.tv_nsec) / 1000000.0;

        // Calculate how long to sleep
        if (sampling_period_ns > 0) {
            int64_t elapsed_ns = (current_time.tv_sec - last_cycle_time.tv_sec) * 1000000000 +
                                (current_time.tv_nsec - last_cycle_time.tv_nsec);
            if (elapsed_ns < sampling_period_ns) {
                int64_t sleep_ns = sampling_period_ns - elapsed_ns;
                struct timespec sleep_time;
                sleep_time.tv_sec = sleep_ns / 1000000000;
                sleep_time.tv_nsec = sleep_ns % 1000000000;
                nanosleep(&sleep_time, NULL);
            }
        }

        // Update statistics
        cycle_count++;
        if (cycle_count % 500 == 0) {  // 每500个周期打印一次统计信息
            double target_period_ms = sampling_period_ns / 1000000.0;
            printf("Timing Statistics:\n");
            printf("  Target Period: %.3f ms\n", target_period_ms);
            printf("  Actual Period: %.3f ms\n", actual_cycle_ms);
            printf("  Difference: %.3f ms\n", actual_cycle_ms - target_period_ms);
        }

        // Store current time for next cycle
        last_cycle_time = current_time;

        ec_send_processdata();
        current_wkc = ec_receive_processdata(EC_TIMEOUTRET/10);
        return (current_wkc >= expected_wkc);
    }
*/
    bool send_receive_data() {
        // 先发送和接收数据
        ec_send_processdata();
        current_wkc = ec_receive_processdata(EC_TIMEOUTRET/10);

        struct timespec current_time;
        clock_gettime(CLOCK_MONOTONIC, &current_time);

        // 计算从上次执行到现在实际经过的时间
        int64_t elapsed_ns = (current_time.tv_sec - last_cycle_time.tv_sec) * 1000000000LL +
                            (current_time.tv_nsec - last_cycle_time.tv_nsec);

        // 计算需要休眠的时间
        if (sampling_period_ns > 0) {
            int64_t sleep_ns = sampling_period_ns - elapsed_ns;
            if (sleep_ns > 0) {
                struct timespec sleep_time;
                sleep_time.tv_sec = sleep_ns / 1000000000LL;
                sleep_time.tv_nsec = sleep_ns % 1000000000LL;
                nanosleep(&sleep_time, NULL);

                // 更新当前时间，包含sleep时间
                clock_gettime(CLOCK_MONOTONIC, &current_time);
            }
        }

        // 计算实际周期时间（包含了sleep时间）
        double actual_cycle_ms = (current_time.tv_sec - last_cycle_time.tv_sec) * 1000.0 +
                               (current_time.tv_nsec - last_cycle_time.tv_nsec) / 1000000.0;

        // 更新统计信息
        cycle_count++;
        if (cycle_count % 500 == 0) {
            double target_period_ms = sampling_period_ns / 1000000.0;
            //printf("Timing Statistics:\n");
            //printf("  Target Period: %.3f ms\n", target_period_ms);
            //printf("  Actual Period: %.3f ms\n", actual_cycle_ms);
            //printf("  Difference: %.3f ms\n", actual_cycle_ms - target_period_ms);
        }

        // 更新last_cycle_time为当前时间
        last_cycle_time = current_time;

        return (current_wkc >= expected_wkc);
    }

    void cleanup() {
        if(inited) {
            printf("\nCleaning up EtherCAT connection\n");
            ec_close();
            inited = false;
        }
    }

    void setSamplingPeriod(double seconds) {
        sampling_period_ns = static_cast<int64_t>(seconds * 1e9);
        if(sampling_period_ns < 10000) { // min 10us
            sampling_period_ns = 10000;
        }
        printf("Setting sampling period to %.3f ms\n", seconds * 1000.0);
    }

    bool isOperational() const {
        return ec_slave[0].state == EC_STATE_OPERATIONAL;
    }

private:
    bool configureVelocityMode(int slave) {
        printf("Configuring velocity mode for slave %d\n", slave);

        // 设置运行模式
        uint8_t mode = 3;
        if (ec_SDOwrite(slave, 0x6060, 0, FALSE, sizeof(mode), &mode, EC_TIMEOUTRXM) <= 0) {
            return false;
        }
        osal_usleep(10000);

        // 配置速度限制
        uint32_t max_speed = 6000;
        ec_SDOwrite(slave, 0x607F, 0, FALSE, sizeof(max_speed), &max_speed, EC_TIMEOUTRXM);
        ec_SDOwrite(slave, 0x6080, 0, FALSE, sizeof(max_speed), &max_speed, EC_TIMEOUTRXM);

        // 设置加减速时间
        uint32_t acc_time = 500;
        ec_SDOwrite(slave, 0x6083, 0, FALSE, sizeof(acc_time), &acc_time, EC_TIMEOUTRXM);
        ec_SDOwrite(slave, 0x6084, 0, FALSE, sizeof(acc_time), &acc_time, EC_TIMEOUTRXM);

        return configureVelocityPDO(slave);
    }

    bool configureVelocityPDO(int slave) {
        // 清除同步管理器配置
        uint8_t zero = 0;
        ec_SDOwrite(slave, 0x1C12, 0, FALSE, sizeof(zero), &zero, EC_TIMEOUTRXM);
        ec_SDOwrite(slave, 0x1C13, 0, FALSE, sizeof(zero), &zero, EC_TIMEOUTRXM);
        osal_usleep(10000);

        // 配置RXPDO
        ec_SDOwrite(slave, 0x1600, 0, FALSE, sizeof(zero), &zero, EC_TIMEOUTRXM);
        uint32_t map = 0x60400010;  // 控制字
        ec_SDOwrite(slave, 0x1600, 1, FALSE, sizeof(map), &map, EC_TIMEOUTRXM);
        map = 0x60FF0020;  // 目标速度
        ec_SDOwrite(slave, 0x1600, 2, FALSE, sizeof(map), &map, EC_TIMEOUTRXM);
        uint8_t mapCount = 2;
        ec_SDOwrite(slave, 0x1600, 0, FALSE, sizeof(mapCount), &mapCount, EC_TIMEOUTRXM);

        // 配置TXPDO
        ec_SDOwrite(slave, 0x1A00, 0, FALSE, sizeof(zero), &zero, EC_TIMEOUTRXM);
        map = 0x60410010;  // 状态字
        ec_SDOwrite(slave, 0x1A00, 1, FALSE, sizeof(map), &map, EC_TIMEOUTRXM);
        map = 0x606C0020;  // 实际速度
        ec_SDOwrite(slave, 0x1A00, 2, FALSE, sizeof(map), &map, EC_TIMEOUTRXM);
        map = 0x60640020;  // 实际位置
        ec_SDOwrite(slave, 0x1A00, 3, FALSE, sizeof(map), &map, EC_TIMEOUTRXM);
        mapCount = 3;
        ec_SDOwrite(slave, 0x1A00, 0, FALSE, sizeof(mapCount), &mapCount, EC_TIMEOUTRXM);

        return configureSyncManagers(slave);
    }

    bool configureTorqueMode(int slave) {
        printf("Configuring torque mode for slave %d\n", slave);

        // 设置运行模式
        uint8_t mode = 4;
        if (ec_SDOwrite(slave, 0x6060, 0, FALSE, sizeof(mode), &mode, EC_TIMEOUTRXM) <= 0) {
            return false;
        }
        osal_usleep(10000);

        // 设置转矩相关参数
        uint32_t torque_slope = 100;
        ec_SDOwrite(slave, 0x6087, 0, FALSE, sizeof(torque_slope), &torque_slope, EC_TIMEOUTRXM);

        uint16_t max_torque = 1000;
        ec_SDOwrite(slave, 0x6072, 0, FALSE, sizeof(max_torque), &max_torque, EC_TIMEOUTRXM);
        ec_SDOwrite(slave, 0x60E0, 0, FALSE, sizeof(max_torque), &max_torque, EC_TIMEOUTRXM);
        ec_SDOwrite(slave, 0x60E1, 0, FALSE, sizeof(max_torque), &max_torque, EC_TIMEOUTRXM);

        return configureTorquePDO(slave);
    }

    bool configureTorquePDO(int slave) {
        // 清除同步管理器配置
        uint8_t zero = 0;
        ec_SDOwrite(slave, 0x1C12, 0, FALSE, sizeof(zero), &zero, EC_TIMEOUTRXM);
        ec_SDOwrite(slave, 0x1C13, 0, FALSE, sizeof(zero), &zero, EC_TIMEOUTRXM);
        osal_usleep(10000);

        // 配置RXPDO
        ec_SDOwrite(slave, 0x1600, 0, FALSE, sizeof(zero), &zero, EC_TIMEOUTRXM);
        uint32_t map = 0x60400010;  // 控制字
        ec_SDOwrite(slave, 0x1600, 1, FALSE, sizeof(map), &map, EC_TIMEOUTRXM);
        map = 0x60710010;  // 目标转矩
        ec_SDOwrite(slave, 0x1600, 2, FALSE, sizeof(map), &map, EC_TIMEOUTRXM);
        uint8_t mapCount = 2;
        ec_SDOwrite(slave, 0x1600, 0, FALSE, sizeof(mapCount), &mapCount, EC_TIMEOUTRXM);

        // 配置TXPDO
        ec_SDOwrite(slave, 0x1A00, 0, FALSE, sizeof(zero), &zero, EC_TIMEOUTRXM);
        map = 0x60410010;  // 状态字
        ec_SDOwrite(slave, 0x1A00, 1, FALSE, sizeof(map), &map, EC_TIMEOUTRXM);
        map = 0x60770010;  // 实际转矩
        ec_SDOwrite(slave, 0x1A00, 2, FALSE, sizeof(map), &map, EC_TIMEOUTRXM);
        //ec_SDOwrite(slave, 0x1A00, 0, FALSE, sizeof(mapCount), &mapCount, EC_TIMEOUTRXM);

        // 添加实际速度映射
        map = 0x606C0020;  // 实际速度
        ec_SDOwrite(slave, 0x1A00, 3, FALSE, sizeof(map), &map, EC_TIMEOUTRXM);
        // 设置映射数量为3（增加了速度映射）
        mapCount = 3;
        ec_SDOwrite(slave, 0x1A00, 0, FALSE, sizeof(mapCount), &mapCount, EC_TIMEOUTRXM);

        return configureSyncManagers(slave);
    }

    bool configureSyncManagers(int slave) {
        // 配置同步管理器
        uint16_t pdoAssign = 0x1600;
        uint8_t zero = 0;
        ec_SDOwrite(slave, 0x1C12, 0, FALSE, sizeof(zero), &zero, EC_TIMEOUTRXM);
        ec_SDOwrite(slave, 0x1C12, 1, FALSE, sizeof(pdoAssign), &pdoAssign, EC_TIMEOUTRXM);
        uint8_t mapCount = 1;
        ec_SDOwrite(slave, 0x1C12, 0, FALSE, sizeof(mapCount), &mapCount, EC_TIMEOUTRXM);

        pdoAssign = 0x1A00;
        ec_SDOwrite(slave, 0x1C13, 0, FALSE, sizeof(zero), &zero, EC_TIMEOUTRXM);
        ec_SDOwrite(slave, 0x1C13, 1, FALSE, sizeof(pdoAssign), &pdoAssign, EC_TIMEOUTRXM);
        ec_SDOwrite(slave, 0x1C13, 0, FALSE, sizeof(mapCount), &mapCount, EC_TIMEOUTRXM);

        return true;
    }

    void printSlaveInfo(const char* message) {
        printf("\n%s:\n", message);
        for(int i = 1; i <= ec_slavecount; i++) {
            printf("Slave %d\n", i);
            printf(" Name: %s\n", ec_slave[i].name);
            printf(" Output size: %dbits\n", ec_slave[i].Obits);
            printf(" Input size: %dbits\n", ec_slave[i].Ibits);
            printf(" State: %d\n", ec_slave[i].state);
            printf(" ALstatuscode: 0x%04x\n", ec_slave[i].ALstatuscode);
        }
    }

    std::string interface_name;
    std::mutex mutex_;
    uint8_t IOmap[4096];
    bool inited{false};
    int64_t sampling_period_ns{0};
    int expected_wkc{0};
    int current_wkc{0};

    // Timing monitoring variables
    struct timespec last_cycle_time;
    long cycle_count{0};
};

#endif
