package com.ncslab.block.machineLearning.pt;

import com.ncslab.block.machineLearning.MachineLearning;
import org.json.JSONObject;

import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.NCSLabModel;

public class MultilayerPerceptron extends PTModel {
    private Parameter inputFeatures,
                outputFeatures,
                hiddenLayers,
                learningRate,
                epochs;
    private String lossFunctionString, datasetString, activationString;


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

        this.parameterList.add(this.inputFeatures);
        this.parameterList.add(this.outputFeatures);
        this.parameterList.add(this.hiddenLayers);
        this.parameterList.add(this.learningRate);
        this.parameterList.add(this.epochs);

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

        String initCode = "/*Code for initialization of block MLTest:(" + getBlockId() + ")" + getBlockName() + "*/\n";

        initCode += this.inputFeatures.getInitCodeC();
        initCode += this.outputFeatures.getInitCodeC();
        initCode += this.hiddenLayers.getInitCodeC();
        initCode += this.learningRate.getInitCodeC();
        initCode += this.epochs.getInitCodeC();

        // init the model.
        initCode += this.modelVariable.getInitCodeC();

        if (loadPath != null && !loadPath.equals("None")){
            initCode += String.format("%s->loadModel(\"%s\");\n",
                this.modelVariable.getName(),
                this.loadPath);
        }
        if(datasetString != null && !datasetString.equals("None")) {
            initCode += String.format("%s->trainModel(\"%s\", int(%s), %s);\n", this.modelVariable.getName(),
                this.datasetString,
                this.epochs.getName(),
                this.learningRate.getName());
        }

        code.addInitCode(initCode);
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
