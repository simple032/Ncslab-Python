package circuit.loop;

import java.util.Vector;

import block.Block;
import block.io.InputPort;
import block.math.Add;
import line.Line;

import circuit.CircuitModel;

public class LoopSolver {
	
	private CircuitModel circuitModel;
	
	private Block outputBlock;
	
	private Vector<Vector<LinearBlockElement>> forwardList=new Vector<Vector<LinearBlockElement>>();
	
	private Vector<Vector<LinearBlockElement>> loopList=new Vector<Vector<LinearBlockElement>>();
	
	private Vector<Block> modelBlockList;
	private Vector<Line> modelLineist;
	
	private Vector<LinearBlockElement> loopPointList=new Vector<LinearBlockElement>();
	
	private Vector<Vector<LinearBlockElement>> loopForwardList=new Vector<Vector<LinearBlockElement>>();
	
	private Vector<ForwardGroup> forwardGroupList=new Vector<ForwardGroup>();
	
	private Vector<TargetGroup> targetGroupList=new Vector<TargetGroup>();
	
	public LoopSolver(Block outputBlock,CircuitModel circuitModel) {
		this.outputBlock=outputBlock;
		this.circuitModel=circuitModel;
		modelBlockList=circuitModel.getModelBlocks();
		modelLineist=circuitModel.getModelLines();
	}
	
	public void addForward(Vector<LinearBlockElement> blockPath) {
		Vector<LinearBlockElement> forward=new Vector<LinearBlockElement>();
		
		for(LinearBlockElement element:blockPath) {
			LinearBlockElement newElement=new LinearBlockElement(element.getBlock(),element.getSign());
			forward.add(newElement);
		}
		
		forwardList.add(forward);
	}
	
	public void addLoop(Vector<LinearBlockElement> blockPath,Block block) {
		Vector<LinearBlockElement> loop=new Vector<LinearBlockElement>();
		boolean isLoopStarted=false;
		for(LinearBlockElement element:blockPath) {
			if(block==element.getBlock()) {
				isLoopStarted=true;
				loopPointList.add(element);
			}
			if(isLoopStarted) {
				LinearBlockElement newElement=new LinearBlockElement(element.getBlock(),element.getSign());
				loop.add(newElement);
			}
			
		}
		
		loopList.add(loop);
	}
	
	public boolean isLoopIncluded() {
		if(loopList.size()==0) {
			return false;
		}
		else {
			return true;
		}
	}
	
	public void showLoopSolver() {
		System.out.println("+++++++++++++++++++++++++++");
		System.out.println(outputBlock.getBlockName()+"...");
		System.out.println("Forward:");
		
		
		for(TargetGroup targetGroup:targetGroupList) {
			System.out.println("/****"+targetGroup.getTargetElement().getBlock().getBlockName()+"***/");
			int i=0;
			for(ForwardGroup forwardGroup:targetGroup.getForwardGroupList()) {
				Vector<ForwardLine> forwardLineList=forwardGroup.getForwardLineList();
				System.out.println("Group "+i);
				i++;
				for(ForwardLine forwardLine:forwardLineList) {
					int j=0;
					System.out.print("Line "+j+"\t");
					j++;
					for(LinearBlockElement element:forwardLine.getForward()) {
						System.out.print(element.getBlock().getBlockName()+":"+(element.getSign()?"+":"-")+'\t');
					}
					System.out.println();
					System.out.println("Contact Loop:");
					for(Vector<LinearBlockElement> contactLoopList:forwardLine.getContactLoopList()) {
						for(LinearBlockElement element:contactLoopList) {
							System.out.print(element.getBlock().getBlockName()+":"+(element.getSign()?"+":"-")+'\t');
						}
						System.out.println();
					}
					
					System.out.println("Ohter Loop:");
					for(Vector<LinearBlockElement> otherLoopList:forwardLine.getOtherLoopList()) {
						for(LinearBlockElement element:otherLoopList) {
							System.out.print(element.getBlock().getBlockName()+":"+(element.getSign()?"+":"-")+'\t');
						}
						System.out.println();
					}
					
				}
			}
		}
		
		
		System.out.println("Loop:");
		for(Vector<LinearBlockElement> loop:loopList) {
			for(LinearBlockElement element:loop) {
				System.out.print(element.getBlock().getBlockName()+":"+(element.getSign()?"+":"-")+'\t');
			}
			System.out.println();
		}
		System.out.println("+++++++++++++++++++++++++++");
	}
	
	private Vector<LinearBlockElement> blockPath;
	
	private boolean isInBlockPath(Block block) {
		for(LinearBlockElement pathElement:blockPath) {
			if(block==pathElement.getBlock()) {
				return true;
			}
		}
		return false;
	}
	
	private void showBlockPath() {
		for(LinearBlockElement element:blockPath) {
			System.out.print(element.getBlock().getBlockName()+"/"+(element.getSign()?"+":"-")+"\t");
		}
		System.out.println();
	}
	
	private void addLoopForward(Vector<LinearBlockElement> blockPath) {
		Vector<LinearBlockElement> forward=new Vector<LinearBlockElement>();
		
		for(LinearBlockElement element:blockPath) {
			LinearBlockElement newElement=new LinearBlockElement(element.getBlock(),element.getSign());
			forward.add(newElement);
		}
		
		loopForwardList.add(forward);
	}
	
	private void searchBlock(Block block) {
		// 如果在BlockPath中,说明发现环路
		if (isInBlockPath(block)) {
			return;
		}
		
		// 如果时终端模块或者是feedthrough==false的模块,说明发现前向通道
		if(block.getInputPortList().size() == 0 || block.getOutputPortList().get(0).getFeedThrough() == false) {
			// 保存前向通道
			LinearBlockElement element=new LinearBlockElement(block,true);
			blockPath.add(element);
			addLoopForward(blockPath);
			// System.out.println("Forward:");
			//showBlockPath();
			blockPath.remove(element);
			return;
		}
		
		LinearBlockElement element=new LinearBlockElement(block,true);
		blockPath.add(element);
		
		// 如果是Add,说明需要动态调整符号
		if (block.getBlockType().equals("Add")) {
			Vector<InputPort> inputPortList = block.getInputPortList();

			int i = 0;
			for (InputPort input : inputPortList) {
				Add add = (Add) block;
				element.setSign(add.getSign(i));
				i++;
				Block newBlock = input.getLinkedLine().getLinkedOutputPort().getBLock();
				// System.out.println(input.getLinkedLine().getLinkedOutputPort().getBLock().getBlockName());
				searchBlock(newBlock);
			}
		} else {
			Vector<InputPort> inputPortList = block.getInputPortList();
			InputPort input = inputPortList.get(0);
			Block newBlock = input.getLinkedLine().getLinkedOutputPort().getBLock();
			searchBlock(newBlock);
		}
		
		blockPath.remove(element);
	}
	
	// 寻找面向代数环节点的Forward通道
	public void solveLoop() {
		System.out.println("Solver loops for " + outputBlock.getBlockName() + "...");
		for (LinearBlockElement element : loopPointList) {
			System.out.println(element.getBlock().getBlockName());
			blockPath = new Vector<LinearBlockElement>();
			searchBlock(element.getBlock());
		}

		forwardList = loopForwardList;
		
		setupForwardGroupList();
		setupTargetGroupList();
	}
	
	private void setupTargetGroupList() {
		for(LinearBlockElement element:loopPointList) {
			TargetGroup targetGroup=new TargetGroup(element);
			for(ForwardGroup forwardGroup:forwardGroupList) {
				if(forwardGroup.getEndElement().getBlock()==targetGroup.getTargetElement().getBlock()) {
					targetGroup.addForwardGroup(forwardGroup);
				}
			}
			targetGroupList.add(targetGroup);
		}
	}
	
	private void setupForwardGroupList() {
		for(Vector<LinearBlockElement> forward:forwardList) {
			LinearBlockElement startElement=forward.lastElement();
			LinearBlockElement endElement=forward.firstElement();
			
			boolean isFound=false;
			for(ForwardGroup forwardGroup:forwardGroupList) {
				if(forwardGroup.isSameStartEnd(startElement, endElement)) {
					forwardGroup.addForward(forward);
					isFound=true;
				}
			}
			
			if(isFound==false) {
				ForwardGroup forwardGroup=new ForwardGroup(forward);
				forwardGroupList.add(forwardGroup);
			}
			
		}
		
		for(ForwardGroup forwardGroup:forwardGroupList) {
			for(ForwardLine forwardLine:forwardGroup.getForwardLineList()) {
				forwardLine.setupLoops(loopList);
			}
		}
	}
}
