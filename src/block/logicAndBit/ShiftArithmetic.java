package block.logicAndBit;

import org.json.JSONObject;

import block.Block;
import block.data.DataType;
import block.io.InputPort;
import block.io.OutputPort;
import block.io.OutputSignal;
import block.io.Parameter;
import code.c.CodeStructC;
import ncslablink.MatDimException;
import ncslablink.NCSLabModel;

public class ShiftArithmetic extends Block{
	block.io.Parameter value;
	
	public ShiftArithmetic(JSONObject blockIn,NCSLabModel model) {
		super(blockIn,model);
		inputPortList.add(new InputPort(this,1));
		outputPortList.add(new OutputPort(this,1,true));
		value = new Parameter(this,1,"value",paramValues.getString("BitShiftNumber"));
		parameterList.add(value);
   }
	
	public void generateInitCodeC(CodeStructC code) {
		super.generateInitCodeC(code);
		String initCode="/*Code for initialization of block Shift Arithmetic:("+getBlockId()+")"+getBlockName()+"*/\n";
		initCode+=value.getInitCodeC();
		code.addInitCode(initCode);
  }
	
	public void generateOutputCodeC(CodeStructC code) {
		String outputCode="/*Code for output of block Shift Arithmetic:("+getBlockId()+")"+getBlockName()+"*/\n";
		OutputPort out  = outputPortList.get(0);
		OutputSignal signal=inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
		String direction=paramValues.getString("BitShiftDirection");
		switch(signal.getDataType()) {
		case REAL:
			switch(direction) {
			case "Left":
				 outputCode+=out.getOutputSignalC().getName()+"="+signal.getName()+"*pow(2,"+value.getName()+");\n";
				break;
			case "Right":
				outputCode+=out.getOutputSignalC().getName()+"="+signal.getName()+"*pow(0.5,"+value.getName()+");\n";
				break;
			}
			break;
		case MATRIX:
			for(int i=0; i < signal.getHeight(); i++) {
				for(int j=0; j < signal.getWidth(); j++) {
					switch(direction){
					case "Left":
						outputCode+=out.getOutputSignalC().getName()+"("+i+","+j+")="+signal.getName()+"("+i+","+j+")*pow(2,"+value.getName()+");\n";
						break;
					case "Right":
						outputCode+=out.getOutputSignalC().getName()+"("+i+","+j+")="+signal.getName()+"("+i+","+j+")*pow(0.5,"+value.getName()+");\n";
						break;
				   }
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
	  if(value.getDataType() == DataType.MATRIX) {
		 MatDimException e=new MatDimException("Block "+this.blockName+" param Number can't be MATRIX!\n \n");
		throw(e); 
	  }
  }
}
