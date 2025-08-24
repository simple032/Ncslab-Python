package com.ncslab.block.machineLearning;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.machineLearning.MachineLearningDto;

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
    
    
    /**
     * DTO-NATIVE Constructor - Creates MachineLearning block directly from BlockDto DTO
     */
    public MachineLearning(MachineLearningDto blockDto, NCSLabModel model) {
        super(blockDto, model);
        System.out.println("DTO-NATIVE: MachineLearning block created successfully - " + blockDto.getBlockName());
    }


    public static final List<String> outputNames = new ArrayList<>();
    public static final List<String> inputNames = new ArrayList<>();

    public static final Map<String, String> PARAMETER_DEFAULTS;
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("savePath", "model.bin");
        PARAMETER_DEFAULTS.put("loadPath", "None");
    }

    static {

        outputNames.add("out1");
        inputNames.add("in1");
    }

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
        code.addIncludeCode("#include \"MLModel.hpp\"\n");
        code.addWrittenFile("../../ml/MLModel.hpp", "MLModel.hpp");
        
        context.put("block", this);
        
        String codeStr = com.ncslab.util.TemplateManager.renderTemplate("c/machineLearning/MachineLearning/init.vm", context);
        code.addInitCode(codeStr);
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
        super.generateOutputCodeC(code);
        
        OutputPort in_opt1 = inputPortList.get(0).getLinkedLine().getLinkedOutputPort();
        OutputPort out_opt1 = outputPortList.get(0);
        
        context.put("block", this);
        context.put("modelVariable", modelVariable);
        context.put("inputSignal", in_opt1.getOutputSignalC().getName());
        context.put("outputSignal", out_opt1.getOutputSignalC().getName());
        context.put("width", _width);
        
        String codeStr = com.ncslab.util.TemplateManager.renderTemplate("c/machineLearning/MachineLearning/output.vm", context);
        code.addOutputCode(codeStr);
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
