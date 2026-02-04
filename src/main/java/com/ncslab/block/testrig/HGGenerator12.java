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

public class HGGenerator12 extends Block {
	Parameter cParaXd;
	Parameter cParaXdd;
	Parameter cParaXddd;
	Parameter cParaXq;
	Parameter cParaXqq;
	Parameter cParaXqqq;
	Parameter cParaXl;
	Parameter cParaTd0;
	Parameter cParaTd000;
	Parameter cParaTq0;
	Parameter cParaTq000;
	Parameter cParaRs;
	Parameter cParafn;
	Parameter cParath;
	Parameter cParapha;
	
	
	Parameter cParaXad;
	Parameter cParaXaq;
	Parameter cParaXfl;	
	Parameter cParaXDl;
	Parameter cParaXQ1l;
	Parameter cParaXQ2l;
	Parameter cParaXQ2l1;
	Parameter cParaXQ2l2;
	Parameter cParaomega0;
	Parameter cParaRf;
	Parameter cParaRD;
	Parameter cParaRQ1;
	Parameter cParaRQ2;
	Parameter cParaRQ2v;
	Parameter cParaXadd;
	Parameter cParaXaqq;
	Parameter cParakf;
	Parameter cParakD;
	Parameter cParakQ1;
	Parameter cParakQ22;
	
	InputPort inC1=new InputPort(this,1,"C1");
	InputPort inEfd=new InputPort(this,2,"Efd");
	InputPort inIa1=new InputPort(this,3,"Ia1");
	InputPort inIb1=new InputPort(this,4,"Ib1");
	InputPort inIc1=new InputPort(this,5,"Ic1");
	InputPort inIa2=new InputPort(this,6,"Ia2");
	InputPort inIb2=new InputPort(this,7,"Ib2");
	InputPort inIc2=new InputPort(this,8,"Ic2");
	InputPort inIa3=new InputPort(this,9,"Ia3");
	InputPort inIb3=new InputPort(this,10,"Ib3");
	InputPort inIc3=new InputPort(this,11,"Ic3");
	InputPort inIa4=new InputPort(this,12,"Ia4");
	InputPort inIb4=new InputPort(this,13,"Ib4");
	InputPort inIc4=new InputPort(this,14,"Ic4");
	
	OutputPort outea1=new OutputPort(this,"ea1",1,false);
	OutputPort outeb1=new OutputPort(this,"eb1",2,false);
	OutputPort outec1=new OutputPort(this,"ec1",3,false);
	OutputPort outea2=new OutputPort(this,"ea2",4,false);
	OutputPort outeb2=new OutputPort(this,"eb2",5,false);
	OutputPort outec2=new OutputPort(this,"ec2",6,false);
	OutputPort outea3=new OutputPort(this,"ea3",7,false);
	OutputPort outeb3=new OutputPort(this,"eb3",8,false);
	OutputPort outec3=new OutputPort(this,"ec3",9,false);
	OutputPort outea4=new OutputPort(this,"ea4",10,false);
	OutputPort outeb4=new OutputPort(this,"eb4",11,false);
	OutputPort outec4=new OutputPort(this,"ec4",12,false);
	OutputPort outomega=new OutputPort(this,"omega",13,false);
	
	
	State statepsif=new State(this,1,"psif");
	State statepsiD=new State(this,2,"psiD");
	State statepsiQ1=new State(this,3,"psiQ1");
	State statepsiQ21=new State(this,4,"psiQ21");
	State statepsiQ22=new State(this,5,"psiQ22");
	State statetheta=new State(this,6,"theta");
	State stateomega=new State(this,7,"omega");
	
	public HGGenerator12(JSONObject blockJSON,NCSLabModel model) {
		super(blockJSON,model);
		
		
		inputPortList.add(inC1);
		inputPortList.add(inEfd);
		inputPortList.add(inIa1);
		inputPortList.add(inIb1);
		inputPortList.add(inIc1);
		inputPortList.add(inIa2);
		inputPortList.add(inIb2);
		inputPortList.add(inIc2);
		inputPortList.add(inIa3);
		inputPortList.add(inIb3);
		inputPortList.add(inIc3);
		inputPortList.add(inIa4);
		inputPortList.add(inIb4);
		inputPortList.add(inIc4);
		
		outputPortList.add(outea1);
		outputPortList.add(outeb1);
		outputPortList.add(outec1);
		outputPortList.add(outea2);
		outputPortList.add(outeb2);
		outputPortList.add(outec2);
		outputPortList.add(outea3);
		outputPortList.add(outeb3);
		outputPortList.add(outec3);
		outputPortList.add(outea4);
		outputPortList.add(outeb4);
		outputPortList.add(outec4);
		outputPortList.add(outomega);
		
		
		cParaXd=new Parameter(this,parameterList.size()+1,"Xd",paramValues.getString("Xd"));
		parameterList.add(cParaXd);
		cParaXdd=new Parameter(this,parameterList.size()+1,"Xdd",paramValues.getString("Xdd"));
		parameterList.add(cParaXdd);
		cParaXddd=new Parameter(this,parameterList.size()+1,"Xddd",paramValues.getString("Xddd"));
		parameterList.add(cParaXddd);
		cParaXq=new Parameter(this,parameterList.size()+1,"Xq",paramValues.getString("Xq"));
		parameterList.add(cParaXq);
		cParaXqq=new Parameter(this,parameterList.size()+1,"Xqq",paramValues.getString("Xqq"));
		parameterList.add(cParaXqq);
		cParaXqqq=new Parameter(this,parameterList.size()+1,"Xqqq",paramValues.getString("Xqqq"));
		parameterList.add(cParaXqqq);
		cParaXl=new Parameter(this,parameterList.size()+1,"Xl",paramValues.getString("Xl"));
		parameterList.add(cParaXl);
		cParaTd0=new Parameter(this,parameterList.size()+1,"Td0",paramValues.getString("Td0"));
		parameterList.add(cParaTd0);
		cParaTd000=new Parameter(this,parameterList.size()+1,"Td000",paramValues.getString("Td000"));
		parameterList.add(cParaTd000);
		cParaTq0=new Parameter(this,parameterList.size()+1,"Tq0",paramValues.getString("Tq0"));
		parameterList.add(cParaTq0);
		cParaTq000=new Parameter(this,parameterList.size()+1,"Tq000",paramValues.getString("Tq000"));
		parameterList.add(cParaTq000);
		cParaRs=new Parameter(this,parameterList.size()+1,"Rs",paramValues.getString("Rs"));
		parameterList.add(cParaRs);
		cParafn=new Parameter(this,parameterList.size()+1,"fn",paramValues.getString("fn"));
		parameterList.add(cParafn);
		cParath=new Parameter(this,parameterList.size()+1,"th",paramValues.getString("th"));
		parameterList.add(cParath);
		cParapha=new Parameter(this,parameterList.size()+1,"pha",paramValues.getString("pha"));
		parameterList.add(cParapha);
		
		/*
		cParaXad=new Parameter(this,parameterList.size()+1,"Xad","0");
		parameterList.add(cParaXad);
		cParaXaq=new Parameter(this,parameterList.size()+1,"Xaq","0");
		parameterList.add(cParaXaq);
		cParaXfl=new Parameter(this,parameterList.size()+1,"Xfl","0");
		parameterList.add(cParaXfl);
		cParaXDl=new Parameter(this,parameterList.size()+1,"XDl","0");
		parameterList.add(cParaXDl);
		cParaXQ1l=new Parameter(this,parameterList.size()+1,"XQ1l","0");
		parameterList.add(cParaXQ1l);
		cParaXQ2l=new Parameter(this,parameterList.size()+1,"XQ2l","0");
		parameterList.add(cParaXQ2l);
		cParaXQ2l1=new Parameter(this,parameterList.size()+1,"XQ2l1","0");
		parameterList.add(cParaXQ2l1);
		cParaXQ2l2=new Parameter(this,parameterList.size()+1,"XQ2l2","0");
		parameterList.add(cParaXQ2l2);
		cParaomega0=new Parameter(this,parameterList.size()+1,"omega0","0");
		parameterList.add(cParaomega0);
		cParaRf=new Parameter(this,parameterList.size()+1,"Rf","0");
		parameterList.add(cParaRf);
		cParaRD=new Parameter(this,parameterList.size()+1,"RD","0");
		parameterList.add(cParaRD);		
		cParaRQ1=new Parameter(this,parameterList.size()+1,"RQ1","0");
		parameterList.add(cParaRQ1);
		cParaRQ2=new Parameter(this,parameterList.size()+1,"RQ2","0");
		parameterList.add(cParaRQ2);
		cParaRQ2v=new Parameter(this,parameterList.size()+1,"RQ2v","0");
		parameterList.add(cParaRQ2v);
		cParaXadd=new Parameter(this,parameterList.size()+1,"Xadd","0");
		parameterList.add(cParaXadd);
		cParaXaqq=new Parameter(this,parameterList.size()+1,"Xaqq","0");
		parameterList.add(cParaXaqq);
		cParakf=new Parameter(this,parameterList.size()+1,"kf","0");
		parameterList.add(cParakf);
		cParakD=new Parameter(this,parameterList.size()+1,"kD","0");
		parameterList.add(cParakD);		
		cParakQ1=new Parameter(this,parameterList.size()+1,"kQ1","0");
		parameterList.add(cParakQ1);
		cParakQ22=new Parameter(this,parameterList.size()+1,"kQ22","0");
		parameterList.add(cParakQ22);*/
		
		stateList.add(statepsif);
		stateList.add(statepsiD);
		stateList.add(statepsiQ1);
		stateList.add(statepsiQ21);
		stateList.add(statepsiQ22);
		stateList.add(statetheta);
		stateList.add(stateomega);
	}
	
	
	
	
	public void generateInitCodeC(CodeStructC code) {
		super.generateInitCodeC(code);

        com.ncslab.util.TemplateUtils.populateAllContext(context, this);
        
        String[] keys=context.getKeys();
        for(String key:keys) {
        	System.out.println(key+":\t"+context.get(key));
        }

        String codeStr = TemplateManager.renderTemplate("c/testrig/HGGenerator12/init.vm", context);
        code.addInitCode(codeStr);
	}
	
	//define arrays to save data
	 public void generateArraysCodeC(CodeStructC code) {
		 String arraysCode="/*Define arrays for block Generator:("+getBlockId()+")"+getBlockName()+"*/\n";
		 
		 code.addArraysCode(arraysCode);
	 }
	 
	 public void  generateDerivativeCodeC(CodeStructC code) {
		 com.ncslab.util.TemplateUtils.populateAllContext(context, this);

	     String derivativeCode = TemplateManager.renderTemplate("c/testrig/HGGenerator12/derivative.vm", context);
	     code.addDerivativeCode(derivativeCode);
	 }
	 
	 public void generateOutputCodeC(CodeStructC code) {
		 com.ncslab.util.TemplateUtils.populateAllContext(context, this);
 
	     String codeStr = TemplateManager.renderTemplate("c/testrig/HGGenerator12/output.vm", context);
	     code.addOutputCode(codeStr);
	 }
}