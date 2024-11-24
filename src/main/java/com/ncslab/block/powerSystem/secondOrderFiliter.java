package com.ncslab.block.powerSystem;

import com.ncslab.block.Block;

import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.Getter;
import org.json.JSONArray;
import org.json.JSONObject;
import com.ncslab.block.Block;
import com.ncslab.block.data.DataType;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;
public class secondOrderFiliter extends Block{
	private double[] ACInitialInputs;

	Parameter filterType;
	Parameter naturalFrequency;
	Parameter dampingRatio;
	Parameter sampleTime;
	Parameter initState;
	Parameter DCInitialInput;

    @Getter
    public static final Vector<String> parameterNames = new Vector<>();

    @Getter
    public static final Vector<String> outputNames = new Vector<>();
    @Getter
    public static final Vector<String> inputNames = new Vector<>();

    static {

        outputNames.add("out1");
        inputNames.add("in1");
        parameterNames.add("filterType");
        parameterNames.add("naturalFrequency");
        parameterNames.add("dampingRatio");
        parameterNames.add("sampleTime");
        parameterNames.add("initState");
        parameterNames.add("DCInitialInput");
    }

	public secondOrderFiliter(JSONObject blockIn, NCSLabModel model) {
		super(blockIn, model);

		if(paramValues.getString("secondInitState").equals("on")) parseVector();

		//1个输入，1个输出
		inputPortList.add(new InputPort(this,1));
		outputPortList.add(new OutputPort(this,1,true));

		filterType=new Parameter(this,parameterList.size()+1,"filterType",paramValues.getString("secondFilterType"));
		parameterList.add(filterType);
		naturalFrequency=new Parameter(this,parameterList.size()+1,"naturalFrequency",paramValues.getString("secondFrequency"));
		parameterList.add(naturalFrequency);
		dampingRatio=new Parameter(this,parameterList.size()+1,"dampingRatio",paramValues.getString("secondDampingRatio"));
		parameterList.add(dampingRatio);
		sampleTime=new Parameter(this,parameterList.size()+1,"sampleTime",paramValues.getString("sampleTime"));
		parameterList.add(sampleTime);
		initState=new Parameter(this,parameterList.size()+1,"initState",paramValues.getString("secondInitState"));
		parameterList.add(initState);
		DCInitialInput=new Parameter(this,parameterList.size()+1,"DCInitialInput",paramValues.getString("secondDCInput"));
		parameterList.add(DCInitialInput);

	}
	public void generateArraysCodeC(CodeStructC code) {
		 String arraysCode="/*Define arrays for block Second-Order Filter:("+getBlockId()+")"+getBlockName()+"*/\n";

		 arraysCode+="double "+"Block"+getBlockId()+"SecondOrderFilter_b[3]={0};\n";
		 arraysCode+="double "+"Block"+getBlockId()+"SecondOrderFilter_a[2]={0};\n";
		 arraysCode+="double "+"Block"+getBlockId()+"SecondOrderFilter_x[2]={0};\n";
		 arraysCode+="double "+"Block"+getBlockId()+"SecondOrderFilter_y[2]={0};\n";
		 code.addArraysCode(arraysCode);
	 }
	public void generateInitCodeC(CodeStructC code) {
		super.generateInitCodeC(code);
		String initCode="/*Code for initialization of block Second-Order Filter:("+getBlockId()+")"+getBlockName()+"*/\n";

		initCode+=naturalFrequency.getInitCodeC();
		initCode+=dampingRatio.getInitCodeC();
		initCode+=sampleTime.getInitCodeC();
		if(paramValues.getString("secondInitState").equals("on")) initCode+=DCInitialInput.getInitCodeC();

		if(paramValues.getString("sampleTime").equals("0")||paramValues.getString("sampleTime").equals("-1")) {
			initCode+="double T=STEP_SIZE;\n";
		}else {
			initCode+="double T="+sampleTime.getName()+";\n";
		}
		initCode+="double omega_n = 2 * 3.14159265358979323846 * "+naturalFrequency.getName()+";\n"
				 +"double w2T2 = omega_n*omega_n*T*T;\n"
				 ;
		initCode+="double a0 = 4 + 4 * "+dampingRatio.getName()+"*omega_n*T + w2T2;\n"
				 +"Block"+getBlockId()+"SecondOrderFilter_a[0]=(-8 + 2 * w2T2) / a0;\n"
				 +"Block"+getBlockId()+"SecondOrderFilter_a[1]=(4 - 4 * "+dampingRatio.getName()+"*omega_n*T + w2T2) / a0;\n";

		if(paramValues.getString("secondFilterType").equals("Lowpass")) {
			initCode+="Block"+getBlockId()+"SecondOrderFilter_b[0]=(w2T2) / a0;\n"
					 +"Block"+getBlockId()+"SecondOrderFilter_b[1]=(2 * w2T2) / a0;\n"
					 +"Block"+getBlockId()+"SecondOrderFilter_b[2]=(w2T2) / a0;\n";
		}else if(paramValues.getString("secondFilterType").equals("Highpass")) {
			initCode+="Block"+getBlockId()+"SecondOrderFilter_b[0]=(4) / a0;\n"
					 +"Block"+getBlockId()+"SecondOrderFilter_b[1]=(-8) / a0;\n"
					 +"Block"+getBlockId()+"SecondOrderFilter_b[2]=(4) / a0;\n";
		}else if(paramValues.getString("secondFilterType").equals("Bandpass")) {//Bandpass
			initCode+="Block"+getBlockId()+"SecondOrderFilter_b[0]=(4 * omega_n * T * "+dampingRatio.getName()+") / a0;\n"
					 +"Block"+getBlockId()+"SecondOrderFilter_b[1]= 0;\n"
					 +"Block"+getBlockId()+"SecondOrderFilter_b[2]=(-4 * omega_n * T *"+dampingRatio.getName()+") / a0;\n";
		}
		else {//Bandstop(Notch)
			initCode+="Block"+getBlockId()+"SecondOrderFilter_b[0]=(4 * omega_n * T * "+dampingRatio.getName()+") / a0;\n"
					 +"Block"+getBlockId()+"SecondOrderFilter_b[1]= 0;\n"
					 +"Block"+getBlockId()+"SecondOrderFilter_b[2]=(-4 * omega_n * T *"+dampingRatio.getName()+") / a0;\n";
		}

		if(paramValues.getString("secondInitState").equals("on")) {
			initCode+="double initialAC="+ACInitialInputs[0]+"*sin(2*3.14159265358979323846*"+ACInitialInputs[2]+"/360.0*T+"+ACInitialInputs[1]+"*2*3.14159265358979323846/360);\n"
					 +"Block"+getBlockId()+"SecondOrderFilter_y[0]=initialAC+"+DCInitialInput.getName()+";\n"
					 +"Block"+getBlockId()+"SecondOrderFilter_y[1]=initialAC+"+DCInitialInput.getName()+";\n";
		}

		code.addInitCode(initCode);
	}





	public void generateOutputCodeC(CodeStructC code) {
		String outputCode="/*Code for output of block Second-Order Filter:("+getBlockId()+")"+getBlockName()+"*/\n";
		outputCode+="if(mp->majorStep>0) {\n";

		outputCode+="{real_T currentTime = model.time;\n";
		outputCode+="real_T sampleTimeTmp = "+sampleTime.getName()+"==-1?model.stepSize:"+sampleTime.getName()+";\n";
		outputCode+="sampleTimeTmp = sampleTimeTmp ==0?model.stepSize:sampleTimeTmp;\n";
		outputCode+="if(fabs(floor(currentTime/sampleTimeTmp+0.5)-currentTime/sampleTimeTmp)<0.000001) {\n";

		String inputData = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName();
		String outputFilter = outputPortList.get(0).getOutputSignalC().getName();

		outputCode+=outputFilter+"=Block"+getBlockId()+"SecondOrderFilter_b[0]*"+inputData
				  +"+Block"+getBlockId()+"SecondOrderFilter_b[1]*Block"+getBlockId()+"SecondOrderFilter_x[0]"
				  +"+Block"+getBlockId()+"SecondOrderFilter_b[2]*Block"+getBlockId()+"SecondOrderFilter_x[1]"
				  +"-Block"+getBlockId()+"SecondOrderFilter_a[0]*Block"+getBlockId()+"SecondOrderFilter_y[0]"
				  +"-Block"+getBlockId()+"SecondOrderFilter_a[1]*Block"+getBlockId()+"SecondOrderFilter_y[1];\n";


		outputCode+="Block"+getBlockId()+"SecondOrderFilter_x[1]=Block"+getBlockId()+"SecondOrderFilter_x[0];\n"
				  +"Block"+getBlockId()+"SecondOrderFilter_x[0]="+inputData+";\n"
				  +"Block"+getBlockId()+"SecondOrderFilter_y[1]=Block"+getBlockId()+"SecondOrderFilter_y[0];\n"
				  +"Block"+getBlockId()+"SecondOrderFilter_y[0]="+outputFilter+";\n";


		outputCode+="}\n";
		outputCode+="}\n";
		outputCode+="}\n";
		code.addOutputCode(outputCode);
	}

//	public void generateDerivativeCodeC(CodeStructC code) {
//		String derivativeCode="/*Code for Derivative of block Second-Order Filter:("+getBlockId()+")"+getBlockName()+"*/\n";
//		String inputData = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName();
//		String outputFilter = outputPortList.get(0).getOutputSignalC().getName();
//
//		derivativeCode+="Block"+getBlockId()+"SecondOrderFilter_x[1]=Block"+getBlockId()+"SecondOrderFilter_x[0];\n"
//				  +"Block"+getBlockId()+"SecondOrderFilter_x[0]="+inputData+";\n"
//				  +"Block"+getBlockId()+"SecondOrderFilter_y[1]=Block"+getBlockId()+"SecondOrderFilter_y[0];\n"
//				  +"Block"+getBlockId()+"SecondOrderFilter_y[0]="+outputFilter+";\n";
//
//		code.addDerivativeCode(derivativeCode);
//	}

	public void updateDimension() throws MatDimException{
		OutputPort out  = outputPortList.get(0);
		InputPort in  = inputPortList.get(0);
		OutputSignal signal=in.getLinkedLine().getLinkedOutputPort().getOutputSignalC();
		if((Double.parseDouble(paramValues.getString("sampleTime").trim())*1000000)%(model.getConfig().getFixedStep()*1000000)>0.000001) {
			  MatDimException e=new MatDimException("Parameter(sampleTime) of Block "+this.blockName+" must be an integer multiple of the fixed-step size!\n \n");
			throw(e);
		}
	    if(sampleTime.getDataType()!=DataType.REAL) {
			MatDimException e=new MatDimException("Parameter(sampleTime) of Block "+this.blockName+" must be a real double scalar(period)!\n \n");
			throw(e);
	    }
	    out.setHeight(signal.getHeight());
		out.setWidth(signal.getWidth());
		out.getOutputSignalC().setHeight(signal.getHeight());
		out.getOutputSignalC().setWidth(signal.getWidth());
		out.getOutputSignalC().setDataType(signal.getDataType());
    }


	private void parseVector() {
		String initialInputsStr=paramValues.getString("secondACInput");

		String regEx = "[' ']+";
		Pattern p = Pattern.compile(regEx);
		Matcher m = p.matcher(initialInputsStr);
		JSONArray initialInputsArray=new JSONArray(m.replaceAll(",").trim());

		ACInitialInputs=new double[initialInputsArray.length()];
		for(int i=0;i<initialInputsArray.length();i++) {
			ACInitialInputs[i]=initialInputsArray.getDouble(i);
		}

	}
}
