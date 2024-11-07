package com.ncslab.block.continuous;

import lombok.Getter;
import org.json.JSONObject;

import Jama.Matrix;

import com.ncslab.block.Block;
import com.ncslab.block.data.DataType;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.block.io.OutputSignal;

import java.util.Vector;

public class TransportDelay extends Block {

	private Parameter initialoutput;
	private Parameter delaytime;
	OutputPort output;
	InputPort input;
	Matrix test;
	private double number[] = null;


    @Getter
    public static final Vector<String> parameterNames = new Vector<>();

    @Getter
    public static final Vector<String> outputNames = new Vector<>();
    @Getter
    public static final Vector<String> inputNames = new Vector<>();

    static {
        parameterNames.add("InitialOutput");
        parameterNames.add("DelayTime");

        outputNames.add("out1");
        inputNames.add("in1");
    }

	public TransportDelay(JSONObject blockIn, NCSLabModel model) {
		super(blockIn, model);
		test = parseMatrix(paramValues.getString("DelayTime"));
		initialoutput = new Parameter(this, 1, "InitialOutput", paramValues.getString("InitialOutput"));
		parameterList.add(initialoutput);

		delaytime = new Parameter(this, 2, "DelayTime", paramValues.getString("DelayTime"));
		parameterList.add(delaytime);

		input = new InputPort(this, 1);
		inputPortList.add(input);
		output = new OutputPort(this, 1, true);
		outputPortList.add(output);
	}

	private Matrix parseMatrix(String matrixString) {
		matrixString = matrixString.replaceAll("\\[\\s*", "");
		matrixString = matrixString.replaceAll("\\s*\\]", "");

		String[] parentMat = matrixString.split("\\s*;\\s*");
		double[][] childMat = new double[parentMat.length][];
		for (int i = 0; i < parentMat.length; i++) {
			String[] child = parentMat[i].split("(\\s*\\,\\s*)|(\\s+)");
			childMat[i] = new double[child.length];
			for (int j = 0; j < child.length; j++) {
				String doubleString = child[j].replaceAll("\\s+", "");
				childMat[i][j] = Double.parseDouble(doubleString);
			}
		}
		return new Matrix(childMat);
	}

	public void generateInitCodeC(CodeStructC code) {
		super.generateInitCodeC(code);
		String initCode = "/*Code for initialization of block TransportDelay:(" + getBlockId() + ")" + getBlockName()
				+ "*/\n";
		initCode += delaytime.getInitCodeC();
		initCode += initialoutput.getInitCodeC();
		code.addInitCode(initCode);
	}

	public void generateArraysCodeC(CodeStructC code) {
		String arraysCode = "/*Define arrays for block transport_Delay:(" + getBlockId() + ")" + getBlockName()
				+ "*/\n";
		OutputSignal signal = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
		String ss=this.model.getConfig().getSolver();
		if(ss.equals("ode1")||ss.equals("ode2")||ss.equals("ode3")||ss.equals("ode4")||ss.equals("ode5")||ss.equals("ode6")) {
		switch (signal.getDataType()) {
		case REAL:
			switch (delaytime.getDataType()) {
			case REAL:
				arraysCode += "double " + "Block" + getBlockId() + "_transport_delay_savedata[(int)("
						+ paramValues.getDouble("DelayTime") + "/STEP_SIZE)+1];\n";
				break;
			case MATRIX:
				for (int i = 0; i < delaytime.getHeight(); i++) {
					for (int j = 0; j < delaytime.getWidth(); j++) {
						arraysCode += "double " + "Block" + getBlockId() + "_transport_delay_savedata_" + i + "_" + j+ "_[(int)(" + test.get(i, j) + "/STEP_SIZE)+1];\n";
					}
				}
				break;
			}
			break;
		case MATRIX:
			switch (delaytime.getDataType()) {
			case REAL:
				for (int i = 0; i < signal.getHeight(); i++) {
					for (int j = 0; j < signal.getWidth(); j++) {
						arraysCode += "double " + "Block" + getBlockId() + "_transport_delay_savedata_" + i + "_" + j+ "_[(int)(" + paramValues.getDouble("DelayTime") + "/STEP_SIZE)+1];\n";
					}
				}
				break;
			case MATRIX:
				for (int i = 0; i < signal.getHeight(); i++) {
					for (int j = 0; j < signal.getWidth(); j++) {
						arraysCode += "double " + "Block" + getBlockId() + "_transport_delay_savedata_" + i + "_" + j+ "_[(int)(" + test.get(i, j) + "/STEP_SIZE)+1];\n";
					}
				}
				break;
			}
			break;
		}
	  }else {
		  if(signal.getDataType()==DataType.REAL&&delaytime.getDataType()==DataType.MATRIX) {
			  arraysCode += "double " + "Block" + getBlockId() + "tout["+delaytime.getHeight()+"*"+delaytime.getWidth()+"][2000];\n";
			  arraysCode += "double " + "Block" + getBlockId() + "yout["+delaytime.getHeight()+"*"+delaytime.getWidth()+"][2000];\n";
		  }else {
		  arraysCode += "double " + "Block" + getBlockId() + "tout["+signal.getHeight()+"*"+signal.getWidth()+"][2000];\n";
		  arraysCode += "double " + "Block" + getBlockId() + "yout["+signal.getHeight()+"*"+signal.getWidth()+"][2000];\n";
		  }
	  }
		code.addArraysCode(arraysCode);
	}

	public void generateOutputCodeC(CodeStructC code) {
		String outputCode = "/*Code for output of block TransportDelay:(" + getBlockId() + ")" + getBlockName()
				+ "*/\n";

		OutputSignal signal = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
		String ss=this.model.getConfig().getSolver();
		if(ss.equals("ode1")||ss.equals("ode2")||ss.equals("ode3")||ss.equals("ode4")||ss.equals("ode5")||ss.equals("ode6"))  {
		switch (signal.getDataType()) {
		case REAL:
			switch (delaytime.getDataType()) {
			case REAL:
				outputCode+="if(mp->majorStep>0) {\n";
				 for(int i=0;i<(int)(paramValues.getDouble("DelayTime")/model.getConfig().getFixedStep());i++){
					  int k=i+1;
				  outputCode+="Block"+getBlockId()+"_transport_delay_savedata["+i+"]="+"Block"+getBlockId()+"_transport_delay_savedata["+k+"];\n";}
				  outputCode+="Block"+getBlockId()+"_transport_delay_savedata[(int)("+paramValues.getDouble("DelayTime")+"/STEP_SIZE)]="+signal.getName()+";\n";
				  outputCode+= "if(model.time<" + paramValues.getDouble("DelayTime") + ") {\n";
					outputCode += outputPortList.get(0).getOutputSignalC().getName() + "=" + initialoutput.getName()
							+ ";}\n";
					outputCode+= "else{\n";
					outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"=Block"+getBlockId()+"_transport_delay_savedata[0];}\n";
					outputCode+="}\n";
				break;
			case MATRIX:
				outputCode+="if(mp->majorStep>0) {\n";
				for (int i = 0; i < delaytime.getHeight(); i++) {
					for (int j = 0; j < delaytime.getWidth(); j++) {
						 for(int m=0;m<(int)(test.get(i,j)/model.getConfig().getFixedStep());m++){
						  outputCode+="Block"+getBlockId()+"_transport_delay_savedata_" + i + "_" + j+ "_["+m+"]="+"Block"+getBlockId()+"_transport_delay_savedata_" + i + "_" + j+ "_["+m+"+1];\n";}
						  outputCode+="Block"+getBlockId()+"_transport_delay_savedata_" + i + "_" + j+ "_[(int)("+test.get(i,j)+"/STEP_SIZE)]="+signal.getName()+";\n";
						  outputCode+= "if(model.time<"+delaytime.getName()+"("+i+","+j+")) {\n";
							outputCode += outputPortList.get(0).getOutputSignalC().getName()+"("+i+","+j+")=" + initialoutput.getName()+"("+i+","+j+");}\n";
							outputCode+= "else{\n";
							outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"("+i+","+j+")=Block"+getBlockId()+"_transport_delay_savedata_" + i + "_" + j+ "_[0];}\n";
					}
				}
				outputCode+="}\n";
				break;
			}
			break;
		case MATRIX:
			switch (delaytime.getDataType()) {
			case REAL:
				outputCode+="if(mp->majorStep>0) {\n";
				for (int i = 0; i < signal.getHeight(); i++) {
					for (int j = 0; j < signal.getWidth(); j++) {
							 for(int m=0;m<(int)(paramValues.getDouble("DelayTime")/model.getConfig().getFixedStep());m++){
							  outputCode+="Block"+getBlockId()+"_transport_delay_savedata_" + i + "_" + j+ "_["+m+"]="+"Block"+getBlockId()+"_transport_delay_savedata_" + i + "_" + j+ "_["+m+"+1];\n";}
							  outputCode+="Block"+getBlockId()+"_transport_delay_savedata_" + i + "_" + j+ "_[(int)("+paramValues.getDouble("DelayTime")+"/STEP_SIZE)]="+signal.getName()+"("+i+","+j+");\n";
							  outputCode+= "if(model.time<" + paramValues.getDouble("DelayTime") + ") {\n";
								outputCode += outputPortList.get(0).getOutputSignalC().getName() + "("+i+","+j+")=" + initialoutput.getName()+ ";}\n";
								outputCode+= "else{\n";
								outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"("+i+","+j+")=Block"+getBlockId()+"_transport_delay_savedata_" + i + "_" + j+ "_[0];}\n";
					   }
				   }
				outputCode+="}\n";
				break;
			case MATRIX:
				outputCode+="if(mp->majorStep>0) {\n";
				for (int i = 0; i < delaytime.getHeight(); i++) {
					for (int j = 0; j < delaytime.getWidth(); j++) {
						 for(int m=0;m<(int)(test.get(i,j)/model.getConfig().getFixedStep());m++){
						  outputCode+="Block"+getBlockId()+"_transport_delay_savedata_" + i + "_" + j+ "_["+m+"]="+"Block"+getBlockId()+"_transport_delay_savedata_" + i + "_" + j+ "_["+m+"+1];\n";}
						  outputCode+="Block"+getBlockId()+"_transport_delay_savedata_" + i + "_" + j+ "_[(int)("+test.get(i,j)+"/STEP_SIZE)]="+signal.getName()+"("+i+","+j+");\n";
						  outputCode+= "if(model.time<"+delaytime.getName()+"("+i+","+j+")) {\n";
							outputCode += outputPortList.get(0).getOutputSignalC().getName()+"("+i+","+j+")=" + initialoutput.getName()+"("+i+","+j+");}\n";
							outputCode+= "else{\n";
							outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"("+i+","+j+")=Block"+getBlockId()+"_transport_delay_savedata_" + i + "_" + j+ "_[0];}\n";
					}
				}
				outputCode+="}\n";
				break;
			}
			break;
		}
	  }else {
		  switch (signal.getDataType()) {
			case REAL:
				switch (delaytime.getDataType()) {
				case REAL:
					outputCode+="if(mp->majorStep>0){\n";
					outputCode+="int vi=0;\n";
					outputCode+="for(int i=1998;i>=0;i--){\n";
					outputCode+="Block" + getBlockId() + "yout[0][i+1]=Block" + getBlockId() + "yout[0][i];\n";
					outputCode+="Block" + getBlockId() + "tout[0][i+1]=Block" + getBlockId() + "tout[0][i];\n";
					outputCode+="Block" + getBlockId() + "yout[0][0]="+signal.getName()+";\n";
					outputCode+="Block" + getBlockId() + "tout[0][0]=model.time;\n";
					outputCode+="}\n";
					outputCode+="if((model.time-"+delaytime.getName()+")<=0){\n";
					outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"="+initialoutput.getName()+";}\n";
					outputCode+="else{\n";
					outputCode+="for(int i=0;i<2000;i++){\n";
					outputCode+="if((model.time-"+delaytime.getName()+")>=Block"+ getBlockId() + "tout[0][i]){";
					outputCode+="vi=i;\n";
					outputCode+="break;}}\n";
					outputCode+="if(vi==0){\n";
					outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"="+initialoutput.getName()+";}\n";
					outputCode+="else{\n";
					outputCode+="double f1=(Block"+ getBlockId() + "tout[0][vi-1]-(model.time-"+delaytime.getName()+"))/(Block"+ getBlockId() + "tout[0][vi-1]-Block"+ getBlockId() + "tout[0][vi]);\n";
					outputCode+="double f2=1.0-f1;\n";
					outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"=f1*Block"+getBlockId() + "yout[0][vi]+f2*Block"+getBlockId() + "yout[0][vi-1];}}}\n";
					break;
				case MATRIX:
					outputCode+="int v["+delaytime.getHeight()+"*"+delaytime.getWidth()+"];\n";
					outputCode+="double f1["+delaytime.getHeight()+"*"+delaytime.getWidth()+"];\n";
					outputCode+="double f2["+delaytime.getHeight()+"*"+delaytime.getWidth()+"];\n";
					for (int i = 0; i < delaytime.getHeight(); i++) {
						for (int j = 0; j < delaytime.getWidth(); j++) {
							outputCode+="if(mp->majorStep>0){\n";
							outputCode+="for(int m=1998;m>=0;m--){\n";
							outputCode+="Block" + getBlockId() + "yout[("+i+"+1)*"+j+"][m+1]=Block" + getBlockId() + "yout[("+i+"+1)*"+j+"][m];\n";
							outputCode+="Block" + getBlockId() + "tout[("+i+"+1)*"+j+"][m+1]=Block" + getBlockId() + "tout[("+i+"+1)*"+j+"][m];\n";
							outputCode+="Block" + getBlockId() + "yout[("+i+"+1)*"+j+"][0]="+signal.getName()+";\n";
							outputCode+="Block" + getBlockId() + "tout[("+i+"+1)*"+j+"][0]=model.time;\n";
							outputCode+="}\n";
							outputCode+="if((model.time-"+delaytime.getName()+"("+i+","+j+"))<=0){\n";
							outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"("+i+","+j+")="+initialoutput.getName()+"("+i+","+j+");}\n";
							outputCode+="else{\n";
							outputCode+="for(int n=0;n<2000;n++){\n";
							outputCode+="if((model.time-"+delaytime.getName()+"("+i+","+j+"))>=Block"+ getBlockId() + "tout[("+i+"+1)*"+j+"][n]){\n";
							outputCode+="v[("+i+"+1)*"+j+"]=n;\n";
							outputCode+="break;}}\n";
							outputCode+="if(v[("+i+"+1)*"+j+"]==0){\n";
							outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"("+i+","+j+")="+initialoutput.getName()+"("+i+","+j+");}\n";
							outputCode+="else{\n";
							outputCode+="f1[("+i+"+1)*"+j+"]=(Block"+ getBlockId() + "tout[("+i+"+1)*"+j+"][v[("+i+"+1)*"+j+"]-1]-(model.time-"+delaytime.getName()+"("+i+","+j+")))/(Block"+ getBlockId() + "tout[("+i+"+1)*"+j+"][v[("+i+"+1)*"+j+"]-1]-Block"+ getBlockId() + "tout[("+i+"+1)*"+j+"][v[("+i+"+1)*"+j+"]]);\n";
							outputCode+="f2[("+i+"+1)*"+j+"]=1.0-f1[("+i+"+1)*"+j+"];\n";
							outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"("+i+","+j+")=f1[("+i+"+1)*"+j+"]*Block"+getBlockId() + "yout[("+i+"+1)*"+j+"][v[("+i+"+1)*"+j+"]]+f2[("+i+"+1)*"+j+"]*Block"+getBlockId() + "yout[("+i+"+1)*"+j+"][v[("+i+"+1)*"+j+"]-1];}}}\n";
						}
					}
					break;
				}
				break;
			case  MATRIX:
				switch (delaytime.getDataType()) {
				case REAL:
					outputCode+="int v["+signal.getHeight()+"*"+signal.getWidth()+"];\n";
					outputCode+="double f1["+signal.getHeight()+"*"+signal.getWidth()+"];\n";
					outputCode+="double f2["+signal.getHeight()+"*"+signal.getWidth()+"];\n";
					for (int i = 0; i < signal.getHeight(); i++) {
						for (int j = 0; j < signal.getWidth(); j++) {
							outputCode+="if(mp->majorStep>0){\n";
							outputCode+="for(int m=1998;m>=0;m--){\n";
							outputCode+="Block" + getBlockId() + "yout[("+i+"+1)*"+j+"][m+1]=Block" + getBlockId() + "yout[("+i+"+1)*"+j+"][m];\n";
							outputCode+="Block" + getBlockId() + "tout[("+i+"+1)*"+j+"][m+1]=Block" + getBlockId() + "tout[("+i+"+1)*"+j+"][m];\n";
							outputCode+="Block" + getBlockId() + "yout[("+i+"+1)*"+j+"][0]="+signal.getName()+"("+i+","+j+");\n";
							outputCode+="Block" + getBlockId() + "tout[("+i+"+1)*"+j+"][0]=model.time;\n";
							outputCode+="}\n";
							outputCode+="if((model.time-"+delaytime.getName()+")<=0){\n";
							outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"("+i+","+j+")="+initialoutput.getName()+";}\n";
							outputCode+="else{\n";
							outputCode+="for(int n=0;n<2000;n++){\n";
							outputCode+="if((model.time-"+delaytime.getName()+")>=Block"+ getBlockId() + "tout[("+i+"+1)*"+j+"][n]){\n";
							outputCode+="v[("+i+"+1)*"+j+"]=n;\n";
							outputCode+="break;}}\n";
							outputCode+="if(v[("+i+"+1)*"+j+"]==0){\n";
							outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"("+i+","+j+")="+initialoutput.getName()+";}\n";
							outputCode+="else{\n";
							outputCode+="f1[("+i+"+1)*"+j+"]=(Block"+ getBlockId() + "tout[("+i+"+1)*"+j+"][v[("+i+"+1)*"+j+"]-1]-(model.time-"+delaytime.getName()+"))/(Block"+ getBlockId() + "tout[("+i+"+1)*"+j+"][v[("+i+"+1)*"+j+"]-1]-Block"+ getBlockId() + "tout[("+i+"+1)*"+j+"][v[("+i+"+1)*"+j+"]]);\n";
							outputCode+="f2[("+i+"+1)*"+j+"]=1.0-f1[("+i+"+1)*"+j+"];\n";
							outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"("+i+","+j+")=f1[("+i+"+1)*"+j+"]*Block"+getBlockId() + "yout[("+i+"+1)*"+j+"][v[("+i+"+1)*"+j+"]]+f2[("+i+"+1)*"+j+"]*Block"+getBlockId() + "yout[("+i+"+1)*"+j+"][v[("+i+"+1)*"+j+"]-1];}}}\n";
						}
					}
					break;
				case MATRIX:
					outputCode+="int v["+signal.getHeight()+"*"+signal.getWidth()+"];\n";
					outputCode+="double f1["+signal.getHeight()+"*"+signal.getWidth()+"];\n";
					outputCode+="double f2["+signal.getHeight()+"*"+signal.getWidth()+"];\n";
					for (int i = 0; i < signal.getHeight(); i++) {
						for (int j = 0; j < signal.getWidth(); j++) {
							outputCode+="if(mp->majorStep>0){\n";
							outputCode+="for(int m=1998;m>=0;m--){\n";
							outputCode+="Block" + getBlockId() + "yout[("+i+"+1)*"+j+"][m+1]=Block" + getBlockId() + "yout[("+i+"+1)*"+j+"][m];\n";
							outputCode+="Block" + getBlockId() + "tout[("+i+"+1)*"+j+"][m+1]=Block" + getBlockId() + "tout[("+i+"+1)*"+j+"][m];\n";
							outputCode+="Block" + getBlockId() + "yout[("+i+"+1)*"+j+"][0]="+signal.getName()+"("+i+","+j+");\n";
							outputCode+="Block" + getBlockId() + "tout[("+i+"+1)*"+j+"][0]=model.time;\n";
							outputCode+="}\n";
							outputCode+="if((model.time-"+delaytime.getName()+"("+i+","+j+"))<=0){\n";
							outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"("+i+","+j+")="+initialoutput.getName()+"("+i+","+j+");}\n";
							outputCode+="else{\n";
							outputCode+="for(int n=0;n<2000;n++){\n";
							outputCode+="if((model.time-"+delaytime.getName()+"("+i+","+j+"))>=Block"+ getBlockId() + "tout[("+i+"+1)*"+j+"][n]){\n";
							outputCode+="v[("+i+"+1)*"+j+"]=n;\n";
							outputCode+="break;}}\n";
							outputCode+="if(v[("+i+"+1)*"+j+"]==0){\n";
							outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"("+i+","+j+")="+initialoutput.getName()+"("+i+","+j+");}\n";
							outputCode+="else{\n";
							outputCode+="f1[("+i+"+1)*"+j+"]=(Block"+ getBlockId() + "tout[("+i+"+1)*"+j+"][v[("+i+"+1)*"+j+"]-1]-(model.time-"+delaytime.getName()+"("+i+","+j+")))/(Block"+ getBlockId() + "tout[("+i+"+1)*"+j+"][v[("+i+"+1)*"+j+"]-1]-Block"+ getBlockId() + "tout[("+i+"+1)*"+j+"][v[("+i+"+1)*"+j+"]]);\n";
							outputCode+="f2[("+i+"+1)*"+j+"]=1.0-f1[("+i+"+1)*"+j+"];\n";
							outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"("+i+","+j+")=f1[("+i+"+1)*"+j+"]*Block"+getBlockId() + "yout[("+i+"+1)*"+j+"][v[("+i+"+1)*"+j+"]]+f2[("+i+"+1)*"+j+"]*Block"+getBlockId() + "yout[("+i+"+1)*"+j+"][v[("+i+"+1)*"+j+"]-1];}}}\n";
						}
					}
					break;
				}
				break;
		  }
	   }
		code.addOutputCode(outputCode);
	}

	public void updateDimension() throws MatDimException {

		OutputPort out = outputPortList.get(0);
		InputPort in = inputPortList.get(0);
		OutputSignal signal = in.getLinkedLine().getLinkedOutputPort().getOutputSignalC();
		switch (signal.getDataType()) {
		case REAL:
			switch (delaytime.getDataType()) {
			case REAL:
				out.setHeight(1);
				out.setWidth(1);
				out.getOutputSignalC().setHeight(1);
				out.getOutputSignalC().setWidth(1);
				out.getOutputSignalC().setDataType(DataType.REAL);
				break;
			case MATRIX:
				out.setHeight(delaytime.getHeight());
				out.setWidth(delaytime.getWidth());
				out.getOutputSignalC().setHeight(delaytime.getHeight());
				out.getOutputSignalC().setWidth(delaytime.getWidth());
				out.getOutputSignalC().setDataType(DataType.MATRIX);
				break;
			}
			break;
		case MATRIX:
			switch (delaytime.getDataType()) {
			case REAL:
				out.setHeight(signal.getHeight());
				out.setWidth(signal.getWidth());
				out.getOutputSignalC().setHeight(signal.getHeight());
				out.getOutputSignalC().setWidth(signal.getWidth());
				out.getOutputSignalC().setDataType(DataType.MATRIX);
				break;
			case MATRIX:
				out.setHeight(signal.getHeight());
				out.setWidth(signal.getWidth());
				out.getOutputSignalC().setHeight(signal.getHeight());
				out.getOutputSignalC().setWidth(signal.getWidth());
				out.getOutputSignalC().setDataType(DataType.MATRIX);
				break;
			}
			break;
		}
	}

	public void checkDimension() throws MatDimException {
		OutputSignal signal = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
		switch (initialoutput.getDataType()) {
		case REAL:
			switch (signal.getDataType()) {
			case REAL:

				break;
			case MATRIX:
				if (initialoutput.getHeight() != delaytime.getHeight()
						|| initialoutput.getWidth() != delaytime.getWidth()) {
					MatDimException e = new MatDimException(
							"Block " + this.blockName + " input dimensions don't match!");
					throw (e);
				}
				break;
			}
			break;
		case MATRIX:
			switch (signal.getDataType()) {
			case REAL:
				if (initialoutput.getHeight() != delaytime.getHeight()
						|| initialoutput.getWidth() != delaytime.getWidth()) {
					MatDimException e = new MatDimException(
							"Block " + this.blockName + " input dimensions don't match1!");
					throw (e);
				}
				break;
			case MATRIX:
				if (signal.getHeight() != delaytime.getHeight() || signal.getWidth() != delaytime.getWidth()
						|| signal.getHeight() != initialoutput.getHeight()
						|| signal.getWidth() != initialoutput.getWidth()
						|| initialoutput.getHeight() != delaytime.getHeight()
						|| initialoutput.getWidth() != delaytime.getWidth()) {
					MatDimException e = new MatDimException(
							"Block " + this.blockName + " input dimensions don't match!");
					throw (e);
				}
				break;
			}
			break;
		}

	}
}
