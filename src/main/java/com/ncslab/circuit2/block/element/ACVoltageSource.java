package com.ncslab.circuit2.block.element;

import org.json.JSONObject;

import com.ncslab.block.io.Parameter;
import com.ncslab.block.data.Data;
import com.ncslab.circuit2.block.baseelement.VoltageSource;
import com.ncslab.ncslablink.NCSLabModel;

public class ACVoltageSource extends VoltageSource {

	private String amp;
	private String shift;
	private String frequency;
	private String currentTime ="model.time";
	
	private double ampValue;
	private double shiftValue;
	private double frequencyValue;
	
	public ACVoltageSource(int id, JSONObject blockJSON, NCSLabModel model) {
		super(id, blockJSON, model);
		// TODO Auto-generated constructor stub
		amp = Data.parseExpression(paramValues.getString("amp"));
		shift = Data.parseExpression(paramValues.getString("shift"));
		frequency = Data.parseExpression(paramValues.getString("frequency"));
		
		ampValue=Double.parseDouble(amp);
		shiftValue=Double.parseDouble(shift);
		frequencyValue=Double.parseDouble(frequency);
	}
	
	public ACVoltageSource(JSONObject blockJSON, NCSLabModel model) {
		super(0, blockJSON, model);
		// TODO Auto-generated constructor stub
		amp = Data.parseExpression(paramValues.getString("amp"));
		shift = Data.parseExpression(paramValues.getString("shift"));
		frequency = Data.parseExpression(paramValues.getString("frequency"));
		
		ampValue=Double.parseDouble(amp);
		shiftValue=Double.parseDouble(shift);
		frequencyValue=Double.parseDouble(frequency);
	}

	public String getVString() {
		return amp+"*sin("+frequency+"*2*3.1415926*("+currentTime+")+"+shift+"*3.1415926/180)";
	}

	@Override
	public double getVValue(double t) {
		// TODO Auto-generated method stub
		return ampValue*Math.sin(frequencyValue*2*3.1415926*t+shiftValue*3.1415926/180);
	}
}
