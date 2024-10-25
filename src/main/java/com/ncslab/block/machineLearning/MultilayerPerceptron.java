package com.ncslab.block.machineLearning;

import java.util.Vector;

import org.json.JSONObject;

import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.block.BlockType;
import com.greenpineyu.fel.parser.FelParser.primary_return;
import com.ncslab.block.Block;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.block.data.DataType;

public class MultilayerPerceptron extends MachineLearning {
    private Parameter inputFeatures,
                outputFeatures,
                hiddenLayers,
                learningRate,
                epoch;
    private String lossFunctionString, datasetString, activationString;


    public MultilayerPerceptron(JSONObject jsonObject, NCSLabModel model) {
        super(jsonObject, model);

        this.inputFeatures = new Parameter(this, 1, "inputFeatures", paramValues.getString("inputFeatures"));
        this.outputFeatures = new Parameter(this, 2, "outputFeatures", paramValues.getString("outputFeatures"));
        this.hiddenLayers = new Parameter(this, 3, "hiddenLayers", paramValues.getString("hiddenLayers"));  // New parameter for hidden layers
        this.learningRate = new Parameter(this, 4, "learningRate", paramValues.getString("learningRate"));
        this.epoch = new Parameter(this, 5, "epoch", paramValues.getString("epoch"));

        this.modelVariable = new MLVariable(this, 1, "multilayerPerceptron", "2333");

        this.lossFunctionString = paramValues.getString("lossFunction").trim().toUpperCase();
        this.datasetString = paramValues.getString("dataset").trim().toLowerCase();
        this.activationString = paramValues.getString("activationFunction").trim().toLowerCase();

        this.parameterList.add(this.inputFeatures);
        this.parameterList.add(this.outputFeatures);
        this.parameterList.add(this.hiddenLayers);
        this.parameterList.add(this.learningRate);
        this.parameterList.add(this.epoch);

        this.globalVariableList.add(this.modelVariable);

        this._height = 1;
        this._width = (int)(Double.parseDouble(paramValues.getString("outputFeatures")));
    }

    @Override
    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);
        code.addIncludeCode("#include \"MultilayerPerceptron.hpp\"\n");
        code.addWrittenFile("../../ml/MLP/MultilayerPerceptron.hpp", "MultilayerPerceptron.hpp");
        code.addWrittenFile("../../ml/MLP/multilayer_perceptron_model.py", "multilayer_perceptron_model.py");

        String initCode = "/*Code for initialization of block MLTest:(" + getBlockId() + ")" + getBlockName() + "*/\n";

        initCode += this.inputFeatures.getInitCodeC();
        initCode += this.outputFeatures.getInitCodeC();
        initCode += this.hiddenLayers.getInitCodeC();
        initCode += this.learningRate.getInitCodeC();
        initCode += this.epoch.getInitCodeC();

        // init the model.
        initCode += this.modelVariable.getInitCodeC();

        //train the model.
        initCode += String.format("%s->trainModel(\"/home/square/ncslablink/ncslablink/src/main/java/com/ncslab/code/c/%s.csv\", %s, %s);\n",
                        this.modelVariable.getName(),
                        this.datasetString,
                        this.epoch.getName(),
                        this.learningRate.getName());

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
