package com.ncslab.block.function;

import java.util.Vector;

import com.ncslab.block.discrete.DiscreteBlock;
import com.ncslab.block.io.*;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.ModelException;
import org.json.JSONObject;

import com.ncslab.block.Block;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.NCSLabModel;

public class SFunction extends DiscreteBlock {

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
                parameterList.add(new Parameter(this, i+1, "para"+(i+1), para[i].replaceAll(" ", "")));
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
        parameterList.add(sampleTime);
        setSampleTime(sampleTime);

        simStructName=blockName.replace("-", "");
    }

    public Vector<InputPort> getInputPortFromSFcn(String filename) {
        Vector<InputPort> lip = new Vector<InputPort>();
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
        String arraysCode="SimStruct S"+this.simStructName+";\n";
        arraysCode+="SimStruct *"+this.simStructName+"=&S"+this.simStructName+";\n";
        //the main function of S-Function
        arraysCode+="extern void "+this.getSFcnName()+"(SimStruct* S);\n";
        if(this.numContState>0) {
            arraysCode+="REAL Block"+this.blockId+"_State_Cont["+this.numContState+"] = { 0 };\n";
            arraysCode+="REAL Block"+this.blockId+"_State_Derivative["+this.numContState+"] = { 0 };\n";
        }
        if(this.numDiscState>0) {
            arraysCode+="REAL Block"+this.blockId+"_State_Disc["+this.numDiscState+"] = { 0 };\n";
            arraysCode+="REAL Block"+this.blockId+"_State_Derivative["+this.numDiscState+"] = { 0 };\n";
        }

        code.addArraysCode(arraysCode);
    }

    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);
        String initCode="/*Code for initialization of block " + name + ":("+getBlockId()+")"+getBlockName()+"*/\n";

        for(Parameter parameter:parameterList) {
            initCode+=parameter.getInitCodeC();
        }
        initCode+=this.simStructName+"->parentBlock=&block"+this.getBlockId()+";\n";

        if(this.numContState>0) {
            initCode+=this.simStructName+"->states.contStates = Block"+this.blockId+"_State_Cont;\n";
            initCode+=this.simStructName+"->states.derivative = Block"+this.blockId+"_State_Derivative;\n";
        }
        if(this.numDiscState>0) {
            initCode+=this.simStructName+"->states.discStates = Block"+this.blockId+"_State_Disc;\n";
            initCode+=this.simStructName+"->states.derivative = Block"+this.blockId+"_State_Derivative;\n";
        }

        initCode+=this.getSFcnName()+"("+this.simStructName+");\n";
        initCode+="(*"+this.simStructName+"->initializeSizes)("+this.simStructName+");\n";
        initCode+="(*"+this.simStructName+"->initializeSampleTimes)("+this.simStructName+");\n";
        initCode+="(*"+this.simStructName+"->initializeConditions)("+this.simStructName+");\n";
        initCode+="(*"+this.simStructName+"->start)("+this.simStructName+");\n";

        initCode+="sample_time[sample_i]="+sampleTime.getName()+";\n";
        initCode+="sample_i=sample_i+1;\n";
        code.addInitCode(initCode);
    }

    public void generateIncludeCodeC(CodeStructC code) {
//		String includeCode="/*Code for include files of block " + name + ":("+getBlockId()+")"+getBlockName()+"*/\n";
//		code.addIncludeCode(includeCode);
    }

    public void generateOutputCodeC(CodeStructC code) {
        String outputCode="/*Code for output of block " + name + ":("+getBlockId()+")"+getBlockName()+"*/\n";
        if(this.numDiscState>0) {
            outputCode+="if(block"+this.getBlockId()+".discreteTime<=mp->time||"+"block"+this.getBlockId()+".discreteTime-mp->time<0.0000001){\n";
            outputCode+="(*"+this.simStructName+"->outputs)("+this.simStructName+",0);\n";
            outputCode+="}\n";
        }else {
            outputCode+="(*"+this.simStructName+"->outputs)("+this.simStructName+",0);\n";
        }
        code.addOutputCode(outputCode);
    }

    public void  generateDerivativeCodeC(CodeStructC code) {
        String derivativeCode="/*Code for Derivative of " + name + ":("+getBlockId()+")"+getBlockName()+"*/\n";
        derivativeCode+="(*"+this.simStructName+"->derivatives)("+this.simStructName+");\n";
        code.addDerivativeCode(derivativeCode);
    }

    public void generateUpdateCodeC(CodeStructC code) throws MatDimException{
        String updateCode="/*Code for update of block "+getBlockType()+":("+getBlockId()+")"+getBlockName()+"*/\n";
        for (int i = 0; i < this.numContState; i++) {
            State state = stateList.get(i);
            updateCode+=state.getName()+"="
                +state.getName()+"+"
                +state.getDerivativeName()
                +"*"
                +"model.stepSize"
                +";\n";
        }

        code.addUpdateCode(updateCode);
    }

    public void generateDiscreteUpdateCodeCInside(CodeStructC code) throws MatDimException{
        String discreteUpdateCode="/*Code for discrete update of discrete_transfer_fun(Inside):("+getBlockId()+")"+getBlockName()+"*/\n";
        discreteUpdateCode +="(*"+this.simStructName+"->update)("+this.simStructName+");\n";
        code.addDiscreteUpdateCode(discreteUpdateCode);
    }

    public void generateStatementCodeC(CodeStructC code) {
        String statementCode = "/*Code for statement of " + name + ":("+getBlockId()+")"+getBlockName()+"*/\n";
//		statementCode += "void "+ this.fcnName + "_" + getBlockId() +"(SimStruct* rts);\n";
        code.addStatementCode(statementCode);
    }

    public void generateTerminateCodeC(CodeStructC code) {
        String terminateCode = "";
        terminateCode+="(*"+this.simStructName+"->terminate)("+this.simStructName+");\n";
        code.addTerminateCode(terminateCode);
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

