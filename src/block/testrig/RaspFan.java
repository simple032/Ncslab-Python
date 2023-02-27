package block.testrig;

import org.json.JSONObject;

import block.Block;
import block.io.InputPort;
import block.io.OutputPort;
import block.io.State;
import code.c.CodeStructC;
import ncslablink.NCSLabModel;

public class RaspFan extends Block {
	String hardwareDefineName;
	public RaspFan(JSONObject blockJSON,NCSLabModel model) {
        super(blockJSON,model);
		this.isHardware=true;
		inputPortList.add(new InputPort(this,1));
		outputPortList.add(new OutputPort(this,"FanSpeed",1,false));
	}
	public String getHardwareDefineCodeC() {
		String hardwareDefineCode="";
		hardwareDefineName="Block"+this.getBlockId()+"_RaspFan";
		hardwareDefineCode+="RASPFAN "+hardwareDefineName+";\n";
		hardwareDefineCode+="HANDLE hComm;\n";
		return hardwareDefineCode;
	}
	public void generateInitCodeC(CodeStructC code) {
		super.generateInitCodeC(code);
		String initCode="/*Code for initialization of block RaspFan:("+getBlockId()+")"+getBlockName()+"*/\n";
		switch(model.getModelMode()) {
		case Simulation:
			break;
		case Compilation:
			hardwareDefineName="Block"+this.getBlockId()+"_RaspFan";
			initCode+="initRaspFan(&"+hardwareDefineName+");\n";
			String port = "\"/dev/ttyUSB0\"";
			int baudrate = 9600;
			initCode+="char msg[255];\n";
			initCode+="hComm = Serialport_Open((char *)"+port+", "+baudrate+",(char *)msg);\n";
			break;
		}
		code.addInitCode(initCode);
	}
	public void generateOutputCodeC(CodeStructC code) {
		String outputCode="/*Code for output of block RaspFan:("+getBlockId()+")"+getBlockName()+"*/\n";
		switch(model.getModelMode()) {
		case Simulation:
			break;
		case Compilation:
			hardwareDefineName="Block"+this.getBlockId()+"_RaspFan";
			outputCode+="int sendLength=0;\n";
			outputCode+="if(mp->majorStep>0) {\n";
			outputCode+=hardwareDefineName+".raspFanPWM=1.0/3000.0*"+this.getInputPortVariable(0)+";\n";
			outputCode+=hardwareDefineName+".raspFanPWM="+hardwareDefineName+".raspFanPWM>1.0?1.0:"+hardwareDefineName+".raspFanPWM;\n";
			outputCode+=hardwareDefineName+".raspFanPWM="+hardwareDefineName+".raspFanPWM<0?0:"+hardwareDefineName+".raspFanPWM;\n";
			outputCode+=hardwareDefineName+".raspFanPWM="+hardwareDefineName+".raspFanPWM;\n";
			outputCode+="outputRaspFan(&"+hardwareDefineName+");\n";
			outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"=2.0*"+hardwareDefineName+".fanspeed_output;\n";
			outputCode+="int speed="+hardwareDefineName+".fanspeed_output*2.0;\n";
			//数码管显示
			outputCode+="char sendData[1024];\n";
            outputCode+="sprintf(sendData,\"$001,%02d#\",speed);\n";
			outputCode+="sendLength=strlen(sendData);\n";
			outputCode+="Serialport_Send(hComm,sendData,sendLength);\n";	
			outputCode+="}\n";
			break;
		}
		code.addOutputCode(outputCode);
	}
	public void  generateDerivativeCodeC(CodeStructC code) {
		String derivativeCode="/*Code for Derivative of RaspFan" + ":("+getBlockId()+")"+getBlockName()+"*/\n";
		
		switch(model.getModelMode()) {
		case Simulation:
		     break;
		case Compilation:
			break;
		}
		code.addDerivativeCode(derivativeCode);
	}
}
