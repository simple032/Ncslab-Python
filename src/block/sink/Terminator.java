package block.sink;

import org.json.JSONObject;

import block.io.InputPort;
import code.m.CodeStructM;
import ncslablink.NCSLabModel;

public class Terminator extends block.Block{
	public Terminator(JSONObject scopeIn,NCSLabModel model) {
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
		code.addInitCode(initCode);
	}
}
