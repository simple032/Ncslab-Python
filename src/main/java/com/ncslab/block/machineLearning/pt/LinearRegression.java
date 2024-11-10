package com.ncslab.block.machineLearning.pt;

import com.ncslab.block.machineLearning.MachineLearning;
import lombok.Getter;
import org.json.JSONObject;

import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.NCSLabModel;

import java.util.Vector;

public class LinearRegression extends PTModel{
    private Parameter
            inputFeatures,
            outputFeatures,
            learningRate,
            epochs;

    private String lossString, datasetString;

    @Getter
    public static final Vector<String> parameterNames = new Vector<>();

    @Getter
    public static final Vector<String> outputNames = new Vector<>();
    @Getter
    public static final Vector<String> inputNames = new Vector<>();

    static {

        outputNames.add("out1");
        inputNames.add("in1");
        parameterNames.add("inputFeatures");
        parameterNames.add("outputFeatures");
        parameterNames.add("learningRate");
        parameterNames.add("epochs");
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

        this.parameterList.add(this.inputFeatures);
        this.parameterList.add(this.outputFeatures);
        this.parameterList.add(this.learningRate);
        this.parameterList.add(this.epochs);

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

		String initCode="/*Code for initialization of block MLTest:("+getBlockId()+")"+getBlockName()+"*/\n";

        initCode += this.inputFeatures.getInitCodeC();
        initCode += this.outputFeatures.getInitCodeC();;
        initCode += this.learningRate.getInitCodeC();
        initCode += this.epochs.getInitCodeC();
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
