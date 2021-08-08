package block.testrig;

import org.json.JSONObject;

import block.Block;
import block.io.InputPort;
import block.io.OutputPort;
import ncslablink.NCSLabModel;

public class WaterLevel extends Block {
	private double pumeK=1;
	private double pumeT=2;
	
	private double waterLevelK=0.1;
	private double waterLevelT=50;
	
	public WaterLevel(JSONObject blockJSON,NCSLabModel model) {
		super(blockJSON,model);
		
		//一个输入，两个输出
		inputPortList.add(new InputPort(this,1));
		outputPortList.add(new OutputPort(this,1,false));
		outputPortList.add(new OutputPort(this,2,false));
	}
	
	public String generateInitCode() {
		String code=super.generateInitCode();
		code+="Block"+getBlockId()+"_PumpState=0;\n";
		code+="Block"+getBlockId()+"_LevelState=0;\n";
		
		this.initCode=code;
		return code;
	}
	
	public String generateUpdateCode() {
		String code=super.generateUpdateCode();
		
		code+="Block"+getBlockId()+"_PumpState="
				+"Block"+getBlockId()+"_PumpState+"
				+"(Block"+getInputPortList().get(0).getLinkedLine().getLinkedOutputPort().getBLock().getBlockId()
				+"_Output"
				+getInputPortList().get(0).getLinkedLine().getLinkedOutputPort().getNumber()
				+"*"+pumeK+"-"+"Block"+getBlockId()+"_PumpState)"
				+"*"+(1/pumeT)
				+"*"+getModel().getConfig().getFixedStep()
				+";\n";
		
		code+="Block"+getBlockId()+"_LevelState="
				+"Block"+getBlockId()+"_LevelState+"
				+"(Block"+getBlockId()+"_PumpState*"+waterLevelK+"-"+"Block"+getBlockId()+"_LevelState)"
				+"*"+(1/waterLevelT)
				+"*"+getModel().getConfig().getFixedStep()
				+";\n";
		
		this.updateCode=code;
		return code;
	}
	
	public String generateOutputCode() {
		String code="Block"+this.getBlockId()+"_Output1="
				+"Block"+getBlockId()+"_PumpState"
				+";\n";
		
		code+="Block"+this.getBlockId()+"_Output2="
				+"Block"+getBlockId()+"_LevelState"
				+ ";\n";
		
		return code;
	}
}
