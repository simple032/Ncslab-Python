package com.ncslab.block.logicAndBit;

import lombok.Getter;
import org.json.JSONObject;

import com.ncslab.block.Block;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;

import java.util.Vector;

public class RelationalOperator extends Block{



    @Getter
    public static final Vector<String> outputNames = new Vector<>();
    @Getter
    public static final Vector<String> inputNames = new Vector<>();

    static {

        outputNames.add("out1");
        inputNames.add("in1");
        inputNames.add("in2");
    }

	public RelationalOperator(JSONObject blockIn,NCSLabModel model) {
		super(blockIn,model);
		inputPortList.add(new InputPort(this,1));
		inputPortList.add(new InputPort(this,2));
		OutputPort output=new OutputPort(this,1,true);
		outputPortList.add(output);
  }

	 public void generateOutputCodeC(CodeStructC code) {
			String outputCode="/*Code for output of block Relational operator:("+getBlockId()+")"+getBlockName()+"*/\n";
			OutputPort out  = outputPortList.get(0);
			OutputSignal signal1=inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
			OutputSignal signal2=inputPortList.get(1).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
			String operator=paramValues.getString("Operator");
			switch(signal1.getDataType()) {
			case REAL:
				if(operator.equals("~=")) {
				outputCode+="if("+signal1.getName()+"!="+signal2.getName()+") {\n";
				outputCode+=out.getOutputSignalC().getName()+"=1.0;}else{\n";
				outputCode+=out.getOutputSignalC().getName()+"=0.0;}\n";
				}else {
					outputCode+="if("+signal1.getName()+operator+signal2.getName()+") {\n";
					outputCode+=out.getOutputSignalC().getName()+"=1.0;}else{\n";
					outputCode+=out.getOutputSignalC().getName()+"=0.0;}\n";
				}
				break;
			case MATRIX:
				if(operator.equals("~=")) {
					for(int i = 0; i < signal1.getHeight(); i++) {
						for(int j = 0; j < signal1.getWidth(); j++) {
							outputCode+="if("+signal1.getName()+"("+i+","+j+")!="+signal2.getName()+"("+i+","+j+")) {\n";
							outputCode+=out.getOutputSignalC().getName()+"("+i+","+j+")=1.0;}else{\n";
							outputCode+=out.getOutputSignalC().getName()+"("+i+","+j+")=0.0;}\n";
						}
					}
				}else {
					for(int i = 0; i < signal1.getHeight(); i++) {
						for(int j = 0; j < signal1.getWidth(); j++) {
							outputCode+="if("+signal1.getName()+"("+i+","+j+")"+operator+signal2.getName()+"("+i+","+j+")) {\n";
							outputCode+=out.getOutputSignalC().getName()+"("+i+","+j+")=1.0;}else{\n";
							outputCode+=out.getOutputSignalC().getName()+"("+i+","+j+")=0.0;}\n";
						}
					}
				}
				break;
			}
			code.addOutputCode(outputCode);
	 }

	public void updateDimension() throws MatDimException{
	    OutputPort out  = outputPortList.get(0);
	    OutputSignal signal1=inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
	    OutputSignal signal2=inputPortList.get(1).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
	    if(signal1.getHeight() != signal2.getHeight() || signal1.getWidth() != signal2.getWidth()) {
			 MatDimException e=new MatDimException("Block "+this.blockName+" two input dimension don't match!\n \n");
			throw(e);
		  }
			out.setHeight(signal1.getHeight());
			out.setWidth(signal1.getWidth());
			out.getOutputSignalC().setHeight(signal1.getHeight());
			out.getOutputSignalC().setWidth(signal1.getWidth());
			out.getOutputSignalC().setDataType(signal1.getDataType());
   }

  public void checkDimension() throws MatDimException{
  }
}
