package com.ncslab.block.machineLearning.pt;

import com.ncslab.block.machineLearning.MachineLearning;
import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.dto.BlockJson;

import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.NCSLabModel;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class CNN extends MachineLearning {
    public Parameter layersNumber, numClasses, lossFunction,
    learningRate, inputFeatures,
    channelSize, hiddenLayers,
    activationFunction, dataset;
    private MLVariable modelVariable;


    
    
    /**
     * DTO-NATIVE Constructor - Creates CNN block directly from BlockJson DTO
     */
    public CNN(BlockJson blockDto, NCSLabModel model) {
        super(blockDto, model);
        System.out.println("DTO-NATIVE: CNN block created successfully - " + blockDto.getBlockName());
    }


    public static final Map<String, String> PARAMETER_DEFAULTS;
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("inputFeatures", "3");
        PARAMETER_DEFAULTS.put("numClasses", "10");
        PARAMETER_DEFAULTS.put("activationFunction", "relu");
        PARAMETER_DEFAULTS.put("learningRate", "0.001");
        PARAMETER_DEFAULTS.put("lossFunction", "cross_entropy");
        PARAMETER_DEFAULTS.put("channelSize", "32");
        PARAMETER_DEFAULTS.put("hiddenLayers", "[64, 32]");
        PARAMETER_DEFAULTS.put("dataset", "winddata1.csv");

    }

    public static final Vector<String> outputNames = new Vector<>();
    public static final Vector<String> inputNames = new Vector<>();

    static {
        outputNames.add("out1");
        inputNames.add("in1");
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

        context.put("block", this);
        context.put("inputFeatures", inputFeatures);
        context.put("activationFunction", activationFunction);
        context.put("learningRate", learningRate);
        context.put("lossFunction", lossFunction);
        context.put("channelSize", channelSize);
        context.put("dataset", dataset);
        context.put("modelVariable", modelVariable);

        String codeStr = com.ncslab.util.TemplateManager.renderTemplate("c/machineLearning/pt/CNN/init.vm", context);
        code.addInitCode(codeStr);
    }
    @Override
    public void generateOutputCodeC(CodeStructC code) {
        super.generateOutputCodeC(code);
        
        String outputSignal = outputPortList.get(0).getOutputSignalC().getName();
        
        context.put("block", this);
        context.put("modelVariable", modelVariable);
        context.put("outputSignal", outputSignal);
        
        String codeStr = com.ncslab.util.TemplateManager.renderTemplate("c/machineLearning/pt/CNN/output.vm", context);
        code.addOutputCode(codeStr);
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
