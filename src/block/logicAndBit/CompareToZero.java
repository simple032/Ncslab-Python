package block.logicAndBit;

import org.json.JSONObject;

import block.Block;
import block.io.InputPort;
import block.io.OutputPort;
import block.io.OutputSignal;
import code.c.CodeStructC;
import ncslablink.MatDimException;
import ncslablink.NCSLabModel;

public class CompareToZero extends Block{
	
	public CompareToZero(JSONObject blockIn,NCSLabModel model) {
		super(blockIn,model);
		inputPortList.add(new InputPort(this,1));
		outputPortList.add(new OutputPort(this,1,true));
    }
	
	public void generateInitCodeC(CodeStructC code) {
		super.generateInitCodeC(code);
		String initCode="/*Code for initialization of block Compare To Zero:("+getBlockId()+")"+getBlockName()+"*/\n";
		code.addInitCode(initCode);
	}
	
	public void generateOutputCodeC(CodeStructC code) {
		String outputCode="/*Code for output of block Compare To Zreo:("+getBlockId()+")"+getBlockName()+"*/\n";
		OutputPort out  = outputPortList.get(0);
		OutputSignal signal=inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
		String relop=paramValues.getString("relop");
		if(relop.equals("~=")) {
			relop = "!=";
		}
		switch(signal.getDataType()) {
		case REAL:
			outputCode+="if("+signal.getName()+relop+"0) {\n";
            outputCode+=out.getOutputSignalC().getName()+"= 1.0;}else{\n";
            outputCode+=out.getOutputSignalC().getName()+"= 0.0;}\n";
			break;
		case MATRIX:
			for(int i=0; i < signal.getHeight(); i++) {
				for(int j=0; j < signal.getWidth(); j++) {
					outputCode+="if("+signal.getName()+"("+i+","+j+")"+relop+"0) {\n";
		            outputCode+=out.getOutputSignalC().getName()+"("+i+","+j+")= 1.0;}else{\n";
		            outputCode+=out.getOutputSignalC().getName()+"("+i+","+j+")= 0.0;}\n";
				   }
			   }
			break;
		}
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
	}

}
