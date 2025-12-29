package com.ncslab.circuit2.block.multielement;

public interface Recalc {
	//重计算的判断标志名
	String isRecalcString="isRecalc";
	public String getOldStatusString();
	public String getStatusString();
	public String getIsRecalcCode();
}
