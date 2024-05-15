#ifndef NCS_LOGISTICREGRESSION
#define NCS_LOGISTICREGRESSION
#include "MLModel.hpp"

using ncsml::MLModel;
class LogisticRegression : public MLModel {
public:
    LogisticRegression(size_t input_layer, size_t output_layer) : MLModel("logistic_regression_model", "LogisticRegression") {
        //init logistic regression model
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

    ~LogisticRegression() {
    }
};

#endif