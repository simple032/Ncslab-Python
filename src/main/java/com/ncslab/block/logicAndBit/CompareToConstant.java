package com.ncslab.block.logicAndBit;

import lombok.Getter;
import org.json.JSONObject;

import com.ncslab.block.Block;
import com.ncslab.block.data.DataType;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;

import java.util.Vector;

public class CompareToConstant extends Block{
	Parameter value;

    String relop;

    @Getter
    public static final Vector<String> parameterNames = new Vector<>();

    @Getter
    public static final Vector<String> outputNames = new Vector<>();
    @Getter
    public static final Vector<String> inputNames = new Vector<>();

    static {

        outputNames.add("out1");
        inputNames.add("in1");
        parameterNames.add("value");
    }

  public CompareToConstant(JSONObject blockIn,NCSLabModel model) {
		super(blockIn,model);
		inputPortList.add(new InputPort(this,1));
		outputPortList.add(new OutputPort(this,1,true));
		value = new Parameter(this,1,"value",paramValues.getString("const"));
		parameterList.add(value);

        relop=paramValues.getString("relop");
      if(relop.equals("~=")) {
          relop = "!=";
      }
  }

  public void generateInitCodeC(CodeStructC code) {
		super.generateInitCodeC(code);
		String initCode="/*Code for initialization of block Compare To Constant:("+getBlockId()+")"+getBlockName()+"*/\n";
		initCode+=value.getInitCodeC();
		code.addInitCode(initCode);
  }

  public void generateOutputCodeC(CodeStructC code) {
		String outputCode="/*Code for output of block Compare To Constant:("+getBlockId()+")"+getBlockName()+"*/\n";
		OutputPort out  = outputPortList.get(0);
		OutputPort ops = inputPortList.get(0).getLinkedLine().getLinkedOutputPort();
		OutputSignal signal=inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
		String relop=paramValues.getString("relop");

		switch(signal.getDataType()) {
		case REAL:
			switch(value.getDataType()) {
			case REAL:
                 outputCode+="if("+signal.getName()+relop+value.getName()+") {\n";
                 outputCode+=out.getOutputSignalC().getName()+"= 1.0;}else{\n";
                 outputCode+=out.getOutputSignalC().getName()+"= 0.0;}\n";
                 break;
			case MATRIX:
				for(int i = 0; i < value.getHeight(); i++) {
					for(int j = 0; j < value.getWidth(); j++) {
						outputCode+="if("+signal.getName()+relop+value.getName()+"("+i+","+j+")) {\n";
		                outputCode+=out.getOutputSignalC().getName()+"("+i+","+j+")= 1.0;}else{\n";
		                outputCode+=out.getOutputSignalC().getName()+"("+i+","+j+")= 0.0;}\n";
					}
				}
				break;
			}
			break;
		case MATRIX:
			switch(value.getDataType()) {
			case REAL:
				for(int i=0; i < signal.getHeight(); i++) {
					for(int j=0; j < signal.getWidth(); j++) {
						outputCode+="if("+signal.getName()+"("+i+","+j+")"+relop+value.getName()+") {\n";
			            outputCode+=out.getOutputSignalC().getName()+"("+i+","+j+")= 1.0;}else{\n";
			            outputCode+=out.getOutputSignalC().getName()+"("+i+","+j+")= 0.0;}\n";
					   }
				   }
			     break;
			case MATRIX:
				for(int i=0; i < signal.getHeight(); i++) {
					for(int j=0; j < signal.getWidth(); j++) {
						outputCode+="if("+signal.getName()+"("+i+","+j+")"+relop+value.getName()+"("+i+","+j+")) {\n";
			            outputCode+=out.getOutputSignalC().getName()+"("+i+","+j+")= 1.0;}else{\n";
			            outputCode+=out.getOutputSignalC().getName()+"("+i+","+j+")= 0.0;}\n";
					   }
				   }
			     break;
			}
			break;
		}
		code.addOutputCode(outputCode);
  }

  public void updateDimension() throws MatDimException{
	    OutputPort out  = outputPortList.get(0);
	    InputPort in  = inputPortList.get(0);
	    OutputSignal signal=in.getLinkedLine().getLinkedOutputPort().getOutputSignalC();

	    if(value.getDataType()==DataType.MATRIX&&signal.getDataType()==DataType.REAL) {
			out.setHeight(value.getHeight());
			out.setWidth(value.getWidth());
			out.getOutputSignalC().setHeight(value.getHeight());
			out.getOutputSignalC().setWidth(value.getWidth());
			out.getOutputSignalC().setDataType(DataType.MATRIX);
		}
		else if(value.getDataType()==DataType.REAL&&signal.getDataType()==DataType.MATRIX) {
			out.setHeight(signal.getHeight());
			out.setWidth(signal.getWidth());
			out.getOutputSignalC().setHeight(signal.getHeight());
			out.getOutputSignalC().setWidth(signal.getWidth());
			out.getOutputSignalC().setDataType(signal.getDataType());
		}
		else{
			if(value.getWidth()!=signal.getWidth()||value.getHeight()!=signal.getHeight()) {
			MatDimException e=new MatDimException("Block "+this.blockName+" input dimension doesn't match the Compare To Constant dimension!\n \n");
			throw(e);
			}
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
