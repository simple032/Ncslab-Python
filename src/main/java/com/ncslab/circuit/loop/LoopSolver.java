package com.ncslab.circuit.loop;

import java.util.ArrayList;
import java.util.List;

import com.ncslab.block.Block;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.math.Add;
import com.ncslab.line.Line;

import com.ncslab.circuit.CircuitModel;

public class LoopSolver {

	private CircuitModel circuitModel;

	private Block outputBlock;

	private List<List<LinearBlockElement>> forwardList = new ArrayList<>();

	private List<List<LinearBlockElement>> loopList = new ArrayList<>();

	private List<Block> modelBlockList;
	private List<Line> modelLineist;

	private List<LinearBlockElement> loopPointList = new ArrayList<>();

	private List<List<LinearBlockElement>> loopForwardList = new ArrayList<>();

	private List<ForwardGroup> forwardGroupList = new ArrayList<>();

	private List<TargetGroup> targetGroupList = new ArrayList<>();

	public LoopSolver(Block outputBlock,CircuitModel circuitModel) {
		this.outputBlock=outputBlock;
		this.circuitModel=circuitModel;
		modelBlockList=circuitModel.getModelBlocks();
		modelLineist=circuitModel.getModelLines();
	}

	public void addForward(List<LinearBlockElement> blockPath) {
		List<LinearBlockElement> forward = new ArrayList<>();

		for(LinearBlockElement element:blockPath) {
			LinearBlockElement newElement=new LinearBlockElement(element.getBlock(),element.getSign());
			forward.add(newElement);
		}

		forwardList.add(forward);
	}

	//将发现的环路加入LoopList,并且找到LoopPoint,加入loopPointList
	public void addLoop(List<LinearBlockElement> blockPath,Block block) {
		List<LinearBlockElement> loop = new ArrayList<>();
		boolean isLoopStarted=false;
		for(LinearBlockElement element:blockPath) {
			if(block==element.getBlock()) {
				isLoopStarted=true;
				//block相关的节点是LoopPoint
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

	public void showLoopSolver() throws CircuitLoopException{
		System.out.println("+++++++++++++++++++++++++++");
		System.out.println(outputBlock.getBlockName()+"...");
		System.out.println("Forward:");


		for(TargetGroup targetGroup:targetGroupList) {
			System.out.println("/****"+targetGroup.getTargetElement().getBlock().getBlockName()+"***/");
			int i=0;
			for(ForwardGroup forwardGroup:targetGroup.getForwardGroupList()) {
				List<ForwardLine> forwardLineList=forwardGroup.getForwardLineList();
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
					for(List<LinearBlockElement> contactLoopList:forwardLine.getContactLoopList()) {
						for(LinearBlockElement element:contactLoopList) {
							System.out.print(element.getBlock().getBlockName()+":"+(element.getSign()?"+":"-")+'\t');
						}
						System.out.println();
					}

					System.out.println("Ohter Loop:");
					for(List<LinearBlockElement> otherLoopList:forwardLine.getOtherLoopList()) {
						for(LinearBlockElement element:otherLoopList) {
							System.out.print(element.getBlock().getBlockName()+":"+(element.getSign()?"+":"-")+'\t');
						}
						System.out.println();
					}

				}
				//System.out.println("Code :"+forwardGroup.getGroupString());
			}
			System.out.println("Code :"+targetGroup.getTargetGroupString());
		}


		System.out.println("Loop:");
		for(List<LinearBlockElement> loop:loopList) {
			for(LinearBlockElement element:loop) {
				System.out.print(element.getBlock().getBlockName()+":"+(element.getSign()?"+":"-")+'\t');
			}
			System.out.println();
		}
		System.out.println("+++++++++++++++++++++++++++");
	}

	private List<LinearBlockElement> blockPath;

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

	private void addLoopForward(List<LinearBlockElement> blockPath) {
		List<LinearBlockElement> forward = new ArrayList<>();

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
			List<InputPort> inputPortList = block.getInputPortList();

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
			List<InputPort> inputPortList = block.getInputPortList();
			InputPort input = inputPortList.get(0);
			Block newBlock = input.getLinkedLine().getLinkedOutputPort().getBLock();
			searchBlock(newBlock);
		}

		blockPath.remove(element);
	}

	// //解开代数环
	public void solveLoop() throws CircuitLoopException{
		System.out.println("Solver loops for " + outputBlock.getBlockName() + "...");
		//寻找面向代数环节点LoopPoint的Forward通道
		for (LinearBlockElement element : loopPointList) {
			//System.out.println(element.getBlock().getBlockName());
			blockPath = new ArrayList<>();
			//根据LoopPoint,重新搜索Forward和Loop节点
			searchBlock(element.getBlock());
		}

		//重新设置Forward的列表,新的列表面向LoopPoint
		forwardList = loopForwardList;

		//设置ForwardGroup,ForwardGroup是起点和终点一样的前向通道集合
		setupForwardGroupList();
		//设置TargetGroup,TargetGroup是计算某一个LoopPoint的ForwardGroup集合,每个ForwardGroup面向一个输入点
		setupTargetGroupList();
	}

	//设置TargetGroup,生成TargetGroup的代码,TargetGroup是计算某一个LoopPoint的ForwardGroup集合,每个ForwardGroup面向一个输入点
	private void setupTargetGroupList() throws CircuitLoopException{
		//根据LoopPoint,建立TargetGroup
		for(LinearBlockElement element:loopPointList) {
			TargetGroup targetGroup=new TargetGroup(element);
			for(ForwardGroup forwardGroup:forwardGroupList) {
				if(forwardGroup.getEndElement().getBlock()==targetGroup.getTargetElement().getBlock()) {
					targetGroup.addForwardGroup(forwardGroup);
				}
			}
			targetGroupList.add(targetGroup);
		}

		for(TargetGroup targetGroup:targetGroupList) {

			//建立TargetGroup的相关Block
			targetGroup.setupRelatedBlockList();

			//生成每一个TargerGroup的代码
			targetGroup.generateTargetGroupCode();
		}
	}

	//设置ForwardGroup,ForwardGroup是起点和终点一样的前向通道集合
	private void setupForwardGroupList() {
		//将起点和终点相同的Forward列表,放入一个ForwardGroup
		for(List<LinearBlockElement> forward:forwardList) {
			LinearBlockElement startElement=forward.get(forward.size() - 1);
			LinearBlockElement endElement=forward.get(0);

			//如果group存在,就插进去
			boolean isFound=false;
			for(ForwardGroup forwardGroup:forwardGroupList) {
				if(forwardGroup.isSameStartEnd(startElement, endElement)) {
					forwardGroup.addForward(forward);
					isFound=true;
				}
			}

			//如果不存在,就建立一个
			if(isFound==false) {
				ForwardGroup forwardGroup=new ForwardGroup(forward,loopList);
				forwardGroupList.add(forwardGroup);
			}

		}

		//将Loop也放入ForwardGroup一起处理
		for(ForwardGroup forwardGroup:forwardGroupList) {
			for(ForwardLine forwardLine:forwardGroup.getForwardLineList()) {
				//处理Loop,看前向通道和Loop是否有接触
				forwardLine.setupLoops(loopList);
			}
		}
	}
}
