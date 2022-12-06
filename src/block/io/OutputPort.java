package block.io;

import java.util.Vector;

import block.Block;

import line.Line;

public class OutputPort {
	
	private Block block;
	
	private int width=1;
	private int height=1;
	
	private int number;
	
	private Vector<Line> linkedLineList=new Vector<Line>();
	
	private boolean isFeedThrough=false;
	
	private boolean isDimThrough=true;
	
	private boolean isCodeGenerated=false;
	
	private boolean isDimScaned=false;
	
	private block.io.OutputSignal outputSignalC=null;
	
	private String name;
	
	public OutputPort(Block block,int number){
		this.block=block;
		this.number=number;
		//区分监控组态中不同模块中的输出信号,replace方法用于处理部分模块的非连续字符串命名问题
		this.name=block.getBlockName().replace(" ", "_").replace("(", "_").replace(")", "")+"_out"+number;
	}
	
	public OutputPort(Block block,int number,boolean isFeedThrough){
		this.block=block;
		this.number=number;
		//区分监控组态中不同模块中的输出信号,replace方法用于处理部分模块的非连续字符串命名问题
		this.name=block.getBlockName().replace(" ", "_").replace("(", "_").replace(")", "")+"_out"+number;
		this.isFeedThrough=isFeedThrough;
	}
	
	public OutputPort(Block block,String name,int number,boolean isFeedThrough){
		this.block=block;
		this.number=number;
		//区分监控组态中不同模块中的输出信号,replace方法用于处理部分模块的非连续字符串命名问题
		this.name=block.getBlockName().replace(" ", "_").replace("(", "_").replace(")", "")+"_"+name;
		this.isFeedThrough=isFeedThrough;
	}
	
	public block.io.OutputSignal getOutputSignalC(){
		return this.outputSignalC;
	}
	
	public void setOutputSignalC(block.io.OutputSignal outputSignalC){
		this.outputSignalC=outputSignalC;
	}
	
	public int getNumber() {
		return number;
	}
	
	public void addLinkedLine(Line linkedLine) {
		this.linkedLineList.add(linkedLine);
	}
	
	public Vector<Line> getLinkedLineList() {
		return this.linkedLineList;
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
	
	public void setIsDimScaned(boolean isDimScaned) {
		this.isDimScaned=isDimScaned;
	}
	
	
	public void setIsCodeGenerated(boolean isCodeGenerated) {
		this.isCodeGenerated=isCodeGenerated;
	}
	
	public String getName() {
		return this.name;
	}
	
	public void setName(String name) {
		this.name=name;
	}
	
	public int getWidth() {
		return this.width;
	}

	public void setWidth(int width) {
		// TODO Auto-generated method stub
		this.width=width;
		//outputSignalC.setWidth(width);
	}
	
	public int getHeight() {
		return this.height;
	}

	public void setHeight(int height) {
		// TODO Auto-generated method stub
		this.height=height;
		//outputSignalC.setHeight(height);
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
	
}
