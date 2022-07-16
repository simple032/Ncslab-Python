package circuit.block.io;

import java.util.Vector;

import circuit.block.CircuitBlock;
import circuit.block.io.CircuitNode;

public class CircuitPort {
	private CircuitBlock block;
	private int number;
	private String name;
	
	private CircuitNode circuitNode;
	
	private boolean isCurrentDecided=false;
	
	private Vector<PortCurrent> currentList=new Vector<PortCurrent>();
	
	public CircuitPort(CircuitBlock block,String name,int number){
		this.block=block;
		this.name=name;
		this.number=number;
	}
	
	public void setCurrentList(Vector<PortCurrent> currentList) {
		this.currentList=currentList;
		isCurrentDecided=true;
	}
	
	public void setCircuitNode(CircuitNode circuitNode) {
		this.circuitNode=circuitNode;
	}
	
	public CircuitNode getCircuitNode() {
		return this.circuitNode;
	}
	
	public Vector<PortCurrent> getCurrentList(){
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
	
	public Vector<PortCurrent> getReverseCurrentList() {
		Vector<PortCurrent> reverseCurrentList=new Vector<PortCurrent>();
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
	
	public void setupDirectCurrent(Vector<CircuitPort> otherPortList) {
		
		currentList.clear();
		for(CircuitPort port:otherPortList) {
			Vector<PortCurrent> currents=port.getCurrentList();
			for(PortCurrent current:currents) {
				currentList.add(current.getReverseCurrent());
			}
		}
		
		isCurrentDecided=true;
	}
}
