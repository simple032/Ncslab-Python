package block.io.terminal;

import block.Block;

public class ScopeStruct extends Terminal {
	private int maxDataLength=500;
	private int width=1;
	private int height=1;
	
	private String localName;
	
	public ScopeStruct(Block block,int id,String localName){
		super(block, id, localName);
		this.name="Block"+block.getBlockId()+"_Scope_"+localName;
		this.localName=localName;
	}
	
	public String getDefineCodeC() {
		String code="";
		
		//code+="REAL "+this.name+"_Buffer["+this.maxDataLength*this.width*this.height+"];\n";
		//code+="REAL "+this.name+"_Time["+this.maxDataLength+"];\n";
		code+="SCOPE "+this.name+"={(char *)\""+this.localName+"\","+maxDataLength+","+width+","+height+"};\n";//",0,"+this.name+"_Buffer"+","+this.name+"_Time"+"};\n";
		code+=this.getTerminalDefineCode("Scope");
		
		return code;
	}
	
	public void setDimension(int width,int height) {
		this.width=width;
		this.height=height;
	}
	
	public int getWidth() {
		return this.width;
	}
	
	public int getHeight() {
		return this.height;
	}
	
	public void setMaxDataLength(int maxDataLength) {
		this.maxDataLength=maxDataLength;
	}
}
