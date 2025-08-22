package com.ncslab.block.powerSystem;

import com.ncslab.block.Block;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.Getter;
import org.json.JSONArray;
import org.json.JSONObject;
import com.ncslab.dto.core.BlockDto;
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


    
    
    /**
     * DTO-NATIVE Constructor - Creates secondOrderFiliter block directly from BlockDto DTO
     */
    public secondOrderFiliter(BlockDto blockDto, NCSLabModel model) {
        super(blockDto, model);
        System.out.println("DTO-NATIVE: secondOrderFiliter block created successfully - " + blockDto.getBlockName());
    }


    public static final List<String> outputNames = new ArrayList<>();
    public static final List<String> inputNames = new ArrayList<>();

    public static final Map<String, String> PARAMETER_DEFAULTS = new HashMap<>();

    static {

        outputNames.add("out1");
        inputNames.add("in1");

        // Parameter defaults
        PARAMETER_DEFAULTS.put("filterType", "Lowpass");
        PARAMETER_DEFAULTS.put("naturalFrequency", "1.0");
        PARAMETER_DEFAULTS.put("dampingRatio", "0.707");
        PARAMETER_DEFAULTS.put("sampleTime", "-1");
        PARAMETER_DEFAULTS.put("initState", "off");
        PARAMETER_DEFAULTS.put("DCInitialInput", "0");
    }

	public secondOrderFiliter(JSONObject blockIn, NCSLabModel model) {
		super(blockIn, model);

		if(paramValues.getString("secondInitState").equals("on")) parseVector();

		//1个输入，1个输出
		inputPortList.add(new InputPort(this,1));
		outputPortList.add(new OutputPort(this,1,true));

		filterType=new Parameter(this,parameterList.size()+1,"filterType",paramValues.getString("secondFilterType"));
		naturalFrequency=new Parameter(this,parameterList.size()+1,"naturalFrequency",paramValues.getString("secondFrequency"));
		dampingRatio=new Parameter(this,parameterList.size()+1,"dampingRatio",paramValues.getString("secondDampingRatio"));
		sampleTime=new Parameter(this,parameterList.size()+1,"sampleTime",paramValues.getString("sampleTime"));
		initState=new Parameter(this,parameterList.size()+1,"initState",paramValues.getString("secondInitState"));
		DCInitialInput=new Parameter(this,parameterList.size()+1,"DCInitialInput",paramValues.getString("secondDCInput"));

	}
	public void generateArraysCodeC(CodeStructC code) {
		super.generateArraysCodeC(code);
		
		context.put("block", this);
		
		String codeStr = com.ncslab.util.TemplateManager.renderTemplate("c/powerSystem/secondOrderFiliter/arrays.vm", context);
		code.addArraysCode(codeStr);
	 }
	public void generateInitCodeC(CodeStructC code) {
		super.generateInitCodeC(code);
		
		context.put("block", this);
		context.put("naturalFrequency", naturalFrequency);
		context.put("dampingRatio", dampingRatio);
		context.put("sampleTime", sampleTime);
		context.put("DCInitialInput", DCInitialInput);
		context.put("initState", paramValues.getString("secondInitState"));
		context.put("sampleTimeValue", paramValues.getString("sampleTime"));
		context.put("filterTypeValue", paramValues.getString("secondFilterType"));
		if(ACInitialInputs != null) {
			context.put("ACInitialInputs", ACInitialInputs);
		}
		
		String codeStr = com.ncslab.util.TemplateManager.renderTemplate("c/powerSystem/secondOrderFiliter/init.vm", context);
		code.addInitCode(codeStr);
	}

	public void generateOutputCodeC(CodeStructC code) {
		super.generateOutputCodeC(code);
		
		String inputData = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName();
		String outputFilter = outputPortList.get(0).getOutputSignalC().getName();
		
		context.put("block", this);
		context.put("sampleTime", sampleTime);
		context.put("inputData", inputData);
		context.put("outputFilter", outputFilter);
		
		String codeStr = com.ncslab.util.TemplateManager.renderTemplate("c/powerSystem/secondOrderFiliter/output.vm", context);
		code.addOutputCode(codeStr);
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
