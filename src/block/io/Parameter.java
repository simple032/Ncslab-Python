package block.io;
import block.Block;

import block.data.Data;
import block.data.DataType;
import lombok.Getter;

public class Parameter {
	
	@Getter
    private int id;
	private String name;
	@Getter
    private String localName;
	private Block block;
	
	@Getter
    private Data data=null;
	
	public Parameter(Block block,int id,String localName,String dataString) {
		this.block=block;
		this.id=id;
		this.name="Block"+block.getBlockId()+"_Parameter_"+localName;
		this.localName=localName;
		
		data=new Data(dataString);
	}
	
	public String getName() {
		this.name="Block"+block.getBlockId()+"_Parameter_"+localName;
		return this.name;
	}

    public DataType getDataType() {
		return data.getDataType();
	}
	
	public String getDefineString() {
		String defineString="";
		
		switch(data.getDataType()) {
		case REAL:
		case MATRIX:
			defineString="REAL"; 
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

	public String getInitCodePLC() {
		String code;
		
		code=data.getInitCodePLC(this.getName());
		
		return code;
	}
	
	public String getDefineCodePLC() {
		String code;
		
		code=data.getDefineCodePLC(this.getName());
		
		return code;
	}
	
	public boolean isZero() {
		return data.isZero();
	}
	
	public String getDataStructureInitCodeC() {
		String code="";
		code+="parameter"+block.getBlockId()+"_"+this.getId()+".name=(char *)\""+this.getLocalName()+"\";\n";
		code+="parameter"+block.getBlockId()+"_"+this.getId()+".width="+this.getWidth()+";\n";
		code+="parameter"+block.getBlockId()+"_"+this.getId()+".height="+this.getHeight()+";\n";
		if(data.getDataType()==DataType.REAL) {
			code+="parameter"+block.getBlockId()+"_"+this.getId()+".type=SINGLE;\n";
			code+="parameter"+block.getBlockId()+"_"+this.getId()+".vp=&"+this.getName()+";\n";
		}
		else {
			code+="parameter"+block.getBlockId()+"_"+this.getId()+".type=MATRIX;\n";
			code+="parameter"+block.getBlockId()+"_"+this.getId()+".vp=&"+this.getName()+";\n";
		}
//		code+="parameter"+block.getBlockId()+"_"+this.getId()+".path=(char *)\""+block.getModel().getModelRealName()+"/"+block.getBlockName()+"\";\n";
		code+="parameter"+block.getBlockId()+"_"+this.getId()+".path=(char *)\""+block.getBlockPath()+"/"+block.getBlockName()+"\";\n";
		return code;
	}
}
