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

public class Step extends Block{
    Parameter time0;
    Parameter after;
    Parameter before;

    @Getter
    public static final Vector<String> parameterNames = new Vector<>();

    @Getter
    public static final Vector<String> outputNames = new Vector<>();


    static {

        outputNames.add("out1");

        parameterNames.add("time");
        parameterNames.add("after");
        parameterNames.add("before");
    }

	public Step(JSONObject blockJSON,NCSLabModel model) {
		super(blockJSON, model);
		//����һ�����
		outputPortList.add(new OutputPort(this,1,false));
		time0=new Parameter(this,1,"time",paramValues.getString("Time"));
		after=new Parameter(this,2,"after",paramValues.getString("After"));
		before=new Parameter(this,3,"before",paramValues.getString("Before"));
		parameterList.add(time0);
		parameterList.add(after);
		parameterList.add(before);
		outputPortList.get(0).setHeight(time0.getHeight());
		outputPortList.get(0).setWidth(time0.getWidth());
	}

	public void generateInitCodeM(CodeStructM code) {
		super.generateInitCodeM(code);
		String initCode="";
		initCode+=time0.getInitCodeM();
		initCode+=after.getInitCodeM();
		initCode+=before.getInitCodeM();
		code.addInitCode(initCode);
	}
	public void generateOutputCodeM(CodeStructM code) {
		super.generateOutputCodeM(code);
		String outputCode="";
		switch(time0.getDataType()) {
		case REAL:
		outputCode+="if sign(t-"+time0.getName()+"+offset)>=0\n";
        outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"="+after.getName()+";\n";
        outputCode+="else\n";
        outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"="+before.getName()+";\n";
        outputCode+="end\n";
		break;
		case MATRIX:
			for(int i=1;i<time0.getHeight()+1;i++) {
				for(int j=1;j<time0.getWidth()+1;j++) {
					outputCode+="if sign(t-"+time0.getName()+"("+i+","+j+")+offset)>=0\n";
			        outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"("+i+","+j+")="+after.getName()+"("+i+","+j+");\n";
			        outputCode+="else\n";
			        outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"("+i+","+j+")="+before.getName()+"("+i+","+j+");\n";
			        outputCode+="end\n";
				}
			}
			break;
		}
		code.addOutputCode(outputCode);
	}
	public void generateInitCodeC(CodeStructC code) {
		super.generateInitCodeC(code);
		String initCode="/*Code for initialization of block Step:("+getBlockId()+")"+getBlockName()+"*/\n";
		initCode+=time0.getInitCodeC();
		initCode+=after.getInitCodeC();
		initCode+=before.getInitCodeC();
		code.addInitCode(initCode);
	}
	public void generateOutputCodeC(CodeStructC code) {
		String outputCode="/*Code for output of block Step:("+getBlockId()+")"+getBlockName()+"*/\n";
		outputCode+="{real_T currentTime = model.time;\n";
		switch(time0.getDataType()) {
		case REAL:
			outputCode+="if(currentTime<"+time0.getName()+"){\n";
			outputCode+=this.getOutputPortVariable(0)+"="+before.getName()+";}\n";
			outputCode+="else{\n";
			outputCode+=this.getOutputPortVariable(0)+"="+after.getName()+";}\n";
			break;
		case MATRIX:
			for(int i=0;i<time0.getHeight();i++) {
				for(int j=0;j<time0.getWidth();j++) {
			outputCode+="if(currentTime<"+time0.getName()+"("+i+","+j+")){\n";
			outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"("+i+","+j+")="+before.getName()+"("+i+","+j+");}\n";
			outputCode+="else{\n";
			outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"("+i+","+j+")="+after.getName()+"("+i+","+j+");}\n";
		       }
			}
			break;
			}
		outputCode+="}\n";
		code.addOutputCode(outputCode);
	}
    public void updateDimension() throws MatDimException{
    	if(time0.getWidth()!=after.getWidth()
    			||time0.getWidth()!=before.getWidth()
    			||time0.getHeight()!=after.getHeight()
    			||time0.getHeight()!=before.getHeight()) {
    		MatDimException e=new MatDimException("Block "+this.blockName+" input dimensions don't match!All input dimensions should be same!");
			throw(e);
    	}
    }
}
