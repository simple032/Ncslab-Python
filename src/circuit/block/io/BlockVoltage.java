package circuit.block.io;

import circuit.block.CircuitBlock;

public class BlockVoltage {
	
	private CircuitBlock circuitBlock;
	private boolean sign;
	
	public BlockVoltage(CircuitBlock circuitBlock,boolean sign){
		this.circuitBlock=circuitBlock;
		this.sign=sign;
	}
	
	public CircuitBlock getCircuitBlock() {
		return this.circuitBlock;
	}
	
	public boolean getSign() {
		return this.sign;
	}
}
