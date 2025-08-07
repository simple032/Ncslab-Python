#include <iostream>
#include <vector>
#include <string>
#include <cmath>
#include <functional>

class InvertedPendulum {
private:
    double m0; // 小车质量
    double m1; // 摆球质量
    double l;  // 杆长
    double g;  // 重力加速度
    double x, x_dot, theta, theta_dot; // 系统状态

    // 函数指针，指向数值积分方法
    std::function<void(double, double)> step_solver;

    // 系统的状态微分方程
    void derivatives(const std::vector<double> &state, double u, std::vector<double> &dstate) {
        double s = sin(state[2]);
        double c = cos(state[2]);
        double total_mass = m0 + m1;
        double temp = (u + m1 * l * state[3] * state[3] * s) / total_mass;
        double theta_acc = (g * s - c * temp) / (l * (4.0 / 3.0 - m1 * c * c / total_mass));
        double x_acc = temp - m1 * l * theta_acc * c / total_mass;

        dstate[0] = state[1];
        dstate[1] = x_acc;
        dstate[2] = state[3];
        dstate[3] = theta_acc;
    }

    // 欧拉法
    void eulerStep(double u, double t) {
        std::vector<double> state = {x, x_dot, theta, theta_dot};
        std::vector<double> dstate(4);

        derivatives(state, u, dstate);

        x += dstate[0] * t;
        x_dot += dstate[1] * t;
        theta += dstate[2] * t;
        theta_dot += dstate[3] * t;
    }

    // 四阶龙格库塔法
    void rk4Step(double u, double t) {
        std::vector<double> state = {x, x_dot, theta, theta_dot};
        std::vector<double> k1(4), k2(4), k3(4), k4(4), temp_state(4);

        derivatives(state, u, k1);
        for (int i = 0; i < 4; ++i) temp_state[i] = state[i] + 0.5 * t * k1[i];
        derivatives(temp_state, u, k2);
        for (int i = 0; i < 4; ++i) temp_state[i] = state[i] + 0.5 * t * k2[i];
        derivatives(temp_state, u, k3);
        for (int i = 0; i < 4; ++i) temp_state[i] = state[i] + t * k3[i];
        derivatives(temp_state, u, k4);

        x += (k1[0] + 2 * k2[0] + 2 * k3[0] + k4[0]) * t / 6.0;
        x_dot += (k1[1] + 2 * k2[1] + 2 * k3[1] + k4[1]) * t / 6.0;
        theta += (k1[2] + 2 * k2[2] + 2 * k3[2] + k4[2]) * t / 6.0;
        theta_dot += (k1[3] + 2 * k2[3] + 2 * k3[3] + k4[3]) * t / 6.0;
    }

public:
    // 构造函数
    InvertedPendulum(double m0, double m1, double l, double g, const std::string &solver, const std::vector<double> &init_state)
        : m0(m0), m1(m1), l(l), g(g) {
        if (init_state.size() != 4) {
            throw std::invalid_argument("Initial state must be a vector of size 4.");
        }
        x = init_state[0];
        x_dot = init_state[1];
        theta = init_state[2];
        theta_dot = init_state[3];

        if (solver == "Euler") {
            step_solver = [this](double u, double t) { eulerStep(u, t); };
        } else if (solver == "RK4") {
            step_solver = [this](double u, double t) { rk4Step(u, t); };
        } else {
            throw std::invalid_argument("Unsupported solver method.");
        }
    }

    // 单步仿真函数
    std::vector<double> step(double u, double t) {
        step_solver(u, t);
        return {x, x_dot, theta, theta_dot};
    }
};
