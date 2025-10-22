package com.ncslab.block.io;

import com.ncslab.block.Block;

import com.ncslab.block.data.Data;
import com.ncslab.block.data.DataType;
import lombok.Getter;
import lombok.Setter;
import Jama.Matrix;

public class Parameter {

    @Getter
    private int id;
    //this.name="Block"+block.getBlockId()+"_Parameter_"+localName;
    //区分监控组态中不同模块中的参数,replace方法用于处理部分模块的非连续字符串命名问题
    //		this.name=block.getBlockName().replace(" ", "_").replace("-", "_").replace("(", "_").replace(")", "")+"_"+localName;
    @Getter
    private String name;
    @Getter
    private String localName;
    @Getter
	@Setter
	private Block block;

    @Getter
    private Data data=null;

	public Parameter(Block block,int id,String localName,String inString) {
		this.block=block;
		this.id=id;
		this.localName=localName;

		// updateName();

		data=new Data(inString);
	}

	// Method to update parameter name after block reference is set
	public void updateName() {
		if (block == null) {
			// If block is null, use a generic name
			this.name = "Parameter_" + localName;
			return;
		}
		
		// String blockUUID = block.getBlockUUID(); // Placeholder for UUID retrieval logic
		// if (blockUUID != null && !blockUUID.equals("null") && !blockUUID.isEmpty()) {
		// 	this.name = "Block"+blockUUID.replace("-","_")+"_"+localName;
		// } else {
			// Use block ID when UUID is null/empty
			this.name = "Block"+block.getBlockId()+"_"+localName;
		// }
		
	}

    public DataType getDataType() {
		return data.getDataType();
	}

    public double getDouble() { return data.getInitValue(); }

    public double getValue() { return data.getInitValue(); }

    public String getInitString() { return data.getInitString(); }

    public String getDataString() { return data.getDataString(); }

	public double[]  getDoubleArray() { return data.getDoubleArray(); }

	public Matrix getMatrix() { return data.getMatrix(); }

    public boolean equals(String string) { return string.equals(data.getInitString());}

	public String getDefineString() {
		String defineString="";

		switch(data.getDataType()) {
		case REAL:
            defineString="REAL";
            break;
		case MATRIX:
			defineString="Matrix";
            break;
		}

		return defineString;
	}

    public int getWidth() {
		return data.getWidth();
	}

	public int getHeight() {
		return data.getHeight();
	}

	public String getInitCodeM() {
		String code;

		code=data.getInitCodeM(this.getName());

		return code;
	}

    public String getInitCodeC() {
		String code;

		code=data.getInitCodeC(this.getName());

		return code;
	}

	public String getDefineCodeC() {
		String code;

		code=data.getDefineCodeC(this.getName());

		return code;
	}

    // TODO:
//	public String getInitCodePLC() {
//		String code;
//
//		code=data.getInitCodePLC(this.getName());
//
//		return code;
//	}
//
//    // TODO:
//	public String getDefineCodePLC() {
//		String code;
//
//		code=data.getDefineCodePLC(this.getName());
//
//		return code;
//	}

	public boolean isZero() {
		return data.isZero();
	}

	public String getDataStructureInitCodeC() {
		String code="";
		//code+="parameter"+block.getBlockId()+"_"+this.getId()+".name=(char *)\""+this.getLocalName()+"\";\n";
		code+="parameter"+block.getBlockId()+"_"+this.getId()+".name=(char *)\""+this.localName+"\";\n";
		code+="parameter"+block.getBlockId()+"_"+this.getId()+".width="+this.getWidth()+";\n";
		code+="parameter"+block.getBlockId()+"_"+this.getId()+".height="+this.getHeight()+";\n";
		if(data.getDataType()==DataType.REAL) {
			code+="parameter"+block.getBlockId()+"_"+this.getId()+".type=SINGLE;\n";
			code+="parameter"+block.getBlockId()+"_"+this.getId()+".vp=&"+this.name+";\n";
		}
		else {
			code+="parameter"+block.getBlockId()+"_"+this.getId()+".type=MATRIX;\n";
			code+="parameter"+block.getBlockId()+"_"+this.getId()+".vp=&"+this.name+";\n";
		}
//		code+="parameter"+block.getBlockId()+"_"+this.getId()+".path=(char *)\""+block.getModel().getModelRealName()+"/"+block.getBlockName()+"\";\n";
		code+="parameter"+block.getBlockId()+"_"+this.getId()+".path=(char *)\""+block.getBlockPath()+"/"+block.getBlockName()+"\";\n";
		return code;
	}

    public boolean isScalar(){
        return data.getHeight()==1 && data.getWidth()==1;
    }
}
