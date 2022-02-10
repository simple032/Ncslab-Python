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

	private String mainCode="";

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
		//return initCode+outputCode+updateCode+derivativeCode;
		return mainCode;
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
	
	private String calculateDerivativeCode(double[] weights) {
		String code="";
		for(State state:stateList) {
			code+=state.getDerivativeName()+"=0";
			int i=0;
			for(double w:weights) {
				code+="+("+weights[i]+")*"+state.getDerivativeName()+i;
				i++;
			}
			code+=";\n";
		}
		return code;
	}
	
	private String calculateStateDifCode(int seq1,int seq2) {
		String code="";
		code+="dif=0;\n";
		for(State state:stateList) {
			if(state.getWidth()==1&&state.getHeight()==1) {
				code+="if dif<abs("+state.getName()+seq1+"-"+state.getName()+seq2+");\n";
				code+="dif=abs("+state.getName()+seq1+"-"+state.getName()+seq2+");\n";
				code+="end\n";
			}
			else {
				for(int i=0;i<state.getHeight();i++) {
					for(int j=0;j<state.getWidth();j++) {
						code+="if dif<abs("+state.getName()+seq1+"("+(i+1)+","+(j+1)+")"+"-"+state.getName()+seq2+"("+(i+1)+","+(j+1)+")"+");\n";
						code+="dif=abs("+state.getName()+seq1+"("+(i+1)+","+(j+1)+")"+"-"+state.getName()+seq2+"("+(i+1)+","+(j+1)+")"+");\n";
						code+="end\n";
					}
				}
			}
		}
		return code;
	}
	
	/*
	private String getDerivativeCode(double[] weight) {
		String code="";
		
		for(State state:stateList) {
			code+=state.getDerivativeName()+"=0";
			for(int i=0;i<weight.length;i++) {
				code+="+"+state.getDerivativeName()+i+"*("+weight[i]+")";
			}
			code+=";\n";
		}
		
		return code;
	}*/
	
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
	

	private String getOde23Code() {
		String code="";
		
		code+="time=[time t];\n";
		
		code+="offset=0;\n";
		code+="storeEnable=1;\n";
		code+=outputCode;
		code+="storeEnable=0;\n";
		
		code+=getStateStoreCode(0);
		
		code+="for i=1:2\n";
		
		code+="%Calculate K1\n";
		code+=derivativeCode;
		code+=getDerivativeStoreCode(0);
		
		code+="%Calculate K2\n";
		code+=getStateRestoreCode(0);
		code+="stepSize=h/4;\n";
		code+=updateCode;
		code+="offset=h/4;\n";
		code+=outputCode;
		code+=derivativeCode;
		code+=getDerivativeStoreCode(1);
		
		code+="%Calculate K3\n";
		code+=getStateRestoreCode(0);
		code+="stepSize=h*3.0/8.0;\n";
		double[] weights3= {1.0/4.0,3.0/4.0};
		code+=calculateDerivativeCode(weights3);
		code+=updateCode;
		code+="offset=h*3.0/8.0;\n";
		code+=outputCode;
		code+=derivativeCode;
		code+=getDerivativeStoreCode(2);
		
		code+="%Calculate K4\n";
		code+=getStateRestoreCode(0);
		code+="stepSize=h*12.0/13.0;\n";
		double[] weights4= {1932.0/2028.0,-7200.0/2028.0,7296.0/2028.0};
		code+=calculateDerivativeCode(weights4);
		code+=updateCode;
		code+="offset=h*12.0/13.0;\n";
		code+=outputCode;
		code+=derivativeCode;
		code+=getDerivativeStoreCode(3);
		
		code+="%Calculate K5\n";
		code+=getStateRestoreCode(0);
		code+="stepSize=h;\n";
		double[] weights5= {439.0/216.0,-8.0,3680.0/513.0,-845.0/4104.0};
		code+=calculateDerivativeCode(weights5);
		code+=updateCode;
		code+="offset=h;\n";
		code+=outputCode;
		code+=derivativeCode;
		code+=getDerivativeStoreCode(4);
		
		code+="%Calculate K6\n";
		code+=getStateRestoreCode(0);
		code+="stepSize=h/2.0;\n";
		double[] weights6= {-8.0/27.0*2.0,2.0*2.0,-3544.0/2565.0*2.0,1859.0/4104.0*2.0,-11.0/40.0*2.0};
		code+=calculateDerivativeCode(weights6);
		code+=updateCode;
		code+="offset=h/2.0;\n";
		code+=outputCode;
		code+=derivativeCode;
		code+=getDerivativeStoreCode(5);
		
		code+="%update\n";
		code+=getStateRestoreCode(0);
		code+="stepSize=h;\n";
		double[] weightss1= {25.0/216.0,0.0,1408.0/2565.0,2197.0/4104.0,-1.0/5.0};
		//double[] weights= {16.0/135.0, 0.0, 6656.0/12825.0, 28561.0/56430.0,-9.0/50.0,2.0/55.0};
		code+=calculateDerivativeCode(weightss1);
		code+=updateCode;
		code+=getStateStoreCode(1);
		
		code+="%update\n";
		code+=getStateRestoreCode(0);
		code+="stepSize=h;\n";
		//double[] weights= {25.0/216.0,0.0,1408.0/2565.0,2197.0/4104.0,-1.0/5.0};
		double[] weightss2= {16.0/135.0, 0.0, 6656.0/12825.0, 28561.0/56430.0,-9.0/50.0,2.0/55.0};
		code+=calculateDerivativeCode(weightss2);
		code+=updateCode;
		code+=getStateStoreCode(2);
		
		
		code+=calculateStateDifCode(1,2);
		
		code+="nh=((tol*h)/dif)^0.25*0.84*h;\n";
		
		//code+="if nh<h || i==2 ||t!="+model.getConfig().getStartTime()+" \n";
		code+="if nh>h || i==2 \n";
		code+="break;\n";
		code+="end\n";
		
		code+="h=nh;\n";
		code+=getStateRestoreCode(0);
		code+="offset=0;\n";
		code+=outputCode;
		
		code+="end\n";
		
		
		return code;
	}
	
	private void writeNCSLabMainCode() {
		String fileName="ncslabmain"+model.getModelSeq()+".m";
		
		
		String code="%clear all\n";
		code+="ScopeNum=0;\n";
		code+="ScopeList=[];\n";
		code+=initCode;
		
		switch(model.getSolver())
		{
		case ode1:
		case ode2:
		case ode3:
		case ode4:
			code+="time="+model.getConfig().getStartTime()+":"+model.getConfig().getFixedStep()+":"+model.getConfig().getStopTime()+";\n";
			code+="for t="+model.getConfig().getStartTime()+":"+model.getConfig().getFixedStep()+":"+model.getConfig().getStopTime()+"\n";
			code+="h="+model.getConfig().getFixedStep()+";\n";
			break;
		
		case ode23:
			code+="time=[];\n";
			code+="t="+model.getConfig().getStartTime()+";\n";
			code+="h=0.01;\n";
			code+="tol=1E-6;\n";
			code+="while t<"+model.getConfig().getStopTime()+"\n";
			break;
		}
		
		
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

		case ode23:
			code+=getOde23Code();
			break;
		}
		
		switch(model.getSolver())
		{
		case ode1:
		case ode2:
		case ode3:
		case ode4:
			code+="end\n";
			break;
		case ode23:
			code+="t=t+h;\n";
			code+="h=nh;\n;";
			code+="end\n";
			break;
		}		
		
		mainCode=code;

		
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
	

	public String getCodePath() {
		return codePath;
	}
}
