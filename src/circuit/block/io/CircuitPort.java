package circuit.block.io;

import circuit.block.CircuitBlock;
import circuit.block.io.CircuitNode;

public class CircuitPort {
	private CircuitBlock block;
	private int number;
	private String name;
	
	private CircuitNode circuitNode;
	
	public CircuitPort(CircuitBlock block,String name,int number){
		this.block=block;
		this.name=name;
		this.number=number;
	}
	
	public void setCircuitNode(CircuitNode circuitNode) {
		this.circuitNode=circuitNode;
	}
	
	public CircuitNode getCircuitNode() {
		return this.circuitNode;
	}
	
	public String getName() {
		return this.name;
	}
	
	public CircuitBlock getBlock() {
		return this.block;
	}
}
