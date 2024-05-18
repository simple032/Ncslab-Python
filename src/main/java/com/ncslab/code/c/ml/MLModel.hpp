#ifndef NCS_ML_MODEL
#define NCS_ML_MODEL 1
#include <Python.h>
#include <iostream>
#include <vector>
#include <string>
#include <stdexcept>
#include <memory> 

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

        // >>>>>>>>>>>>>>>>> Previously used code 
        // pFuncInitModel = PyObject_GetAttrString(pModule, "init_model");
        // pFuncTrainModel = PyObject_GetAttrString(pModule, "train_model");
        // pFuncPredict = PyObject_GetAttrString(pModule, "predict");
        // if (!pFuncInitModel || !pFuncTrainModel || !pFuncPredict) {
        //     PyErr_Print();
        //     Py_XDECREF(pFuncInitModel);
        //     Py_XDECREF(pFuncTrainModel);
        //     Py_XDECREF(pFuncPredict);
        //     throw std::runtime_error("Failed to load necessary Python functions");
        // }
        pClass = PyObject_GetAttrString(pModule, model_type); // class of the python
        if (!pClass || !PyCallable_Check(pClass)) {
            PyErr_Print();
            Py_XDECREF(pClass);
            throw std::runtime_error("Failed to load necessary Python class!");
        }
        // <<<<<<<<<<<<<<<<< FEATED code

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

    virtual std::vector<double> predict(const std::vector<double>& inputs){
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

    //todos: switch loss function, activation function, and other functions.

protected:
    // PyObject *pModule, *pFuncInitModel, *pFuncTrainModel, *pFuncPredict;
    PyObject *pModule, *pClass, *pInstance, *pFuncTrainByFile, *pFuncPredict;
};
};
#endif