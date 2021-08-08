package block.testrig;

import org.json.JSONObject;

import block.Block;
import block.io.InputPort;
import block.io.OutputPort;
import ncslablink.NCSLabModel;

public class WaterLevel extends Block {
	public WaterLevel(JSONObject blockJSON,NCSLabModel model) {
		super(blockJSON,model);
		
		//一个输入，两个输出
		inputPortList.add(new InputPort(this,1));
		outputPortList.add(new OutputPort(this,1,false));
		outputPortList.add(new OutputPort(this,2,false));
	}
	
	public String generateInitCode() {
		String code=super.generateInitCode();
		code+="Block"+getBlockId()+"_PumpState=0;\n";
		code+="Block"+getBlockId()+"_LevelState=0;\n";
		
		this.initCode=code;
		return code;
	}
	
	public String generateOutputCode() {
		String code="Block"+this.getBlockId()+"_Output1="
				+"Block"+getBlockId()+"_PumpState"
				+";\n";
		
		code+="Block"+this.getBlockId()+"_Output2="
				+"Block"+getBlockId()+"_LevelState"
				+ ";\n";
		
		return code;
	}
}
