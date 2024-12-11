package com.ncslab.block.source;

import lombok.Getter;
import org.json.JSONObject;

import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;

import java.util.Vector;

public class Clock extends com.ncslab.block.Block{




    @Getter
    public static final Vector<String> outputNames = new Vector<>();


    static {

        outputNames.add("out1");

    }
	public Clock(JSONObject blockJSON,NCSLabModel model) {
		super(blockJSON,model);
		outputPortList.add(new OutputPort(this,1,false));
	}
	public void generateOutputCodeC(CodeStructC code) {
		String outputCode="/*Code for output of block Clock::("+getBlockId()+")"+getBlockName()+"*/\n";
		outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"=model.time;\n";
		code.addOutputCode(outputCode);
	}
    public void updateDimension() throws MatDimException{

    }
	public void checkDimension() throws MatDimException{

	}
}
