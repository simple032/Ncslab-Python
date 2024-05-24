#ifndef NCS_CNN
#define NCS_CNN 1
#include "MLModel.hpp"
#include <stdexcept>

class CNN : public ncsml::MLModel {
public:
    CNN(size_t num_classes)
    : MLModel("cnn_model", "SimpleCNN"), pInstance(nullptr), pFuncTrainByFile(nullptr), pFuncPredict(nullptr) {
        PyObject* pArgs = Py_BuildValue("(i)", num_classes);
        pInstance = PyObject_CallObject(pClass, pArgs);
        Py_DECREF(pArgs);
        if (!pInstance) {
            PyErr_Print();
            throw std::runtime_error("Failed to create instance of the class");
        }
        pFuncTrainByFile = PyObject_GetAttrString(pInstance, "train_by_file");
        pFuncPredict = PyObject_GetAttrString(pInstance, "predict");
        if (!pFuncTrainByFile || !pFuncPredict) {
            PyErr_Print();
            Py_XDECREF(pFuncTrainByFile);
            Py_XDECREF(pFuncPredict);
            Py_XDECREF(pInstance);
            throw std::runtime_error("Failed to load necessary Python functions");
        }
    }
};
#endif
