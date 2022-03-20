package block.io.terminal;

import block.Block;

abstract public class Terminal {
	protected int id;
	protected String name;
	protected String localName;
	
	protected String terminalName;
	
	
	protected Block block;
	
	public Terminal(Block block,int id,String localName){
		this.id=id;
		this.block=block;
		this.localName=localName;
		this.terminalName="Block"+block.getBlockId()+"_Terminal_"+localName;
	}
	
	public String getTerminalDefineCode(String type) {
		String code="";
		code+="TERMINAL "+this.terminalName+"={"+type+",&"+this.name+"};\n";
		return code;
	}
	
	public String getTerminalName() {
		return this.terminalName;
	}
	
	public String getName() {
		return this.name;
	}
	
	abstract public String getDefineCodeC();
}
