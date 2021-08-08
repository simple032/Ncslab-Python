package block.continuous;

import org.json.JSONObject;

import block.io.InputPort;
import block.io.OutputPort;

public class PIDController extends block.Block{
	public PIDController(JSONObject blockIn) {
		super(blockIn);
		
		//一个输入，一个输出
		inputPortList.add(new InputPort(this,1));
		outputPortList.add(new OutputPort(this,1,true));
	}
}
