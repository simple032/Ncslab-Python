package block.function;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.Vector;
import java.util.regex.Pattern;

import java.util.regex.Matcher;
import org.json.JSONArray;
import org.json.JSONObject;

import block.Block;
import block.io.InputPort;
import block.io.OutputPort;
import block.io.Parameter;
import block.io.State;
import code.c.CodeStructC;
import code.m.CodeStructM;
import ncslablink.NCSLabModel;

public class SFunctionBuilder extends Block {
	
	private String name = "S-Function Builder";
	
	private String fcnName = "";
	
	private JSONObject code;	
	
	private String[] parameters;	
	private String[] sFunctionModuleList;
	
	
	public SFunctionBuilder(JSONObject blockJSON,NCSLabModel model) {
		super(blockJSON,model);
		
		
		//һ�����룬�������		
		
		System.out.println(blockJSON);
		parseParamValues();
		
		//inputPortList = getInputPortFromSFcn("");
	}
	
	enum Parameter{
		inputPortNumber,
		outputPortNumber,
		stateNumber
	};
	
	private void parseParamValues() {
		this.fcnName = paramValues.getString("FunctionBuilderName");
		//Dynamic port allocation
		//JSONArray jo = new JSONArray(paramValues.getString("Parameters"));
//		int inputPortsNum = (int)jo.get(0);
//		int outputPortsNum = (int)jo.get(1);
//		int statesNum = (int)jo.get(2);		
		
		int inputPortsNum = (int)paramValues.getInt("InputPortNumber");
		int outputPortsNum = (int)paramValues.getInt("OutputPortNumber");
		int statesNum = 0;		
		
		for(int i=0; i<inputPortsNum; i++)
		{
			inputPortList.add(new InputPort(this, i+1));
		}
		for(int i=0; i<outputPortsNum; i++) 
		{
			outputPortList.add(new OutputPort(this, i+1));
		}
		for(int i=0; i<statesNum; i++) 
		{
			stateList.add(new State(this, i+1, "State"+(i+1)));
		}
		parameters = paramValues.getString("Parameters").split(",");
		for(int i=0; i<parameters.length; i++) 
		{
			//TODO: java: constructor Parameter in class block.io.Parameter cannot be applied to given types;
			//  required: block.Block,int,java.lang.String,java.lang.String
			//  found: block.function.SFunctionBuilder,int,java.lang.String
			//  reason: actual and formal argument lists differ in length
//			parameterList.add(new block.io.Parameter(this, i+1, "Para"+(i+1)), "");
		}				
		//TODO:deal s-function modules	
		if(paramValues.getString("SFunctionModules").length()>0) {
			this.sFunctionModuleList = paramValues.getString("SFunctionModules").split(" ");
		}else {
			this.sFunctionModuleList = new String[0];
		}
		System.out.println("sFunctionModuleList len:"+sFunctionModuleList.length);

		this.code = new JSONObject(paramValues.getString("Code"));
	}
	
	Pattern pattern = Pattern.compile("ssGetSFcnParam\\(S,(\\d+)\\)");

	private String replaceParameters(String oldcode) {
		String newcode = oldcode;
		Matcher matcher = pattern.matcher(newcode);
		while(matcher.find()) {
			int idx = Integer.parseInt(matcher.group(1));			
			newcode = matcher.replaceFirst(parameters[idx]);
			matcher = pattern.matcher(newcode);
		}	
		return newcode;
	}
	
	public void generateSourceFile() {		
		FileOutputStream outputStream;
		String filename = this.fcnName + "_" + getBlockId() + ".c";
		System.out.println("Writing file "+ filename);
		String filepath = utils.Property.instance.getProperty("CCodePath")
				+ "/" + model.getUserId() + "/" + model.getModelId();
		File file = new File(filepath +"/"+ filename);
		String code = "";
		try {
			outputStream = new FileOutputStream(file);
			OutputStreamWriter osw = new OutputStreamWriter(outputStream);
			//1.Predefines
			code += "#define S_FUNCTION_NAME " + this.fcnName + "_" + getBlockId() + " \n"
					+ "#include \"ncslabccode.h\"\n"
					+ "#include \"ncslabdefines.h\"\n";			
			
			
			code += replaceParameters(this.code.getString("predefine")) +"\n";
			
			code += "extern MODEL* mp;\n";
			
			//2.Start
			code += "static void mdlStart(SimStruct* S)\n"
					+ "{\n"
					+ replaceParameters(this.code.getString("start")) + "\n"
					+ "}\n";
			
			//2.Outputs
			code += "static void mdlOutputs(SimStruct* S, int tid)\n"
					+ "{\n"
					+ replaceParameters(this.code.getString("outputs")) + "\n"
					+ "}\n";

			//3.Update/Derivatives
			code += "static void mdlUpdate(SimStruct* S, int tid)\n"
					+ "{\n"
					+ replaceParameters(this.code.getString("update")) + "\n"
					+ "}\n";
			
			//3.Update/Derivatives
			code += "#define MDL_DERIVATIVES\n";
			code += "static void mdlDerivatives(SimStruct* S)\n"
					+ "{\n"
					+ replaceParameters(this.code.getString("derivatives")) + "\n"
					+ "}\n";
			//4.Terminate
			code += "static void mdlTerminate(SimStruct* S)\n"
					+ "{\n"
					+ replaceParameters(this.code.getString("terminate")) + "\n"
					+ "}\n";
			
			//5.Interface
			code += "\n\n#include \"ncslabsfun.h\"\n";
			osw.write(code);
			osw.close();
			outputStream.close();
		}
		catch(Exception e) {
			e.printStackTrace();
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
	
	
	public void generateInitCodeC(CodeStructC code) {
		super.generateInitCodeC(code);
		
		String initCode="/*Code for initialization of block " + name + ":("+getBlockId()+")"+getBlockName()+"*/\n";
//		initCode+=pumpState.getName()+"="+0+";\n";
//		initCode+=levelState.getName()+"="+0+";\n"; 
		initCode += fcnName+"_"+getBlockId()+"(&sfcnStruc"+getBlockId()+");\n";
		//init number of inputs and outputs
		initCode += "block"+getBlockId()+".inputPortNum="+inputPortList.size()+";";
		initCode += "block"+getBlockId()+".outputPortNum="+outputPortList.size()+";";
		initCode += "sfcnStruc"+getBlockId()+".parentBlock=(BLOCK*)&block"+getBlockId()+";\n";
		initCode +=  "sfcnStart(sfcnStruc" + getBlockId() + ");\n";
		
		code.addInitCode(initCode);
	}
	
	public void generateIncludeCodeC(CodeStructC code) {
//		String includeCode="/*Code for include files of block " + name + ":("+getBlockId()+")"+getBlockName()+"*/\n";
//		code.addIncludeCode(includeCode);
	}
	
	public boolean isSFcnBlock() {
		return true;
	}
	
	public String getSFcnName() {
		return this.fcnName;
	}	
	
	public String[] getSFunctionModuleList(){		
		return sFunctionModuleList;		
	}

	public void generateOutputCodeC(CodeStructC code) {
		String outputCode="/*Code for output of block " + name + ":("+getBlockId()+")"+getBlockName()+"*/\n";
		
//		outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"="+pumpState.getName()+";\n";
//		outputCode+=outputPortList.get(1).getOutputSignalC().getName()+"="+levelState.getName()+";\n";
		outputCode +=  "sfcnOutputs(sfcnStruc" + getBlockId() + ",0);\n";
		code.addOutputCode(outputCode);
	}
	
	public void  generateDerivativeCodeC(CodeStructC code) {
		String derivativeCode="/*Code for Derivative of " + name + ":("+getBlockId()+")"+getBlockName()+"*/\n";	
		derivativeCode +=  "sfcnDerivatives(sfcnStruc" + getBlockId() + ");\n";
		code.addDerivativeCode(derivativeCode);
	}
	
	public void generateStatementCodeC(CodeStructC code) {
		String statementCode = "/*Code for statement of " + name + ":("+getBlockId()+")"+getBlockName()+"*/\n";	
		statementCode += "SimStruct sfcnStruc"+ getBlockId() +";\n";
		statementCode += "void "+ this.fcnName + "_" + getBlockId() +"(SimStruct* rts);\n";
		code.addStatementCode(statementCode);
	}
}
