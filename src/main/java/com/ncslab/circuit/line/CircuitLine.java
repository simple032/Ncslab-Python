package com.ncslab.circuit.line;

import java.util.Vector;

import org.json.JSONObject;

import com.ncslab.block.Block;
import com.ncslab.block.io.OutputPort;
import com.ncslab.circuit.block.CircuitBlock;
import com.ncslab.circuit.block.io.CircuitPort;
import com.ncslab.line.Line;
import com.ncslab.ncslablink.NCSLabModel;

public class CircuitLine {
	private Vector<CircuitPort> circuitPortList = new Vector<CircuitPort>();

	public CircuitLine(JSONObject lineJSON, Vector<CircuitBlock> blockList) {
		String fromBlockName = lineJSON.getString("fromBlockName");
		String toBlockName = lineJSON.getString("toBlockName");

		// 寻找FromBlock和ToBlock
		CircuitBlock fromBlock = null;
		CircuitBlock toBlock = null;

		for (CircuitBlock block : blockList) {
			if (block.getBlockName().equals(fromBlockName)) {
				fromBlock = block;
			}
			if (block.getBlockName().equals(toBlockName)) {
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
		circuitPortList.add(toPort);
	}
	
	public CircuitPort getFromPort() {
		return this.circuitPortList.get(0);
	}
	
	public CircuitPort getToPort() {
		return this.circuitPortList.get(1);
	}
	
	public static CircuitLine createCircuitLine(JSONObject lineJSON,Vector<CircuitBlock> blockList) {
		CircuitLine line=new CircuitLine(lineJSON,blockList);
		
		return line;
	}
}
