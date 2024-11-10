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

public class Pulse extends Block{
	    Parameter amplitude;
	    Parameter period;
	    Parameter pulseWidth;
	    Parameter phaseDelay;


    @Getter
    public static final Vector<String> parameterNames = new Vector<>();

    @Getter
    public static final Vector<String> outputNames = new Vector<>();


    static {

        outputNames.add("out1");
        parameterNames.add("amplitude");
        parameterNames.add("period");
        parameterNames.add("pulseWidth");
        parameterNames.add("phaseDelay");


    }
	public Pulse(JSONObject blockJSON,NCSLabModel model) {
		super(blockJSON, model);
		//����һ�����
		outputPortList.add(new OutputPort(this,1,false));
        amplitude=new Parameter(this,1,"amplitude",paramValues.getString("Amplitude"));
		period=new Parameter(this,2,"period",paramValues.getString("Period"));
		pulseWidth=new Parameter(this,3,"pulseWidth",paramValues.getString("PulseWidth"));
		phaseDelay=new Parameter(this,4,"phaseDelay",paramValues.getString("PhaseDelay"));
		parameterList.add(amplitude);
		parameterList.add(period);
		parameterList.add(pulseWidth);
		parameterList.add(phaseDelay);
		outputPortList.get(0).setHeight(amplitude.getHeight());
		outputPortList.get(0).setWidth(amplitude.getWidth());
	}
	public void generateInitCodeM(CodeStructM code) {
		super.generateInitCodeM(code);
		String initCode="";
		initCode+=amplitude.getInitCodeM();
		initCode+=period.getInitCodeM();
		initCode+=pulseWidth.getInitCodeM();
		initCode+=phaseDelay.getInitCodeM();
		code.addInitCode(initCode);
	}
	public void generateOutputCodeM(CodeStructM code) {
		super.generateOutputCodeM(code);
		String outputCode="";
		switch(amplitude.getDataType()) {
		case REAL:
	    outputCode+="if (t+offset)<"+phaseDelay.getName()+"\n";
	    outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"=0;\n";
	    outputCode+="elseif mod(t+offset-"+phaseDelay.getName()+","+period.getName()+")<"+period.getName()+"*"+pulseWidth.getName()+"/100\n";
	    outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"="+amplitude.getName()+";\n";
	    outputCode+="else\n";
	    outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"=0;\n";
	    outputCode+="end\n";
	    break;
		case MATRIX:
			for(int i=1;i<amplitude.getHeight()+1;i++) {
				for(int j=1;j<amplitude.getWidth()+1;j++) {
					  outputCode+="if (t+offset)<"+phaseDelay.getName()+"("+i+","+j+")\n";
					    outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"("+i+","+j+")=0;\n";
					    outputCode+="elseif mod(t+offset-"+phaseDelay.getName()+"("+i+","+j+"),"+period.getName()+"("+i+","+j+"))<"+period.getName()+"("+i+","+j+")*"+pulseWidth.getName()+"("+i+","+j+")/100\n";
					    outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"("+i+","+j+")="+amplitude.getName()+"("+i+","+j+");\n";
					    outputCode+="else\n";
					    outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"("+i+","+j+")=0;\n";
					    outputCode+="end\n";
				}
			}
			break;
	    }
		code.addOutputCode(outputCode);
	}
	public void generateInitCodeC(CodeStructC code) {
		super.generateInitCodeC(code);
		String initCode="/*Code for initialization of block Pulse:("+getBlockId()+")"+getBlockName()+"*/\n";
		initCode+=amplitude.getInitCodeC();
		initCode+=period.getInitCodeC();
		initCode+=pulseWidth.getInitCodeC();
		initCode+=phaseDelay.getInitCodeC();
		code.addInitCode(initCode);
	}
	public void generateOutputCodeC(CodeStructC code) {
		String outputCode="/*Code for output of block Pulse:("+getBlockId()+")"+getBlockName()+"*/\n";
		outputCode+="{real_T currentTime = model.time;\n";
		switch(amplitude.getDataType()) {
		case REAL:
			    outputCode+="if(currentTime<"+phaseDelay.getName()+"){\n";
			    outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"=0;}\n";
			    outputCode+="else if(((int)((currentTime-"+phaseDelay.getName()+")*100)%(int)("+period.getName()+"*100))<"+period.getName()+"*"+pulseWidth.getName()+"){\n";
			    outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"="+amplitude.getName()+";}\n";
			    outputCode+="else{\n";
			    outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"=0;}\n";
			break;
		case MATRIX:
			for(int i=0;i<amplitude.getHeight();i++) {
				for(int j=0;j<amplitude.getWidth();j++) {
					outputCode+="if(currentTime<"+phaseDelay.getName()+"("+i+","+j+")){\n";
				    outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"("+i+","+j+")=0;}\n";
				    outputCode+="else if(((int)((currentTime-"+phaseDelay.getName()+"("+i+","+j+"))*100)%(int)("+period.getName()+"("+i+","+j+")*100))<"+period.getName()+"("+i+","+j+")*"+pulseWidth.getName()+"("+i+","+j+")){\n";
				    outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"("+i+","+j+")="+amplitude.getName()+"("+i+","+j+");}\n";
				    outputCode+="else{\n";
				    outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"("+i+","+j+")=0;}\n";
		          }
			   }
			break;
			}
		outputCode+="}\n";
		code.addOutputCode(outputCode);
	}
	 public void updateDimension() throws MatDimException{
	    	if(amplitude.getWidth()!=period.getWidth()
	    			||amplitude.getWidth()!=pulseWidth.getWidth()
	    			||amplitude.getWidth()!=phaseDelay.getWidth()
	    			||amplitude.getHeight()!=period.getHeight()
	    			||amplitude.getHeight()!=pulseWidth.getHeight()
	    			||amplitude.getHeight()!=phaseDelay.getHeight()) {
	    		MatDimException e=new MatDimException("Block "+this.blockName+" input dimensions don't match!All input dimensions should be same!");
				throw(e);
	    	}
	    }
}
