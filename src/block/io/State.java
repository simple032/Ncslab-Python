package block.io;

import block.Block;
import block.data.Data;
import block.data.DataType;

public class State {
	private int id;
	private String name;
	private String localName;
	private int width=1;
	
	private Block block;
	
	private Data data=null;
	
	public State(Block block,int id,String localName){
		this.block=block;
		this.id=id;
		this.name="Block"+block.getBlockId()+"_State_"+localName;
		this.localName=localName;
		
		this.data=new Data();
	}
	
	public State(Block block,int id,String localName,int height,int width){
		this.block=block;
		this.id=id;
		this.name="Block"+block.getBlockId()+"_State_"+localName;
		this.localName=localName;
		
		this.data=new Data(height,width);
	}
	
	public String getName() {
		this.name="Block"+block.getBlockId()+"_State_"+localName;
		return this.name;
	}
	
	public String getDerivativeName() {
		this.name="Block"+block.getBlockId()+"_State_"+localName;
		
		return this.name+"_Derivative";
	}
	
	public String getLocalName() {
		return this.localName;
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
			code="Matrix "+name+"(0,0);\n";
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
	
	public int getId() {
		return this.id;
	}
	
	public int getWidth() {
		return data.getWidth();
	}
	
	public int getHeight() {
		return data.getHeight();
	}
}
