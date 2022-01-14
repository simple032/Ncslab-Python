package block.io;

import block.Block;
import block.data.DataType;
import line.Line;

public class InputPort {
	
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
		OutputSignal signal=getLinkedLine().getLinkedOutputPort().getOutputSignalC();
		return signal.getWidth();
	}
	
	public int getHeight() {
		OutputSignal signal=getLinkedLine().getLinkedOutputPort().getOutputSignalC();
		return signal.getHeight();
	}
	
	public boolean isVector() {
		OutputSignal signal=getLinkedLine().getLinkedOutputPort().getOutputSignalC();
		if(signal.getDataType()==DataType.MATRIX&&signal.getWidth()==1||signal.getHeight()==1) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public boolean isReal() {
		OutputSignal signal=getLinkedLine().getLinkedOutputPort().getOutputSignalC();
		if(signal.getDataType()==DataType.REAL) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public int getVectorSize() {
		OutputSignal signal=getLinkedLine().getLinkedOutputPort().getOutputSignalC();
		if(signal.getWidth()>1) {
			return signal.getWidth();
		}
		if(signal.getHeight()>1) {
			return signal.getHeight();
		}
		return 1;
	}
	
	public String getDataStructureInitCodeC() {
		String code="";
		
		OutputSignal signal=getLinkedLine().getLinkedOutputPort().getOutputSignalC();
		
		switch(signal.getDataType()) {
		case REAL:
			code+="signal"+block.getBlockId()+"_In"+this.getNumber()+".vp=&"+signal.getName()+";\n";
			code+="signal"+block.getBlockId()+"_In"+this.getNumber()+".width="+signal.getWidth()+";\n";
			code+="signal"+block.getBlockId()+"_In"+this.getNumber()+".height="+signal.getHeight()+";\n";
			code+="signal"+block.getBlockId()+"_In"+this.getNumber()+".name=(char *)\""+this.getName()+"\";\n";
			code+="signal"+block.getBlockId()+"_In"+this.getNumber()+".path=(char *)\""+this.getBLock().getModel().getModelRealName()+"/"+block.getBlockName()+"/"+this.getName()+"\";\n";
			break;
		case MATRIX:
			code+="signal"+block.getBlockId()+"_In"+this.getNumber()+".vp="+signal.getName()+";\n";
			code+="signal"+block.getBlockId()+"_In"+this.getNumber()+".width="+signal.getWidth()+";\n";
			code+="signal"+block.getBlockId()+"_In"+this.getNumber()+".height="+signal.getHeight()+";\n";
			code+="signal"+block.getBlockId()+"_In"+this.getNumber()+".name=(char *)\""+this.getName()+"\";\n";
			code+="signal"+block.getBlockId()+"_In"+this.getNumber()+".path=(char *)\""+this.getBLock().getModel().getModelRealName()+"/"+block.getBlockName()+"/"+this.getName()+"\";\n";
			break;
		}
		
		return code;
	}
}
