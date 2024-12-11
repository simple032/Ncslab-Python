#ifndef NCS_LINEARREGRESSION
#define NCS_LINEARREGRESSION 1
#include "PTModel.hpp"

using ncsml::PTModel;

class LinearRegression : public PTModel {
public:
    LinearRegression(size_t inputFeatures, size_t outputFeatures, const std::string loss_function)
    : PTModel("linear_regression_model", "LinearRegression") {
        PyObject* pArgs = Py_BuildValue("(iis)", inputFeatures, outputFeatures, loss_function.c_str());
        this->pInstance = PyObject_CallObject(pClass, pArgs);
        Py_DECREF(pArgs);
        if(pInstance == nullptr){
            PyErr_Print();
            Py_XDECREF(pInstance);
            throw std::runtime_error("Failed to create instance of the class");
        }
        pFuncTrainByFile = PyObject_GetAttrString(pInstance, "train_by_file");
        pFuncSave = PyObject_GetAttrString(pInstance, "save_model");
        pFuncPredict = PyObject_GetAttrString(pInstance, "predict");
        if (!pFuncTrainByFile || !pFuncPredict || !pFuncSave) {
            PyErr_Print();
            Py_XDECREF(pFuncTrainByFile);
            Py_XDECREF(pFuncPredict);
            Py_XDECREF(pFuncSave);
            throw std::runtime_error("Failed to load necessary Python functions");
        }
    }

    ~LinearRegression() {

    }
};

// std::unique_ptr<LinearRegression> linearRegressionModel;
#endif
