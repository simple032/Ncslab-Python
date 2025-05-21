package com.ncslab.block.io;

import java.util.Vector;

import com.ncslab.block.Block;
import com.ncslab.block.data.Data;
import com.ncslab.block.data.DataType;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.line.Line;
import com.ncslab.util.TemplateManager;
import lombok.Getter;
import lombok.Setter;
import org.apache.velocity.VelocityContext;

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

    private boolean isCodeGenerated=false;

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

    public void setIsCodeGenerated(boolean isCodeGenerated) {      this.isCodeGenerated=isCodeGenerated;}

    public void setIsDimScaned(boolean isDimScaned) {        this.isDimScaned=isDimScaned;    }

    public String getDataStructureInitCodeC() {
		OutputSignal signal=getOutputSignalC();

        VelocityContext context = new VelocityContext();
        context.put("realDataType", DataType.REAL);
        context.put("blockId", block.getBlockId());
        context.put("blockName", block.getBlockName());
        context.put("blockPath", block.getBlockPath());
        context.put("signal", signal);
        context.put("number", getNumber());
        context.put("name", getName());

        return TemplateManager.renderTemplate("c/io/OutputPort/init.vm", context);
	}

    public void setData(Data data){
        outputSignalC.setData(data);
    }

}
