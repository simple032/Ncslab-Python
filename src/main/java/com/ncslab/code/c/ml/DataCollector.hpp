#include <iostream>
#include <fstream>
#include <vector>
#include <string>
#include <stdexcept>
#include <memory>
#include <iomanip>  //  control floating-point precision
#include "Matrix.hpp"

class DataCollector{
public:
    DataCollector(int input_size, int output_size):
    input_size(input_size), output_size(output_size){
        // printf("input: %d, output: %d\n", input_size, output_size);
    }

    void collect(Matrix& input_mat, Matrix& output_mat){
        collectInputs(input_mat);
        collectOutputs(output_mat);
    }

    void collect(Matrix&input_mat, REAL output_val){
        collectInputs(input_mat);
        outputs.push_back(std::vector<REAL>(1, output_val));
    }

    void collect(REAL input_val, Matrix&output_mat){
        collectOutputs(output_mat);
        inputs.push_back(std::vector<REAL>(1, input_val));
    }

    void collect(REAL input_val, REAL output_val){
        outputs.push_back(std::vector<REAL>(1, output_val));
        inputs.push_back(std::vector<REAL>(1, input_val));
    }


    void collectInputs(Matrix&input_mat){
        inputs.push_back(std::vector<REAL>(input_mat.data(), input_mat.data() + input_size));
    }

    void collectOutputs(Matrix&output_mat){
        outputs.push_back(std::vector<REAL>(output_mat.data(), output_mat.data() + output_size));
    }

     // 将数据保存到 CSV 文件
    void save(const std::string& path) {
        // open file and start to write
        std::ofstream file(path);
        if (!file.is_open()) {
            throw std::runtime_error("Failed to open file: " + path);
        }
        file << std::fixed << std::setprecision(5);  // set decimal places

        for (size_t i = 0; i < input_size; ++i){
            file << "X" << i << ",";
        }
        for(size_t i = 0; i < output_size-1; ++i){
            file << "Y" << i <<",";
        }
        file << "Y"<<output_size<<"\n";
        size_t length = inputs.size();
        for (size_t i = 0; i < length; ++i) {
            // write inputs
            for (size_t j = 0; j < input_size; ++j) {
                file << inputs[i][j];
                if (j < input_size - 1) {
                    file << ",";
                }
            }

            file << ",";

            // write outputs
            for (size_t j = 0; j < output_size; ++j) {
                file << outputs[i][j];
                if (j < output_size - 1) {
                    file << ",";
                }
            }

            file << "\n";
        }

        file.close();
    }


private:
    int input_size, output_size;
    std::vector<std::vector<REAL>> inputs;
    std::vector<std::vector<REAL>> outputs;
};
