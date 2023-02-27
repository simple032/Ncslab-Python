package block.testrig;

import org.json.JSONObject;

import block.Block;
import block.io.InputPort;
import block.io.OutputPort;
import block.io.Parameter;
import code.c.CodeStructC;
import code.m.CodeStructM;
import ncslablink.NCSLabModel;

public class Superposition extends Block {
	

	block.io.Parameter BCM1;
	block.io.Parameter BCM2;
	block.io.Parameter BCM3;
	block.io.Parameter AD1;
	block.io.Parameter AD2;

	public Superposition(JSONObject blockJSON,NCSLabModel model) {
		super(blockJSON,model);
		
		//三个输入
		inputPortList.add(new InputPort(this,1));
		BCM1=new Parameter(this,1,"BCM1",paramValues.getString("BCM1"));
		parameterList.add(BCM1);
		inputPortList.add(new InputPort(this,2));
		BCM2=new Parameter(this,2,"BCM2",paramValues.getString("BCM2"));
		parameterList.add(BCM2);
		inputPortList.add(new InputPort(this,3));
		BCM3=new Parameter(this,3,"BCM3",paramValues.getString("BCM3"));
		parameterList.add(BCM3);
		//七个输出
		outputPortList.add(new OutputPort(this,"AD1",1,false));
		AD1=new Parameter(this,4,"AD1",paramValues.getString("AD1"));
		parameterList.add(AD1);
		outputPortList.add(new OutputPort(this,"AD2",2,false));
		AD2=new Parameter(this,5,"AD2",paramValues.getString("AD2"));
		parameterList.add(AD2);
		
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
		
		String initCode="/*Code for initialization of block Superposition:("+getBlockId()+")"+getBlockName()+"*/\n";
		initCode+=BCM1.getName()+"="+paramValues.getDouble("BCM1")+";\n"; 
		initCode+="wiringPiSetupGpio();\n"
				+ "pinMode ("+BCM1.getName()+", OUTPUT);\r\n";
		initCode+=BCM2.getName()+"="+paramValues.getDouble("BCM2")+";\n"; 
		initCode+="wiringPiSetupGpio();\n"
				+ "pinMode ("+BCM2.getName()+", OUTPUT);\r\n";
		initCode+=BCM3.getName()+"="+paramValues.getDouble("BCM3")+";\n"; 
		initCode+="wiringPiSetupGpio();\n"
				+ "pinMode ("+BCM3.getName()+", OUTPUT);\r\n";
		
		initCode+=AD1.getName()+"="+paramValues.getDouble("AD1")+";\n"; 
		initCode+="DEV_ModuleInit();\n"
				+"if(ADS1256_init() == 1){\r\n"
				+ "        printf(\"\\r\\n ADS1256_init   END \\r\\n\");\r\n"
				+ "        DEV_ModuleExit();\r\n"
				+ "        exit(0);\r\n"
				+ "    }\n";
		initCode+=AD2.getName()+"="+paramValues.getDouble("AD2")+";\n"; 
		initCode+="DEV_ModuleInit();\n"
				+"if(ADS1256_init() == 1){\r\n"
				+ "        printf(\"\\r\\n ADS1256_init   END \\r\\n\");\r\n"
				+ "        DEV_ModuleExit();\r\n"
				+ "        exit(0);\r\n"
				+ "    }\n";

		code.addInitCode(initCode);
	}
	
	public void generateOutputCodeC(CodeStructC code) {
		String outputCode="/*Code for output of block Superposition:("+getBlockId()+")"+getBlockName()+"*/\n";
		outputCode+="if("+inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName()+"){\n";
		outputCode+="digitalWrite("+BCM1.getName()+", HIGH);}\n";
		outputCode+="else{\n";
		outputCode+="digitalWrite("+BCM1.getName()+", LOW);}\n";
		outputCode+="if("+inputPortList.get(1).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName()+"){\n";
		outputCode+="digitalWrite("+BCM2.getName()+", HIGH);}\n";
		outputCode+="else{\n";
		outputCode+="digitalWrite("+BCM2.getName()+", LOW);}\n";
		outputCode+="if("+inputPortList.get(2).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName()+"){\n";
		outputCode+="digitalWrite("+BCM3.getName()+", HIGH);}\n";
		outputCode+="else{\n";
		outputCode+="digitalWrite("+BCM3.getName()+", LOW);}\n";
		
		outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"=ADS1256_GetChannalValue("+AD1.getName()+")*16.6666667/0x7fffff;\n";
		outputCode+=outputPortList.get(1).getOutputSignalC().getName()+"=ADS1256_GetChannalValue("+AD2.getName()+")*16.6666667/0x7fffff;\n";
		
		code.addOutputCode(outputCode);
	}
	public void  generateDerivativeCodeC(CodeStructC code) {
		String derivativeCode="/*Code for Derivative of Superposition:("+getBlockId()+")"+getBlockName()+"*/\n";

		code.addDerivativeCode(derivativeCode);
	}
	
}

