package com.ncslab.block.testrig;

import lombok.Getter;
import org.json.JSONObject;

import com.ncslab.block.Block;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.State;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.NCSLabModel;

import java.util.Vector;

import com.ncslab.util.TemplateManager;

public class Kirchhoff extends Block {

	Parameter BCM;
	Parameter AD1;
	Parameter AD2;
	Parameter AD3;
	Parameter AD4;
	Parameter AD5;
	Parameter AD6;
	Parameter AD7;

    private static final Vector<String> parameterNames = new Vector<>();

    private static final Vector<String> outputNames = new Vector<>();
    private static final Vector<String> inputNames = new Vector<>();

    static {

        outputNames.add("AD1");
        outputNames.add("AD2");
        outputNames.add("AD3");
        outputNames.add("AD4");
        outputNames.add("AD5");
        outputNames.add("AD6");
        outputNames.add("AD7");
        inputNames.add("in1");
        parameterNames.add("BCM");
        parameterNames.add("AD1");
        parameterNames.add("AD2");
        parameterNames.add("AD3");
        parameterNames.add("AD4");
        parameterNames.add("AD5");
        parameterNames.add("AD6");
        parameterNames.add("AD7");

    }

	public Kirchhoff(JSONObject blockJSON, NCSLabModel model) {
		super(blockJSON, model);

		// 一个输入
		inputPortList.add(new InputPort(this, 1));
		BCM = new Parameter(this, 1, "BCM", paramValues.getString("BCM"));
		parameterList.add(BCM);
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

		String initCode = "/*Code for initialization of block Kirchhoff:(" + getBlockId() + ")" + getBlockName()
				+ "*/\n";
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

		initCode += BCM.getName() + "=" + paramValues.getDouble("BCM") + ";\n";
		initCode += "wiringPiSetupGpio();\n"
				+ "pinMode (" + BCM.getName() + ", OUTPUT);\r\n";
		code.addInitCode(initCode);
	}

	public void generateOutputCodeC(CodeStructC code) {
        context.put("block", this);
	    context.put("states", getStates());
	    context.put("inputPortVariable", getInputPortVariable(0));
	    context.put("outputPortVariables", getOutputPortVariables());
	    context.put("modelMode", model.getModelMode().name());

	    String codeStr = TemplateManager.renderTemplate("c/testrig/Kirchhoff/output.vm", context);
	    code.addOutputCode(codeStr);
	}

	private Vector<State> getStates() {
	    return stateList;
	}

	public void generateDerivativeCodeC(CodeStructC code) {
	    String derivativeCode = "/*Code for Derivative of Kirchhoff:(" + getBlockId() + ")" + getBlockName() + "*/\n";

	    code.addDerivativeCode(derivativeCode);
	}

}
