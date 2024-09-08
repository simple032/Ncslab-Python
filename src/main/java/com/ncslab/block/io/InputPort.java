package com.ncslab.block.io;

import com.ncslab.block.Block;
import com.ncslab.block.data.DataType;
import com.ncslab.line.Line;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import lombok.Getter;
import lombok.Setter;

public class InputPort {

	private OutputPort linkedOutputPort;

	private Block block;

	@Getter
    private int number;

	@Getter
    private String name;

	@Getter
    @Setter
    private Line linkedLine=null;

	public InputPort(Block block,int number){
		this.block=block;

		this.number=number;

		this.linkedOutputPort=null;
		//区分监控组态中不同模块中的输入,replace方法用于处理部分模块的非连续字符串命名问题
		if(block.getBlockType().equals("Scope")) {
			this.name=block.getBlockName();
		}else {
		this.name=block.getBlockName().replace(" ", "_").replace("-", "_").replace("(", "_").replace(")", "")+"_in"+number;
		}
	}

    public Block getBLock() {
		return this.block;
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
			code+="signal"+block.getBlockId()+"_In"+this.getNumber()+".path=(char *)\""+this.getBLock().getBlockPath()+"/"+block.getBlockName()+"/"+this.getName()+"\";\n";
			code+="signal"+block.getBlockId()+"_In"+this.getNumber()+".type=SINGLE;\n";
			break;
		case MATRIX:
			code+="signal"+block.getBlockId()+"_In"+this.getNumber()+".vp=&"+signal.getName()+";\n";
			code+="signal"+block.getBlockId()+"_In"+this.getNumber()+".width="+signal.getWidth()+";\n";
			code+="signal"+block.getBlockId()+"_In"+this.getNumber()+".height="+signal.getHeight()+";\n";
			code+="signal"+block.getBlockId()+"_In"+this.getNumber()+".name=(char *)\""+this.getName()+"\";\n";
//			code+="signal"+block.getBlockId()+"_In"+this.getNumber()+".path=(char *)\""+this.getBLock().getModel().getModelRealName()+"/"+block.getBlockName()+"/"+this.getName()+"\";\n";
			code+="signal"+block.getBlockId()+"_In"+this.getNumber()+".path=(char *)\""+this.getBLock().getBlockPath()+"/"+block.getBlockName()+"/"+this.getName()+"\";\n";
			code+="signal"+block.getBlockId()+"_In"+this.getNumber()+".type=MATRIX;\n";
			break;
		}

		return code;
	}
}
