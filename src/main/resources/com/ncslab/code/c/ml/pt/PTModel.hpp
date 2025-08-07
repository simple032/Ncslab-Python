#ifndef NCS_PTMODEL
#define NCS_PTMODEL 1
#include <Python.h>

#include "MLModel.hpp"

using ncsml::MLModel;

namespace ncsml{
class PTModel : public MLModel{
public:
    /**
     * @Param model_name: file name without ".py"
     * @Param model_type: python class, like "LinearRegression" in "class LinearRegression".
    */
    PTModel(const char* model_name, const char* model_type) {
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

    virtual ~PTModel() {
        Py_XDECREF(pInstance);
        Py_XDECREF(pClass);
        Py_XDECREF(pFuncTrainByFile);
        Py_XDECREF(pFuncPredict);
        Py_DECREF(pModule);
        Py_XDECREF(pFuncSave);
    }

    virtual void trainModel(const std::string& filename, int epochs, double lr) override{
//        std::cout << "Training the model...\n";
        PyObject* pArgs = Py_BuildValue("(sid)", filename.c_str(), epochs, lr);
        PyObject_CallObject(pFuncTrainByFile, pArgs);
        Py_DECREF(pArgs);
      return;
    }

    virtual Matrix predict(const Matrix& inputs) override{
        // std::cout << "debug in local predict\n";
        if (pFuncPredict == nullptr) {
            std::cout << "Error: pFuncPredict is nullptr when trying to predict" << std::endl;
            throw new std::runtime_error("Failed to load necessary Python functions");
        }

        PyObject* pList = PyList_New(inputs.size());
        for (size_t i = 0; i < inputs.size(); ++i) {
            PyObject* pFloat = PyFloat_FromDouble(inputs(i));
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

        Py_ssize_t size = 0;
        if (PyList_Check(pResult)) {
            size = PyList_Size(pResult);
        }else{
            throw std::runtime_error("Result is not a list");
        }
        // std::cout << "size: " << size << std::endl;
        Matrix results(size, 1);
        // std::cout <<"debug after matrix\n";

        for (Py_ssize_t i = 0; i < size; ++i) {
            PyObject* pItem = PyList_GetItem(pResult, i);
            if (PyFloat_Check(pItem)) {
                double value = PyFloat_AsDouble(pItem);
                results(i) = value;
            } else {
                // Handle the error for non-float items.
                std::cout << "Error: All items in the result list must be floats.\n" << std::endl;
                Py_DECREF(pResult);
                throw std::runtime_error("Non-float item encountered in prediction results");
            }
        }

        // std::cout << "debug: before return\n";
        Py_DECREF(pResult);
        return results;
    }

    virtual Matrix predict(double input) override{
        Matrix input_mat(1, 1);
        input_mat(0) = input;
        return predict(input_mat);
    }

     virtual void saveModel(std::string path) override{
        // save model in python
        // whose method should be named with "save_model"
        // with params to be a string.
        PyObject* pArgs = Py_BuildValue("(s)", path.c_str());
        PyObject* pResult = PyObject_CallObject(pFuncSave, pArgs);
        Py_DECREF(pArgs);
        Py_DECREF(pResult);
    }

    virtual void loadModel(std::string path) override{
        PyObject* pFuncLoad = PyObject_GetAttrString(pInstance, "load_model");
        PyObject* pArgs = Py_BuildValue("(s)", path.c_str());
        // std::cout << "loading 1\n";
        PyObject* pResult = PyObject_CallObject(pFuncLoad, pArgs);

        // std::cout << "loading 2\n";
        Py_DECREF(pArgs);

        // std::cout << "loading 3\n";
        Py_DECREF(pResult);
        Py_XDECREF(pFuncLoad);
    }


protected:
    PyObject *pModule, *pClass, *pInstance, *pFuncTrainByFile, *pFuncPredict, *pFuncSave;
};
};

#endif
