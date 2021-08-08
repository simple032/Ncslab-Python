package block.math;

import org.json.JSONObject;
import java.util.Vector;

import block.Block;
import block.io.OutputPort;
import block.io.InputPort;

public class Sum extends Block {
	
	private String seq; 
	
	public Sum(JSONObject blockJSON) {
		super(blockJSON);
		
		//Ò»¸öÊä³ö
		outputPortList.add(new OutputPort(this,1,true));
		
		paraseParamValues();
	}
	
	private void paraseParamValues() {
		seq=paramValues.getString("Inputs");
		
		for(int i=0;i<seq.length();i++) {
			inputPortList.add(new InputPort(this,i+1));
		}
	}
}
