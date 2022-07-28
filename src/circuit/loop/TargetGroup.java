package circuit.loop;

import java.util.Vector;

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
	
	public String getTargetGroupString() {
		String targerGroupString="";
		
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
