package circuit;

import java.util.Vector;

import circuit.block.CircuitBlock;
import circuit.line.CircuitLine;
import ncslablink.NCSLabModel;

public class CircuitModel {
	private NCSLabModel model;
	private Vector<CircuitBlock> blockList=new Vector<CircuitBlock>();
	private Vector<CircuitLine> lineList=new Vector<CircuitLine>();
	CircuitModel(NCSLabModel model,Vector<CircuitBlock> blockList,Vector<CircuitLine> lineList){
		this.model=model;
		this.blockList=blockList;
		this.lineList=lineList;
	}
	
	public void setupModel() {
		System.out.println("Setup Circuit Model");
	}
	
	public static CircuitModel CreateCircuitModel(NCSLabModel model,Vector<CircuitBlock> blockList,Vector<CircuitLine> lineList) {
		return new CircuitModel(model,blockList,lineList);
	}
}  
