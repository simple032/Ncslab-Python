package com.ncslab.circuit2.block.element;

import org.json.JSONObject;

import com.ncslab.circuit2.block.baseelement.CurrentSource;
import com.ncslab.ncslablink.NCSLabModel;

public class ACCurrentSource extends CurrentSource {
	
	private String amp;
	private String shift;
	private String frequency;
	private String currentTime ="model.time";
	
	private double ampValue;
	private double shiftValue;
	private double frequencyValue;
	public ACCurrentSource(int id, JSONObject blockJSON, NCSLabModel model) {
		super(id, blockJSON, model);
		// TODO Auto-generated constructor stub
		amp = paramValues.getString("amp");
		shift = paramValues.getString("shift");
		frequency = paramValues.getString("frequency");
		
		ampValue=Double.parseDouble(amp);
		shiftValue=Double.parseDouble(shift);
		frequencyValue=Double.parseDouble(frequency);
	}
	
	public ACCurrentSource(JSONObject blockJSON, NCSLabModel model) {
		super(0, blockJSON, model);
		// TODO Auto-generated constructor stub
		amp = paramValues.getString("amp");
		shift = paramValues.getString("shift");
		frequency = paramValues.getString("frequency");
		
		ampValue=Double.parseDouble(amp);
		shiftValue=Double.parseDouble(shift);
		frequencyValue=Double.parseDouble(frequency);
	}

	public String getIString() {
		return amp+"*sin("+frequency+"*2*acos(-1.0)*("+currentTime+"-"+shift+"*acos(-1)/180))";
	}

	@Override
	public double getIValue(double t) {
		// TODO Auto-generated method stub
		return ampValue*Math.sin(frequencyValue*(2*3.1415926)*t-shiftValue*2*3.1415926);
	}
}
