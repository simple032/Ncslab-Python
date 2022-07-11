package circuit;

import java.util.Vector;

import circuit.block.BlockMode;
import circuit.block.CircuitBlock;
import circuit.line.CircuitLine;
import circuit.block.io.CircuitNode;
import circuit.block.io.CircuitPort;
import ncslablink.NCSLabModel;

import ncslablink.ModelException;

public class CircuitModel {
	private NCSLabModel model;
	private Vector<CircuitBlock> blockList=new Vector<CircuitBlock>();
	private Vector<CircuitLine> lineList=new Vector<CircuitLine>();
	private Vector<CircuitNode> nodeList=new Vector<CircuitNode>();
	
	private Vector<BlockMode> blockModeList=new Vector<BlockMode>();
	
	private boolean isTreeFound=false;
	
	CircuitModel(NCSLabModel model,Vector<CircuitBlock> blockList,Vector<CircuitLine> lineList) throws ModelException{
		this.model=model;
		this.blockList=blockList;
		this.lineList=lineList;
		createCircuitNodes();
		
		getTree();
		
		if(isTreeFound==false) {
			throw(new ModelException("The circuit part can not be resolved."));
		}
		showBranches();
	}
	
	private Vector<CircuitNode> nodeListNew=new Vector<CircuitNode>();
	
	private void getTree() {
		
		//开始从各个顶点搜索
		for(CircuitNode startNode:nodeList) {
			nodeListNew.add(startNode);
			searchTree(startNode);
			nodeListNew.remove(startNode);
		}
		
		for(int i=0;i<blockModeList.size();i++) {
			blockList.get(i).setBlockMode(blockModeList.get(i));
		}
		
		/*
		CircuitNode startNode=nodeList.get(0);
		nodeListNew.add(startNode);
		searchTree(startNode);
		nodeListNew.remove(startNode);*/
	}
	
	private void showBranches() {
		for(CircuitBlock block:blockList) {
			System.out.print(block.getBlockName()+":"+block.getBlockMode()+"\t");
		}
		System.out.println();
	}
	
	private boolean isValidTree() {
		
		for(CircuitBlock block:blockList) {
			if(block.isBranchOnly()&&block.getBlockMode()!=BlockMode.Branch) {
				return false;
			}
		}
		
		return true;
	}
	
	private void setupLinks() {
		for(CircuitBlock block:blockList) {
			if(block.getBlockMode()==null) {
				block.setBlockMode(BlockMode.Link);
			}
			blockModeList.add(block.getBlockMode());
		}
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
		if(isTreeFound) {
			return;
		}
		
		Vector<CircuitPort> circuitPortList = node.getCircuitPortList();

		// 寻找只能做branch的block
		Vector<CircuitPort> branchOnlyList = new Vector<CircuitPort>();
		Vector<CircuitPort> anythingList = new Vector<CircuitPort>();
		for (CircuitPort circuitPort : circuitPortList) {
			CircuitBlock block = circuitPort.getBlock();
			// 是否没有折回
			if (isNewNodeIncluded(block.getAnotherCircuitPort(circuitPort).getCircuitNode()) == false) {
				// 是否时只能做树枝
				if (block.isBranchOnly()) {
					branchOnlyList.add(circuitPort);
				} else if (block.isAnything()) {
					anythingList.add(circuitPort);
				}
			}
		}
		
		//如果增加必须的树枝,超过了节点总数,说明这样增加是不行的,必须返回
		if(branchOnlyList.size()+nodeListNew.size()>nodeList.size()) {
			return;
		}
		
		// 把必须做树枝的设为树枝,加入路径
		for (CircuitPort circuitPort : branchOnlyList) {
			CircuitBlock block = circuitPort.getBlock();
			//设为树枝
			block.setBlockMode(BlockMode.Branch);
			
			//如果增加的节点引来重复,说明这个图有问题,无解,需要给出异常
			if (isNewNodeIncluded(block.getAnotherCircuitPort(circuitPort).getCircuitNode()) == true) {
				
			}
			//节点增加进去
			nodeListNew.add(block.getAnotherCircuitPort(circuitPort).getCircuitNode());
			//showNewNodes();
		}
		
		int n=(1<<anythingList.size());
		//System.out.println(anythingList.size()+":"+n);
		
		for(int i=0;i<n;i++) {
			int j=0;
			for (CircuitPort circuitPort : anythingList) {
				int on=(i>>j)%2;
				j++;
				if(on==1) {
					circuitPort.getBlock().setBlockMode(BlockMode.Branch);
					nodeListNew.add(circuitPort.getBlock().getAnotherCircuitPort(circuitPort).getCircuitNode());
					//showNewNodes();
				}
			}
			
			//如果节点数没有超出,就递归搜索
			if(nodeListNew.size()<nodeList.size()) {
				for (CircuitPort circuitPort : branchOnlyList) {
					searchTree(circuitPort.getBlock().getAnotherCircuitPort(circuitPort).getCircuitNode());
				}
				for (CircuitPort circuitPort : anythingList) {
					if(circuitPort.getBlock().getBlockMode()==BlockMode.Branch) {
						searchTree(circuitPort.getBlock().getAnotherCircuitPort(circuitPort).getCircuitNode());
					}
				}
			}
			else 
			if(nodeListNew.size()==nodeList.size()) {
				if(isValidTree()) {
					setupLinks();
					//showNewNodes();
					isTreeFound=true;
				}
			}
			
			j=0;
			for (CircuitPort circuitPort : anythingList) {
				int on=(i>>j)%2;
				j++;
				if(on==1) {
					circuitPort.getBlock().setBlockMode(null);
					nodeListNew.remove(circuitPort.getBlock().getAnotherCircuitPort(circuitPort).getCircuitNode());
				}
			}
		}
		
		
		//搜索完毕,返回
		for (CircuitPort circuitPort : branchOnlyList) {
			CircuitBlock block = circuitPort.getBlock();
			block.setBlockMode(null);
			nodeListNew.remove(block.getAnotherCircuitPort(circuitPort).getCircuitNode());
		}
	}

	
	/*
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
	}*/
	
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
	
	public static CircuitModel CreateCircuitModel(NCSLabModel model,Vector<CircuitBlock> blockList,Vector<CircuitLine> lineList) throws ModelException{
		return new CircuitModel(model,blockList,lineList);
	}
}  
