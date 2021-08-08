package block.source;

import org.json.JSONObject;

import block.io.OutputPort;

public class Constant extends block.Block{
	public Constant(JSONObject blockJSON) {
		super(blockJSON);
		
		//Ò»¸öÊä³ö
		outputPortList.add(new OutputPort(this,1,false));
	}
}
