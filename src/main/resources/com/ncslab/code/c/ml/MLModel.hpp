#ifndef NCS_ML_MODEL
#define NCS_ML_MODEL 1
#include <iostream>
#include <vector>
#include <string>
#include <stdexcept>
#include <memory>

#include "Matrix.hpp"

namespace ncsml{
class MLModel {
public:
    virtual ~MLModel() {
    }

    virtual void trainModel(const std::string& filename, int epochs, double lr) = 0;

    virtual Matrix predict(const Matrix& inputs) = 0;

    virtual Matrix predict(double input) = 0;

     virtual void saveModel(std::string path) = 0;

    virtual void loadModel(std::string path) = 0;
};
};
#endif
