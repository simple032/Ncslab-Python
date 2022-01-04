package block.io;

import code.c.DataTypeC;
import block.Block;

public class OutputSignal {
	private int id;
	private String name;
	private String localName;
	private int width=1;
	private DataTypeC type=DataTypeC.REAL;
	private Block block;
	private int outputPortId;
	
	public OutputSignal(Block block,int id,int outputPortId,String localName){
		this.block=block;
		this.type=DataTypeC.REAL;
		this.id=id;
		this.name="Block"+block.getBlockId()+"_Output"+outputPortId;
		this.localName=localName;
		this.outputPortId=outputPortId;
	}
	
	public OutputSignal(Block block, int id, int outputPortId, String localName, int width) {
		this.block=block;
		this.type=DataTypeC.REAL;
		this.id=id;
		this.width=width;
		this.name="Block"+block.getBlockId()+"_Output"+outputPortId;
		this.localName=localName;
		this.outputPortId=outputPortId;
	}

	public String getName() {
		this.name="Block"+block.getBlockId()+"_Output"+outputPortId;
		return this.name;
	}
	
	public String getDefineString() {
		String defineString="";
		
		switch(type) {
		case REAL:
			defineString="REAL"; 
		}
		
		return defineString;
	}
	
	public int getWidth() {
		return this.width;
	}

	public void setWidth(int width) {
		// TODO Auto-generated method stub
		this.width = width;
	}
}
