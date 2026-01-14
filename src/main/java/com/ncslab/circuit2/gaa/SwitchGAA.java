package com.ncslab.circuit2.gaa;

import java.util.Vector;

import lombok.Getter;
import lombok.Setter;

import com.ncslab.circuit2.partition.CircuitPartition;

public class SwitchGAA {
	private int sizeOriginal; //原始的矩阵大小
	@Getter
	@Setter
	private int switchNum; //开关的个数
	private int storeGAASize; //逆阵表的大小
	
	@Getter
	@Setter
	private CircuitPartition part;
	
	private Vector<StoreGAA> storeGAAList=new Vector<StoreGAA>();
	
	public SwitchGAA(CircuitPartition part) {
		this.part=part;
	}
	
	public StoreGAA findStoreGAA(long switchStatus) {
		for(StoreGAA storeGAA:storeGAAList) {
			if(storeGAA.getSwitchStatus()==switchStatus) {
				if(storeGAA.isVariableChanged()) {
					double gAA[][]=part.getGAAValue();
					int[] vIndex=new int[gAA.length];
					for(int i=0;i<vIndex.length;i++) {
						vIndex[i]=i;
					}
					
					storeGAA.setVIndex(vIndex);
					storeGAA.setGAA(gAA);
					storeGAA.setPart(part);
					
					storeGAA.circuitCombine();
					storeGAA.setupInv();
					storeGAA.setVariableChanged(false);
					
				}
				return storeGAA;
			}
		}
		return null;
	}
	
	public void setVariableChanged() {
		for(StoreGAA storeGAA:storeGAAList) {
			storeGAA.setVariableChanged(true);
		}
	}
	
	public StoreGAA addStoreGAA(long switchStatus) {
		StoreGAA storeGAA=new StoreGAA();
		
		storeGAA.setSwitchStatus(switchStatus);
		
		double gAA[][]=part.getGAAValue();
		int[] vIndex=new int[gAA.length];
		for(int i=0;i<vIndex.length;i++) {
			vIndex[i]=i;
		}
		
		storeGAA.setVIndex(vIndex);
		storeGAA.setGAA(gAA);
		storeGAA.setPart(part);
		
		storeGAA.circuitCombine();
		storeGAA.setupInv();
		
		storeGAAList.add(storeGAA);
		
		return storeGAA;
	}
}
