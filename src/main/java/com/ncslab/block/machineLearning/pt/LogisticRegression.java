package com.ncslab.block.machineLearning.pt;

import com.ncslab.block.machineLearning.MachineLearning;
import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.machineLearning.pt.LogisticRegressionDto;

import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.NCSLabModel;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

public class LogisticRegression extends PTModel {
    private Parameter
    inputFeatures,
    outputFeatures,
    learningRate,
    epochs;

    private String lossString, datasetString;


    
    
    /**
     * DTO-NATIVE Constructor - Creates LogisticRegression block directly from BlockDto DTO
     */
    public LogisticRegression(LogisticRegressionDto blockDto, NCSLabModel model) {
        super(blockDto, model);
        System.out.println("DTO-NATIVE: LogisticRegression block created successfully - " + blockDto.getBlockName());
    }


    public static final Map<String, String> PARAMETER_DEFAULTS;
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("inputFeatures", "1");
        PARAMETER_DEFAULTS.put("outputFeatures", "1");
        PARAMETER_DEFAULTS.put("learningRate", "0.01");
        PARAMETER_DEFAULTS.put("epochs", "100");
        PARAMETER_DEFAULTS.put("lossFunction", "CROSS_ENTROPY");
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

    public LogisticRegression(JSONObject jsonObject, NCSLabModel model){
        super(jsonObject, model);

        this.inputFeatures = new Parameter(this, 1, "inputFeatures", paramValues.getString("inputFeatures"));
        this.outputFeatures = new Parameter(this, 2, "outputFeatures", paramValues.getString("outputFeatures"));
        this.epochs = new Parameter(this, 3, "epochs", paramValues.getString("epochs"));
        this.learningRate = new Parameter(this, 4, "learningRate", paramValues.getString("learningRate"));
        this.modelVariable = new MachineLearning.MLVariable(this, 1, "logisticRegression", "2333");

        this.lossString = paramValues.getString("lossFunction").trim().toUpperCase();
        this.datasetString = paramValues.getString("dataset").trim().toLowerCase();
        this.globalVariableList.add(this.modelVariable);

        this._height = 1;
        // classification, only stands for one output.
        //i.e. if output features is 3, the output value can be 0, 1 or 2.
        this._width = 1;
        // this._width = (int)(Double.parseDouble(paramValues.getString("outputFeatures")));
    }

    @Override
        public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);

        code.addIncludeCode("#include \"LogisticRegression.hpp\"\n");
        code.addWrittenFile("../../ml/pt/LogisticRegression.hpp", "LogisticRegression.hpp");
        code.addWrittenFile("../../ml/pt/logistic_regression_model.py", "logistic_regression_model.py");

        context.put("block", this);
        context.put("inputFeatures", inputFeatures);
        context.put("outputFeatures", outputFeatures);
        context.put("learningRate", learningRate);
        context.put("epochs", epochs);
        context.put("modelVariable", modelVariable);
        context.put("datasetString", datasetString);

        String codeStr = com.ncslab.util.TemplateManager.renderTemplate("c/machineLearning/pt/LogisticRegression/init.vm", context);
        code.addInitCode(codeStr);
    }

    @Override
    public String getVariableName() {
        // return "std::unique_ptr<LinearRegression>";
        return "LogisticRegression*";
    }

    @Override
    public String getVariableParameters() {
        // return "std::make_unique<LinearRegression>(3,1)";
        return String.format("new LogisticRegression(size_t(%s), size_t(%s), \"%s\");",
            this.inputFeatures.getName(),
            this.outputFeatures.getName(),
            this.lossString);
    }

    @Override
    public String getEndCode(){
        return String.format("delete %s;\n", this.modelVariable.getName());
    }
}
