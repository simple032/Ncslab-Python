package block.math;

import org.json.JSONObject;

import block.Block;
import block.data.DataType;
import block.io.OutputPort;
import block.io.OutputSignal;
import block.io.Parameter;
import code.c.CodeStructC;
import code.m.CodeStructM;
import block.io.InputPort;
import ncslablink.MatDimException;
import ncslablink.NCSLabModel;

public class TrigFunction extends Block{
	block.io.Parameter trigFunc;
	public TrigFunction(JSONObject blockJSON,NCSLabModel model) {
		super(blockJSON,model);
		
		//����һ�����
		outputPortList.add(new OutputPort(this,1,true));
		//����һ������
		inputPortList.add(new InputPort(this,1));	
		
		trigFunc=new Parameter(this,1,"trigFunc",paramValues.getString("TrigonometricFunction"));
		parameterList.add(trigFunc);
		
		if(paramValues.getString("TrigonometricFunction").equals("atan2")) {
			inputPortList.add(new InputPort(this,2));	
		}
	}
	public void generateOutputCodeC(CodeStructC code) {
		String outputCode="/*Code for output of block Tr:("+getBlockId()+")"+getBlockName()+"*/\n";
		OutputPort out  = outputPortList.get(0);
		OutputSignal signal = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
		switch(signal.getDataType()) {
		case REAL:
			if(!paramValues.getString("TrigonometricFunction").equals("atan2")) {
				outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"="+paramValues.getString("TrigonometricFunction")+"("+signal.getName()+");\n";
			}else {
				outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"="+paramValues.getString("TrigonometricFunction")+"("+signal.getName()+","+inputPortList.get(1).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName()+");\n";
			}
			break;
		case MATRIX:
			if(!paramValues.getString("TrigonometricFunction").equals("atan2")) {
				for(int i=0;i<signal.getHeight();i++) {
					for(int j=0;j<signal.getWidth();j++) {
					    outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"("+i+","+j+")="+paramValues.getString("TrigonometricFunction")+"("+signal.getName()+"("+i+","+j+"));\n";
			          }
				   }
			}else {
				for(int i=0;i<signal.getHeight();i++) {
					for(int j=0;j<signal.getWidth();j++) {
					    outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"("+i+","+j+")="+paramValues.getString("TrigonometricFunction")+"("+signal.getName()+","+inputPortList.get(1).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName()+"("+i+","+j+"));\n";
			          }
				   }
			}
			break;
		}
		code.addOutputCode(outputCode);
	}
	public void updateDimension() throws MatDimException{
		if(inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getDataType()==DataType.MATRIX) {
		OutputPort out  = outputPortList.get(0);
		OutputSignal signal=inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
			out.setHeight(signal.getHeight());
			out.setWidth(signal.getWidth());
			out.getOutputSignalC().setHeight(signal.getHeight());
			out.getOutputSignalC().setWidth(signal.getWidth());
			out.getOutputSignalC().setDataType(signal.getDataType());	
	   }
	}
    public void checkDimension() throws MatDimException{
		
	}

}

