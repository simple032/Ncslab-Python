package com.ncslab.block.machineLearning.pt;

import com.ncslab.block.machineLearning.MachineLearning;
import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.dto.BlockJson;

import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.NCSLabModel;

import java.util.HashMap;
import java.util.Map;

public class A2C extends PTModel {
    private Parameter inputFeatures,
                outputFeatures,
                learningRate,
                discountFactor;


    
    
    /**
     * DTO-NATIVE Constructor - Creates A2C block directly from BlockJson DTO
     */
    public A2C(BlockJson blockDto, NCSLabModel model) {
        super(blockDto, model);
        System.out.println("DTO-NATIVE: A2C block created successfully - " + blockDto.getBlockName());
    }


    public static final Map<String, String> PARAMETER_DEFAULTS;
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("inputFeatures", "1");
        PARAMETER_DEFAULTS.put("outputFeatures", "1");
        PARAMETER_DEFAULTS.put("learningRate", "0.001");
        PARAMETER_DEFAULTS.put("discountFactor", "0.99");
        PARAMETER_DEFAULTS.put("loadPath", "None");
        PARAMETER_DEFAULTS.put("savePath", "None");

    }

    public A2C(JSONObject jsonObject, NCSLabModel model) {
        super(jsonObject, model);

        // create the 4 parameters from paramValues
        this.inputFeatures = new Parameter(this, 1, "inputFeatures", paramValues.getString("inputFeatures"));
        this.outputFeatures = new Parameter(this, 2, "outputFeatures", paramValues.getString("outputFeatures"));
        this.learningRate = new Parameter(this, 4, "learningRate", paramValues.getString("learningRate"));
        this.discountFactor = new Parameter(this, 5, "discountFactor", paramValues.getString("discountFactor"));

        // create A2C model
        this.modelVariable = new MachineLearning.MLVariable(this, 1, "a2c", "2333");

        // get the path string from paramValues
        this.loadPath = paramValues.getString("loadPath").trim();
        this.savePath = paramValues.getString("savePath").trim();

        // add the 4 parameters to parameterList

        // add the model to globalVariableList
        this.globalVariableList.add(this.modelVariable);

        // control the output matrix size
        this._height = 1;
        this._width = (int)(Double.parseDouble(paramValues.getString("outputFeatures")));
    }

    @Override
    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);

        // include necessary terms
        code.addIncludeCode("#include \"A2C.hpp\"\n");
        code.addWrittenFile("../../ml/pt/A2C.hpp", "A2C.hpp");
        code.addWrittenFile("../../ml/pt/A2C.py", "A2C.py");

        context.put("block", this);
        context.put("inputFeatures", inputFeatures);
        context.put("outputFeatures", outputFeatures);
        context.put("learningRate", learningRate);
        context.put("discountFactor", discountFactor);
        context.put("modelVariable", modelVariable);
        context.put("loadPath", loadPath);

        String codeStr = com.ncslab.util.TemplateManager.renderTemplate("c/machineLearning/pt/A2C/init.vm", context);
        code.addInitCode(codeStr);
    }

    @Override
    public String getVariableName() {
        return "A2C*";
    }

    @Override
    public String getVariableParameters() {
        return String.format("new A2C(size_t(%s), size_t(%s), %s, %s)",
            this.inputFeatures.getName(),
            this.outputFeatures.getName(),
            this.learningRate.getName(),
            this.discountFactor.getName());
    }

    @Override
    public String getEndCode() {
        return String.format("delete %s;\n", this.modelVariable.getName());
    }
}
