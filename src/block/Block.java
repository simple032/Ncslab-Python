package block;

import java.util.Vector;
import org.json.JSONObject;

import block.io.InputPort;
import block.io.OutputPort;
import block.io.Parameter;
import block.io.State;
import block.io.OutputSignal;
import code.c.CodeStructC;
import code.m.CodeStructM;
import ncslablink.MatDimException;
import ncslablink.NCSLabModel;

//����Blockģ��Ļ��࣬������block�Ŀ�ܣ������Ҫ���ɸ������ԣ���Ҫ���Ӹ��������������Ľӿ�
public class Block implements block.lan.MCodeBlock,block.lan.CCodeBlock{
	
	//Block�����ͣ���Ҫ��BlockType�н���block��ʱ��ֱ�Դ�
	protected String blockType;
	protected String blockName;
	
	protected int blockId=0;
	
	//Block�Ĳ�������Ϊ��ͬ��block�в�ͬ�Ĳ����������ԭ����json��ʽ�洢
	protected JSONObject paramValues;
	
	//����������˿ڵ��б�
	protected Vector<InputPort> inputPortList=new Vector<InputPort>();
	protected Vector<OutputPort> outputPortList=new Vector<OutputPort>();
	
	protected Vector<Parameter> parameterList=new Vector<Parameter>();
	protected Vector<State> stateList=new Vector<State>();
	
	protected Vector<OutputSignal> outputSignalList=new Vector<OutputSignal>();
	
	//�Ƿ�����Ĵ����Ѿ����ɣ�������ɣ����������ģ���ʱ��ֱ�����þ����ˣ��Ͳ���Ҫ��һ��������
	protected boolean isOutputCodeGenerated=false;
	
	//ָ���ϼ�Modelģ�͵�ָ��
	protected NCSLabModel model;
	
	//Block��Singal�еĸ�����Signalû��Java�����ݽṹ��Signal������InputPort������Ҳ������OutputPort�е��������忴��������ʱ���϶�
	protected int signalNum=0;
	
	protected Block(JSONObject blockIn,NCSLabModel model) {
		this.blockType=blockIn.getString("blockType");
		this.blockName=blockIn.getString("blockName");
		this.paramValues=blockIn.getJSONObject("paramValues");
		this.model=model;
		
	}
	
	public void setSignalNum(int signalNum) {
		this.signalNum=signalNum;
	}
	
	public int getSignalNum() {
		return this.signalNum;
	}
	
	public NCSLabModel getModel() {
		return this.model;
	}
	
	public boolean isTerminalBlock() {
		return (outputPortList.size()==0);
	}
	
	public String getBlockName() {
		return blockName;
	}
	
	public String getBlockType() {
		return blockType;
	}
	
	public Vector<InputPort> getInputPortList(){
		return inputPortList;
	}
	
	public Vector<OutputPort> getOutputPortList(){
		return outputPortList;
	}
	
	public Vector<Parameter> getParameterList(){
		return parameterList;
	}
	
	public Vector<State> getStateList(){
		return stateList;
	}
	
	public void setBlockId(int blockId) {
		this.blockId=blockId;
	}
	
	public int getBlockId() {
		return this.blockId;
	}

	public boolean getIsOutputCodeGenerated() {
		return this.isOutputCodeGenerated;
	}
	
	public String getInputPortVariable(int n) {
		return inputPortList.get(n).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName();
	}
	
	public String getOutputPortVariable(int n) {
		return outputPortList.get(n).getOutputSignalC().getName();
	}
	
	//����M���Ե�Output����,��ͬ��Block���ͣ�������������������Լ��Ĵ���
	public void  generateOutputCodeM(CodeStructM code) {
	
	}
	
	public void setIsOuputCodeGenerated(boolean isOutputCodeGenerated) {
		this.isOutputCodeGenerated=isOutputCodeGenerated;
		for(OutputPort outputPort:outputPortList) {
			outputPort.setIsCodeGenerated(true);
		}
	}
	
	//����M���Ե�Output���룬����һ������
	public void generateBlockOutputCodeM(CodeStructM code) {
		System.out.println("Generating block output code ("+blockId+"):"+blockName);
		
		generateOutputCodeM(code);
		
		/*
		isOutputCodeGenerated=true;
		for(OutputPort outputPort:outputPortList) {
			outputPort.setIsCodeGenerated(true);
		}*/
	}
	
	//����M���Ե�Init����,��ͬ��Block���ͣ�������������������Լ��Ĵ���
	public void generateInitCodeM(CodeStructM code) {
	}

	//����M���Ե�Init���룬����һ������
	public void generateBlockInitCodeM(CodeStructM code) {
		for(OutputSignal outputSignal:outputSignalList) {
			code.addOutputSignal(outputSignal);
		}
		for(Parameter parameter:parameterList) {
			code.addParameter(parameter);
		}
		for(State state:stateList) {
			code.addState(state);
		}
		generateInitCodeM(code);
	}
	
	//����M���Ե�Update����,��ͬ��Block���ͣ�������������������Լ��Ĵ���
	public void generateUpdateCodeM(CodeStructM code) {
		String updateCode="";
		
		for(State state:stateList) {
			updateCode+=state.getName()+"="
					+state.getName()+"+"
					+state.getDerivativeName()
					+"*"
					+"stepSize"
					+";\n";
		}
		
		code.addUpdateCode(updateCode);
	}
	
	//����M���Ե�Update���룬����һ������
	public void generateBlockUpdateCodeM(CodeStructM code) {
		generateUpdateCodeM(code);
	}
	
	public void generateBlockDerivativeCodeM(CodeStructM code) {
		generateDerivativeCodeM(code);
	}
	public void generateDerivativeCodeM(CodeStructM code) {
		
	}
	
	public void updateBlock() {
		int i=0;
		for(OutputPort outputPort:outputPortList) {
			OutputSignal outputSignal=new OutputSignal(this,i,outputPort.getNumber(),outputPort.getName(),outputPort.getWidth());
			outputPort.setOutputSignalC(outputSignal);
			outputSignalList.add(outputSignal);
			i++;
		}
	}
	
	
	//c���ԵĴ������ɷ�������M������ͬ
	//����C���Ե�Init���룬����һ������
	public void generateBlockInitCodeC(CodeStructC code) {
		for(OutputSignal outputSignal:outputSignalList) {
			code.addOutputSignal(outputSignal);
		}
		generateInitCodeC(code);
	}
	
	//����C���Ե�Init����,��ͬ��Block���ͣ�������������������Լ��Ĵ���
	public void generateInitCodeC(CodeStructC code) {
		for(Parameter parameter:parameterList) {
			code.addParameter(parameter);
		}
		for(State state:stateList) {
			code.addState(state);
		}
	}
	
	//����C���Ե�Output���룬����һ������
	public void generateBlockOutputCodeC(CodeStructC code) {
		System.out.println("Generating block output code ("+blockId+"):"+blockName);
		
		generateOutputCodeC(code);
		
		/*
		isOutputCodeGenerated=true;
		for(OutputPort outputPort:outputPortList) {
			outputPort.setIsCodeGenerated(true); 
		}*/
		
	}
	
	//����C���Ե�Output����,��ͬ��Block���ͣ�������������������Լ��Ĵ���
	public void generateOutputCodeC(CodeStructC code) {
		
	}
	
	//����C���Ե�Update���룬����һ������
	public void generateBlockUpdateCodeC(CodeStructC code) throws MatDimException {
		generateUpdateCodeC(code);
	}
	
	//����C���Ե�Update����,��ͬ��Block���ͣ�������������������Լ��Ĵ���
	public void generateUpdateCodeC(CodeStructC code) throws MatDimException {
		String updateCode="/*Code for update of block "+getBlockType()+":("+getBlockId()+")"+getBlockName()+"*/\n";
		
		for(State state:stateList) {
			updateCode+=state.getName()+"="
					+state.getName()+"+"
					+state.getDerivativeName()
					+"*"
					+"model.stepSize"
					+";\n";
		}
		
		code.addUpdateCode(updateCode);
	}
	
	public void generateBlockDerivativeCodeC(CodeStructC code) {
		generateDerivativeCodeC(code);
	}
	public void generateDerivativeCodeC(CodeStructC code) {
		
	}

	public boolean isSFcnBlock() {
		// TODO Auto-generated method stub
		return false;
	}

	public void generateSourceFile() {
		// TODO Auto-generated method stub
		
	}

	public String getSFcnName() {
		// TODO Auto-generated method stub
		return "";
	}

	public void generateBlockStatementCodeC(CodeStructC code) {
		// TODO Auto-generated method stub
		generateStatementCodeC(code);
	}
	
	public void generateStatementCodeC(CodeStructC code) {
		
	}

	public String[] getSFunctionModuleList() {
		// TODO Auto-generated method stub
		return null;
	}

	public void updateDimension() {
		// TODO Auto-generated method stub
		
	}
}
