package com.ncslab.block.testrig;

import com.ncslab.util.TemplateManager;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.dto.core.BlockDto;

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
	private List<State> xStateList = new ArrayList<>();

    
    
    /**
     * DTO-NATIVE Constructor - Creates Alp block directly from BlockDto DTO
     */
    public Alp(BlockDto blockDto, NCSLabModel model) {
        super(blockDto, model);
        System.out.println("DTO-NATIVE: Alp block created successfully - " + blockDto.getBlockName());
    }



    public static final List<String> outputNames = new ArrayList<>();
    public static final List<String> inputNames = new ArrayList<>();

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
		hardwareDefineName="Block"+this.getBlockId()+"_Alp";
		com.ncslab.util.TemplateUtils.populateAllContext(context, this);
		context.put("hardwareDefineName", hardwareDefineName);
		
		return TemplateManager.renderTemplate("c/testrig/Alp/hardware_define.vm", context);
	}
public void generateInitCodeC(CodeStructC code) {
    super.generateInitCodeC(code);

    com.ncslab.util.TemplateUtils.populateAllContext(context, this);

    String codeStr = TemplateManager.renderTemplate("c/testrig/Alp/init.vm", context);
    code.addInitCode(codeStr);
}
	public void generateOutputCodeC(CodeStructC code) {
	        com.ncslab.util.TemplateUtils.populateAllContext(context, this);
	        context.put("states", xStateList);
	        context.put("num", Arrays.stream(num).boxed().collect(Collectors.toList()));
	
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
