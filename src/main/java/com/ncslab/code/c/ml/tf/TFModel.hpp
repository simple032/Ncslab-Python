#ifndef NCS_TFMODEL
#define NCS_TFMODEL 1
#include "MLModel.hpp"

using ncsml::MLModel;

namespace ncsml{
class TFMODEL : public MLModel{
public:
    virtual void trainModel(const std::string& filename, int epochs, double lr) = 0;

    virtual Matrix predict(const Matrix& inputs) = 0;

    virtual Matrix predict(double input) = 0;

     virtual void saveModel(std::string path) = 0;

    virtual void loadModel(std::string path) = 0;
};
};

#endif
