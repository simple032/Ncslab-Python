package com.ncslab.circuit2.block.element;

import org.json.JSONObject;

import com.ncslab.circuit2.block.baseelement.CurrentSource;
import com.ncslab.ncslablink.NCSLabModel;

public class ACCurrentSource extends CurrentSource {
	
	private String amp;
	private String shift;
	private String frequency;
	private String currentTime ="model.time";
	public ACCurrentSource(int id, JSONObject blockJSON, NCSLabModel model) {
		super(id, blockJSON, model);
		// TODO Auto-generated constructor stub
		amp = paramValues.getString("amp");
		shift = paramValues.getString("shift");
		frequency = paramValues.getString("frequency");
	}

	public String getIString() {
		return amp+"*sin("+frequency+"*2*acos(-1.0)*("+currentTime+"-"+shift+"*acos(-1)/180))";
	}
}
