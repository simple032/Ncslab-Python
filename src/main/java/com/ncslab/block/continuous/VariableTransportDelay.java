package com.ncslab.block.continuous;

import lombok.Getter;
import org.json.JSONObject;

import com.ncslab.block.Block;
import com.ncslab.block.data.DataType;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;

import java.util.Vector;

public class VariableTransportDelay extends Block{

	Parameter DelayType;
	Parameter MaxDelayTime;
	Parameter InitialOutput;
	Parameter InitialBuffsize;
	Parameter PadeOrder;


    @Getter
    public static final Vector<String> parameterNames = new Vector<>();

    @Getter
    public static final Vector<String> outputNames = new Vector<>();
    @Getter
    public static final Vector<String> inputNames = new Vector<>();

    static {
        parameterNames.add("DelayType");
        parameterNames.add("MaxDelayTime");
        parameterNames.add("InitialOutput");
        parameterNames.add("InitialBuffsize");
        parameterNames.add("PadeOrder");
        outputNames.add("out1");
        inputNames.add("in1");
        inputNames.add("in2");
    }

	public VariableTransportDelay(JSONObject blockIn, NCSLabModel model) {
		super(blockIn, model);

		//2个输入，1个输出
		inputPortList.add(new InputPort(this,1));
		inputPortList.add(new InputPort(this,2));
		outputPortList.add(new OutputPort(this,1,true));

		DelayType=new Parameter(this,parameterList.size()+1,"DelayType",paramValues.getString("VariableDelayType"));
		parameterList.add(DelayType);
		MaxDelayTime=new Parameter(this,parameterList.size()+1,"MaxDelayTime",paramValues.getString("MaxDelayTime"));
		parameterList.add(MaxDelayTime);
		InitialOutput=new Parameter(this,parameterList.size()+1,"InitialOutput",paramValues.getString("InitialOutput"));
		parameterList.add(InitialOutput);
		InitialBuffsize=new Parameter(this,parameterList.size()+1,"InitialBuffsize",paramValues.getString("InitialBuffsize"));
		parameterList.add(InitialBuffsize);
		PadeOrder=new Parameter(this,parameterList.size()+1,"PadeOrder",paramValues.getString("PadeOrder"));
		parameterList.add(PadeOrder);
	}


	public void generateArraysCodeC(CodeStructC code) {
		 String arraysCode="/*Define arrays for block VariableTransportDelay:("+getBlockId()+")"+getBlockName()+"*/\n";

//		 arraysCode+="int VariableTransportDelay"+getBlockId()+"BuffSize="+paramValues.getInt("InitialBuffsize")+";\n";
//		 arraysCode+="if("+paramValues.getInt("MaxDelayTime")+"/STEP_SIZE>"+paramValues.getInt("InitialBuffsize")+"){\n"
//		 		   + "VariableTransportDelay"+getBlockId()+"BuffSize=(int)("+paramValues.getInt("MaxDelayTime")+"/STEP_SIZE);}\n";
//		 arraysCode+="double "+"Block"+getBlockId()+"VariableTransportDelay_saveData[VariableTransportDelay"+getBlockId()+"BuffSize]={0};\n";
//		 arraysCode+="for(int i=0;i<VariableTransportDelay"+getBlockId()+"BuffSize;i++){\n"
//		 		   + "Block"+getBlockId()+"VariableTransportDelay_saveData[i]="+paramValues.getDouble("InitialOutput")+";\n"
//		 		   + "}\n";

		 arraysCode+="int VariableTransportDelay"+getBlockId()+"BuffSize=("+paramValues.getDouble("MaxDelayTime")+"/STEP_SIZE+1>"+paramValues.getString("InitialBuffsize")+")?"
		 		   + "((int)("+paramValues.getDouble("MaxDelayTime")+"/STEP_SIZE+1)):"+paramValues.getString("InitialBuffsize")+";\n";
//		 arraysCode+="double "+"Block"+getBlockId()+"VariableTransportDelay_saveData["+paramValues.getInt("InitialBuffsize")+"]={0};\n";
		 arraysCode+="double *Block"+getBlockId()+"VariableTransportDelay_saveData;\n";
		 arraysCode+="double *Block"+getBlockId()+"VariableTransport_Ti_saveData;\n";

		 code.addArraysCode(arraysCode);
	 }

	public void generateInitCodeC(CodeStructC code) {
		super.generateInitCodeC(code);
		String initCode="/*Code for initialization of block VariableTransportDelay:("+getBlockId()+")"+getBlockName()+"*/\n";

		initCode+=MaxDelayTime.getInitCodeC();
		initCode+=PadeOrder.getInitCodeC();

		initCode+="Block"+getBlockId()+"VariableTransportDelay_saveData=(double *)malloc(VariableTransportDelay"+getBlockId()+"BuffSize*sizeof(double));\n";
		initCode+="Block"+getBlockId()+"VariableTransport_Ti_saveData=(double *)malloc(VariableTransportDelay"+getBlockId()+"BuffSize*sizeof(double));\n";

		initCode+="for(int i=0;i<VariableTransportDelay"+getBlockId()+"BuffSize;i++){\n"
		 		   + "Block"+getBlockId()+"VariableTransportDelay_saveData[i]="+paramValues.getDouble("InitialOutput")+";\n"
		 		   + "Block"+getBlockId()+"VariableTransport_Ti_saveData[i]=0;\n"
		 		   + "}\n";

		code.addInitCode(initCode);
	}

	 public void generateOutputCodeC (CodeStructC code){
		  String outputCode="/*Code for output of block VariableTransportDelay:("+getBlockId()+")"+getBlockName()+"*/\n";

		  OutputPort out  = outputPortList.get(0);
		  OutputPort ops = inputPortList.get(0).getLinkedLine().getLinkedOutputPort();
		  OutputSignal signal=inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
		  OutputSignal signal2=inputPortList.get(1).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
		  outputCode+="if(mp->majorStep>0) {\n";


		  outputCode+="for(int i=VariableTransportDelay"+getBlockId()+"BuffSize-1;i>0;i--){\n"
		 		   + "Block"+getBlockId()+"VariableTransportDelay_saveData[i]=Block"+getBlockId()+"VariableTransportDelay_saveData[i-1];\n"
		 		   + "Block"+getBlockId()+"VariableTransport_Ti_saveData[i]=Block"+getBlockId()+"VariableTransport_Ti_saveData[i-1];\n"
		 		   + "}\n"
		 		   + "Block"+getBlockId()+"VariableTransportDelay_saveData[0]="+signal.getName()+";\n";

		  outputCode+="{real_T delayTime = "+signal2.getName()+";\n"
				  	+ "if(delayTime>="+MaxDelayTime.getName()+"){delayTime="+MaxDelayTime.getName()+";}\n"
				  	+ "else if(delayTime<=0) {delayTime=STEP_SIZE;}\n";

		  outputCode+= "Block"+getBlockId()+"VariableTransport_Ti_saveData[0]=1.0/delayTime;\n";

		  if(paramValues.getString("VariableDelayType").equals("Variable Time Delay")) {
			  outputCode+= outputPortList.get(0).getOutputSignalC().getName()+"=Block"+getBlockId()+"VariableTransportDelay_saveData[(int)floor(delayTime/STEP_SIZE)];\n";
		  }else {
			  outputCode+="int delayTimeIndex = 0;\n"
			  			+ "double intergSum=0;\n"
			  			+ "for(int i=0;i<VariableTransportDelay"+getBlockId()+"BuffSize;i++){\n"
			  			+ "	intergSum+=STEP_SIZE*Block"+getBlockId()+"VariableTransport_Ti_saveData[i];\n"
			  			+ "	delayTimeIndex=i;\n"
			  			+ "	if(intergSum>=1) {break;}\n"
			  			+ "}\n"

			  			+ outputPortList.get(0).getOutputSignalC().getName()+"=Block"+getBlockId()+"VariableTransportDelay_saveData[delayTimeIndex];\n";

			  outputCode+="}\n";
		  }

		  outputCode+="}\n";

		  code.addOutputCode(outputCode);

	 }

	 public void updateDimension() throws MatDimException{
			OutputPort out  = outputPortList.get(0);
			InputPort in  = inputPortList.get(0);
			OutputSignal signal=in.getLinkedLine().getLinkedOutputPort().getOutputSignalC();

			out.setHeight(signal.getHeight());
			out.setWidth(signal.getWidth());
			out.getOutputSignalC().setHeight(signal.getHeight());
			out.getOutputSignalC().setWidth(signal.getWidth());
			out.getOutputSignalC().setDataType(signal.getDataType());
		}
	public void checkDimension() throws MatDimException{
		 if(MaxDelayTime.getDataType()!=DataType.REAL||InitialOutput.getDataType()!=DataType.REAL||InitialBuffsize.getDataType()!=DataType.REAL||PadeOrder.getDataType()!=DataType.REAL) {
				MatDimException e=new MatDimException("Parameter of Block "+this.blockName+" can't be Matrix!\n \n");
				throw(e);
			}
	}
}
