package com.ncslab.circuit;

import java.util.Vector;

import com.ncslab.circuit.block.BlockMode;
import com.ncslab.circuit.block.CircuitBlock;
import com.ncslab.circuit.line.CircuitLine;
import com.ncslab.circuit.block.io.CircuitNode;
import com.ncslab.circuit.block.io.CircuitPort;
import com.ncslab.circuit.block.io.BlockVoltage;
import com.ncslab.ncslablink.NCSLabModel;

import com.ncslab.block.Block;
import com.ncslab.line.Line;
import com.ncslab.block.io.InputPort;

import com.ncslab.block.math.Add;

import com.ncslab.circuit.loop.LoopSolver;
import com.ncslab.circuit.loop.CircuitLoopException;
import com.ncslab.circuit.loop.LinearBlockElement;

import com.ncslab.circuit.block.electblock.ElectBlock;

import com.ncslab.ncslablink.ModelException;

public class CircuitModel {
	private NCSLabModel model;
	private Vector<CircuitBlock> blockList=new Vector<CircuitBlock>();
	private Vector<CircuitLine> lineList=new Vector<CircuitLine>();
	private Vector<CircuitNode> nodeList=new Vector<CircuitNode>();
	
	private Vector<BlockMode> blockModeList=new Vector<BlockMode>();
	
	private Vector<Block> terminalBlockList=new Vector<Block>();
	
	private boolean isTreeFound=false;
	
	CircuitModel(NCSLabModel model,Vector<CircuitBlock> blockList,Vector<CircuitLine> lineList) throws ModelException{
		this.model=model;
		this.blockList=blockList;
		this.lineList=lineList;
		
		for(CircuitBlock block:blockList) {
			block.setCircuitModel(this);
		}
		
		//建立节点的数学模型
		createCircuitNodes();
		
		//获得生成树
		getTree();
		
		//如果无法产生生成树,就报错
		if(isTreeFound==false) {
			throw(new ModelException("The circuit part can not be resolved."));
		}
		showBranches();
		
		//setupBlocks();
	}
	
	public void clearElectBlockLoop() {
		Vector<Block> blockList=this.getModelBlocks();
		for(Block block:blockList) {
			if(block instanceof ElectBlock) {
				ElectBlock electBlock=(ElectBlock)block;
				electBlock.clearLoop();
			}
		}
	}
	
	public void addTerminalBlocks(Block terminalBlock) {
		terminalBlockList.add(terminalBlock);
	}
	
	//记录生成树的路径
	private Vector<CircuitNode> nodeListNew=new Vector<CircuitNode>();
	
	private void getTree() {
		
		//开始从各个顶点搜索
		for(CircuitNode startNode:nodeList) {
			//将节点加入生成树路径
			nodeListNew.add(startNode);
			//搜索这个节点
			searchTree(startNode);
			//将节点从生成树路径去除
			nodeListNew.remove(startNode);
		}
		
		//将遍历结果,也就是各个树枝的模式,赋值给各个Block
		for(int i=0;i<blockModeList.size();i++) {
			blockList.get(i).setBlockMode(blockModeList.get(i));
		}
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
	
	private boolean isSetupLinkCalled=false;
	
	private void setupLinks() {
		if(isSetupLinkCalled) {
			return;
		}
		for(CircuitBlock block:blockList) {
			if(block.getBlockMode()==null) {
				block.setBlockMode(BlockMode.Link);
			}
			blockModeList.add(block.getBlockMode());
		}
		isSetupLinkCalled=true;
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
		
		//如果已经发现了生成树,就返回,终止遍历
		if(isTreeFound) {
			return;
		}
		
		//这个Node所有相连的Port
		Vector<CircuitPort> circuitPortList = node.getCircuitPortList();

		// 寻找只能做branch的block
		Vector<CircuitPort> branchOnlyList = new Vector<CircuitPort>();
		//寻找可以做Branch和link的树枝
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
		
		//根据可以做树枝也可以做连枝的个数,穷举所有可能性
		int n=(1<<anythingList.size());
		//System.out.println(anythingList.size()+":"+n);
		
		//穷举搜索这些可能性
		for(int i=0;i<n;i++) {
			//根据i的值,设置各个Anything的树枝的状态
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
					//搜索只能做树枝的节点的邻居节点
					searchTree(circuitPort.getBlock().getAnotherCircuitPort(circuitPort).getCircuitNode());
				}
				for (CircuitPort circuitPort : anythingList) {
					//如果现在作为anythingList的节点也是树枝状态,则也搜索它的邻居节点
					if(circuitPort.getBlock().getBlockMode()==BlockMode.Branch) {
						searchTree(circuitPort.getBlock().getAnotherCircuitPort(circuitPort).getCircuitNode());
					}
				}
			}
			else 
			//否则如果搜索的节点数正好等于节点总数,说明所有的节点都遍历了,树已经生成
			if(nodeListNew.size()==nodeList.size()) {
				if(isValidTree()) {
					//将没有设置成Link的节点,设置成Link形式
					setupLinks();
					//showNewNodes();
					
					//设置树已经生成的标志
					isTreeFound=true;
				}
			}
			
			//遍历完毕,根据i的值,恢复各个Anything的树枝的状态
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
		
		
		//搜索完毕,清除这个节点各个树枝的状态
		for (CircuitPort circuitPort : branchOnlyList) {
			CircuitBlock block = circuitPort.getBlock();
			block.setBlockMode(null);
			nodeListNew.remove(block.getAnotherCircuitPort(circuitPort).getCircuitNode());
		}
	}
	
	//搜索所有的Line,建立节点
	private void createCircuitNodes() {
		int nodeId=1;
		//遍历所有的Line
		for(CircuitLine line:lineList) {
			//System.out.println(line.getFromPort().getBlock().getBlockName()+":"+line.getToPort().getBlock().getBlockName());
			
			//如果这个Link的Form和To的Port有一个在Node中,就把另一个也加入到Node中
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
			
			//如果没有,说明要建立一个新的Node
			if(found==false) {
				CircuitNode newNode=new CircuitNode(nodeId++);
				newNode.addCircuitPort(fromPort);
				newNode.addCircuitPort(toPort);
				nodeList.add(newNode);
			}
		}
	}
		
	private Vector<CircuitBlock> linkBlockList=new Vector<CircuitBlock>();
	private Vector<BlockVoltage> voltagePath;
	private boolean isLoopFound=false;
	private CircuitPort refPort;
	
	//寻找Loop,建立电压方程
	private void setupBlockVoltage() {
		
		//寻找所有做树枝的Block
		for(CircuitBlock block:blockList) {
			if(block.getBlockMode()==BlockMode.Link) {
				linkBlockList.add(block);
			}
		}
		
		//搜索所有做树枝的Block
		for(CircuitBlock block:linkBlockList) {
			voltagePath=new Vector<BlockVoltage>();
			isLoopFound=false;
			//搜索树枝
			searchBlock(block,true,null);
			showBlockPath(block);
		}
	}
	
	private boolean isBlockInPath(CircuitBlock block) {
		for(BlockVoltage voltage:voltagePath) {
			if(voltage.getCircuitBlock()==block) {
				return true;
			}
		}
		return false;
	}
	
	private void showBlockPath(CircuitBlock block) {
		System.out.println("Found Loop for "+block.getBlockName());
		for(BlockVoltage voltage:block.getVoltageList()) {
			System.out.print(voltage.getCircuitBlock().getBlockName()+"/"+voltage.getSign()+"\t");
		}
		System.out.println();
	}
	
	private void saveLoop() {
		
		CircuitBlock block=voltagePath.get(0).getCircuitBlock();
		
		Vector<BlockVoltage> voltageList=new Vector<BlockVoltage>();
		
		for(int i=1;i<voltagePath.size();i++) {
			BlockVoltage voltage=voltagePath.get(i);
			voltageList.add(voltage);
		}
		
		block.setVoltageList(voltageList);
	}
	
	//搜索连枝的对应Loop,baseSign传递和连枝的方向,与连枝方向相反,继承符号,否则相反,basePort表示和上一个搜索树枝连接的Port
	private void searchBlock(CircuitBlock baseBlock,boolean baseSign,CircuitPort basePort) {
		if(isLoopFound) {
			return;
		}
		//将电压表达压入voltagePath
		BlockVoltage blockVoltage=new BlockVoltage(baseBlock,baseSign);
		voltagePath.add(blockVoltage);
		
		//如果没有basePort,则把第一个Port当成时basePort,并且当成refPort,refPort用来判断符号
		if(basePort==null) {
			basePort=baseBlock.getCurcuitPortList().get(0);
			refPort=basePort;
		}
		
		//寻找Block另一边的Port和Node
		CircuitPort anotherPort=baseBlock.getAnotherCircuitPort(basePort);
		CircuitNode anotherNode=anotherPort.getCircuitNode();
		
		//获得另一边的Node的所有port
		Vector<CircuitPort> portList=anotherNode.getOtherCircuitPortList(anotherPort);
		//搜索每一个Port
		for(CircuitPort port:portList) {
			//获得Block的Path
			CircuitBlock block=port.getBlock();
			//如果这个Block不再Path里面
			if(isBlockInPath(block)==false) {
				//如果这个模块是树枝,说明需要搜索
				if(block.getBlockMode()==BlockMode.Branch) {
					
					//根据与refPort的方向,确定符号
					boolean sign=!(refPort.getCircuitPortType()==port.getCircuitPortType());
					//搜索这个Block
					searchBlock(block,sign,port);
				}
			}
			//如果Block在Path里面,说明是回路,保存回路,设置标志位
			else {
				saveLoop();
				isLoopFound=true;
			}
		}
		
		voltagePath.remove(blockVoltage);
	}
	
	private void showNodes() {
		for(CircuitNode node:nodeList) {
			node.showNode();
		}
	}
	
	//建立节点的电流之间的关系
	private void setupNodeCurrent() {
		
		//一个个节点搜索,建立电流方程
		for(CircuitNode node:nodeList) {
			//搜索节点
			node.searchNode(null);
			//System.out.println(node.getIsNodeCurrentDecided());
		}
	}
	
	public Vector<Block> getModelBlocks() {
		Vector<Block> modelBlockList=new Vector<Block>();
		
		for(CircuitBlock block:blockList) {
			modelBlockList.addAll(block.getBlockList());
		}
		
		return modelBlockList;
	}
	
	public Vector<Line> getModelLines(){
		Vector<Line> modelLineList=new Vector<Line>();
		
		for(CircuitBlock block:blockList) {
			modelLineList.addAll(block.getLineList());
		}
		
		return modelLineList;
	}
	
	
	//将Circuit转化成Block和连线
	public void setupModel() {
		System.out.println("Setup Circuit Model Node Current...");
		//建立节点的电流之间的关系
		setupNodeCurrent();
		
		System.out.println("Setup Circuit Model Block Voltage...");
		//寻找Loop,建立电压方程
		setupBlockVoltage();
		
		System.out.println("Setup Circuit Model blocks...");
		
		//一个个设置Block,并建立内部连接关系
		for(CircuitBlock block:blockList) {
			block.setupBlocks();
		}
		
		//建立了模块之后,需要updateBlock
		Vector<Block> modelBlocks=getModelBlocks();
		for(Block block:modelBlocks) {
			block.updateBlock();
		}
		
		// 设置各个Block之间的连接线
		for (CircuitBlock block : blockList) {
			block.setupBlockConnections();
		}
		
		//loopProcess();
		
		//showNodes();
	}
	
	private Vector<LinearBlockElement> blockPath;
	
	private LoopSolver loopSolver;
	
	private boolean isInBlockPath(Block baseBlock) {
		for(LinearBlockElement element:blockPath) {
			if(element.getBlock()==baseBlock) {
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
	
	//搜索模块,寻找前向通道和相关的Loop
	private void searchBlock(Block block) {
		//如果在BlockPath中,说明发现环路
		if(isInBlockPath(block)) {
			//要先保存环路
			//LinearBlockElement element=new LinearBlockElement(block,true);
			//blockPath.add(element);
			loopSolver.addLoop(blockPath,block);
			//System.out.println("Loop:");
			//showBlockPath();
			//blockPath.remove(element);
			return;
		}
		
		//如果时终端模块或者是feedthrough==false的模块,说明发现前向通道
		if(block.getInputPortList().size()==0
				||block.getOutputPortList().get(0).getFeedThrough()==false) {
			//保存前向通道
			
			LinearBlockElement element=new LinearBlockElement(block,true);
			blockPath.add(element);
			loopSolver.addForward(blockPath);
			//System.out.println("Forward:");
			//showBlockPath();
			blockPath.remove(element);
			return;
		}
		
		LinearBlockElement element=new LinearBlockElement(block,true);
		blockPath.add(element);
		
		//如果是Add,说明需要动态调整符号
		if(block.getBlockType().equals("Add")){
			Vector<InputPort> inputPortList=block.getInputPortList();
			
			int i=0;
			for(InputPort input:inputPortList) {
				Add add=(Add)block; 
				element.setSign(add.getSign(i));
				i++;
				Block newBlock=input.getLinkedLine().getLinkedOutputPort().getBLock();
				//System.out.println(input.getLinkedLine().getLinkedOutputPort().getBLock().getBlockName());
				//向前递归搜索
				searchBlock(newBlock);
			}
		}
		else {
			Vector<InputPort> inputPortList=block.getInputPortList();
			InputPort input=inputPortList.get(0);
			
			//如果和Input有连接,则搜索(Input可能与Circuit之外的模块连接,此时还没有处理,LinkedLine应该是null)
			if(input.getLinkedLine()!=null) {
				Block newBlock=input.getLinkedLine().getLinkedOutputPort().getBLock();
				//向前递归搜索
				searchBlock(newBlock);
			}
			
		}
		
		
		blockPath.remove(element);
	}
	
	//解开代数环的代码
	public void loopProcess() throws CircuitLoopException{
		System.out.println("Solving possible linear algebraic loops...");
		//terminalBlockList.clear();
		//找到Circuit部分的终端模块和广义终端模块(输出连接到Feedthrough=false的模块)
		for(Block block:this.getModelBlocks()) {
			if(block.getOutputPortList().size()==0) {
				terminalBlockList.add(block);
			}
			else
			if(block.getOutputPortList().get(0).getFeedThrough()==false&&block.getInputPortList().size()!=0)
			{
				terminalBlockList.add(block.getInputPortList().get(0).getLinkedLine().getLinkedOutputPort().getBLock());
				//terminalBlockList.add(block);
			}
		}
		
		//解开代数环的解算器组合,计算每一个终端模块都可能产生代数环
		Vector<LoopSolver> loopSolverList=new Vector<LoopSolver>();
		
		//搜索每一个模块的解算流程,寻找可能有代数环的输出
		for(Block terminalBlock:terminalBlockList) {
			//System.out.println(terminalBlock.getBlockName()+"...");
			
			//准备搜索这个接单,建立路径的数据结构
			blockPath=new Vector<LinearBlockElement>();
			//建立LoopSolver模型
			loopSolver=new LoopSolver(terminalBlock,this);
			//搜索这个节点
			searchBlock(terminalBlock);
			//如果节点有Loop,则需要处理,放入需要处理的队列
			if(loopSolver.isLoopIncluded()) {
				//loopSolver.showLoopSolver();
				loopSolverList.add(loopSolver);
			}
		}
		
		
		//建立面向代数环节点的前馈通道
		for(LoopSolver loopSolver:loopSolverList) {
			//解开代数环
			loopSolver.solveLoop();
			loopSolver.showLoopSolver();
		}
	}
	
	public static CircuitModel CreateCircuitModel(NCSLabModel model,Vector<CircuitBlock> blockList,Vector<CircuitLine> lineList) throws ModelException{
		return new CircuitModel(model,blockList,lineList);
	}
}  
