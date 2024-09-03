package com.ncslab.block.machineLearning;

import java.util.Vector;

import org.json.JSONObject;

import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.block.BlockType;
import com.ncslab.block.Block;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.block.data.DataType;

public class LinearRegression extends MachineLearning{
    private Parameter 
            inputFeatures,
            outputFeatures, 
            learningRate, 
            epochs;
            
    private String lossString, datasetString;

    public LinearRegression(JSONObject jsonObject, NCSLabModel model){
        super(jsonObject, model);

        CodeStructC.addIncludeCode("#include \"LinearRegression.hpp\"\n");
        CodeStructC.addWrittenFile("../../../ml/LR/LinearRegression.hpp", "LinearRegression.hpp");
        CodeStructC.addWrittenFile("../../../ml/LR/linear_regression_model.py", "linear_regression_model.py");
        
        this.inputFeatures = new Parameter(this, 1, "inputFeatures", paramValues.getString("inputFeatures"));
        this.outputFeatures = new Parameter(this, 2, "outputFeatures", paramValues.getString("outputFeatures"));
        this.epochs = new Parameter(this, 3, "epochs", paramValues.getString("epochs"));
        this.learningRate = new Parameter(this, 4, "learningRate", paramValues.getString("learningRate"));
        this.modelVariable = new MLVariable(this, 1, "linearRegression", "2333");

        this.lossString = paramValues.getString("lossFunction").trim().toUpperCase();
        this.datasetString = paramValues.getString("dataset").trim().toLowerCase();

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


    // @Override
    // public void generateOutputCodeC(CodeStructC code) {
    //     StringBuilder sb = new StringBuilder("/*Code for output of block Linear Regression:("+getBlockId()+")"+getBlockName()+"*/\n");
        
    //     sb.append(String.format("std::vector<double> %s_v = {1.0,2.0,3.0};", this.modelVariable.getName()));
    //     sb.append(outputPortList.get(0).getOutputSignalC().getName());     
    //     sb.append(String.format("=%s->predict(%s_v)[0];\n", 
    //                 this.modelVariable.getName(),
    //                 this.modelVariable.getName()));
        
    //     code.addOutputCode(sb.toString());
    // }

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
