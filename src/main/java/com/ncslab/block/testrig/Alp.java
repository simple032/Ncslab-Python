package com.ncslab.block.testrig;

import com.ncslab.util.TemplateManager;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.Vector;

import lombok.Getter;
import org.json.JSONObject;

import com.ncslab.block.Block;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.State;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.NCSLabModel;

public class Alp extends Block {
	String hardwareDefineName;
	private double num[] = {0.1308,0,0};
	private double den[]= {1,3.091,1.19,0.2};
	private Vector<State> xStateList=new Vector<State>();



    @Getter
    public static final Vector<String> outputNames = new Vector<>();
    @Getter
    public static final Vector<String> inputNames = new Vector<>();

    static {

        outputNames.add("FanSpeed");
        outputNames.add("Position");
        inputNames.add("in1");

    }
	public Alp(JSONObject blockJSON,NCSLabModel model) {
        super(blockJSON,model);
		this.isHardware=true;
		inputPortList.add(new InputPort(this,1));
		outputPortList.add(new OutputPort(this,"FanSpeed",1,false));
		outputPortList.add(new OutputPort(this,"Position",2,false));
		switch(model.getModelMode()) {
		case Simulation:
			for(int i=0;i<3;i++) {
				State xState=new State(this,i+1,"x"+(i+1));
				xStateList.add(xState);
				stateList.add(xState);
			}
			break;
		case Compilation:
			break;
			}
	}
	public String getHardwareDefineCodeC() {
		String hardwareDefineCode="";
		hardwareDefineName="Block"+this.getBlockId()+"_Alp";
		hardwareDefineCode+="ALP "+hardwareDefineName+";\n";
		return hardwareDefineCode;
	}
public void generateInitCodeC(CodeStructC code) {
    super.generateInitCodeC(code);

    context.put("block", this);
    context.put("states", stateList);
    context.put("modelMode", model.getModelMode().name());

    String codeStr = TemplateManager.renderTemplate("c/testrig/Alp/init.vm", context);
    code.addInitCode(codeStr);
}
	public void generateOutputCodeC(CodeStructC code) {
	        context.put("block", this);
	        context.put("states", xStateList);
	        context.put("num", Arrays.stream(num).boxed().collect(Collectors.toList()));
	        context.put("modelMode", model.getModelMode().name());
	        context.put("inputPortVariable", getInputPortVariable(0));
	        context.put("outputPortVariables", getOutputPortVariables());
	
	        String codeStr = TemplateManager.renderTemplate("c/testrig/Alp/output.vm", context);
	        code.addOutputCode(codeStr);
	    }
	public void  generateDerivativeCodeC(CodeStructC code) {
		String derivativeCode="/*Code for Derivative of ALP" + ":("+getBlockId()+")"+getBlockName()+"*/\n";

		switch(model.getModelMode()) {
		case Simulation:
			for(int i=0;i<xStateList.size()-1;i++) {
				derivativeCode+=xStateList.get(i).getDerivativeName()+"="
						+xStateList.get(i+1).getName()
						+";\n";
			}
			derivativeCode+=xStateList.get(xStateList.size()-1).getDerivativeName()+"=("+getInputPortVariable(0);
			int i=den.length-1;
			for(State xState:xStateList) {
				derivativeCode+="-"+xState.getName()+"*"+den[i];
				i--;
			}
			derivativeCode+=");\n";
		     break;
		case Compilation:
			break;
		}

		code.addDerivativeCode(derivativeCode);
	}
}
