package com.ncslab.block.hardware.stm32;

import lombok.Getter;
import org.json.JSONObject;

import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.util.TemplateManager;

import java.util.Vector;

public class UDPReceiver extends com.ncslab.block.Block{

	Parameter localPort;


    @Getter
    public static final Vector<String> parameterNames = new Vector<>();

    @Getter
    public static final Vector<String> outputNames = new Vector<>();


    static {

        outputNames.add("out1");
        parameterNames.add("localPort");
    }
	public UDPReceiver(JSONObject blockJSON, NCSLabModel model) {
		super(blockJSON,model);

		//һ������
		outputPortList.add(new OutputPort(this,1,false));

		localPort=new Parameter(this,1,"localPort",paramValues.getString("UdpReceiverLocalPortForStm32"));
		parameterList.add(localPort);
	}

	public void generateInitCodeM(CodeStructM code) {
		super.generateInitCodeM(code);

		String initCode="";

//		initCode+=channel.getName()+"="+paramValues.getInt("PWMForStm32Channel")+";\n";
//
//		initCode+="TIM3_PWM_Init(2*1000000*STEP_SIZE-1,100-1,"+paramValues.getInt("PWMForStm32Channel")+");";
//
		code.addInitCode(initCode);
	}

	public void generateOutputCodeM(CodeStructM code) {
		super.generateOutputCodeM(code);
		String outputCode="";

//		outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"="+channel.getName()+";\n";

		code.addOutputCode(outputCode);
	}

	public void generateInitCodeC(CodeStructC code) {
		super.generateInitCodeC(code);

		context.put("block", this);
		context.put("localPort", localPort.getData().getIntValue());
	
		code.addInitCode(TemplateManager.renderTemplate("c/hardware/stm32/UDPReceiver/init.vm", context));
		// Removed unused initCode reference

	}

	public void generateOutputCodeC(CodeStructC code) {
		context.put("block", this);
	
		context.put("outputSignal", getOutputPortList().get(0).getOutputSignalC().getName());
		context.put("localPort", localPort.getData().getIntValue());
		code.addOutputCode(TemplateManager.renderTemplate("c/hardware/stm32/UDPReceiver/output.vm", context));

		// Removed unused outputCode reference
	}
}
