package circuit.loop;

import java.util.Vector;

import block.Block;
import circuit.block.electblock.ElectBlock;

public class ForwardLine {
	private Vector<LinearBlockElement> forward=new Vector<LinearBlockElement>();
	
	private Vector<Vector<LinearBlockElement>> contactLoopList=new Vector<Vector<LinearBlockElement>>();
	
	private Vector<Vector<LinearBlockElement>> otherLoopList=new Vector<Vector<LinearBlockElement>>();
	
	public ForwardLine(Vector<LinearBlockElement> forward) {
		this.forward=forward;
	}
	
	public Vector<LinearBlockElement> getForward(){
		return this.forward;
	}
	
	private boolean isContact(Vector<LinearBlockElement> line1,Vector<LinearBlockElement> line2) {
		boolean isFound=false;
		for(LinearBlockElement element1:line1) {
			if(isFound) {
				break;
			}
			for(LinearBlockElement element2:line2) {
				if(element1.getBlock()==element2.getBlock()&&element1.getSign()==element2.getSign()) {
					isFound=true;
				}
			}
		}
		return isFound;
	}
	
	public void setupLoops(Vector<Vector<LinearBlockElement>> loopList) {
		for(Vector<LinearBlockElement> loop:loopList) {
			if(isContact(loop,forward)) {
				contactLoopList.add(loop);
			}
			else {
				otherLoopList.add(loop);
			}
		}
	}
	
	public Vector<Vector<LinearBlockElement>> getContactLoopList(){
		return this.contactLoopList;
	}
	
	public Vector<Vector<LinearBlockElement>> getOtherLoopList(){
		return this.otherLoopList;
	}
	
	public static String getLineString(Vector<LinearBlockElement> line) {
		String lineString="";
		
		boolean first=true;
		for(LinearBlockElement element:line) {
			Block block=element.getBlock();
			
			if(block instanceof ElectBlock) {
				if(first) {
					first=false;
				}
				else {
					lineString+="*";
				}
				ElectBlock electBlock=(ElectBlock)block;
				lineString+="("+electBlock.getGainBlock()+"*"+(element.getSign()?"1":"(-1)")+")";
			}
			else {
				//此处应该抛出异常
			}
		}
		
		return lineString;
	}
	
	public String getForwardLineString() {
		String forwardLineString="";
		
		forwardLineString+=getLineString(forward);
		
		forwardLineString+="*(1";
		for(Vector<LinearBlockElement> otherLoop:otherLoopList) {
			forwardLineString+="-";
			forwardLineString+=getLineString(otherLoop);
		}
		
		forwardLineString+=")";
		
		return forwardLineString;
	}
}
