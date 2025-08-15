package com.ncslab.circuit.block.io;

import java.util.ArrayList;
import java.util.List;

import com.ncslab.circuit.block.CircuitBlock;
import com.ncslab.circuit.block.io.CircuitNode;

public class CircuitPort {
	private CircuitBlock block;
	private int number;
	private String name;
	
	private CircuitNode circuitNode;
	
	private boolean isCurrentDecided=false;
	
	private List<PortCurrent> currentList = new ArrayList<>();
	
	private CircuitPortType circuitPortType;
	
	public CircuitPort(CircuitBlock block,String name,CircuitPortType circuitPortType,int number){
		this.block=block;
		this.name=name;
		this.number=number;
		this.circuitPortType=circuitPortType;
	}
	
	public void setName(String name) {
		this.name=name;
	}
	
	public void setCurrentList(List<PortCurrent> currentList) {
		this.currentList=currentList;
		isCurrentDecided=true;
	}
	
	public void setCircuitNode(CircuitNode circuitNode) {
		this.circuitNode=circuitNode;
	}
	
	public CircuitPortType getCircuitPortType() {
		return this.circuitPortType;
	}
	
	public CircuitNode getCircuitNode() {
		return this.circuitNode;
	}
	
	public List<PortCurrent> getCurrentList(){
		return this.currentList;
	}
	
	public String getName() {
		return this.name;
	}
	
	public CircuitBlock getBlock() {
		return this.block;
	}
	
	public boolean getIsCurrentDecided() {
		return isCurrentDecided;
	}
	
	public List<PortCurrent> getReverseCurrentList() {
		List<PortCurrent> reverseCurrentList = new ArrayList<>();
		for(PortCurrent current:currentList) {
			reverseCurrentList.add(current.getReverseCurrent());
		}
		return reverseCurrentList;
	}
	
	public void setupDirectCurrent() {
		boolean sign=(block.getCurcuitPortList().get(0)==this);
		
		PortCurrent current=new PortCurrent(block,sign);
		
		currentList.clear();
		currentList.add(current);
		
		isCurrentDecided=true;
	}
	
	public void setupDirectCurrent(List<CircuitPort> otherPortList) {
		
		currentList.clear();
		for(CircuitPort port:otherPortList) {
			List<PortCurrent> currents=port.getCurrentList();
			for(PortCurrent current:currents) {
				currentList.add(current.getReverseCurrent());
			}
		}
		
		isCurrentDecided=true;
	}
}
