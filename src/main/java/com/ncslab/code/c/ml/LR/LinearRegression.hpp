#ifndef NCS_LINEARREGRESSION
#define NCS_LINEARREGRESSION 1
#include "MLModel.hpp"

using ncsml::MLModel;

class LinearRegression : public MLModel {
public:
    LinearRegression(size_t inputFeatures, size_t outputFeatures) 
    : MLModel("linear_regression_model", "LinearRegression") {
        //init linear regression model
        // PyObject* pArgs = Py_BuildValue("(ii)", inputFeatures, outputFeatures);
        // PyObject_CallObject(this->pFuncInitModel, pArgs);
        // Py_DECREF(pArgs);

        PyObject* pArgs = Py_BuildValue("(ii)", inputFeatures, outputFeatures);
        this->pInstance = PyObject_CallObject(pClass, pArgs);
        Py_DECREF(pArgs);
        if(pInstance == nullptr){
            PyErr_Print();
            Py_XDECREF(pInstance);
            throw std::runtime_error("Failed to create instance of the class");
        }
        pFuncTrainByFile = PyObject_GetAttrString(pInstance, "train_by_file");
        pFuncPredict = PyObject_GetAttrString(pInstance, "predict");
        if (!pFuncTrainByFile || !pFuncPredict) {
            PyErr_Print();
            Py_XDECREF(pFuncTrainByFile);
            Py_XDECREF(pFuncPredict);
            throw std::runtime_error("Failed to load necessary Python functions");
        }
    }

    ~LinearRegression() {
        
    }
};

// std::unique_ptr<LinearRegression> linearRegressionModel;
#endif
