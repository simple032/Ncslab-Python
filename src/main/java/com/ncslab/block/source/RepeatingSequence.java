package com.ncslab.block.source;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;

import com.ncslab.block.data.DataType;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.block.Block;

public class RepeatingSequence extends Block {
	Parameter rep_seq_t;
	Parameter rep_seq_y;

	public RepeatingSequence(JSONObject blockJSON,NCSLabModel model) {
		super(blockJSON, model);
		//����һ�����
		outputPortList.add(new OutputPort(this,1,false));
		rep_seq_t=new Parameter(this,1,"rep_seq_t",paramValues.getString("rep_seq_t"));
		rep_seq_y=new Parameter(this,2,"rep_seq_y",paramValues.getString("rep_seq_y"));
		parameterList.add(rep_seq_t);
		parameterList.add(rep_seq_y);
		outputPortList.get(0).setHeight(1);
		outputPortList.get(0).setWidth(1);
	  }
	//define arrays to save data
	 public void generateArraysCodeC(CodeStructC code) {
		 String arraysCode="/*Define arrays for block RepeatingSeque("+getBlockId()+")"+getBlockName()+"*/\n";
		 arraysCode+="double "+"Block"+getBlockId()+"_savedata["+rep_seq_t.getWidth()+"-1];\n";
		 arraysCode+="double "+"Block"+getBlockId()+"_savedata1["+rep_seq_t.getWidth()+"-1];\n";
		 arraysCode+="double "+"Block"+getBlockId()+"_savedata2=0;\n";
		 code.addArraysCode(arraysCode);
	 }
	public void generateInitCodeC(CodeStructC code) {
		super.generateInitCodeC(code);
		String initCode="/*Code for initialization of block Sine Wave:("+getBlockId()+")"+getBlockName()+"*/\n";
		initCode+=rep_seq_t.getInitCodeC();
		initCode+=rep_seq_y.getInitCodeC();
		for(int i=0;i<rep_seq_t.getWidth()-1;i++) {
			initCode+="Block"+getBlockId()+"_savedata["+i+"]=("+rep_seq_y.getName()+"(0,"+i+"+1)-"+rep_seq_y.getName()+"(0,"+i+"))/("+rep_seq_t.getName()+"(0,"+i+"+1)-"+rep_seq_t.getName()+"(0,"+i+"));\n";
			initCode+="Block"+getBlockId()+"_savedata1["+i+"]=("+rep_seq_t.getName()+"(0,"+i+"+1)-"+rep_seq_t.getName()+"(0,"+i+"));\n";
		}
		code.addInitCode(initCode);
	}
	public void generateOutputCodeC(CodeStructC code) {
		String outputCode="/*Code for output of block RepeatingSequence:("+getBlockId()+")"+getBlockName()+"*/\n";
		outputCode+="{real_T currentTime = model.time;\n";
		outputCode+="Block"+getBlockId()+"_savedata2="+rep_seq_y.getName()+"(0,0);\n";
		outputCode+="int ll=0;\n";
		outputCode+="for(int i="+rep_seq_t.getWidth()+"-1;i>0;i--) {\n";
	    outputCode+="if((int)(currentTime*100)%(int)(("+rep_seq_t.getName()+"(0,"+(rep_seq_t.getWidth()-1)+")-"+rep_seq_t.getName()+"(0,0))*100)<"+rep_seq_t.getName()+"(0,i)*100&&(int)(currentTime*100)%(int)(("+rep_seq_t.getName()+"(0,"+(rep_seq_t.getWidth()-1)+")-"+rep_seq_t.getName()+"(0,0))*100)>="+rep_seq_t.getName()+"(0,i-1)*100){\n";
	    outputCode+="ll=i;\n";
	    outputCode+="break;}}\n";
		outputCode+="for(int i=0;i<ll-1;i++){\n";
		outputCode+="Block"+getBlockId()+"_savedata2+=(Block"+getBlockId()+"_savedata[i])*"+"Block"+getBlockId()+"_savedata1[i];}\n";
		outputCode+="Block"+getBlockId()+"_savedata2+=(Block"+getBlockId()+"_savedata[ll-1])*(currentTime-floor(currentTime/("+rep_seq_t.getName()+"(0,"+(rep_seq_t.getWidth()-1)+")-"+rep_seq_t.getName()+"(0,0)))*("+rep_seq_t.getName()+"(0,"+(rep_seq_t.getWidth()-1)+"))-"+rep_seq_t.getName()+"(0,ll-1));\n";
		outputCode+=this.getOutputPortVariable(0)+"=Block"+getBlockId()+"_savedata2;\n";
		outputCode+="}\n";
		code.addOutputCode(outputCode);
	}
	 public void updateDimension() throws MatDimException{
	    	if(rep_seq_t.getWidth()!=rep_seq_y.getWidth()) {
	    		MatDimException e=new MatDimException("Block "+this.blockName+" input dimensions don't match!All input dimensions should be same!");
				throw(e);
	    	}
	    	if(rep_seq_t.getHeight()!=1||rep_seq_y.getHeight()!=1||rep_seq_t.getWidth()==1||rep_seq_y.getWidth()==1) {
	    		MatDimException e=new MatDimException("Block "+this.blockName+" input dimensions must be 1*n!");
				throw(e);
	    	}
	    	if(rep_seq_t.getDataType()==DataType.REAL||rep_seq_y.getDataType()==DataType.REAL) {
		    		MatDimException e=new MatDimException("Block "+this.blockName+" input dimensions must be 1*n!");
					throw(e);
	    	}
	    }
}
