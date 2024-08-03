package block.powerSystem;

import block.Block;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;
import block.Block;
import block.data.DataType;
import block.io.OutputPort;
import block.io.Parameter;
import code.c.CodeStructC;
import code.m.CodeStructM;
import block.io.InputPort;
import block.io.OutputSignal;
import ncslablink.MatDimException;
import ncslablink.NCSLabModel;
public class pll3ph extends Block{
	private double[] initialInputs;
	private double[] regularGains;
	
	Parameter MinimumFreq;
	Parameter timeDerivative;
	Parameter maxFrequency;
	Parameter filterFrequency;
	Parameter sampleTime;
	
	public pll3ph(JSONObject blockIn, NCSLabModel model) {
		super(blockIn, model);

		parseVector();
		
		//3个输入，2个输出
		inputPortList.add(new InputPort(this,1));
		inputPortList.add(new InputPort(this,2));
		inputPortList.add(new InputPort(this,3));
		outputPortList.add(new OutputPort(this,1,true));
		outputPortList.add(new OutputPort(this,2,true));
		MinimumFreq=new Parameter(this,parameterList.size()+1,"MinimumFreq",paramValues.getString("MinimumFreq"));
		parameterList.add(MinimumFreq);
		timeDerivative=new Parameter(this,parameterList.size()+1,"timeDerivative",paramValues.getString("timeDerivative"));
		parameterList.add(timeDerivative);
		maxFrequency=new Parameter(this,parameterList.size()+1,"maxFrequency",paramValues.getString("maxFrequency"));
		parameterList.add(maxFrequency);
		filterFrequency=new Parameter(this,parameterList.size()+1,"filterFrequency",paramValues.getString("filterFrequency"));
		parameterList.add(filterFrequency);
		sampleTime=new Parameter(this,parameterList.size()+1,"sampleTime",paramValues.getString("sampleTime"));
		parameterList.add(sampleTime);
	}
	
	public void generateInitCodeC(CodeStructC code) {
		super.generateInitCodeC(code);
		String initCode="/*Code for initialization of block PLL (3ph):("+getBlockId()+")"+getBlockName()+"*/\n";
		code.addInitCode(initCode);
	}
	
	
	
	
	private void parseVector() {
		String initialInputsStr=paramValues.getString("initialInputs");
		String regularGainsStr=paramValues.getString("regularGains");
		
		//System.out.println(numStr+denStr);
		
		String regEx = "[' ']+"; 
		Pattern p = Pattern.compile(regEx);  
		Matcher m = p.matcher(initialInputsStr);
		JSONArray initialInputsArray=new JSONArray(m.replaceAll(",").trim());
		
		m=p.matcher(regularGainsStr);
		JSONArray regularGainsArray=new JSONArray(m.replaceAll(",").trim());
		
		initialInputs=new double[initialInputsArray.length()];
		for(int i=0;i<initialInputsArray.length();i++) {
			initialInputs[i]=initialInputsArray.getDouble(i);
		}
		
		regularGains=new double[regularGainsArray.length()];
		for(int i=0;i<regularGainsArray.length();i++) {
			regularGains[i]=regularGainsArray.getDouble(i);
		}
	}
}
