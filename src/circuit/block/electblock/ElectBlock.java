package circuit.block.electblock;

import java.util.Vector;
import block.Block;

public interface ElectBlock {
	public String getGainBlock();
	
	public void setElecLoopString(String electLoopString);
	
	public void setRelatedBlockList(Vector<Block> relatedBlockList);
	
	public Vector<Block> getRelatedBlockList();
	
	public boolean isLoopPoint();
}
