package com.ncslab.block.machineLearning.pt;

import com.ncslab.block.machineLearning.MachineLearning;
import org.json.JSONObject;

import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.NCSLabModel;

public class A2C extends PTModel {
    private Parameter inputFeatures,
                outputFeatures,
                learningRate,
                discountFactor;

    public A2C(JSONObject jsonObject, NCSLabModel model) {
        super(jsonObject, model);

        // create the 4 parameters from paramValues
        this.inputFeatures = new Parameter(this, 1, "inputFeatures", paramValues.getString("inputFeatures"));
        this.outputFeatures = new Parameter(this, 2, "outputFeatures", paramValues.getString("outputFeatures"));
        this.learningRate = new Parameter(this, 4, "learningRate", paramValues.getString("learningRate"));
        this.discountFactor = new Parameter(this, 5, "discountFactor", paramValues.getString("discountFactor"));

        // create A2C model
        this.modelVariable = new MachineLearning.MLVariable(this, 1, "a2c", "2333");

        // get the path string from paramValues
        this.loadPath = paramValues.getString("loadPath").trim();
        this.savePath = paramValues.getString("savePath").trim();

        // add the 4 parameters to parameterList
        this.parameterList.add(this.inputFeatures);
        this.parameterList.add(this.outputFeatures);
        this.parameterList.add(this.learningRate);
        this.parameterList.add(this.discountFactor);

        // add the model to globalVariableList
        this.globalVariableList.add(this.modelVariable);

        // control the output matrix size
        this._height = 1;
        this._width = (int)(Double.parseDouble(paramValues.getString("outputFeatures")));
    }

    @Override
    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);

        // include necessary terms
        code.addIncludeCode("#include \"A2C.hpp\"\n");
        code.addWrittenFile("../../ml/pt/A2C.hpp", "A2C.hpp");
        code.addWrittenFile("../../ml/pt/A2C.py", "A2C.py");

        String initCode = "/*Code for initialization of block MLTest:(" + getBlockId() + ")" + getBlockName() + "*/\n";

        // generate init code for parameters.
        initCode += this.inputFeatures.getInitCodeC();
        initCode += this.outputFeatures.getInitCodeC();
        initCode += this.learningRate.getInitCodeC();
        initCode += this.discountFactor.getInitCodeC();

        // generate init code for the model.
        // ATTENTION: you have to init the model AFTER you finished initializing the parameters that will
        // pass to the constructor of the model in A2C.hpp!
        initCode += this.modelVariable.getInitCodeC();

        // load model.
        if (loadPath != null && !loadPath.equals("None")){
            initCode += String.format("%s->loadModel(\"%s\");\n",
                this.modelVariable.getName(),
                this.loadPath);
        }

        code.addInitCode(initCode);
    }

    @Override
    public String getVariableName() {
        return "A2C*";
    }

    @Override
    public String getVariableParameters() {
        return String.format("new A2C(size_t(%s), size_t(%s), %s, %s)",
            this.inputFeatures.getName(),
            this.outputFeatures.getName(),
            this.learningRate.getName(),
            this.discountFactor.getName());
    }

    @Override
    public String getEndCode() {
        return String.format("delete %s;\n", this.modelVariable.getName());
    }
}
