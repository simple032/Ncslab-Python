package com.ncslab.block.io;

import com.ncslab.block.data.DataType;
import com.ncslab.block.Block;
import lombok.Getter;
import lombok.Setter;

public class OutputSignal {
	private int id;
	private String name;
	private String localName;
	@Getter
    private int width=1;
	@Getter
    private int height=1;
	@Setter
    @Getter
    private DataType dataType = DataType.REAL;
	@Getter
    private Block block;
	private int outputPortId;

	public OutputSignal(Block block,int id,int outputPortId,String localName){
		this.block=block;
		this.id=id;
		this.name="Block"+block.getBlockId()+"_Output"+outputPortId;
		this.localName=localName;
		this.outputPortId=outputPortId;
	}

	public OutputSignal(Block block, int id, int outputPortId, String localName, int width,int height) {
		this.block=block;
		this.id=id;
		this.width=width;
		this.height=height;
		this.name="Block"+block.getBlockId()+"_Output"+outputPortId;
		this.localName=localName;
		this.outputPortId=outputPortId;

		if(width>1||height>1) {
			this.dataType =DataType.MATRIX;
		}
	}

	public String getName() {
		this.name="Block"+block.getBlockId()+"_Output"+outputPortId;
		return this.name;
	}

	public String getDefineString() {
		String defineString="";

		switch(dataType) {
		case REAL:
		case MATRIX:
			defineString="REAL";
		}

		return defineString;
	}

	public String getDefineCodeC() {
		String code="";
		switch(dataType) {
		case REAL:
			code+="REAL "+getName()+";\n";
			break;
		case MATRIX:
			//code+="REAL "+name+"["+initMatrix.getRowDimension()+"]["+initMatrix.getColumnDimension()+"]"+";\n";
			code+="Matrix "+getName()+"("+height+","+width+")"+";\n";
			break;
		}
		return code;

	}

    public void setWidth(int width) {
		// TODO Auto-generated method stub
		this.width = width;
		if(width>1) {
			this.dataType =DataType.MATRIX;
		}
	}

    public void setHeight(int height) {
		// TODO Auto-generated method stub
		this.height = height;
		if(height>1) {
			this.dataType =DataType.MATRIX;
		}
	}

}
