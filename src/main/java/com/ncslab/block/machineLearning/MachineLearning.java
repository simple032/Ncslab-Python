package com.ncslab.block.machineLearning;

import java.util.Vector;

import org.json.JSONObject;

import com.ncslab.block.io.GlobalVariable;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.block.Block;
import com.ncslab.block.data.DataType;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;

public abstract class MachineLearning extends Block{

    protected OutputPort outputPort;
    protected int _width, _height;
    protected MLVariable modelVariable;
    protected String savePath, loadPath;

    public MachineLearning(JSONObject jsonObject, NCSLabModel model){
        super(jsonObject, model);

        this.inputPortList.add(new InputPort(this, 1));

        this.outputPort = new OutputPort(this, 1, true);
        this.outputPort.setDimThrough(false);
        this.outputPortList.add(this.outputPort);
    }

    @Override
    public void generateInitCodeC(CodeStructC code){
        super.generateInitCodeC(code);
        String initCode = "/*Code for initialization of block MLTest:("+getBlockId()+")"+getBlockName()+"*/\n";
        code.addIncludeCode("#include \"MLModel.hpp\"\n");
        code.addWrittenFile("../../ml/MLModel.hpp", "MLModel.hpp");
        code.addInitCode(initCode);
    }

    @Override
    public void updateDimension() throws MatDimException{
        this.outputPort.setHeight(this._width);
        this.outputPort.setWidth(this._height);
        this.outputPort.getOutputSignalC().setHeight(this._width);
        this.outputPort.getOutputSignalC().setWidth(this._height);
        this.outputPort.getOutputSignalC().setDataType(this._width > 1? DataType.MATRIX:DataType.REAL);
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

        if (this._width == 1) {
            // return double.
            sb.append(out_opt1.getOutputSignalC().getName());
            sb.append(String.format("=%s->predict(%s_mat)(0);\n",
            this.modelVariable.getName(),
            this.modelVariable.getName()));

        }else{
            sb.append(String.format(
                "%s = %s->predict(%s_mat);\n",
                out_opt1.getOutputSignalC().getName(),
                this.modelVariable.getName(),
                this.modelVariable.getName()
            ));
        }

        code.addOutputCode(sb.toString());
    }

    public abstract String getVariableName();

    public abstract String getVariableParameters();

    public String getEndCode(){
        // todo: this part should be run at the end of the main code, but before the end code.
        if (savePath != null && !savePath.equals("None")){
            return String.format("%s->saveModel(\"%s\");\n",
                this.modelVariable.getName(),
                this.savePath);
        }
        return "";
    }

    /**
     * Check the dimension of the input data
     * Default, the machine learning toolbox support the matrix input.
     */
    @Override
    public void checkDimension() throws MatDimException{
		return;
	}

    protected class MLVariable extends GlobalVariable{
        public MLVariable(Block block, int id, String localName, String dataString) {
            super(block, id, localName, dataString);
        }

        @Override
        public String getInitCodeC() {
            return String.format("%s = %s;\n",
                        MLVariable.this.getName(),
                        MachineLearning.this.getVariableParameters());
        }

        @Override
        public String getDefineCodeC() {
            return String.format("%s %s;\n",
            MachineLearning.this.getVariableName(),
            MLVariable.this.getName());
        }

        @Override
        public String getEndCodeC() {
            return MachineLearning.this.getEndCode();
        }
    }
}
