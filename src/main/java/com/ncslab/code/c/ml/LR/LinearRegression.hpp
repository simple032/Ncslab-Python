#ifndef NCS_LINEARREGRESSION
#define NCS_LINEARREGRESSION 1
#include "MLModel.hpp"

using ncsml::MLModel;

class LinearRegression : public MLModel {
public:
    LinearRegression(size_t inputFeatures, size_t outputFeatures) 
    : MLModel("linear_regression_model") {
        //init linear regression model
        PyObject* pArgs = Py_BuildValue("(ii)", inputFeatures, outputFeatures);
        PyObject_CallObject(this->pFuncInitModel, pArgs);
        Py_DECREF(pArgs);
    }

    ~LinearRegression() {
        
    }
};

std::unique_ptr<LinearRegression> linearRegressionModel;
#endif
