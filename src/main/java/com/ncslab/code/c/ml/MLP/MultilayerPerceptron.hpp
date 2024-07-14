#ifndef NCS_MULTILAYERPERCEPTRON
#define NCS_MULTILAYERPERCEPTRON
#include "MLModel.hpp"

using ncsml::MLModel;

class MultilayerPerceptron : public MLModel {
public:
    MultilayerPerceptron(size_t input_layer, size_t output_layer, const std::vector<size_t> hidden_layers) 
    : MLModel("multilayer_perceptron_model", "MLP") {
        // Create a Python list for hidden_layers
        PyObject* py_hidden_layers = PyList_New(hidden_layers.size());
        for (size_t i = 0; i < hidden_layers.size(); ++i) {
            PyList_SetItem(py_hidden_layers, i, PyLong_FromSize_t(hidden_layers[i]));
        }

        // Create argument tuple with (input_layer, output_layer, hidden_layers)
        PyObject* pArgs = Py_BuildValue("(iiO)", input_layer, output_layer, py_hidden_layers);
        
        this->pInstance = PyObject_CallObject(pClass, pArgs);
        
        Py_DECREF(py_hidden_layers);
        Py_DECREF(pArgs);
        if (pInstance == nullptr) {
            PyErr_Print();
            Py_XDECREF(pInstance);
            throw std::runtime_error("Failed to create instance of the class");
        }
        pFuncTrainByFile = PyObject_GetAttrString(pInstance, "train_by_file");
        pFuncPredict = PyObject_GetAttrString(pInstance, "predict");
        if (!pFuncTrainByFile || !PyCallable_Check(pFuncTrainByFile) ||
            !pFuncPredict || !PyCallable_Check(pFuncPredict)) {
            PyErr_Print();
            Py_XDECREF(pFuncTrainByFile);
            Py_XDECREF(pFuncPredict);
            Py_XDECREF(pInstance);
            throw std::runtime_error("Failed to load necessary Python functions");
        }
    }

    ~MultilayerPerceptron() {
        Py_XDECREF(pFuncTrainByFile);
        Py_XDECREF(pFuncPredict);
        Py_XDECREF(pInstance);
    }

    // void train_by_file(const std::string& filename, int epochs, float lr) {
    //     PyObject* pValue = PyObject_CallFunction(pFuncTrainByFile, "sif", filename.c_str(), epochs, lr);
    //     if (pValue == nullptr) {
    //         PyErr_Print();
    //         throw std::runtime_error("Failed to train model by file");
    //     }
    //     Py_DECREF(pValue);
    // }

    // std::vector<size_t> predict(const std::vector<double>& input) {
    //     PyObject* pInput = PyList_New(input.size());
    //     for (size_t i = 0; i < input.size(); ++i) {
    //         PyList_SetItem(pInput, i, PyFloat_FromDouble(input[i]));
    //     }

    //     PyObject* pValue = PyObject_CallFunctionObjArgs(pFuncPredict, pInput, NULL);
    //     Py_DECREF(pInput);
    //     if (pValue == nullptr) {
    //         PyErr_Print();
    //         throw std::runtime_error("Failed to predict");
    //     }

    //     std::vector<size_t> result(PyList_Size(pValue));
    //     for (size_t i = 0; i < result.size(); ++i) {
    //         result[i] = PyLong_AsSize_t(PyList_GetItem(pValue, i));
    //     }
    //     Py_DECREF(pValue);
    //     return result;
    // }

// private:
//     PyObject *pFuncTrainByFile;
//     PyObject *pFuncPredict;
//     PyObject *pInstance;
};

#endif
