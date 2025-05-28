package com.ncslab.block.machineLearning.pt;

import com.ncslab.block.machineLearning.MachineLearning;
import lombok.Getter;
import org.json.JSONObject;

import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.NCSLabModel;

import java.util.Vector;

public class LogisticRegression extends PTModel {
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

    public LogisticRegression(JSONObject jsonObject, NCSLabModel model){
        super(jsonObject, model);

        this.inputFeatures = new Parameter(this, 1, "inputFeatures", paramValues.getString("inputFeatures"));
        this.outputFeatures = new Parameter(this, 2, "outputFeatures", paramValues.getString("outputFeatures"));
        this.epochs = new Parameter(this, 3, "epochs", paramValues.getString("epochs"));
        this.learningRate = new Parameter(this, 4, "learningRate", paramValues.getString("learningRate"));
        this.modelVariable = new MachineLearning.MLVariable(this, 1, "logisticRegression", "2333");

        this.lossString = paramValues.getString("lossFunction").trim().toUpperCase();
        this.datasetString = paramValues.getString("dataset").trim().toLowerCase();

        parameterList.add(this.inputFeatures);
        parameterList.add(this.outputFeatures);
        parameterList.add(this.learningRate);
        parameterList.add(this.epochs);

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

        code.addIncludeCode("#include \"LogisticRegression.hpp\"\n");
        code.addWrittenFile("../../ml/pt/LogisticRegression.hpp", "LogisticRegression.hpp");
        code.addWrittenFile("../../ml/pt/logistic_regression_model.py", "logistic_regression_model.py");

        String initCode="";

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
