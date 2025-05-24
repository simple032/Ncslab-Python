package com.ncslab.block.io.terminal;

import com.ncslab.block.Block;
import com.ncslab.block.data.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Vector;

public class ScopeStruct extends Terminal {
	@Setter
    @Getter
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
		this.name="Block"+block.getBlockId()+"_Scope_"+localName;
		this.localName=localName;
		this.timeList=new Vector<>();
		this.dataList=new Vector<>();
	}

	public String getDefineCodeC() {
		String code="";

		//code+="REAL "+this.name+"_Buffer["+this.maxDataLength*this.width*this.height+"];\n";
		//code+="REAL "+this.name+"_Time["+this.maxDataLength+"];\n";
		code+="SCOPE "+this.name+"={(char *)\""+this.localName+"\","
            +"(char *)\""+this.block.getBlockPath()+"\","
            +"(char *)\""+this.block.getBlockUUID()+"\","
            +maxDataLength+","+width+","+height+"};\n";//",0,"+this.name+"_Buffer"+","+this.name+"_Time"+"};\n";
		code+=this.getTerminalDefineCode("Scope");

		return code;
	}

	public void setDimension(int width,int height) {
		this.width=width;
		this.height=height;
	}

    public void addTimeSeries(double time,Data data) {
    	this.timeList.add(time);
        if(height==1 && width==1) {
        	this.dataList.add(data.getInitValue());
            return;
        }
        for (int h=0;h<this.height;h++) {
            for (int w=0;w<this.width;w++) {
                this.dataList.add(data.getMatrix().get(h, w));
            }
        }

    }

    public Block getBlock() {
    	return this.block;
    }
}
