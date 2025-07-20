package com.ncslab.block.continuous;

import lombok.Getter;
import org.json.JSONObject;

import com.ncslab.block.data.DataType;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.block.io.Parameter;
import com.ncslab.block.io.State;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.util.TemplateManager;
import org.apache.velocity.VelocityContext;

import java.util.Vector;
import java.util.Map;
import java.util.HashMap;

public class PIDControllerback extends com.ncslab.block.Block{

	Parameter cparaP;
	Parameter cparaI;
	Parameter cparaD;
	Parameter cparaN;
	Parameter lowerSaturationLimit=null;
	Parameter upperSaturationLimit=null;
	State stateIntegral;
	State stateFilter;

	Parameter externalReset;//zhou_20240507 add externalReset
	Parameter sampleTime;

    public static final Vector<String> outputNames = new Vector<>();
    public static final Vector<String> inputNames = new Vector<>();

    public static final Map<String, String> PARAMETER_DEFAULTS = new HashMap<>();

    static {
        outputNames.add("out1");
        inputNames.add("in1");

        PARAMETER_DEFAULTS.put("P", "1");
        PARAMETER_DEFAULTS.put("I", "1");
        PARAMETER_DEFAULTS.put("D", "0");
        PARAMETER_DEFAULTS.put("N", "100");
        PARAMETER_DEFAULTS.put("externalReset", "off");
        PARAMETER_DEFAULTS.put("simpleTime", "-1");
        PARAMETER_DEFAULTS.put("LimitOutput", "off");
        PARAMETER_DEFAULTS.put("LowerSaturationLimit", "-inf");
        PARAMETER_DEFAULTS.put("UpperSaturationLimit", "inf");
    }
	public PIDControllerback(JSONObject blockIn,NCSLabModel model) {
		super(blockIn,model);

		//一个输入，一个输出
		inputPortList.add(new InputPort(this,1));
		outputPortList.add(new OutputPort(this,1,true));
		cparaP=new Parameter(this,parameterList.size()+1,"P",paramValues.getString("P"));
		cparaI=new Parameter(this,parameterList.size()+1,"I",paramValues.getString("I"));
		cparaD=new Parameter(this,parameterList.size()+1,"D",paramValues.getString("D"));
		cparaN=new Parameter(this,parameterList.size()+1,"N",paramValues.getString("N"));

		//zhou_20240507 add externalReset
		externalReset=new Parameter(this,parameterList.size()+1,"externalReset",paramValues.getString("externalReset"));
		if(paramValues.getString("externalReset").equals("on")) {
			inputPortList.add(new InputPort(this,2));
		}
		sampleTime=new Parameter(this,parameterList.size()+1,"sampleTime",paramValues.getString("simpleTime"));
		/*stateIntegral=new State(this,1,"integral");
		stateList.add(stateIntegral);
		stateFilter=new State(this,2,"filter");
		stateList.add(stateFilter);*/
		if(paramValues.getString("LimitOutput").equals("on")) {
			lowerSaturationLimit=new Parameter(this,parameterList.size()+1,"LowerSaturationLimit",paramValues.getString("LowerSaturationLimit"));
			upperSaturationLimit=new Parameter(this,parameterList.size()+1,"UpperSaturationLimit",paramValues.getString("UpperSaturationLimit"));
		}
	}
	 //define arrays to save data
	 public void generateArraysCodeC(CodeStructC code) {

		 context.put("blockId", getBlockId());
		 context.put("blockName", getBlockName());
		 context.put("cparaP", cparaP);
		 context.put("realDataType", DataType.REAL);
		 context.put("matrixDataType", DataType.MATRIX);

		 OutputSignal signal = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
		 context.put("inputSignal", signal);

		 String arraysCode = TemplateManager.renderTemplate("c/continuous/PIDControllerback/arrays.vm", context);
		 code.addArraysCode(arraysCode);
	 }
	public void generateInitCodeC(CodeStructC code) {
		super.generateInitCodeC(code);


		context.put("cparaP", cparaP);
		context.put("cparaI", cparaI);
		context.put("cparaD", cparaD);
		context.put("cparaN", cparaN);
		context.put("limitOutput", paramValues.getString("LimitOutput"));
		context.put("realDataType", DataType.REAL);
		context.put("matrixDataType", DataType.MATRIX);

		if(paramValues.getString("LimitOutput").equals("on")) {
			context.put("lowerSaturationLimit", lowerSaturationLimit);
			context.put("upperSaturationLimit", upperSaturationLimit);
		}

		context.put("stateIntegral", stateIntegral);
		context.put("stateFilter", stateFilter);
		context.put("sampleTime", sampleTime);

		String initCode = TemplateManager.renderTemplate("c/continuous/PIDControllerback/init.vm", context);
		code.addInitCode(initCode);
	}

	public void generateOutputCodeC(CodeStructC code) {

		context.put("realDataType", DataType.REAL);
		context.put("matrixDataType", DataType.MATRIX);
		context.put("blockId", getBlockId());
		context.put("blockName", getBlockName());
		context.put("cparaP", cparaP);
		context.put("cparaI", cparaI);
		context.put("cparaD", cparaD);
		context.put("cparaN", cparaN);
		context.put("stateIntegral", stateIntegral);
		context.put("stateFilter", stateFilter);
		context.put("sampleTime", sampleTime);
		context.put("externalReset", paramValues.getString("externalReset"));
		context.put("limitOutput", paramValues.getString("LimitOutput"));
		context.put("realDataType", DataType.REAL);
		context.put("matrixDataType", DataType.MATRIX);

		OutputSignal signal = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
		context.put("inputSignal", signal);

		if(paramValues.getString("externalReset").equals("on")) {
			context.put("externalResetSignal", inputPortList.get(1).getLinkedLine().getLinkedOutputPort().getOutputSignalC());
		}

		if(paramValues.getString("LimitOutput").equals("on")) {
			context.put("upperSaturationLimit", upperSaturationLimit);
			context.put("lowerSaturationLimit", lowerSaturationLimit);
		}

		context.put("outputPortVariable", this.getOutputPortVariable(0));

		String outputCode = TemplateManager.renderTemplate("c/continuous/PIDControllerback/output.vm", context);
		code.addOutputCode(outputCode);
	}

	public void  generateDerivativeCodeC(CodeStructC code) {

		context.put("realDataType", DataType.REAL);
		context.put("matrixDataType", DataType.MATRIX);
		context.put("blockId", getBlockId());
		context.put("blockName", getBlockName());
		context.put("sampleTime", sampleTime);
		context.put("stateIntegral", stateIntegral);
		context.put("stateFilter", stateFilter);
		context.put("cparaP", cparaP);
		context.put("realDataType", DataType.REAL);
		context.put("matrixDataType", DataType.MATRIX);

		OutputSignal signal = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
		context.put("inputSignal", signal);

		String derivativeCode = TemplateManager.renderTemplate("c/continuous/PIDControllerback/derivative.vm", context);
		code.addDerivativeCode(derivativeCode);
	}
	public void  generateUpdateCodeC(CodeStructC code) {

		context.put("realDataType", DataType.REAL);
		context.put("matrixDataType", DataType.MATRIX);
		context.put("blockId", getBlockId());
		context.put("blockName", getBlockName());
		context.put("sampleTime", sampleTime);
		context.put("stateIntegral", stateIntegral);
		context.put("stateFilter", stateFilter);
		context.put("cparaP", cparaP);
		context.put("realDataType", DataType.REAL);
		context.put("matrixDataType", DataType.MATRIX);

		OutputSignal signal = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
		context.put("inputSignal", signal);

		String updateCode = TemplateManager.renderTemplate("c/continuous/PIDControllerback/update.vm", context);
		code.addUpdateCode(updateCode);
	}
	   public void updateDimension() throws MatDimException{
		    OutputPort out  = outputPortList.get(0);
		    InputPort in  = inputPortList.get(0);
		    OutputSignal signal=in.getLinkedLine().getLinkedOutputPort().getOutputSignalC();

		    if((Double.parseDouble(paramValues.getString("simpleTime").trim())*1000000)%(model.getConfig().getFixedStep()*1000000)>0.000001) {
				  MatDimException e=new MatDimException("Parameter(sampleTime) of Block "+this.blockName+" must be an integer multiple of the fixed-step size!\n \n");
					throw(e);
			}
		    if(sampleTime.getDataType()!=DataType.REAL) {
					MatDimException e=new MatDimException("Parameter(sampleTime) of Block "+this.blockName+" must be a real double scalar(period)!\n \n");
					throw(e);
			}

		    if(signal.getDataType()==DataType.REAL) {
		    	stateIntegral=new State(this,1,"stateIntegral",cparaP.getHeight(),cparaP.getWidth());
		    	stateFilter=new State(this,2,"stateFilter",cparaP.getHeight(),cparaP.getWidth());
		    	}
				else {
				stateIntegral=new State(this,1,"stateIntegral",signal.getHeight(),signal.getWidth());
				stateFilter=new State(this,2,"stateFilter",signal.getHeight(),signal.getWidth());
				}
		        stateList.add(stateIntegral);
				stateList.add(stateFilter);
				if(cparaP.getWidth()!=cparaD.getWidth()||
				   cparaP.getWidth()!=cparaI.getWidth()||
				   cparaP.getWidth()!=cparaN.getWidth()||
				   cparaP.getHeight()!=cparaD.getHeight()||
				   cparaP.getHeight()!=cparaI.getHeight()||
				   cparaP.getHeight()!=cparaN.getHeight()) {
					MatDimException e=new MatDimException("Block "+this.blockName+" input dimension doesn't match!All input dimension must be same!\n \n");
					throw(e);
					}
				if(paramValues.getString("LimitOutput").equals("on")) {
					if(lowerSaturationLimit.getHeight()!=cparaP.getHeight()||
					 upperSaturationLimit.getHeight()!=cparaP.getHeight()||
					 lowerSaturationLimit.getWidth()!=cparaP.getWidth()||
					 upperSaturationLimit.getWidth()!=cparaP.getWidth()) {
					MatDimException e=new MatDimException("Block "+this.blockName+" input dimension doesn't match!All input dimension must be same!\n \n");
					throw(e);
					}
				}

		    if(cparaP.getDataType()==DataType.MATRIX&&signal.getDataType()==DataType.REAL) {
				out.setHeight(cparaP.getHeight());
				out.setWidth(cparaP.getWidth());
				out.getOutputSignalC().setHeight(cparaP.getHeight());
				out.getOutputSignalC().setWidth(cparaP.getWidth());
				out.getOutputSignalC().setDataType(DataType.MATRIX);
			}
			else if(cparaP.getDataType()==DataType.REAL&&signal.getDataType()==DataType.MATRIX) {
				out.setHeight(signal.getHeight());
				out.setWidth(signal.getWidth());
				out.getOutputSignalC().setHeight(signal.getHeight());
				out.getOutputSignalC().setWidth(signal.getWidth());
				out.getOutputSignalC().setDataType(signal.getDataType());
			}
			else{
				if(cparaP.getWidth()!=signal.getWidth()||cparaP.getHeight()!=signal.getHeight()) {
				MatDimException e=new MatDimException("Block "+this.blockName+" input dimension doesn't match the P dimension!\n \n");
				throw(e);
				}
				out.setHeight(cparaP.getHeight());
				out.setWidth(cparaP.getWidth());
				out.getOutputSignalC().setHeight(cparaP.getHeight());
				out.getOutputSignalC().setWidth(cparaP.getWidth());
				out.getOutputSignalC().setDataType(cparaP.getDataType());
			}
	     }
	   public void checkDimension() throws MatDimException{
	    }
}
