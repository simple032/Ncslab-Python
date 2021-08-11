package block.io;

import java.util.Vector;

import block.Block;

import line.Line;

public class OutputPort {
	private int width=1;
	
	private Block block;
	
	private int number;
	
	private Vector<Line> linkedLineList=new Vector<Line>();
	
	private boolean isFeedThrough=false;
	
	private boolean isCodeGenerated=false;
	
	private code.c.OutputSignal outputSignalC=null;
	
	private String name;
	
	public OutputPort(Block block,int number){
		this.block=block;
		this.number=number;
		this.name="out"+number;
	}
	
	public OutputPort(Block block,int number,boolean isFeedThrough){
		this.block=block;
		this.number=number;
		this.name="out"+number;
		this.isFeedThrough=isFeedThrough;
	}
	
	public code.c.OutputSignal getOutputSignalC(){
		return this.outputSignalC;
	}
	
	public void setOutputSignalC(code.c.OutputSignal outputSignalC){
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
	
	public boolean getIsCodeGenerated() {
		return this.isCodeGenerated;
	}
	
	public void setIsCodeGenerated(boolean isCodeGenerated) {
		this.isCodeGenerated=isCodeGenerated;
	}
	
	public String getName() {
		return this.name;
	}
	
	public int getWidth() {
		return this.width;
	}
}
