package com.ncslab.block.io.terminal;

import com.ncslab.block.Block;
import com.ncslab.block.data.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Vector;

public class ScopeStruct extends Terminal {
	@Setter
    private int maxDataLength=500;
    @Getter
    private int width=1;
    @Getter
    private int height=1;

    @Getter
    private Vector<Double> timeList;
    @Getter
    private Vector<Double> dataList;

	public ScopeStruct(Block block,int id,String localName){
		super(block, id, localName);
		this.name="Block" + block.getBlockId() + "_Scope_" + localName;
		this.localName=localName;
		this.timeList=new Vector<>();
		this.dataList=new Vector<>();
	}

	public String getDefineCodeC() {
		String code="";

		//code+="REAL "+this.name+"_Buffer["+this.maxDataLength*this.width*this.height+"];\n";
		//code+="REAL "+this.name+"_Time["+this.maxDataLength+"];\n";
		
		// Only declare the SCOPE variable, initialization happens in getInitCodeC()
		code+="SCOPE "+this.name+";\n";
		code+=this.getTerminalDefineCode("Scope");

		return code;
	}

	public String getInitCodeC() {
		String code="";
		
		// Check if this platform supports path and uuid fields
		String blockPath = this.block.getBlockPath();
		String blockUUID = this.block.getBlockUUID();
		
		// Generate SCOPE initialization based on platform capabilities
		if (blockPath != null && blockUUID != null && !blockPath.isEmpty() && !blockUUID.isEmpty()) {
			// Windows/standard version with path and uuid
			code+=this.name+".name = (char *)\""+this.localName+"\";\n";
			code+=this.name+".path = (char *)\""+blockPath+"\";\n";
			code+=this.name+".uuid = (char *)\""+blockUUID+"\";\n";
			code+=this.name+".maxDataLength = "+maxDataLength+";\n";
			code+=this.name+".width = "+width+";\n";
			code+=this.name+".height = "+height+";\n";
			code+=this.name+".cursor = 0;\n";
			code+=this.name+".isFull = 0;\n";
		} else {
			// Linux/loong version without path and uuid
			code+=this.name+".name = (char *)\""+this.localName+"\";\n";
			code+=this.name+".maxDataLength = "+maxDataLength+";\n";
			code+=this.name+".width = "+width+";\n";
			code+=this.name+".height = "+height+";\n";
			code+=this.name+".cursor = 0;\n";
			code+=this.name+".isFull = 0;\n";
		}
		
		return code;
	}

	public void setDimension(int width,int height) {
		this.width=width;
		this.height=height;
	}

    public void addTimeSeries(double time,Data data) {
    	while(timeList.size()>=maxDataLength) {
            this.timeList.remove(0);
            if(this.height==1 && this.width==1) {
            	this.dataList.removeElementAt(0);
            }else{
                for (int h=0;h<this.height;h++) {
                    for (int w=0;w<this.width;w++) {
                        this.dataList.removeElementAt(0);
                    }
                }
            }
        }

        this.timeList.add(time);
        if(this.height==1 && this.width==1) {
        	this.dataList.add(data.getInitValue());
        }else{
            for (int h=0;h<this.height;h++) {
                for (int w=0;w<this.width;w++) {
                    this.dataList.add(data.getMatrix().get(h, w));
                }
            }
        }

    }

    public Block getBlock() {
    	return this.block;
    }
}
