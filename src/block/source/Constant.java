package block.source;

import org.json.JSONObject;

import block.io.OutputPort;
import ncslablink.NCSLabModel;

public class Constant extends block.Block{
	public Constant(JSONObject blockJSON,NCSLabModel model) {
		super(blockJSON,model);
		
		//Ò»¸öÊä³ö
		outputPortList.add(new OutputPort(this,1,false));
	}
	
	public String generateOutputCodeM() {
		String code="Block"+this.getBlockId()+"_Output1="+paramValues.getInt("Value")+";\n";
		
		return code;
	}
}
