package com.ncslab.block.machineLearning;

import org.json.JSONObject;

import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.block.BlockType;
import com.ncslab.block.Block;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.block.data.DataType;
import com.ncslab.block.machineLearning.LinearRegression;
import com.ncslab.block.machineLearning.MachineLearning;

public class LogisticRegression extends MachineLearning {

    public LogisticRegression(JSONObject jsonObject, NCSLabModel model) {
        super(jsonObject, model);
    }

    public void generateInitCodeC(CodeStructC code) {
		super.generateInitCodeC(code);

		String initCode="/*Code for initialization of block MLTest:("+getBlockId()+")"+getBlockName()+"*/\n";

        // initCode += this.inputFeatures.getInitCodeC();
        // initCode += this.activationFunction.getInitCodeC();
        // initCode += this.learningRate.getInitCodeC();
        // initCode += this.lossFunction.getInitCodeC();
        // initCode += this.dataset.getInitCodeC();

        // initCode += "Py_Initialize();\n";
        // initCode += "PyRun_SimpleString(\"import sys\");\n";
        // initCode += "PyRun_SimpleString(\"sys.path.append('./')\");\n";
        // initCode += "linearRegressionModel = std::make_unique<LinearRegression>(3,1);\n";
        // initCode += "linearRegressionModel->trainModel(\"/home/square/ncslablink/ncslablink/src/main/java/com/ncslab/code/c/winddata1.csv\", 100, 0.01);\n";
        code.addInitCode(initCode);
    }

    @Override
    public void generateOutputCodeC(CodeStructC code) {
        StringBuilder sb = new StringBuilder("/*Code for output of block Constant:("+getBlockId()+")"+getBlockName()+"*/\n");
        
        sb.append("std::vector<double> v = {1.0,2.0,3.0};");
        sb.append(outputPortList.get(0).getOutputSignalC().getName());
        
        sb.append("=linearRegressionModel->predict(v)[0];\n");
        
        code.addOutputCode(sb.toString());
    }

    @Override
    public String getVariableName() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getVariableName'");
    }

    @Override
    public String getVariableParameters() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getVariableParameters'");
    }
}
