package com.ncslab.circuit2.block.element;

import org.json.JSONObject;

import com.ncslab.circuit2.block.CircuitBlock;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.block.data.Data;
import com.ncslab.circuit2.block.baseelement.*;

public class Capacitor extends CircuitBlockSingle {
	private String cString;
	private double cValue;
	public Capacitor(int id,JSONObject blockJSON,NCSLabModel model) {
		super(id,blockJSON,model);
		//System.out.println(blockJSON);
		cString=Data.parseExpression(paramValues.getString("c"));
		cValue=Double.parseDouble(cString);
	}
	
	public Capacitor(JSONObject blockJSON,NCSLabModel model) {
		super(0,blockJSON,model);
		//System.out.println(blockJSON);
		cString=Data.parseExpression(paramValues.getString("c"));
		cValue=Double.parseDouble(cString);
	}
	
	public String getRString() {
		if (Double.isNaN(cValue) || Double.isInfinite(cValue)) {
			return "(" + this.getSimpleTime() + "/2.0/(" + Data.cxxScalarMacroForNonFiniteDouble(cValue) + "))";
		}
		String cTok = runtimeParamExpression("c", Data.evaluatedJavaScalarToCxxToken(this.cString));
		return "(" + this.getSimpleTime() + "/2.0/(" + cTok + "))";
	}
	
	public double getRValue() {
		double sampleTime=this.getSampleTimeValue();
		return this.getSampleTimeValue()/2.0/cValue;
	}
	
	public String getHisString() {
		String hisString;
		hisString="EBlock"+this.getBlockId()+"_His";
		return hisString;
	}
	
	public String getHisUpdateString() {
		String hisUpdateString="";
		
		String hisString=getHisString();
		
		String lv=this.getCurcuitPortList().get(0).getCircuitNode().getNodeString();
		String rv=this.getCurcuitPortList().get(1).getCircuitNode().getNodeString();
		
		String cTok = (Double.isNaN(cValue) || Double.isInfinite(cValue))
				? Data.cxxScalarMacroForNonFiniteDouble(cValue)
				: runtimeParamExpression("c", Data.evaluatedJavaScalarToCxxToken(this.cString));
		hisUpdateString+=hisString+"=-"+hisString+"-(4.0*"+cTok+")/("+this.getSimpleTime()+")*("+lv+"-"+rv+")";
		
		return hisUpdateString;
	}

	private String runtimeParamExpression(String paramName, String defaultToken) {
		String key = getBlockUUID();
		if (key == null || key.trim().isEmpty() || "null".equalsIgnoreCase(key.trim())) {
			key = getBlockPath() + "/" + getBlockName();
		}
		return "ncslab_runtime_param(\"" + key.replace("\\", "\\\\").replace("\"", "\\\"")
				+ "." + paramName + "\", " + defaultToken + ")";
	}
	
	public void updateHis() {
		double lv=this.getCurcuitPortList().get(0).getCircuitNode().getVoltage();
		double rv=this.getCurcuitPortList().get(1).getCircuitNode().getVoltage();
		
		hisValue=-hisValue-4.0*cValue/this.getSampleTimeValue()*(lv-rv);
	}
	
	public String getCurrentCode() {
		String code=super.getCurrentCode();
		
		String v0=this.getCurcuitPortList().get(0).getCircuitNode().getNodeString();
		String v1=this.getCurcuitPortList().get(1).getCircuitNode().getNodeString();
		
		code+=this.getCurrentString()+"=("+v0+"-"+v1+")/"+this.getRString()+"+"+this.getHisString()+";\n";
		return code;
	}
	
	public String generateHisStringCode() {
		return "REAL EBlock"+this.getBlockId()+"_His=0;\n";
	}
}
