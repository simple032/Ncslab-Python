#include <Python.h>
#include <iostream>
#include <vector>
#include <string>
#include <stdexcept>
#include <memory> 

class LinearRegression {
public:
    LinearRegression() {
        PyObject* pName = PyUnicode_DecodeFSDefault("linear_regression_model");
        pModule = PyImport_Import(pName);
        Py_DECREF(pName);
        if (pModule == nullptr) {
            PyErr_Print();
            // throw std::runtime_error("Failed to load Python module");
        } else {
            // std::cout << "Successfully load Python module" << std::endl;
        }

        pFuncInitModel = PyObject_GetAttrString(pModule, "init_model");
        pFuncTrainModel = PyObject_GetAttrString(pModule, "train_model");
        pFuncPredict = PyObject_GetAttrString(pModule, "predict");

        if (!pFuncInitModel || !pFuncTrainModel || !pFuncPredict) {
            PyErr_Print();
            Py_XDECREF(pFuncInitModel);
            Py_XDECREF(pFuncTrainModel);
            Py_XDECREF(pFuncPredict);
            // throw std::runtime_error("Failed to load necessary Python functions");
        }
    }

    ~LinearRegression() {
    }

    PyObject* initModel() {
        return PyObject_CallObject(pFuncInitModel, nullptr);
    }

    void trainModel(const std::string& filename, int epochs, double lr) {
        PyObject* pArgs = Py_BuildValue("(sid)", filename.c_str(), epochs, lr);
        PyObject_CallObject(pFuncTrainModel, pArgs);
        Py_DECREF(pArgs);
    }

    double predict(const std::vector<double>& inputs) {
        if (pFuncPredict == nullptr) {
            // std::cout << "Error: pFuncPredict is nullptr when trying to predict" << std::endl;
            return 0.0;
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
            return 0.0;
        }

        double result = PyFloat_AsDouble(pResult);
        Py_DECREF(pResult);
        return result;
}

private:
    PyObject *pModule, *pFuncInitModel, *pFuncTrainModel, *pFuncPredict;
};

std::unique_ptr<LinearRegression> linearRegressionModel;

void initModel() {
    linearRegressionModel = std::make_unique<LinearRegression>();
}

void trainModel(const std::string& filename, int epochs, double lr) {
    linearRegressionModel->trainModel(filename, epochs, lr);
}

double predict(const std::vector<double>& inputs) {
    return linearRegressionModel->predict(inputs);
}

double getResult(double d1, double d2, double d3) {
    // std::cout << "===INTO Getting results===" << std::endl;
    std::vector<double> inputs = {d1, d2 * d2, d3};

    // std::cout << "Vector elements: ";
    // for (const auto& element : inputs) {
        // std::cout << element << " ";
    // }
    // std::cout << std::endl;
    
    // std::cout << "===INTO Getting results===" << std::endl;
    return linearRegressionModel->predict(inputs);
}

double getTestResult(double d1, LinearRegression* linearRegressionModel) {
    std::vector<double> inputs = {d1};
    return linearRegressionModel->predict(inputs);
}
