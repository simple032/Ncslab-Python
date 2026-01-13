package com.ncslab.circuit2.block.io;

import java.util.Vector;

import com.greenpineyu.fel.parser.FelParser.integerLiteral_return;

import com.ncslab.circuit2.block.BlockModeType;
import com.ncslab.circuit2.block.io.CircuitPort;

import com.ncslab.circuit2.partition.CircuitPartition;

public class CircuitNode {
	private Vector<CircuitPort> circuitPortList=new Vector<CircuitPort>();
	private Vector<CircuitPort> vsCircuitPortList=new Vector<CircuitPort>();
	private Vector<CircuitPort> combinedCircuitPortList = new Vector<CircuitPort>();
	
	private Vector<CircuitPartition> partitionList = new Vector<CircuitPartition>();
	
	private boolean isFloating = true;
	private boolean isRef=false;
	private boolean isPartRef=false;
	private boolean isOpAmpOutput = false;
	private int nodeId;
	
	private int partitionerNodeId;
	
	private int partNodeId;
	
	public CircuitNode(int nodeId) {
		this.nodeId=nodeId;
	}
	
	public void setIsRef(boolean isRef) {
		this.isRef=isRef;
	}
	
	public boolean getIsRef() {
		return this.isRef;
	}
	
	public void setIsPartRef(boolean isPartRef) {
		this.isPartRef=isPartRef;
	}
	
	public boolean getIsPartRef() {
		return this.isPartRef;
	}
	
	public int getNodeId() {
		return this.nodeId;
	}
	
	public void setNodeId(int nodeId) {
		this.nodeId = nodeId;
	}
	
	public int getPartNodeId() {
		return this.partNodeId;
	}
	
	public void setPartNodeId(int partNodeId) {
		this.partNodeId = partNodeId;
	}
	
	public int getPartitionerNodeId() {
		return this.partitionerNodeId;
	}
	
	public void setPartitionerNodeId(int partitionerNodeId) {
		this.partitionerNodeId = partitionerNodeId;
	}
	
	public String getHisString() {
		String hisString="0";
		if(!isOpAmpOutput) {
			for(CircuitPort port:circuitPortList) {
				String sign=(port.getCircuitPortType()==CircuitPortType.Left)?"+":"-";
				hisString+=sign+port.getBlock().getHisString();
			}
		}
		return hisString;
	}
	
	public double getHisValue() {
		double hisValue=0;
		
		if(!isOpAmpOutput) {
			for(CircuitPort port:circuitPortList) {
				double sign=(port.getCircuitPortType()==CircuitPortType.Left)?1.0:-1.0;
				hisValue+=sign*port.getBlock().getHisValue();
			}
		}
		
		return hisValue;
	}
	
	public Vector<CircuitPartition> getPartitionList(){
		return this.partitionList;
	}
	
	public void addPartition(CircuitPartition part) {
		this.partitionList.add(part);
	}
	
	public void addCircuitPort(CircuitPort circuitPort) {
		circuitPortList.add(circuitPort);
		circuitPort.setCircuitNode(this);
		if(circuitPort.getBlock().getBlockModeType()==BlockModeType.VoltageSource) {
			vsCircuitPortList.add(circuitPort);
		}else 
		if(circuitPort.getBlock().getBlockModeType()==BlockModeType.OpAmp && circuitPort.getName() == "RConn1"){
			isOpAmpOutput = true;
		}
	}
	
	public void addCombinedCircuitPort(CircuitPort circuitPort) {
		combinedCircuitPortList.add(circuitPort);
	}
	
	public Vector<CircuitPort> getCircuitPortList(){
		return this.circuitPortList;
	}
	
	public Vector<CircuitPort> getVsCircuitPortList(){
		return this.vsCircuitPortList;
	}
	
	public boolean isCircuitPortIncluded(CircuitPort port) {
		for(CircuitPort circuitPort:circuitPortList) {
			if(port==circuitPort) {
				return true;
			}
		}
		return false;
	}
	
	public String getNodeString() {
		return "V"+nodeId;
	}

	public Vector<CircuitPort> getCombinedCircuitPortList() {
		return combinedCircuitPortList;
	}

	public boolean isFloating() {
		return isFloating;
	}

	public void setFloating(boolean isFloating) {
		this.isFloating = isFloating;
	}

}
