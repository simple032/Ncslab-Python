package com.ncslab.block.ensembleModel;

import jakarta.persistence.Embeddable;

import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.dto.BlockJson;

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

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

public class EnsembleModel extends Block{
    private Parameter m0, m1, l, initState, g;
    private String solverString;

    protected OutputPort outputPort;
    protected int _width, _height;
    protected ENVariable modelVariable;


    
    
    /**
     * DTO-NATIVE Constructor - Creates EnsembleModel block directly from BlockJson DTO
     */
    public EnsembleModel(BlockJson blockDto, NCSLabModel model) {
        super(blockDto, model);
        System.out.println("DTO-NATIVE: EnsembleModel block created successfully - " + blockDto.getBlockName());
    }


    public static final Map<String, String> PARAMETER_DEFAULTS;
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("m0", "1.0");
        PARAMETER_DEFAULTS.put("m1", "0.1");
        PARAMETER_DEFAULTS.put("l", "0.5");
        PARAMETER_DEFAULTS.put("initState", "[0 0 0 0]");
        PARAMETER_DEFAULTS.put("solver", "ode4");

    }

    public static final List<String> outputNames = new ArrayList<>();
    public static final List<String> inputNames = new ArrayList<>();

    static {
        outputNames.add("out1");
        inputNames.add("in1");
    }

    public EnsembleModel(JSONObject jsonObject, NCSLabModel model) {
        super(jsonObject, model);

        this.m0 = new Parameter(this, 1, "m0", paramValues.getString("m0"));
        this.m1 = new Parameter(this, 2, "m1", paramValues.getString("m1"));
        this.l = new Parameter(this, 3, "l", paramValues.getString("l"));
        this.initState = new Parameter(this, 4, "initState", paramValues.getString("initState"));

        this.modelVariable = new ENVariable(this, 1, "invertedPendulum", "233");

        this.solverString = paramValues.getString("solver").trim();
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

        context.put("block", this);
        context.put("m0", m0);
        context.put("m1", m1);
        context.put("l", l);
        context.put("initState", initState);
        context.put("modelVariable", modelVariable);

        String codeStr = com.ncslab.util.TemplateManager.renderTemplate("c/ensembleModel/EnsembleModel/init.vm", context);
        code.addInitCode(codeStr);
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
        
        String codeStr = com.ncslab.util.TemplateManager.renderTemplate("c/ensembleModel/EnsembleModel/output.vm", context);
        code.addOutputCode(codeStr);
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
