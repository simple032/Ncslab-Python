package com.ncslab.block.machineLearning;

import java.util.Vector;

import org.json.JSONObject;

import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.block.BlockType;
import com.greenpineyu.fel.parser.FelParser.primary_return;
import com.ncslab.block.Block;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.block.data.DataType;

public class A2C extends MachineLearning {
    private Parameter inputFeatures,
                outputFeatures,
                learningRate;
    private String modelString;


    public A2C(JSONObject jsonObject, NCSLabModel model) {
        super(jsonObject, model);

        CodeStructC.addIncludeCode("#include \"A2C.hpp\"\n");
        CodeStructC.addWrittenFile("../../../ml/A2C/A2C.hpp", "A2C.hpp");
        CodeStructC.addWrittenFile("../../../ml/A2C/A2C.py", "A2C.py");

        this.inputFeatures = new Parameter(this, 1, "inputFeatures", paramValues.getString("inputFeatures"));
        this.outputFeatures = new Parameter(this, 2, "outputFeatures", paramValues.getString("outputFeatures"));
        this.learningRate = new Parameter(this, 4, "learningRate", paramValues.getString("learningRate"));

        this.modelVariable = new MLVariable(this, 1, "a2c", "2333");

        this.modelString = paramValues.getString("model").trim().toLowerCase();

        this.parameterList.add(this.inputFeatures);
        this.parameterList.add(this.outputFeatures);
        this.parameterList.add(this.learningRate);

        this.globalVariableList.add(this.modelVariable);

        this._height = 1;
        this._width = (int)(Double.parseDouble(paramValues.getString("outputFeatures")));
    }

    @Override
    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);

        String initCode = "/*Code for initialization of block MLTest:(" + getBlockId() + ")" + getBlockName() + "*/\n";

        initCode += this.inputFeatures.getInitCodeC();
        initCode += this.outputFeatures.getInitCodeC();
        initCode += this.learningRate.getInitCodeC();

        // init the model.
        initCode += this.modelVariable.getInitCodeC();

        // load model.
        initCode += String.format("%s->load_model(\"/home/square/ncslablink/ncslablink/src/main/java/com/ncslab/code/c/%s.pt\");\n",
        this.modelVariable.getName(),
        this.modelString);

        code.addInitCode(initCode);
    }

    // @Override
    // public void generateOutputCodeC(CodeStructC code) {
    //     StringBuilder sb = new StringBuilder("/*Code for output of block Multilayer Perceptron:(" + getBlockId() + ")" + getBlockName() + "*/\n");

    //     OutputPort in_opt1 = inputPortList.get(0).getLinkedLine().getLinkedOutputPort(); // input source's output
    //     OutputPort out_opt1 = outputPortList.get(0);

    //     sb.append(String.format("auto %s_v = %s;\n", this.modelVariable.getName(), in_opt1.getOutputSignalC().getName()));

    //     if (this._width == 1) {
    //         // 调用 predict 方法返回 double
    //         sb.append(out_opt1.getOutputSignalC().getName());
    //         sb.append(String.format("=%s->predict(%s_v)[0];\n",
    //         this.modelVariable.getName(),
    //         this.modelVariable.getName()));

    //     }else{
    //         // 调用 predict 方法返回 std::vector<double>
    //         sb.append(String.format(
    //             "std::vector<double> result = %s->predict(%s_v);\n",
    //             this.modelVariable.getName(),
    //             this.modelVariable.getName()
    //         ));
    //         // 定义 Eigen::VectorXd 并进行转换
    //         sb.append(String.format("Matrix result_vector(%s);\n", out_opt1.getOutputSignalC().getName()));
    //         sb.append("for (size_t i = 0; i < result.size(); ++i) {\n");
    //         sb.append("    result_vector(i) = result[i];\n");
    //         sb.append("}\n");
    //         // 赋值给输出信号
    //         sb.append(out_opt1.getOutputSignalC().getName());
    //         sb.append("=result_vector;\n");
    //     }

    //     code.addOutputCode(sb.toString());
    // }

    @Override
    public void updateDimension() throws MatDimException{
        this.outputPort.setHeight(this._width);
        this.outputPort.setWidth(this._height);
        this.outputPort.getOutputSignalC().setHeight(this._width);
        this.outputPort.getOutputSignalC().setWidth(this._height);
        this.outputPort.getOutputSignalC().setDataType(this._width > 1? DataType.MATRIX:DataType.REAL);
    }

    @Override
    public String getVariableName() {
        return "A2C*";
    }

    @Override
    public String getVariableParameters() {
        return String.format("new A2C(size_t(%s), size_t(%s))",
        this.inputFeatures.getName(),
        this.outputFeatures.getName());
    }

    @Override
    public String getEndCode() {
        return String.format("delete %s;\n", this.modelVariable.getName());
    }
}
