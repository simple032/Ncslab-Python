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
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TwoDimensionLookupTableBlock extends LookupTableBlock{

    // === Static Parameter Definitions ===

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
        breakpointsForDimension1 = getParameterByName("breakpointsForDimension1");
        breakpointsForDimension2 = getParameterByName("breakpointsForDimension2");
//        breakpointsForDimension3 = getParameterByName("breakpointsForDimension3");
//        breakpointsForDimension4 = getParameterByName("breakpointsForDimension4");

        table = getParameterByName("table");

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
        // Populate all standard template variables first
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        // Add lookup table specific context
        context.put("tableName", getTableName());

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
