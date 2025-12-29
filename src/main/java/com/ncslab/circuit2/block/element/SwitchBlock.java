package com.ncslab.circuit2.block.element;

public interface SwitchBlock {
	default public boolean isDynamic() {
		return true;
	}
	public String getSwitchCode();
	public String getSwitchCode(int partId);
	public void setSwitchId(int switchId);
	public int getSwitchId();
	
	public void setSwitchPartId(int switchPartId);
	public int getSwitchPartId();
}
