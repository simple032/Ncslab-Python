package line;

import java.util.Vector;

import org.json.JSONObject;

import block.Block;
import ncslablink.NCSLabModel;
import block.io.InputPort;
import block.io.OutputPort;

public class Line {
	
	private int width=1;
	
	private InputPort linkedInputPort=null;
	private OutputPort linkedOutputPort=null;
	
	private int lineId=0;
	
	Line(JSONObject lineJSON,Vector<Block> blockList){
		String fromBlockName=lineJSON.getString("fromBlockName");
		String toBlockName=lineJSON.getString("toBlockName");
		
		//—∞’“Line¡Ω∂ÀµƒBlock
		Block fromBlock=null;
		Block toBlock=null;
		for(Block block:blockList) {
			if(block.getBlockName().equals(fromBlockName)) {
				fromBlock=block;
			}
			if(block.getBlockName().equals(toBlockName)) {
				toBlock=block;
			}
		}
		
		if(fromBlock==null||toBlock==null) {
			return;
		}
		
		//—∞’“from∂Àµƒ ‰≥ˆ∂À
		int fromPortNo=lineJSON.getInt("fromPortNo");
		OutputPort fromPort=null;
		
		Vector<OutputPort> outputPortList=fromBlock.getOutputPortList();
		for(OutputPort outputPort:outputPortList) {
			if(outputPort.getNumber()==fromPortNo) {
				fromPort=outputPort;
			}
		}
		
		if(fromPort==null) {
			return;
		}
		
		this.linkedOutputPort=fromPort;
		fromPort.addLinkedLine(this);
		
		//—∞’“toµƒ ‰»Î∂À
		int toPortNo=lineJSON.getInt("toPortNo");
		InputPort toPort=null;
		
		Vector<InputPort> inputPortList=toBlock.getInputPortList();
		for(InputPort inputPort:inputPortList) {
			if(inputPort.getNumber()==toPortNo) {
				toPort=inputPort;
			}
		}
		
		if(toPort==null) {
			return;
		}
		
		this.linkedInputPort=toPort;
		toPort.setLinkedLine(this);
		
	}
	
	public InputPort getLinkedInputPort() {
		return this.linkedInputPort;
	}
	
	public OutputPort getLinkedOutputPort() {
		return this.linkedOutputPort;
	}
	
	public void setLineId(int lineId) {
		this.lineId=lineId;
	}
	
	public int getLineId() {
		return this.lineId;
	}
	
	public static Line createLine(JSONObject lineJSON,NCSLabModel model) {
		Vector<Block> blockList=model.getBlockList();
		
		Line line=new Line(lineJSON,blockList);
		
		return line;
	}
	

}
