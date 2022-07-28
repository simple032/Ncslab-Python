package circuit.loop;

import java.util.Vector;

public class ForwardGroup {
	
	private LinearBlockElement startElement;
	private LinearBlockElement endElement;
	
	//private Vector<Vector<LinearBlockElement>> forwardList=new Vector<Vector<LinearBlockElement>>();
	
	private Vector<ForwardLine> forwardLineList=new Vector<ForwardLine>();
	
	public ForwardGroup(Vector<LinearBlockElement> forward) {
		startElement=forward.lastElement();
		endElement=forward.firstElement();
		//forwardList.add(forward);
		
		ForwardLine forwardLine=new ForwardLine(forward);
		forwardLineList.add(forwardLine);
	}
	
	public boolean isSameStartEnd(LinearBlockElement startElement,LinearBlockElement endElement) {
		if(this.startElement.getBlock()==startElement.getBlock()&&this.endElement.getBlock()==endElement.getBlock()) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public void addForward(Vector<LinearBlockElement> forward) {
		//forwardList.add(forward);
		
		ForwardLine forwardLine=new ForwardLine(forward);
		forwardLineList.add(forwardLine);
	}
	
	public Vector<ForwardLine> getForwardLineList(){
		return this.forwardLineList;
	}
	
	public LinearBlockElement getEndElement() {
		return this.endElement;
	}
}
