package com.ncslab.circuit.loop;

import java.util.Vector;

import com.ncslab.block.Block;
import com.ncslab.circuit.block.electblock.ElectBlock;

import com.ncslab.circuit.loop.CircuitLoopException;

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
		//建立与前向通道先接触的LoopList,以及不想连的LoopList
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
	
	//生成Forward通道的乘积Code
	public static String getLineString(Vector<LinearBlockElement> line) throws CircuitLoopException{
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
	
	//获得前向通道的梅逊公式分子计算公式,考虑不接触回路
	public String getForwardLineString() throws CircuitLoopException{
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
