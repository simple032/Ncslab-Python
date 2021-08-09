package block.sink;

import org.json.JSONObject;

import block.io.InputPort;
import ncslablink.NCSLabModel;

public class Scope extends block.Block{
	public Scope(JSONObject scopeIn,NCSLabModel model) {
		super(scopeIn,model);
		
		//“ª∏ˆ ‰»Î
		inputPortList.add(new InputPort(this,1));
	}
	
	public String generateOutputCode() {
		String code=getBlockName()+"=["+getBlockName()
				+" Block"+getInputPortList().get(0).getLinkedLine().getLinkedOutputPort().getBLock().getBlockId() 
				+"_Output"+getInputPortList().get(0).getLinkedLine().getLinkedOutputPort().getNumber()
				+"]"
				+";\n";
		
		return code;
	}
	
	public String generateInitCode() {
		String code=getBlockName()+"=[];\n";
		
		this.initCode=code;
		return code;
	}
}
