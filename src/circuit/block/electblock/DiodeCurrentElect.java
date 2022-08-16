package circuit.block.electblock;

import java.util.Vector;

import org.json.JSONObject;

import block.Block;
import block.elect.DiodeCurrent;
import ncslablink.NCSLabModel;

public class DiodeCurrentElect extends DiodeCurrent implements ElectBlock {
	
	private String electLoopString;
	
	private Vector<Block> relatedBlockList=new Vector<Block>();
	
	public DiodeCurrentElect(JSONObject blockJSON, NCSLabModel model) {
		super(blockJSON,model);
	}
	
	public String getGainBlock() {
		return null;
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
