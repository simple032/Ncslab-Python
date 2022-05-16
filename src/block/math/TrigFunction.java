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
	public TrigFunction(JSONObject blockJSON,NCSLabModel model) {
		super(blockJSON,model);
		//����һ�����
		outputPortList.add(new OutputPort(this,1,true));
		//����һ������
		inputPortList.add(new InputPort(this,1));	
	}
	public void generateOutputCodeC(CodeStructC code) {
		String outputCode="/*Code for output of block Tr:("+getBlockId()+")"+getBlockName()+"*/\n";
		OutputPort out  = outputPortList.get(0);
		OutputSignal signal = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
		switch(signal.getDataType()) {
		case REAL:
			outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"=sin("+signal.getName()+");\n";
			break;
		case MATRIX:
				for(int i=0;i<signal.getHeight();i++) {
					for(int j=0;j<signal.getWidth();j++) {
					    outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"("+i+","+j+")=sin("+signal.getName()+"("+i+","+j+"));\n";
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

