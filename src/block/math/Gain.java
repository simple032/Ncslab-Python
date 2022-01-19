package block.math;

import org.json.JSONObject;

import block.Block;
import block.data.DataType;
import block.io.OutputPort;
import block.io.Parameter;
import code.c.CodeStructC;
import code.m.CodeStructM;
import block.io.InputPort;
import block.io.OutputSignal;
import ncslablink.MatDimException;
import ncslablink.NCSLabModel;

public class Gain extends Block{
	block.io.Parameter gain;
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
	
	public void generateOutputCodeM(CodeStructM code) {
		super.generateOutputCodeM(code);
		//String code="Block"+this.getBlockId()+"_Output1="+paramValues.getDouble("Gain")+"*Block"+getInputPortList().get(0).getLinkedLine().getLinkedOutputPort().getBLock().getBlockId()+"_Output"+getInputPortList().get(0).getLinkedLine().getLinkedOutputPort().getNumber()+";\n";
		String ouputCode=outputPortList.get(0).getOutputSignalC().getName()+"="+inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName()+"*"+paramValues.getString("Gain")+";\n";
		
		code.addOutputCode(ouputCode);
	}
	
	
	public void generateInitCodeC(CodeStructC code) {
		super.generateInitCodeC(code);
		
		String initCode="/*Code for initialization of block Gain:("+getBlockId()+")"+getBlockName()+"*/\n";
		//initCode+=gain.getName()+"="+paramValues.getDouble("Gain")+";\n"; 
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
							outputCode+=out.getOutputSignalC().getName()+"["+i+"]["+j+"]=";
							outputCode+=gain.getName()+"*"+ops.getOutputSignalC().getName()+"["+i+"]["+j+"];\n";
						}	
					}
					break;
				}
				break;
			case MATRIX:
				for(int i=0; i<ops.getHeight(); i++) {
					for(int j=0;j<ops.getWidth();j++) {
						outputCode+=out.getOutputSignalC().getName()+"["+i+"]["+j+"]=";
						outputCode+=gain.getName()+"["+i+"]["+j+"]"+"*"+ops.getOutputSignalC().getName()+"["+i+"]["+j+"];\n";
					}	
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
					outputCode+=out.getOutputSignalC().getName()+"[i][0]"+"="+gain.getName()+"*"+ops.getOutputSignalC().getName()+"[i][0];\n";
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
					outputCode+="for(int i=0;i<"+ops.getHeight()+";i++){\n";
					outputCode+="for(int j=0;j<"+gain.getWidth()+";j++){\n";
					outputCode+=out.getOutputSignalC().getName()+"[i][j]"+"=0;\n";
					outputCode+="for(int k=0;k<"+gain.getHeight()+";k++){\n";
					outputCode+=out.getOutputSignalC().getName()+"[i][j]"+"+="+ops.getOutputSignalC().getName()+"[i][k]*"+gain.getName()+"[k][j]"+";\n";
					outputCode+="}\n";
					outputCode+="}\n";
					outputCode+="}\n";
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
		/*
		if(!this.multiplication) {			
			out.setWidth(in.getWidth());
			out.getOutputSignalC().setWidth(in.getWidth());
		}else {
			
		}*/
		if(this.multiplication==false) {
			switch(gain.getDataType()) {
			case REAL:
				/*
				if(signal.getHeight()!=in.getHeight()||signal.getWidth()!=in.getWidth()) {
					MatDimException e=new MatDimException("Block "+this.blockName+" input dimension error!\n \n");
					throw(e);
				}*/
				out.setHeight(signal.getHeight());
				out.setWidth(signal.getWidth());
				out.getOutputSignalC().setHeight(signal.getHeight());
				out.getOutputSignalC().setWidth(signal.getWidth());
				out.getOutputSignalC().setDataType(signal.getDataType());
				break;
			case MATRIX:
				/*
				if(signal.getHeight()!=in.getHeight()||signal.getWidth()!=in.getWidth()) {
					MatDimException e=new MatDimException("Block "+this.blockName+" input dimension error!\n \n");
					throw(e);
				}*/
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
}
