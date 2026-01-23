package com.ncslab.block.testrig;

import java.io.InputStream;
import java.io.*;

import org.json.JSONObject;

import com.ncslab.block.Block;
import com.ncslab.block.data.DataType;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.block.io.Parameter;
import com.ncslab.block.io.State;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.util.TemplateManager;

public class HGGenerator7 extends Block {
	Parameter cParaOmega0;
	Parameter cParaXd;
	Parameter cParaXq;
	Parameter cParaXdd;
	Parameter cParaXddd;
	Parameter cParaXqqq;
	Parameter cParaX1;
	Parameter cParaTd0;
	Parameter cParaTd000;
	Parameter cParaTq000;
	Parameter cParaH;
	Parameter cParaR;
	
	
	Parameter cParaRQ;
	Parameter cParaRF;
	Parameter cParaRD;
	
	Parameter cParaXl;
	Parameter cParaLd;
	Parameter cParaLq;
	Parameter cParaLAD;
	Parameter cParaLAQ;
	Parameter cParaLQ;
	Parameter cParaLF;
	Parameter cParaLD;
	
	Parameter cParaLMD;
	Parameter cParaLMQ;
	
	InputPort inC1=new InputPort(this,1,"C1");
	InputPort inEfd=new InputPort(this,2,"Efd");
	InputPort inUd=new InputPort(this,3,"Ud");
	InputPort inUq=new InputPort(this,4,"Uq");
	
	OutputPort outId=new OutputPort(this,"Id",1,false);
	OutputPort outIq=new OutputPort(this,"Iq",2,false);
	OutputPort outTheta=new OutputPort(this,"Theta",3,false);
	OutputPort outOmega=new OutputPort(this,"Omega",4,false);
	
	
	State stateTheta=new State(this,1,"Theta");
	State stateOmega=new State(this,2,"Omega");
	State statePsiq=new State(this,3,"Psiq");
	State statePsiQ=new State(this,4,"PsiQ");
	State statePsid=new State(this,5,"Psid");
	State statePsiD=new State(this,6,"PsiD");
	State statePsiF=new State(this,7,"PsiF");
	
	public HGGenerator7(JSONObject blockJSON,NCSLabModel model) {
		super(blockJSON,model);
		
		
		inputPortList.add(inC1);
		inputPortList.add(inEfd);
		inputPortList.add(inUd);
		inputPortList.add(inUq);
		
		outputPortList.add(outId);
		outputPortList.add(outIq);
		outputPortList.add(outTheta);
		outputPortList.add(outOmega);
		
		
		cParaOmega0=new Parameter(this,parameterList.size()+1,"Omega0",paramValues.getString("Omega0"));
		parameterList.add(cParaOmega0);
		cParaXd=new Parameter(this,parameterList.size()+1,"Xd",paramValues.getString("Xd"));
		parameterList.add(cParaXd);
		cParaXq=new Parameter(this,parameterList.size()+1,"Xq",paramValues.getString("Xq"));
		parameterList.add(cParaXq);
		cParaXdd=new Parameter(this,parameterList.size()+1,"Xdd",paramValues.getString("Xdd"));
		parameterList.add(cParaXdd);
		
		cParaXddd=new Parameter(this,parameterList.size()+1,"Xddd",paramValues.getString("Xddd"));
		parameterList.add(cParaXddd);
		cParaXqqq=new Parameter(this,parameterList.size()+1,"Xqqq",paramValues.getString("Xqqq"));
		parameterList.add(cParaXqqq);
		cParaX1=new Parameter(this,parameterList.size()+1,"X1",paramValues.getString("X1"));
		parameterList.add(cParaX1);
		cParaTd0=new Parameter(this,parameterList.size()+1,"Td0",paramValues.getString("Td0"));
		parameterList.add(cParaTd0);
		
		cParaTd000=new Parameter(this,parameterList.size()+1,"Td000",paramValues.getString("Td000"));
		parameterList.add(cParaTd000);
		cParaTq000=new Parameter(this,parameterList.size()+1,"Tq000",paramValues.getString("Tq000"));
		parameterList.add(cParaTq000);
		cParaH=new Parameter(this,parameterList.size()+1,"H",paramValues.getString("H"));
		parameterList.add(cParaH);
		cParaR=new Parameter(this,parameterList.size()+1,"R",paramValues.getString("R"));
		parameterList.add(cParaR);
		
		cParaRQ=new Parameter(this,parameterList.size()+1,"rQ","0");
		parameterList.add(cParaRQ);
		cParaRF=new Parameter(this,parameterList.size()+1,"rF","0");
		parameterList.add(cParaRF);
		cParaRD=new Parameter(this,parameterList.size()+1,"rD","0");
		parameterList.add(cParaRD);
		
		cParaXl=new Parameter(this,parameterList.size()+1,"Xl","0");
		parameterList.add(cParaXl);
		cParaLd=new Parameter(this,parameterList.size()+1,"ld","0");
		parameterList.add(cParaLd);
		cParaLq=new Parameter(this,parameterList.size()+1,"lq","0");
		parameterList.add(cParaLq);
		cParaLAD=new Parameter(this,parameterList.size()+1,"lAD","0");
		parameterList.add(cParaLAD);
		cParaLAQ=new Parameter(this,parameterList.size()+1,"lAQ","0");
		parameterList.add(cParaLAQ);
		cParaLQ=new Parameter(this,parameterList.size()+1,"lQ","0");
		parameterList.add(cParaLQ);
		cParaLF=new Parameter(this,parameterList.size()+1,"lF","0");
		parameterList.add(cParaLF);
		cParaLD=new Parameter(this,parameterList.size()+1,"lD","0");
		parameterList.add(cParaLD);
		
		cParaLMD=new Parameter(this,parameterList.size()+1,"LMD","0");
		parameterList.add(cParaLMD);
		cParaLMQ=new Parameter(this,parameterList.size()+1,"LMQ","0");
		parameterList.add(cParaLMQ);
		
		stateList.add(stateTheta);
		stateList.add(stateOmega);
		stateList.add(statePsiq);
		stateList.add(statePsiQ);
		stateList.add(statePsid);
		stateList.add(statePsiD);
		stateList.add(statePsiF);
	}
	
	
	
	
	public void generateInitCodeC(CodeStructC code) {
		super.generateInitCodeC(code);

        com.ncslab.util.TemplateUtils.populateAllContext(context, this);
        
        String[] keys=context.getKeys();
        for(String key:keys) {
        	System.out.println(key+":\t"+context.get(key));
        }

        String codeStr = TemplateManager.renderTemplate("c/testrig/HGGenerator7/init.vm", context);
        code.addInitCode(codeStr);
	}
	
	//define arrays to save data
	 public void generateArraysCodeC(CodeStructC code) {
		 String arraysCode="/*Define arrays for block Generator:("+getBlockId()+")"+getBlockName()+"*/\n";
		 
		 code.addArraysCode(arraysCode);
	 }
	 
	 public void  generateDerivativeCodeC(CodeStructC code) {
		 com.ncslab.util.TemplateUtils.populateAllContext(context, this);

	     String derivativeCode = TemplateManager.renderTemplate("c/testrig/HGGenerator7/derivative.vm", context);
	     code.addDerivativeCode(derivativeCode);
	 }
	 
	 public void generateOutputCodeC(CodeStructC code) {
		 com.ncslab.util.TemplateUtils.populateAllContext(context, this);
 
	     String codeStr = TemplateManager.renderTemplate("c/testrig/HGGenerator7/output.vm", context);
	     code.addOutputCode(codeStr);
	 }
}