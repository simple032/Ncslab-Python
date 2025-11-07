package com.ncslab.block.powerSystem;

import com.ncslab.block.Block;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.Getter;
import org.json.JSONArray;
import org.json.JSONObject;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.powerSystem.PLL3phDto;
import com.ncslab.block.Block;
import com.ncslab.block.data.DataType;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;
public class pll3ph extends Block{
	private double[] initialInputs;
	private double[] regularGains;

	Parameter MinimumFreq;
	Parameter timeDerivative;
	Parameter maxFrequency;
	Parameter filterFrequency;
	Parameter sampleTime;

    
    
    /**
     * DTO-NATIVE Constructor - Creates pll3ph block directly from BlockDto DTO
     */
    public pll3ph(BlockDto blockDto, NCSLabModel model) {
        super(blockDto, model);
        System.out.println("DTO-NATIVE: pll3ph block created successfully - " + blockDto.getBlockName());
    }


    public static final Map<String, String> PARAMETER_DEFAULTS;
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("MinimumFreq", "50");
        PARAMETER_DEFAULTS.put("timeDerivative", "1");
        PARAMETER_DEFAULTS.put("maxFrequency", "100");
        PARAMETER_DEFAULTS.put("filterFrequency", "10");
        PARAMETER_DEFAULTS.put("sampleTime", "0.001");
        PARAMETER_DEFAULTS.put("initialInputs", "[0 0]");
        PARAMETER_DEFAULTS.put("regularGains", "[1 1]");
    }

    public static final List<String> outputNames = new ArrayList<>();
    public static final List<String> inputNames = new ArrayList<>();

    static {

        outputNames.add("out1");
        outputNames.add("out2");
        inputNames.add("in1");
        inputNames.add("in2");
        inputNames.add("in3");
    }

	public pll3ph(JSONObject blockIn, NCSLabModel model) {
		super(blockIn, model);

		parseVector();

		//3个输入，2个输出
		inputPortList.add(new InputPort(this,1));
		inputPortList.add(new InputPort(this,2));
		inputPortList.add(new InputPort(this,3));
		outputPortList.add(new OutputPort(this,1,true));
		outputPortList.add(new OutputPort(this,2,true));
		MinimumFreq=getParameterByName("MinimumFreq");
		timeDerivative=getParameterByName("timeDerivative");
		maxFrequency=getParameterByName("maxFrequency");
		filterFrequency=getParameterByName("filterFrequency");
		sampleTime=getParameterByName("sampleTime");
	}

	public void generateInitCodeC(CodeStructC code) {
		super.generateInitCodeC(code);
		
		context.put("block", this);
		
		String codeStr = com.ncslab.util.TemplateManager.renderTemplate("c/powerSystem/pll3ph/init.vm", context);
		code.addInitCode(codeStr);
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
	
	public void generateOutputCodeC(CodeStructC code) {
		super.generateOutputCodeC(code);
		
		context.put("block", this);
		context.put("MinimumFreq", MinimumFreq.getName());
		context.put("sampleTime", sampleTime.getName());
		context.put("regularGains", regularGains);
		
		String codeStr = com.ncslab.util.TemplateManager.renderTemplate("c/powerSystem/pll3ph/output.vm", context);
		code.addOutputCode(codeStr);
	}
}
