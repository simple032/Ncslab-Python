package com.ncslab.circuit2.line;

import java.util.Vector;

import org.json.JSONObject;

import com.ncslab.block.Block;
import com.ncslab.block.io.OutputPort;
import com.ncslab.circuit2.block.CircuitBlock;
import com.ncslab.circuit2.block.io.CircuitPort;
import com.ncslab.line.Line;
import com.ncslab.ncslablink.NCSLabModel;

public class CircuitLine {
	private Vector<CircuitPort> circuitPortList = new Vector<CircuitPort>();
	private String linePath;
	
	public CircuitLine(CircuitPort fromPort,CircuitPort toPort) {
		circuitPortList.add(fromPort);
		circuitPortList.add(toPort);
	}

	public CircuitLine(JSONObject lineJSON, Vector<CircuitBlock> blockList) {
		String fromBlockName = lineJSON.getString("fromBlockName");
		String toBlockName = lineJSON.getString("toBlockName");
		linePath=lineJSON.getString("linePath");

		// 寻找FromBlock和ToBlock
		CircuitBlock fromBlock = null;
		CircuitBlock toBlock = null;

		for (CircuitBlock block : blockList) {
			if (block.getBlockName().equals(fromBlockName)&&block.getBlockPath().equals(linePath)) {
				fromBlock = block;
			}
			if (block.getBlockName().equals(toBlockName)&&block.getBlockPath().equals(linePath)) {
				toBlock = block;
			}
		}

		if (fromBlock == null || toBlock == null) {
			return;
		}

		// 寻找FromBLock的CircuitPort
		String fromPortNo = lineJSON.getString("fromPortNo");
		CircuitPort fromPort = null;

		Vector<CircuitPort> fromBlockPortList = fromBlock.getCurcuitPortList();
		for (CircuitPort circuitPort : fromBlockPortList) {
			if (circuitPort.getName().equals(fromPortNo)) {
				fromPort = circuitPort;
			}
		}

		if (fromPort == null) {
			return;
		}
		fromPort.getCircuitLineList().add(this);
		circuitPortList.add(fromPort);

		// 寻找ToBLock的CircuitPort
		String toPortNo = lineJSON.getString("toPortNo");
		CircuitPort toPort = null;

		Vector<CircuitPort> toBlockPortList = toBlock.getCurcuitPortList();
		for (CircuitPort circuitPort : toBlockPortList) {
			if (circuitPort.getName().equals(toPortNo)) {
				toPort = circuitPort;
			}
		}

		if (toPort == null) {
			return;
		}
		toPort.getCircuitLineList().add(this);
		circuitPortList.add(toPort);
	}
	
	public CircuitPort getFromPort() {
		return this.circuitPortList.get(0);
	}
	
	public CircuitPort getToPort() {
		return this.circuitPortList.get(1);
	}
	
	public void replacePort(CircuitPort oldOne,CircuitPort newOne) {
		if(circuitPortList.get(0)==oldOne) {
			circuitPortList.set(0, newOne);
		}
		if(circuitPortList.get(1)==oldOne) {
			circuitPortList.set(1, newOne);
		}
	}
	
	public static CircuitLine createCircuitLine(JSONObject lineJSON,Vector<CircuitBlock> blockList) {
		CircuitLine line=new CircuitLine(lineJSON,blockList);
		
		return line;
	}
}
