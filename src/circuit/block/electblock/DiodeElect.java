package circuit.block.electblock;

import java.util.Vector;

import org.json.JSONObject;

import block.Block;
import block.elect.Diode;
import ncslablink.NCSLabModel;

public class DiodeElect extends Diode implements ElectBlock {
	
	private String electLoopString;
	
	private Vector<Block> relatedBlockList=new Vector<Block>();
	
	public DiodeElect(JSONObject blockJSON, NCSLabModel model) {
		super(blockJSON,model);
	}
	
	public String getGainBlock() {
		String gainString="(("+this.getOutputPortVariable(0)+"!=0)?("+this.getOutputPortVariable(0)+"/"+this.getInputPortVariable(0)+"):0)\n";

		return gainString;
	}
	
	public void setElecLoopString(String electLoopString) {
		this.electLoopString=electLoopString;
	}
	
	public void setRelatedBlockList(Vector<Block> relatedBlockList) {
		this.relatedBlockList=relatedBlockList;
	}
	
	public Vector<Block> getRelatedBlockList(){
		return this.relatedBlockList;
	}
	
	public boolean isLoopPoint() {
		if(relatedBlockList.size()>0) {
			return true;
		}
		else {
			return false;
		}
	}

}
