package code;

import java.util.Vector;

import org.json.JSONObject;

import block.Block;
import block.io.InputPort;
import block.io.OutputPort;
import line.Line;
import ncslablink.ErrorMessage;
import ncslablink.ModelException;
import ncslablink.NCSLabModel;

abstract public class CodeModel extends NCSLabModel {
	
	protected Vector<Block> terminalBlockList=new Vector<Block>();
	protected boolean isAlgebraicLoop=false;
	
	protected Vector<Block> scanBlockList=new Vector<Block>();
	protected Vector<OutputPort> outputPortPathList=new Vector<OutputPort>();	
	
	protected CodeModel(JSONObject jsonIn) throws ModelException{
		super(jsonIn);
	}
	
	//生成代码的通用范式，不同的语言的生成，可以重载
	public void generate() {
		System.out.println("Generating codes......");

		//首先生成初始化代码
		generateInitCode();
		
		//找到终端的Block（只有输入没有输出），放入terminalBlockList，作为遍历的入口
		findTerminalBlocks();
		//遍历各个模块，按照顺序生成Output的代码
		scanOutputChain();
		
		//首先生成数据更新的代码，Update的代码不像Ouput，不需要注意顺序
		generateUpdateCode();
	}
	
	//初始化的代码，继承的类可以重载
	abstract protected void generateInitCode();
	//Update的代码，继承的类可以重载
	abstract protected void generateUpdateCode();
	//某个模块输出的代码，继承的类可以重载
	abstract protected void generateBlockOutputCode(Block block); 
	
	private void scanInputPort(InputPort inputPort) {
		Line line=inputPort.getLinkedLine();
		OutputPort outputPort=line.getLinkedOutputPort();
		
		//如果输出端口已经生成完毕，则不用再生成，结束这一个分支的遍历
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
		
		
		//记录这个OutputPort已经在现有路径回路中，作为记忆
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
		
		//如果有Feedthrough的模块，则要遍历整个模块的InputPort
		if(isFeedThroughBlock) {
			Vector<InputPort> InputPortList=block.getInputPortList();
			for(InputPort input:InputPortList) {
				//递归调用，实现遍历
				scanInputPort(input);
			}
			//遍历完成，也要生成模块的输出代码
			generateBlockOutputCode(block);
		}
		//如果没有，就直接生成模块的输出代码
		else {
			//生成模块的输出代码
			generateBlockOutputCode(block);
			//尽管这个模块的输出计算不取决于当前的输入，但是它的Update还是需要输入量的计算。因此将这个模块加入scanBlockList，进入二次遍历
			scanBlockList.add(block);
		}
		
		//清除记忆的路径回路中的这个模块
		outputPortPathList.remove(outputPortPathList.size()-1);
	}
	
	/*进行遍历的方法*/
	private void scanOutputChain() {
		System.out.println("scaning outputChain");

		//遍历所有的终端模块
		for(Block block:terminalBlockList) {
			Vector<InputPort> inputPortList=block.getInputPortList();
			for(InputPort inputPort:inputPortList) {
				scanInputPort(inputPort);
			}
			
			//code+=block.generateBlockOutputCodeM();
			generateBlockOutputCode(block);
		}
		
		//进行二次遍历，因为二次遍历过程中，scanBlockList中的元素动态变化，所有要用while循环
		while(scanBlockList.isEmpty()==false) {
			//取出第一个元素进行遍历
			Block block=scanBlockList.remove(0);
			Vector<InputPort> inputPortList=block.getInputPortList();
			for(InputPort inputPort:inputPortList) {
				scanInputPort(inputPort);
			}
		}
	}
	
	/*寻找终端Block的函数，将所有的终端block加入terminalBlockList，为遍历做准备 */
	private void findTerminalBlocks() {
		System.out.println("Looking for terminal blocks");
		for(Block block:blockList) {
			if(block.isTerminalBlock()) {
				System.out.println("Found ("+block.getBlockId()+"): "+block.getBlockName());
				terminalBlockList.add(block);
			}
		}
	}
	
	
}
