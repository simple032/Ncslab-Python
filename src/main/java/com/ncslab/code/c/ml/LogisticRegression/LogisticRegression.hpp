#ifndef NCS_LOGISTICREGRESSION
#define NCS_LOGISTICREGRESSION
#include "MLModel.hpp"

using ncsml::MLModel;
class LogisticRegression : public MLModel {
public:
    LogisticRegression(size_t input_layer, size_t output_layer) : MLModel("logistic_regression_model") {
        //init logistic regression model
        PyObject* pArgs = Py_BuildValue("(ii)", input_layer, output_layer);
        PyObject_CallObject(this->pFuncInitModel, pArgs);
        Py_DECREF(pArgs);
    }

    ~LogisticRegression() {
    }
};

#endif