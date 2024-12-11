package com.ncslab.block.ensembleModel;

import javax.persistence.Embeddable;

import lombok.Getter;
import org.json.JSONObject;

import com.ncslab.block.io.GlobalVariable;
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

import java.util.Vector;

public class EnsembleModel extends Block{
    private Parameter m0, m1, l, initState, g;
    private String solverString;

    protected OutputPort outputPort;
    protected int _width, _height;
    protected ENVariable modelVariable;

    @Getter
    public static final Vector<String> parameterNames = new Vector<>();

    @Getter
    public static final Vector<String> outputNames = new Vector<>();
    @Getter
    public static final Vector<String> inputNames = new Vector<>();

    static {

        outputNames.add("out1");
        inputNames.add("in1");
        parameterNames.add("m0");
        parameterNames.add("m1");
        parameterNames.add("l");
        parameterNames.add("initState");
    }

    public EnsembleModel(JSONObject jsonObject, NCSLabModel model) {
        super(jsonObject, model);

        this.m0 = new Parameter(this, 1, "m0", paramValues.getString("m0"));
        this.m1 = new Parameter(this, 2, "m1", paramValues.getString("m1"));
        this.l = new Parameter(this, 3, "l", paramValues.getString("l"));
        this.initState = new Parameter(this, 4, "initState", paramValues.getString("initState"));

        this.modelVariable = new ENVariable(this, 1, "invertedPendulum", "233");

        this.solverString = paramValues.getString("solver").trim();

        this.parameterList.add(this.m0);
        this.parameterList.add(this.m1);
        this.parameterList.add(this.l);
        this.parameterList.add(this.initState);

        this.inputPortList.add(new InputPort(this, 1));
        this.outputPort = new OutputPort(this, 1);
        this.outputPortList.add(this.outputPort);

        this.globalVariableList.add(this.modelVariable);

        this._height = 1;
        this._width = 4;
    }

    @Override
    public void generateInitCodeC(CodeStructC code){
        super.generateInitCodeC(code);
        code.addIncludeCode("#include \"inverted_pendulum.hpp\"\n");
        code.addWrittenFile("../../ensemble/inverted_pendulum.hpp", "inverted_pendulum.hpp");

        String initCode = "/*Code for initialization of block InvertedPendulumTest:(" + getBlockId() + ")" + getBlockName() + "*/\n";

        initCode += this.m0.getInitCodeC();
        initCode += this.m1.getInitCodeC();
        initCode += this.l.getInitCodeC();
        initCode += "REAL g = 9.8;\n";
        initCode += this.initState.getInitCodeC();

        initCode += "std::vector<double> init_state(4);\n";
        initCode += "for (int i = 0; i < 4; ++i) {\n";
        initCode += "    init_state[i] = InvertedPendulum1_initState(0, i);\n";
        initCode += "}\n";

        initCode += this.modelVariable.getInitCodeC();

        code.addInitCode(initCode);
    }

    @Override
    public void generateOutputCodeC(CodeStructC code) {
        StringBuilder sb = new StringBuilder(String.format(
            "/*Code for output of block %s: (%s) %s*/\n",
            getClass().getSimpleName(),
            getBlockId(),
            getBlockName()));
        OutputPort in_opt1 = inputPortList.get(0).getLinkedLine().getLinkedOutputPort(); // input source's output
        OutputPort out_opt1 = outputPortList.get(0);

        sb.append(String.format("auto %s_mat = %s;\n", this.modelVariable.getName(), in_opt1.getOutputSignalC().getName()));

        sb.append(String.format(
        "{\n" +
        "    std::vector<double> temp_vec = %s->step(%s_mat, 0.01);\n" +
        "    Eigen::MatrixXd temp_mat(temp_vec.size(), 1);\n" +
        "    for (size_t i = 0; i < temp_vec.size(); ++i) {\n" +
        "        temp_mat(i, 0) = temp_vec[i];\n" +
        "    }\n" +
        "    %s = temp_mat;\n" +
        "}\n",
        this.modelVariable.getName(),
        this.modelVariable.getName(),
        out_opt1.getOutputSignalC().getName()));

        code.addOutputCode(sb.toString());
    }

    public String getVariableName() {
        return "InvertedPendulum ";
    }

    public String getVariableParameters() {
        return String.format(
            "new InvertedPendulum(%s, %s, %s, %s, \"%s\", init_state)",
        this.m0.getName(),
        this.m1.getName(),
        this.l.getName(),
        "g",
        this.solverString);
        // this.initState.getName());
    }

    public String getEndCode(){
        return "";
    }

    @Override
    public void checkDimension() throws MatDimException{
		return;
	}

    @Override
    public void updateDimension() throws MatDimException{
        this.outputPort.setHeight(this._width);
        this.outputPort.setWidth(this._height);
        this.outputPort.getOutputSignalC().setHeight(this._width);
        this.outputPort.getOutputSignalC().setWidth(this._height);
        this.outputPort.getOutputSignalC().setDataType(this._width > 1? DataType.MATRIX:DataType.REAL);
    }

    protected class ENVariable extends GlobalVariable{
        public ENVariable(Block block, int id, String localName, String dataString) {
            super(block, id, localName, dataString);
        }

         @Override
        public String getInitCodeC() {
            return String.format("%s = new InvertedPendulum(%s, %s, %s, %s, \"%s\", init_state);\n",
                        ENVariable.this.getName(),
                        EnsembleModel.this.m0.getName(),
                        EnsembleModel.this.m1.getName(),
                        EnsembleModel.this.l.getName(),
                        "g",
                        EnsembleModel.this.solverString);
        }

        @Override
        public String getDefineCodeC() {
            return String.format("%s *%s;\n",
            EnsembleModel.this.getVariableName(),
            ENVariable.this.getName());
        }

        @Override
        public String getEndCodeC() {
            return EnsembleModel.this.getEndCode();
        }
    }

}
