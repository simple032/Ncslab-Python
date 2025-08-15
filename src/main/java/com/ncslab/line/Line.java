package com.ncslab.line;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import org.json.JSONObject;

import com.ncslab.block.Block;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;

public class Line {

	private int width=1;

	@Getter
    private InputPort linkedInputPort=null;
	@Setter
    @Getter
    private OutputPort linkedOutputPort=null;

	@Setter
    @Getter
    private int lineId=0;

	Line(JSONObject lineJSON,List<Block> blockList){
		String fromBlockName=lineJSON.getString("fromBlockName");
		String toBlockName=lineJSON.getString("toBlockName");
        String fromBlockUUID=lineJSON.optString("fromBlockUUID", "null");
        String toBlockUUID=lineJSON.optString("toBlockUUID", "null");

		//Ѱ��Line���˵�Block
		Block fromBlock=null;
		Block toBlock=null;
		for(Block block:blockList) {
            if(fromBlockUUID.equals("null")||toBlockUUID.equals("null")) {
                if(block.getBlockName().equals(fromBlockName)) {
                    fromBlock=block;
                }
                if(block.getBlockName().equals(toBlockName)) {
                    toBlock=block;
                }
            }else{
                if(fromBlockUUID.equals(block.getBlockUUID())){
                    fromBlock=block;
                }
                if(block.getBlockUUID().equals(toBlockUUID)){
                    toBlock=block;
                }
            }

		}

		if(fromBlock==null||toBlock==null) {
			return;
		}

		//Ѱ��from�˵������
		int fromPortNo=lineJSON.getInt("fromPortNo");
		OutputPort fromPort=null;

		List<OutputPort> outputPortList=fromBlock.getOutputPortList();
		//modified by zhou 20240520
		if(fromBlock.getBlockType().equals("Out")) {
			fromPort=fromBlock.getOutputPortList().get(0);
		}
		else {
			for(OutputPort outputPort:outputPortList) {
				if(outputPort.getNumber()==fromPortNo) {
					fromPort=outputPort;
				}
			}
		}

//		for(OutputPort outputPort:outputPortList) {
//			if(outputPort.getNumber()==fromPortNo) {
//				fromPort=outputPort;
//			}
//		}

		if(fromPort==null) {
			return;
		}

		this.linkedOutputPort=fromPort;
		fromPort.addLinkedLine(this);

		//Ѱ��to�������
		int toPortNo=lineJSON.getInt("toPortNo");
		InputPort toPort=null;

		List<InputPort> inputPortList=toBlock.getInputPortList();
		//modified by zhou 20240520
		if(toBlock.getBlockType().equals("In")) {
			toPort=toBlock.getInputPortList().get(0);
		}
		else {
			for(InputPort inputPort:inputPortList) {
//				System.out.println(" inputPort getNumber in Line.java is :"+inputPort.getNumber());
				if(inputPort.getNumber()==toPortNo) {
					toPort=inputPort;
				}
			}
		}

		if(toPort==null) {
			return;
		}

		//toPort.setWidth(fromPort.getWidth());

		this.linkedInputPort=toPort;
		toPort.setLinkedLine(this);

	}

    public static Line createLine(JSONObject lineJSON,NCSLabModel model) {
		List<Block> blockList=model.getBlockList();

		Line line=new Line(lineJSON,blockList);

		return line;
	}

	public static Line createLine(JSONObject lineJSON,List<Block> blockList) {
		Line line=new Line(lineJSON,blockList);

		return line;
	}


}
