package com.ncslab.circuit2.block.element;

import org.json.JSONObject;

import com.greenpineyu.fel.parser.FelParser.integerLiteral_return;

import com.ncslab.block.io.Parameter;
import com.ncslab.circuit2.block.baseelement.CircuitBlockSingle;
import com.ncslab.ncslablink.NCSLabModel;

public class SeriesRLCBranch extends CircuitBlockSingle {
	
	private String typeString;
	private String rString;
	private String lString;
	private String cString;
	private String hisString="";
	private String cHisString="";
	private String lHisString="";
	private String iHisString="";
	private String gString;
	
	private com.ncslab.block.io.Parameter r;
	private com.ncslab.block.io.Parameter l;
	private com.ncslab.block.io.Parameter c;
	public SeriesRLCBranch(int id, JSONObject blockJSON, NCSLabModel model) {
		super(id, blockJSON, model);
		// TODO Auto-generated constructor stub
		typeString = paramValues.getString("Branchtype");
		
		r=new Parameter(this,1,"R",paramValues.getString("R"));
		l=new Parameter(this,1,"L",paramValues.getString("L"));
		c=new Parameter(this,1,"C",paramValues.getString("C"));
		
		
		
		
		rString = paramValues.getString("R");
		lString = paramValues.getString("L");
		cString = paramValues.getString("C");
		
		
		if(r.getData().getInitValue()<=0) {
			rString = "1.0";
		}
		if(l.getData().getInitValue()<=0) {
			lString = "1e-6";
		}
		if(c.getData().getInitValue()<=0) {
			cString = "1e-6";
		}
		
		hisString="EBlock"+this.getBlockId()+"_His";
		switch (typeString) {
		case "RL":
			gString = "(1.0/("+this.rString+")+2.0*("+this.lString+")/"+this.getSimpleTime()+")";
			lHisString = "EBlock"+this.getBlockId()+"_lHis";
			iHisString = "EBlock"+this.getBlockId()+"_iHis";
			break;
		case "RC":
			gString = "(1.0/("+this.rString+")+"+this.getSimpleTime()+"/2.0/("+this.cString+"))";
			cHisString = "EBlock"+this.getBlockId()+"_cHis";
			iHisString = "EBlock"+this.getBlockId()+"_iHis";
			break;
		case "R":
			gString = "(1.0/("+this.rString+"))";
			hisString = "0.0";
			break;
		case "L":
			gString = "(2.0*("+this.lString+")/"+this.getSimpleTime()+")";
			break;
		case "C":
			gString = "("+this.getSimpleTime()+"/2.0/("+this.cString+"))";
			break;
		case "LC":
			gString = "(2.0*("+this.lString+")/"+this.getSimpleTime()+"+"+this.getSimpleTime()+"/2.0/("+this.cString+"))";
			lHisString = "EBlock"+this.getBlockId()+"_lHis";
			iHisString = "EBlock"+this.getBlockId()+"_iHis";
			break;
		case "RLC":
			gString = "(1.0/("+this.rString+")+2.0*("+this.lString+")/"+this.getSimpleTime()+"+"+this.getSimpleTime()+"/2.0/("+this.cString+"))";
			lHisString = "EBlock"+this.getBlockId()+"_lHis";
			iHisString = "EBlock"+this.getBlockId()+"_iHis";
			break;
		default:
			break;
		}
		
	}

	public String getRString() {
		return gString;
	}
	
	public String getHisString() {
		//String hisString;
		//hisString="EBlock"+this.getBlockId()+"_His";
		return hisString;
	}
	
	public String getHisUpdateString() {
		String hisUpdateString="";
		
		String lv=this.getCurcuitPortList().get(0).getCircuitNode().getNodeString();
		String rv=this.getCurcuitPortList().get(1).getCircuitNode().getNodeString();
		switch (typeString) {
		case "R":
			break;
		case "L":
			hisUpdateString+=this.hisString+"=("+this.hisString+"+"+this.getSimpleTime()+"/("+this.lString+")*("+lv+"-"+rv+"))";
			break;
		case "C":
			hisUpdateString+=this.hisString+"=-"+this.hisString+"-(4.0*"+this.cString+")/("+this.getSimpleTime()+")*("+lv+"-"+rv+")";
			break;
		case "RL":
			hisUpdateString+=this.lHisString+"=(2.0*"+this.lString+"/"+this.getSimpleTime()+"*("+this.getCurrentString()+"-"+this.iHisString+")-"+this.lHisString+");\n";
			hisUpdateString+=this.iHisString+"="+this.getCurrentString()+";\n";
			hisUpdateString+=this.hisString+"=("+this.hisString+"+(2.0*"+this.lHisString+"/"+this.gString+"));\n";
			break;
		case "RC":
			hisUpdateString+=this.cHisString+"=("+this.getSimpleTime()+"/2.0/"+this.cString+"*("+this.getCurrentString()+"+"+this.iHisString+")+"+this.cHisString+");\n";
			hisUpdateString+=this.iHisString+"="+this.getCurrentString()+";\n";
			hisUpdateString+=this.hisString+"=(-1.0*"+this.hisString+"+(-2.0*"+this.cHisString+"/"+this.gString+"));\n";
			break;
			//RLC,LC
		default: 
			hisUpdateString+=this.lHisString+"=(2.0*"+this.lString+"/"+this.getSimpleTime()+"*("+this.getCurrentString()+"-"+this.iHisString+")-"+this.lHisString+");\n";
			hisUpdateString+=this.iHisString+"="+this.getCurrentString()+";\n";
			hisUpdateString+=this.hisString+"=("+this.hisString+"+(2.0*"+lHisString+"-"+this.getSimpleTime()+"*"+this.iHisString+"/"+this.cString+")/"+this.gString+");\n";
			break;
		}
		
		return hisUpdateString;
	}
	
	public String getCurrentCode() {
		String code=super.getCurrentCode();
		
		String v0=this.getCurcuitPortList().get(0).getCircuitNode().getNodeString();
		String v1=this.getCurcuitPortList().get(1).getCircuitNode().getNodeString();
		
		code+=this.getCurrentString()+"=("+v0+"-"+v1+")/"+this.getRString()+"+"+this.getHisString()+";\n";
		return code;
	}
	
	public String generateHisStringCode() {
		String circuitDefineCode="";
		switch (typeString) {
		case "R":
			break;
		case "L":
			circuitDefineCode+="REAL EBlock"+this.getBlockId()+"_His=0;\n";
			break;
		case "C":
			circuitDefineCode+="REAL EBlock"+this.getBlockId()+"_His=0;\n";
			break;
		case "RC":
			circuitDefineCode+="REAL EBlock"+this.getBlockId()+"_cHis=0;\n";
			circuitDefineCode+="REAL EBlock"+this.getBlockId()+"_iHis=0;\n";
			circuitDefineCode+="REAL EBlock"+this.getBlockId()+"_His=0;\n";
			break;
		default:
			circuitDefineCode+="REAL EBlock"+this.getBlockId()+"_lHis=0;\n";
			circuitDefineCode+="REAL EBlock"+this.getBlockId()+"_iHis=0;\n";
			circuitDefineCode+="REAL EBlock"+this.getBlockId()+"_His=0;\n";
			break;
		}
		return circuitDefineCode;
	}
}
