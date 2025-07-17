package com.ncslab.block.lookupTable;

import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;

import org.checkerframework.checker.units.qual.s;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TwoDimensionLookupTableBlock extends LookupTableBlock{

    // === Static Parameter Definitions ===
    public static final Vector<String> parameterNames = new Vector<>();
    
    // Parameter defaults matching database format
    public static final Map<String, String> PARAMETER_DEFAULTS;
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("BreakpointsForDimension1", "[-4:5]");
        PARAMETER_DEFAULTS.put("BreakpointsForDimension2", "[-3:3]");
        PARAMETER_DEFAULTS.put("Table", "[0:9;10:19;20:29;30:39;40:49;50:59;60:69]");
        PARAMETER_DEFAULTS.put("InterpMethod", "Linear");
        PARAMETER_DEFAULTS.put("ExtrapMethod", "Clip");
        PARAMETER_DEFAULTS.put("SampleTime", "-1");
        PARAMETER_DEFAULTS.put("OutDataTypeStr", "Inherit: Same as first input");
    }

    static {
        // Parameter names for 2-D lookup table
        parameterNames.add("BreakpointsForDimension1");
        parameterNames.add("BreakpointsForDimension2");
        parameterNames.add("Table");
        parameterNames.add("InterpMethod");
        parameterNames.add("ExtrapMethod");
        parameterNames.add("SampleTime");
        parameterNames.add("OutDataTypeStr");
    }

    private Parameter breakpointsForDimension1;
    private Parameter breakpointsForDimension2;
//    private Parameter breakpointsForDimension3;
//    private Parameter breakpointsForDimension4;
    private Parameter table;

    private double[] x_dat = null;
    private double[] y_dat = null;
    private double[][] z_dat = null;

    public TwoDimensionLookupTableBlock(JSONObject blockIn, NCSLabModel model) {
        super(blockIn, model);

        inputPortList.add(new InputPort(this,1));
        inputPortList.add(new InputPort(this,2));

        outputPortList.add(new OutputPort(this,1,true));

        parseParameters();
    }
    protected void parseParameters(){
        breakpointsForDimension1 = new Parameter(this, 1, "breakpointsForDimension1", paramValues.getString("BreakpointsForDimension1"));
        breakpointsForDimension2 = new Parameter(this, 1, "breakpointsForDimension2", paramValues.getString("BreakpointsForDimension2"));
//        breakpointsForDimension3 = new Parameter(this, 1, "breakpointsForDimension3", paramValues.optString("BreakpointsForDimension3"));
//        breakpointsForDimension4 = new Parameter(this, 1, "breakpointsForDimension4", paramValues.optString("BreakpointsForDimension4"));

        table = new Parameter(this, 1, "table", paramValues.getString("Table"));

        x_dat = parseMatlabVector(breakpointsForDimension1.getDataString());
        y_dat = parseMatlabVector(breakpointsForDimension2.getDataString());
        z_dat = parseMatlabMatrix(table.getDataString());
    }

    @Override
    public void generateInitCodeC(CodeStructC code){
        super.generateInitCodeC(code);
        
        context.put("block", this);
        context.put("tableName", getTableName());
        context.put("xDataLength", x_dat.length);
        context.put("yDataLength", y_dat.length);
        context.put("xData", x_dat);
        context.put("yData", y_dat);
        context.put("zData", z_dat);
        
        String codeStr = com.ncslab.util.TemplateManager.renderTemplate("c/lookupTable/TwoDimensionLookupTableBlock/init.vm", context);
        code.addInitCode(codeStr);
    }

    @Override
    public void generateArraysCodeC(CodeStructC code){
        super.generateArraysCodeC(code);
        
        context.put("block", this);
        context.put("tableName", getTableName());
        
        String codeStr = com.ncslab.util.TemplateManager.renderTemplate("c/lookupTable/TwoDimensionLookupTableBlock/arrays.vm", context);
        code.addArraysCode(codeStr);
    }

    @Override
    public void generateOutputCodeC(CodeStructC code){
        super.generateOutputCodeC(code);
        
        String inputSignal1 = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName();
        String inputSignal2 = inputPortList.get(1).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName();
        String outputSignal = outputPortList.get(0).getOutputSignalC().getName();
        
        context.put("block", this);
        context.put("tableName", getTableName());
        context.put("inputSignal1", inputSignal1);
        context.put("inputSignal2", inputSignal2);
        context.put("outputSignal", outputSignal);
        
        String codeStr = com.ncslab.util.TemplateManager.renderTemplate("c/lookupTable/TwoDimensionLookupTableBlock/output.vm", context);
        code.addOutputCode(codeStr);
    }

    @Override
    public void checkDimension() throws MatDimException {
        if(inputPortList.get(0).getLinkedLine()==null || inputPortList.get(1).getLinkedLine()==null) {
            throw new MatDimException("Input port of block 2-D Lookup table:("+getBlockId()+")"+getBlockName()+" is not connected");
        }
        if(x_dat.length!=z_dat.length || y_dat.length!=z_dat[0].length) {
            throw new MatDimException("Dimension of table of block 2-D Lookup table:("+getBlockId()+")"+getBlockName()+" is not correct");
        }
    }
}
