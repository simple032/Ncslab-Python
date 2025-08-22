package com.ncslab.block.machineLearning.tf;

import com.ncslab.block.machineLearning.pt.PTModel;
import com.ncslab.ncslablink.NCSLabModel;
import org.json.JSONObject;
import com.ncslab.dto.core.BlockDto;


public class ELinearRegression extends PTModel {
    public ELinearRegression(JSONObject jsonObject, NCSLabModel model) {
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
