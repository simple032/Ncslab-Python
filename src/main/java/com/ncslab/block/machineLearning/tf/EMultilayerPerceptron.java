package com.ncslab.block.machineLearning.tf;

import com.ncslab.ncslablink.NCSLabModel;
import org.json.JSONObject;

public class EMultilayerPerceptron extends TFModel{
    public EMultilayerPerceptron(JSONObject jsonObject, NCSLabModel model) {
        super(jsonObject, model);
    }

    @Override
    public String getVariableName() {
        return "";
    }

    @Override
    public String getVariableParameters() {
        return "";
    }
}
