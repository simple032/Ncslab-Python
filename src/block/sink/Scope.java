package block.sink;

import lombok.Getter;
import org.json.JSONObject;

import block.data.DataType;
import block.io.InputPort;
import code.c.CodeStructC;
import code.m.CodeStructM;
import ncslablink.MatDimException;
import ncslablink.NCSLabModel;

import block.io.OutputSignal;

import block.io.terminal.ScopeStruct;

import java.util.Vector;

public class Scope extends block.Block{
	
	ScopeStruct scopeStruct;

	@Getter
	public static final Vector<String> inputNames = new Vector<>();

	static {
		inputNames.add("in1");
	}

	public Scope(JSONObject scopeIn,NCSLabModel model) {
		super(scopeIn,model);
		
		//һ������
		inputPortList.add(new InputPort(this,1));
	}
	
	public void generateOutputCodeM(CodeStructM code) {
		super.generateOutputCodeM(code);
		String outputCode="";
		outputCode+="if storeEnable>0\n";
		outputCode+=getBlockName()+"=["+getBlockName()
				+" Block"+getInputPortList().get(0).getLinkedLine().getLinkedOutputPort().getBLock().getBlockId() 
				+"_Output"+getInputPortList().get(0).getLinkedLine().getLinkedOutputPort().getNumber()
				+"]"
				+";\n";
		outputCode+="end\n";
		code.addOutputCode(outputCode);
	}
	
	public void generateInitCodeM(CodeStructM code) {
		super.generateInitCodeM(code);
		String initCode="";
		code.addGlobalDefineCode("global "+getBlockName()+";\n");
		initCode+=getBlockName()+"=[];\n";
		initCode+="ScopeNum=ScopeNum+1;\n";	
		initCode+="ScopeList=[ScopeList; '"+getBlockName()+"'];\n";
		code.addInitCode(initCode);
	}
	
	public void generateInitCodeC(CodeStructC code) {
		super.generateInitCodeC(code);
		
		if(model.getModelMode()==ncslablink.ModelMode.Simulation) {
		
			String initCode="/*Code for initialization of block Scope:("+getBlockId()+")"+getBlockName()+"*/\n";
		
			initCode+=scopeStruct.getName()+".cursor=0;\n";
			initCode+=scopeStruct.getName()+".isFull=0;\n";
		
			code.addInitCode(initCode);
		}
	}
	
	public void generateOutputCodeC(CodeStructC code) {
		
		if(model.getModelMode()==ncslablink.ModelMode.Simulation) {
		
			OutputSignal signal=this.inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
			
			String outputCode="/*Code for output of block Scope:("+getBlockId()+")"+getBlockName()+"*/\n";
			
			outputCode+="if(sfcnIsMajorStep()){\n";
			outputCode+=scopeStruct.getName()+".timeBuffer["+scopeStruct.getName()+".cursor]=sfcnGetT();\n";
			
			switch(signal.getDataType()) {
			case REAL:
				outputCode+=scopeStruct.getName()+".buffer["+scopeStruct.getName()+".cursor]="+signal.getName()+";\n";
				break;
			case MATRIX:
				outputCode+="for(int i=0;i<"+signal.getHeight()+";i++){\n";
				outputCode+="for(int j=0;j<"+signal.getWidth()+";j++){\n";
				outputCode+="int pos=i*"+signal.getWidth()+"+j+"+scopeStruct.getName()+".cursor*"+signal.getHeight()+"*"+signal.getWidth()+";\n";
				outputCode+=scopeStruct.getName()+".buffer[pos]="+signal.getName()+"(i,j);\n";
				outputCode+="}\n";
				outputCode+="}\n";
				break;
			}
			
			outputCode+=scopeStruct.getName()+".cursor++;\n";
			outputCode+="if("+scopeStruct.getName()+".cursor>="+scopeStruct.getName()+".maxDataLength){\n";
			outputCode+=scopeStruct.getName()+".cursor-="+scopeStruct.getName()+".maxDataLength;\n";
			outputCode+=scopeStruct.getName()+".isFull=1;\n";
			outputCode+="}\n";
			
			outputCode+="}\n";
			code.addOutputCode(outputCode);
		}
	}
	
	public void generateTerminateCodeC(CodeStructC code) {
		
		if(model.getModelMode()==ncslablink.ModelMode.Simulation) {
		
			String terminateCode="/*Code for terminate code of block Scope:("+getBlockId()+")"+getBlockName()+"*/\n";
			
			//terminateCode+="printf(\"%d\\n\","+scopeStruct.getName()+".cursor);\n";
			
			code.addTerminateCode(terminateCode);
		}
	}
	
	public void updateDimension() throws MatDimException{
		
		
	}
	
	public void checkDimension() throws MatDimException{
		scopeStruct=new ScopeStruct(this,1,this.blockName);
		
		OutputSignal signal=this.inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
		scopeStruct.setDimension(signal.getWidth(), signal.getHeight());
		scopeStruct.setMaxDataLength(3000);
		
		model.addTerminal(scopeStruct);
	}
}
