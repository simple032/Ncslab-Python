package code.c;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Vector;
import java.io.InputStream;

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
	
	public String dataStructureCode="";
	public String dataStructureInitCode="";
	
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
				+"#include\"ncslabccode.h\"\n";
	}
	
	public void generateFinalCodes() {
		String preCode=parameterDefineCode+"\n"+stateDefineCode+"\n"+outputSignalDefineCode+"\n";
		
		mainCode=includeCode+"\n" 
				+preCode+"\n"
				+dataStructureCode+"\n"
				+"main(){\n"
				+dataStructureInitCode+"\n"
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
	
	public void gnenrateDataStructureCode() {
		dataStructureCode+="/*Define data structures*/\n";
		
		if(model.getBlockList().size()>0) {
			int i=0;
			for(Block block:model.getBlockList()) {
				
				dataStructureCode+="BLOCK block"+i+"={\""+block.getBlockName()+"\"};\n";
				i++;
			}
			dataStructureCode+="BLOCK *blocks["+model.getBlockList().size()+"];\n";
		}
		else {
			dataStructureCode+="BLOCK **blocks=NULL";
		}
		dataStructureCode+="MODEL model={\""+model.getModelRealName()+"\","+model.getBlockList().size()+"};\n";  
		
		dataStructureInitCode+="/*Initialize data structure*/\n";
		
		for(int i=0;i<model.getBlockList().size();i++) {
			Block block=model.getBlockList().get(i);
			dataStructureInitCode+="blocks["+i+"]=&block"+i+";\n";
		}
		
		dataStructureInitCode+="model.blocks=blocks;";
	}
	
	private String codePathBase=utils.Property.instance.getProperty("CCodePath");
	private String codePath;
	
	private void writeMainCFile() {
		System.out.println("Writing main C file ncslabccode.c...");
		
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
	
	private void writeMakefile() {
		System.out.println("Writing makefile...");
		
		InputStream InputStream = this.getClass().getResourceAsStream("makefile");
		
		File file=new File(codePath+"/makefile");
		FileOutputStream outputStream;
    	try {
    		outputStream = new FileOutputStream(file);
    		byte[] buffer=new byte[1024];
    		int len;
    		while((len=InputStream.read(buffer))>0) {
    			outputStream.write(buffer,0,len);
    		}
    		outputStream.close();
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
	}
	
	private void writeHFiles() {
		System.out.println("Writing ncslabccode.h...");
		
		InputStream InputStream = this.getClass().getResourceAsStream("ncslabccode.h");
		
		File file=new File(codePath+"/ncslabccode.h");
		FileOutputStream outputStream;
    	try {
    		outputStream = new FileOutputStream(file);
    		byte[] buffer=new byte[1024];
    		int len;
    		while((len=InputStream.read(buffer))>0) {
    			outputStream.write(buffer,0,len);
    		}
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
		writeHFiles();
    	writeMakefile();
    	
    	//makeExeFile();
	}
	
	public boolean makeExeFile() {
		try {
			Process process=Runtime.getRuntime().exec("make", null, new File(codePath));
			process.waitFor();
			
			if(process.exitValue()==0) {
				return true;
			}
			else {
				byte[] buffer=new byte[process.getErrorStream().available()];
				process.getErrorStream().read(buffer);
				System.err.println(new String(buffer));
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
}
