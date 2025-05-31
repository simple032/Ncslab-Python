package com.ncslab.block.hardware.stm32;

import lombok.Getter;
import org.json.JSONObject;

import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.util.TemplateManager;

import java.util.Vector;

public class UDPSender extends com.ncslab.block.Block{

	Parameter remoteIp,remotePort;


    @Getter
    public static final Vector<String> parameterNames = new Vector<>();


    @Getter
    public static final Vector<String> inputNames = new Vector<>();

    static {
        parameterNames.add("remoteIp");
        parameterNames.add("remotePort");
        inputNames.add("in1");
    }
	public UDPSender(JSONObject blockJSON, NCSLabModel model) {
		super(blockJSON,model);

		//һ������
		inputPortList.add(new InputPort(this,1));

		remoteIp=new Parameter(this,1,"remoteIp",paramValues.getString("UdpSenderRemoteIpForStm32"));
		parameterList.add(remoteIp);
		remotePort=new Parameter(this,2,"remotePort",paramValues.getString("UdpSenderRemotePortForStm32"));
		parameterList.add(remotePort);
	}

	public void generateInitCodeM(CodeStructM code) {
		super.generateInitCodeM(code);

		String initCode="";
		code.addInitCode(initCode);
	}

	public void generateOutputCodeM(CodeStructM code) {
		super.generateOutputCodeM(code);
		String outputCode="";

		code.addOutputCode(outputCode);
	}

	public void generateInitCodeC(CodeStructC code) {
		super.generateInitCodeC(code);

		context.put("block", this);
		context.put("remoteIP",  remoteIp.getInitString());
		context.put("remotePort",  remotePort.getData().getIntValue());
		context.put("inputSignal",  inputPortList.get(0) .getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName());
		code.addInitCode(TemplateManager.renderTemplate("c/hardware/stm32/UDPSender/init.vm", context));
		// Removed unused initCode reference
	}

	public void generateOutputCodeC(CodeStructC code) {
		context.put("block", this);
	
		code.addOutputCode(TemplateManager.renderTemplate("c/hardware/stm32/UDPSender/output.vm", context));

		// Removed unused outputCode reference
	}
}
