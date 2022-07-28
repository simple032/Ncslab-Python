package circuit.loop;

import java.util.Vector;

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
}
