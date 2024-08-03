package com.ncslab.block.machineLearning;

import java.io.IOException;

import org.json.JSONObject;

import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.block.BlockType;
import com.ncslab.block.Block;
import com.ncslab.ncslablink.ModelException;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.block.data.DataType;

public class LogisticRegression extends MachineLearning {
    private Parameter 
    inputFeatures,
    outputFeatures, 
    learningRate, 
    epochs;
    
    private String lossString, datasetString;

    public LogisticRegression(JSONObject jsonObject, NCSLabModel model){
        super(jsonObject, model);

        CodeStructC.addIncludeCode("#include \"LogisticRegression.hpp\"\n");
        CodeStructC.addWrittenFile("../../../ml/LogisticRegression/LogisticRegression.hpp", "LogisticRegression.hpp");
        CodeStructC.addWrittenFile("../../../ml/LogisticRegression/logistic_regression_model.py", "logistic_regression_model.py");

        this.inputFeatures = new Parameter(this, 1, "inputFeatures", paramValues.getString("inputFeatures"));
        this.outputFeatures = new Parameter(this, 2, "outputFeatures", paramValues.getString("outputFeatures"));
        this.epochs = new Parameter(this, 3, "epochs", paramValues.getString("epochs"));
        this.learningRate = new Parameter(this, 4, "learningRate", paramValues.getString("learningRate"));
        this.modelVariable = new MLVariable(this, 1, "logisticRegression", "2333");

        this.lossString = paramValues.getString("lossFunction").trim().toUpperCase();
        this.datasetString = paramValues.getString("dataset").trim().toLowerCase();

        this.parameterList.add(this.inputFeatures);
        this.parameterList.add(this.outputFeatures);
        this.parameterList.add(this.learningRate);
        this.parameterList.add(this.epochs);

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

        String initCode="/*Code for initialization of block MLTest:("+getBlockId()+")"+getBlockName()+"*/\n";

        initCode += this.inputFeatures.getInitCodeC();
        initCode += this.outputFeatures.getInitCodeC();;
        initCode += this.learningRate.getInitCodeC();
        initCode += this.epochs.getInitCodeC();
        initCode += this.modelVariable.getInitCodeC();

        initCode += String.format("%s->trainModel(\"/home/square/ncslablink/ncslablink/src/main/java/com/ncslab/code/c/%s.csv\", %s, %s);\n", 
                    this.modelVariable.getName(), 
                    this.datasetString,
                    this.epochs.getName(),
                    this.learningRate.getName());

        code.addInitCode(initCode);
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
