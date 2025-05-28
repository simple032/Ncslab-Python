package com.ncslab.block.machineLearning.pt;

import com.ncslab.block.machineLearning.MachineLearning;
import lombok.Getter;
import org.json.JSONObject;

import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.NCSLabModel;

import java.util.Vector;

public class CNN extends MachineLearning {
    public Parameter layersNumber, numClasses, lossFunction,
    learningRate, inputFeatures,
    channelSize, hiddenLayers,
    activationFunction, dataset;
    private MLVariable modelVariable;

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
        parameterNames.add("numClasses");
        parameterNames.add("activationFunction");
        parameterNames.add("learningRate");
        parameterNames.add("lossFunction");
        parameterNames.add("channelSize");
        parameterNames.add("hiddenLayers");
        parameterNames.add("dataset");

    }
    public CNN(JSONObject jsonObject, NCSLabModel model){
        super(jsonObject, model);

        this.inputFeatures = new Parameter(this, 1, "inputFeatures", paramValues.getString("inputFeatures"));
        this.numClasses = new Parameter(this, 2, "numClasses", paramValues.getString("numClasses"));
        this.activationFunction = new Parameter(this, 3, "activationFunction", paramValues.getString("activationFunction"));
        this.learningRate = new Parameter(this, 4, "learningRate", paramValues.getString("learningRate"));
        this.lossFunction = new Parameter(this, 5,"lossFunction", paramValues.getString("lossFunction"));
        this.channelSize = new Parameter(this, 6, "channelSize", paramValues.getString("channelSize"));
        this.hiddenLayers = new Parameter(this, 7, "hiddenLayers", paramValues.getString("hiddenLayers"));//todo: matrix 2 vector
        this.dataset = new Parameter(this, 8, "dataset", paramValues.getString("dataset"));
        this.modelVariable = new MLVariable(this, 1, "CNN", "2333");
        parameterList.add(this.inputFeatures);
        parameterList.add(this.numClasses);
        parameterList.add(this.activationFunction);
        parameterList.add(this.learningRate);
        parameterList.add(this.lossFunction);
        parameterList.add(this.channelSize);
        parameterList.add(this.hiddenLayers);
        parameterList.add(this.dataset);
        this.globalVariableList.add(this.modelVariable);

        this.inputPortList.add(new InputPort(this, 1));
        this.outputPortList.add(new OutputPort(this, 1));
    }

    @Override
    public void generateInitCodeC(CodeStructC code) {
		super.generateInitCodeC(code);

        code.addIncludeCode("#include \"CNN.hpp\"\n");
        code.addWrittenFile("../../ml/pt/CNN.hpp", "CNN.hpp");
        code.addWrittenFile("../../ml/pt/cnn_model.py", "cnn_model.py");

		String initCode="";

        initCode += this.inputFeatures.getInitCodeC();
        initCode += this.activationFunction.getInitCodeC();
        initCode += this.learningRate.getInitCodeC();
        initCode += this.lossFunction.getInitCodeC();
        initCode += this.channelSize.getInitCodeC();
        initCode += this.channelSize.getInitCodeC();
        initCode += this.dataset.getInitCodeC();
        initCode += this.modelVariable.getInitCodeC();

        initCode += String.format("%s->trainModel(\"/home/square/ncslablink/ncslablink/src/main/java/com/ncslab/code/c/winddata1.csv\", %d, %f);\n", this.modelVariable.getName(), 100, 0.01);

        code.addInitCode(initCode);
    }


    @Override
    public void generateOutputCodeC(CodeStructC code) {
        StringBuilder sb = new StringBuilder("/*Code for output of block CNN:("+getBlockId()+")"+getBlockName()+"*/\n");

        sb.append(String.format("std::vector<double> %s_v = {1.0,2.0,3.0};", this.modelVariable.getName()));
        sb.append(outputPortList.get(0).getOutputSignalC().getName());
        sb.append(String.format("=%s->predict(%s_v)[0];\n",
                    this.modelVariable.getName(),
                    this.modelVariable.getName()));

        code.addOutputCode(sb.toString());
    }

    @Override
    public String getVariableName() {
        return "std::unique_ptr<CNN>";
    }

    @Override
    public String getVariableParameters() {
        return String.format("std::make_unique<CNN>(%s, %s, {1,2})",
         this.numClasses.getName(),
         this.channelSize.getName(),
         this.hiddenLayers.getName());
    }

}
