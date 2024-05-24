#ifndef NCS_CNN
#define NCS_CNN 1
#include "MLModel.hpp"
#include <stdexcept>

class CNN : public ncsml::MLModel {
public:
    CNN(size_t num_classes)
    : MLModel("cnn_model", "SimpleCNN"), pInstance(nullptr), pFuncTrainByFile(nullptr), pFuncPredict(nullptr) {
        Py_Initialize();
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

    ~CNN() {
        Py_XDECREF(pFuncTrainByFile);
        Py_XDECREF(pFuncPredict);
        Py_XDECREF(pInstance);
        Py_Finalize();
    }

    void train_by_file(const std::string& filename, int epochs, double lr) {
        if (!pFuncTrainByFile) {
            throw std::runtime_error("train_by_file function not loaded");
        }
        PyObject* pValue = PyObject_CallFunction(pFuncTrainByFile, "sif", filename.c_str(), epochs, lr);
        if (!pValue) {
            PyErr_Print();
            throw std::runtime_error("Failed to call train_by_file");
        }
        Py_DECREF(pValue);
    }

    // std::vector<int> predict(const std::vector<float>& inputs) {
    //     if (!pFuncPredict) {
    //         throw std::runtime_error("predict function not loaded");
    //     }
    //     PyObject* pList = PyList_New(inputs.size());
    //     for (size_t i = 0; i < inputs.size(); ++i) {
    //         PyList_SetItem(pList, i, PyFloat_FromDouble(inputs[i]));
    //     }
    //     PyObject* pValue = PyObject_CallFunctionObjArgs(pFuncPredict, pList, nullptr);
    //     Py_DECREF(pList);
    //     if (!pValue) {
    //         PyErr_Print();
    //         throw std::runtime_error("Failed to call predict");
    //     }
    //     std::vector<int> result;
    //     if (PyList_Check(pValue)) {
    //         for (Py_ssize_t i = 0; i < PyList_Size(pValue); ++i) {
    //             PyObject* item = PyList_GetItem(pValue, i);
    //             result.push_back(static_cast<int>(PyLong_AsLong(item)));
    //         }
    //     }
    //     Py_DECREF(pValue);
    //     return result;
    // }

private:
    PyObject* pInstance;
    PyObject* pFuncTrainByFile;
    PyObject* pFuncPredict;
};
#endif
