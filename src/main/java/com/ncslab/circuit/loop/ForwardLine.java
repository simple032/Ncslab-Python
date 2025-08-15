package com.ncslab.circuit.loop;

import java.util.ArrayList;
import java.util.List;

import com.ncslab.block.Block;
import com.ncslab.circuit.block.electblock.ElectBlock;

import com.ncslab.circuit.loop.CircuitLoopException;

public class ForwardLine {
	private List<LinearBlockElement> forward = new ArrayList<>();
	
	private List<List<LinearBlockElement>> contactLoopList = new ArrayList<>();
	
	private List<List<LinearBlockElement>> otherLoopList = new ArrayList<>();
	
	public ForwardLine(List<LinearBlockElement> forward) {
		this.forward=forward;
	}
	
	public List<LinearBlockElement> getForward(){
		return this.forward;
	}
	
	private boolean isContact(List<LinearBlockElement> line1,List<LinearBlockElement> line2) {
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
	
	
	public void setupLoops(List<List<LinearBlockElement>> loopList) {
		//建立与前向通道先接触的LoopList,以及不想连的LoopList
		for(List<LinearBlockElement> loop:loopList) {
			if(isContact(loop,forward)) {
				contactLoopList.add(loop);
			}
			else {
				otherLoopList.add(loop);
			}
		}
	}
	
	public List<List<LinearBlockElement>> getContactLoopList(){
		return this.contactLoopList;
	}
	
	public List<List<LinearBlockElement>> getOtherLoopList(){
		return this.otherLoopList;
	}
	
	//生成Forward通道的乘积Code
	public static String getLineString(List<LinearBlockElement> line) throws CircuitLoopException{
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
		for(List<LinearBlockElement> otherLoop:otherLoopList) {
			forwardLineString+="-";
			forwardLineString+=getLineString(otherLoop);
		}
		
		forwardLineString+=")";
		
		return forwardLineString;
	}
}
