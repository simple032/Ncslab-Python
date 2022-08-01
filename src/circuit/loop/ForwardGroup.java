package circuit.loop;

import java.util.Vector;

public class ForwardGroup {
	
	private LinearBlockElement startElement;
	private LinearBlockElement endElement;
	
	private Vector<Vector<LinearBlockElement>> loopList=new Vector<Vector<LinearBlockElement>>();
	
	private Vector<ForwardLine> forwardLineList=new Vector<ForwardLine>();
	
	public ForwardGroup(Vector<LinearBlockElement> forward,Vector<Vector<LinearBlockElement>> loopList) {
		startElement=forward.lastElement();
		endElement=forward.firstElement();
		//forwardList.add(forward);
		
		ForwardLine forwardLine=new ForwardLine(forward);
		forwardLineList.add(forwardLine);
		
		this.loopList=loopList;
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
	
	public LinearBlockElement getStartElement() {
		return this.startElement;
	}
	
	public String getGroupString() {
		String groupString="";
		
		String numString="";
		
		boolean first=true;
		
		for(ForwardLine forwardLine:forwardLineList) {
			if(first) {
				first=false;
			}
			else {
				numString+="+";
			}
			numString+=forwardLine.getForwardLineString();
		}
		
		groupString+=startElement.getOutputNameString()+"*("+numString+")";
		
		String denString="/(1";
		
		for(Vector<LinearBlockElement> loop:loopList) {
			denString+="-"+ForwardLine.getLineString(loop);
		}
		
		denString+=")";
		
		groupString+=denString;
				
		return groupString;
	}
}
