package com.ncslab.block.lookupTable;

import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.NCSLabModel;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OneDimensionLookupTableBlock extends LookupTableBlock{

    // === Static Parameter Definitions ===
    public static final Vector<String> parameterNames = new Vector<>();
    
    // Parameter defaults matching database format
    public static final Map<String, String> PARAMETER_DEFAULTS;
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("BreakpointsForDimension1", "[-4:5]");
        PARAMETER_DEFAULTS.put("Table", "[0:9]");
        PARAMETER_DEFAULTS.put("InterpMethod", "Linear");
        PARAMETER_DEFAULTS.put("ExtrapMethod", "Clip");
        PARAMETER_DEFAULTS.put("SampleTime", "-1");
        PARAMETER_DEFAULTS.put("OutDataTypeStr", "Inherit: Same as first input");
    }

    static {
        // Parameter names for 1-D lookup table
        parameterNames.add("BreakpointsForDimension1");
        parameterNames.add("Table");
        parameterNames.add("InterpMethod");
        parameterNames.add("ExtrapMethod");
        parameterNames.add("SampleTime");
        parameterNames.add("OutDataTypeStr");
    }

    private Parameter breakpointsForDimension1;
//    private Parameter breakpointsForDimension2;
//    private Parameter breakpointsForDimension3;
//    private Parameter breakpointsForDimension4;
    private Parameter table;

    private double[] x_dat = null;
    private double[] y_dat = null;

    public OneDimensionLookupTableBlock(JSONObject blockIn, NCSLabModel model) {
        super(blockIn, model);

        inputPortList.add(new InputPort(this,1));
        outputPortList.add(new OutputPort(this,1,true));

        parseParameters();
    }

    protected void parseParameters(){
        breakpointsForDimension1 = new Parameter(this, 1, "breakpointsForDimension1", paramValues.getString("BreakpointsForDimension1"));
//        breakpointsForDimension2 = new Parameter(this, 1, "breakpointsForDimension2", paramValues.optString("BreakpointsForDimension2"));
//        breakpointsForDimension3 = new Parameter(this, 1, "breakpointsForDimension3", paramValues.optString("BreakpointsForDimension3"));
//        breakpointsForDimension4 = new Parameter(this, 1, "breakpointsForDimension4", paramValues.optString("BreakpointsForDimension4"));

        table = new Parameter(this, 1, "table", paramValues.getString("Table"));

        x_dat = parseMatlabVector(breakpointsForDimension1.getDataString());
        y_dat = parseMatlabVector(table.getDataString());
    }

    @Override
    public void generateInitCodeC(CodeStructC code){
        super.generateInitCodeC(code);
        
        context.put("block", this);
        context.put("tableName", getTableName());
        context.put("xDataLength", x_dat.length);
        context.put("xData", x_dat);
        context.put("yData", y_dat);
        
        String codeStr = com.ncslab.util.TemplateManager.renderTemplate("c/lookupTable/OneDimensionLookupTableBlock/init.vm", context);
        code.addInitCode(codeStr);
    }

    @Override
    public void generateArraysCodeC(CodeStructC code){
        super.generateArraysCodeC(code);
        
        context.put("block", this);
        context.put("tableName", getTableName());
        
        String codeStr = com.ncslab.util.TemplateManager.renderTemplate("c/lookupTable/OneDimensionLookupTableBlock/arrays.vm", context);
        code.addArraysCode(codeStr);
    }

    @Override
    public void generateOutputCodeC(CodeStructC code){
        super.generateOutputCodeC(code);
        
        String inputSignal = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName();
        String outputSignal = outputPortList.get(0).getOutputSignalC().getName();
        
        context.put("block", this);
        context.put("tableName", getTableName());
        context.put("inputSignal", inputSignal);
        context.put("outputSignal", outputSignal);
        
        String codeStr = com.ncslab.util.TemplateManager.renderTemplate("c/lookupTable/OneDimensionLookupTableBlock/output.vm", context);
        code.addOutputCode(codeStr);
    }
}
