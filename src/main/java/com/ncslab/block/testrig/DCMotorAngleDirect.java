package com.ncslab.block.testrig;

import com.ncslab.block.Block;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.Parameter;
import com.ncslab.block.io.State;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.NCSLabModel;
import lombok.Getter;
import org.json.JSONObject;

import java.util.Vector;

public class DCMotorAngleDirect extends Block {

    String hardwareDefineName;

	private String name = "DCMotorAngleDirect";

	private State speedState;

	private double motorK=106.25;
	private double motorT=0.07;

    private Parameter baudrate_encoder;
    private Parameter port_encoder;

    @Getter
    public static final Vector<String> parameterNames = new Vector<>();

    @Getter
    public static final Vector<String> outputNames = new Vector<>();
    @Getter
    public static final Vector<String> inputNames = new Vector<>();

    static {

        outputNames.add("Speed");
        outputNames.add("Angle");
        inputNames.add("in1");
    }

	public DCMotorAngleDirect(JSONObject blockJSON, NCSLabModel model) {
		super(blockJSON,model);


		inputPortList.add(new InputPort(this,1));
		outputPortList.add(new OutputPort(this,"Speed",1,false));
		outputPortList.add(new OutputPort(this,"Angle",2,false));
		this.isHardware=true;

        port_encoder = new Parameter(this, 1, "port", blockJSON.optString("port", "\"/dev/ttyAMA0\""));
        parameterList.add(port_encoder);

        baudrate_encoder = new Parameter(this, 2, "baudrate", blockJSON.optString("baudrate", "115200"));
        parameterList.add(baudrate_encoder);

		switch(model.getModelMode()) {
		case Simulation:
			speedState=new State(this,1,"speedState");
			stateList.add(speedState);
			break;
		case Compilation:
			break;
		}
	}

	public String getHardwareDefineCodeC() {
		String hardwareDefineCode="";
		hardwareDefineName="Block"+this.getBlockId()+"_DCMotorAngleDirect";
        hardwareDefineCode+="DCMOTORANGLEDIRECT "+hardwareDefineName+";\n";
		hardwareDefineCode+="HANDLE hComm;\n";
		return hardwareDefineCode;
	}
	 public void generateArraysCodeC(CodeStructC code) {
		 String arraysCode="/*Define arrays for block DCMotorAngle:("+getBlockId()+")"+getBlockName()+"*/\n";
		 arraysCode+="double angledata=0;\n";
		 arraysCode+="double angledata1=0;\n";
		 arraysCode+="int angle_N=0;\n";
		 code.addArraysCode(arraysCode);
	 }

	public void generateInitCodeC(CodeStructC code) {
		super.generateInitCodeC(code);

		String initCode="/*Code for initialization of block " + name + ":("+getBlockId()+")"+getBlockName()+"*/\n";

		switch(model.getModelMode()) {
		case Simulation:
			initCode+=speedState.getName()+"="+0+";\n";
			break;
		case Compilation:
            hardwareDefineName="Block"+this.getBlockId()+"_DCMotorAngleDirect";
            initCode+="initDCMotorAngleDirect(&"+hardwareDefineName+");\n";
			//1.Open the serial port
			String port = port_encoder.getInitString();
			initCode+="char msg[255];\n";
			initCode+="hComm = Serialport_Open((char *)"+port+", "+baudrate_encoder.getInitString()+",(char *)msg);\n";
			break;
		}

		code.addInitCode(initCode);
	}


	public void generateIncludeCodeC(CodeStructC code) {
		String includeCode="/*Code for include files of block " + name + ":("+getBlockId()+")"+getBlockName()+"*/\n";
		includeCode += "#include \"ncs_serialport.h\"\n";
        code.addIncludeCode(includeCode);
	}

	public void addLine(String originCode, String newLine) {

	}

	public void generateOutputCodeC(CodeStructC code) {
		String outputCode="/*Code for output of block " + name + ":("+getBlockId()+")"+getBlockName()+"*/\n";
		switch(model.getModelMode()) {
		case Simulation:
			outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"=10000*"+speedState.getName()+";\n";
			break;
		case Compilation:
            hardwareDefineName="Block"+this.getBlockId()+"_DCMotorAngleDirect";
			int bufLen = 255;
			outputCode+="if(mp->majorStep>0){\n";
			//pwm output
            outputCode+=hardwareDefineName+".PWM="+this.getInputPortVariable(0)+";\n";
            outputCode+=hardwareDefineName+".PWM="+hardwareDefineName+".PWM>0.5?0.5:"+hardwareDefineName+".PWM;\n";
            outputCode+=hardwareDefineName+".PWM="+hardwareDefineName+".PWM<-0.5?-0.5:"+hardwareDefineName+".PWM;\n";
            //outputCode+=hardwareDefineName+".raspFanPWM="+hardwareDefineName+".raspFanPWM;\n";
            outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"="+ hardwareDefineName+".speedPulse*5.79582;\n";
            outputCode+="outputDCMotorAngleDirect(&"+hardwareDefineName+");\n";

			//angle control
			outputCode+="unsigned char cmdangle[]={0x01,0x04,0x00,0x00,0x00,0x04,0xf1,0xc9};\n";
			outputCode+="Serialport_Send(hComm,cmdangle,8);\n";
			outputCode+="char recvBuff1["+bufLen+"]={0};\n";
			outputCode+="Serialport_Recv(hComm,(uint8_t*)recvBuff1,13);\n";
			outputCode+="double Angle=0.01098633*(recvBuff1[6]+(recvBuff1[5]<<8));\n";
            outputCode+="if(angledata-Angle>=180){\n";
            outputCode+="angle_N=1+angle_N;\n";
            outputCode+="}else if(angledata-Angle<=-180){\n";
			outputCode+="angle_N=-1+angle_N;\n";
			outputCode+="}\n";
            outputCode+=outputPortList.get(1).getOutputSignalC().getName()+"=angle_N*360+Angle;\n";
            outputCode+="angledata=Angle;\n";
			//outputCode+=outputPortList.get(1).getOutputSignalC().getName()+"=Angle;\n";
			//outputCode+="for(int i=0; i<13; i++){printf(\"%d=%d \", i, recvBuff1[i]);} printf(\"\\n\");\n";
			outputCode+="}\n";
			break;
		}

		code.addOutputCode(outputCode);
	}

	public void  generateDerivativeCodeC(CodeStructC code) {
		String derivativeCode="/*Code for Derivative of " + name + ":("+getBlockId()+")"+getBlockName()+"*/\n";

		switch(model.getModelMode()) {
		case Simulation:
		derivativeCode+=speedState.getDerivativeName()+"=("
				+this.getInputPortVariable(0)
				+"*"+motorK+"-"+speedState.getName()+")"
				+"*"+(1/motorT)
				+";\n";

		case Compilation:
			break;
		}

		code.addDerivativeCode(derivativeCode);
	}
}

