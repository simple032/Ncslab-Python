package circuit.block.io;

import circuit.block.CircuitBlock;

public class PortCurrent {
	private CircuitBlock circuitBlock;
	private boolean sign;
	
	PortCurrent(CircuitBlock circuitBlock,boolean sign){
		this.circuitBlock=circuitBlock;
		this.sign=sign;
	}
	
	public PortCurrent getReverseCurrent() {
		return new PortCurrent(circuitBlock,!sign);
	}
	
	public CircuitBlock getCircuitBlock() {
		return this.circuitBlock;
	}
	
	public boolean getSign() {
		return this.sign;
	}
}
