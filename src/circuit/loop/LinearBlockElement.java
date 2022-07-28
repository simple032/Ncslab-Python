package circuit.loop;

import block.Block;

public class LinearBlockElement {
	private Block block;
	private boolean sign;
	public LinearBlockElement(Block block,boolean sign) {
		this.block=block;
		this.sign=sign;
	}
	
	public Block getBlock() {
		return this.block;
	}
	
	public boolean getSign() {
		return this.sign;
	}
	
	public void setSign(boolean sign) {
		this.sign=sign;
	}
	
	public String getOutputNameString() {
		return block.getOutputPortList().get(0).getOutputSignalC().getName(); 
	}
}
