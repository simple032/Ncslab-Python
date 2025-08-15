package com.ncslab.circuit.loop;

import java.util.ArrayList;
import java.util.List;

import com.ncslab.block.Block;

import com.ncslab.circuit.block.electblock.ElectBlock;

public class TargetGroup {
	
	//用来计算的目标节点,是LoopPoint
	private LinearBlockElement targetElement;
	
	//所有前向通道集合
	private List<ForwardGroup> forwardGroupList = new ArrayList<>();
	
	private List<Block> relatedBlockList;
	
	public TargetGroup(LinearBlockElement targetElement) {
		this.targetElement=targetElement;
	}
	
	public void addForwardGroup(ForwardGroup forwardGroup) {
		forwardGroupList.add(forwardGroup);
	}
	
	public LinearBlockElement getTargetElement() {
		return this.targetElement;
	}
	
	public List<ForwardGroup> getForwardGroupList(){
		return this.forwardGroupList;
	}
	
	//寻找与解开loop代码相关的模块
	public void setupRelatedBlockList(){
		relatedBlockList=new ArrayList<>();
		for(ForwardGroup forwardGroup:forwardGroupList) {
			relatedBlockList.add(forwardGroup.getStartElement().getBlock());
		}	
	}
	
	//生成解开Loop的代码
	public void generateTargetGroupCode() throws CircuitLoopException{
		Block block=targetElement.getBlock();
		
		if(block instanceof ElectBlock) {
			ElectBlock electBlock=(ElectBlock)block;
			electBlock.setRelatedBlockList(relatedBlockList);
			
			//生成Loop的代码,放在这个LoopPoint上面
			electBlock.setElecLoopString(getTargetGroupString());
		}
		else {
			//应该抛出异常 
		}
	}
	
	public String getTargetGroupString() throws CircuitLoopException{
		String targerGroupString=targetElement.getBlock().getOutputPortVariable(0)+"=";
		
		boolean first=true;
		
		for(ForwardGroup forwardGroup:forwardGroupList) {
			if(first) {
				first=false;
			}
			else {
				targerGroupString+="+";
			}
			
			targerGroupString+=forwardGroup.getGroupString();
		}
		
		return targerGroupString;
	}
}
