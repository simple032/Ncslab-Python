package circuit;

import java.util.Vector;

import circuit.block.BlockMode;
import circuit.block.CircuitBlock;
import circuit.line.CircuitLine;
import circuit.block.io.CircuitNode;
import circuit.block.io.CircuitPort;
import circuit.block.io.BlockVoltage;
import ncslablink.NCSLabModel;

import block.Block;
import line.Line;

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
		
		showNodes();
	}
	
	public static CircuitModel CreateCircuitModel(NCSLabModel model,Vector<CircuitBlock> blockList,Vector<CircuitLine> lineList) throws ModelException{
		return new CircuitModel(model,blockList,lineList);
	}
}  
