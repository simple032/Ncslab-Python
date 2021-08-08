package code;

import org.json.JSONObject;
import java.util.Vector;

import ncslablink.ErrorMessage;
import ncslablink.NCSLabModel;
import block.Block;
import block.io.InputPort;
import block.io.OutputPort;
import line.Line;

public class CodeModel extends NCSLabModel{
	
	private String code="";
	CodeModel(JSONObject jsonIn){
		super(jsonIn);
	}
	
	public static CodeModel createFromJSON(JSONObject jsonIn) {
		CodeModel model=new CodeModel(jsonIn);
		
		return model;
	}
	
	public String getCode() {
		return this.code;
	}
	
	public void generate() {
		System.out.println("Generating codes......");
		generateInitCode();
		
		code+="for t="+this.getConfig().getStartTime()+":"+this.getConfig().getFixedStep()+":"+this.getConfig().getStopTime()+"\n";
		
		findTerminalBlocks();
		scanOutputChain();
		
		generateUpdateCode();
		
		code+="end\n";
	}
	
	private void generateUpdateCode() {
		System.out.println("Generating update codes......");
	}
	
	private void generateInitCode() {
		System.out.println("Generating init codes......");
		for(Block block:blockList) {
			System.out.println("Generating init codes for ("+block.getBlockId()+")"+block.getBlockName());
			
			this.code+=block.generateInitCode();
		}
	}
	
	private Vector<Block> terminalBlockList=new Vector<Block>();
	private Vector<Block> scanBlockList=new Vector<Block>();
	private Vector<OutputPort> outputPortPathList=new Vector<OutputPort>();
	
	private void findTerminalBlocks() {
		System.out.println("Looking for terminal blocks");
		for(Block block:blockList) {
			if(block.isTerminalBlock()) {
				System.out.println("Found ("+block.getBlockId()+"): "+block.getBlockName());
				terminalBlockList.add(block);
			}
		}
	}
	
	private void scanOutputChain() {
		System.out.println("scaning outputChain");
		for(Block block:terminalBlockList) {
			Vector<InputPort> inputPortList=block.getInputPortList();
			for(InputPort inputPort:inputPortList) {
				scanInputPort(inputPort);
			}
			
			code+=block.generateBlockOutputCode();
		}
		
		while(scanBlockList.isEmpty()==false) {
			Block block=scanBlockList.remove(0);
			Vector<InputPort> inputPortList=block.getInputPortList();
			for(InputPort inputPort:inputPortList) {
				scanInputPort(inputPort);
			}
		}
	}
	
	private boolean isAlgebraicLoop=false;
	
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
			code+=block.generateBlockOutputCode();
		}
		//如果没有，就直接生成模块的额输出代码
		else {
			code+=block.generateBlockOutputCode();
			scanBlockList.add(block);
		}
		
		outputPortPathList.remove(outputPortPathList.size()-1);
	}
}
