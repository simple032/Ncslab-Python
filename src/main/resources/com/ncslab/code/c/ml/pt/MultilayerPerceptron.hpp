#ifndef NCS_MULTILAYERPERCEPTRON
#define NCS_MULTILAYERPERCEPTRON

#include "MLModel.hpp"
#include <Eigen/Dense>
#include <stdexcept>
#include <vector>

using ncsml::PTModel;

class MultilayerPerceptron : public PTModel {
public:
    MultilayerPerceptron(size_t input_layer, size_t output_layer, double hidden_layers_d, const std::string loss_function, const std::string activation_function)
    : PTModel("multilayer_perceptron_model", "MLP") {
        std::vector<uint32_t> hidden_layers(1, 0);
        hidden_layers[0] = uint32_t(hidden_layers_d);

        this->_init(input_layer, output_layer, hidden_layers, loss_function, activation_function);
    }

    ~MultilayerPerceptron() {
        Py_XDECREF(pFuncTrainByFile);
        Py_XDECREF(pFuncPredict);
        Py_XDECREF(pInstance);
    }

    MultilayerPerceptron(size_t input_layer, size_t output_layer, Matrix hidden_layers_eigen, const std::string loss_function, const std::string activation_function)
    : PTModel("multilayer_perceptron_model", "MLP") {
        std::vector<uint32_t> hidden_layers(hidden_layers_eigen.size());
        for (uint32_t i = 0; i < hidden_layers_eigen.size(); ++i){
            hidden_layers[i] = uint32_t(hidden_layers_eigen(i));
        }
        this->_init(input_layer, output_layer, hidden_layers, loss_function, activation_function);
    }

private:
    void _init(size_t input_layer, size_t output_layer, std::vector<uint32_t> hidden_layers, const std::string loss_function = "CE", const std::string activation_function = "sigmoid") {
        // Create a Python list for hidden_layers
        PyObject* py_hidden_layers = PyList_New(hidden_layers.size());
        for (size_t i = 0; i < hidden_layers.size(); ++i) {
            PyList_SetItem(py_hidden_layers, i, PyLong_FromSize_t(hidden_layers[i]));
        }

        // Create argument tuple with (input_layer, output_layer, hidden_layers, loss_function, activation_function)
        PyObject* pArgs = Py_BuildValue("(iiOss)", input_layer, output_layer, py_hidden_layers, loss_function.c_str(), activation_function.c_str());
        this->pInstance = PyObject_CallObject(pClass, pArgs);
        Py_DECREF(py_hidden_layers);
        Py_DECREF(pArgs);
        if (pInstance == nullptr) {
            PyErr_Print();
            Py_XDECREF(pInstance);
            throw std::runtime_error("Failed to create instance of the class");
        }
        // pFuncSetActivationFunction = PyObject_GetAttrString(pInstance, "set_activation_function");
        pFuncTrainByFile = PyObject_GetAttrString(pInstance, "train_by_file");
        pFuncPredict = PyObject_GetAttrString(pInstance, "predict");
        pFuncSave = PyObject_GetAttrString(pInstance, "save_model");
        if (!pFuncTrainByFile || !PyCallable_Check(pFuncTrainByFile) ||
            !pFuncPredict || !PyCallable_Check(pFuncPredict)) {
            PyErr_Print();
            Py_XDECREF(pFuncTrainByFile);
            Py_XDECREF(pFuncPredict);
            Py_XDECREF(pFuncSave);
            throw std::runtime_error("Failed to load necessary Python functions");
        }
    }
};

#endif
