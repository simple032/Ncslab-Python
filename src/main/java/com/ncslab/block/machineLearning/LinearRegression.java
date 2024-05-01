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
    // static{
    //     BlockType.put("DJDYYDS", DJDYYDS::new);
    // }
    // layersNumber: "",
    //                 inputFeatures: "",
    //                 activationFunction: "",
    //                 dataset: ""

    private Parameter layersNumber, lossFunction, learningRate, inputFeatures, activationFunction, dataset;

    

    public LinearRegression(JSONObject jsonObject, NCSLabModel model){
        super(jsonObject, model);

        // this.layersNumber = new Parameter(this, 1, "layersNumber", paramValues.getString("layersNumber"));
        // this.inputFeatures = new Parameter(this, 2, "inputFeatures", paramValues.getString("inputFeatures"));
        // this.activationFunction = new Parameter(this, 3, "activationFunction", paramValues.getString("activationFunction"));
        // this.dataset = new Parameter(this, 4, "dataset", paramValues.getString("dataset"));
        
        // this.parameterList.add(layersNumber);
        // this.parameterList.add(inputFeatures);
        // this.parameterList.add(activationFunction);
        // this.parameterList.add(dataset);

       
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

        // currently, we do not have input port

        this.outputPortList.add(new OutputPort(this, 1));
    }

    @Override
    public void generateInitCodeC(CodeStructC code) {
		super.generateInitCodeC(code);
		
		String initCode="/*Code for initialization of block MLTest:("+getBlockId()+")"+getBlockName()+"*/\n";
		// initCode+=value.getName()+"="+paramValues.getDouble("Value")+";\n";
		// initCode+="#include \"/home/square/ncslablink/ncslablink/src/main/java/com/ncslab/code/c/DJDscgjxyss.h\"\n";
		// initCode+="#include \"DJDscgjxyss.h\"\n";

		// initCode+=this.inputFeatures.getInitCodeC();
		// initCode+=this.layersNumber.getInitCodeC();
        // initCode+=this.activationFunction.getInitCodeC();
        // initCode+=this.dataset.getInitCodeC();

        initCode += this.inputFeatures.getInitCodeC();
        initCode += this.activationFunction.getInitCodeC();
        initCode += this.learningRate.getInitCodeC();
        initCode += this.lossFunction.getInitCodeC();
        initCode += this.dataset.getInitCodeC();
        initCode += "initModel();\n";
        initCode += "train_model(machineLearningModel);\n";

		code.addInitCode(initCode);
	}

    @Override
    public void generateOutputCodeC(CodeStructC code) {
		// String outputCode=;
		StringBuilder sb = new StringBuilder("/*Code for output of block Constant:("+getBlockId()+")"+getBlockName()+"*/\n");
		// switch(this.layersNumber.getDataType()) {
		// case REAL:
			// outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"="+this.layersNumber.getName()+"+funcionaaa(layersNumber, inputFeature, inputFeature,activationFunction);\n";
			sb.append(outputPortList.get(0).getOutputSignalC().getName());
            // sb.append(String.format("=funcionaaa(%s, %s, %s, %s);\n", inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName(), this.inputFeatures.getName(), this.activationFunction.getName(), this.dataset.getName()));
            sb.append(String.format("=getResult(machineLearningModel, %s,%s,%f);\n", 
                            inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName(), 
                            inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName(),
                            0.0));
            // sb.append(String.format("=getTestResult(machineLearningModel, 1.0);\n");
            // sb.append(String.format("=getTestResult(machineLearningModel, %s);\n", inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName()));
            // sb.append("=1.0;\n");
            //     break;
		// case MATRIX:
			// outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"="+this.layersNumber.getName()+";\n";
			/*
			for(int i=0;i<value.getHeight();i++) {
				for(int j=0;j<value.getWidth();j++) {A
					outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"("+i+","+j+")="+value.getName()+"["+i+"]["+j+"];\n";
				}
			}*/
		// 	break;
		// }
		code.addOutputCode(sb.toString());
	}


    // static DJDYYDS create(JSONObject jsonObject, NCSLabModel model){
    //     return new DJDYYDS(jsonObject, model);
    // }
}
