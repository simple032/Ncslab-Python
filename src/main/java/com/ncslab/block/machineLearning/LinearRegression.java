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
    public Parameter layersNumber, lossFunction, learningRate, inputFeatures, activationFunction, dataset;
    private MLVariable modelVariable;
    public LinearRegression(JSONObject jsonObject, NCSLabModel model){
        super(jsonObject, model);

        this.inputFeatures = new Parameter(this, 1, "inputFeatures", paramValues.getString("inputFeatures"));
        this.activationFunction = new Parameter(this, 2, "activationFunction", paramValues.getString("activationFunction"));
        this.learningRate = new Parameter(this, 3, "learningRate", paramValues.getString("learningRate"));
        this.lossFunction = new Parameter(this, 4,"lossFunction", paramValues.getString("lossFunction"));
        this.dataset = new Parameter(this, 5, "dataset", paramValues.getString("dataset"));
        this.modelVariable = new MLVariable(this, 1, "linearRegression", "2333");

        this.parameterList.add(this.inputFeatures);
        this.parameterList.add(this.activationFunction);
        this.parameterList.add(this.learningRate);
        this.parameterList.add(this.lossFunction);
        this.parameterList.add(this.dataset);
        this.globalVariableList.add(this.modelVariable);

        this.inputPortList.add(new InputPort(this, 1));
        this.outputPortList.add(new OutputPort(this, 1));
    }

    @Override
    public void generateInitCodeC(CodeStructC code) {
		super.generateInitCodeC(code);

		String initCode="/*Code for initialization of block MLTest:("+getBlockId()+")"+getBlockName()+"*/\n";

        initCode += this.inputFeatures.getInitCodeC();
        initCode += this.activationFunction.getInitCodeC();
        initCode += this.learningRate.getInitCodeC();
        initCode += this.lossFunction.getInitCodeC();
        initCode += this.dataset.getInitCodeC();
        // initCode += "Py_Initialize();\n";
        // initCode += "PyRun_SimpleString(\"import sys\");\n";
        // initCode += "PyRun_SimpleString(\"sys.path.append('./')\");\n";
        initCode += this.modelVariable.getInitCodeC();

        initCode += String.format("%s->trainModel(\"/home/square/ncslablink/ncslablink/src/main/java/com/ncslab/code/c/winddata1.csv\", %d, %f);\n", this.modelVariable.getName(), 100, 0.01);
        // initCode += "linearRegressionModel = std::make_unique<LinearRegression>(3,1);\n";
        // initCode += "linearRegressionModel->trainModel(\"/home/square/ncslablink/ncslablink/src/main/java/com/ncslab/code/c/winddata1.csv\", 100, 0.01);\n";
        code.addInitCode(initCode);
    }


    @Override
    public void generateOutputCodeC(CodeStructC code) {
        StringBuilder sb = new StringBuilder("/*Code for output of block Linear Regression:("+getBlockId()+")"+getBlockName()+"*/\n");
        
        sb.append(String.format("std::vector<double> %s_v = {1.0,2.0,3.0};", this.modelVariable.getName()));
        sb.append(outputPortList.get(0).getOutputSignalC().getName());     
        sb.append(String.format("=%s->predict(%s_v)[0];\n", 
                    this.modelVariable.getName(),
                    this.modelVariable.getName()));
        
        code.addOutputCode(sb.toString());
    }

    @Override
    public String getVariableName() {
        // TODO Auto-generated method stub
        // throw new UnsupportedOperationException("Unimplemented method 'getVariableName'");
        return "std::unique_ptr<LinearRegression>";
    }

    @Override
    public String getVariableParameters() {
        // TODO Auto-generated method stub
        // throw new UnsupportedOperationException("Unimplemented method 'getVariableParameters'");
        // Vector<String> v = new Vector<String>();
        // v.add("3");
        // v.add("1");
        // return v;
        return "std::make_unique<LinearRegression>(3,1)";
    }
}
