package circuit.block.electblock;

import java.util.Vector;

import org.json.JSONObject;

import block.Block;
import block.elect.DiodeCurrent;
import ncslablink.NCSLabModel;

public class DiodeCurrentElect extends DiodeCurrent implements ElectBlock {
	
	public DiodeCurrentElect(JSONObject blockJSON, NCSLabModel model) {
		super(blockJSON,model);
	}
	
	@Override
	public String getGainBlock() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setElecLoopString(String electLoopString) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setRelatedBlockList(Vector<Block> relatedBlockList) {
		// TODO Auto-generated method stub

	}

	@Override
	public Vector<Block> getRelatedBlockList() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isLoopPoint() {
		// TODO Auto-generated method stub
		return false;
	}

}
