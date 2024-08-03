#ifndef NCS_A2C
#define NCS_A2C 1

#include "MLModel.hpp"
#include <Eigen/Dense>
#include <stdexcept>
#include <vector>

using ncsml::MLModel;

class A2C : public MLModel{
public:
    A2C(size_t input_layer, size_t output_layer): MLModel("A2C", "A2CAgent") {
        
        // Create argument tuple with (input_layer, output_layer)
        // std::cout << "start creating instance" << std::endl;
        PyObject* pArgs = Py_BuildValue("(ii)", input_layer, output_layer);
        this->pInstance = PyObject_CallObject(pClass, pArgs);
        // std::cout << "finish creating instance" << std::endl;
        Py_DECREF(pArgs);
        if (pInstance == nullptr) {
            PyErr_Print();
            Py_XDECREF(pInstance);
            throw std::runtime_error("Failed to create instance of the class");
        }
        // pFuncSetActivationFunction = PyObject_GetAttrString(pInstance, "set_activation_function");
        // pFuncTrainByFile = PyObject_GetAttrString(pInstance, "train_by_file");
        pFuncPredict = PyObject_GetAttrString(pInstance, "predict");
        pFuncLoadModel = PyObject_GetAttrString(pInstance, "load_model");
        // std::cout << "finish getting functions" << std::endl;
        if (!pFuncPredict || !PyCallable_Check(pFuncPredict) ||
            !pFuncLoadModel || !PyCallable_Check(pFuncLoadModel)) {
            PyErr_Print();
            Py_XDECREF(pFuncPredict);
            Py_XDECREF(pInstance);
            throw std::runtime_error("Failed to load necessary Python functions");
        }
    }

    void load_model(const std::string& filename) {
        PyObject* pArgs = Py_BuildValue("(s)", filename.c_str());
        PyObject_CallObject(pFuncLoadModel, pArgs);
        Py_DECREF(pArgs);
    }
    
private:
    PyObject* pFuncLoadModel;
};

#endif