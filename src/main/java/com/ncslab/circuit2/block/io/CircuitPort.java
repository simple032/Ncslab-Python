package com.ncslab.circuit2.block.io;

import java.util.Vector;

import com.ncslab.circuit2.block.CircuitBlock;
import com.ncslab.circuit2.block.io.CircuitNode;
import com.ncslab.circuit2.block.baseelement.CircuitBlockSingle;
import com.ncslab.circuit2.line.CircuitLine;

public class CircuitPort {
	private CircuitBlock block;
	private int number;
	private String name;
	
	private CircuitNode circuitNode;
	
	private CircuitPortType circuitPortType;
	
	private Vector<CircuitLine> circuitLineList=new Vector<CircuitLine>();
	
	public CircuitPort(CircuitBlock block,String name,CircuitPortType circuitPortType,int number){
		this.block=block;
		this.name=name;
		this.number=number;
		this.circuitPortType=circuitPortType;
	}
	
	public void setName(String name) {
		this.name=name;
	}
	
	public void setCircuitNode(CircuitNode circuitNode) {
		this.circuitNode=circuitNode;
	}
	
	public void setCircuitPortType(CircuitPortType circuitPortType) {
		this.circuitPortType=circuitPortType;
	}
	
	public CircuitPortType getCircuitPortType() {
		return this.circuitPortType;
	}
	
	public CircuitNode getCircuitNode() {
		return this.circuitNode;
	}
	
	public String getName() {
		return this.name;
	}
	
	public void setCircuitLineList(Vector<CircuitLine> circuitLineList) {
		this.circuitLineList=circuitLineList;
	}
	
	public Vector<CircuitLine> getCircuitLineList() {
		return this.circuitLineList;
	}
	
	public CircuitBlockSingle getBlock() {
		if(this.block instanceof CircuitBlockSingle) {
			return (CircuitBlockSingle)(this.block);
		}
		else {
			return null;
		}
		
	}
	
	
}
