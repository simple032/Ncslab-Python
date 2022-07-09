package circuit;

import java.util.Vector;

import circuit.block.BlockMode;
import circuit.block.CircuitBlock;
import circuit.line.CircuitLine;
import circuit.block.io.CircuitNode;
import circuit.block.io.CircuitPort;
import ncslablink.NCSLabModel;

public class CircuitModel {
	private NCSLabModel model;
	private Vector<CircuitBlock> blockList=new Vector<CircuitBlock>();
	private Vector<CircuitLine> lineList=new Vector<CircuitLine>();
	private Vector<CircuitNode> nodeList=new Vector<CircuitNode>();
	CircuitModel(NCSLabModel model,Vector<CircuitBlock> blockList,Vector<CircuitLine> lineList){
		this.model=model;
		this.blockList=blockList;
		this.lineList=lineList;
		createCircuitNodes();
		
		getTree();
	}
	
	private Vector<CircuitNode> nodeListNew=new Vector<CircuitNode>();
	
	private void getTree() {
		
		for(CircuitNode startNode:nodeList) {
			searchTree(startNode);
		}
		//CircuitNode startNode=nodeList.get(1);
		//searchTree(startNode);
	}
	
	private void showNewNodes() {
		for(CircuitNode node:nodeListNew) {
			System.out.print(node.getNodeId()+":");
		}
		System.out.println();
		
		for(CircuitBlock block:blockList) {
			System.out.print(block.getBlockName()+":"+block.getBlockMode()+"\t");
		}
		System.out.println();
	}
	
	private boolean isNewNodeIncluded(CircuitNode circuitNodeNew) {
		for(CircuitNode circuitNode:nodeListNew) {
			if(circuitNodeNew==circuitNode) {
				return true;
			}
		}
		return false;
	}
	
	private void searchTree(CircuitNode node) {
		nodeListNew.add(node);
		//showNewNodes();
		if(nodeListNew.size()==nodeList.size()) {
			showNewNodes();
		}
		else {
			Vector<CircuitPort> circuitPortList=node.getCircuitPortList();
			for(CircuitPort circuitPort:circuitPortList) {
				CircuitBlock block=circuitPort.getBlock();
				if(block.isBranchPossible()) {
					
					CircuitPort otherPort=block.getAnotherCircuitPort(circuitPort);
					CircuitNode newNode=otherPort.getCircuitNode();
					if(isNewNodeIncluded(newNode)==false) {
						block.setBlockMode(BlockMode.Branch);
						searchTree(newNode);
						block.setBlockMode(null);
					}
				}
			}
		}
		nodeListNew.remove(node);
	}
	
	private void createCircuitNodes() {
		int nodeId=1;
		for(CircuitLine line:lineList) {
			boolean found;
			CircuitPort fromPort=line.getFromPort();
			CircuitPort toPort=line.getToPort();
			found=false;
			for(CircuitNode node:nodeList) {
				if(node.isCircuitPortIncluded(fromPort)) {
					found=true;
					node.addCircuitPort(toPort);
				}
				else
				if(node.isCircuitPortIncluded(toPort)) {
					found=true;
					node.addCircuitPort(fromPort);
				}
			}
			
			
			if(found==false) {
				CircuitNode newNode=new CircuitNode(nodeId++);
				newNode.addCircuitPort(fromPort);
				newNode.addCircuitPort(toPort);
				nodeList.add(newNode);
			}
		}
	}
	
	public void setupModel() {
		System.out.println("Setup Circuit Model");
	}
	
	public static CircuitModel CreateCircuitModel(NCSLabModel model,Vector<CircuitBlock> blockList,Vector<CircuitLine> lineList) {
		return new CircuitModel(model,blockList,lineList);
	}
}  
