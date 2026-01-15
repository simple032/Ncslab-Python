package com.ncslab.circuit2;

import java.util.Vector;

import lombok.Getter;
import lombok.Setter;

import org.apache.ibatis.annotations.Delete;
//import org.apache.jasper.tagplugins.jstl.core.If;

import com.greenpineyu.fel.parser.FelParser.integerLiteral_return;

import com.ncslab.circuit2.block.BlockMode;
import com.ncslab.circuit2.block.BlockModeType;
import com.ncslab.circuit2.block.CircuitBlock;
import com.ncslab.circuit2.line.CircuitLine;
import com.ncslab.circuit2.block.io.CircuitNode;
import com.ncslab.circuit2.block.io.CircuitPort;
import com.ncslab.circuit2.block.io.CircuitPortType;
import com.ncslab.circuit2.block.multielement.OpAmp;
import com.ncslab.ncslablink.NCSLabModel;
//import oracle.toplink.essentials.internal.parsing.LiteralNode;
import com.ncslab.circuit2.block.baseelement.VoltageSource;
import com.ncslab.circuit2.block.element.CurrentSensor;
import com.ncslab.circuit2.block.baseelement.CircuitBlockSingle;
import com.ncslab.circuit2.block.baseelement.CurrentSource;
import com.ncslab.circuit2.block.baseelement.CircuitBlockMulti;

import com.ncslab.circuit2.block.element.SwitchBlock;
import com.ncslab.circuit2.block.element.VariableBlock;
import com.ncslab.circuit2.partition.CircuitPartition;
import com.ncslab.circuit2.partition.CircuitPartitioner;

import com.ncslab.ncslablink.ModelException;

public class CircuitModel2 {
	private NCSLabModel model;
	private Vector<CircuitBlock> blockList=new Vector<CircuitBlock>();
	private Vector<CircuitBlockSingle> singleBlockList=new Vector<CircuitBlockSingle>();
	private Vector<CircuitBlockMulti> multiBlockList=new Vector<CircuitBlockMulti>();
	private Vector<CircuitLine> lineList=new Vector<CircuitLine>();
	private Vector<CircuitNode> nodeList=new Vector<CircuitNode>();
	
	private Vector<CircuitBlock> vsBlockList=new Vector<CircuitBlock>();
	
	private Vector<CircuitBlock> switchBlockList=new Vector<CircuitBlock>();
	
	private Vector<CircuitBlock> variableBlockList=new Vector<CircuitBlock>();
	
	//参考节点
	private Vector<Integer> refList = new Vector<Integer>();
	//如Current Sensor的短路模块,需删除结点
	private Vector<CircuitBlock> shortBlockList = new Vector<CircuitBlock>();
	
	private CircuitPartitioner partitioner;
	
	private int mSize;
	private String[][] gAA;
	private String[] iA;
	
	@Getter
	@Setter
	private boolean isRecalc=false;
	
	
	CircuitModel2(NCSLabModel model,Vector<CircuitBlock> blockList,Vector<CircuitLine> lineList) throws ModelException{
		this.model=model;
		this.blockList=blockList;
		this.lineList=lineList;
		
		int blockId=blockList.size()+1;
		
		for(CircuitBlock block:blockList) {
			if(block instanceof CircuitBlockSingle) {
				this.singleBlockList.add((CircuitBlockSingle)block);
			}
			else
			if(block instanceof CircuitBlockMulti) {
				CircuitBlockMulti multiBlock=(CircuitBlockMulti)block;
				multiBlock.setupSubCircuitBlocks(blockId);
				
				blockId+=multiBlock.getSingleCurcuitBlockList().size();
				this.singleBlockList.addAll(multiBlock.getSingleCurcuitBlockList());
				this.lineList.addAll(multiBlock.getCircuitLineList());
				multiBlockList.add(multiBlock);
			}
		}
		
		int switchId=0;
		for(CircuitBlock block:singleBlockList) {
			if(block instanceof SwitchBlock) {
				switchBlockList.add(block);
				((SwitchBlock) block).setSwitchId(switchId++);
			}
		}
		
		int variableId=0;
		for(CircuitBlock block:singleBlockList) {
			if(block instanceof VariableBlock) {
				variableBlockList.add(block);
				((VariableBlock) block).setVariableBlockId(variableId++);
			}
		}
		
	}
	
	public String getGAADefineCode() {
		String code="/*GAA define code*/\n";
		
		code+="SwitchGAA switchGAA;\n";
		
		code+="SwitchGAA *psGaa=&switchGAA;\n";
		
		code+="uint32_T switchStatus["+(switchBlockList.size()/32+1)+"];\n";
		
		code+="REAL gAAOriginal["+gAA.length*gAA.length+"];\n";
		
		code+="void getUpdatedGAA(REAL *gAA,int size){\n";
		code+="REAL gAAValue["+gAA.length*gAA.length+"]={\n";
		for(int i=0;i<gAA.length;i++) {
			for(int j=0;j<gAA.length;j++) {
				code+=gAA[i][j];
				if(i!=gAA.length-1||j!=gAA.length-1) {
					code+=",";
				}
				
			}
			code+='\n';
		}
		code+="};\n";
		
		code+="memcpy(gAA,gAAValue,size*sizeof(REAL));\n";
		
		code+="}\n";
		
		code+="//CircuitThread circuitThreads[CIRCUIT_THREAD_NUM];\n";
		
		return code;
	}
	
	
	public String getGAAInitCode() {
		String code="/*GAA Init code*/\n";
		
		code+="getUpdatedGAA(gAAOriginal,"+gAA.length*gAA.length+");\n";
		code+="switchGAA.gAAOriginal=gAAOriginal;\n";
		code+="switchGAA.sizeOriginal="+gAA.length+";\n";
		code+="switchGAA.switchNum="+(switchBlockList.size())+";\n";
		code+="switchGAA.switchStatus=switchStatus;\n";
		code+="switchGAA.storeGAASize=0;\n";
		code+="switchGAA.storeGAA=(StoreGAA *)malloc(0);\n";
		
		//code+="addSwitchCombine(&switchGAA,switchGAA.gAAOriginal,"+switchBlockList.size()+","+gAA.length+",switchGAA.switchStatus);\n";
		
		code+="//startCircuitThread(circuitThreads,CIRCUIT_THREAD_NUM);\n";
		
		return code;
	}
	
	public String getVariableDefineCode() {
		String code="/*Circuit Varialbe Define Code*/\n";
		for(CircuitBlock block:variableBlockList) {
			VariableBlock varBlock=(VariableBlock)block;
			code+=varBlock.getVariableDefineCode();
		}
		return code;
	}
	
	public String getVariableChangeCode() {
		String code="/*Checking all the variable Blocks*/\n";
		for(CircuitBlock block:variableBlockList) {
			VariableBlock varBlock=(VariableBlock)block;
			code+=varBlock.getVariableChangeCode();
		}
		
		code+="/*Set variable change status for all the GAAStore*/\n";
		
		code+="if("+com.ncslab.circuit2.block.element.VariableBlock.isVariableChanged+"==1){\n";
		code+="for(int i=0;i<switchGAA.storeGAASize;i++){\n";
		code+="switchGAA.storeGAA[i].isVariableChanged=1;\n";
		code+="}\n";
		code+="}\n";
		
		return code;
	}
	
	
	public Vector<CircuitBlockMulti> getMultiBlockList(){
		return this.multiBlockList;
	}
	
	public Vector<CircuitBlock> getVsBlockList(){
		return this.vsBlockList;
	}
	
	public Vector<CircuitBlock> getSwitchBlockList(){
		return this.switchBlockList;
	}
	
	public Vector<Integer> getRefList() {
		return this.refList;
	}
	
	public void calculateInits(double tStart) {
		createCircuitNodes();
		
		/*
		for(CircuitBlockMulti multiBlock:multiBlockList) {
			multiBlock.setupLogicCode();
		}*/
		
		setRef();
		
		System.out.println("Partiationing...");
		partitioner=CircuitPartitioner.CreateCircuitPartitioner(this);
		
		for(CircuitPartition part:partitioner.getPartitionList()) {
			part.calculateInits(tStart);
		}
		
		//CreateGAA();
		
		/*
		for(CircuitNode node:nodeList) {
			System.out.println(node.getNodeId()+":"+node.getCircuitPortList().size()+":"+node.getVsCircuitPortList().size());
			for(CircuitPort port:node.getCircuitPortList()) {
				System.out.print(port.getBlock().getBlockName()+":"+port.getName()+"\t");
			}
			System.out.println();
		}*/
	}
	
	public void calculateOutputs(double t) {
		//System.out.println(t);
		for(CircuitPartition part:partitioner.getPartitionList()) {
			part.calculateOutputs(t);
		}
		
		for(CircuitBlockMulti block:multiBlockList) {
			block.updateLogic(t);
		}
		
		if(this.isRecalc) {
			this.isRecalc=false;
			for(CircuitPartition part:partitioner.getPartitionList()) {
				part.calculateOutputs(t);
			}
			for(CircuitBlockMulti block:multiBlockList) {
				block.updateLogic(t);
			}
		}
	}
	
	public void calculateUpdate(double t) {
		for(CircuitBlockSingle block:singleBlockList) {
			block.updateHis();
		}
	}
	
	public void generate() {
		//建立节点的数学模型
				createCircuitNodes();
				
				for(CircuitBlockMulti multiBlock:multiBlockList) {
					multiBlock.setupLogicCode();
				}
				
				setRef();
				
				System.out.println("Partiationing...");
				partitioner=CircuitPartitioner.CreateCircuitPartitioner(this);
				
				CreateGAA();
				
				for(CircuitNode node:nodeList) {
					System.out.println(node.getNodeId()+":"+node.getCircuitPortList().size()+":"+node.getVsCircuitPortList().size());
					for(CircuitPort port:node.getCircuitPortList()) {
						System.out.print(port.getBlock().getBlockName()+":"+port.getName()+"\t");
					}
					System.out.println();
				}
				
				
	}
	
	//设置参考点
	private void setRef() {
		int id=0;
		for(CircuitBlockSingle block:singleBlockList) {
			if(block.getBlockModeType()==BlockModeType.VoltageSource) {
				vsBlockList.add(block);
				((VoltageSource)block).setVsId(id++);		
			}else
			if(block.getBlockModeType()==BlockModeType.Ground) {
				block.getCurcuitPortList().get(0).getCircuitNode().setIsRef(true);
				block.getCurcuitPortList().get(0).getCircuitNode().setFloating(false);
				this.getRefList().add(block.getCurcuitPortList().get(0).getCircuitNode().getNodeId());			
			}
		}
		
		checkFloatingNode();
		
	}
	
	//检查浮空节点,若存在设置参考点
	private void checkFloatingNode() {
		int checkedNode = 0;
		int nodeIdIndex = 0;
		int refIdIndex = 0;

		while(checkedNode<nodeList.size()) {
			if(refIdIndex<refList.size()) {
				//BFS
				Vector<Integer> bfs = new Vector<Integer>();
				while(refIdIndex<refList.size()) {
					bfs.add(refList.get(refIdIndex));
					nodeList.get(refList.get(refIdIndex)).setFloating(false);
					++checkedNode;
					++refIdIndex;
				}
				int index = 0;
				while(index < bfs.size()) {
					CircuitNode node = nodeList.get(bfs.get(index));
					for(CircuitPort port : node.getCircuitPortList()) {
						CircuitBlockSingle block = port.getBlock();
						//注意处理非2端口的模块,目前只有接地和运放.
						CircuitNode nNode = block.getAnotherCircuitPort(port).getCircuitNode();
						if(nNode.isFloating()) {
							nNode.setFloating(false);
							++checkedNode;
							bfs.add(nNode.getNodeId());
						}
					}
					++index;
				}
			}else {
				while(nodeIdIndex<nodeList.size()) {
					CircuitNode node = nodeList.get(nodeIdIndex);
					++nodeIdIndex;
					if(node.isFloating()) {
						refList.add(node.getNodeId());
						node.setIsRef(true);
						node.setFloating(false);
						break;
					}
				}
			}
		}
	}
	private void combineNode(CircuitBlockSingle block) {
		CircuitPort lPort = block.getCurcuitPortList().get(0);
		CircuitPort rPort = block.getCurcuitPortList().get(1);
		CircuitNode lNode = lPort.getCircuitNode();
		CircuitNode rNode = rPort.getCircuitNode();
		
		//node去除Sensor的端口,但Sensor的端口应保留node信息以读取电流
		lNode.getCircuitPortList().remove(lPort);
		rNode.getCircuitPortList().remove(rPort);
		
		//除去lNode
		lPort.setCircuitNode(rNode);
		for(CircuitPort port : lNode.getCircuitPortList()) {
			rNode.addCircuitPort(port);
			rNode.addCombinedCircuitPort(port);
		}
		
		int i = lNode.getNodeId();
		nodeList.remove(i);
		for(;i<nodeList.size();++i) {
			nodeList.get(i).setNodeId(i);
		}
	}

	public Vector<CircuitNode> getNodeList(){
		return this.nodeList;
	}
	
	public Vector<CircuitBlock> getBlockList(){
		return this.blockList;
	}
	
	public Vector<CircuitBlockSingle> getSingleBlockList(){
		return this.singleBlockList;
	}
	
	public String[][] getGAA(){
		return this.gAA;
	}
	
	public String[] getIA() {
		return this.iA;
	}
	
	private void CreateGAA() {
		int vsStart=nodeList.size();
		mSize=nodeList.size()+vsBlockList.size();
		gAA=new String[mSize][mSize];
		iA=new String[mSize];
		for(int i=0;i<mSize;i++) {
			for(int j=0;j<mSize;j++) {
				gAA[i][j]="0.0";
			}
		}
		
		for(int i=0;i<mSize;i++) {
			iA[i]="0.0";
		}
		
		for(CircuitNode node:nodeList) {
			//如果是参考节点,直接自身节点等于0
			if(node.getIsRef()) {
				gAA[node.getNodeId()][node.getNodeId()]="1.0";
			}
			else {
				for(CircuitPort port:node.getCircuitPortList()) {
					CircuitBlockSingle block=port.getBlock();
					if(block.getBlockModeType()==BlockModeType.Nromal) {
						CircuitNode nNode=block.getAnotherCircuitPort(port).getCircuitNode();
						gAA[node.getNodeId()][node.getNodeId()]+="-1.0/("+block.getRString()+")";
						gAA[node.getNodeId()][nNode.getNodeId()]+="+1.0/("+block.getRString()+")";
					}
					else
					//如果是电压源,则访问电压源的电流作为未知数
					if(block.getBlockModeType()==BlockModeType.VoltageSource) {
						if(port.getCircuitPortType()==CircuitPortType.Left) {
							gAA[node.getNodeId()][vsStart+((VoltageSource)block).getVsId()]="-1.0";
						}
						else {
							gAA[node.getNodeId()][vsStart+((VoltageSource)block).getVsId()]="1.0";
						}
					}else					
					//如果是电流源,则在iA中增加一项
					if(block instanceof CurrentSource) {
						CurrentSource cBlock=(CurrentSource)block;
						String sign=port.getCircuitPortType()==CircuitPortType.Left?"(-1.0)":"(1.0)";
						iA[node.getNodeId()]+="+"+sign+"*("+cBlock.getIString()+")";
					}else 						
					//如果是运放输出端,结点电压受输入端电压差控制
					if(block.getBlockModeType() == BlockModeType.OpAmp && port.getName() == "RConn1") {
						int nodeId1 = block.getCurcuitPortList().get(0).getCircuitNode().getNodeId();
						int nodeId2 = block.getCurcuitPortList().get(2).getCircuitNode().getNodeId();
						String amp = ((OpAmp)block).getAmplitude();
						for(int i=0;i<mSize;++i) {
							gAA[node.getNodeId()][i]="0.0";
						}
						gAA[node.getNodeId()][node.getNodeId()] = "-1.0";
						gAA[node.getNodeId()][nodeId1] = "-1.0*"+amp;
						gAA[node.getNodeId()][nodeId2] = amp;
						break;
					}
				}
				iA[node.getNodeId()]+="+"+node.getHisString();
			}
		}
		
		/*处理电压源,增加关于电压源的方程*/
		for(CircuitBlock block:vsBlockList) {
			VoltageSource vsBlock=(VoltageSource)block;
			vsBlock.setCurrentId(vsStart+((VoltageSource)block).getVsId());
			gAA[vsStart+((VoltageSource)block).getVsId()][vsBlock.getCurcuitPortList().get(0).getCircuitNode().getNodeId()]="1.0";
			gAA[vsStart+((VoltageSource)block).getVsId()][vsBlock.getCurcuitPortList().get(1).getCircuitNode().getNodeId()]="-1.0";
			iA[vsStart+((VoltageSource)block).getVsId()]="("+vsBlock.getVString()+")";
		}
		
		/*
		for(int i=0;i<mSize;i++) {
			for(int j=0;j<mSize;j++) {
				System.out.print(gAA[i][j]+"\t");
			}
			System.out.println(iA[i]);
		}
		
		for(CircuitBlockSingle block:singleBlockList) {
			System.out.print(block.getHisUpdateString()+"\t");
		}
		System.out.println();*/
	}
	
	//扫描新添加的port，如果出现在两个node中，就把这两个node合并
	private void combileNodeByPort(CircuitPort port) {
		//寻找包含port的Node，放在portNodeList中
		Vector<CircuitNode> portNodeList=new Vector<CircuitNode>();
		for(CircuitNode node:nodeList) {
			if(node.isCircuitPortIncluded(port)) {
				portNodeList.add(node);
			}
		}
		//System.out.println(portNodeList.size());
		//如果出现两次，说明这两个Node需要合并
		if(portNodeList.size()==2) {
			CircuitNode node0=portNodeList.get(0);
			CircuitNode node1=portNodeList.get(1);
			
			//node1.getCircuitPortList().remove(port);
			
			//node0.getCircuitPortList().addAll(node1.getCircuitPortList());
			
			//把node1中的port合并到node0中，不能重复
			for(CircuitPort addPort:node1.getCircuitPortList()) {
				if(node0.isCircuitPortIncluded(addPort)==false) {
					node0.addCircuitPort(addPort);
				}
			}
			
			/*
			for(CircuitPort nodePort:node0.getCircuitPortList()) {
				nodePort.setCircuitNode(node0);
			}*/
			//删除小node1
			nodeList.remove(node1);
		}
	}
	
	//搜索所有的Line,建立节点
	private void createCircuitNodes() {
		int nodeId=0;
		//遍历所有的Line
		for(CircuitLine line:lineList) {
			//System.out.println(line.getFromPort().getBlock().getBlockName()+":"+line.getToPort().getBlock().getBlockName());
			
			//如果这个Link的Form和To的Port有一个在Node中,就把另一个也加入到Node中
			boolean found;
			CircuitPort fromPort=line.getFromPort();
			CircuitPort toPort=line.getToPort();
			
			CircuitPort port=null;
			found=false;
			for(CircuitNode node:nodeList) {
				if(node.isCircuitPortIncluded(fromPort)) {
					found=true;
					node.addCircuitPort(toPort);
					port=toPort;
				}
				else
				if(node.isCircuitPortIncluded(toPort)) {
					found=true;
					node.addCircuitPort(fromPort);
					port=fromPort;
				}
			}
			
			//有可能添加的那个port本来就在别的Node中，说明这两个node就是一个node,这个时候要把那个Node和现在这个node合并
			if(found) {
				combileNodeByPort(port);
			}
			
			//如果没有,说明要建立一个新的Node
			if(found==false) {
				CircuitNode newNode=new CircuitNode(nodeId++);
				newNode.addCircuitPort(fromPort);
				newNode.addCircuitPort(toPort);
				nodeList.add(newNode);
			}
		}
		
		//因为有可能有node的合并，因此要重新编号id
		nodeId=0;
		for(CircuitNode node:nodeList) {
			node.setNodeId(nodeId++);
			for(CircuitPort port:node.getCircuitPortList()) {
				port.setCircuitNode(node);
			}
		}
	}
	
	public String getCircuitPartitionDefineCode() {
		return partitioner.getCircuitDefineCode();
	}
	
	public String getCircuitPartitionInitCode() {
		return partitioner.getCircuitInitCode();
	}
	
	public String getCircuitPartitionOutputCode() {
		return partitioner.getCircuitOutputCode();
	}
	
	public static CircuitModel2 CreateCircuitModel(NCSLabModel model,Vector<CircuitBlock> blockList,Vector<CircuitLine> lineList) throws ModelException{
		return new CircuitModel2(model,blockList,lineList);
	}
}  
