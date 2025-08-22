package com.ncslab.block.machineLearning.pt;

import com.ncslab.block.machineLearning.MachineLearning;
import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.dto.core.BlockDto;

import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.NCSLabModel;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

public class LinearRegression extends PTModel{
    private Parameter
            inputFeatures,
            outputFeatures,
            learningRate,
            epochs;

    private String lossString, datasetString;


    
    
    /**
     * DTO-NATIVE Constructor - Creates LinearRegression block directly from BlockDto DTO
     */
    public LinearRegression(BlockDto blockDto, NCSLabModel model) {
        super(blockDto, model);
        System.out.println("DTO-NATIVE: LinearRegression block created successfully - " + blockDto.getBlockName());
    }


    public static final Map<String, String> PARAMETER_DEFAULTS;
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("inputFeatures", "1");
        PARAMETER_DEFAULTS.put("outputFeatures", "1");
        PARAMETER_DEFAULTS.put("learningRate", "0.01");
        PARAMETER_DEFAULTS.put("epochs", "100");
        PARAMETER_DEFAULTS.put("lossFunction", "MSE");
        PARAMETER_DEFAULTS.put("dataset", "winddata1.csv");
        PARAMETER_DEFAULTS.put("loadPath", "None");
        PARAMETER_DEFAULTS.put("savePath", "None");

    }

    public static final List<String> outputNames = new ArrayList<>();
    public static final List<String> inputNames = new ArrayList<>();

    static {
        outputNames.add("out1");
        inputNames.add("in1");
    }

    public LinearRegression(JSONObject jsonObject, NCSLabModel model){
        super(jsonObject, model);

        this.inputFeatures = new Parameter(this, 1, "inputFeatures", paramValues.getString("inputFeatures"));
        this.outputFeatures = new Parameter(this, 2, "outputFeatures", paramValues.getString("outputFeatures"));
        this.epochs = new Parameter(this, 3, "epochs", paramValues.getString("epochs"));
        this.learningRate = new Parameter(this, 4, "learningRate", paramValues.getString("learningRate"));
        this.modelVariable = new MachineLearning.MLVariable(this, 1, "linearRegression", "2333");

        this.lossString = paramValues.getString("lossFunction").trim().toUpperCase();
        this.datasetString = paramValues.getString("dataset").trim();

        this.loadPath = paramValues.getString("loadPath").trim();
        this.savePath = paramValues.getString("savePath").trim();
        this.globalVariableList.add(this.modelVariable);

        this._height = 1;
        this._width = (int)(Double.parseDouble(paramValues.getString("outputFeatures")));
    }

    @Override
    public void generateInitCodeC(CodeStructC code) {
		super.generateInitCodeC(code);
        code.addIncludeCode("#include \"LinearRegression.hpp\"\n");
        code.addWrittenFile("../../ml/pt/LinearRegression.hpp", "LinearRegression.hpp");
        code.addWrittenFile("../../ml/pt/linear_regression_model.py", "linear_regression_model.py");

        context.put("block", this);
        context.put("inputFeatures", inputFeatures);
        context.put("outputFeatures", outputFeatures);
        context.put("learningRate", learningRate);
        context.put("epochs", epochs);
        context.put("modelVariable", modelVariable);
        context.put("loadPath", loadPath);
        context.put("datasetString", datasetString);

        String codeStr = com.ncslab.util.TemplateManager.renderTemplate("c/machineLearning/pt/LinearRegression/init.vm", context);
        code.addInitCode(codeStr);
    }

    @Override
    public String getVariableName() {
        return "std::unique_ptr<LinearRegression>";
    }

    @Override
    public String getVariableParameters() {
        return String.format("std::make_unique<LinearRegression>(%s, %s, \"%s\")",
                this.inputFeatures.getName(),
                this.outputFeatures.getName(),
                this.lossString);
    }
}
