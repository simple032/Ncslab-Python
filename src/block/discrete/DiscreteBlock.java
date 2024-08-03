package block.discrete;

import org.json.JSONObject;

import block.Block;
import ncslablink.MatDimException;
import ncslablink.NCSLabModel;
import block.io.Parameter;
import code.c.CodeStructC;

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
	
	public void generateDiscreteUpdateCodeCInside(CodeStructC code) throws MatDimException{
		
	}
	
	public void generateDiscreteUpdateCodeC(CodeStructC code) throws MatDimException{
		String discreteUpdateCode="/*Code for update of discrete block "+getBlockType()+":("+getBlockId()+")"+getBlockName()+"*/\n";
		/*
		discreteUpdateCode+="dist=distance(mp->time,"+sampleTime+");\n";
		discreteUpdateCode+="if(fabs(floor(mp->time/"+sampleTime+"+0.5)-mp->time/"+sampleTime+")<0.000001) {\n";
		code.addDiscreteUpdateCode(discreteUpdateCode);
		
		generateDiscreteUpdateCodeCInside(code);
		
		discreteUpdateCode="}\n";
		code.addDiscreteUpdateCode(discreteUpdateCode);*/
		
		//discreteUpdateCode+="if(block"+this.getBlockId()+".discreteTime>mp->time){\n";
		//discreteUpdateCode+="block"+this.getBlockId()+".discreteUpdated=0;\n";
		//discreteUpdateCode+="}\n";
		
		//discreteUpdateCode+="while(block"+this.getBlockId()+".discreteTime<=mp->time){\n";
		discreteUpdateCode+="while(block"+this.getBlockId()+".discreteTime<=mp->time||"+"block"+this.getBlockId()+".discreteTime-mp->time<0.0000001){\n";
		code.addDiscreteUpdateCode(discreteUpdateCode);
		
		generateDiscreteUpdateCodeCInside(code);
		
		discreteUpdateCode="block"+this.getBlockId()+".discreteTime+="+this.sampleTime+";\n";
		discreteUpdateCode+="block"+this.getBlockId()+".discreteUpdated=1;\n";
		discreteUpdateCode+="}\n";
		code.addDiscreteUpdateCode(discreteUpdateCode);
	}
	
	
	public void generateUpdateCodeC(CodeStructC code) throws MatDimException {
		
	}

}
