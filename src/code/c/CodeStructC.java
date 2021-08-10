package code.c;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Vector;

import code.CodeModel;
import block.Block;
import block.io.OutputPort;

public class CodeStructC {
	
	public String mainCode="";
	public String includeCode="";
	public String initCode="";
	
	public String outputCode="";
	
	public String updateCode="";
	
	public String parameterDefineCode="";
	public String stateDefineCode="";
	public String outputSignalDefineCode="";
	
	private int parameterIndex=1;
	private int stateIndex=1;
	
	private Vector<Parameter> parameterList=new Vector<Parameter>();
	private Vector<State> stateList=new Vector<State>();
	private Vector<OutputSignal> outputSignalList=new Vector<OutputSignal>();
	
	private CodeModel model;
	
	public CodeStructC(CodeModel model) {
		this.model=model;
		
	}
	
	public String getInitCode() {
		return this.initCode;
	}
	
	public void addInitCode(String code) {
		initCode+=code;
	}
	
	public String getOutputCode() {
		return this.outputCode;
	}
	
	public void addOutputCode(String code) {
		outputCode+=code;
	}
	
	public String getUpdateCode() {
		return this.updateCode;
	}
	
	public void addUpdateCode(String code) {
		updateCode+=code;
	}
	
	public void generateIncludeCode() {
		includeCode+=""
				+"#include\"stdlib.h\"\n"
				+"#define REAL double";
	}
	
	public void generateFinalCodes() {
		String preCode=parameterDefineCode+"\n"+stateDefineCode+"\n"+outputSignalDefineCode+"\n";
		
		mainCode=includeCode+"\n" 
				+preCode+"\n"
				+"main(){\n"
				+initCode+"\n"
				+DataTypeC.getRealString()+" time=0;\n"
				+"while(time<"+model.getConfig().getStopTime()+"){\n"
				+outputCode+"\n"+updateCode
				+"time+="+model.getConfig().getFixedStep()+";\n"
				+"}\n"
				+"}\n";
	}
	
	public String getMainCode() {
		
		
		return mainCode;
	}
	
	public Parameter addParameter(Block block,String localName) {
		Parameter parameter=new Parameter(parameterIndex++,"Block"+block.getBlockId()+"_Parameter_"+localName,localName); 
		
		parameterList.add(parameter);
		
		return parameter;
	}
	
	public State addState(Block block,String localName) {
		State state=new State(stateIndex++,"Block"+block.getBlockId()+"_State_"+localName,localName); 
		
		stateList.add(state);
		
		return state;
	}
	
	public OutputSignal addOutputSignal(Block block,OutputPort outputPort) {
		OutputSignal outputSignal=new OutputSignal(stateIndex++,"Block"+block.getBlockId()+"_Output"+outputPort.getNumber(),"out"+outputPort.getNumber()); 
		
		outputSignalList.add(outputSignal);
		
		outputPort.setOutputSignalC(outputSignal);
		
		return outputSignal;
	}
	
	public void generateParameterDefineCode() {
		parameterDefineCode+="/*Define variables for parameters*/\n";
		for(Parameter parameter:parameterList) {
			parameterDefineCode+=parameter.getDefineString()+" "+parameter.getName()+";\n";
		}
	}
	
	public void generateStateDefineCode() {
		stateDefineCode+="/*Define variables for states*/\n";
		for(State state:stateList) {
			stateDefineCode+=state.getDefineString()+" "+state.getName()+";\n";
		}
	}
	
	public void generateOutputSignalDefineCode() {
		outputSignalDefineCode+="/*Define variables for output signals*/\n";
		for(OutputSignal outputSignal:outputSignalList) {
			outputSignalDefineCode+=outputSignal.getDefineString()+" "+outputSignal.getName()+";\n";
		}
	}
	
	private String codePathBase=utils.Property.instance.getProperty("CCodePath");
	private String codePath;
	
	private void writeMainCFile() {
		File file = new File(codePath+"ncslabccode.c");
    	FileOutputStream outputStream;
    	try {
    		outputStream = new FileOutputStream(file);
    		outputStream.write(mainCode.getBytes());
    		outputStream.close();
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
	}
	
	public void writeCCodeFiles() {
		
		String userPath=codePathBase+model.getUserId();
		
		File file=new File(userPath);
		if(file.exists()==false) {
			file.mkdir();
		}
		
		String modelPath=userPath+"/"+model.getModelId();
		file=new File(modelPath);
		if(file.exists()==false) {
			file.mkdir();
		}
		
		codePath=modelPath+"/";
		writeMainCFile();
    	
	}
	
}
