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

abstract public class CodeModel extends NCSLabModel {

	protected Vector<Block> terminalBlockList=new Vector<Block>();
	protected boolean isAlgebraicLoop=false;

	protected Vector<Block> scanBlockList=new Vector<Block>();
	protected Vector<OutputPort> outputPortPathList=new Vector<OutputPort>();	

	protected Vector<Block> outputChain=new Vector<Block>();

	protected Solver solver=Solver.ode1;

	protected CodeModel(JSONObject jsonIn,ModelMode mode) throws ModelException{
		super(jsonIn,mode);
	}

	protected CodeModel(JSONObject jsonIn,ModelMode mode,Solver solver) throws ModelException{
		super(jsonIn,mode);
		this.solver=solver;
	}

	private void generateOutputCodeFromChain(CodeGenerationOption option) {
		for(Block block:outputChain) {
			//������ɣ�ҲҪ����ģ����������
			generateBlockOutputCode(block,option);
		}
	}

	private void setupOuputChain() {
		//�ҵ��ն˵�Block��ֻ������û�������������terminalBlockList����Ϊ���������
		findTerminalBlocks();
		//��������ģ�飬����˳������Output�Ĵ���
		scanOutputChain();
	}

	public void setSolver(Solver solver) {
		this.solver=solver;
	}
	
	public Solver getSolver() {
		return this.solver;
	}

	/*
	public void generateOde1() {
		CodeGenerationOption option=new CodeGenerationOption();

		//�������ɳ�ʼ������
		generateInitCode(option);

		generateOutputCodeFromChain(option);

		//�����������ݸ��µĴ��룬Update�Ĵ��벻��Ouput������Ҫע��˳��
		generateUpdateCode(option);
	}*/
	
	/*
	public void generateOde1() {
		CodeGenerationOption option=new CodeGenerationOption();

		//�������ɳ�ʼ������
		generateInitCode(option);
		//��������Ĵ���		
		generateOutputCodeFromChain(option);
		//����΢�����Ĵ���
		generateDerivativeCode(option);

		//�����������ݸ��µĴ��룬Update�Ĵ��벻��Ouput������Ҫע��˳��
		generateUpdateCode(option);
	}*/


	//���ɴ����ͨ�÷�ʽ����ͬ�����Ե����ɣ���������
	public void generate() {

		CodeGenerationOption option=new CodeGenerationOption();

		System.out.println("Generating codes......");

		setupOuputChain();

		//�������ɳ�ʼ������
		generateInitCode(option);
		//��������Ĵ���		
		generateOutputCodeFromChain(option);
		//����΢�����Ĵ���
		generateDerivativeCode(option);

		//�����������ݸ��µĴ��룬Update�Ĵ��벻��Ouput������Ҫע��˳��
		try {
			generateUpdateCode(option);
		} catch (MatDimException e) {
			// TODO Auto-generated catch block
			errorList.add(new ErrorMessage(100, e.getMessage()));
		}
		
		generateStatementCode(option);
	}

	//��ʼ���Ĵ��룬�̳е����������
	abstract protected void generateInitCode(CodeGenerationOption option);
	//Update�Ĵ��룬�̳е����������
	abstract protected void generateUpdateCode(CodeGenerationOption option) throws MatDimException;
	//ĳ��ģ������Ĵ��룬�̳е����������
	abstract protected void generateBlockOutputCode(Block block,CodeGenerationOption option); 

	abstract protected void generateDerivativeCode(CodeGenerationOption option);
	
	abstract protected void generateStatementCode(CodeGenerationOption option);

	private void scanInputPort(InputPort inputPort) {
		Line line=inputPort.getLinkedLine();
		OutputPort outputPort=line.getLinkedOutputPort();

		//�������˿��Ѿ�������ϣ����������ɣ�������һ����֧�ı���
		if(outputPort.getIsCodeGenerated()==true) {
			return;
		}

		for(OutputPort output:outputPortPathList) {
			if(output==outputPort) {
				isAlgebraicLoop=true;
			}
		}

		//������ִ�����
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


		//��¼���OutputPort�Ѿ�������·����·�У���Ϊ����
		outputPortPathList.add(outputPort);

		//���û�����ɣ��Ǿͱ���block���������block�Ĵ���
		Block block=outputPort.getBLock();

		boolean isFeedThroughBlock=false;
		Vector<OutputPort> outputPortList=block.getOutputPortList();
		for(OutputPort output:outputPortList) {
			if(output.getFeedThrough()==true) {
				isFeedThroughBlock=true;
			}
		}

		//�����Feedthrough��ģ�飬��Ҫ��������ģ���InputPort
		if(isFeedThroughBlock) {
			Vector<InputPort> InputPortList=block.getInputPortList();
			for(InputPort input:InputPortList) {
				//�ݹ���ã�ʵ�ֱ���
				scanInputPort(input);
			}
			//������ɣ�ҲҪ����ģ����������
			//generateBlockOutputCode(block);
			outputChain.add(block);
			block.setIsOuputCodeGenerated(true);
		}
		//���û�У���ֱ������ģ����������
		else {
			//����ģ����������
			//generateBlockOutputCode(block);
			outputChain.add(block);
			block.setIsOuputCodeGenerated(true);

			//�������ģ���������㲻ȡ���ڵ�ǰ�����룬��������Update������Ҫ�������ļ��㡣��˽����ģ�����scanBlockList��������α���
			scanBlockList.add(block);
		}

		//��������·����·�е����ģ��
		outputPortPathList.remove(outputPortPathList.size()-1);
	}

	/*���б����ķ���*/
	private void scanOutputChain() {
		System.out.println("scaning outputChain");

		//�������е��ն�ģ��
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

		//���ж��α�������Ϊ���α��������У�scanBlockList�е�Ԫ�ض�̬�仯������Ҫ��whileѭ��
		while(scanBlockList.isEmpty()==false) {
			//ȡ����һ��Ԫ�ؽ��б���
			Block block=scanBlockList.remove(0);
			Vector<InputPort> inputPortList=block.getInputPortList();
			for(InputPort inputPort:inputPortList) {
				scanInputPort(inputPort);
			}
		}
	}

	/*Ѱ���ն�Block�ĺ����������е��ն�block����terminalBlockList��Ϊ������׼�� */
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
