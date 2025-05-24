package com.ncslab.block.route;

import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.MatDimException;
import lombok.Getter;
import org.json.JSONObject;

import com.ncslab.block.Block;
import com.ncslab.block.io.InputPort;
import com.ncslab.ncslablink.NCSLabModel;

import java.util.Vector;

public class To extends Block {
	@Getter
    private String tagName;


    @Getter
    public static final Vector<String> inputNames = new Vector<>();
    @Getter
    public static final Vector<String> parameterNames = new Vector<>();

    static {


        inputNames.add("in1");
        parameterNames.add("GotoTag");
    }
	public To(JSONObject blockIn,NCSLabModel model) {
		super(blockIn,model);
        inputPortList.add(new InputPort(this,1));
        outputPortList.add(new OutputPort(this,1,true));
		tagName=paramValues.getString("GotoTag");
		//System.out.println(tagName);
	}

    public void generateOutputCodeC(CodeStructC code) {
        String outputCode="/*Code for output of block In:("+getBlockId()+")"+getBlockName()+"*/\n";
        outputCode+=this.getOutputPortVariable(0)+"="+this.getInputPortVariable(0)+";\n";
        code.addOutputCode(outputCode);
    }

    public void updateDimension() throws MatDimException {
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
