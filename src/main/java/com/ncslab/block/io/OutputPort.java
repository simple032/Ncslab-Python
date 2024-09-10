package com.ncslab.block.io;

import java.util.Vector;

import com.ncslab.block.Block;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.line.Line;
import lombok.Getter;
import lombok.Setter;

public class OutputPort {

	private Block block;

    // TODO Auto-generated method stub
    //outputSignalC.setWidth(width);
    @Setter
    @Getter
    private int width=1;
    // TODO Auto-generated method stub
    @Setter
    @Getter
    private int height=1;

	@Getter
    private int number;

	@Getter
    private Vector<Line> linkedLineList=new Vector<Line>();

	private boolean isFeedThrough=false;

	private boolean isDimThrough=true;

	@Setter
    private boolean isCodeGenerated=false;

	@Setter
    private boolean isDimScaned=false;

	@Setter
    @Getter
    private OutputSignal outputSignalC=null;

	@Getter
    @Setter
    private String name;

	public OutputPort(Block block,int number){
		this.block=block;
		this.number=number;
		//区分监控组态中不同模块中的输出信号,replace方法用于处理部分模块的非连续字符串命名问题
		this.name=block.getBlockName().replace(" ", "_").replace("-", "_").replace("(", "_").replace(")", "")+"_out"+number;
	}

	public OutputPort(Block block,int number,boolean isFeedThrough){
		this.block=block;
		this.number=number;
		//区分监控组态中不同模块中的输出信号,replace方法用于处理部分模块的非连续字符串命名问题
		this.name=block.getBlockName().replace(" ", "_").replace("-", "_").replace("(", "_").replace(")", "")+"_out"+number;
		this.isFeedThrough=isFeedThrough;
	}

	public OutputPort(Block block,String name,int number,boolean isFeedThrough){
		this.block=block;
		this.number=number;
		//区分监控组态中不同模块中的输出信号,replace方法用于处理部分模块的非连续字符串命名问题
		this.name=block.getBlockName().replace(" ", "_").replace("-", "_").replace("(", "_").replace(")", "")+"_"+name;
		this.isFeedThrough=isFeedThrough;
	}

    public void addLinkedLine(Line linkedLine) {
		this.linkedLineList.add(linkedLine);
	}

    public Block getBLock() {
		return this.block;
	}

	public boolean getFeedThrough() {
		return this.isFeedThrough;
	}

	public void setFeedThrough(boolean feedThrough) {
		this.isFeedThrough=feedThrough;
	}

	public boolean getDimThrough() {
		return this.isDimThrough;
	}

	public void setDimThrough(boolean isDimThrough) {
		this.isDimThrough=isDimThrough;
	}

	public boolean getIsCodeGenerated() {
		return this.isCodeGenerated;
	}

	public boolean getIsDimScaned() {
		return this.isDimScaned;
	}

    public String getDataStructureInitCodeC() {
		String code="";

		OutputSignal signal=getOutputSignalC();

		switch(signal.getDataType()) {
		case REAL:
			code+="signal"+block.getBlockId()+"_Out"+this.getNumber()+".vp=&"+signal.getName()+";\n";
			code+="signal"+block.getBlockId()+"_Out"+this.getNumber()+".width="+signal.getWidth()+";\n";
			code+="signal"+block.getBlockId()+"_Out"+this.getNumber()+".height="+signal.getHeight()+";\n";
			code+="signal"+block.getBlockId()+"_Out"+this.getNumber()+".name=(char *)\""+this.getName()+"\";\n";
			//code+="signal"+block.getBlockId()+"_Out"+this.getNumber()+".path=(char *)\""+this.getBLock().getModel().getModelRealName()+"/"+block.getBlockName()+"/"+this.getName()+"\";\n";
			code+="signal"+block.getBlockId()+"_Out"+this.getNumber()+".path=(char *)\""+this.getBLock().getBlockPath()+"/"+block.getBlockName()+"/"+this.getName()+"\";\n";
			code+="signal"+block.getBlockId()+"_Out"+this.getNumber()+".type=SINGLE;\n";
			break;
		case MATRIX:
			code+="signal"+block.getBlockId()+"_Out"+this.getNumber()+".vp=&"+signal.getName()+";\n";
			code+="signal"+block.getBlockId()+"_Out"+this.getNumber()+".width="+signal.getWidth()+";\n";
			code+="signal"+block.getBlockId()+"_Out"+this.getNumber()+".height="+signal.getHeight()+";\n";
			code+="signal"+block.getBlockId()+"_Out"+this.getNumber()+".name=(char *)\""+this.getName()+"\";\n";
			//code+="signal"+block.getBlockId()+"_Out"+this.getNumber()+".path=(char *)\""+this.getBLock().getModel().getModelRealName()+"/"+block.getBlockName()+"/"+this.getName()+"\";\n";
			code+="signal"+block.getBlockId()+"_Out"+this.getNumber()+".path=(char *)\""+this.getBLock().getBlockPath()+"/"+block.getBlockName()+"/"+this.getName()+"\";\n";
			code+="signal"+block.getBlockId()+"_Out"+this.getNumber()+".type=MATRIX;\n";
			break;
		}

		return code;
	}

    public void setIsCodeGenerated(boolean isCodeGenerated) {
        this.isCodeGenerated=isCodeGenerated;
    }

    public void setIsDimScaned(boolean isDimScaned) {
        this.isDimScaned=isDimScaned;
    }
}
