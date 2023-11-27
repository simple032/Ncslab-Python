package com.ncslab.block.lan;

import com.ncslab.code.m.CodeStructM;

public interface MCodeBlock {
	public void generateOutputCodeM(CodeStructM code);
	public void generateBlockOutputCodeM(CodeStructM code);
	public void generateBlockInitCodeM(CodeStructM code);
	public void generateInitCodeM(CodeStructM code);
	public void generateBlockUpdateCodeM(CodeStructM code);
	public void generateUpdateCodeM(CodeStructM code);
	
	public void generateBlockDerivativeCodeM(CodeStructM code);
	public void generateDerivativeCodeM(CodeStructM code);
}
