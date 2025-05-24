package com.ncslab.block.subsystem;
import java.util.Vector;

import com.ncslab.block.io.Parameter;
import lombok.Getter;
import lombok.Setter;
import org.json.JSONObject;
import com.ncslab.block.Block;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.block.Block;

public class In extends Block{

    @Getter
    Parameter no;
    @Setter
    Subsystem subsystem;

    @Getter
    public static final Vector<String> outputNames = new Vector<>();
    @Getter
    public static final Vector<String> parameterNames = new Vector<>();

    static {

        outputNames.add("out1");
        parameterNames.add("No");
    }
	public In(JSONObject blockJSON,NCSLabModel model) {
		super(blockJSON,model);
        inputPortList.add(new InputPort(this,1));
		outputPortList.add(new OutputPort(this,1,true));
        no = new Parameter(this, 1, "no", String.valueOf(paramValues.getInt("No")));
        parameterList.add(no);
	}
	public void generateOutputCodeC(CodeStructC code) {
		String outputCode="/*Code for output of block In:("+getBlockId()+")"+getBlockName()+"*/\n";
	    outputCode+=this.getOutputPortVariable(0)+"="+this.getInputPortVariable(0)+";\n";
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

    @Override
    public void calculateOutput(double t) {
        InputPort in = inputPortList.get(0);
        OutputPort out = outputPortList.get(0);
        out.setData(in.getData());
    }

    @Override
    public void calculateInit() {
        InputPort in = inputPortList.get(0);
        OutputPort out = outputPortList.get(0);
        out.setData(in.getData());
    }
}
