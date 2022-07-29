package code;

import java.util.Vector;

import org.json.JSONObject;

import block.Block;
import block.io.InputPort;
import block.io.OutputPort;
import line.Line;
import ncslablink.ErrorMessage;
import ncslablink.MatDimException;
import ncslablink.ModelException;
import ncslablink.ModelMode;
import ncslablink.NCSLabModel;

import circuit.block.electblock.ElectBlock;

abstract public class CodeModel extends NCSLabModel {

	protected Vector<Block> terminalBlockList=new Vector<Block>();
	protected boolean isAlgebraicLoop=false;

	protected Vector<Block> scanBlockList=new Vector<Block>();
	protected Vector<OutputPort> outputPortPathList=new Vector<OutputPort>();	

	//输出链，应该先输出哪个，然后再输出哪个
	protected Vector<Block> outputChain=new Vector<Block>();

	protected Solver solver=Solver.ode4;

	protected CodeModel(JSONObject jsonIn,ModelMode mode) throws ModelException{
		super(jsonIn,mode);
		
		setupSolver();
		System.out.println(this.solver);
	}

	protected CodeModel(JSONObject jsonIn,ModelMode mode,Solver solver) throws ModelException{
		super(jsonIn,mode);
		this.solver=solver;
	}
	
	private void setupSolver() {
		String solverString=this.getConfig().getSolver();
		switch(solverString) {	
		case "VariableStepAuto":
			solver=Solver.ode45;
			break;
		case "ode45":
			solver=Solver.ode45;
			break;
		case "ode5":
			solver=Solver.ode5;
			break;
		case "ode8":
			solver=Solver.ode6;
			break;
		case "ode4":
			solver=Solver.ode4;
			break;
		case "ode3":
			solver=Solver.ode3;
			break;
		case "ode2":
			solver=Solver.ode2;
			break;
		case "ode1":
			solver=Solver.ode1;
			break;
		case "ode23":
			solver=Solver.ode23;
			break;
		}
		/*if(solverString.equals("VariableStepAuto")||solverString.equals("ode45")) {
			solver=Solver.ode45;
		}
		if(solverString.equals("ode5")) {
			solver=Solver.ode5;
		}
		if(solverString.equals("ode8")) {
			solver=Solver.ode6;
		}
		if(solverString.equals("ode23")) {
			solver=Solver.ode23;
		}*/
	}

	private void generateOutputCodeFromChain(CodeGenerationOption option) {
		for(Block block:outputChain) {
			//根据输出链，建立Ouput的代码
			generateBlockOutputCode(block,option);
		}
	}
	
	private void setupElectBlocks() {
		System.out.println("Looking for elect blocks");
		for(Block block:blockList) {
			if(block instanceof ElectBlock) {
				ElectBlock electBlock=(ElectBlock)block;
				//如果是loopPoint
				if(electBlock.isLoopPoint()) {
					System.out.println("Found ("+block.getBlockId()+"): "+block.getBlockName());
					block.setFeedThrough(false);
					
					Vector<Block> relatedBlockList=electBlock.getRelatedBlockList();
					
					for(Block relatedBlock:relatedBlockList) {
						if(relatedBlock.getIsOutputCodeGenerated()==false) {
							outputChain.add(relatedBlock);
							//terminalBlockList.add(relatedBlock);
							relatedBlock.setIsOuputCodeGenerated(true);
							scanBlockList.add(relatedBlock);
						}
					}
					outputChain.add(block);
					block.setIsOuputCodeGenerated(true);
					scanBlockList.add(block);
				}
			}
		}
	}
	
	/*建立输出链，决定应该先嫉妒是你哪个模块，再计算哪个模块*/
	private void setupOuputChain() {
		
		setupElectBlocks();
		
		//找到终端的Block
		findTerminalBlocks();
		//沿着终端模块，建立输出链
		scanOutputChain();
	}

	public void setSolver(Solver solver) {
		this.solver=solver;
	}
	
	public Solver getSolver() {
		return this.solver;
	}


	//生成代码的通用范式，不同的语言的生成，可以重载
	public void generate() {

		CodeGenerationOption option=new CodeGenerationOption();

		System.out.println("Generating codes......");

		setupOuputChain();

		//首先生成初始化代码
		generateInitCode(option);
		//根据输出链，建立Ouput的代码
		generateOutputCodeFromChain(option);
		//计算为分量的代码
		generateDerivativeCode(option);
		
		//author:xiazhiqiang
		generateArraysCode(option);
		//end

		//根据微分量，建立Update的代码 
		try {
			generateUpdateCode(option);
		} catch (MatDimException e) {
			// TODO Auto-generated catch block
			errorList.add(new ErrorMessage(100, e.getMessage()));
		}
		
		generateStatementCode(option);
		
		generateTerminateCode(option);
	}

	//初始化的代码，继承的类可以重载
	abstract protected void generateInitCode(CodeGenerationOption option);
	//Update的代码，继承的类可以重载
	abstract protected void generateUpdateCode(CodeGenerationOption option) throws MatDimException;
	//某个模块输出的代码，继承的类可以重载
	abstract protected void generateBlockOutputCode(Block block,CodeGenerationOption option); 
	
	//author:xiazhiqiang
	abstract protected void generateArraysCode(CodeGenerationOption option);
	//end

	abstract protected void generateDerivativeCode(CodeGenerationOption option);
	
	abstract protected void generateStatementCode(CodeGenerationOption option);
	
	abstract protected void generateTerminateCode(CodeGenerationOption option);

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
			
			System.err.println(errorString);
			ErrorMessage errorMessage=new ErrorMessage(ErrorMessage.AlgebraicLoop,errorString+"\n");
			addErrorMessage(errorMessage);
			isAlgebraicLoop=false;
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
			//generateBlockOutputCode(block);
			outputChain.add(block);
			block.setIsOuputCodeGenerated(true);
		}
		//如果没有，就直接生成模块的输出代码
		else {
			//生成模块的输出代码
			//generateBlockOutputCode(block);
			outputChain.add(block);
			block.setIsOuputCodeGenerated(true);

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
			//generateBlockOutputCode(block);
			outputChain.add(block);
			block.setIsOuputCodeGenerated(true);
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
