package com.ncslab.circuit2.partition;

import java.util.Comparator;
import java.util.Vector;

import lombok.Getter;

//import com.sun.java.swing.plaf.windows.TMSchema.Part;

import com.ncslab.circuit2.CircuitModel2;
import com.ncslab.circuit2.block.CircuitBlock;
import com.ncslab.circuit2.block.baseelement.CircuitBlockMulti;
import com.ncslab.circuit2.block.baseelement.CircuitBlockSingle;
import com.ncslab.circuit2.line.CircuitLine;
import com.ncslab.ncslablink.ModelException;
import com.ncslab.ncslablink.NCSLabModel;

import com.ncslab.circuit2.block.io.CircuitNode;
import com.ncslab.circuit2.block.io.CircuitPort;
import com.ncslab.circuit2.block.multielement.Recalc;

public class CircuitPartitioner {
	
	static int MaxSwitchNum=6;
	
	
	
	CircuitModel2 model;
	
	private Vector<CircuitBlock> switchBlockList;
	private Vector<CircuitBlock> blockList;
	private Vector<CircuitBlockSingle> singleBlockList;
	private Vector<CircuitBlockMulti> multiBlockList;
	
	@Getter
	private Vector<CircuitPartition> partitionList=new Vector<CircuitPartition>();
	
	private Vector<CircuitPartition> sepPartitionList=new Vector<CircuitPartition>();
	
	private Vector<CircuitNode> nodeList=new Vector<CircuitNode>();
	
	private int voltageStart;
	
	private int[] currentStarts;
	
	private int cStart;
	
	private String[][] gAA;
	
	private String[] iA;
	
	private int mSize;
	
	CircuitPartitioner(CircuitModel2 model){
		this.model=model;
		this.switchBlockList=model.getSwitchBlockList();
		this.blockList=model.getBlockList();
		initPartition();	
		partition();
	}
	
	public static CircuitPartitioner CreateCircuitPartitioner(CircuitModel2 model){
		return new CircuitPartitioner(model);
	}
	
	private void initPartition() {
		for(CircuitBlock block:blockList) {
			CircuitPartition partition=new CircuitPartition(block);
			partitionList.add(partition);
		}
	}
	
	//计算矩阵的大小mSize
	//mSize=node个数（V）+partition个数(c)+各个Partition的对外current的个数（current以对外流出为正，对外current的个数就是exNode的个数）
	private void getStarts() {
		voltageStart=0;
		
		cStart=nodeList.size();
		
		currentStarts=new int[partitionList.size()];
		
		currentStarts[0]=cStart+partitionList.size();
		mSize=currentStarts[0]+partitionList.get(0).getExNodeList().size();
		
		for(int i=1;i<partitionList.size();i++) {
			currentStarts[i]=mSize;
			mSize=currentStarts[i]+partitionList.get(i).getExNodeList().size();
		}
		
	}
	
	private void partition() { 

		
		printPartitions();
		
		//进行分割
		partitionStep();
		
		//设置相应的数据结构连接
		int id=0;
		for(CircuitPartition part:partitionList) {
			part.setId(id++);
			part.partitionSetup();
			
			for(CircuitNode node:part.getExNodeList()) {
				if(nodeList.contains(node)==false) {
					nodeList.add(node);
				}
			}
		}
		
		//计算矩阵的大小mSize，mSize=node个数（V）+partition个数(c)+各个Partition的对外current的个数（current以对外流出为正，对外current的个数就是exNode的个数）
		getStarts();
		
		printPartitions();
		
		int i=0;
		for(CircuitPartition part:partitionList) {
			System.out.println("Generating Part"+(i++));
			part.generate();
		}
		
		createGaa();
	}
	
	@Getter
	private double[][] gAAValue;
	@Getter
	private double[] iAValue;
	
	boolean isPrint=true;
	
	public void createGaaValue(){
		//gAAValue=new double[mSize][mSize];
		//iAValue=new double[mSize];
		
		int lineNum=0;
		int partNum=0;
		for(CircuitPartition part:partitionList) {
			int i=0;
			double[][] currentEffValue=part.getCurrentEffValue();
			for(CircuitNode node:part.getExNodeList()) {
				gAAValue[lineNum][node.getPartitionerNodeId()]=1.0;
				gAAValue[lineNum][cStart+partNum]=1.0;
				int j=0;
				for(CircuitNode node1:part.getExNodeList()) {
					gAAValue[lineNum][currentStarts[partNum]+j]=-currentEffValue[i][j];
					j++;
				}
				lineNum++;
				i++;
			}
			partNum++;
		}
		
		lineNum=0;
		for(CircuitPartition part:partitionList) {
			int i=0;
			double[] iAEffValue=part.getIAEffValue();
			for(CircuitNode node:part.getExNodeList()) {
				iAValue[lineNum]=iAEffValue[i];
				lineNum++;
				i++;
			}
			
		}
		
		partNum=0;
		for(CircuitPartition part:partitionList) {
			if(part.getExNodeList().isEmpty()) {
				partNum++;
				continue;
			}
			int i=0;
			for(CircuitNode node:part.getExNodeList()) {
				gAAValue[lineNum][currentStarts[partNum]+i]=1.0;
				i++;
			}
			lineNum++;
			partNum++;
		}
		
		for(int i=0;i<nodeList.size()-1;i++) {
			CircuitNode node=nodeList.get(i);
			partNum=0;
			for(CircuitPartition part:partitionList) {
				for(int j=0;j<part.getExNodeList().size();j++) {
					if(part.getExNodeList().get(j).getNodeId()==node.getNodeId()) {
						gAAValue[lineNum][currentStarts[partNum]+j]=1.0;
					}
				}
				partNum++;
			}
			lineNum++;
		}
		
		//一般有几个独立参考点，就会剩下几项（需要核查），参考点直接置0
		int i=0;
		while(lineNum<mSize) {
			gAAValue[lineNum][cStart+i]=1.0;
			lineNum++;
			i++;
		}
		
		/*
		if(isPrint) {
			isPrint=false;
			for(i=0;i<gAAValue.length;i++) {
				for(int j=0;j<gAAValue.length;j++) {
					System.out.print(gAAValue[i][j]+"\t");
				}
				System.out.println();
			}
			System.out.println();
			for(i=0;i<gAAValue.length;i++) {
				System.out.print(iAValue[i]+"\t");
			}
			System.out.println();
		}*/
	}
	
	private void createGaa() {
		gAA=new String[mSize][mSize];
		
		iA=new String[mSize];
		
		gAAValue=new double[mSize][mSize];
		iAValue=new double[mSize];
		
		for(int i=0;i<mSize;i++) {
			iA[i]="0.0";
			for(int j=0;j<mSize;j++) {
				gAA[i][j]="0.0";
			}
		}
		
		int partitionerNodeId=0;
		for(CircuitNode node:nodeList) {
			node.setPartitionerNodeId(partitionerNodeId++);
		}
		
		int lineNum=0;
		int partNum=0;
		for(CircuitPartition part:partitionList) {
			int i=0;
			for(CircuitNode node:part.getExNodeList()) {
				gAA[lineNum][node.getPartitionerNodeId()]="1.0";
				gAA[lineNum][cStart+partNum]="1.0";
				int j=0;
				for(CircuitNode node1:part.getExNodeList()) {
					gAA[lineNum][currentStarts[partNum]+j]="-"+part.getCurrentEff(i, j);
					j++;
				}
				lineNum++;
				i++;
			}
			partNum++;
		}
		
		lineNum=0;
		for(CircuitPartition part:partitionList) {
			int i=0;
			for(CircuitNode node:part.getExNodeList()) {
				iA[lineNum]=part.getIAEff(i);
				lineNum++;
				i++;
			}
			
		}
		
		partNum=0;
		for(CircuitPartition part:partitionList) {
			if(part.getExNodeList().isEmpty()) {
				partNum++;
				continue;
			}
			int i=0;
			for(CircuitNode node:part.getExNodeList()) {
				gAA[lineNum][currentStarts[partNum]+i]="1.0";
				i++;
			}
			lineNum++;
			partNum++;
		}
		
		for(int i=0;i<nodeList.size()-1;i++) {
			CircuitNode node=nodeList.get(i);
			partNum=0;
			for(CircuitPartition part:partitionList) {
				for(int j=0;j<part.getExNodeList().size();j++) {
					if(part.getExNodeList().get(j).getNodeId()==node.getNodeId()) {
						gAA[lineNum][currentStarts[partNum]+j]="1.0";
					}
				}
				partNum++;
			}
			lineNum++;
		}
		
		//一般有几个独立参考点，就会剩下几项（需要核查），参考点直接置0
		int i=0;
		while(lineNum<mSize) {
			gAA[lineNum][cStart+i]="1.0";
			lineNum++;
			i++;
		}
	}
	
	//进行分区的合并
	private void partitionStep() {
		while(true) {
			//按照开关个数从大到小排序
			sortPartition();
			int partSize=partitionList.size();
			
			//找到最后的一个Partition
			CircuitPartition partCom=partitionList.get(partSize-1);
			//寻找可以合并的part，放在partC中
			CircuitPartition partC=null;
			//conNum=1,表示至少有一个连接，才能合并
			int conNum=1;
			//一个个遍历，寻找可以合并的part
			for(int i=0;i<partSize-1;i++) {
				CircuitPartition part=partitionList.get(i);
				
				int num=getConnections(part,partCom);
				
				//可以合并的条件，和现有part有更多的连接，并且合并之后开关数小于最大允许值
				if(num>=conNum&&(part.getDynamicSwitchNum()+partCom.getDynamicSwitchNum())<=MaxSwitchNum) {
					partC=part;
					conNum=num;
				}
				
				System.out.print(num+"\t");
			}	
			System.out.println();
			
			//如果找到了可以合并的part，就进行合并
			if(partC!=null) {
				combinePartitions(partCom,partC);
			}
			//如果找不到可以合并的part，说明partCom就是一个最终的part。把partCom移除，保存起来
			else {
				sepPartitionList.add(partCom);
				partitionList.remove(partCom);
				
				//如果没有可以合并的part，那就停止合并
				if(partitionList.size()==0) {
					break;
				}
			}
		}
		
		partitionList.addAll(sepPartitionList);
	}
	
	private void combinePartitions(CircuitPartition partA,CircuitPartition partB) {
		partA.combinePartition(partB);
		
		partitionList.remove(partB);
	}
	
	//获得两个part连接个数，寻找公共节点，公共节点就是连接点
	private int getConnections(CircuitPartition part1,CircuitPartition part2) {
		int num=0;
		for(CircuitNode node:part1.getExNodeList()) {
			if(part2.getExNodeList().contains(node)) {
				num++;
			}
		}
		return num;
	}
	
	class SwitchNumComparator implements Comparator<CircuitPartition> {
		@Override
		public int compare(CircuitPartition a, CircuitPartition b) {
		return Integer.compare(b.getDynamicSwitchNum(), a.getDynamicSwitchNum());
		}
	}
	
	private void sortPartition() {
		partitionList.sort(new SwitchNumComparator());
	}
	
	
	public void partitionerProcess(double[] x) {
		int vI=0;
		for(CircuitNode node:nodeList) {
			node.setVoltage(x[vI]);
			vI++;
		}
		
		
		for(CircuitPartition part:partitionList) {
			//code+=part.getPartitionResultCode();
			//计算各个公共端电流引起的电压变化
			for(int i=0;i<part.getExIntNodeList().size();i++) {
				//CircuitNode node=part.getExNodeList().get(i);
				//code+=part.getExNodeCurrentString(i)+"=gsl_vector_get (x,"+(this.currentStarts[part.getId()]+i)+");\n";
				int pos=this.currentStarts[part.getId()]+i;
				part.getExIntNodeList().get(i).setExNodeCurrentValue(x[pos]);
			}
			//获得电压偏移量
			//code+=part.getVolOffsetString()+"=gsl_vector_get (x,"+(this.cStart+part.getId())+");\n";
			part.setVolOffsetValue(x[this.cStart+part.getId()]);
		}
	}
	
	//生成分区计算的调用+合并计算的代码+合并之后返回各个分区计算的函数代码
	private String getPartitionerProcessCode() {
		String code="/*Partitioner Process code*/\n";
		//是否需要重新计算的代码
		code += "int " + Recalc.isRecalcString + "=0;\n";
		//合并计算代码的函数partitionerProcessCode()
		code+="void partitionerProcessCode(){\n";
		
		//分区计算的代码
		for(CircuitPartition part:partitionList) {
			code+=part.getPrefix()+"_CircuitOutputCode();\n";
		}
		
		//定义gAA
		code+="REAL gAA["+mSize*mSize+"]={\n";
		
		for(int i=0;i<mSize;i++) {
			for(int j=0;j<mSize;j++) {
				if(i==mSize-1&&j==mSize-1) {
					code+=gAA[i][j]+"\t";
				}
				else {
					code+=gAA[i][j]+",\t";
				}
				
			}
			code+="\n";
		}
		
		code+="};\n";
		
		//定义iA
		code+="REAL iA["+mSize+"]={";
		for(int i=0;i<mSize;i++) {
			if(i==mSize-1) {
				code+=iA[i]+"\t";
			}
			else {
				code+=iA[i]+",\t";
			}
		}
		code+="};\n";
		
		//重置重新计算标志
		code += Recalc.isRecalcString + "=0;\n";
		
		//求解线性方程的解
		code+="gsl_vector_view b = gsl_vector_view_array (iA, "+mSize+");\n";
		code+="gsl_vector *x = gsl_vector_alloc ("+mSize+");\n";

		code+="int s;\n";
		code+="gsl_matrix_view m = gsl_matrix_view_array (gAA, "+mSize+", "+mSize+");\n";
		code+="gsl_permutation * p = gsl_permutation_alloc ("+mSize+");\n";
		code+="gsl_linalg_LU_decomp (&m.matrix, p, &s);\n";
		code+="gsl_linalg_LU_solve (&m.matrix, p, &b.vector, x);\n";

		code+="gsl_permutation_free (p);\n";
		
		//获取公共node的电压值
		int vi=0;
		for(CircuitNode node:nodeList) {
			code+=node.getNodeString()+"=gsl_vector_get (x,"+vi+");\n";
			vi++;
		}
		
		//计算各个part的电压修正值，两个部分，一个是电压偏移量，另一个是公共端电流引起的电压变化
		for(CircuitPartition part:partitionList) {
			//code+=part.getPartitionResultCode();
			//计算各个公共端电流引起的电压变化
			code+="/*Partition offests code for part"+part.getId()+"*/\n";
			for(int i=0;i<part.getExIntNodeList().size();i++) {
				//CircuitNode node=part.getExNodeList().get(i);
				code+=part.getExNodeCurrentString(i)+"=gsl_vector_get (x,"+(this.currentStarts[part.getId()]+i)+");\n";
			}
			//获得电压偏移量
			code+=part.getVolOffsetString()+"=gsl_vector_get (x,"+(this.cStart+part.getId())+");\n";
		}
		
		code+="gsl_vector_free (x);\n";
		
		//根据part的电压修正值，反馈回各个part进行相应的修正
		for(CircuitPartition part:partitionList) {
			code+=part.getPrefix()+"_CircuitOutputCodePost();\n";
		}
		
		//计算各个复合器件的状态切换
		for(CircuitPartition part:partitionList) {
			code+=part.getMultiBlockCode();
		}
		
		code+="};\n";
		
		return code;
	}
	
	public String getCircuitDefineCode() {
		String circuitDefineCode="";
		circuitDefineCode+="/*Define circuit variables for Partitions*/\n";
		
		//定义Partition的数据结构
		circuitDefineCode+="Partition partition["+partitionList.size()+"];\n";
		circuitDefineCode+="Partitioner partitioner={"+partitionList.size()+",partition};\n";
		
		//定义各个Partition的变量，以partx_开头
		for(CircuitPartition part:partitionList) {
			circuitDefineCode+=part.getCircuitDefineCode();
		}
		
		//定义各个Partition计算的函数
		circuitDefineCode+=getPartitionerProcessCode();
		
		return circuitDefineCode;
	}
	
	public String getCircuitInitCode() {
		String code="/*Circuit init code for Partitions*/\n";
		
		//初始化各个Partition
		int i=0;
		for(CircuitPartition part:partitionList) {
			code+=part.getGAAInitCode(i);
			i++;
		}
		
		return code;
	}
	
	private String getVariableChangeCode() {
		String code="/*Variable Change code for Partitions*/\n";
		for(CircuitPartition part:partitionList) {
			//code+=part.getVariableChangeCode();
		}
		
		code+="/*Set variable change status for all the GAAStore in partitions*/\n";
		
		code+="for(int i=0;i<"+partitionList.size()+";i++){\n";
		code+="if(partitioner.partitions[i].isVariableChanged==1){\n";
		code+="for(int j=0;j<partitioner.partitions[i].pSwitchGaa->storeGAASize;j++){\n";
		code+="partitioner.partitions[i].pSwitchGaa->storeGAA[j].isVariableChanged=1;\n";
		code+="}\n";
		code+="}\n";
		code+="}\n";
		
		code+="\n";
		
		return code;
	}
	
	public String getCircuitOutputCode() {
		String code="/*Circuit output code for Partitions*/\n";
		/*
		code+=getVariableChangeCode();
		
		for(CircuitPartition part:partitionList) {
			code+=part.getSwitchCombineCode();
		}
		
		code+="for(int i=0;i<"+partitionList.size()+";i++){\n";
		
		code+="int refSize=partitioner.partitions[i].refSize;\n";
		
		code+="uint32_T *ref=partitioner.partitions[i].refs;\n";
		
		code+="int size=partitioner.partitions[i].size;\n";
		code+="int oldSize=partitioner.partitions[i].size;\n";
		
		code+="uint32_T *vIndex=partitioner.partitions[i].vIndex;\n";
		code+="uint32_T *vIndexOriginal=partitioner.partitions[i].vIndexOriginal;\n";
		code+="memcpy(vIndex,vIndexOriginal,sizeof(uint32_T)*size);\n";
		
		code+="REAL *gAAc=partitioner.partitions[i].pSwitchGaa->gAAOriginal;\n";
		
		code+="REAL *iAc=partitioner.partitions[i].iAOriginal;\n";
		code+="(*(partitioner.partitions[i].getUpdatedIA))(iAc);\n";
		
		code+="REAL gAA[size*size];\n";
		
		code+="REAL iA[size];\n";
		
		code+="}\n";*/
		
		
		
		
		//进行各个Partition的计算
		code+="partitionerProcessCode();\n";
		
		//如果需要重新计算，那么就重新算一下
		code+="if("+Recalc.isRecalcString+"){\n";
		code+="partitionerProcessCode();\n";
		code+="}\n";
		
		
		return code;
	}
	
	private void printPartitions() {
		for(CircuitPartition partition:partitionList) {
			partition.printPartion();
		}
	}
}
