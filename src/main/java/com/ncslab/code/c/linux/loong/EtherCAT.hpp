#ifndef ETHERCAT_HPP
#define ETHERCAT_HPP

#include <memory>
#include <string>
#include <thread>
#include <atomic>
#include <mutex>
#include <map>

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

class EtherCAT {
public:
    static EtherCAT* getInstance() {
        static EtherCAT instance;
        return &instance;
    }

    bool init(const std::string& ifname) {
        std::lock_guard<std::mutex> lock(mutex_);
        interface_name = ifname;
        if (!initializeEtherCAT()) {
            return false;
        }
        if (!startCommunication()) {
            return false;
        }

        return true;
    }

    void setSamplingPeriod(double seconds) {
        sampling_period_ns = static_cast<int64_t>(seconds * 1e9);
        printf("Setting sampling period to %.3f ms\n", seconds * 1000.0);
    }

    void cleanup() {
        stopCommunication();
        ec_close();
    }

    void updateOutput(int slaveId, double value) {
        output_values_[slaveId].store(value);
    }

    double readInput(int slaveId) {
        return input_values_[slaveId].load();
    }

    bool isOperational() const {
        return running_.load();
    }

private:
    EtherCAT() : running_(false), sampling_period_ns(1000000) {} // 默认1ms
    ~EtherCAT() { cleanup(); }
    EtherCAT(const EtherCAT&) = delete;
    EtherCAT& operator=(const EtherCAT&) = delete;

    bool initializeEtherCAT() {
        // 关闭之前的连接
        ec_close();
        osal_usleep(500000);  // 等待500ms

        // 初始化 SOEM
        if (ec_init(interface_name.c_str()) <= 0) {
            printf("Failed to initialize EtherCAT master\n");
            return false;
        }

        // 配置从站
        if (ec_config_init(FALSE) <= 0) {
            printf("Failed to initialize slave configuration\n");
            ec_close();
            return false;
        }

        // 映射PDO
        if (ec_config_map(&IOmap) <= 0) {
            printf("Failed to map PDOs\n");
            return false;
        }

        // 状态转换序列
        struct {
            uint16_t state;
            const char* name;
        } states[] = {
            {EC_STATE_INIT, "INIT"},
            {EC_STATE_PRE_OP, "PRE_OP"},
            {EC_STATE_SAFE_OP, "SAFE_OP"},
            {EC_STATE_OPERATIONAL, "OPERATIONAL"}
        };

        // 遍历所有从站进行状态转换
        for (const auto& state : states) {
            printf("Setting all slaves to %s state\n", state.name);

            // 遍历所有从站
            for (int slave = 1; slave <= ec_slavecount; slave++) {
                ec_slave[slave].state = state.state;
                ec_writestate(slave);
            }

            // 等待所有从站达到目标状态
            int retries = 40;
            bool all_reached = false;
            while (retries-- && !all_reached) {
                all_reached = true;
                for (int slave = 1; slave <= ec_slavecount; slave++) {
                    if (ec_statecheck(slave, state.state, EC_TIMEOUTSTATE) != state.state) {
                        all_reached = false;
                        break;
                    }
                }
                if (!all_reached) {
                    osal_usleep(100000);
                }
            }

            if (!all_reached) {
                printf("Failed to reach %s state for all slaves\n", state.name);
                return false;
            }

            printf("All slaves reached %s state\n", state.name);
            osal_usleep(200000);
        }

        return true;
    }

    bool startCommunication() {
        running_ = true;
        comm_thread_ = std::thread(&EtherCAT::communicationThread, this);

        // 设置线程优先级
        sched_param param;
        param.sched_priority = 99;
        pthread_setschedparam(comm_thread_.native_handle(), SCHED_FIFO, &param);

        // 绑定到特定CPU核心
        cpu_set_t cpuset;
        CPU_ZERO(&cpuset);
        CPU_SET(0, &cpuset);  // 绑定到核心0
        pthread_setaffinity_np(comm_thread_.native_handle(), sizeof(cpu_set_t), &cpuset);

        return true;
    }

    void stopCommunication() {
        running_ = false;
        if (comm_thread_.joinable()) {
            comm_thread_.join();
        }
    }

    void communicationThread() {
        struct timespec next_cycle, actual_start, actual_end;;
        clock_gettime(CLOCK_MONOTONIC, &next_cycle);

        while (running_) {
            clock_gettime(CLOCK_MONOTONIC, &actual_start);
            // 处理所有从站的输出数据
            for (auto& pair : output_values_) {
                int slave_id = pair.first;
                if (strcmp(ec_slave[slave_id].name, "AnaOut") == 0) {
                    double output_value = pair.second.load();
                    int16_t raw_output = static_cast<int16_t>(output_value * 32767.0 / 10.0);
                    *((int16_t*)ec_slave[slave_id].outputs) = raw_output;
                }
                else if (strcmp(ec_slave[slave_id].name, "DigOut") == 0) {
                    uint8_t digital_value = static_cast<uint8_t>(pair.second.load() != 0.0 ? 1 : 0);
                    *((uint8_t*)ec_slave[slave_id].outputs) = digital_value;
                    printf("Slave ID: %d, Output Value: %f\n", slave_id, pair.second.load());
                }

            }

            // 发送和接收数据
            ec_send_processdata();

            if (ec_receive_processdata(EC_TIMEOUTRET) > 0) {
                //printf("hi\n");
                // 处理所有从站的输入数据
                for (auto& pair : input_values_) {
                    //printf("hello\n");
                    int slave_id = pair.first;
                    // 模拟量输入
                    if (strcmp(ec_slave[slave_id].name, "AnaIn") == 0) {
                        uint8_t* input_ptr = ec_slave[slave_id].inputs;
                        uint16_t raw_value = (input_ptr[2] << 8) | input_ptr[1];
                        double input_value = (static_cast<double>(raw_value) / 32767.0) * 10.0 * 2.0;
                        pair.second.store(input_value);
                    }// 数字量输入
                    else if (strcmp(ec_slave[slave_id].name, "DigIn") == 0) {
                        uint8_t* input_ptr = ec_slave[slave_id].inputs;
                        bool digital_value = (*input_ptr & 0x01) != 0;
                        pair.second.store(digital_value ? 1.0 : 0.0);
                        printf("Slave ID: %d, Digital Input: %d\n", slave_id, digital_value ? 1 : 0);
                    }

                }
            }

            // 检查从站状态
            static int check_counter = 0;
            if (++check_counter >= 10000) {
                check_counter = 0;
                ec_readstate();
                for (int slave = 1; slave <= ec_slavecount; slave++) {
                    if (ec_slave[slave].state != EC_STATE_OPERATIONAL) {
                        printf("Slave %d not OPERATIONAL (state=0x%x)\n", slave, ec_slave[slave].state);
                        ec_slave[slave].state = EC_STATE_OPERATIONAL;
                        ec_writestate(slave);
                    }
                }
            }

            // 记录循环结束时间
            clock_gettime(CLOCK_MONOTONIC, &actual_end);
            // 计算实际执行时间
            long actual_duration = (actual_end.tv_sec - actual_start.tv_sec) * 1000000000 +
                                 (actual_end.tv_nsec - actual_start.tv_nsec);
            // 每1000次循环打印一次时间
            static int print_counter = 0;
            if (++print_counter >= 1000) {
                print_counter = 0;
                //printf("Set period: %ld ns, Actual execution time: %ld ns (%.3f ms)\n", sampling_period_ns, actual_duration, actual_duration/1000000.0);
            }
            // 等待下一个周期
            next_cycle.tv_nsec += sampling_period_ns;
            if (next_cycle.tv_nsec >= 1000000000) {
                next_cycle.tv_sec++;
                next_cycle.tv_nsec -= 1000000000;
            }
            clock_nanosleep(CLOCK_MONOTONIC, TIMER_ABSTIME, &next_cycle, NULL);
        }
    }

    std::string interface_name;
    std::thread comm_thread_;
    std::atomic<bool> running_;
    std::mutex mutex_;
    std::map<int, std::atomic<double>> input_values_;
    std::map<int, std::atomic<double>> output_values_;
    uint8_t IOmap[4096];
    int64_t sampling_period_ns;
};

#endif
