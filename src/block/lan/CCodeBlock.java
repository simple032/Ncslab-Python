package block.lan;

import code.c.CodeStructC;

public interface CCodeBlock {
	public void generateBlockInitCodeC(CodeStructC code);
	public void generateInitCodeC(CodeStructC code);
	public void generateBlockOutputCodeC(CodeStructC code);
	public void generateOutputCodeC(CodeStructC code);
	public void generateBlockUpdateCodeC(CodeStructC code);
	public void generateUpdateCodeC(CodeStructC code);
	
	public void generateBlockDerivativeCodeC(CodeStructC code);
	public void generateDerivativeCodeC(CodeStructC code);
}
