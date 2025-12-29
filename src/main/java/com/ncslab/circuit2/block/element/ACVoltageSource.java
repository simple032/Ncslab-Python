package com.ncslab.circuit2.block.element;

import org.json.JSONObject;

import com.ncslab.block.io.Parameter;
import com.ncslab.circuit2.block.baseelement.VoltageSource;
import com.ncslab.ncslablink.NCSLabModel;

public class ACVoltageSource extends VoltageSource {

	private String amp;
	private String shift;
	private String frequency;
	private String currentTime ="model.time";
	
	public ACVoltageSource(int id, JSONObject blockJSON, NCSLabModel model) {
		super(id, blockJSON, model);
		// TODO Auto-generated constructor stub
		amp = paramValues.getString("amp");
		shift = paramValues.getString("shift");
		frequency = paramValues.getString("frequency");
	}
	
	public ACVoltageSource(JSONObject blockJSON, NCSLabModel model) {
		super(0, blockJSON, model);
		// TODO Auto-generated constructor stub
		amp = paramValues.getString("amp");
		shift = paramValues.getString("shift");
		frequency = paramValues.getString("frequency");
	}

	public String getVString() {
		return amp+"*sin("+frequency+"*2*3.1415926*("+currentTime+")+"+shift+"*3.1415926/180)";
	}
}
