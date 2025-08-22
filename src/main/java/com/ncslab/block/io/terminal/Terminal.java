package com.ncslab.block.io.terminal;

import com.ncslab.block.Block;
import lombok.Getter;

abstract public class Terminal {
	protected int id;
    @Getter
    protected String name;
	protected String localName;
    @Getter
    protected String terminalName;

	protected Block block;

	public Terminal(Block block, int id, String localName){
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

    abstract public String getDefineCodeC();
}
