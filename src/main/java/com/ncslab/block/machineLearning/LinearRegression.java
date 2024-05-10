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

public class LinearRegression extends Block{
    private Parameter layersNumber, lossFunction, learningRate, inputFeatures, activationFunction, dataset;
    public LinearRegression(JSONObject jsonObject, NCSLabModel model){
        super(jsonObject, model);

        this.inputFeatures = new Parameter(this, 1, "inputFeatures", paramValues.getString("inputFeatures"));
        this.activationFunction = new Parameter(this, 2, "activationFunction", paramValues.getString("activationFunction"));
        this.learningRate = new Parameter(this, 3, "learningRate", paramValues.getString("learningRate"));
        this.lossFunction = new Parameter(this, 4,"lossFunction", paramValues.getString("lossFunction"));
        this.dataset = new Parameter(this, 5, "dataset", paramValues.getString("dataset"));

        this.parameterList.add(this.inputFeatures);
        this.parameterList.add(this.activationFunction);
        this.parameterList.add(this.learningRate);
        this.parameterList.add(this.lossFunction);
        this.parameterList.add(this.dataset);

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
        initCode += "Py_Initialize();\n";
        initCode += "PyRun_SimpleString(\"import sys\");\n";
        initCode += "PyRun_SimpleString(\"sys.path.append('/home/square/ncslablink/ncslablink/src/main/java/com/ncslab/code/c/ml/LR')\");\n";
        initCode += "initModel();\n";
        initCode += "trainModel(\"/home/square/ncslablink/ncslablink/src/main/java/com/ncslab/code/c/winddata1.csv\", 100, 0.01);\n";
        code.addInitCode(initCode);
    }

    @Override
    public void generateOutputCodeC(CodeStructC code) {
        StringBuilder sb = new StringBuilder("/*Code for output of block Constant:("+getBlockId()+")"+getBlockName()+"*/\n");
        sb.append(outputPortList.get(0).getOutputSignalC().getName());
        sb.append(String.format("=getResult(%s,%s,%f);\n", 
                            inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName(), 
                            inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName(),
                            0.0));
        sb.append("Py_Finalize();\n");
        code.addOutputCode(sb.toString());
    }
}
