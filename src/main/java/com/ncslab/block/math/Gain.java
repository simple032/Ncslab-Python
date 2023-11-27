package com.ncslab.block.math;

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

public class Gain extends Block{
	protected Parameter gain;
	//Matrix gain;
	boolean multiplication = false;
	public Gain(JSONObject blockJSON,NCSLabModel model) {
		super(blockJSON,model);
		
		
		//this.gain = new Matrix(paramValues.getString("Gain"));
		this.gain=new Parameter(this,1,"gain",paramValues.getString("Gain"));
		this.multiplication = "Matrix(*)".equals(paramValues.getString("Multiplication"));

		InputPort in;
		OutputPort out;
		
		if(!this.multiplication) {
			out = new OutputPort(this,1,true);
			in = new InputPort(this,1);
		}else {			
			//out = new OutputPort(this,1,true,gain.row);
			//in = new InputPort(this,1,gain.column);
			
			out = new OutputPort(this,1,true);
			in = new InputPort(this,1);
		}
		//一锟斤拷锟斤拷锟�
		
		outputPortList.add(out);
		
		//一锟斤拷锟斤拷锟斤拷
		inputPortList.add(in);
		
//		gain=new Parameter(this,1,"value");
//		parameterList.add(gain);
		
		parameterList.add(gain);
		
	}
	public void generateInitCodeM(CodeStructM code) {
		super.generateInitCodeM(code);
		String initCode="";
		initCode+=gain.getInitCodeM();
		code.addInitCode(initCode);
	}
	public void generateOutputCodeM(CodeStructM code) {
		super.generateOutputCodeM(code);
		OutputPort out  = outputPortList.get(0);
		OutputPort ops = inputPortList.get(0).getLinkedLine().getLinkedOutputPort();
		String outputCode="";
		//outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"="+inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName()+"*"+paramValues.getString("Gain")+";\n";
		if(this.multiplication==false) {
			switch(gain.getDataType()) {
			case REAL:
				switch(ops.getOutputSignalC().getDataType()) {
				case REAL:
					outputCode+=out.getOutputSignalC().getName()+"=";
					outputCode+=gain.getName()+"*"+ops.getOutputSignalC().getName()+";\n";
					break;
				case MATRIX:
					for(int i=1; i<ops.getHeight()+1; i++) {
						for(int j=1;j<ops.getWidth()+1;j++) {
							outputCode+=out.getOutputSignalC().getName()+"("+i+","+j+")=";
							outputCode+=gain.getName()+"*"+ops.getOutputSignalC().getName()+"("+i+","+j+");\n";
						}	
					}
					break;
				}
				break;
			case MATRIX:
				switch(ops.getOutputSignalC().getDataType()) {
				case REAL:
					for(int i=1; i<gain.getHeight()+1; i++) {
						for(int j=1;j<gain.getWidth()+1;j++) {
							outputCode+=out.getOutputSignalC().getName()+"("+i+","+j+")=";
							outputCode+=gain.getName()+"("+i+","+j+")"+"*"+ops.getOutputSignalC().getName()+";\n";
						}	
					}
					break;
				case MATRIX:
				for(int i=1; i<ops.getHeight()+1; i++) {
					for(int j=1;j<ops.getWidth()+1;j++) {
						outputCode+=out.getOutputSignalC().getName()+"("+i+","+j+")=";
						outputCode+=gain.getName()+"("+i+","+j+")"+"*"+ops.getOutputSignalC().getName()+"("+i+","+j+");\n";
					}	
				}
				break;
			}
				break;
		  }
		}
		else {
			switch(gain.getDataType()) {
			case REAL:
				switch(ops.getOutputSignalC().getDataType()) {
				case REAL:
					outputCode+=out.getOutputSignalC().getName()+"=";
					outputCode+=gain.getName()+"*"+ops.getOutputSignalC().getName()+";\n";
					break;
				case MATRIX:
					for(int i=1;i<ops.getHeight()+1;i++) {
					outputCode+=out.getOutputSignalC().getName()+"("+i+",1)"+"="+gain.getName()+"*"+ops.getOutputSignalC().getName()+"("+i+",1);\n";
					}
					break;
				}
				break;
			case MATRIX:
				switch(ops.getOutputSignalC().getDataType()) {
				case REAL:
					for(int i=1;i<gain.getWidth()+1;i++){
					outputCode+=out.getOutputSignalC().getName()+"(1,"+i+")"+"="+gain.getName()+"(1,"+i+")*"+ops.getOutputSignalC().getName()+";\n";
					}
					break;
				case MATRIX:
					outputCode+=out.getOutputSignalC().getName()+"="+ops.getOutputSignalC().getName()+"*"+gain.getName()+";\n";
					break;
				}				
			}
		}
		code.addOutputCode(outputCode);
	}
	
	
	public void generateInitCodeC(CodeStructC code) {
		super.generateInitCodeC(code);
		
		String initCode="/*Code for initialization of block Gain:("+getBlockId()+")"+getBlockName()+"*/\n";
		initCode+=gain.getInitCodeC();
		code.addInitCode(initCode);
	}
	
	public void generateOutputCodeC(CodeStructC code) {
		String outputCode="/*Code for output of block Constant:("+getBlockId()+")"+getBlockName()+"*/\n";
		
		OutputPort out  = outputPortList.get(0);
		OutputPort ops = inputPortList.get(0).getLinkedLine().getLinkedOutputPort();
		
		if(this.multiplication==false) {
			switch(gain.getDataType()) {
			case REAL:
				switch(ops.getOutputSignalC().getDataType()) {
				case REAL:
					outputCode+=out.getOutputSignalC().getName()+"=";
					outputCode+=gain.getName()+"*"+ops.getOutputSignalC().getName()+";\n";
					break;
				case MATRIX:
					for(int i=0; i<ops.getHeight(); i++) {
						for(int j=0;j<ops.getWidth();j++) {
							outputCode+=out.getOutputSignalC().getName()+"("+i+","+j+")=";
							outputCode+=gain.getName()+"*"+ops.getOutputSignalC().getName()+"("+i+","+j+");\n";
						}	
					}
					break;
				}
				break;
			case MATRIX:
				switch(ops.getOutputSignalC().getDataType()) {
				case REAL:
					for(int i=0; i<gain.getHeight(); i++) {
						for(int j=0;j<gain.getWidth();j++) {
							outputCode+=out.getOutputSignalC().getName()+"("+i+","+j+")=";
							outputCode+=gain.getName()+"("+i+","+j+")"+"*"+ops.getOutputSignalC().getName()+";\n";
						}	
					}
					break;
				case MATRIX:
				for(int i=0; i<ops.getHeight(); i++) {
					for(int j=0;j<ops.getWidth();j++) {
						outputCode+=out.getOutputSignalC().getName()+"("+i+","+j+")=";
						outputCode+=gain.getName()+"("+i+","+j+")"+"*"+ops.getOutputSignalC().getName()+"("+i+","+j+");\n";
					}	
				   }
				break;
				}
				break;
			}
		}
		else {
			switch(gain.getDataType()) {
			case REAL:
				switch(ops.getOutputSignalC().getDataType()) {
				case REAL:
					outputCode+=out.getOutputSignalC().getName()+"=";
					outputCode+=gain.getName()+"*"+ops.getOutputSignalC().getName()+";\n";
					break;
				case MATRIX:
					outputCode+="for(int i=0;i<"+ops.getHeight()+";i++){\n";
					outputCode+=out.getOutputSignalC().getName()+"(i,0)"+"="+gain.getName()+"*"+ops.getOutputSignalC().getName()+"(i,0);\n";
					outputCode+="}\n";
					break;
				}
				break;
			case MATRIX:
				switch(ops.getOutputSignalC().getDataType()) {
				case REAL:
					outputCode+="for(int i=0;i<"+gain.getWidth()+";i++){\n";
					outputCode+=out.getOutputSignalC().getName()+"[0][i]"+"="+gain.getName()+"[0][i]*"+ops.getOutputSignalC().getName()+";\n";
					outputCode+="}\n";
					break;
				case MATRIX:
					outputCode+=out.getOutputSignalC().getName()+"="+ops.getOutputSignalC().getName()+"*"+gain.getName()+";\n";
					break;
				}				
			}
		}
		
		
		code.addOutputCode(outputCode);
	}
	
	
	public void updateDimension() throws MatDimException{
		OutputPort out  = outputPortList.get(0);
		InputPort in  = inputPortList.get(0);
		OutputSignal signal=in.getLinkedLine().getLinkedOutputPort().getOutputSignalC();
		if(this.multiplication==false) {
			switch(gain.getDataType()) {
			case REAL:
				out.setHeight(signal.getHeight());
				out.setWidth(signal.getWidth());
				out.getOutputSignalC().setHeight(signal.getHeight());
				out.getOutputSignalC().setWidth(signal.getWidth());
				out.getOutputSignalC().setDataType(signal.getDataType());
				break;
			case MATRIX:
				switch(signal.getDataType()) {
				case REAL:
					out.setHeight(gain.getHeight());
					out.setWidth(gain.getWidth());
					out.getOutputSignalC().setHeight(gain.getHeight());
					out.getOutputSignalC().setWidth(gain.getWidth());
					out.getOutputSignalC().setDataType(gain.getDataType());
					break;
				case MATRIX:
				if(signal.getHeight()!=gain.getHeight()||signal.getWidth()!=gain.getWidth()) {
					MatDimException e=new MatDimException("Block "+this.blockName+" input dimension doesn't match the gain dimension!\n \n");
					throw(e);
				}
				out.setHeight(signal.getHeight());
				out.setWidth(signal.getWidth());
				out.getOutputSignalC().setHeight(signal.getHeight());
				out.getOutputSignalC().setWidth(signal.getWidth());
				out.getOutputSignalC().setDataType(signal.getDataType());
				break;
				}
				break;
			}
		}
		else {
			if(signal.getWidth()!=gain.getHeight()) {
				MatDimException e=new MatDimException("Block "+this.blockName+" input dimension doesn't match the gain dimension!\n \n");
				throw(e);
			}
			
			out.setHeight(signal.getHeight());
			out.setWidth(gain.getWidth());
			out.getOutputSignalC().setHeight(signal.getHeight());
			out.getOutputSignalC().setWidth(gain.getWidth());
			out.getOutputSignalC().setDataType(DataType.MATRIX);
		}
	}
	
	public void checkDimension() throws MatDimException{
	}
}
