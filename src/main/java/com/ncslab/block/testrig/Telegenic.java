package com.ncslab.block.testrig;

import lombok.Getter;
import org.json.JSONObject;

import com.ncslab.block.Block;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.NCSLabModel;

import java.util.Vector;

public class Telegenic extends Block {

	Parameter DA;
	Parameter AD1;
	Parameter AD2;
	Parameter AD3;
	Parameter AD4;
	Parameter AD5;
	Parameter AD6;
	Parameter AD7;

    @Getter
    public static final Vector<String> parameterNames = new Vector<>();

    @Getter
    public static final Vector<String> outputNames = new Vector<>();
    @Getter
    public static final Vector<String> inputNames = new Vector<>();

    static {

        outputNames.add("AD1");
        outputNames.add("AD2");
        outputNames.add("AD3");
        outputNames.add("AD4");
        outputNames.add("AD5");
        outputNames.add("AD6");
        outputNames.add("AD7");
        inputNames.add("in1");
        parameterNames.add("DA");
        parameterNames.add("AD1");
        parameterNames.add("AD2");
        parameterNames.add("AD3");
        parameterNames.add("AD4");
        parameterNames.add("AD5");
        parameterNames.add("AD6");
        parameterNames.add("AD7");
    }

	public Telegenic(JSONObject blockJSON, NCSLabModel model) {
		super(blockJSON, model);

		// 一个输入
		inputPortList.add(new InputPort(this, 1));
		DA = new Parameter(this, 1, "DA", paramValues.getString("DA"));
		parameterList.add(DA);
		// 七个输出
		outputPortList.add(new OutputPort(this, "AD1", 1, false));
		AD1 = new Parameter(this, 2, "AD1", paramValues.getString("AD1"));
		parameterList.add(AD1);
		outputPortList.add(new OutputPort(this, "AD2", 2, false));
		AD2 = new Parameter(this, 3, "AD2", paramValues.getString("AD2"));
		parameterList.add(AD2);
		outputPortList.add(new OutputPort(this, "AD3", 3, false));
		AD3 = new Parameter(this, 4, "AD3", paramValues.getString("AD3"));
		parameterList.add(AD3);
		outputPortList.add(new OutputPort(this, "AD4", 4, false));
		AD4 = new Parameter(this, 5, "AD4", paramValues.getString("AD4"));
		parameterList.add(AD4);
		outputPortList.add(new OutputPort(this, "AD5", 5, false));
		AD5 = new Parameter(this, 6, "AD5", paramValues.getString("AD5"));
		parameterList.add(AD5);
		outputPortList.add(new OutputPort(this, "AD6", 6, false));
		AD6 = new Parameter(this, 7, "AD6", paramValues.getString("AD6"));
		parameterList.add(AD6);
		outputPortList.add(new OutputPort(this, "AD7", 7, false));
		AD7 = new Parameter(this, 8, "AD7", paramValues.getString("AD7"));
		parameterList.add(AD7);

	}

	public void generateInitCodeM(CodeStructM code) {
		super.generateInitCodeM(code);
		String initCode = "";

		code.addInitCode(initCode);
	}

	public void generateOutputCodeM(CodeStructM code) {
		super.generateOutputCodeM(code);
		String outputCode = "";

		code.addOutputCode(outputCode);
	}

	public void generateInitCodeC(CodeStructC code) {
		super.generateInitCodeC(code);

		String initCode = "/*Code for initialization of block Telegenic:(" + getBlockId() + ")" + getBlockName()
				+ "*/\n";
		initCode += DA.getName() + "=" + paramValues.getDouble("DA") + ";\n";
		initCode += "DEV_ModuleInit();\n"
				+ "printf(\"\\r\\n Program start \\r\\n\");\r\n"
				+ "DAC8532_Out_Voltage(channel_A, 0);\r\n"
				+ "DAC8532_Out_Voltage(channel_B, 0);\r\n";

		initCode += AD1.getName() + "=" + paramValues.getDouble("AD1") + ";\n";
		initCode += "DEV_ModuleInit();\n"
				+ "if(ADS1256_init() == 1){\r\n"
				+ "        printf(\"\\r\\n ADS1256_init   END \\r\\n\");\r\n"
				+ "        DEV_ModuleExit();\r\n"
				+ "        exit(0);\r\n"
				+ "    }\n";
		initCode += AD2.getName() + "=" + paramValues.getDouble("AD2") + ";\n";
		initCode += "DEV_ModuleInit();\n"
				+ "if(ADS1256_init() == 1){\r\n"
				+ "        printf(\"\\r\\n ADS1256_init   END \\r\\n\");\r\n"
				+ "        DEV_ModuleExit();\r\n"
				+ "        exit(0);\r\n"
				+ "    }\n";
		initCode += AD3.getName() + "=" + paramValues.getDouble("AD3") + ";\n";
		initCode += "DEV_ModuleInit();\n"
				+ "if(ADS1256_init() == 1){\r\n"
				+ "        printf(\"\\r\\n ADS1256_init   END \\r\\n\");\r\n"
				+ "        DEV_ModuleExit();\r\n"
				+ "        exit(0);\r\n"
				+ "    }\n";
		initCode += AD4.getName() + "=" + paramValues.getDouble("AD4") + ";\n";
		initCode += "DEV_ModuleInit();\n"
				+ "if(ADS1256_init() == 1){\r\n"
				+ "        printf(\"\\r\\n ADS1256_init   END \\r\\n\");\r\n"
				+ "        DEV_ModuleExit();\r\n"
				+ "        exit(0);\r\n"
				+ "    }\n";
		initCode += AD5.getName() + "=" + paramValues.getDouble("AD5") + ";\n";
		initCode += "DEV_ModuleInit();\n"
				+ "if(ADS1256_init() == 1){\r\n"
				+ "        printf(\"\\r\\n ADS1256_init   END \\r\\n\");\r\n"
				+ "        DEV_ModuleExit();\r\n"
				+ "        exit(0);\r\n"
				+ "    }\n";
		initCode += AD6.getName() + "=" + paramValues.getDouble("AD6") + ";\n";
		initCode += "DEV_ModuleInit();\n"
				+ "if(ADS1256_init() == 1){\r\n"
				+ "        printf(\"\\r\\n ADS1256_init   END \\r\\n\");\r\n"
				+ "        DEV_ModuleExit();\r\n"
				+ "        exit(0);\r\n"
				+ "    }\n";
		initCode += AD7.getName() + "=" + paramValues.getDouble("AD7") + ";\n";
		initCode += "DEV_ModuleInit();\n"
				+ "if(ADS1256_init() == 1){\r\n"
				+ "        printf(\"\\r\\n ADS1256_init   END \\r\\n\");\r\n"
				+ "        DEV_ModuleExit();\r\n"
				+ "        exit(0);\r\n"
				+ "    }\n";

		code.addInitCode(initCode);
	}

	public void generateOutputCodeC(CodeStructC code) {
		String outputCode = "/*Code for output of block Telegenic:(" + getBlockId() + ")" + getBlockName() + "*/\n";
		outputCode += "if(" + DA.getName() + "){\n";
		outputCode += "DAC8532_Out_Voltage(channel_B,"
				+ inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName() + ");}\n";
		outputCode += "else{\n";
		outputCode += "DAC8532_Out_Voltage(channel_A,("
				+ inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName() + ")/4);}\n";

		outputCode += outputPortList.get(0).getOutputSignalC().getName() + "=ADS1256_GetChannalValue(" + AD1.getName()
				+ ")*5.0/0x7fffff;\n";
		outputCode += outputPortList.get(1).getOutputSignalC().getName() + "=ADS1256_GetChannalValue(" + AD2.getName()
				+ ")*16.6666667/0x7fffff;\n";
		outputCode += outputPortList.get(2).getOutputSignalC().getName() + "=ADS1256_GetChannalValue(" + AD3.getName()
				+ ")*(-5.0)/0x7fffff;\n";
		outputCode += outputPortList.get(3).getOutputSignalC().getName() + "=ADS1256_GetChannalValue(" + AD4.getName()
				+ ")*5.0/0x7fffff;\n";
		outputCode += outputPortList.get(4).getOutputSignalC().getName() + "=ADS1256_GetChannalValue(" + AD5.getName()
				+ ")*5.0/0x7fffff;\n";
		outputCode += outputPortList.get(5).getOutputSignalC().getName() + "=ADS1256_GetChannalValue(" + AD6.getName()
				+ ")*(-16.6666667)/0x7fffff;\n";
		outputCode += outputPortList.get(6).getOutputSignalC().getName() + "=ADS1256_GetChannalValue(" + AD7.getName()
				+ ")*(-16.6666667)/0x7fffff;\n";

		code.addOutputCode(outputCode);
	}

	public void generateDerivativeCodeC(CodeStructC code) {
		String derivativeCode = "/*Code for Derivative of Telegenic:(" + getBlockId() + ")" + getBlockName() + "*/\n";

		code.addDerivativeCode(derivativeCode);
	}

}
