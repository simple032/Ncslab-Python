package block.io;

import java.util.Vector;

import block.Block;

import line.Line;

public class OutputPort {
	private int width=1;
	
	private Block block;
	
	private int number;
	
	private Vector<Line> linkedLineList=new Vector<Line>();
	
	public OutputPort(Block block,int number){
		this.block=block;
		this.number=number;
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
}
