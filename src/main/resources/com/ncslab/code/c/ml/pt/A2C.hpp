#ifndef NCS_A2C
#define NCS_A2C 1

#include "PTModel.hpp"
#include <Eigen/Dense>
#include <stdexcept>
#include <vector>

using ncsml::PTModel;

class A2C : public PTModel{
public:
    A2C(size_t input_layer, size_t output_layer, REAL lr, REAL gamma): MLModel("A2C", "A2CAgent") {
        PyObject* pArgs = Py_BuildValue("(iiff)", input_layer, output_layer, lr, gamma);
        this->pInstance = PyObject_CallObject(pClass, pArgs);
        Py_DECREF(pArgs);
        if (pInstance == nullptr) {
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
};

#endif
