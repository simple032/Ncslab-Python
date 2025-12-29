package com.ncslab.circuit2.block.baseelement;

import org.json.JSONObject;

import com.ncslab.circuit2.block.CircuitBlock;
import com.ncslab.circuit2.block.io.CircuitPort;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.circuit2.line.CircuitLine;
import java.util.Vector;

abstract public class CircuitBlockMulti extends CircuitBlock {
	
	private Vector<CircuitBlockSingle> singleCurcuitBlockList=new Vector<CircuitBlockSingle>();
	private Vector<CircuitLine> circuitLineList=new Vector<CircuitLine>();
	
	public CircuitBlockMulti(int id,JSONObject blockJSON,NCSLabModel model) {
		super(id,blockJSON,model);
	}
	
	public Vector<CircuitBlockSingle> getSingleCurcuitBlockList(){
		return this.singleCurcuitBlockList;
	}
	
	public Vector<CircuitLine> getCircuitLineList(){
		return this.circuitLineList;
	}
	
	abstract public void setupSubCircuitBlocks(int blockId);
	
	public void replacePort(CircuitPort oldOne,CircuitPort newOne) {
		Vector<CircuitLine> lineList=oldOne.getCircuitLineList();
		for(CircuitLine line:lineList) {
			line.replacePort(oldOne, newOne);
		}
		
		newOne.setCircuitLineList(oldOne.getCircuitLineList());
	}
	
	public String getVoltageString() {
		String vString="";
		return vString;
	}
	
	abstract public void setupLogicCode();
	abstract public void setupLogicCode(int partId);
}
