package code.m;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Vector;

import block.io.OutputSignal;
import block.io.Parameter;
import block.io.State;
import code.c.CodeModelC;

public class CodeStructM {
	//init初始化的代码
	public String initCode="";
	//Output的代码
	public String outputCode="";
	//update的代码
	public String updateCode="";

	//derivative的代码
	public String derivativeCode="";

	public String globalDefineCode="";

	private String codePathBase=utils.Property.instance.getProperty("MCodePath");
	//目标文件夹的位置codePathBase/用户id/modelId
	private String codePath;

	private Vector<Parameter> parameterList=new Vector<Parameter>();
	private Vector<State> stateList=new Vector<State>();
	private Vector<OutputSignal> outputSignalList=new Vector<OutputSignal>();

	private CodeModelM model;

	public CodeStructM(CodeModelM model) {
		this.model=model;

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

	public String getMainCode() {
		return initCode+outputCode+updateCode+derivativeCode;
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
	
	public void addGlobalDefineCode(String code) {
		globalDefineCode+=code;
	}

	//生成定义global的代码
	public void generateGlobalDefineCode() {
		for(Parameter parameter:parameterList) {
			globalDefineCode+="global "+parameter.getName()+";\n";
		}
		for(OutputSignal outputSignal:outputSignalList) {
			globalDefineCode+="global "+outputSignal.getName()+";\n";
		}
		for(State state:stateList) {
			globalDefineCode+="global "+state.getName()+";\n";
			globalDefineCode+="global "+state.getDerivativeName()+";\n";
		}
	}

	public void writeMCodeFiles() {

		generateGlobalDefineCode();

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

		codePath=modelPath+"/";

		//writeInitFunction();
		//writeOutputFunction();
		//writeDerivativeFunction();
		//writeUpdateFunction();
		
		writeNCSLabMainCode();
		
		//writeNCSLabFile("ncslabmain.m","clear all;\n"
		//								+globalDefineCode
		//								);
		
		//writeNCSLabFile("ode1.m","function ode1\n"+globalDefineCode,"end\n");
	}
	
	private String getOde1Code() {
		String code="";
		code+="offset=0;\n";
		code+="storeEnable=1;\n";
		code+=outputCode;
		code+="storeEnable=0;\n";
		code+=derivativeCode;
		code+="stepSize="+model.getConfig().getFixedStep()+";\n";
		code+=updateCode;
		
		return code;
	}
	
	private String getStateStoreCode(int num) {
		String code="";
		for(State state:stateList) {
			code+=state.getName()+num+"="+state.getName()+";\n";
		}
		return code;
	}
	
	private String getStateRestoreCode(int num) {
		String code="";
		for(State state:stateList) {
			code+=state.getName()+"="+state.getName()+num+";\n";
		}
		return code;
	}
	
	private String getDerivativeStoreCode(int num) {
		String code="";
		for(State state:stateList) {
			code+=state.getDerivativeName()+num+"="+state.getDerivativeName()+";\n";
		}
		return code;
	}
	
	private String getOde2Code() {
		String code="";
		code+="offset=0;\n";
		code+="storeEnable=1;\n";
		code+=outputCode;
		code+="storeEnable=0;\n";
		
		
		code+="%Calculate K0\n";
		code+=derivativeCode;
		//code+=getDerivativeStoreCode(0);
		
		code+="%Calculate K1\n";
		code+=getStateStoreCode(0);
		code+="stepSize="+model.getConfig().getFixedStep()/2+";\n";
		code+="offset="+model.getConfig().getFixedStep()/2+";\n";
		code+=updateCode;
		code+=outputCode;
		code+=derivativeCode;
		//code+=getDerivativeStoreCode(1);
		
		code+="%update\n";
		code+=getStateRestoreCode(0);
		code+="stepSize="+model.getConfig().getFixedStep()+";\n";
		//code+=derivativeCode;
		code+=updateCode;
		
		return code;
	}
	
	private String calculateDerivativeCode(double[] weights) {
		String code="";
		for(State state:stateList) {
			code+=state.getDerivativeName()+"=0";
			int i=0;
			for(double w:weights) {
				code+="+"+weights[i]+"*"+state.getDerivativeName()+i;
				i++;
			}
			code+=";\n";
		}
		return code;
	}
	
	private String getOde3Code() {
		String code="";
		
		code+="offset=0;\n";
		code+="storeEnable=1;\n";
		code+=outputCode;
		code+="storeEnable=0;\n";
		
		
		code+="%Calculate K0\n";
		code+=derivativeCode;
		code+=getDerivativeStoreCode(0);
		
		code+="%Calculate K1\n";
		code+=getStateStoreCode(0);
		code+="stepSize="+model.getConfig().getFixedStep()/2+";\n";
		code+=updateCode;
		code+="offset="+model.getConfig().getFixedStep()/2+";\n";
		code+=outputCode;
		code+=derivativeCode;
		code+=getDerivativeStoreCode(1);
		
		code+="%Calculate K2\n";
		code+=getStateRestoreCode(0);
		code+="stepSize="+model.getConfig().getFixedStep()+";\n";
		double[] weights= {-1,2};
		code+=calculateDerivativeCode(weights);
		code+=updateCode;
		code+="offset="+model.getConfig().getFixedStep()+";\n";
		code+=outputCode;
		code+=derivativeCode;
		code+=getDerivativeStoreCode(2);
		
		code+="%update\n";
		code+=getStateRestoreCode(0);
		code+="stepSize="+model.getConfig().getFixedStep()+";\n";
		double[] weights1= {1.0/6,4.0/6,1.0/6};
		code+=calculateDerivativeCode(weights1);
		code+=updateCode;
		
		return code;
	}
	
	private String getOde4Code() {
		String code="";
		
		code+="offset=0;\n";
		code+="storeEnable=1;\n";
		code+=outputCode;
		code+="storeEnable=0;\n";
		
		
		code+="%Calculate K0\n";
		code+=derivativeCode;
		code+=getDerivativeStoreCode(0);
		
		code+="%Calculate K1\n";
		code+=getStateStoreCode(0);
		code+="stepSize="+model.getConfig().getFixedStep()/2+";\n";
		code+=updateCode;
		code+="offset="+model.getConfig().getFixedStep()/2+";\n";
		code+=outputCode;
		code+=derivativeCode;
		code+=getDerivativeStoreCode(1);
		
		code+="%Calculate K2\n";
		code+=getStateRestoreCode(0);
		code+="stepSize="+model.getConfig().getFixedStep()/2+";\n";
		code+=updateCode;
		code+="offset="+model.getConfig().getFixedStep()/2+";\n";
		code+=outputCode;
		code+=derivativeCode;
		code+=getDerivativeStoreCode(2);
		
		code+="%Calculate K3\n";
		code+=getStateRestoreCode(0);
		code+="stepSize="+model.getConfig().getFixedStep()+";\n";
		code+=updateCode;
		code+="offset="+model.getConfig().getFixedStep()+";\n";
		code+=outputCode;
		code+=derivativeCode;
		code+=getDerivativeStoreCode(3);
		
		code+="%update\n";
		code+=getStateRestoreCode(0);
		code+="stepSize="+model.getConfig().getFixedStep()+";\n";
		double[] weights1= {1.0/6,2.0/6,2.0/6,1.0/6};
		code+=calculateDerivativeCode(weights1);
		code+=updateCode;
		
		return code;
	}
	
	private void writeNCSLabMainCode() {
		String fileName="ncslabmain.m";

		String code="clear all\n";
		code+=initCode;
		code+="for t="+model.getConfig().getStartTime()+":"+model.getConfig().getFixedStep()+":"+model.getConfig().getStopTime()+"\n";
		
		switch(model.getSolver()) {
		case ode1:
			code+=getOde1Code();
			break;
		case ode2:
			code+=getOde2Code();
			break;
		case ode3:
			code+=getOde3Code();
			break;
		case ode4:
			code+=getOde4Code();
			break;
		}
		
		code+="end\n";
		
		writeFile(fileName,code);
	}
	
	private void writeFile(String fileName,String code) {
		System.out.println("Writing file "+fileName+" ...");

		File file=new File(codePath+"/"+fileName);
		FileOutputStream outputStream;
		try {
			outputStream = new FileOutputStream(file);
			outputStream.write(code.getBytes());
			outputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/*
	private void writeNCSLabMainCode() {
		String fileName="ncslabmain.m";

		String code="clear all\n";
		code+=globalDefineCode;
		code+="modelInit;\n";
		code+="for t="+model.getConfig().getStartTime()+":"+model.getConfig().getFixedStep()+":"+model.getConfig().getStopTime()+"\n";
		code+="modelOutput;\n";
		code+="modelDerivative;\n";
		code+="modelUpdate;\n";
		code+="end\n";
		
		code+="function modelInit\n";
		code+=globalDefineCode;
		code+=initCode;
		code+="end\n";
		
		code+="function modelOutput\n";
		code+=globalDefineCode;
		code+=outputCode;
		code+="end\n";
		
		code+="function modelDerivative\n";
		code+=globalDefineCode;
		code+=derivativeCode;
		code+="end\n";
		
		code+="function modelUpdate\n";
		code+=globalDefineCode;
		code+=updateCode;
		code+="end\n";
		
		writeFile(fileName,code);
	}*/
	/*
	private void writeFile(String fileName,String code) {
		System.out.println("Writing file "+fileName+" ...");

		File file=new File(codePath+"/"+fileName);
		FileOutputStream outputStream;
		try {
			outputStream = new FileOutputStream(file);
			outputStream.write(code.getBytes());
			outputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void writeInitFunction() {
		String fileName="modelInit.m";

		String code="function modelInit\n";
		code+=globalDefineCode;
		code+=initCode;
		code+="end\n";

		writeFile(fileName,code);
	}

	private void writeOutputFunction() {
		String fileName="modelOutput.m";

		String code="function modelOutput\n";
		code+=globalDefineCode;
		code+=outputCode;
		code+="end\n";

		writeFile(fileName,code);
	}

	private void writeDerivativeFunction() {
		String fileName="modelDerivative.m";

		String code="function modelDerivative\n";
		code+=globalDefineCode;
		code+=derivativeCode;
		code+="end\n";

		writeFile(fileName,code);
	}

	private void writeUpdateFunction() {
		String fileName="modelUpdate.m";

		String code="function modelUpdate\n";
		code+=globalDefineCode;
		code+=updateCode;
		code+="end\n";

		writeFile(fileName,code);
	}
	
	private void writeNCSLabFile(String fileName) {
		writeNCSLabFile(fileName,"","");
	}
	
	//写文件的方法，将文件从resource中拷贝出来，写在目标文件夹
	private void writeNCSLabFile(String fileName,String preCode,String sufCode) {
		System.out.println("Writing file "+fileName+"...");
		InputStream InputStream = this.getClass().getResourceAsStream(fileName);

		File file=new File(codePath+"/"+fileName);
		FileOutputStream outputStream;
		try {
			outputStream = new FileOutputStream(file);
			
			outputStream.write(preCode.getBytes());
			
			byte[] buffer=new byte[1024];
			int len;
			while((len=InputStream.read(buffer))>0) {
				outputStream.write(buffer,0,len);
			}
			
			outputStream.write(sufCode.getBytes());
			outputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}*/
}
