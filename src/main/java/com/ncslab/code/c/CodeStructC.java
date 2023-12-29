package com.ncslab.code.c;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.util.Vector;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.*;

import com.ncslab.ncslablink.ModelMode;
import com.ncslab.block.Block;
import com.ncslab.block.io.*;
import com.ncslab.block.io.terminal.Terminal;
import com.ncslab.utils.Property;


abstract public class CodeStructC {


	//模块的输入是否作为信号

	public boolean inputAsSignal=true;
	//模块的输出是否作为信号
	public boolean outputAsSignal=true;


	//头文件的代码
	
	//author:xiazhiqiang
	//define arrays to save data
	public String arraysCode="";
    //end

	public String includeCode="";
	//init初始化的代码
	public String initCode="";
	//Output的代码
	public String outputCode="";
	//update的代码
	public String updateCode="";
	
	public String discreteUpdateCode="";
	
	public String sinkOutputCode="";
	public String sinkStatusClearCode="";

	//定义的代码
	public String statementCode="";
	//微分计算的代码
	public String derivativeCode="";
	
	public String terminateCode="";

	/*����Parameter�Ĵ��� ��*REAL Block5_Parameter_P*/

	public String parameterDefineCode="";
	/*����State�Ĵ��� �� REAL Block1_State_pumpState;*/
	public String stateDefineCode="";
	/*����Output�źŵĴ��룬�� REAL Block1_Output1;*/
	public String outputSignalDefineCode="";
	
	public String hardwareDefineCode="";



	/*定义所有监控数据实体的代码，包括INPUT_PORT OUT_PORT PARAMTER STATE SIGNAL BLOCK*/
	public String dataStructureCode="";
	/*定义监控数据实体初始化的代码，初始化各个组件结构的名称，path等，让指针指向指定的位置，建立数据结构， */
	public String dataStructureInitCode="";

	private int parameterIndex=1;
	private int stateIndex=1;

	private Vector<Parameter> parameterList=new Vector<Parameter>();
	private Vector<State> stateList=new Vector<State>();
	private Vector<OutputSignal> outputSignalList=new Vector<OutputSignal>();

	protected CodeModelC model;

	public CodeStructC(CodeModelC model) {
		this.model=model;

	}

	public String getInitCode() {
		return this.initCode;
	}

	public void addInitCode(String code) {
		initCode+=code;
	}
	//author:xiazhiqiang
	public String getArraysCode() {
		return this.arraysCode;
	}
	public void addArraysCode(String code) {
		arraysCode+=code;
	}
    //end

	public String getOutputCode() {
		return this.outputCode;
	}

	public void addOutputCode(String code) {
		outputCode+=code;
	}
	
	public void addTerminateCode(String code) {
		terminateCode+=code;
	}

	public String getUpdateCode() {
		return this.updateCode;
	}

	public void addUpdateCode(String code) {
		updateCode+=code;
	}
	
	public void addDiscreteUpdateCode(String code) {
		discreteUpdateCode+=code;
	}
	
	public void addSinkOutputCode(String code) {
		sinkOutputCode+=code;
	}
	
	public void addSinkStatusClearCode(String code) {
		sinkOutputCode+=code;
	}

	public void addDerivativeCode(String code) {
		derivativeCode+=code;
	}

	public void generateIncludeCode() {
		includeCode+=""
				+"#include\"ncslabccode.h\"\n"
				+"#include\"ncslabdefines.h\"\n"
				+"#include\"ncs_serialport.h\"\n"
				+"#include\"ncslab.h\"\n"
				+"#include\"math.h\"\n"
				+"#ifdef _RT\n"
				+"#include\"hardware.h\"\n"
				+"#include\"ADS1256.h\"\n"
				+"#include\"DAC8532.h\"\n"
				+"#include\"Debug.h\"\n"
				+"#include\"wiringPi.h\"\n"
				+"#include\"wiringPiSPI.h\"\n"
				+"#endif\n"
				+"#include <iostream>\n"
				
				
				//+"#include <octave/oct.h>\n"
				+"#include \"Matrix.h\"\n"
				//xiazhiqiang:Stores the sampling time of discrete modules
				+"double  sample_time["+model.getBlockList().size()+"]={};\n"
				+"int sample_i=0;\n"
				//end
				;
	}

	protected void writeMainCodeFile() {
		System.out.println("Writing file mainccode.c...");
		//precode是参数，状态，和输出的定义，以全局变量的方式
		String preCode="extern MODEL* mp;\n"
					+statementCode+"\n"
					+hardwareDefineCode+"\n"
					+parameterDefineCode+"\n"
					+stateDefineCode+"\n"
					+outputSignalDefineCode+"\n";
		String mainCCode=includeCode+"\n"
				//author:xiazhiqiang
				//add define arrays code
				+arraysCode+"\n"
				//end
				
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
				
				+"void NCSLabDiscreteUpdate(){\n"
				+"double dist;\n"
				+discreteUpdateCode+"\n"
				+"}\n"
				
				+"void NCSLabSinkOutput(){\n"
				+sinkOutputCode+"\n"
				+sinkStatusClearCode+"\n"
				+"}\n"
				
				+"void NCSLabTerminate(){\n"
				+terminateCode+"\n"
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


	//加入全局的Parameter的列表
	public void addParameter(Parameter parameter) {
		parameterList.add(parameter);
	}
	//加入全局的state的列表
	public void addState(State state) {
		stateList.add(state);
	}
	//加入全局的信号的列表
	public void addOutputSignal(OutputSignal outputSignal) {
		outputSignalList.add(outputSignal);
	}
	
	public void generateHardwareDefineCode() {
		hardwareDefineCode+="/*Define hardware structures*/\n";
		for(Block block:model.getBlockList()) {
			if(block.getIsHardware()) {
				hardwareDefineCode+=block.getHardwareDefineCodeC();
			}
		}
	}
	
	// generate code for defining parameters
	public void generateParameterDefineCode() {
		parameterDefineCode+="/*Define variables for parameters*/\n";
		for(Parameter parameter:parameterList) {
			//parameterDefineCode+=parameter.getDefineString()+" "+parameter.getName()+";\n";
			parameterDefineCode+=parameter.getDefineCodeC();
		}
	}

	//生成定义State的代码
	public void generateStateDefineCode() {
		stateDefineCode+="/*Define variables for states*/\n";
		for(State state:stateList) {
			stateDefineCode+=state.getDefineCodeC();
			//stateDefineCode+=state.getDefineString()+" "+state.getDerivativeName()+";\n";
		}
	}


	//生成定义Output的代码
	public void generateOutputSignalDefineCode() {
		outputSignalDefineCode+="/*Define variables for output signals*/\n";
		for(OutputSignal outputSignal:outputSignalList) {
			/*
			if(outputSignal.getWidth()==1) //compatible with former version
				outputSignalDefineCode+=outputSignal.getDefineString()+" "+outputSignal.getName()+";\n";
			else
				outputSignalDefineCode+=outputSignal.getDefineString()+" "+outputSignal.getName()+"["+outputSignal.getWidth()+"];\n";*/
			/*
			switch(outputSignal.getDataType()) {
			case REAL:
				outputSignalDefineCode+=outputSignal.getDefineString()+" "+outputSignal.getName()+";\n";
				break;
			case MATRIX:
				outputSignalDefineCode+=outputSignal.getDefineString()+" "+outputSignal.getName()+"["+outputSignal.getHeight()+"]["+outputSignal.getWidth()+"];\n";
				break;
			}*/
			outputSignalDefineCode+=outputSignal.getDefineCodeC();
		}
	}



	protected String codePathBase=Property.instance.getProperty("CCodePath");
	//目标文件夹的位置codePathBase/用户id/modelId
	protected String codePath;
	
	public String getCodePath() {
		return this.codePath;
	}

	protected void writeMakefile(String fileName) {
		System.out.println("Writing file "+fileName+"...");
		InputStream InputStream = this.getClass().getResourceAsStream(fileName);

		File file = new File(codePath+"/"+fileName);
		FileOutputStream outputStream;
		try {
			outputStream = new FileOutputStream(file);
			//ckeckout the s-function file
			String sfcn = "SFCNOBJS=";
			for(Block block : model.getBlockList())
			{
				if(block.isSFcnBlock()) {
					sfcn += 
					block.getSFcnName()+"_"+block.getBlockId()+".o ";
					for(String module : block.getSFunctionModuleList())
					{
						sfcn += module + ".o ";
					}
				}
			}
			sfcn += "\n";	
					
			byte[] buffer = sfcn.getBytes();
			outputStream.write(buffer,0,buffer.length);
			
			buffer = new byte[1024];
			
			int len;
			while((len=InputStream.read(buffer))>0) {
				outputStream.write(buffer,0,len);
			}
			
			outputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	//写文件的方法，将文件从resource中拷贝出来，写在目标文件夹
	protected void writeNCSLabFile(String fileName) {
		System.out.println("Writing file "+fileName+"...");
		InputStream InputStream = this.getClass().getResourceAsStream(fileName);

		File file=new File(codePath+"/"+fileName);
		if(file.exists()) {
			return;
		}
		FileOutputStream outputStream;
		try {
			outputStream = new FileOutputStream(file);						
			byte[] buffer = new byte[1024];			
			int len;
			while((len=InputStream.read(buffer))>0) {
				outputStream.write(buffer,0,len);
			}
			
			outputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	protected void writeNCSLabFile(String fileName, String fileNameOut) {
		// TODO: writeNCSLabFile(fileName, fileNameOut, false);
		writeNCSLabFile(fileName, fileNameOut, true);
	}
	
	protected void writeNCSLabFile(String fileName, String fileNameOut, boolean overwrite) {
		System.out.println("Writing file "+fileName+"...");
		InputStream InputStream = this.getClass().getResourceAsStream(fileName);

		File file=new File(codePath+"/"+fileNameOut);
		if(file.exists()&&overwrite==false) {
			return;
		}
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
	
	/*生成宏定义，定义各种数据结构的个数*/
	protected void wirteDefineFile() {
		
		String code="#define STATE_NUM "+stateList.size()+"\n";
		
		code+="#define SINGLE_STATE_NUM "+model.getSingleStateNum()+"\n";
		code+="#define MATRIX_STATE_NUM "+model.getMatrixStateNum()+"\n";
		
		code+="#define STEP_SIZE (1.0*"+model.getConfig().getFixedStep()+")\n";
		
		if(model.getModelMode()==ModelMode.Simulation) {
			code+="#define MAX_DATA_POINTS "+model.getConfig().getMaxDataPoints()+"\n";
		}
		
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
	
	protected byte[] readFile(String fileName) {
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

		//生成目标文件夹的位置codePathBase/用户id/modelId
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

		this.codePath=modelPath+"/";

		//写入周边的资源文件
		//makefile
		writeMakefile("makefile");
		//主数据结构
		writeNCSLabFile("ncslabccode.h");
		//main函数以及定时器
		writeNCSLabFile("ncslabmain.c");
		//访问主数据结构的接口API定义
		writeNCSLabFile("DataApi.c");
		writeNCSLabFile("DataApi.h");
		
		writeNCSLabFile("util.c");

		//实现Netcon协议的通用文件
		writeNCSLabFile("ServerThread.c");
		writeNCSLabFile("ServerThread.h");
		writeNCSLabFile("ClientThread.c");
		writeNCSLabFile("ClientThread.h");
		writeNCSLabFile("UploadThread.c");
		writeNCSLabFile("UploadThread.h");
		writeNCSLabFile("ncslabdefines.h");
		writeNCSLabFile("ncs_serialport_pi.c");
		writeNCSLabFile("ncs_serialport.h");
		writeNCSLabFile("hardware.c");
		writeNCSLabFile("hardware.h");

		//写入生成的主代码ncslabccdoe.c
		for(Block block: model.getBlockList()) {
			if(block.isSFcnBlock()) {
				block.generateSourceFile();
			}
		}

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
		case ode5:
			writeNCSLabFile("ode5.c","onestep.c");
			break;	
		case ode6:
			writeNCSLabFile("ode6.c","onestep.c");
			break;	
		default:
			System.out.println("error");
			break;
		}
		
	}

	public boolean makeExeFile() {
		try {
			//启动make，生成可执行代码
			Process process=Runtime.getRuntime().exec("make", null, new File(codePath));
			//读取OutputStream和errStream。如果读取不及时，会出现阻塞
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


			//等待makefile的完成
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


	//建立Model,block,input,output,signal,state,parameter等数据结构，并初始化
	public void gnenrateDataStructureCode() {
		//建立一系列数据结构的定义
		generateDataStrucure();
		//初始化数据结构，实现数据结构之间的指针连接
		generateDataStrucureInit();
	}

	//建立数据结构的定义
	private void generateDataStrucure() {
		dataStructureCode+="/*Define data structures*/\n";
		
		if(model.getModelMode()==ModelMode.Simulation) {
			dataStructureCode+="/*Define terminal structures*/\n";
		
			for(Terminal terminal:model.getTerminalList()) {
				dataStructureCode+=terminal.getDefineCodeC();
			}
			
			dataStructureCode+="TERMINAL *terminals["+model.getTerminalList().size()+"];\n";
		}

		dataStructureCode+="/*Define inputPort structures*/\n";
		for(Block block:model.getBlockList()) {
			if(block.getInputPortList().size()>0) {
				for(InputPort input:block.getInputPortList()) {
					dataStructureCode+="INPUT_PORT inputPort"+input.getBLock().getBlockId()+"_"+input.getNumber()+"={(char *)\""+input.getName()+"\","+input.getWidth()+"};\n";
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
					dataStructureCode+="OUTPUT_PORT outputPort"+output.getBLock().getBlockId()+"_"+output.getNumber()+"={(char *)\""+output.getName()+"\","+output.getWidth()+"};\n";
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
		int singleStateNum=0;
		int matrixStateNum=0;
		int stateNum=0;
		for(Block block:model.getBlockList()) {
			if(block.getStateList().size()>0) {
				for(State state:block.getStateList()) {
					dataStructureCode+="STATE state"+block.getBlockId()+"_"+state.getId()+"={(char *)\""+state.getLocalName()+"\","+state.getWidth()+"};\n";
					switch(state.getDataType()) {
					case REAL:
						singleStateNum++;
						break;
					case MATRIX:
						matrixStateNum++;
						break;
					}
				}
				dataStructureCode+="STATE *states"+block.getBlockId()+"["+block.getStateList().size()+"];\n";
			}
			else {
				dataStructureCode+="STATE **states"+block.getBlockId()+"=NULL;\n";
			}
		}
		stateNum=singleStateNum+matrixStateNum;
		dataStructureCode+="STATE *states["+stateNum+"];\n";
		model.setStateNum(singleStateNum,matrixStateNum);

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
				dataStructureCode+="BLOCK block"+block.getBlockId()+"={(char *)\""+block.getBlockType()+"\",(char *)\""+block.getBlockName()+"\","+block.getInputPortList().size()+","+block.getOutputPortList().size()+","+block.getParameterList().size()+","+block.getStateList().size()+","+block.getSignalNum()+","+model.getConfig().getStartTime()+",0};\n";
			}
			dataStructureCode+="BLOCK *blocks["+model.getBlockList().size()+"];\n";
		}
		else {
			dataStructureCode+="BLOCK **blocks=NULL";
		}
		dataStructureCode+="MODEL model={(char *)\""+model.getModelRealName()+"\","+model.getBlockList().size()+","+model.getConfig().getFixedStep()+","+model.getConfig().getStartTime()+","+model.getConfig().getStopTime()+"};\n";  
	}


	//初始化数据结构，实现数据结构之间的指针连接
	private void generateDataStrucureInit() {
		dataStructureInitCode+="/*Initialize data structure*/\n";
		
		if(model.getModelMode()==ModelMode.Simulation) {
			dataStructureInitCode+="/*Initialize terminals*/\n";
		
			int i=0;
			for(Terminal terminal:model.getTerminalList()) {
				dataStructureInitCode+="terminals["+i+"]=&"+terminal.getTerminalName()+";\n";
				i++;
			}
			
			dataStructureInitCode+="model.terminalNum="+model.getTerminalList().size()+";\n";
		}

		dataStructureInitCode+="/*Initialize inputs*/\n";
		for(Block block:model.getBlockList()) {
			dataStructureInitCode+="/*Initialize inputs for block ("+block.getBlockId()+")"+block.getBlockName()+"*/\n";
			for(InputPort input:block.getInputPortList()) {
				if(input.getLinkedLine().getLinkedOutputPort().getWidth()==1) {
					dataStructureInitCode+="inputPort"+block.getBlockId()+"_"+input.getNumber()+".vp=&"+input.getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName()+";\n";
				}else {
					dataStructureInitCode+="inputPort"+block.getBlockId()+"_"+input.getNumber()+".vp=&"+input.getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName()+";\n";
				}
			}
		}

		dataStructureInitCode+="/*Initialize outputs*/\n";
		for(Block block:model.getBlockList()) {
			dataStructureInitCode+="/*Initialize outputs for block ("+block.getBlockId()+")"+block.getBlockName()+"*/\n";
			for(OutputPort output:block.getOutputPortList()) {
				if(output.getWidth()==1)
					dataStructureInitCode+="outputPort"+block.getBlockId()+"_"+output.getNumber()+".vp=&"+output.getOutputSignalC().getName()+";\n";
				else
					dataStructureInitCode+="outputPort"+block.getBlockId()+"_"+output.getNumber()+".vp=&"+output.getOutputSignalC().getName()+";\n";
			}
		}

		dataStructureInitCode+="/*Initialize parameters*/\n";
		for(Block block:model.getBlockList()) {
			dataStructureInitCode+="/*Initialize parameters for block ("+block.getBlockId()+")"+block.getBlockName()+"*/\n";
			for(Parameter parameter:block.getParameterList()) {
				dataStructureInitCode+=parameter.getDataStructureInitCodeC();
			}
		}

		dataStructureInitCode+="/*Initialize states*/\n";
		for(Block block:model.getBlockList()) {
			dataStructureInitCode+="/*Initialize states for block ("+block.getBlockId()+")"+block.getBlockName()+"*/\n";
			for(State state:block.getStateList()) {
				dataStructureInitCode+="state"+block.getBlockId()+"_"+state.getId()+".height="+state.getHeight()+";\n";
				dataStructureInitCode+="state"+block.getBlockId()+"_"+state.getId()+".width="+state.getWidth()+";\n";
				switch(state.getDataType()) {
				case REAL:
					dataStructureInitCode+="state"+block.getBlockId()+"_"+state.getId()+".type=SINGLE;\n";
					break;
				case MATRIX:
					dataStructureInitCode+="state"+block.getBlockId()+"_"+state.getId()+".type=MATRIX;\n";
					break;
				}
				dataStructureInitCode+="state"+block.getBlockId()+"_"+state.getId()+".vp=&"+state.getName()+";\n";
				dataStructureInitCode+="state"+block.getBlockId()+"_"+state.getId()+".dvp=&"+state.getDerivativeName()+";\n";
			}
		}

		dataStructureInitCode+="/*Initialize signals*/\n";
		for(Block block:model.getBlockList()) {
			dataStructureInitCode+="/*Initialize signals for block ("+block.getBlockId()+")"+block.getBlockName()+"*/\n";
			if(inputAsSignal) {
				for(InputPort input:block.getInputPortList()) {
					dataStructureInitCode+=input.getDataStructureInitCodeC();
				}
			}
			if(outputAsSignal) {
				for(OutputPort output:block.getOutputPortList()) {
					dataStructureInitCode+=output.getDataStructureInitCodeC();
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
		dataStructureInitCode+="model.discreteTime=model.startTime;\n";

		dataStructureInitCode+="model.signalNum="+signalNum+";\n";
		dataStructureInitCode+="model.signals=signals;\n";

		dataStructureInitCode+="model.parameterNum="+parameterNum+";\n";
		dataStructureInitCode+="model.parameters=parameters;\n";

		dataStructureInitCode+="model.stateNum="+stateNum+";\n";
		dataStructureInitCode+="model.states=states;\n";
	}

	public void addStatementCode(String code) {
		// TODO Auto-generated method stub
		this.statementCode += code;
	}

}
