package com.ncslab.block.machineLearning.pt;

import com.ncslab.block.machineLearning.MachineLearning;
import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.machineLearning.pt.MultilayerPerceptronDto;

import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.NCSLabModel;

import java.util.HashMap;
import java.util.Map;

public class MultilayerPerceptron extends PTModel {
    private Parameter inputFeatures,
                outputFeatures,
                hiddenLayers,
                learningRate,
                epochs;
    private String lossFunctionString, datasetString, activationString;


    
    
    /**
     * DTO-NATIVE Constructor - Creates MultilayerPerceptron block directly from BlockDto DTO
     */
    public MultilayerPerceptron(BlockDto blockDto, NCSLabModel model) {
        super(blockDto, model);
        System.out.println("DTO-NATIVE: MultilayerPerceptron block created successfully - " + blockDto.getBlockName());
    }


    public static final Map<String, String> PARAMETER_DEFAULTS;
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("inputFeatures", "1");
        PARAMETER_DEFAULTS.put("outputFeatures", "1");
        PARAMETER_DEFAULTS.put("hiddenLayers", "[64, 32]");
        PARAMETER_DEFAULTS.put("learningRate", "0.01");
        PARAMETER_DEFAULTS.put("epoch", "100");
        PARAMETER_DEFAULTS.put("lossFunction", "MSE");
        PARAMETER_DEFAULTS.put("dataset", "winddata1.csv");
        PARAMETER_DEFAULTS.put("activationFunction", "relu");
        PARAMETER_DEFAULTS.put("loadPath", "None");
        PARAMETER_DEFAULTS.put("savePath", "None");

    }
    public MultilayerPerceptron(JSONObject jsonObject, NCSLabModel model) {
        super(jsonObject, model);

        this.inputFeatures = new Parameter(this, 1, "inputFeatures", paramValues.getString("inputFeatures"));
        this.outputFeatures = new Parameter(this, 2, "outputFeatures", paramValues.getString("outputFeatures"));
        this.hiddenLayers = new Parameter(this, 3, "hiddenLayers", paramValues.getString("hiddenLayers"));  // New parameter for hidden layers
        this.learningRate = new Parameter(this, 4, "learningRate", paramValues.getString("learningRate"));
        this.epochs = new Parameter(this, 5, "epoch", paramValues.getString("epoch"));

        this.modelVariable = new MachineLearning.MLVariable(this, 1, "multilayerPerceptron", "2333");

        this.lossFunctionString = paramValues.getString("lossFunction").trim().toUpperCase();
        this.datasetString = paramValues.getString("dataset").trim();
        this.activationString = paramValues.getString("activationFunction").trim().toLowerCase();

        this.loadPath = paramValues.getString("loadPath").trim();
        this.savePath = paramValues.getString("savePath").trim();
        this.globalVariableList.add(this.modelVariable);

        this._height = 1;
        this._width = (int)(Double.parseDouble(paramValues.getString("outputFeatures")));
    }

    @Override
    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);
        code.addIncludeCode("#include \"MultilayerPerceptron.hpp\"\n");
        code.addWrittenFile("../../ml/pt/MultilayerPerceptron.hpp", "MultilayerPerceptron.hpp");
        code.addWrittenFile("../../ml/pt/multilayer_perceptron_model.py", "multilayer_perceptron_model.py");

        context.put("block", this);
        context.put("inputFeatures", inputFeatures);
        context.put("outputFeatures", outputFeatures);
        context.put("hiddenLayers", hiddenLayers);
        context.put("learningRate", learningRate);
        context.put("epochs", epochs);
        context.put("modelVariable", modelVariable);
        context.put("loadPath", loadPath);
        context.put("datasetString", datasetString);

        String codeStr = com.ncslab.util.TemplateManager.renderTemplate("c/machineLearning/pt/MultilayerPerceptron/init.vm", context);
        code.addInitCode(codeStr);
    }

    @Override
    public String getVariableName() {
        return "std::unique_ptr<MultilayerPerceptron>";
    }

    @Override
    public String getVariableParameters() {
        return String.format("std::make_unique<MultilayerPerceptron>(size_t(%s), size_t(%s), %s, \"%s\", \"%s\")",
        this.inputFeatures.getName(),
        this.outputFeatures.getName(),
        this.hiddenLayers.getName(),
        this.lossFunctionString,
        this.activationString);
    }
}
