package block.io;

import block.data.DataType;
import block.Block;

public class OutputSignal {
	private int id;
	private String name;
	private String localName;
	private int width=1;
	private int height=1;
	private DataType type=DataType.REAL;
	private Block block;
	private int outputPortId;
	
	public OutputSignal(Block block,int id,int outputPortId,String localName){
		this.block=block;
		this.type=DataType.REAL;
		this.id=id;
		this.name="Block"+block.getBlockId()+"_Output"+outputPortId;
		this.localName=localName;
		this.outputPortId=outputPortId;
	}
	
	public OutputSignal(Block block, int id, int outputPortId, String localName, int width,int height) {
		this.block=block;
		this.type=DataType.REAL;
		this.id=id;
		this.width=width;
		this.height=height;
		this.name="Block"+block.getBlockId()+"_Output"+outputPortId;
		this.localName=localName;
		this.outputPortId=outputPortId;
		
		if(width>1||height>1) {
			this.type=DataType.MATRIX;
		}
	}

	public String getName() {
		this.name="Block"+block.getBlockId()+"_Output"+outputPortId;
		return this.name;
	}
	
	public String getDefineString() {
		String defineString="";
		
		switch(type) {
		case REAL:
		case MATRIX:
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
		if(width>1) {
			this.type=DataType.MATRIX;
		}
	}
	
	public int getHeight() {
		return this.height;
	}

	public void setHeight(int height) {
		// TODO Auto-generated method stub
		this.height = height;
		if(height>1) {
			this.type=DataType.MATRIX;
		}
	}
	
	public DataType getDataType() {
		return this.type;
	}
}
