#ifndef NCS_CNN
#define NCS_CNN 1
#include "MLModel.hpp"
#include <stdexcept>

class CNN : public ncsml::MLModel {
public:
    CNN(size_t num_classes, size_t channel_size, std::vector<size_t> hidden_layers)
    : MLModel("cnn_model", "SimpleCNN"), pInstance(nullptr), pFuncTrainByFile(nullptr), pFuncPredict(nullptr) {
        // parameters:
        // int: num_classes
        // int: channel_size
        // list: hidden_layers
        PyObject* pArgs = Py_BuildValue("(iiO)", num_classes, channel_size, PyList_New(hidden_layers.size()));
        for (size_t i = 0; i < hidden_layers.size(); ++i) {
            PyList_SetItem(PyList_GetItem(pArgs, 2), i, PyLong_FromSize_t(hidden_layers[i]));
        }
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
