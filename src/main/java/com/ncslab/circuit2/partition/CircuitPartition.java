package com.ncslab.circuit2.partition;

import java.util.Vector;
import lombok.Getter;
import org.apache.commons.math3.linear.*;

import com.ncslab.circuit2.CircuitModel2;
import com.ncslab.circuit2.block.BlockModeType;
import com.ncslab.circuit2.block.CircuitBlock;
import com.ncslab.circuit2.block.baseelement.CircuitBlockSingle;
import com.ncslab.circuit2.block.baseelement.CurrentSource;
import com.ncslab.circuit2.block.baseelement.VoltageSource;
import com.ncslab.circuit2.block.baseelement.CircuitBlockMulti;
import com.ncslab.circuit2.block.element.SwitchBlock;
import com.ncslab.circuit2.block.element.VariableBlock;
import com.ncslab.circuit2.block.io.CircuitNode;
import com.ncslab.circuit2.block.io.CircuitPort;
import com.ncslab.circuit2.block.io.CircuitPortType;
import com.ncslab.circuit2.block.multielement.OpAmp;
import com.ncslab.circuit2.block.multielement.Recalc;
import com.ncslab.circuit2.gaa.SwitchGAA;
import com.ncslab.circuit2.gaa.StoreGAA;

public class CircuitPartition {
	
	private Vector<CircuitBlock> blockList=new Vector<CircuitBlock>();
	private Vector<CircuitBlockSingle> singleBlockList=new Vector<CircuitBlockSingle>();
	private Vector<CircuitBlockMulti> multiBlockList=new Vector<CircuitBlockMulti>();
	
	//内部节点列表，内部Node，说明没有和外部相连的node
	private Vector<CircuitNode> intNodeList=new Vector<CircuitNode>();
	
	//内部和外部节点列表=intNodeList+exIntNodeList
	private Vector<CircuitNode> nodeList;
	//外部节点列表，因为外部节点和多个Partition连接，这里面的列表是和别的partition公共的
	private Vector<CircuitNode> exNodeList=new Vector<CircuitNode>();
	//外部公共节点的克隆，除了partNodeId之外，各个参数保持一致，本part私有
	private Vector<CircuitNode> exIntNodeList=new Vector<CircuitNode>();
	//电压源列表
	private Vector<CircuitBlock> vsBlockList=new Vector<CircuitBlock>();
	//参考节点列表，因为是节点合并形成part，不共地的电路不会放到一个part，所以一个part应该只有一个
	private Vector<CircuitNode> refNodeList;
	//开关器件的列表
	@Getter
	private Vector<CircuitBlock> switchBlockList=new Vector<CircuitBlock>();
	//变参数器件的列表
	private Vector<CircuitBlock> variableBlockList=new Vector<CircuitBlock>();
	
	private Vector<CircuitBlock> dynamicSwitchBlockList=new Vector<CircuitBlock>();
	
	private int id;
	
	//节点中对外电流对于part中各个节点的影响矩阵 
	private String currentEff[][];
	
	//不考虑对外电流，对外节点的电压值
	private String iAEff[];
	
	
	private SwitchGAA switchGAA;
	
	//part的构造函数，每个part都是从一个block开始的
	public CircuitPartition(CircuitBlock block) {
		//从一个block开始，建立part
		blockList.add(block);
		if(block instanceof CircuitBlockMulti) {
			CircuitBlockMulti blockMulti=(CircuitBlockMulti)block;
			singleBlockList.addAll(blockMulti.getSingleCurcuitBlockList());
			multiBlockList.add(blockMulti);
		}
		else {
			singleBlockList.add((CircuitBlockSingle)block);
		}
		
		//初始的时候，block的连接的node，就是对外node
		for(CircuitPort port:block.getCurcuitPortList()) {
			exNodeList.add(port.getCircuitNode());
		}
		
		//按照各个SingleBlock,增加内部节点
		for(CircuitBlockSingle singleBlock:singleBlockList) {
			for(CircuitPort port:singleBlock.getCurcuitPortList()) {
				addNodeList(port.getCircuitNode());
			}
		}
		
		//内部节点列表应该去除外部节点
		for(CircuitNode node:exNodeList) {
			intNodeList.remove(node);
		}
	}
	
	public void setId(int id) {
		this.id=id;
	}
	
	public String getCurrentEff(int x,int y) {
		return currentEff[x][y];
	}
	
	public String getIAEff(int x) {
		return iAEff[x];
	}
	
	public int getId() {
		return this.id;
	}
	
	//每个part的prefix格式
	public String getPrefix() {
		return "Part"+id;
	}
	
	public Vector<CircuitBlock> getBlockList(){
		return this.blockList;
	}
	
	public Vector<CircuitBlockSingle> getSingleBlockList(){
		return this.singleBlockList;
	}
	
	public Vector<CircuitNode> getExNodeList(){
		return this.exNodeList;
	}
	
	public Vector<CircuitNode> getExIntNodeList(){
		return this.exIntNodeList;
	}
	
	public Vector<CircuitNode> getIntNodeList(){
		return this.intNodeList;
	}
	
	public Vector<CircuitBlockMulti> getMultiBlockList(){
		return this.multiBlockList;
	}
	
	//将part合并到本part中去
	public void combinePartition(CircuitPartition part) {
		//合并各个list
		blockList.addAll(part.getBlockList());
		singleBlockList.addAll(part.getSingleBlockList());
		multiBlockList.addAll(part.getMultiBlockList());
		intNodeList.addAll(part.getIntNodeList());
		
		//合并外部node的list。合并方法，首先互相遍历合并part的exNodelist，如果有重复，则需要遍历；让后判断，如果两个node合并，如果没有其他的对外连接，则这合并后的node是一个内部node,否则还是一个外部node
		for(CircuitNode node:part.getExNodeList()) {
			//如果这是个公共Node，则需要合并
			if(exNodeList.contains(node)) {
				boolean externalNode=false;
				//遍历这个node连接的所有blcok，如果有block不是blockList中的，则说明有外部连接
				for(CircuitPort port:node.getCircuitPortList()) {
					if(singleBlockList.contains(port.getBlock())==false) {
						externalNode=true;
						break;
					}
				}
				//如果有外部连接，说明合并之后还是外部连接，什么都不用做
				if(externalNode) {
					//exNodeList.add(node);
				}
				//如果没有外部连接，则说明连接后成为了一个内部连接
				else {
					intNodeList.add(node);
					exNodeList.remove(node);
				}
			}
			//如果不是公共Node，说明是个外部的Node
			else {
				exNodeList.add(node);
			}
		}
	}
	
	//part合并之后，要进行的一些善后处理
	public void partitionSetup() {
		//给所有的exNode，复制一份exIntNode，作为这些节点在这个part中的副本
		for(CircuitNode exNode:exNodeList) {
			CircuitNode exIntNode=new CircuitNode(exNode.getNodeId());
			//复制各个内部特性
			exIntNode.setIsRef(exNode.getIsRef());
			
			for(CircuitPort port:exNode.getCircuitPortList()) {
				if(singleBlockList.contains(port.getBlock())) {
					exIntNode.addCircuitPort(port);
				}
			}
			
			//要将partition内部连接到exNode的port，重新指向exIntNode。这样part内部可以构成一个完整的网络（假设没有对外连接）
			for(CircuitBlock block:blockList) {
				for(CircuitPort port:block.getCurcuitPortList()) {
					if(port.getCircuitNode()==exNode) {
						port.setCircuitNode(exIntNode);
					}
				}
			}
			
			exIntNodeList.add(exIntNode);
			
			//外部连接指向这个part
			exNode.addPartition(this);
		}
		
		//给每个电压源赋予part内部id
		int partVsId=0;
		for(CircuitBlockSingle block:singleBlockList) {
			if(block.getBlockModeType()==BlockModeType.VoltageSource) {
				vsBlockList.add(block);
				((VoltageSource)block).setPartVsId(partVsId++);		
			}
		}
		
		//nodeList为part内所有的连接，intNodeList+exIntNodeList
		nodeList=new Vector<CircuitNode>();
		
		nodeList.addAll(intNodeList);
		nodeList.addAll(exIntNodeList);
		
		//为所有node赋予part内部id
		int partNodeId=0;
		for(CircuitNode node:nodeList) {
			node.setPartNodeId(partNodeId++);
		}
		
		//为所有switch的block赋予part内部id，并且加入switchBlockList
		int switchPartId=0;
		for(CircuitBlock block:singleBlockList) {
			if(block instanceof SwitchBlock) {
				switchBlockList.add(block);
				((SwitchBlock) block).setSwitchPartId(switchPartId++);
			}
		}
		
		for(CircuitBlock block:singleBlockList) {
			if(block instanceof SwitchBlock &&((SwitchBlock)block).isDynamic()) {
				dynamicSwitchBlockList.add(block);
			}
		}
		
		//将所有的变参数block,加入variableBlockList
		int variableId=0;
		for(CircuitBlock block:singleBlockList) {
			if(block instanceof VariableBlock) {
				variableBlockList.add(block);
				//((VariableBlock) block).setVariableBlockId(variableId++);
			}
		}
		
		//获得分区下的复合器件状态变化代码
		for(CircuitBlockMulti block:multiBlockList) {
			block.setupLogicCode(id);
		}
		
		//currentEff矩阵的大小等于外部连接的个数.每个外部节点的电压（参考节点除外）都受到各位外部节点流出电流（流出参考节点的电流除外）影响
		currentEff=new String[exNodeList.size()][exNodeList.size()];
		//IAEff矩阵的大小等于外部连接的个数
		iAEff=new String[exNodeList.size()];
		for(int i=0;i<exNodeList.size();i++) {
			iAEff[i]=getPrefix()+"_iAEff_"+i;
			for(int j=0;j<exNodeList.size();j++) {
				currentEff[i][j]=getPrefix()+"_cEff_"+i+"_"+j;
			}
		}
	}
	
	//增加一个内部节点node
	private void addNodeList(CircuitNode nodeAdd) {
		//增加的时候避免重复
		for(CircuitNode node:intNodeList) {
			if(node==nodeAdd) {
				return;
			}
		}
		
		intNodeList.add(nodeAdd);
	}
	
	//获得运行过程中频繁变化的开关个数。虽然电阻为0的时候也等同于开关闭合，但是电阻值不会在运行中变化，所以不是DynamicSwitch。可变电阻和开关是DynamicSwitch
	public int getDynamicSwitchNum() {
		int num=0;
		
		for(CircuitBlockSingle block:singleBlockList) {
			if(block instanceof SwitchBlock &&((SwitchBlock)block).isDynamic()) {
				num++;
			}
		}
		
		return num;
	}
	
	private void setPartRef() {
		refNodeList=new Vector<CircuitNode>();
		
		for(CircuitNode node:nodeList) {
			if(node.getIsRef()) {
				refNodeList.add(node);
				node.setIsPartRef(true);
			}
		}
		
		if(refNodeList.size()==0) {
			CircuitNode refNode=exIntNodeList.get(0);
			refNodeList.add(refNode);
			refNode.setIsPartRef(true);
		}
	}
	
	//初始化Partition的数据结构
	public String getGAAInitCode(int n) {
		String code="/*GAA Init code for part"+id+" */\n";
		
		code+=getPrefix()+"_getUpdatedGAA("+getPrefix()+"_gAAOriginal);\n";
		code+=getPrefix()+"_getUpdatedIA("+getPrefix()+"_iAOriginal);\n";
		code+=getPrefix()+"_switchGAA.gAAOriginal="+getPrefix()+"_gAAOriginal;\n";
		code+=getPrefix()+"_switchGAA.sizeOriginal="+gAA.length+";\n";
		code+=getPrefix()+"_switchGAA.switchNum="+(switchBlockList.size())+";\n";
		code+=getPrefix()+"_switchGAA.switchStatus="+getPrefix()+"_switchStatus;\n";
		code+=getPrefix()+"_switchGAA.storeGAASize=0;\n";
		code+=getPrefix()+"_switchGAA.storeGAA=(StoreGAA *)malloc(0);\n";
		
		code+="partitioner.partitions["+n+"].pSwitchGaa=&"+getPrefix()+"_switchGAA;\n";
		code+="partitioner.partitions["+n+"].iAOriginal="+getPrefix()+"_iAOriginal;\n";
		code+="partitioner.partitions["+n+"].refSize="+getPrefix()+"_refSize;\n";
		code+="partitioner.partitions["+n+"].refs="+getPrefix()+"_refs;\n";
		code+="partitioner.partitions["+n+"].size="+this.gAA.length+";\n";
		
		code+="partitioner.partitions["+n+"].vIndex="+getPrefix()+"_vIndex;\n";
		code+="partitioner.partitions["+n+"].vIndexOriginal="+getPrefix()+"_vIndexOriginal;\n";
		
		code+="partitioner.partitions["+n+"].getUpdatedGAA="+getPrefix()+"_getUpdatedGAA;\n";
		code+="partitioner.partitions["+n+"].getUpdatedIA="+getPrefix()+"_getUpdatedIA;\n";
		
		code+="addSwitchCombine(&"+getPrefix()+"_switchGAA,"+getPrefix()+"_switchGAA.gAAOriginal,"+switchBlockList.size()+","+gAA.length+","+getPrefix()+"_switchGAA.switchStatus);\n";
		
		//code+="//startCircuitThread(circuitThreads,CIRCUIT_THREAD_NUM);\n";
		
		return code;
	}
	
	public void generate() {
		this.setPartRef();
		this.createGAA();
		//System.out.println(generateCircuitDefineCode());
	}
	
	public String getCircuitDefineCode() {
		String circuitDefineCode="";
		circuitDefineCode+="/*Define circuit variables for Part "+id+"*/\n";
		
		for(int i=0;i<exNodeList.size();i++) {
			for(int j=0;j<exNodeList.size();j++) {
				circuitDefineCode+="REAL "+currentEff[i][j]+";\n";
			}
		}
		
		for(int i=0;i<exNodeList.size();i++) {
			circuitDefineCode+="REAL "+iAEff[i]+";\n";
		}
		
		circuitDefineCode+="/*Define circuit variables for Nodes*/\n";
		for(CircuitNode node:nodeList) {
			circuitDefineCode+="REAL "+this.getPrefix()+"_"+node.getNodeString()+"; /*";
			
			for(CircuitPort port:node.getCircuitPortList()) {
				circuitDefineCode+=port.getBlock().getBlockName()+"\t";
			}
			
			circuitDefineCode+="*/\n";
		}
		circuitDefineCode+="/*Define circuit variables for Blocks*/\n";
		for(CircuitBlockSingle block:this.getSingleBlockList()) {
//			String str=block.getHisString();
//			if(str.equals("0")==false){
//				circuitDefineCode+="REAL "+block.getHisString()+"=0;\n";
//			}
			//circuitDefineCode+=block.generateHisStringCode(this.getPrefix());
			circuitDefineCode+="REAL "+this.getPrefix()+"_"+block.getCurrentString()+";\n";
		}
		
		circuitDefineCode+="uint32_T "+this.getPrefix()+"_refSize="+this.refNodeList.size()+";\n";
		circuitDefineCode+="uint32_T "+this.getPrefix()+"_refs["+this.refNodeList.size()+"]={";
		for(int i = 0;i<refNodeList.size()-1;++i) {
			circuitDefineCode+=refNodeList.get(i).getNodeId()+",";
		}
		circuitDefineCode+=refNodeList.get(refNodeList.size()-1).getNodeId()+"};\n";
		
		circuitDefineCode+="int "+this.getPrefix()+"_vIndexOriginal["+gAA.length+"]={";
		for(int i=0;i<gAA.length;i++) {
			if(i==0) {
				circuitDefineCode+=i;
			}
			else {
				circuitDefineCode+=","+i;
			}
		}
		circuitDefineCode+="};\n";
		
		circuitDefineCode+="int "+this.getPrefix()+"_vIndex["+gAA.length+"];\n";
		
		//circuitDefineCode+=getVariableDefineCode();
		
		circuitDefineCode+=getGAADefineCode();
		
		circuitDefineCode+=getPartitionOutputCode();
		
		return circuitDefineCode;
	}
	
	private String getCircuitStatusDefineCode() {

		String circuitStatusDefineCode="/*Circuit status defined code for part"+id+"*/\n";
		for(CircuitBlock block:blockList) {
			//circuitStatusDefineCode+=block.getCircuitStatusDefineCode();
			circuitStatusDefineCode+=block.getCircuitStatusDefineCode().replace("EBlock",getPrefix()+"_EBlock");
		}
		return circuitStatusDefineCode;
	}
	
	private String exNodeCurrentString[];
	
	public String getExNodeCurrentString(int x) {
		return this.exNodeCurrentString[x];
	}
	
	private String volOffsetString;
	
	public String getVolOffsetString() {
		return this.volOffsetString;
	}
	
	public String getPartitionResultCode() {
		String code="/*Partition result code for part"+id+"*/\n";
		return code;
	}
	
	public String getMultiBlockCode() {
		
		String code="/*MultiBlock cocd for part"+id+"*/\n";
		
		for(CircuitBlockMulti block:multiBlockList) {
			if(block instanceof Recalc) {
				Recalc rBlock=(Recalc)block;		
				code+=rBlock.getIsRecalcCode().replace("EBlock", getPrefix()+"_EBlock").replace("V",getPrefix()+"_V");
			}
		}
		
		return code;
	}
	
	private String getInterDefineCode() {
		String code="/*External Current for part"+id+"*/\n";
		
		exNodeCurrentString=new String[exNodeList.size()];
		
		for(int i=0;i<exNodeList.size();i++) {
			exNodeCurrentString[i]=getPrefix()+"_ExtCurrent"+i;
			
			code +="REAL "+exNodeCurrentString[i]+";\n";
		}
		
		volOffsetString=getPrefix()+"_VolOffset";
		code+="REAL "+volOffsetString+";\n";
		
		//code+="gsl_vector *"+getPrefix()+"_x;\n";
		code+="gsl_matrix *"+getPrefix()+"_inv;\n";
		
		return code;
	}
	
	private String getPartitionOutputCode() {
		String code="/*Circuit Output code for Part"+id+"*/\n";
		
		// 模块中一些状态变量的定义，一般用static开头
		code += this.getCircuitStatusDefineCode();
		
		code+=getInterDefineCode();
		
		for(CircuitNode node:nodeList) {
			code+="int "+getPrefix()+"_isRef"+node.getPartNodeId()+";\n";
		}
		
		code+="void "+getPrefix()+"_CircuitOutputCode(){\n";
		
		code+="int refSize = "+this.refNodeList.size()+";\n";
		code+="int ref["+this.refNodeList.size()+"]={";
		for(int i = 0;i<refNodeList.size()-1;++i) {
			code+=refNodeList.get(i).getPartNodeId()+",";
		}
		code+=refNodeList.get(refNodeList.size()-1).getPartNodeId()+"};\n";
		
		// size是矩阵的大小，节点个数+电压源个数
		code += "int size=" + gAA.length + ";\n";
		// 把原始矩阵大小记录下来，开关器件会动态改变矩阵的规模
		code += "int oldSize=" + gAA.length + ";\n";
		
		//把原始节点与结果向量之间的元素对应起来，记录在vIndex中，初始的时候依次赋值即可
		/*
		code += "int vIndex[" + gAA.length + "]={";
		for (int i = 0; i < gAA.length; i++) {
			if (i == 0) {
				code += i;
			} else {
				code += "," + i;
			}
		}
		code += "};\n";*/
		
		code+="int *vIndex="+getPrefix()+"_vIndex;\n";
		code+="memcpy(vIndex,"+getPrefix()+"_vIndexOriginal,"+gAA.length+"*sizeof(uint32_T));\n";
		
		code+="REAL *gAAc="+getPrefix()+"_gAAOriginal;\n";
		
		// 构建原始iA向量iAc
		code += "REAL iAc[" + iA.length + "]={\n";
		for (int i = 0; i < iA.length; i++) {
			code += iA[i];
			if (i != iA.length - 1) {
				code += ",";
			}
		}
		code += "};\n";
		
		//构建工作矩阵
		code+="REAL gAA["+gAA.length*gAA.length+"];\n";
		code+="REAL iA["+iA.length+"];\n";
		
		
		
		code += "int " + com.ncslab.circuit2.block.element.VariableBlock.isVariableChanged + "=0;\n";
		code += this.getVariableChangeCode();
		
		// 是否需要重计算的标志Recalc
		//code += "int " + Recalc.isRecalcString + "=0;\n";

		// 生成根据开关器件状态，合并矩阵Node的判断代码
		code += this.getSwitchCombineCode();
		
		//code+="printf(\"Part "+id+" Switch Status:%x\\n \",partitioner.partitions["+id+"].pSwitchGaa->switchStatus[0]);\n";
		
		//调用求解行列式代码，求解节点电压
		code+=this.getCircuitSolveCode();
		
		code+=getPrefix()+"_inv=inv;\n";
		
		/*
		//遍历所有的复合节点，生成是否需要重计算的判断代码
		for (CircuitBlockMulti block : multiBlockList) {
			if (block instanceof Recalc) {
				Recalc rBlock = (Recalc) block;
				code += rBlock.getIsRecalcCode();
			}
		}*/

		code+="}\n";
		
		code+="void "+getPrefix()+"_CircuitOutputCodePost(){\n";
		
		int i=0;
		/*
		for(CircuitNode node:exIntNodeList) {
			code+=this.getExNodeCurrentString(i)+"*="+getPrefix()+"_isRef"+node.getPartNodeId()+";\n";
			i++;
		}*/
		
		/*
		for(CircuitNode node:nodeList) {
			code+="if("+getPrefix()+"_isRef"+node.getPartNodeId()+"==0){\n";
			code+=getPrefix()+"_"+node.getNodeString()+"+=((-"+this.getVolOffsetString()+")";
			i=0;
			for(CircuitNode exNode:exIntNodeList) {
				code+="+"+this.getExNodeCurrentString(i)+"*gsl_matrix_get("+getPrefix()+"_inv,"+getPrefix()+"_vIndex["+node.getPartNodeId()+"],"+getPrefix()+"_vIndex["+exNode.getPartNodeId()+"])";
				i++;
			}
			code+=");\n";
			code+="}\n";
			code+="else{\n";
			code+=getPrefix()+"_"+node.getNodeString()+"+=(-"+this.getVolOffsetString()+");\n";
			code+="}\n";
		}*/
		
		for(CircuitNode node:nodeList) {
			code+=getPrefix()+"_"+node.getNodeString()+"+=((-"+this.getVolOffsetString()+")";
			i=0;
			for(CircuitNode exNode:exIntNodeList) {
				code+="+(1-"+getPrefix()+"_isRef"+node.getPartNodeId()+")*(1-"+getPrefix()+"_isRef"+exNode.getPartNodeId()+")*"+this.getExNodeCurrentString(i)+"*gsl_matrix_get("+getPrefix()+"_inv,"+getPrefix()+"_vIndex["+node.getPartNodeId()+"],"+getPrefix()+"_vIndex["+exNode.getPartNodeId()+"])";
				i++;
			}
			code+=");\n";
			
		}
		
		for(CircuitNode node:intNodeList) {
			code+=node.getNodeString()+"="+getPrefix()+"_"+node.getNodeString()+";\n";
		}
		
		for(CircuitBlock block:vsBlockList) {
			VoltageSource vsBlock=(VoltageSource)block;
			code+=getPrefix()+"_"+vsBlock.getCurrentString()+"+=(0";
			i=0;
			for(CircuitNode exNode:exIntNodeList) {
				
				code+="+"+this.getExNodeCurrentString(i)+"*gsl_matrix_get("+getPrefix()+"_inv,"+getPrefix()+"_vIndex["+vsBlock.getPartCurrentId()+"],"+getPrefix()+"_vIndex["+exNode.getPartNodeId()+"])";
				i++;
			}
			
			code+=");\n";
			//circuitOutputCode+=getPrefix()+"_"+vsBlock.getCurrentString()+"=gsl_vector_get(x,vIndex["+vsBlock.getPartCurrentId()+"]);\n";		
		}
		
		for(CircuitBlock block:vsBlockList) {
			VoltageSource vsBlock=(VoltageSource)block;
			code+=vsBlock.getCurrentString()+"="+getPrefix()+"_"+vsBlock.getCurrentString()+";\n";
		}
		
		code+="}\n";
		return code;
	}
	
	private String getCircuitSolveCode() {
		String circuitOutputCode="/*Circuit Solver Code for part"+id+"*/\n";
		
		circuitOutputCode+="//寻找能否在表格结构中找到现在开关状态的逆阵\n";
		circuitOutputCode+="StoreGAA *pStoreGaa=findStoreGAA(partitioner.partitions["+id+"].pSwitchGaa,partitioner.partitions["+id+"].pSwitchGaa->switchStatus);\n";
		circuitOutputCode+="gsl_matrix *inv=NULL;\n";
		circuitOutputCode+="//如果找到了逆阵\n";
		circuitOutputCode+="if(pStoreGaa!=NULL){\n";
		circuitOutputCode+="//检查找到的逆阵数据结构里面的变量变换的标志，如果没有变化，就不需要重新计算\n";
		circuitOutputCode+="if(pStoreGaa->isVariableChanged==0){\n";
		circuitOutputCode+="//赋值逆阵inv,准备计算\n";
		circuitOutputCode+="inv=pStoreGaa->inv;\n";
		circuitOutputCode+="//赋值最新的ia向量,准备计算\n";
		circuitOutputCode+="copyCircuitVector(iA,iAc,size);\n";
		
		circuitOutputCode+="//根据开关状态,合并iA和索引矩阵\n";
		for(CircuitBlock block:switchBlockList) {
			SwitchBlock swBlock=(SwitchBlock)(block);
			int switchId=swBlock.getSwitchPartId();
			circuitOutputCode+="/*Circuit Combine Ia for "+block.getBlockName()+"*/\n";
			circuitOutputCode+="if(getSwtichStatus(partitioner.partitions["+id+"].pSwitchGaa,"+switchId+")){\n";
			circuitOutputCode+="CircuitCombineIA(iA,vIndex,&size,ref,"+block.getCurcuitPortList().get(0).getCircuitNode().getPartNodeId()+","+block.getCurcuitPortList().get(1).getCircuitNode().getPartNodeId()+",oldSize,refSize);\n";
			circuitOutputCode+="}\n";
		}
		
		circuitOutputCode+="size=pStoreGaa->size;\n";
		circuitOutputCode+="}\n";
		circuitOutputCode+="//如果变量变化的标志为1，说明有变量变化，需要重新计算逆阵\n";
		circuitOutputCode+="else{\n";
		circuitOutputCode+="//根据新的参数，获得最新的gAA,放在gAAOriginal，gAAOriginal和gAAc指向同一个地址\n";
		circuitOutputCode+=getPrefix()+"_getUpdatedGAA("+getPrefix()+"_gAAOriginal);\n";
		circuitOutputCode+="//拷贝gAA和Ia矩阵，为开关归并做准备\n";
		circuitOutputCode+="copyCircuitMartrix(gAA,gAAc,iA,iAc,size);\n";
		
		circuitOutputCode+="//根据开关状态归并各个矩阵和向量\n";
		for(CircuitBlock block:switchBlockList) {
			SwitchBlock swBlock=(SwitchBlock)(block);
			int switchId=swBlock.getSwitchPartId();
			circuitOutputCode+="/*Circuit Combine for "+block.getBlockName()+"*/\n";
			circuitOutputCode+="if(getSwtichStatus(partitioner.partitions["+id+"].pSwitchGaa,"+switchId+")){\n";
			circuitOutputCode+="CircuitCombine(gAA,iA,vIndex,&size,ref,"+block.getCurcuitPortList().get(0).getCircuitNode().getPartNodeId()+","+block.getCurcuitPortList().get(1).getCircuitNode().getPartNodeId()+",oldSize,refSize);\n";
			circuitOutputCode+="}\n";
		}
		
		circuitOutputCode+="//更新新的inv，放入表格中\n";
		circuitOutputCode+="inv=caclulateInv(pStoreGaa,gAA,size);\n";
		circuitOutputCode+="pStoreGaa->isVariableChanged=0;\n";
		
		circuitOutputCode+="}\n";
		circuitOutputCode+="}\n";
		circuitOutputCode+="//如果没有找到逆阵\n";
		circuitOutputCode+="else{\n";
		circuitOutputCode+="//根据新的参数，获得最新的gAA,放在gAAOriginal，gAAOriginal和gAAc指向同一个地址\n";
		circuitOutputCode+=getPrefix()+"_getUpdatedGAA("+getPrefix()+"_gAAOriginal);\n";
		circuitOutputCode+="//拷贝gAA和Ia矩阵，为开关归并做准备\n";
		circuitOutputCode+="copyCircuitMartrix(gAA,gAAc,iA,iAc,size);\n";
		circuitOutputCode+="//根据开关状态归并各个矩阵和向量\n";
		for(CircuitBlock block:switchBlockList) {
			SwitchBlock swBlock=(SwitchBlock)(block);
			int switchId=swBlock.getSwitchPartId();
			circuitOutputCode+="/*Circuit Combine for "+block.getBlockName()+"*/\n";
			circuitOutputCode+="if(getSwtichStatus(partitioner.partitions["+id+"].pSwitchGaa,"+switchId+")){\n";
			circuitOutputCode+="CircuitCombine(gAA,iA,vIndex,&size,ref,"+block.getCurcuitPortList().get(0).getCircuitNode().getPartNodeId()+","+block.getCurcuitPortList().get(1).getCircuitNode().getPartNodeId()+",oldSize,refSize);\n";
			circuitOutputCode+="}\n";
		}
		circuitOutputCode+="//计算新的inv，添加到表格中\n";
		circuitOutputCode+="inv=addSwitchCombine(partitioner.partitions["+id+"].pSwitchGaa,gAA,"+switchBlockList.size()+",size,partitioner.partitions["+id+"].pSwitchGaa->switchStatus);\n";
		circuitOutputCode+="}\n";
		
		// 调用gsl库，求解行列式的值
		circuitOutputCode += "//矩阵和向量相乘，计算各个节点电压和其他未知数\n";
		circuitOutputCode += "gsl_vector_view b = gsl_vector_view_array (iA, size);\n";
		circuitOutputCode += "gsl_vector *x = gsl_vector_alloc (size);\n";
		circuitOutputCode+="gsl_blas_dgemv(CblasNoTrans, 1.0, inv, &b.vector, 0.0, x);\n";
		//circuitOutputCode+="printf(\"%f\\n\",gsl_vector_get(x,vIndex[1]));\n";
		
		int i=0;
		for(CircuitNode node:nodeList) {
			circuitOutputCode+=getPrefix()+"_"+node.getNodeString()+"=gsl_vector_get(x,vIndex["+(i++)+"]);\n";
		}
		
		for(CircuitBlock block:vsBlockList) {
			VoltageSource vsBlock=(VoltageSource)block;
			circuitOutputCode+=getPrefix()+"_"+vsBlock.getCurrentString()+"=gsl_vector_get(x,vIndex["+vsBlock.getPartCurrentId()+"]);\n";		
		}
		
		//circuitOutputCode+="for(int i=0;i<8;i++){\nprintf(\"%f\\t\",gsl_vector_get(x,vIndex[i]));\n}\n	printf(\"\t\\n\");\n";
		
		
		
		/*
		for(i=0;i<exIntNodeList.size();i++) {
			CircuitNode node1=exIntNodeList.get(i);
			for(int j=0;j<exIntNodeList.size();j++) {
				CircuitNode node2=exIntNodeList.get(j);
				if(node1.getIsPartRef()==false&&node2.getIsPartRef()==false) {
					circuitOutputCode+=currentEff[i][j]+"=gsl_matrix_get(inv,vIndex["+exIntNodeList.get(i).getPartNodeId()+"],vIndex["+exIntNodeList.get(j).getPartNodeId()+"]);\n";
				}
				else {
					circuitOutputCode+=currentEff[i][j]+"=0;\n";
				}
			}
		}*/
		
		for(CircuitNode node:nodeList) {
			circuitOutputCode+=getPrefix()+"_isRef"+node.getPartNodeId()+"=isPartRef("+node.getPartNodeId()+",ref,refSize,vIndex);\n";
		}
		
		for(i=0;i<exIntNodeList.size();i++) {
			CircuitNode node1=exIntNodeList.get(i);
			for(int j=0;j<exIntNodeList.size();j++) {
				CircuitNode node2=exIntNodeList.get(j);
				circuitOutputCode+="if("+getPrefix()+"_isRef"+node1.getPartNodeId()+"==0&&"+getPrefix()+"_isRef"+node2.getPartNodeId()+"==0){\n";
				circuitOutputCode+=currentEff[i][j]+"=gsl_matrix_get(inv,vIndex["+exIntNodeList.get(i).getPartNodeId()+"],vIndex["+exIntNodeList.get(j).getPartNodeId()+"]);\n";
				circuitOutputCode+="}\n";
				circuitOutputCode+="else{\n";
				circuitOutputCode+=currentEff[i][j]+"=0;\n";
				circuitOutputCode+="}\n";
			}
		}
		
		for(i=0;i<exIntNodeList.size();i++) {
			circuitOutputCode+=iAEff[i]+"=gsl_vector_get(x,vIndex["+exIntNodeList.get(i).getPartNodeId()+"]);\n";
		}
		
		circuitOutputCode+="gsl_vector_free (x);\n";
		
		/*
		for(CircuitNode node:exIntNodeList) {
			circuitOutputCode+=node.getNodeString()+";\n";
		}*/
		
		return circuitOutputCode;
	}
	
	private String getVariableDefineCode() {
		String code="/*Circuit Varialbe Define Code*/\n";
		for(CircuitBlock block:variableBlockList) {
			VariableBlock varBlock=(VariableBlock)block;
			code+=varBlock.getVariableDefineCode().replace("EBlock", this.getPrefix()+"_EBlock");
		}
		return code;
	}
	
	private String getGAADefineCode() {
		String code="/*GAA define code*/\n";
		
		code+="SwitchGAA "+this.getPrefix()+"_switchGAA;\n";
		
		code+="SwitchGAA *"+this.getPrefix()+"_psGaa=&"+this.getPrefix()+"_switchGAA;\n";
		
		code+="uint32_T "+this.getPrefix()+"_switchStatus["+(switchBlockList.size()/32+1)+"]={0};\n";
		
		code+="REAL "+this.getPrefix()+"_gAAOriginal["+gAA.length*gAA.length+"];\n";
		code+="REAL "+this.getPrefix()+"_iAOriginal["+gAA.length+"];\n";
		
		code+="void "+this.getPrefix()+"_getUpdatedGAA(REAL *gAA){\n";
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
		
		code+="memcpy(gAA,gAAValue,"+gAA.length*gAA.length+"*sizeof(REAL));\n";
		
		code+="}\n";
		
		code+="void "+this.getPrefix()+"_getUpdatedIA(REAL *iA){\n";
		code+="REAL iAValue["+iA.length+"]={\n";
		for(int i=0;i<iA.length;i++) {
			code+=iA[i];
			if(i!=iA.length-1) {
				code+=",";
			}
		}
		code+="};\n";
		code+="memcpy(iA,iAValue,"+iA.length+"*sizeof(REAL));\n";
		code+="}\n";
		
		code+="//CircuitThread circuitThreads[CIRCUIT_THREAD_NUM];\n";
		
		return code;
	}
	
	private long getSwitchStatus() {
		long switchStatus=0;
		long rate=1;
		for(CircuitBlock block:dynamicSwitchBlockList) {
			SwitchBlock swBlock=(SwitchBlock)block;
			if(swBlock.getSwitchStatus()) {
				switchStatus+=rate;
			}
			rate*=2;
		}
		
		return switchStatus;
	}
	
	public void calculateOutputs(double t) {
		boolean isVariableChanged=false;
		double[] iA=getIAValue();
		
		long switchStatus;
		
		for(CircuitBlock block:this.variableBlockList) {
			VariableBlock vBlock=(VariableBlock)block;
			if(vBlock.isVariableChanged()) {
				isVariableChanged=true;
			}
		}
		if(isVariableChanged) {
			switchGAA.setVariableChanged();
		}
		
		switchStatus=getSwitchStatus();
		
		StoreGAA storeGAA=switchGAA.findStoreGAA(switchStatus);
		if(storeGAA==null) {
			storeGAA=switchGAA.addStoreGAA(switchStatus);
		}
		
		double inv[][]=storeGAA.getInv();
		int[] vIndex=storeGAA.getVIndex();
		iA=storeGAA.getCombineIA(iA);
		
		RealMatrix invM=new Array2DRowRealMatrix(inv);
		RealMatrix iAV=new Array2DRowRealMatrix(iA);
		RealMatrix xM=invM.multiply(iAV);
		
		double[] x=xM.transpose().getData()[0];
		
		for(CircuitNode node:nodeList) {
			node.setPartVoltage(x[vIndex[node.getPartNodeId()]]);
		}
		
		for(CircuitNode node:nodeList) {
			node.setVoltage(x[vIndex[node.getPartNodeId()]]);
		}
		/*
		System.out.println(switchStatus);
		for(int i=0;i<x.length;i++) {
			System.out.print(vIndex[i]+"\t");
		}
		System.out.println();
		
		for(int i=0;i<x.length;i++) {
			System.out.print(x[i]+"\t");
		}
		System.out.println();*/
		
		/*
		for(int i=0;i<gAA.length;i++) {
			for(int j=0;j<gAA.length;j++) {
				System.out.print(gAA[i][j]+"\t");
			}
			System.out.println();
		}
		for(int i=0;i<iA.length;i++) {
			System.out.print(iA[i]+"\t");
		}
		System.out.println();
		System.out.println(isVariableChanged);
		System.out.println(switchStatus);
		System.out.println();*/
		
		
	}
	
	public void calculateInits(double tStart) {
		switchGAA=new SwitchGAA(this);
		switchGAA.setSwitchNum(this.dynamicSwitchBlockList.size());
	}
	
	private int mSize;
	private String[][] gAA;
	private String[] iA;
	
	public double[][] getGAAValue(){
		int vsStart=nodeList.size();
		mSize=nodeList.size()+vsBlockList.size();
		double[][] gAAValue=new double[mSize][mSize];
		
		for(CircuitNode node:nodeList) {
			//如果是参考节点,直接自身节点等于0
			if(node.getIsPartRef()) {
				gAAValue[node.getPartNodeId()][node.getPartNodeId()]=1.0;
			}
			else {
				for(CircuitPort port:node.getCircuitPortList()) {
					CircuitBlockSingle block=port.getBlock();
					if(block.getBlockModeType()==BlockModeType.Nromal) {
						CircuitNode nNode=block.getAnotherCircuitPort(port).getCircuitNode();
						gAAValue[node.getPartNodeId()][node.getPartNodeId()]+=-1.0/block.getRValue();
						gAAValue[node.getPartNodeId()][nNode.getPartNodeId()]+=1.0/block.getRValue();
					}
					else
					//如果是电压源,则访问电压源的电流作为未知数
					if(block.getBlockModeType()==BlockModeType.VoltageSource) {
						if(port.getCircuitPortType()==CircuitPortType.Left) {
							gAAValue[node.getPartNodeId()][vsStart+((VoltageSource)block).getPartVsId()]=-1.0;
						}
						else {
							gAAValue[node.getPartNodeId()][vsStart+((VoltageSource)block).getPartVsId()]=1.0;
						}
					}
					else
					//如果是电流源,则在iA中增加一项
					if(block instanceof CurrentSource) {
						CurrentSource cBlock=(CurrentSource)block;
						double sign=port.getCircuitPortType()==CircuitPortType.Left?(-1.0):(1.0);
						iA[node.getPartNodeId()]+=sign*cBlock.getIValue();
					}
					//如果是电流源,则在iA中增加一项
					//....
				}
			}
		}
		
		/*处理电压源,增加关于电压源的方程*/
		for(CircuitBlock block:vsBlockList) {
			VoltageSource vsBlock=(VoltageSource)block;
			vsBlock.setPartCurrentId(vsStart+((VoltageSource)block).getPartVsId());
			gAAValue[vsStart+((VoltageSource)block).getPartVsId()][vsBlock.getCurcuitPortList().get(0).getCircuitNode().getPartNodeId()]=1.0;
			gAAValue[vsStart+((VoltageSource)block).getPartVsId()][vsBlock.getCurcuitPortList().get(1).getCircuitNode().getPartNodeId()]=-1.0;
		}
		
		return gAAValue;
	}
	
	private double[] getIAValue(){
		int vsStart=nodeList.size();
		mSize=nodeList.size()+vsBlockList.size();
		double[] iAValue=new double[mSize];
		
		for(CircuitNode node:nodeList) {
			if(node.getIsPartRef()==false) {
				iAValue[node.getPartNodeId()]+=node.getHisValue();
			}
		}
		
		for(CircuitBlock block:vsBlockList) {
			VoltageSource vsBlock=(VoltageSource)block;
			vsBlock.setPartCurrentId(vsStart+((VoltageSource)block).getPartVsId());
			iAValue[vsStart+((VoltageSource)block).getPartVsId()]=vsBlock.getVValue();
		}
		
		return iAValue;
	}
	
//	private void getGAAValue() {
//		int vsStart=nodeList.size();
//		mSize=nodeList.size()+vsBlockList.size();
//		gAAValue=new double[mSize][mSize];
//		iAValue=new double[mSize];
//		
//		for(CircuitNode node:nodeList) {
//			//如果是参考节点,直接自身节点等于0
//			if(node.getIsPartRef()) {
//				gAAValue[node.getPartNodeId()][node.getPartNodeId()]=1.0;
//			}
//			else {
//				for(CircuitPort port:node.getCircuitPortList()) {
//					CircuitBlockSingle block=port.getBlock();
//					if(block.getBlockModeType()==BlockModeType.Nromal) {
//						CircuitNode nNode=block.getAnotherCircuitPort(port).getCircuitNode();
//						gAAValue[node.getPartNodeId()][node.getPartNodeId()]+=-1.0/block.getRValue();
//						gAAValue[node.getPartNodeId()][nNode.getPartNodeId()]+=1.0/block.getRValue();
//					}
//					else
//					//如果是电压源,则访问电压源的电流作为未知数
//					if(block.getBlockModeType()==BlockModeType.VoltageSource) {
//						if(port.getCircuitPortType()==CircuitPortType.Left) {
//							gAAValue[node.getPartNodeId()][vsStart+((VoltageSource)block).getPartVsId()]=-1.0;
//						}
//						else {
//							gAAValue[node.getPartNodeId()][vsStart+((VoltageSource)block).getPartVsId()]=1.0;
//						}
//					}
//					else
//					//如果是电流源,则在iA中增加一项
//					if(block instanceof CurrentSource) {
//						CurrentSource cBlock=(CurrentSource)block;
//						double sign=port.getCircuitPortType()==CircuitPortType.Left?(-1.0):(1.0);
//						iA[node.getPartNodeId()]+=sign*cBlock.getIValue();
//					}
//					//如果是电流源,则在iA中增加一项
//					//....
//				}
//				iA[node.getPartNodeId()]+="+"+node.getHisString();
//			}
//		}
//		
//		/*处理电压源,增加关于电压源的方程*/
//		for(CircuitBlock block:vsBlockList) {
//			VoltageSource vsBlock=(VoltageSource)block;
//			vsBlock.setPartCurrentId(vsStart+((VoltageSource)block).getPartVsId());
//			gAAValue[vsStart+((VoltageSource)block).getPartVsId()][vsBlock.getCurcuitPortList().get(0).getCircuitNode().getPartNodeId()]=1.0;
//			gAAValue[vsStart+((VoltageSource)block).getPartVsId()][vsBlock.getCurcuitPortList().get(1).getCircuitNode().getPartNodeId()]=-1.0;
//			iAValue[vsStart+((VoltageSource)block).getPartVsId()]=vsBlock.getVValue();
//		}
//		
//	}
	
	private void createGAA() {
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
			if(node.getIsPartRef()) {
				gAA[node.getPartNodeId()][node.getPartNodeId()]="1.0";
			}
			else {
				for(CircuitPort port:node.getCircuitPortList()) {
					CircuitBlockSingle block=port.getBlock();
					if(block.getBlockModeType()==BlockModeType.Nromal) {
						CircuitNode nNode=block.getAnotherCircuitPort(port).getCircuitNode();
						gAA[node.getPartNodeId()][node.getPartNodeId()]+="-1.0/("+block.getRString()+")";
						gAA[node.getPartNodeId()][nNode.getPartNodeId()]+="+1.0/("+block.getRString()+")";
					}
					else
					//如果是电压源,则访问电压源的电流作为未知数
					if(block.getBlockModeType()==BlockModeType.VoltageSource) {
						if(port.getCircuitPortType()==CircuitPortType.Left) {
							gAA[node.getPartNodeId()][vsStart+((VoltageSource)block).getPartVsId()]="-1.0";
						}
						else {
							gAA[node.getPartNodeId()][vsStart+((VoltageSource)block).getPartVsId()]="1.0";
						}
					}else					
					//如果是电流源,则在iA中增加一项
					if(block instanceof CurrentSource) {
						CurrentSource cBlock=(CurrentSource)block;
						String sign=port.getCircuitPortType()==CircuitPortType.Left?"(-1.0)":"(1.0)";
						iA[node.getPartNodeId()]+="+"+sign+"*("+cBlock.getIString()+")";
					}else 						
					//如果是运放输出端,结点电压受输入端电压差控制
					if(block.getBlockModeType() == BlockModeType.OpAmp && port.getName() == "RConn1") {
						int nodeId1 = block.getCurcuitPortList().get(0).getCircuitNode().getPartNodeId();
						int nodeId2 = block.getCurcuitPortList().get(2).getCircuitNode().getPartNodeId();
						String amp = ((OpAmp)block).getAmplitude();
						for(int i=0;i<mSize;++i) {
							gAA[node.getPartNodeId()][i]="0.0";
						}
						gAA[node.getPartNodeId()][node.getPartNodeId()] = "-1.0";
						gAA[node.getPartNodeId()][nodeId1] = "-1.0*"+amp;
						gAA[node.getPartNodeId()][nodeId2] = amp;
						break;
					}
				}
				iA[node.getPartNodeId()]+="+"+node.getHisString();
			}
		}
		
		/*处理电压源,增加关于电压源的方程*/
		for(CircuitBlock block:vsBlockList) {
			VoltageSource vsBlock=(VoltageSource)block;
			vsBlock.setPartCurrentId(vsStart+((VoltageSource)block).getPartVsId());
			gAA[vsStart+((VoltageSource)block).getPartVsId()][vsBlock.getCurcuitPortList().get(0).getCircuitNode().getPartNodeId()]="1.0";
			gAA[vsStart+((VoltageSource)block).getPartVsId()][vsBlock.getCurcuitPortList().get(1).getCircuitNode().getPartNodeId()]="-1.0";
			iA[vsStart+((VoltageSource)block).getPartVsId()]="("+vsBlock.getVString()+")";
		}
		
		System.out.println("Msize="+mSize+"\t"+intNodeList.size()+":"+vsBlockList.size());
		
		for(int i=0;i<mSize;i++) {
			for(int j=0;j<mSize;j++) {
				System.out.print(gAA[i][j]+"\t");
			}
			System.out.println(iA[i]);
		}
		
		for(CircuitBlockSingle block:singleBlockList) {
			System.out.print(block.getHisUpdateString()+"\t");
		}
		System.out.println();
	}
	
	
//	public String getVariableChangeCode() {
//		String code="/*Variable Change Code for Part"+this.id+"*/\n";
//		code+="partitioner.partitions["+id+"].isVariableChanged=0;\n";
//		
//		code+="/*Checking all the variable Blocks*/\n";
//		for(CircuitBlock block:variableBlockList) {
//			VariableBlock varBlock=(VariableBlock)block;
//			code+=varBlock.getVariableChangeCode().replace("isVariableChanged", "partitioner.partitions["+id+"].isVariableChanged");
//		}
//		
//		return code;
//	}
	
	private String getVariableChangeCode() {
		String code="/*Checking all the variable Blocks*/\n";
		for(CircuitBlock block:variableBlockList) {
			VariableBlock varBlock=(VariableBlock)block;
			code+=varBlock.getVariableChangeCode();
		}
		
		code+="/*Set variable change status for all the GAAStore*/\n";
		
		code+="if("+com.ncslab.circuit2.block.element.VariableBlock.isVariableChanged+"==1){\n";
		code+="for(int i=0;i<partitioner.partitions["+id+"].pSwitchGaa->storeGAASize;i++){\n";
		code+="partitioner.partitions["+id+"].pSwitchGaa->storeGAA[i].isVariableChanged=1;\n";
		code+="}\n";
		code+="}\n";
		
		return code;
	}
	
	//生成根据开关器件状态，合并矩阵Node的判断代码
	public String getSwitchCombineCode() {
		String code="/*Switch Combine Code for part"+id+"*/\n";
		
		for(CircuitBlock block:switchBlockList) {
			com.ncslab.circuit2.block.element.SwitchBlock switchBlock=(com.ncslab.circuit2.block.element.SwitchBlock)block;
			code+=switchBlock.getSwitchCode(this.id).replace("EBlock",getPrefix()+"_EBlock");
		}
		
		return code;
	}
	
	public void printPartion() {
		System.out.print(getDynamicSwitchNum()+":\t");
		for(CircuitBlock block:blockList) {
			System.out.print(block.getBlockName()+"\t");
		}
		
		System.out.print(":::\t");
		
		for(CircuitBlock block:singleBlockList) {
			System.out.print(block.getBlockName()+"\t");
		}
		
		System.out.print(":::\t");
		
		for(CircuitNode node:exNodeList) {
			System.out.print(node.getNodeString()+"\t");
		}
		
		System.out.print(":::\t");
		
		for(CircuitNode node:intNodeList) {
			System.out.print(node.getNodeString()+"\t");
		}
		
		System.out.println();
	}
}
