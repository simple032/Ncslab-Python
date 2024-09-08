#ifndef NCS_LOGISTICREGRESSION
#define NCS_LOGISTICREGRESSION
#include "MLModel.hpp"

using ncsml::MLModel;
class LogisticRegression : public MLModel {
public:
    LogisticRegression(size_t input_layer, size_t output_layer, const std::string lossFunction)
     : MLModel("logistic_regression_model", "LogisticRegression") {
        //init logistic regression model
        PyObject* pArgs = Py_BuildValue("(iis)", input_layer, input_layer, lossFunction.c_str());
        this->pInstance = PyObject_CallObject(pClass, pArgs);
        Py_DECREF(pArgs);
        if(pInstance == nullptr){
            PyErr_Print();
            Py_XDECREF(pInstance);
            throw std::runtime_error("Failed to create instance of the class");
        }
        pFuncTrainByFile = PyObject_GetAttrString(pInstance, "train_by_file");
        pFuncPredict = PyObject_GetAttrString(pInstance, "predict");
        if (!pFuncTrainByFile || !pFuncPredict) {
            PyErr_Print();
            Py_XDECREF(pFuncTrainByFile);
            Py_XDECREF(pFuncPredict);
            throw std::runtime_error("Failed to load necessary Python functions");
        }
    }

    // Matrix predict(const Matrix inputs){
    //     // std::cout << "debug in local predict\n";
    //     if (pFuncPredict == nullptr) {
    //         std::cout << "Error: pFuncPredict is nullptr when trying to predict" << std::endl;
    //         throw new std::runtime_error("Failed to load necessary Python functions");
    //     }

    //     PyObject* pList = PyList_New(inputs.size());
    //     for (size_t i = 0; i < inputs.size(); ++i) {
    //         PyObject* pFloat = PyFloat_FromDouble(inputs(i));
    //         PyList_SetItem(pList, i, pFloat);
    //     }

    //     PyObject* pArgs = Py_BuildValue("(O)", pList);
    //     PyObject* pResult = PyObject_CallObject(pFuncPredict, pArgs);

    //     Py_DECREF(pArgs);
    //     Py_DECREF(pList);

    //     if (pResult == nullptr) {
    //         PyErr_Print();
    //         PyErr_Clear();
    //         throw new std::runtime_error("Failed to load necessary Python functions");
    //     }

    //     Py_ssize_t size = 0;
    //     if (PyList_Check(pResult)) {
    //         size = PyList_Size(pResult);
    //     }else{
    //         throw std::runtime_error("Result is not a list");
    //     }
    //     // std::cout << "size: " << size << std::endl;
    //     Matrix results(size, 1);
    //     // std::cout <<"debug after matrix\n";

    //     for (Py_ssize_t i = 0; i < size; ++i) {
    //         PyObject* pItem = PyList_GetItem(pResult, i);
    //         if (PyFloat_Check(pItem)) {
    //             double value = PyFloat_AsDouble(pItem);
    //             results(i) = value;
    //         } else {
    //             // Handle the error for non-float items.
    //             std::cout << "Error: All items in the result list must be floats." << std::endl;
    //             Py_DECREF(pResult);
    //             throw std::runtime_error("Non-float item encountered in prediction results");
    //         }
    //     }
        
    //     // std::cout << "debug: before return\n";
    //     Py_DECREF(pResult);
    //     return results;
    // }

    ~LogisticRegression() {
    }
};

#endif