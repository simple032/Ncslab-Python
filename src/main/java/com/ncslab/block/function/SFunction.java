package com.ncslab.block.function;

import com.ncslab.block.data.DataType;
import com.ncslab.util.TemplateManager;
import org.apache.velocity.VelocityContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import lombok.Getter;
import com.ncslab.block.discrete.DiscreteBlock;
import com.ncslab.block.io.*;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.ModelException;
import org.json.JSONObject;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.function.SFunctionDto;

import com.ncslab.block.Block;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.NCSLabModel;

public class SFunction extends Block {

    private String name = "S-Function";

    private String fcnName = "";
    private String fileName = "";

    private String functionCode="";

    String simStructName="";

    private int parameterNum = 0;
    private int inputNum;
    private int outputNum;
    private int numContState;
    private int numDiscState;

    private String[] inputPortWidth;
    private String[] outputPortWidth;

    private String[] sampleTimes;
    private String[] offsetTimes;

    private Parameter sampleTime;
    State speedState;

    
    
    /**
     * DTO-NATIVE Constructor - Creates SFunction block directly from BlockDto DTO
     */
    public SFunction(SFunctionDto blockDto, NCSLabModel model) throws ModelException{
        super(blockDto, model);
        System.out.println("DTO-NATIVE: SFunction block created successfully - " + blockDto.getBlockName());
    }


    public static final Map<String, String> PARAMETER_DEFAULTS = new HashMap<>();

    static {

        PARAMETER_DEFAULTS.put("HasCodeCompiled", "false");
        PARAMETER_DEFAULTS.put("FunctionName", "sfunc");
        PARAMETER_DEFAULTS.put("InputNum", "1");
        PARAMETER_DEFAULTS.put("OutputNum", "1");
        PARAMETER_DEFAULTS.put("NumContState", "0");
        PARAMETER_DEFAULTS.put("NumDiscState", "0");
        PARAMETER_DEFAULTS.put("Parameters", "");
        PARAMETER_DEFAULTS.put("InputPortWidth", "1");
        PARAMETER_DEFAULTS.put("OutputPortWidth", "1");
        PARAMETER_DEFAULTS.put("SampleTimes", "-1");
        PARAMETER_DEFAULTS.put("OffsetTimes", "0");
    }

    public SFunction(JSONObject blockJSON,NCSLabModel model) throws ModelException{
        super(blockJSON,model);

        if(!paramValues.optBoolean("HasCodeCompiled",false)) {
            throw new ModelException(blockName+" hasn't been compiled.");
        }
        fcnName = paramValues.getString("FunctionName");
        fileName = fcnName+"_"+blockName.replace(name, "");
//		functionCode= paramValues.getString("SFunctionCode");

        inputNum = paramValues.getInt("InputNum");
        outputNum = paramValues.getInt("OutputNum");
        numContState = paramValues.getInt("NumContState");
        numDiscState = paramValues.getInt("NumDiscState");

        for (int i = 0; i < inputNum; i++) {
            inputPortList.add(new InputPort(this, i+1));
        }
        for (int i = 0; i < outputNum; i++) {
            outputPortList.add(new OutputPort(this,i+1,false));
        }
        for (int i = 0; i < numContState; i++) {
            stateList.add(new State(this, i+1, "Cont["+i+"]"));
        }
        for (int i = 0; i < numDiscState; i++) {
            stateList.add(new State(this, i+1+numContState, "Disc["+i+"]"));//不知道有没有问题
        }

        String parameters=paramValues.getString("Parameters");
        if(parameters.length()>0) {
            String[] para=parameters.split(",");
            for (int i = 0; i < para.length; i++) {
            }
            parameterNum=para.length;
        }

        inputPortWidth = paramValues.getString("InputPortWidth").split(",");
        outputPortWidth = paramValues.getString("OutputPortWidth").split(",");
        for (int i = 0; i < outputPortWidth.length; i++) {
            outputPortList.get(i).setWidth(Integer.parseInt(outputPortWidth[i]));
        }

        sampleTimes = paramValues.getString("SampleTimes").split(",");
        offsetTimes = paramValues.getString("OffsetTimes").split(",");
        sampleTime = new Parameter(this, 1+parameterNum, "sampleTime", sampleTimes[0]);

        simStructName=blockName.replace("-", "");
    }

    public List<InputPort> getInputPortFromSFcn(String filename) {
        List<InputPort> lip = new ArrayList<>();
        int num = 1;
        for(int i=0; i<num; i++)
        {
            lip.add(new InputPort(this, i+1));
        }
        return lip;
    }

    public void setOutputPortNum(int num) {
        for(int i=0; i<num; i++) {
            outputPortList.add(new OutputPort(this, i+1));
        }
    }

    public void generateInitCodeM(CodeStructM code) {
        super.generateInitCodeM(code);
        String initCode="";
//		initCode+=pumpState.getName()+"=0;\n";
//		initCode+=levelState.getName()+"=0;\n";

        code.addInitCode(initCode);
    }

    public void generateDerivativeCodeM(CodeStructM code) {
        super.generateDerivativeCodeM(code);

        String derivativeCode="";

//		derivativeCode+=pumpState.getDerivativeName()+"=("
//				+this.getInputPortVariable(0)
//				+"*"+pumpK+"-"+pumpState.getName()+")"
//				+"*"+(1/pumpT)
//				+";\n";
//
//		derivativeCode+=levelState.getDerivativeName()+"=("
//				+pumpState.getName()+"*"+waterLevelK+"-"+levelState.getName()+")"
//				+"*"+(1/waterLevelT)
//				+";\n";

        code.addDerivativeCode(derivativeCode);
    }

    public void generateOutputCodeM(CodeStructM code) {
        super.generateOutputCodeM(code);
        String outputCode="";

//		outputCode+=getOutputPortVariable(0)+"="
//				+pumpState.getName()
//				+";\n";
//
//		outputCode+=getOutputPortVariable(1)+"="
//				+levelState.getName()
//				+ ";\n";

        code.addOutputCode(outputCode);
    }

    public void generateArraysCodeC(CodeStructC code) {

        context.put("realDataType", DataType.REAL);
        context.put("matrixDataType", DataType.MATRIX);
        context.put("simStructName", simStructName);
        context.put("sfcnName", getSFcnName());
        context.put("blockId", blockId);
        context.put("numContState", numContState);
        context.put("numDiscState", numDiscState);

        String arraysCode = TemplateManager.renderTemplate("c/function/SFunction/arrays.vm", context);
        code.addArraysCode(arraysCode);
    }

    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);
        context.put("block", this);
        context.put("parameterList", parameterList);
        context.put("simStructName", simStructName);
        context.put("blockId", blockId);
        context.put("numContState", numContState);
        context.put("numDiscState", numDiscState);
        context.put("sfcnName", getSFcnName());
        context.put("sampleTime", sampleTime);

        String codeStr = TemplateManager.renderTemplate("c/function/SFunction/init.vm", context);
        code.addInitCode(codeStr);
    }

    public void generateIncludeCodeC(CodeStructC code) {
//		String includeCode="/*Code for include files of block " + name + ":("+getBlockId()+")"+getBlockName()+"*/\n";
//		code.addIncludeCode(includeCode);
    }

    public void generateOutputCodeC(CodeStructC code) {
        context.put("block", this);
        context.put("name", name);
        context.put("simStructName", simStructName);
        context.put("numDiscState", numDiscState);

        String codeStr = TemplateManager.renderTemplate("c/function/SFunction/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void  generateDerivativeCodeC(CodeStructC code) {
        context.put("block", this);
        context.put("name", name);
        context.put("simStructName", simStructName);

        String codeStr = TemplateManager.renderTemplate("c/function/SFunction/derivative.vm", context);
        code.addDerivativeCode(codeStr);
    }

    public void generateUpdateCodeC(CodeStructC code) throws MatDimException{
        context.put("block", this);
        context.put("numContState", numContState);
        context.put("stateList", stateList);

        String codeStr = TemplateManager.renderTemplate("c/function/SFunction/update.vm", context);
        code.addUpdateCode(codeStr);
    }

    public void generateDiscreteUpdateCodeCInside(CodeStructC code) throws MatDimException{
        context.put("block", this);
        context.put("simStructName", simStructName);

        String codeStr = TemplateManager.renderTemplate("c/function/SFunction/discrete_update.vm", context);
        code.addDiscreteUpdateCode(codeStr);
    }

    public void generateStatementCodeC(CodeStructC code) {
        context.put("block", this);
        context.put("name", name);

        String codeStr = TemplateManager.renderTemplate("c/function/SFunction/statement.vm", context);
        code.addStatementCode(codeStr);
    }

    public void generateTerminateCodeC(CodeStructC code) {
        context.put("block", this);
        context.put("simStructName", simStructName);

        String codeStr = TemplateManager.renderTemplate("c/function/SFunction/terminate.vm", context);
        code.addTerminateCode(codeStr);
    }

    public boolean isSFcnBlock() {
        return true;
    }

    public String getSFcnName() {
        return this.fcnName;
    }

    public String getSFunctionCode() {
        return this.functionCode;
    }

    public String getFileName() {
        return fileName;
    }

    public void updateDimension() throws MatDimException {
        super.updateDimension();
    }

    public void checkDimension() throws MatDimException{
        for (int i = 0; i < inputPortWidth.length; i++) {
            OutputSignal out = inputPortList.get(i).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
            if(out.getHeight() == 1 && out.getWidth() == Integer.parseInt(inputPortWidth[i]) || out.getWidth() == 1 && out.getHeight() == Integer.parseInt(inputPortWidth[i])) {
                continue;
            }else {
                throw new MatDimException("Block"+this.blockId+"("+this.getBlockName()+")input port "+(i+1)+" dimension error.\n");
            }
        }
    }
}

