package com.ncslab.block.io;

import com.ncslab.block.Block;
import com.ncslab.block.data.Data;
import com.ncslab.block.data.DataType;
import lombok.Getter;

public class State {
	@Getter
    private int id;
	private String name;
	@Getter
    private String localName;
	private int width=1;

	private Block block;

	private Data data=null;

	private Data derivateData=null;

	public State(Block block,int id,String localName){
		this.block=block;
		this.id=id;
		this.name="Block"+block.getBlockId()+"_State_"+localName;
		this.localName=localName;

		this.data=new Data();
		this.derivateData=new Data();
	}

	public State(Block block,int id,String localName,int height,int width){
		this.block=block;
		this.id=id;
		this.name="Block"+block.getBlockId()+"_State_"+localName;
		this.localName=localName;

		this.data=new Data(height,width);
		this.derivateData=new Data(height,width);
	}

	public String getName() {
		this.name="Block"+block.getBlockId()+"_State_"+localName;
		return this.name;
	}

	public String getDerivativeName() {
		this.name="Block"+block.getBlockId()+"_State_"+localName;

		return this.name+"_Derivative";
	}

    public DataType getDataType() {
		return data.getDataType();
	}

	public String getDefineString() {
		String defineString="";

		switch(data.getDataType()) {
		case REAL:
			defineString="REAL";
			break;
		case MATRIX:
			defineString="Matrix";
		}

		return defineString;
	}

	public String getDefineCodeC() {
		String code;
		if(this.block.getBlockType().equals("Discrete Transfer Fcn")&&this.data.getDataType()==DataType.REAL) {
			code="Matrix "+name+"(1,1);\n";
		}else {
		code=data.getDefineCodeC(this.getName());
		}

		switch(data.getDataType()) {
		case REAL:
			code+=getDefineString()+" "+getDerivativeName()+";\n";
			break;
		case MATRIX:
			code+=getDefineString()+" "+getDerivativeName()+"("+data.getHeight()+","+data.getWidth()+")"+";\n";
			break;
		}

		return code;
	}

    public int getWidth() {
		return data.getWidth();
	}

	public int getHeight() {
		return data.getHeight();
	}
}
