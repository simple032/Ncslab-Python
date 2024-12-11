package com.ncslab.block.source;

import lombok.Getter;
import org.json.JSONObject;

import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.block.Block;

import java.util.Vector;

public class SineWave extends Block{
	Parameter amplitude;
	Parameter bias;
	Parameter frequency;
	Parameter phase;
	Parameter sampleTime;

    @Getter
    public static final Vector<String> parameterNames = new Vector<>();

    @Getter
    public static final Vector<String> outputNames = new Vector<>();


    static {

        outputNames.add("out1");
        parameterNames.add("amplitude");
        parameterNames.add("bias");
        parameterNames.add("frequency");
        parameterNames.add("phase");

    }

	public SineWave(JSONObject blockJSON,NCSLabModel model) {
		super(blockJSON, model);
		//����һ�����
		outputPortList.add(new OutputPort(this,1,false));
        amplitude=new Parameter(this,1,"amplitude",paramValues.getString("Amplitude"));
		bias=new Parameter(this,2,"bias",paramValues.getString("Bias"));
		frequency=new Parameter(this,3,"frequency",paramValues.getString("Frequency"));
		phase=new Parameter(this,4,"phase",paramValues.getString("Phase"));
		parameterList.add(amplitude);
		parameterList.add(bias);
		parameterList.add(frequency);
		parameterList.add(phase);
		outputPortList.get(0).setHeight(amplitude.getHeight());
		outputPortList.get(0).setWidth(amplitude.getWidth());
	}
	public void generateInitCodeM(CodeStructM code) {
		super.generateInitCodeM(code);
		String initCode="";
		initCode+=amplitude.getInitCodeM();
		initCode+=frequency.getInitCodeM();
		initCode+=phase.getInitCodeM();
		initCode+=bias.getInitCodeM();
		code.addInitCode(initCode);
	}
	public void generateOutputCodeM(CodeStructM code) {
		super.generateOutputCodeM(code);
		String outputCode="";
		switch(bias.getDataType()) {
		case REAL:
	    outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"="+bias.getName()+"+"+amplitude.getName()+"*sin("+frequency.getName()+"*(t+offset-"+phase.getName()+"));\n";
		break;
		case MATRIX:
			for(int i=1;i<bias.getHeight()+1;i++) {
				for(int j=1;j<bias.getWidth()+1;j++) {
					 outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"("+i+","+j+")="+bias.getName()+"("+i+","+j+")+"+amplitude.getName()+"("+i+","+j+")*sin("+frequency.getName()+"("+i+","+j+")*(t+offset-"+phase.getName()+"("+i+","+j+")));\n";
				}
			}
			break;
		}
		  code.addOutputCode(outputCode);
	}
	public void generateInitCodeC(CodeStructC code) {
		super.generateInitCodeC(code);
		String initCode="/*Code for initialization of block Sine Wave:("+getBlockId()+")"+getBlockName()+"*/\n";
		initCode+=amplitude.getInitCodeC();
		initCode+=frequency.getInitCodeC();
		initCode+=phase.getInitCodeC();
		initCode+=bias.getInitCodeC();
		code.addInitCode(initCode);
	}
	public void generateOutputCodeC(CodeStructC code) {
		String outputCode="/*Code for output of block Sine Wave:("+getBlockId()+")"+getBlockName()+"*/\n";
		outputCode+="{real_T currentTime = model.time;\n";
		switch(amplitude.getDataType()) {
		case REAL:
			outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"="+bias.getName()+"+"+amplitude.getName()+"*sin("+frequency.getName()+"*(currentTime-"+phase.getName()+"));\n";
			break;
		case MATRIX:
			for(int i=0;i<amplitude.getHeight();i++) {
				for(int j=0;j<amplitude.getWidth();j++) {
			outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"("+i+","+j+")="+bias.getName()+"("+i+","+j+")+"+amplitude.getName()+"("+i+","+j+")"+"*sin("+frequency.getName()+"("+i+","+j+")*(currentTime-"+phase.getName()+"("+i+","+j+")));\n";
		}
			}
			break;
			}
		outputCode+="}\n";
		code.addOutputCode(outputCode);
	}
	 public void updateDimension() throws MatDimException{
	    	if(amplitude.getWidth()!=bias.getWidth()
	    			||amplitude.getWidth()!=frequency.getWidth()
	    			||amplitude.getWidth()!=phase.getWidth()
	    			||amplitude.getHeight()!=bias.getHeight()
	    			||amplitude.getHeight()!=frequency.getHeight()
	    			||amplitude.getHeight()!=phase.getHeight()) {
	    		MatDimException e=new MatDimException("Block "+this.blockName+" input dimensions don't match!All input dimensions should be same!");
				throw(e);
	    	}
	    }
}
