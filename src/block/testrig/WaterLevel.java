package block.testrig;

import org.json.JSONObject;

import block.Block;
import block.io.InputPort;
import block.io.OutputPort;
import code.c.CodeStructC;
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
	
	public String generateInitCodeM() {
		String code=super.generateInitCodeM();
		code+="Block"+getBlockId()+"_PumpState=0;\n";
		code+="Block"+getBlockId()+"_LevelState=0;\n";
		
		return code;
	}
	
	public String generateUpdateCodeM() {
		String code=super.generateUpdateCodeM();
		
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
		
		return code;
	}
	
	public String generateOutputCodeM() {
		String code="Block"+this.getBlockId()+"_Output1="
				+"Block"+getBlockId()+"_PumpState"
				+";\n";
		
		code+="Block"+this.getBlockId()+"_Output2="
				+"Block"+getBlockId()+"_LevelState"
				+ ";\n";
		
		return code;
	}
	
	code.c.State pumpState;
	code.c.State levelState;
	public void generateInitCodeC(CodeStructC code) {
		super.generateInitCodeC(code);
		
		pumpState=code.addState(this, "pumpState");
		levelState=code.addState(this, "levelState");
		
		String initCode="/*Code for initialization of block WaterLevel:("+getBlockId()+")"+getBlockName()+"*/\n";
		initCode+=pumpState.getName()+"="+0+";\n"
					+levelState.getName()+"="+0+";\n"; 
		
		code.addInitCode(initCode);
	}
	
	public void generateOutputCodeC(CodeStructC code) {
		String outputCode="/*Code for output of block WaterLevel:("+getBlockId()+")"+getBlockName()+"*/\n";
		
		outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"="+pumpState.getName()+";\n";
		outputCode+=outputPortList.get(1).getOutputSignalC().getName()+"="+levelState.getName()+";\n";
		
		code.addOutputCode(outputCode);
	}
}
