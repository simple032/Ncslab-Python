package block.lan;

import code.c.CodeStructC;
import ncslablink.MatDimException;

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
}
