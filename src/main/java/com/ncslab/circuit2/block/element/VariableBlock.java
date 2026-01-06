package com.ncslab.circuit2.block.element;

public interface VariableBlock {
	//变量变化的判断标志名
	String isVariableChanged="isVariableChanged";
	//获得变参数模块的ID
	public int getVariableBlockId();
	public void setVariableBlockId(int variableBlockId);
	//变参数模块参数的定义代码
	public String getVariableDefineCode();
	//参数变化的时候需要设置isVariableChanged=1的代码
	public String getVariableChangeCode();
	
	public String getVariableString();
}
