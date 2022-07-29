package circuit.loop;

import java.util.Vector;

import block.Block;

import circuit.block.electblock.ElectBlock;

public class TargetGroup {
	private LinearBlockElement targetElement;
	
	private Vector<ForwardGroup> forwardGroupList=new Vector<ForwardGroup>();
	
	public TargetGroup(LinearBlockElement targetElement) {
		this.targetElement=targetElement;
	}
	
	public void addForwardGroup(ForwardGroup forwardGroup) {
		forwardGroupList.add(forwardGroup);
	}
	
	public LinearBlockElement getTargetElement() {
		return this.targetElement;
	}
	
	public Vector<ForwardGroup> getForwardGroupList(){
		return this.forwardGroupList;
	}
	
	public void setupRelatedBlockList(){
		Vector<Block> relatedBlockList=new Vector<Block>();
		for(ForwardGroup forwardGroup:forwardGroupList) {
			relatedBlockList.add(forwardGroup.getStartElement().getBlock());
		}
		
		Block block=targetElement.getBlock();
		
		if(block instanceof ElectBlock) {
			ElectBlock electBlock=(ElectBlock)block;
			electBlock.setRelatedBlockList(relatedBlockList);
			
			electBlock.setElecLoopString(getTargetGroupString());
		}
		else {
			//应该抛出异常 
		}
	}
	
	public String getTargetGroupString() {
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
