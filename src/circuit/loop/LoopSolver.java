package circuit.loop;

import java.util.Vector;

import block.Block;

public class LoopSolver {
	private Block outputBlock;
	
	private Vector<Vector<LinearBlockElement>> forwardList=new Vector<Vector<LinearBlockElement>>();
	
	private Vector<Vector<LinearBlockElement>> loopList=new Vector<Vector<LinearBlockElement>>();
	
	public LoopSolver(Block outputBlock) {
		this.outputBlock=outputBlock;
	}
	
	public void addForward(Vector<LinearBlockElement> blockPath) {
		Vector<LinearBlockElement> forward=new Vector<LinearBlockElement>();
		
		for(LinearBlockElement element:blockPath) {
			LinearBlockElement newElement=new LinearBlockElement(element.getBlock(),element.getSign());
			forward.add(newElement);
		}
		
		forwardList.add(forward);
	}
	
	public void addLoop(Vector<LinearBlockElement> blockPath,Block block) {
		Vector<LinearBlockElement> loop=new Vector<LinearBlockElement>();
		boolean isLoopStarted=false;
		for(LinearBlockElement element:blockPath) {
			if(block==element.getBlock()) {
				isLoopStarted=true;
			}
			//if(isLoopStarted) {
				LinearBlockElement newElement=new LinearBlockElement(element.getBlock(),element.getSign());
				loop.add(newElement);
			//}
			
		}
		
		loopList.add(loop);
	}
	
	public boolean isLoopIncluded() {
		if(loopList.size()==0) {
			return false;
		}
		else {
			return true;
		}
	}
	
	public void showLoopSolver() {
		System.out.println("+++++++++++++++++++++++++++");
		System.out.println(outputBlock.getBlockName()+"...");
		System.out.println("Forward:");
		for(Vector<LinearBlockElement> forward:forwardList) {
			for(LinearBlockElement element:forward) {
				System.out.print(element.getBlock().getBlockName()+":"+(element.getSign()?"+":"-")+'\t');
			}
			System.out.println();
		}
		System.out.println("Loop:");
		for(Vector<LinearBlockElement> loop:loopList) {
			for(LinearBlockElement element:loop) {
				System.out.print(element.getBlock().getBlockName()+":"+(element.getSign()?"+":"-")+'\t');
			}
			System.out.println();
		}
		System.out.println("+++++++++++++++++++++++++++");
	}
}
