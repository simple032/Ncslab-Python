package code;

import java.util.Vector;

import org.json.JSONObject;

import block.Block;
import block.io.InputPort;
import block.io.OutputPort;
import line.Line;
import ncslablink.ErrorMessage;
import ncslablink.NCSLabModel;

abstract public class CodeModel extends NCSLabModel {
	
	protected Vector<Block> terminalBlockList=new Vector<Block>();
	protected boolean isAlgebraicLoop=false;
	
	protected Vector<Block> scanBlockList=new Vector<Block>();
	protected Vector<OutputPort> outputPortPathList=new Vector<OutputPort>();	
	
	protected CodeModel(JSONObject jsonIn){
		super(jsonIn);
	}
	
	public void generate() {
		System.out.println("Generating codes......");
		generateInitCode();
		
		findTerminalBlocks();
		scanOutputChain();
		
		generateUpdateCode();
	}
	
	abstract protected void generateInitCode();
	
	abstract protected void generateUpdateCode();
	
	abstract protected void generateBlockOutputCode(Block block); 
	
	private void scanInputPort(InputPort inputPort) {
		Line line=inputPort.getLinkedLine();
		OutputPort outputPort=line.getLinkedOutputPort();
		
		//如果输出端口已经生成完毕，则不用再生成，返回
		if(outputPort.getIsCodeGenerated()==true) {
			return;
		}
		
		for(OutputPort output:outputPortPathList) {
			if(output==outputPort) {
				isAlgebraicLoop=true;
			}
		}
		
		//如果发现代数环
		if(isAlgebraicLoop) {
			String errorString;
			errorString="Found algorbet loop!!!";
			boolean isDisp=false;
			for(OutputPort output:outputPortPathList) {
				
				if(output==outputPort) {
					isDisp=true;
				}
				
				if(isDisp) {
					errorString+=output.getBLock().getBlockName()+"->";
				}
			}
			
			errorString+=outputPort.getBLock().getBlockName();
			ErrorMessage errorMessage=new ErrorMessage(ErrorMessage.AlgebraicLoop,errorString);
			addErrorMessage(errorMessage);
			return;
		}
		
		
		//记录这个OutputPort已经在现有回路中
		outputPortPathList.add(outputPort);
		
		//如果没有生成，那就遍历block，生成这个block的代码
		Block block=outputPort.getBLock();
		
		boolean isFeedThroughBlock=false;
		Vector<OutputPort> outputPortList=block.getOutputPortList();
		for(OutputPort output:outputPortList) {
			if(output.getFeedThrough()==true) {
				isFeedThroughBlock=true;
			}
		}
		
		//如果有Feedthrough的模块，则要遍历整个模块的输入port
		if(isFeedThroughBlock) {
			Vector<InputPort> InputPortList=block.getInputPortList();
			for(InputPort input:InputPortList) {
				scanInputPort(input);
			}
			generateBlockOutputCode(block);
		}
		//如果没有，就直接生成模块的额输出代码
		else {
			generateBlockOutputCode(block);
			scanBlockList.add(block);
		}
		
		outputPortPathList.remove(outputPortPathList.size()-1);
	}
	
	protected void scanOutputChain() {
		System.out.println("scaning outputChain");
		for(Block block:terminalBlockList) {
			Vector<InputPort> inputPortList=block.getInputPortList();
			for(InputPort inputPort:inputPortList) {
				scanInputPort(inputPort);
			}
			
			//code+=block.generateBlockOutputCodeM();
			generateBlockOutputCode(block);
		}
		
		while(scanBlockList.isEmpty()==false) {
			Block block=scanBlockList.remove(0);
			Vector<InputPort> inputPortList=block.getInputPortList();
			for(InputPort inputPort:inputPortList) {
				scanInputPort(inputPort);
			}
		}
	}
	
	protected void findTerminalBlocks() {
		System.out.println("Looking for terminal blocks");
		for(Block block:blockList) {
			if(block.isTerminalBlock()) {
				System.out.println("Found ("+block.getBlockId()+"): "+block.getBlockName());
				terminalBlockList.add(block);
			}
		}
	}
	
	
}
