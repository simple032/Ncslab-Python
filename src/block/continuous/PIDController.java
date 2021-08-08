package block.continuous;

import org.json.JSONObject;

import block.io.InputPort;
import block.io.OutputPort;
import ncslablink.NCSLabModel;

public class PIDController extends block.Block{
	public PIDController(JSONObject blockIn,NCSLabModel model) {
		super(blockIn,model);
		
		//一个输入，一个输出
		inputPortList.add(new InputPort(this,1));
		outputPortList.add(new OutputPort(this,1,true));
	}
	
	public String generateInitCode() {
		String code=super.generateInitCode();
		code+="Block"+getBlockId()+"_Integral=0;\n";
		
		this.initCode=code;
		return code;
	}
	
	public String generateUpdateCode() {
		String code=super.generateUpdateCode();
		
		code+="Block"+getBlockId()+"_Integral="
				+"Block"+getBlockId()+"_Integral+"
				+paramValues.getDouble("I")+"*"
				+"Block"+inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getBLock().getBlockId()+"_"
				+"Output"+inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getNumber()
				+"*"
				+getModel().getConfig().getFixedStep()
				+";\n";
		
		this.updateCode=code;
		return code;
	}
	
	public String generateOutputCode() {
		String code="Block"+this.getBlockId()+"_Output1=Block"+getBlockId()+"_Integral"
				+"+"+paramValues.getDouble("P")+"*"
				+"Block"+inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getBLock().getBlockId()+"_"
				+"Output"+inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getNumber()
				+";\n";
		
		return code;
	}
}
