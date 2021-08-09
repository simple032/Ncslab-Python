package code.c;

import java.util.Vector;

import block.Block;
import block.io.OutputPort;

public class CodeStructC {
	
	public String mainCode="";
	public String includeCode="";
	public String initCode="";
	
	public String outputCode="";
	
	public String parameterDefineCode="";
	public String stateDefineCode="";
	public String outputSignalDefineCode="";
	
	private int parameterIndex=1;
	private int stateIndex=1;
	
	private Vector<Parameter> parameterList=new Vector<Parameter>();
	private Vector<State> stateList=new Vector<State>();
	private Vector<OutputSignal> outputSignalList=new Vector<OutputSignal>();
	
	public CodeStructC() {
		
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
	
	public String getMainCode() {
		initCode=parameterDefineCode+stateDefineCode+outputSignalDefineCode+initCode;
		
		mainCode=includeCode+initCode+outputCode;
		
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
		OutputSignal outputSignal=new OutputSignal(stateIndex++,"Block"+block.getBlockId()+"_Output_"+outputPort.getNumber(),"out"+outputPort.getNumber()); 
		
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
	
}
