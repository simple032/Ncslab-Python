package com.ncslab.circuit.block.electblock;

import java.util.Vector;
import com.ncslab.block.Block;

import com.ncslab.circuit.loop.CircuitLoopException;

public interface ElectBlock {
	public String getGainBlock() throws CircuitLoopException;
	
	public void setElecLoopString(String electLoopString);
	
	public void setRelatedBlockList(Vector<Block> relatedBlockList);
	
	public Vector<Block> getRelatedBlockList();
	
	public boolean isLoopPoint();
	
	public void clearLoop();
}
