package com.ncslab.block.machineLearning;

import java.util.Vector;

import org.json.JSONObject;

import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.block.BlockType;
import com.ncslab.block.Block;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.block.data.DataType;

public class MultilayerPerceptron extends MachineLearning {
    public Parameter layersNumber, numClasses, 
    // lossFunction, 
    learningRate, epoch,
    inputFeatures, outputFeatures,
    channelSize, hiddenLayers;
    // activationFunction, 
    // dataset;
    private MLVariable modelVariable;
    private String lossFunctionString, datasetString, activationString;

    private OutputPort outputPort;
    private int _height, _width;
    

    String hiddenLayerCode;

    public MultilayerPerceptron(JSONObject jsonObject, NCSLabModel model) {
        super(jsonObject, model);

        CodeStructC.addIncludeCode("#include \"MultilayerPerceptron.hpp\"\n");
        CodeStructC.addWrittenFile("../../../ml/MLP/MultilayerPerceptron.hpp", "MultilayerPerceptron.hpp");
        CodeStructC.addWrittenFile("../../../ml/MLP/multilayer_perceptron_model.py", "multilayer_perceptron_model.py");

        this.inputFeatures = new Parameter(this, 1, "inputFeatures", paramValues.getString("inputFeatures"));
        this.outputFeatures = new Parameter(this, 7, "outputFeatures", paramValues.getString("outputFeatures"));
        this.hiddenLayers = new Parameter(this, 2, "hiddenLayers", paramValues.getString("hiddenLayers"));  // New parameter for hidden layers
        // this.activationFunction = new Parameter(this, 3, "activationFunction", paramValues.getString("activationFunction"));
        this.learningRate = new Parameter(this, 4, "learningRate", paramValues.getString("learningRate"));
        // this.lossFunction = new Parameter(this, 5, "lossFunction", paramValues.getString("lossFunction"));
        // this.dataset = new Parameter(this, 6, "dataset", paramValues.getString("dataset"));
        this.modelVariable = new MLVariable(this, 1, "multilayerPerceptron", "2333");
        this.epoch = new Parameter(this, 8, "epoch", paramValues.getString("epoch"));
        
        this.lossFunctionString = paramValues.getString("lossFunction").trim().toUpperCase();
        this.datasetString = paramValues.getString("dataset").trim().toLowerCase();
        this.activationString = paramValues.getString("activationFunction").trim().toLowerCase();


        this.parameterList.add(this.inputFeatures);

        this.parameterList.add(this.hiddenLayers); // Add hidden layers parameter to the list

        // this.parameterList.add(this.activationFunction);
        this.parameterList.add(this.learningRate);
        this.parameterList.add(this.epoch);
        // this.parameterList.add(this.lossFunction);
        // this.parameterList.add(this.dataset);
        this.globalVariableList.add(this.modelVariable);

        this.inputPortList.add(new InputPort(this, 1));
        this.outputPort = new OutputPort(this, 1);
        this.outputPortList.add(this.outputPort);
        // this.outputPortList.add(new OutputPort(this, 1));

        this._width = 1;
        this._height = (int)(Double.parseDouble(paramValues.getString("outputFeatures")));

        switch (hiddenLayers.getDataType()) {
            case REAL:
                this.hiddenLayerCode = String.format("%s", hiddenLayers.getName());
                break;
            case MATRIX:
                this.hiddenLayerCode = hiddenLayers.getName();
                break;
            default:
                break;
        }
    }

    @Override
    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);

        String initCode = "/*Code for initialization of block MLTest:(" + getBlockId() + ")" + getBlockName() + "*/\n";

        initCode += this.inputFeatures.getInitCodeC();
        initCode += this.outputFeatures.getInitCodeC();
        initCode += this.hiddenLayers.getInitCodeC();
        // initCode += this.activationFunction.getInitCodeC();
        initCode += this.learningRate.getInitCodeC();
        initCode += this.epoch.getInitCodeC();
        // initCode += this.lossFunction.getInitCodeC();
        // initCode += this.dataset.getInitCodeC();

        

        // get the hidden layers of the model.
        

        initCode += this.modelVariable.getInitCodeC();
        // initCode += String.format("%s->set_activation_function(\"%s\");\n", 
        //                 this.modelVariable.getName(), 
        //                 this.activationString);

        //train the model.
        initCode += String.format("%s->trainModel(\"/home/square/ncslablink/ncslablink/src/main/java/com/ncslab/code/c/%s.csv\", %s, %s);\n",   
                        this.modelVariable.getName(), 
                        this.datasetString, 
                        this.epoch.getName(), 
                        this.learningRate.getName());

        code.addInitCode(initCode);
    }

    @Override
    public void generateOutputCodeC(CodeStructC code) {
        // StringBuilder sb = new StringBuilder("/*Code for output of block Multilayer Perceptron:(" + getBlockId() + ")" + getBlockName() + "*/\n");
        // sb.append(String.format("std::vector<double> %s_v = {1.0,2.0,3.0};", this.modelVariable.getName()));
        // sb.append(outputPortList.get(0).getOutputSignalC().getName());
        // sb.append(String.format("=%s->predict(%s_v)[0];\n",
        //         this.modelVariable.getName(),
        //         this.modelVariable.getName()));
        // code.addOutputCode(sb.toString());

        StringBuilder sb = new StringBuilder("/*Code for output of block Multilayer Perceptron:(" + getBlockId() + ")" + getBlockName() + "*/\n");

        OutputPort in_opt1 = inputPortList.get(0).getLinkedLine().getLinkedOutputPort(); // input source's output
        OutputPort out_opt1 = outputPortList.get(0);

        sb.append(String.format("auto %s_v = %s;\n", this.modelVariable.getName(), in_opt1.getOutputSignalC().getName()));
        
        

        switch (this._height) {
            case 1:
                // 调用 predict 方法返回 double
                sb.append(out_opt1.getOutputSignalC().getName());
                sb.append(String.format("=%s->predict(%s_v)[0];\n",
                this.modelVariable.getName(),
                this.modelVariable.getName()));
                break;
        
            default:
                // 调用 predict 方法返回 std::vector<double>
                sb.append(String.format(
                    "std::vector<double> result = %s->predict(%s_v);\n",
                    this.modelVariable.getName(),
                    this.modelVariable.getName()
                ));
                // 定义 Eigen::VectorXd 并进行转换
                sb.append("Matrix result_vector(result.size());\n");
                sb.append("for (size_t i = 0; i < result.size(); ++i) {\n");
                sb.append("    result_vector(i) = result[i];\n");
                sb.append("}\n");
                // 赋值给输出信号
                sb.append(out_opt1.getOutputSignalC().getName());
                sb.append("=result_vector;\n");
                break;
        }
        // sb.append(String.format("std::vector<double> %s_v = {1.0,2.0,3.0};", this.modelVariable.getName(), inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName()));
        
        code.addOutputCode(sb.toString());
    }

    @Override
    public void updateDimension() throws MatDimException{
        this.outputPort.setHeight(this._height);
        this.outputPort.setWidth(this._width);
        this.outputPort.getOutputSignalC().setHeight(this._height);
        this.outputPort.getOutputSignalC().setWidth(this._width);
        this.outputPort.getOutputSignalC().setDataType(this._height > 1? DataType.MATRIX:DataType.REAL);    
    }

    @Override
    public String getVariableName() {
        return "std::unique_ptr<MultilayerPerceptron>";
    }

    @Override
    public String getVariableParameters() {
        return String.format("std::make_unique<MultilayerPerceptron>(size_t(%s), size_t(%s), %s, \"%s\")", 
        this.inputFeatures.getName(), 
        this.outputFeatures.getName(), 
        this.hiddenLayerCode,
        this.lossFunctionString);
    }
}
