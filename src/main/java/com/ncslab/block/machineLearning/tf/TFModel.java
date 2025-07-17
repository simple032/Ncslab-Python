package com.ncslab.block.machineLearning.tf;

import com.ncslab.block.machineLearning.MachineLearning;
import com.ncslab.ncslablink.NCSLabModel;
import org.json.JSONObject;

public abstract class TFModel extends MachineLearning {
    public TFModel(JSONObject jsonObject, NCSLabModel model) {
        super(jsonObject, model);
    }
}
