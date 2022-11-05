package block.discrete;

import org.json.JSONObject;

import block.Block;
import ncslablink.NCSLabModel;
import block.io.Parameter;

public class DiscreteBlock extends Block {
	
	private double sampleTime=-1;
	
	public DiscreteBlock(JSONObject blockIn,NCSLabModel model) {
		super(blockIn,model);
	}
	
	public double getSampleTime() {
		return this.sampleTime;
	}
	
	public void setSampleTime(double sampleTime) {
		this.sampleTime=sampleTime;
	}
	
	public void setSampleTime(Parameter sampleTime) {
		this.sampleTime=sampleTime.getData().getInitValue();
	}

}
