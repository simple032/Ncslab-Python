package com.ncslab.block.discontinuous;
import org.json.JSONObject;

import com.ncslab.block.Block;
import com.ncslab.block.data.DataType;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.block.io.Parameter;
import com.ncslab.block.io.State;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;

public class Relay extends Block{
	Parameter onSwitchValue;
	Parameter offSwitchValue;
	Parameter onOutputValue;
	Parameter offOutputValue;
	private State xState;
	
	public Relay(JSONObject blockIn,NCSLabModel model) {
		
		super(blockIn,model);
		//һ�����룬һ�����
		inputPortList.add(new InputPort(this,1));
		outputPortList.add(new OutputPort(this,1,true));
		onSwitchValue=new Parameter(this,1,"onSwitchValue",paramValues.getString("OnSwitchValue"));
		offSwitchValue=new Parameter(this,2,"offSwitchValue",paramValues.getString("OffSwitchValue"));
		onOutputValue=new Parameter(this,3,"onOutputValue",paramValues.getString("OnOutputValue"));
		offOutputValue=new Parameter(this,4,"offOutputValue",paramValues.getString("OffOutputValue"));
		parameterList.add(onSwitchValue);
		parameterList.add(offSwitchValue);
		parameterList.add(onOutputValue);
		parameterList.add(offOutputValue);
	}

	 public void generateInitCodeC(CodeStructC code) {
			super.generateInitCodeC(code);
			String initCode="/*Code for initialization of block Relay:("+getBlockId()+")"+getBlockName()+"*/\n";
			initCode+=onSwitchValue.getInitCodeC();
			initCode+=offSwitchValue.getInitCodeC();
			initCode+=onOutputValue.getInitCodeC();
			initCode+=offOutputValue.getInitCodeC();
			if(onSwitchValue.getDataType()==DataType.REAL&&xState.getDataType()==DataType.REAL) {
		     initCode+=xState.getName()+"=0;\n";
			}
			else {
				for(int i=0; i<xState.getHeight(); i++) {
					for(int j=0;j<xState.getWidth();j++) {	
						initCode+=xState.getName()+"("+i+","+j+")=0;\n";
					}
				}
			}
			code.addInitCode(initCode);
		}
	 
	 public void generateOutputCodeC(CodeStructC code) {
			String outputCode="/*Code for output of block Relay:("+getBlockId()+")"+getBlockName()+"*/\n";
			
			OutputPort out  = outputPortList.get(0);
			OutputPort ops = inputPortList.get(0).getLinkedLine().getLinkedOutputPort();
			OutputSignal signal=inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
			switch(onSwitchValue.getDataType()) {
			case REAL:
				switch(ops.getOutputSignalC().getDataType()) {
				case REAL:
					outputCode+="if("+signal.getName()+">"+onSwitchValue.getName()+") {\n";
					outputCode+=xState.getName()+"="+onOutputValue.getName()+";}\n";
					outputCode+="else {\n";
					outputCode+="if("+signal.getName()+"<"+offSwitchValue.getName()+") {\n";
					outputCode+=xState.getName()+"="+offOutputValue.getName()+";}}\n";
					outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"="+xState.getName()+";\n";
					break;
				case MATRIX:
					for(int i=0; i<ops.getHeight(); i++) {
						for(int j=0;j<ops.getWidth();j++) {
							outputCode+="if("+signal.getName()+"("+i+","+j+")>"+onSwitchValue.getName()+") {\n";
							outputCode+=xState.getName()+"("+i+","+j+")="+onOutputValue.getName()+";}\n";
							outputCode+="else {\n";
							outputCode+="if("+signal.getName()+"("+i+","+j+")<"+offSwitchValue.getName()+") {\n";
							outputCode+=xState.getName()+"("+i+","+j+")="+offOutputValue.getName()+";}}\n";
							outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"("+i+","+j+")="+xState.getName()+"("+i+","+j+");\n";
						}
					}
					break;
				}
				break;
			case MATRIX:
				switch(ops.getOutputSignalC().getDataType()) {
				case REAL:
					for(int i=0; i<onSwitchValue.getHeight(); i++) {
						for(int j=0;j<onSwitchValue.getWidth();j++) {
							outputCode+="if("+signal.getName()+">"+onSwitchValue.getName()+"("+i+","+j+")) {\n";
							outputCode+=xState.getName()+"("+i+","+j+")="+onOutputValue.getName()+"("+i+","+j+");}\n";
							outputCode+="else {\n";
							outputCode+="if("+signal.getName()+"<"+offSwitchValue.getName()+"("+i+","+j+")) {\n";
							outputCode+=xState.getName()+"("+i+","+j+")="+offOutputValue.getName()+"("+i+","+j+");}}\n";
							outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"("+i+","+j+")="+xState.getName()+"("+i+","+j+");\n";
						}
					}
					break;
				case MATRIX:
					for(int i=0; i<ops.getHeight(); i++) {
						for(int j=0;j<ops.getWidth();j++) {
							outputCode+="if("+signal.getName()+"("+i+","+j+")>"+onSwitchValue.getName()+"("+i+","+j+")) {\n";
							outputCode+=xState.getName()+"("+i+","+j+")="+onOutputValue.getName()+"("+i+","+j+");}\n";
							outputCode+="else {\n";
							outputCode+="if("+signal.getName()+"("+i+","+j+")<"+offSwitchValue.getName()+"("+i+","+j+")) {\n";
							outputCode+=xState.getName()+"("+i+","+j+")="+offOutputValue.getName()+"("+i+","+j+");}}\n";
							outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"("+i+","+j+")="+xState.getName()+"("+i+","+j+");\n";
						}
					}
					
					break;
				}
		      break;
			}
		      code.addOutputCode(outputCode);
	 }
	 
	 
	public void generateDerivativeCodeC(CodeStructC code) {
		   super.generateDerivativeCodeC(code);
		   
		   String derivativeCode="/*Code for Derivative of block Relay:("+getBlockId()+")"+getBlockName()+"*/\n";
		   
		   derivativeCode+=xState.getDerivativeName()+"=0*"+xState.getName()+";\n";
		   
		   code.addDerivativeCode(derivativeCode);
	   }
	 public void updateDimension() throws MatDimException{
			OutputPort out  = outputPortList.get(0);
			InputPort in  = inputPortList.get(0);
			OutputSignal signal=in.getLinkedLine().getLinkedOutputPort().getOutputSignalC();
			if(signal.getDataType()==DataType.REAL) {
			xState=new State(this,1,"save_data",onSwitchValue.getHeight(),onSwitchValue.getWidth());}
			else {
				xState=new State(this,1,"save_data",signal.getHeight(),signal.getWidth());	
			}
			stateList.add(xState);
			if(onSwitchValue.getWidth()!=offSwitchValue.getWidth()
					||onSwitchValue.getWidth()!=onOutputValue.getWidth()
					||onSwitchValue.getWidth()!=offOutputValue.getWidth()
					||onSwitchValue.getHeight()!=offSwitchValue.getHeight()
					||onSwitchValue.getHeight()!=onOutputValue.getHeight()
					||onSwitchValue.getHeight()!=offOutputValue.getHeight()) {
				MatDimException e=new MatDimException("Block "+this.blockName+" input dimensions don't match!All input dimensions should be same!");
				throw(e);	
			}
			if(onSwitchValue.getDataType()==DataType.MATRIX&&signal.getDataType()==DataType.REAL) {
				out.setHeight(onSwitchValue.getHeight());
				out.setWidth(onSwitchValue.getWidth());
				out.getOutputSignalC().setHeight(onSwitchValue.getHeight());
				out.getOutputSignalC().setWidth(onSwitchValue.getWidth());
				out.getOutputSignalC().setDataType(DataType.MATRIX);	
			}
			else if(onSwitchValue.getDataType()==DataType.REAL&&signal.getDataType()==DataType.MATRIX) {
				out.setHeight(signal.getHeight());
				out.setWidth(signal.getWidth());
				out.getOutputSignalC().setHeight(signal.getHeight());
				out.getOutputSignalC().setWidth(signal.getWidth());
				out.getOutputSignalC().setDataType(signal.getDataType());	
			}
			else{
				if(onSwitchValue.getWidth()!=signal.getWidth()||onSwitchValue.getHeight()!=signal.getHeight()) {
				MatDimException e=new MatDimException("Block "+this.blockName+" input dimension doesn't match the gain dimension!\n \n");
				throw(e);
				}
				out.setHeight(onSwitchValue.getHeight());
				out.setWidth(onSwitchValue.getWidth());
				out.getOutputSignalC().setHeight(onSwitchValue.getHeight());
				out.getOutputSignalC().setWidth(onSwitchValue.getWidth());
				out.getOutputSignalC().setDataType(onSwitchValue.getDataType());	
			}
	  }
	 public void checkDimension() throws MatDimException{
	}    
}

