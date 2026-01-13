package com.ncslab.circuit2.gaa;

import com.ncslab.circuit2.partition.CircuitPartition;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.math3.linear.*;

import com.ncslab.circuit2.block.CircuitBlock;
import com.ncslab.circuit2.block.element.SwitchBlock;
import com.ncslab.circuit2.block.io.CircuitNode;

public class StoreGAA {
	@Getter
	@Setter
	private long switchStatus;
	@Getter
	private double[][] inv;
	@Getter
	@Setter
	boolean isVariableChanged=false;
	@Getter
	@Setter
	private int[] vIndex;
	@Setter
	private double gAA[][];
	@Setter
	private CircuitPartition part;
	
	private double[] combineIAStep(double[] iA,CircuitNode from,CircuitNode to,int[] vIndex) {
		if(from.getIsPartRef()||to.getIsPartRef()) {
			if(to.getIsPartRef()) {
				CircuitNode temp;
				temp=from;
				from=to;
				to=temp;
			}
			int toId=to.getPartNodeId();
			int toIdV=vIndex[toId];
			
			int fromId=from.getPartNodeId();
			int refIdV=vIndex[fromId];
			
			vIndex[toId]=refIdV;
			for(int i=0;i<vIndex.length;i++) {
				if(vIndex[i]>toIdV) {
					vIndex[i]-=1;
				}
			}
			
			for(int i=toIdV;i<iA.length-1;i++){
				iA[i]=iA[i+1];
			}
			
			double[] iANew=new double[iA.length-1];
			for(int i=0;i<iANew.length;i++) {
				iANew[i]=iA[i];
			}
			iA=iANew;
		}
		else {
			int toId=to.getPartNodeId();
			int toIdV=vIndex[toId];
			
			int fromId=from.getPartNodeId();
			int fromIdV=vIndex[fromId];
			if(toIdV!=fromIdV) {
				if(toIdV<fromIdV) {
					int temp;
					temp=fromId;
					fromId=toId;
					toId=temp;
					temp=fromIdV;
					fromIdV=toIdV;
					toIdV=temp;
				}
				iA[fromIdV]+=iA[toIdV];
				
				for(int i=toIdV;i<iA.length-1;i++){
					iA[i]=iA[i+1];
				}
				
				double[] iANew=new double[iA.length-1];
				for(int i=0;i<iANew.length;i++) {
					iANew[i]=iA[i];
				}
				iA=iANew;
				
				vIndex[toId]=vIndex[fromId];
				for(int i=0;i<vIndex.length;i++) {
					if(vIndex[i]>toIdV) {
						vIndex[i]-=1;
					}
				}
			}
			
		}
		return iA;
	}
	
	private void combineStep(CircuitNode from,CircuitNode to) {
		//int fromId=from.getPartNodeId();
		//int toId=to.getPartNodeId();
		if(from.getIsPartRef()||to.getIsPartRef()) {
			if(to.getIsPartRef()) {
				CircuitNode temp;
				temp=from;
				from=to;
				to=temp;
			}
			int toId=to.getPartNodeId();
			int toIdV=vIndex[toId];
			
			int fromId=from.getPartNodeId();
			int refIdV=vIndex[fromId];
			
			vIndex[toId]=refIdV;
			for(int i=0;i<vIndex.length;i++) {
				if(vIndex[i]>toIdV) {
					vIndex[i]-=1;
				}
			}
			
			for(int i=toIdV;i<gAA.length-1;i++) {
				for(int j=0;j<gAA.length;j++) {
					gAA[i][j]=gAA[i+1][j];
				}
			}
			
			for(int i=toIdV;i<gAA.length-1;i++){
				for(int j=0;j<gAA.length-1;j++){
					gAA[j][i]=gAA[j][i+1];
				}
			}
			
			double[][] gAANew=new double[gAA.length-1][gAA.length-1];
			
			for(int i=0;i<gAANew.length;i++) {
				for(int j=0;j<gAANew.length;j++) {
					gAANew[i][j]=gAA[i][j];
				}
			}
			
			gAA=gAANew;
		}
		else {
			int toId=to.getPartNodeId();
			int toIdV=vIndex[toId];
			
			int fromId=from.getPartNodeId();
			int fromIdV=vIndex[fromId];
			if(toIdV!=fromIdV) {
				if(toIdV<fromIdV) {
					int temp;
					temp=fromId;
					fromId=toId;
					toId=temp;
					temp=fromIdV;
					fromIdV=toIdV;
					toIdV=temp;
				}
				for(int i=0;i<gAA.length;i++) {
					gAA[fromIdV][i]+=gAA[toIdV][i];
				}
				
				for(int i=0;i<gAA.length;i++){
					gAA[i][fromIdV]+=gAA[i][toIdV];
				}
				
				for(int i=toIdV;i<gAA.length-1;i++){
					for(int j=0;j<gAA.length;j++){
						gAA[i][j]=gAA[i+1][j];
					}
				}
				for(int i=toIdV;i<gAA.length-1;i++){
					for(int j=0;j<gAA.length-1;j++){
						gAA[j][i]=gAA[j][i+1];
					}
				}
				
				double[][] gAANew=new double[gAA.length-1][gAA.length-1];
				
				for(int i=0;i<gAANew.length;i++) {
					for(int j=0;j<gAANew.length;j++) {
						gAANew[i][j]=gAA[i][j];
					}
				}
				
				gAA=gAANew;
				
				vIndex[toId]=vIndex[fromId];
				for(int i=0;i<vIndex.length;i++) {
					if(vIndex[i]>toIdV) {
						vIndex[i]-=1;
					}
				}
			}				
		}
	}
	
	public void circuitCombine() {
		for(CircuitBlock block:part.getSwitchBlockList()) {
			SwitchBlock swBlock=(SwitchBlock)block;
			if(swBlock.getSwitchStatus()) {
				CircuitNode from=block.getCurcuitPortList().get(0).getCircuitNode();
				CircuitNode to=block.getCurcuitPortList().get(1).getCircuitNode();
				combineStep(from,to);
			}
		}
		
		/*
		System.out.println(switchStatus);
		
		for(int i=0;i<gAA.length;i++) {
			for(int j=0;j<gAA.length;j++) {
				System.out.print(gAA[i][j]+"\t");
			}
			System.out.println();
		}
		System.out.println();*/
	}
	
	
	
	public double[] getCombineIA(double[] iA) {
		int[] vIndex=new int[iA.length];
		for(int i=0;i<vIndex.length;i++) {
			vIndex[i]=i;
		}
		
		for(CircuitBlock block:part.getSwitchBlockList()) {
			SwitchBlock swBlock=(SwitchBlock)block;
			if(swBlock.getSwitchStatus()) {
				CircuitNode from=block.getCurcuitPortList().get(0).getCircuitNode();
				CircuitNode to=block.getCurcuitPortList().get(1).getCircuitNode();
				iA=combineIAStep(iA,from,to,vIndex);
			}
		}
		
		/*
		for(int i=0;i<iA.length;i++) 
		{
			System.out.print(iA[i]+"\t");
		}
		System.out.println();
		for(int i=0;i<iA.length;i++) {
			System.out.print(vIndex[i]+"\t");
		}
		System.out.println();*/
		
		return iA;
	}
	
	public void setupInv() {
		RealMatrix gAAM = new Array2DRowRealMatrix(gAA);
		RealMatrix invM = new LUDecomposition(gAAM).getSolver().getInverse();
		inv=invM.getData();
		
		/*
		for(int i=0;i<inv.length;i++) {
			for(int j=0;j<inv.length;j++) {
				System.out.print(inv[i][j]+"\t");
			}
			System.out.println();
		}
		System.out.println();*/
	}
}