package com.ncslab.block.io.terminal;

import com.ncslab.block.Block;
import lombok.Getter;
import lombok.Setter;

public class ScopeStruct extends Terminal {
	@Setter
    private int maxDataLength=500;
	@Getter
    private int width=1;
	@Getter
    private int height=1;

	public ScopeStruct(Block block,int id,String localName){
		super(block, id, localName);
		this.name="Block"+block.getBlockId()+"_Scope_"+localName;
		this.localName=localName;
	}

	public String getDefineCodeC() {
		String code="";

		//code+="REAL "+this.name+"_Buffer["+this.maxDataLength*this.width*this.height+"];\n";
		//code+="REAL "+this.name+"_Time["+this.maxDataLength+"];\n";
		code+="SCOPE "+this.name+"={(char *)\""+this.localName+"\","
            +"(char *)\""+this.block.getBlockPath()+"\","
            +"(char *)\""+this.block.getBlockUUID()+"\","
            +maxDataLength+","+width+","+height+"};\n";//",0,"+this.name+"_Buffer"+","+this.name+"_Time"+"};\n";
		code+=this.getTerminalDefineCode("Scope");

		return code;
	}

	public void setDimension(int width,int height) {
		this.width=width;
		this.height=height;
	}

}
