#ifndef NCS_CNN
#define NCS_CNN 1
#include"MLModel.hpp"

class CNN:public ncsml::MLModel{
public:
    LinearRegression(size_t num_classes) 
    : MLModel("cnn_model", "SimpleCNN") {
        PyObject* pArgs = Py_BuildValue("(i)", num_classes);
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
#endif