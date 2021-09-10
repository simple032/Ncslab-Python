package code.c;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.util.Vector;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.*;

import code.CodeModel;
import block.Block;
import block.io.OutputPort;
import block.io.OutputSignal;
import block.io.Parameter;
import block.io.State;
import block.io.InputPort;

public class CodeStructC {

	//ģ��������Ƿ���Ϊ�ź�
	public boolean inputAsSignal=true;
	//ģ�������Ƿ���Ϊ�ź�
	public boolean outputAsSignal=true;

	//ͷ�ļ��Ĵ���
	public String includeCode="";
	//init��ʼ���Ĵ���
	public String initCode="";
	//Output�Ĵ���
	public String outputCode="";
	//update�Ĵ���
	public String updateCode="";

	//update�Ĵ���
	public String derivativeCode="";

	/*����Parameter�Ĵ��� ��*REAL Block5_Parameter_P*/
	public String parameterDefineCode="";
	/*����State�Ĵ��� �� REAL Block1_State_pumpState;*/
	public String stateDefineCode="";
	/*����Output�źŵĴ��룬�� REAL Block1_Output1;*/
	public String outputSignalDefineCode="";
	
	public String otherCode="";

	/*�������м������ʵ��Ĵ��룬����INPUT_PORT OUT_PORT PARAMTER STATE SIGNAL BLOCK*/
	public String dataStructureCode="";
	/*����������ʵ���ʼ���Ĵ��룬��ʼ����������ṹ�����ƣ�path�ȣ���ָ��ָ��ָ����λ�ã��������ݽṹ�� */
	public String dataStructureInitCode="";

	private int parameterIndex=1;
	private int stateIndex=1;

	private Vector<Parameter> parameterList=new Vector<Parameter>();
	private Vector<State> stateList=new Vector<State>();
	private Vector<OutputSignal> outputSignalList=new Vector<OutputSignal>();

	private CodeModelC model;

	public CodeStructC(CodeModelC model) {
		this.model=model;

	}

	public String getInitCode() {
		return this.initCode;
	}

	public void addInitCode(String code) {
		initCode+=code;
	}

	public String getOutputCode() {
		return this.outputCode;
	}

	public void addOutputCode(String code) {
		outputCode+=code;
	}

	public String getUpdateCode() {
		return this.updateCode;
	}

	public void addUpdateCode(String code) {
		updateCode+=code;
	}

	public void addDerivativeCode(String code) {
		derivativeCode+=code;
	}

	public void generateIncludeCode() {
		includeCode+=""
				+"#include\"ncslabccode.h\"\n"
				+"#include\"ncslab.h\"\n"
				+"#include\"ncs_serialport.h\"\n";
	}

	private void writeMainCodeFile() {
		System.out.println("Writing file mainccode.c...");
		//precode�ǲ�����״̬��������Ķ��壬��ȫ�ֱ����ķ�ʽ
		String preCode=parameterDefineCode+"\n"+stateDefineCode+"\n"
					+outputSignalDefineCode+"\n"+otherCode+"\n";
		String mainCCode=includeCode+"\n"
				+preCode+"\n"
				+dataStructureCode+"\n"
				+"void NCSLabInit(){\n"
				+dataStructureInitCode+"\n"
				+initCode+"\n"
				+"}\n"

				/*
				+"void NCSLabOneStep(){\n"
				+outputCode+"\n"
				+derivativeCode+"\n"
				+updateCode+"\n"
				+"}\n"*/

				+"void NCSLabOutput(){\n"
				+outputCode+"\n"
				+"}\n"
				
				+"void NCSLabDerivative(){\n"
				+derivativeCode+"\n"
				+"}\n"
				
				+"void NCSLabUpdate(){\n"
				+updateCode+"\n"
				+"}\n"
				
				+"MODEL * NCSLabGetModelP(){\n"
				+"return &model;\n"
				+"}\n"

				+"\n";

		File file = new File(codePath+"mainccode.c");
		FileOutputStream outputStream;
		try {
			outputStream = new FileOutputStream(file);
			outputStream.write(mainCCode.getBytes());
			outputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	//����ȫ�ֵ�Parameter���б�
	public void addParameter(Parameter parameter) {
		parameterList.add(parameter);
	}
	//����ȫ�ֵ�state���б�
	public void addState(State state) {
		stateList.add(state);
	}
	//����ȫ�ֵ��źŵ��б�
	public void addOutputSignal(OutputSignal outputSignal) {
		outputSignalList.add(outputSignal);
	}

	//���ɶ���Parameter�Ĵ���
	public void generateParameterDefineCode() {
		parameterDefineCode+="/*Define variables for parameters*/\n";
		for(Parameter parameter:parameterList) {
			parameterDefineCode+=parameter.getDefineString()+" "+parameter.getName()+";\n";
		}
	}

	//���ɶ���State�Ĵ���
	public void generateStateDefineCode() {
		stateDefineCode+="/*Define variables for states*/\n";
		for(State state:stateList) {
			stateDefineCode+=state.getDefineString()+" "+state.getName()+";\n";
			stateDefineCode+=state.getDefineString()+" "+state.getDerivativeName()+";\n";
		}
	}

	//���ɶ���Output�Ĵ���
	public void generateOutputSignalDefineCode() {
		outputSignalDefineCode+="/*Define variables for output signals*/\n";
		for(OutputSignal outputSignal:outputSignalList) {
			outputSignalDefineCode+=outputSignal.getDefineString()+" "+outputSignal.getName()+";\n";
		}
	}



	private String codePathBase=utils.Property.instance.getProperty("CCodePath");
	//Ŀ���ļ��е�λ��codePathBase/�û�id/modelId
	private String codePath;

	//д�ļ��ķ��������ļ���resource�п���������д��Ŀ���ļ���
	private void writeNCSLabFile(String fileName) {
		System.out.println("Writing file "+fileName+"...");
		InputStream InputStream = this.getClass().getResourceAsStream(fileName);

		File file=new File(codePath+"/"+fileName);
		FileOutputStream outputStream;
		try {
			outputStream = new FileOutputStream(file);
			byte[] buffer=new byte[1024];
			int len;
			while((len=InputStream.read(buffer))>0) {
				outputStream.write(buffer,0,len);
			}
			outputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void writeNCSLabFile(String fileName,String fileNameOut) {
		System.out.println("Writing file "+fileName+"...");
		InputStream InputStream = this.getClass().getResourceAsStream(fileName);

		File file=new File(codePath+"/"+fileNameOut);
		FileOutputStream outputStream;
		try {
			outputStream = new FileOutputStream(file);
			byte[] buffer=new byte[1024];
			int len;
			while((len=InputStream.read(buffer))>0) {
				outputStream.write(buffer,0,len);
			}
			outputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	void wirteDefineFile() {
		
		String code="#define STATE_NUM "+stateList.size()+"\n";
		
		code+="#define STEP_SIZE (1.0*"+model.getConfig().getFixedStep()+")\n";
		
		File file = new File(codePath+"ncslab.h");
		FileOutputStream outputStream;
		try {
			outputStream = new FileOutputStream(file);
			outputStream.write(code.getBytes());
			outputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private byte[] readFile(String fileName) {
		File file = new File(codePath+fileName);
		FileInputStream inputStream;
		byte[] fileData=null;
		try {
			inputStream = new FileInputStream(file);
			fileData=new byte[inputStream.available()];
			inputStream.read(fileData);
			inputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return fileData;
	}
	
	public byte[] readExeFile() {
		return readFile("ncslab");
	}

	public void writeCCodeFiles() {

		//����Ŀ���ļ��е�λ��codePathBase/�û�id/modelId
		String userPath=codePathBase+model.getUserId();

		File file=new File(userPath);
		if(file.exists()==false) {
			file.mkdir();
		}

		String modelPath=userPath+"/"+model.getModelId();
		file=new File(modelPath);
		if(file.exists()==false) {
			file.mkdir();
		}

		codePath=modelPath+"/";

		//д���ܱߵ���Դ�ļ�
		//makefile
		writeNCSLabFile("makefile");
		//�����ݽṹ
		writeNCSLabFile("ncslabccode.h");
		//main�����Լ���ʱ��
		writeNCSLabFile("ncslabmain.c");
		//���������ݽṹ�Ľӿ�API����
		writeNCSLabFile("DataApi.c");
		writeNCSLabFile("DataApi.h");
		
		writeNCSLabFile("util.c");

		//ʵ��NetconЭ���ͨ���ļ�
		writeNCSLabFile("ServerThread.c");
		writeNCSLabFile("ServerThread.h");
		writeNCSLabFile("ClientThread.c");
		writeNCSLabFile("ClientThread.h");
		writeNCSLabFile("UploadThread.c");
		writeNCSLabFile("UploadThread.h");
		
		writeNCSLabFile("ncs_serialport_pi.c");
		writeNCSLabFile("ncs_serialport.h");

		//д�����ɵ�������ncslabccdoe.c
		writeMainCodeFile();
		
		wirteDefineFile();
		
		switch(model.getSolver()) {
		case ode1:
			writeNCSLabFile("ode1.c","onestep.c");
			break;
		case ode2:
			writeNCSLabFile("ode2.c","onestep.c");
			break;
		case ode3:
			writeNCSLabFile("ode3.c","onestep.c");
			break;
		case ode4:
			writeNCSLabFile("ode4.c","onestep.c");
			break;
		}
		
	}

	public boolean makeExeFile() {
		try {
			//����make�����ɿ�ִ�д���
			Process process=Runtime.getRuntime().exec("make", null, new File(codePath));
			//��ȡOutputStream��errStream�������ȡ����ʱ�����������
			BufferedReader in=new BufferedReader(new InputStreamReader(process.getErrorStream()));
			BufferedReader inOut=new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line=null,outLine=null;
			StringBuilder errStr=new StringBuilder();
			StringBuilder outStr=new StringBuilder();

			while((outLine=inOut.readLine())!=null||(line=in.readLine())!=null) {
				if(outLine!=null) {
					outStr.append(outLine);
					System.out.println(outLine);
				}
				if(line!=null) {
					errStr.append(line);
					System.err.println(line);
				}
			}

			//�ȴ�makefile�����
			process.waitFor();

			if(process.exitValue()==0) {
				return true;
			}

		}
		catch(Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	//����Model,block,input,output,signal,state,parameter�����ݽṹ������ʼ��
	public void gnenrateDataStructureCode() {
		//����һϵ�����ݽṹ�Ķ���
		generateDataStrucure();
		//��ʼ�����ݽṹ��ʵ�����ݽṹ֮���ָ������
		generateDataStrucureInit();
	}

	//�������ݽṹ�Ķ���
	private void generateDataStrucure() {
		dataStructureCode+="/*Define data structures*/\n";

		dataStructureCode+="/*Define inputPort structures*/\n";
		for(Block block:model.getBlockList()) {
			if(block.getInputPortList().size()>0) {
				for(InputPort input:block.getInputPortList()) {
					dataStructureCode+="INPUT_PORT inputPort"+input.getBLock().getBlockId()+"_"+input.getNumber()+"={\""+input.getName()+"\","+input.getWidth()+"};\n";
				}
				dataStructureCode+="INPUT_PORT *inputPorts"+block.getBlockId()+"["+block.getInputPortList().size()+"];\n";
			}
			else {
				dataStructureCode+="INPUT_PORT **inputPorts"+block.getBlockId()+"=NULL;\n";
			}

		}

		dataStructureCode+="/*Define outputPort structures*/\n";
		for(Block block:model.getBlockList()) {
			if(block.getOutputPortList().size()>0) {
				for(OutputPort output:block.getOutputPortList()) {
					dataStructureCode+="OUTPUT_PORT outputPort"+output.getBLock().getBlockId()+"_"+output.getNumber()+"={\""+output.getName()+"\","+output.getWidth()+"};\n";
				}
				dataStructureCode+="OUTPUT_PORT *outputPorts"+block.getBlockId()+"["+block.getOutputPortList().size()+"];\n";
			}
			else {
				dataStructureCode+="OUTPUT_PORT **outputPorts"+block.getBlockId()+"=NULL;\n";
			}

		}

		dataStructureCode+="/*Define parameter structures*/\n";
		int parameterNum=0;
		for(Block block:model.getBlockList()) {
			if(block.getParameterList().size()>0) {
				for(Parameter parameter:block.getParameterList()) {
					dataStructureCode+="PARAMETER parameter"+block.getBlockId()+"_"+parameter.getId()+";\n";
					parameterNum++;
				}
				dataStructureCode+="PARAMETER *parameters"+block.getBlockId()+"["+block.getParameterList().size()+"];\n";
			}
			else {
				dataStructureCode+="PARAMETER **parameters"+block.getBlockId()+"=NULL;\n";
			}
		}
		dataStructureCode+="PARAMETER *parameters["+parameterNum+"];\n";
		model.setParameterNum(parameterNum);

		dataStructureCode+="/*Define state structures*/\n";
		int stateNum=0;
		for(Block block:model.getBlockList()) {
			if(block.getStateList().size()>0) {
				for(State state:block.getStateList()) {
					dataStructureCode+="STATE state"+block.getBlockId()+"_"+state.getId()+"={\""+state.getLocalName()+"\","+state.getWidth()+"};\n";
					stateNum++;
				}
				dataStructureCode+="STATE *states"+block.getBlockId()+"["+block.getStateList().size()+"];\n";
			}
			else {
				dataStructureCode+="STATE **states"+block.getBlockId()+"=NULL;\n";
			}
		}
		dataStructureCode+="STATE *states["+stateNum+"];\n";
		model.setStateNum(stateNum);

		dataStructureCode+="/*Define signal structures*/\n";
		int signalNum=0;
		for(Block block:model.getBlockList()) {
			int i=0;
			if(inputAsSignal&&block.getInputPortList().size()>0) {
				for(InputPort input:block.getInputPortList()) {
					dataStructureCode+="SIGNAL signal"+block.getBlockId()+"_In"+input.getNumber()+";\n";
					i++;
					signalNum++;
				}
			}
			if(outputAsSignal&&block.getOutputPortList().size()>0) {
				for(OutputPort output:block.getOutputPortList()) {
					dataStructureCode+="SIGNAL signal"+block.getBlockId()+"_Out"+output.getNumber()+";\n";
					i++;
					signalNum++;
				}
			}
			if(i>0) {
				dataStructureCode+="SIGNAL *signals"+block.getBlockId()+"["+i+"];\n";
			}
			else {
				dataStructureCode+="SIGNAL **signals"+block.getBlockId()+"=NULL;\n";
			}

			block.setSignalNum(i);
		}
		dataStructureCode+="SIGNAL *signals["+signalNum+"];\n";
		model.setSignalNum(signalNum);

		dataStructureCode+="/*Define block structures*/\n";
		if(model.getBlockList().size()>0) {
			for(Block block:model.getBlockList()) {			
				dataStructureCode+="BLOCK block"+block.getBlockId()+"={\""+block.getBlockType()+"\",\""+block.getBlockName()+"\","+block.getInputPortList().size()+","+block.getOutputPortList().size()+","+block.getParameterList().size()+","+block.getStateList().size()+","+block.getSignalNum()+"};\n";
			}
			dataStructureCode+="BLOCK *blocks["+model.getBlockList().size()+"];\n";
		}
		else {
			dataStructureCode+="BLOCK **blocks=NULL";
		}
		dataStructureCode+="MODEL model={\""+model.getModelRealName()+"\","+model.getBlockList().size()+","+model.getConfig().getFixedStep()+","+model.getConfig().getStartTime()+","+model.getConfig().getStopTime()+"};\n";  
	}

	//��ʼ�����ݽṹ��ʵ�����ݽṹ֮���ָ������
	private void generateDataStrucureInit() {
		dataStructureInitCode+="/*Initialize data structure*/\n";

		dataStructureInitCode+="/*Initialize inputs*/\n";
		for(Block block:model.getBlockList()) {
			dataStructureInitCode+="/*Initialize inputs for block ("+block.getBlockId()+")"+block.getBlockName()+"*/\n";
			for(InputPort input:block.getInputPortList()) {
				dataStructureInitCode+="inputPort"+block.getBlockId()+"_"+input.getNumber()+".vp=&"+input.getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName()+";\n";
			}
		}

		dataStructureInitCode+="/*Initialize outputs*/\n";
		for(Block block:model.getBlockList()) {
			dataStructureInitCode+="/*Initialize outputs for block ("+block.getBlockId()+")"+block.getBlockName()+"*/\n";
			for(OutputPort output:block.getOutputPortList()) {
				dataStructureInitCode+="outputPort"+block.getBlockId()+"_"+output.getNumber()+".vp=&"+output.getOutputSignalC().getName()+";\n";
			}
		}

		dataStructureInitCode+="/*Initialize parameters*/\n";
		for(Block block:model.getBlockList()) {
			dataStructureInitCode+="/*Initialize parameters for block ("+block.getBlockId()+")"+block.getBlockName()+"*/\n";
			for(Parameter parameter:block.getParameterList()) {
				dataStructureInitCode+="parameter"+block.getBlockId()+"_"+parameter.getId()+".name=\""+parameter.getLocalName()+"\";\n";
				dataStructureInitCode+="parameter"+block.getBlockId()+"_"+parameter.getId()+".width="+parameter.getWidth()+";\n";
				dataStructureInitCode+="parameter"+block.getBlockId()+"_"+parameter.getId()+".vp=&"+parameter.getName()+";\n";
				dataStructureInitCode+="parameter"+block.getBlockId()+"_"+parameter.getId()+".path=\""+model.getModelRealName()+"/"+block.getBlockName()+"\";\n";
			}
		}

		dataStructureInitCode+="/*Initialize states*/\n";
		for(Block block:model.getBlockList()) {
			dataStructureInitCode+="/*Initialize states for block ("+block.getBlockId()+")"+block.getBlockName()+"*/\n";
			for(State state:block.getStateList()) {
				dataStructureInitCode+="state"+block.getBlockId()+"_"+state.getId()+".vp=&"+state.getName()+";\n";
				dataStructureInitCode+="state"+block.getBlockId()+"_"+state.getId()+".dvp=&"+state.getDerivativeName()+";\n";
			}
		}

		dataStructureInitCode+="/*Initialize signals*/\n";
		for(Block block:model.getBlockList()) {
			dataStructureInitCode+="/*Initialize signals for block ("+block.getBlockId()+")"+block.getBlockName()+"*/\n";
			if(inputAsSignal) {
				for(InputPort input:block.getInputPortList()) {
					dataStructureInitCode+="signal"+block.getBlockId()+"_In"+input.getNumber()+".vp=&"+input.getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName()+";\n";
					dataStructureInitCode+="signal"+block.getBlockId()+"_In"+input.getNumber()+".width="+input.getLinkedLine().getLinkedOutputPort().getWidth()+";\n";
					dataStructureInitCode+="signal"+block.getBlockId()+"_In"+input.getNumber()+".name=\""+input.getName()+"\";\n";
					dataStructureInitCode+="signal"+block.getBlockId()+"_In"+input.getNumber()+".path=\""+model.getModelRealName()+"/"+block.getBlockName()+"/"+input.getName()+"\";\n";
				}
			}
			if(outputAsSignal) {
				for(OutputPort output:block.getOutputPortList()) {
					dataStructureInitCode+="signal"+block.getBlockId()+"_Out"+output.getNumber()+".vp=&"+output.getOutputSignalC().getName()+";\n";
					dataStructureInitCode+="signal"+block.getBlockId()+"_Out"+output.getNumber()+".width="+output.getWidth()+";\n";
					dataStructureInitCode+="signal"+block.getBlockId()+"_Out"+output.getNumber()+".name=\""+output.getName()+"\";\n";
					dataStructureInitCode+="signal"+block.getBlockId()+"_Out"+output.getNumber()+".path=\""+model.getModelRealName()+"/"+block.getBlockName()+"/"+output.getName()+"\";\n";
				}
			}
		}


		dataStructureInitCode+="/*Initialize blocks*/\n";
		int signalNum=0;
		int parameterNum=0;
		int stateNum=0;
		for(Block block:model.getBlockList()) {
			dataStructureInitCode+="/*Initialize block ("+block.getBlockId()+")"+block.getBlockName()+"*/\n";
			int i=0;
			for(InputPort input:block.getInputPortList()) {
				dataStructureInitCode+="inputPorts"+block.getBlockId()+"["+i+"]=&inputPort"+block.getBlockId()+"_"+input.getNumber()+";\n";
				i++;
			}
			dataStructureInitCode+="block"+block.getBlockId()+".inputPorts=inputPorts"+block.getBlockId()+";\n";

			i=0;
			for(OutputPort output:block.getOutputPortList()) {
				dataStructureInitCode+="outputPorts"+block.getBlockId()+"["+i+"]=&outputPort"+block.getBlockId()+"_"+output.getNumber()+";\n";
				i++;
			}
			dataStructureInitCode+="block"+block.getBlockId()+".outputPorts=outputPorts"+block.getBlockId()+";\n";

			i=0;
			for(Parameter parameter:block.getParameterList()) {
				dataStructureInitCode+="parameters"+block.getBlockId()+"["+i+"]=&parameter"+block.getBlockId()+"_"+parameter.getId()+";\n";
				dataStructureInitCode+="parameters["+parameterNum+"]=&parameter"+block.getBlockId()+"_"+parameter.getId()+";\n";
				i++;
				parameterNum++;
			}
			dataStructureInitCode+="block"+block.getBlockId()+".parameters=parameters"+block.getBlockId()+";\n";

			i=0;
			for(State state:block.getStateList()) {
				dataStructureInitCode+="states"+block.getBlockId()+"["+i+"]=&state"+block.getBlockId()+"_"+state.getId()+";\n";
				dataStructureInitCode+="states["+stateNum+"]=&state"+block.getBlockId()+"_"+state.getId()+";\n";
				i++;
				stateNum++;
			}
			dataStructureInitCode+="block"+block.getBlockId()+".states=states"+block.getBlockId()+";\n";

			i=0;
			if(inputAsSignal) {
				for(InputPort input:block.getInputPortList()) {
					dataStructureInitCode+="signals"+block.getBlockId()+"["+i+"]=&signal"+block.getBlockId()+"_In"+input.getNumber()+";\n";
					dataStructureInitCode+="signals["+signalNum+"]=&signal"+block.getBlockId()+"_In"+input.getNumber()+";\n";
					i++;
					signalNum++;
				}
			}
			if(outputAsSignal) {
				for(OutputPort output:block.getOutputPortList()) {
					dataStructureInitCode+="signals"+block.getBlockId()+"["+i+"]=&signal"+block.getBlockId()+"_Out"+output.getNumber()+";\n";
					dataStructureInitCode+="signals["+signalNum+"]=&signal"+block.getBlockId()+"_Out"+output.getNumber()+";\n";
					i++;
					signalNum++;
				}
			}
			dataStructureInitCode+="block"+block.getBlockId()+".signals=signals"+block.getBlockId()+";\n";
		}

		dataStructureInitCode+="/*Initialize model*/\n";
		for(int i=0;i<model.getBlockList().size();i++) {
			Block block=model.getBlockList().get(i);
			dataStructureInitCode+="blocks["+i+"]=&block"+block.getBlockId()+";\n";
		}

		dataStructureInitCode+="model.blocks=blocks;\n";
		dataStructureInitCode+="model.time=model.startTime;\n";
		dataStructureInitCode+="model.offset=0;\n";

		dataStructureInitCode+="model.signalNum="+signalNum+";\n";
		dataStructureInitCode+="model.signals=signals;\n";

		dataStructureInitCode+="model.parameterNum="+parameterNum+";\n";
		dataStructureInitCode+="model.parameters=parameters;\n";

		dataStructureInitCode+="model.stateNum="+stateNum+";\n";
		dataStructureInitCode+="model.states=states;\n";
	}

	public void generateOtherCode() {
		// TODO Auto-generated method stub
		otherCode += "int hComm;\n" + 
				"\n" + 
				"\n" + 
				"unsigned char calcSum(unsigned char bytes[]);\n";
	}

}
