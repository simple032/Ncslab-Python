package com.ncslab.circuit.block.electblock;

import java.util.ArrayList;
import java.util.List;
import com.ncslab.block.Block;

import com.ncslab.circuit.loop.CircuitLoopException;

public interface ElectBlock {
	public String getGainBlock() throws CircuitLoopException;
	
	public void setElecLoopString(String electLoopString);
	
	public void setRelatedBlockList(List<Block> relatedBlockList);
	
	public List<Block> getRelatedBlockList();
	
	public boolean isLoopPoint();
	
	public void clearLoop();
}
