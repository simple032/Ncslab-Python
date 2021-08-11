package block.io;

import block.Block;
import line.Line;

public class InputPort {
	private int width=1;
	
	private OutputPort linkedOutputPort;
	
	private Block block;
	
	private int number;
	
	private String name;
	
	private Line linkedLine=null;
	
	public InputPort(Block block,int number){
		this.block=block;
		
		this.number=number;
		
		this.linkedOutputPort=null;
		
		this.name="in"+number;
	}
	
	public int getNumber() {
		return number;
	}
	
	public void setLinkedLine(Line linkedLine) {
		this.linkedLine=linkedLine;
	}
	
	public Line getLinkedLine() {
		return this.linkedLine;
	}
	
	public Block getBLock() {
		return this.block;
	}
	
	public String getName() {
		return this.name;
	}
	
	public int getWidth() {
		return this.width;
	}
}
