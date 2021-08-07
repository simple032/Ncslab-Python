package block.testrig;

import org.json.JSONObject;

import block.Block;
import block.io.InputPort;
import block.io.OutputPort;

public class WaterLevel extends Block {
	public WaterLevel(JSONObject blockJSON) {
		super(blockJSON);
		
		//一个输入，两个输出
		inputPortList.add(new InputPort());
		outputPortList.add(new OutputPort());
		outputPortList.add(new OutputPort());
	}
}
