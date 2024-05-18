package com.ncslab.block.lan;

import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.MatDimException;

public interface CCodeBlock {
	public void generateBlockInitCodeC(CodeStructC code);
	public void generateInitCodeC(CodeStructC code);
	public void generateBlockOutputCodeC(CodeStructC code);
	public void generateOutputCodeC(CodeStructC code);
	public void generateBlockUpdateCodeC(CodeStructC code) throws MatDimException;
	public void generateUpdateCodeC(CodeStructC code) throws MatDimException;
	
	public void generateBlockDerivativeCodeC(CodeStructC code);
	public void generateDerivativeCodeC(CodeStructC code);
	
	public void generateDiscreteBlockUpdateCodeC(CodeStructC code) throws MatDimException;
	public void generateDiscreteUpdateCodeC(CodeStructC code) throws MatDimException;

	// /**
	//  * Generate the global code for the block in cpp file.
	//  * The variables and functions can be declared here,
	//  * and this code will be <b>directly</b> added to the begining 
	//  * of the <code>maincode.cpp</code> file, after the includings.
	//  * @param code CodeStructC object to store the generated code.
	//  * @Author: Ethy9160
	//  */
	// public void generateGlobalVariableC(CodeStructC code);

	// /**
	//  * Generate the finalize code for the block in cpp file.
	//  * This is the code that will be executed when the simulation ends.
	//  * It is for releasing the resources, such as closing files, freeing memory, etc.
	//  * @param code CodeStructC object to store the generated code.
	//  * @Author: Ethy9160
	//  */
	// public void generateFinalizeCodeC(CodeStructC code);
}
