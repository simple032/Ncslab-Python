#ifndef NCS_ML_MODEL
#define NCS_ML_MODEL 1
#include <Python.h>
#include <iostream>
#include <vector>
#include <string>
#include <stdexcept>
#include <memory> 

#include "Matrix.hpp"

namespace ncsml{
class MLModel {
public:
    /**
     * @Param model_name: file name without ".py"
     * @Param model_type: python class, like "LinearRegression" in "class LinearRegression".
    */
    MLModel(const char* model_name, const char* model_type) {
        PyObject* pName = PyUnicode_DecodeFSDefault(model_name);
        pModule = PyImport_Import(pName);
        Py_DECREF(pName);
        if (pModule == nullptr) {
            PyErr_Print();
            throw std::runtime_error("Failed to load Python module");
        } else {
            // std::cout << "Successfully load Python module" << std::endl;
        }
        pClass = PyObject_GetAttrString(pModule, model_type); // class of the python
        if (!pClass || !PyCallable_Check(pClass)) {
            PyErr_Print();
            Py_XDECREF(pClass);
            throw std::runtime_error("Failed to load necessary Python class!");
        }

    }

    virtual ~MLModel() {
        Py_XDECREF(pInstance);
        Py_XDECREF(pClass);
        Py_XDECREF(pFuncTrainByFile);
        Py_XDECREF(pFuncPredict);           
        Py_DECREF(pModule);
    }

    virtual void trainModel(const std::string& filename, int epochs, double lr) {
        PyObject* pArgs = Py_BuildValue("(sid)", filename.c_str(), epochs, lr);
        PyObject_CallObject(pFuncTrainByFile, pArgs);
        Py_DECREF(pArgs);
    }

    virtual std::vector<double> predict(const std::vector<double> inputs){
        // std::cout << "debug in local predict\n";
        if (pFuncPredict == nullptr) {
            std::cout << "Error: pFuncPredict is nullptr when trying to predict" << std::endl;
            throw new std::runtime_error("Failed to load necessary Python functions");
        }

        PyObject* pList = PyList_New(inputs.size());
        for (size_t i = 0; i < inputs.size(); ++i) {
            PyObject* pFloat = PyFloat_FromDouble(inputs[i]);
            PyList_SetItem(pList, i, pFloat);
        }

        PyObject* pArgs = Py_BuildValue("(O)", pList);
        PyObject* pResult = PyObject_CallObject(pFuncPredict, pArgs);

        Py_DECREF(pArgs);
        Py_DECREF(pList);

        if (pResult == nullptr) {
            PyErr_Print();
            PyErr_Clear();
            throw new std::runtime_error("Failed to load necessary Python functions");
        }

        std::vector<double> results;
        if (PyList_Check(pResult)) {
            Py_ssize_t size = PyList_Size(pResult);
            results.resize(size);

            for (Py_ssize_t i = 0; i < size; ++i) {
                PyObject* pItem = PyList_GetItem(pResult, i);
                if (PyFloat_Check(pItem)) {
                    double value = PyFloat_AsDouble(pItem);
                    results[i] = value;
                } else {
                    // Handle the error for non-float items.
                    std::cout << "Error: All items in the result list must be floats." << std::endl;
                    Py_DECREF(pResult);
                    throw std::runtime_error("Non-float item encountered in prediction results");
                }
            }
        }

        Py_DECREF(pResult);
        return results;
    }

    virtual std::vector<double> predict(const Matrix inputs) {
        std::vector<double> inputs_vec(inputs.size());
        // std::cout << "debug in eigen predict\n";
        memcpy(inputs_vec.data(), inputs.data(), inputs.size() * sizeof(double));
        return predict(inputs_vec);
    }

    virtual std::vector<double> predict(double input){
        std::vector<double> inputs_vec(1, 0);
        inputs_vec[0] = input;
        // std::cout << "debug in double predict\n";
        return predict(inputs_vec);
    }

    

protected:
    PyObject *pModule, *pClass, *pInstance, *pFuncTrainByFile, *pFuncPredict;
};
};
#endif