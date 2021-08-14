package block.io;

import code.c.DataTypeC;
import block.Block;

public class State {
	private int id;
	private String name;
	private String localName;
	private int width=1;
	private DataTypeC type=DataTypeC.REAL;
	
	private Block block;
	
	public State(Block block,int id,String localName){
		this.block=block;
		this.type=DataTypeC.REAL;
		this.id=id;
		this.name="Block"+block.getBlockId()+"_State_"+localName;
		this.localName=localName;
	}
	
	public String getName() {
		this.name="Block"+block.getBlockId()+"_State_"+localName;
		return this.name;
	}
	
	public String getLocalName() {
		return this.localName;
	}
	
	public String getDefineString() {
		String defineString="";
		
		switch(type) {
		case REAL:
			defineString="REAL"; 
		}
		
		return defineString;
	}
	
	public int getId() {
		return this.id;
	}
	
	public int getWidth() {
		return this.width;
	}
}
