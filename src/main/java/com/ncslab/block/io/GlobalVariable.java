package com.ncslab.block.io;
import com.ncslab.block.Block;

import com.ncslab.block.data.Data;
import com.ncslab.block.data.DataType;

/**
 * This is a class for generating global variables in cpp code.
 * This is mainly for the project managers to define the global variables in the cpp file,
 * so that they can use these global variables freely.<br>
 * <b>HOW TO USE</b><br>
 * For example, I want to create a global variable <code>Model *model;</code> in the cpp file,
 * Then you can override the method <code>getDefineCodeC()</code> and make it return <code>"Model *model;\n"</code>.<br>
 * And then if you want to initialize it in <code>NCSLabInit()</code>, like <code>model = new Model();</code>, 
 * you can override the method <code>getInitCodeC()</code>, i.e,
 * You want to use <code>model = new Model();</code> in cpp file, 
 * you can <code>return "model=new Model();\n"</code> in that method.<br>
 * @author Ethy9160
 */
public abstract class GlobalVariable {
   	private int id;
	private String name;
	private String localName;
	private Block block;
	
	private Data data=null;
	
    /**
     * Constructor for Variable.
     * This is for constructing Global Variable.
     * @param block the block.
     * @param id the id of the variable, similar to those in <code>Parameter, State</code>.
     * @param localName the local name you want to use in the cpp file. The final name will be like:<br>
	 * 						<code>BlockName_LocalName</code>.
     * @param dataString I don't know what this mean in this scope, I just copy the structure from <code>Parameter</code>.
     * @author Ethy9160
     */
	public GlobalVariable(Block block,int id,String localName,String dataString) {
		this.block=block;
		this.id=id;
		//this.name="Block"+block.getBlockId()+"_Parameter_"+localName;
		this.name=block.getBlockName().replace(" ", "_").replace("-", "_").replace("(", "_").replace(")", "")+"_"+localName;
		this.localName=localName;
		
		data=new Data("233");
	}
	
	/**
	 * Get the name of the variable that will be used in the cpp file.
	 * @return String for the name of the variable.
	 */
	public String getName() {
		//this.name="Block"+block.getBlockId()+"_Parameter_"+localName;
		//区分监控组态中不同模块中的参数,replace方法用于处理部分模块的非连续字符串命名问题
		this.name=block.getBlockName().replace(" ", "_").replace("-", "_").replace("(", "_").replace(")", "")+"_"+localName;
		return this.name;
	}
	
	/**
	 * Get the local name of the variable. 
	 * The local name is the parameter you have passed into the constructor.
	 * @return String for the local name of the variable.
	 */
	public String getLocalName() {
		return this.localName;
	}
	
	/**
	 * IDK what this mean, I just copy the structure from <code>Parameter</code>.
	 * @return
	 */
	public DataType getDataType() {
		return data.getDataType();
	}



	/**
	 * This is for the project managers to define the Defined String in the cpp file.
	 * @return String for the define code in the cpp file.
	 * @author Ethy9160
	 */
	public abstract String getDefineCodeC();

	/**
     * This is for the project managers to define the Defined String in the cpp file.
     * For example, a variable named lr_model and you want it to be an instance of LearningRegression
     * in .cpp file,
     * then you can override this method and make it return "LinearRegression".
     * @return String of the datatype you want to declare in .cpp file.
     * @author Ethy9160
     */
    public abstract String getInitCodeC();

	/**
	 * This is for the project managers to control the finallized code in the cpp file.
	 * @return String for your final operator on the local variable. DO NOT RETURN NULL!
	 * @author Ethy9160
	 */
	public String getEndCodeC(){
		return "";
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
	
	public String getInitCodeM() {
		String code;
		
		code=data.getInitCodeM(this.getName());
		
		return code;
	}
	
	public Data getData() {
		return this.data;
	}
	
	public boolean isZero() {
		return data.isZero();
	}
	
	// TODO
	public String getDataStructureInitCodeC() {
		String code="";
		//code+="parameter"+block.getBlockId()+"_"+this.getId()+".name=(char *)\""+this.getLocalName()+"\";\n";
		// code+="parameter"+block.getBlockId()+"_"+this.getId()+".name=(char *)\""+this.name+"\";\n";
		// code+="parameter"+block.getBlockId()+"_"+this.getId()+".width="+this.getWidth()+";\n";
		// code+="parameter"+block.getBlockId()+"_"+this.getId()+".height="+this.getHeight()+";\n";
		// if(data.getDataType()==DataType.REAL) {
		// 	code+="parameter"+block.getBlockId()+"_"+this.getId()+".type=SINGLE;\n";
		// 	code+="parameter"+block.getBlockId()+"_"+this.getId()+".vp=&"+this.getName()+";\n";
		// }
		// else {
		// 	code+="parameter"+block.getBlockId()+"_"+this.getId()+".type=MATRIX;\n";
		// 	code+="parameter"+block.getBlockId()+"_"+this.getId()+".vp=&"+this.getName()+";\n";
		// }
		// //code+="parameter"+block.getBlockId()+"_"+this.getId()+".path=(char *)\""+block.getModel().getModelRealName()+"/"+block.getBlockName()+"\";\n";
		// code+="parameter"+block.getBlockId()+"_"+this.getId()+".path=(char *)\""+block.getBlockPath()+"/"+block.getBlockName()+"\";\n";
		return code;
	} 
}
