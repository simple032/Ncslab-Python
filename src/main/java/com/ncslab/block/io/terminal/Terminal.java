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
		this.terminalName=sanitizeCIdentifier("Block"+block.getBlockId()+"_Terminal_"+localName);
	}

	/**
	 * Sanitizes a string to be a valid C identifier by replacing
	 * spaces and special characters with underscores.
	 */
	protected static String sanitizeCIdentifier(String name) {
		if (name == null) {
			return "null";
		}
		// Replace any character that is not a letter, digit, or underscore with an underscore.
		// Also ensure the result is a valid C identifier (doesn't start with a digit).
		String sanitized = name.replaceAll("[^a-zA-Z0-9_]", "_");
		// If it starts with a digit, prefix with an underscore.
		if (!sanitized.isEmpty() && Character.isDigit(sanitized.charAt(0))) {
			sanitized = "_" + sanitized;
		}
		return sanitized;
	}

	public String getTerminalDefineCode(String type) {
		String code="";
		code+="TERMINAL "+this.terminalName+"={"+type+",&"+this.name+"};\n";
		return code;
	}

    abstract public String getDefineCodeC();
}
