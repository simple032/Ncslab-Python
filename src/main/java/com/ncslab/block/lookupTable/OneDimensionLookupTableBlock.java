package com.ncslab.block.lookupTable;

import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.NCSLabModel;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OneDimensionLookupTableBlock extends LookupTableBlock{

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
        String initCode="/*Code for init of block 1-D Lookup table:("+getBlockId()+")"+getBlockName()+"*/\n";
        StringBuilder initCodeBuilder = new StringBuilder();
        initCodeBuilder.append("init_interpolation_table_1d(&").append(getTableName())
            .append(",").append(x_dat.length)
            .append(",").append(1)
            .append(");\n");
        for(int i=0;i<x_dat.length;i++) {
            initCodeBuilder.append("insert_to_x_table1d(&").append(getTableName())
                .append(",").append(i)
                .append(",").append(x_dat[i])
                .append(");\n");
        }
        for(int i=0;i<y_dat.length;i++) {
            initCodeBuilder.append("insert_to_y_table1d(&").append(getTableName())
                .append(",").append(i)
                .append(",").append(y_dat[i])
                .append(");\n");
        }
        initCode += initCodeBuilder.toString();
        code.addInitCode(initCode);
    }

    @Override
    public void generateArraysCodeC(CodeStructC code){
        String arraysCode = "/*Define arrays for block 1-D Lookup table:(" + getBlockId() + ")" + getBlockName() + "*/\n";
        arraysCode += "InterpolationTable1D " + getTableName() +";\n";
        code.addArraysCode(arraysCode);
    }

    @Override
    public void generateOutputCodeC(CodeStructC code){
        String outputCode="/*Code for output of block 1-D Lookup table:("+getBlockId()+")"+getBlockName()+"*/\n";
        String outputCodeBuilder = outputPortList.get(0).getOutputSignalC().getName() +
            " = " + "linear_interpolation_1d(&" + getTableName() +
            ", " + inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName() +
            ");\n";
        outputCode += outputCodeBuilder;
        code.addOutputCode(outputCode);
    }
}
