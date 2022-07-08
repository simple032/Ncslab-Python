package circuit.block.io;

import circuit.block.CircuitBlock;

public class CircuitPort {
	private CircuitBlock block;
	private int number;
	private String name;
	
	public CircuitPort(CircuitBlock block,String name,int number){
		this.block=block;
		this.name=name;
		this.number=number;
	}
	
	public String getName() {
		return this.name;
	}
	
	public CircuitBlock getBlock() {
		return this.block;
	}
}
