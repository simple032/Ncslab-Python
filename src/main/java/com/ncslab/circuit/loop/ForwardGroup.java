package com.ncslab.circuit.loop;

import java.util.ArrayList;
import java.util.List;

public class ForwardGroup {
	
	private LinearBlockElement startElement;
	private LinearBlockElement endElement;
	
	private List<List<LinearBlockElement>> loopList = new ArrayList<>();
	
	private List<ForwardLine> forwardLineList = new ArrayList<>();
	
	public ForwardGroup(List<LinearBlockElement> forward,List<List<LinearBlockElement>> loopList) {
		startElement=forward.get(forward.size() - 1);
		endElement=forward.get(0);
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
	
	public void addForward(List<LinearBlockElement> forward) {
		//forwardList.add(forward);
		
		ForwardLine forwardLine=new ForwardLine(forward);
		forwardLineList.add(forwardLine);
	}
	
	public List<ForwardLine> getForwardLineList(){
		return this.forwardLineList;
	}
	
	public LinearBlockElement getEndElement() {
		return this.endElement;
	}
	
	public LinearBlockElement getStartElement() {
		return this.startElement;
	}
	
	public String getGroupString() throws CircuitLoopException{
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
		
		for(List<LinearBlockElement> loop:loopList) {
			denString+="-"+ForwardLine.getLineString(loop);
		}
		
		denString+=")";
		
		groupString+=denString;
				
		return groupString;
	}
}
