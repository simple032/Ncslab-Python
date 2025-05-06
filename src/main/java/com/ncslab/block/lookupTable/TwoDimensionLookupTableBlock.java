package com.ncslab.block.lookupTable;

import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TwoDimensionLookupTableBlock extends LookupTableBlock{

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
        String initCode="/*Code for init of block 2-D Lookup table:("+getBlockId()+")"+getBlockName()+"*/\n";
        StringBuilder initCodeBuilder = new StringBuilder();
        initCodeBuilder.append("init_interpolation_table(&").append(getTableName())
            .append(",").append(x_dat.length)
            .append(",").append(y_dat.length)
            .append(",").append(1)
            .append(");\n");

        for(int i=0;i<x_dat.length;i++) {
            initCodeBuilder.append("insert_to_x_table(&").append(getTableName())
                .append(",").append(i)
                .append(",").append(x_dat[i]).append(");\n");
        }

        for(int i=0;i<y_dat.length;i++) {
            initCodeBuilder.append("insert_to_y_table(&").append(getTableName())
                .append(",").append(i)
                .append(",").append(y_dat[i]).append(");\n");
        }

        for(int i=0;i<x_dat.length;i++) {
            for(int j=0;j<y_dat.length;j++) {
                initCodeBuilder.append("insert_to_z_table(&").append(getTableName())
                   .append(",").append(i)
                   .append(",").append(j)
                   .append(",").append(z_dat[i][j]).append(");\n");
            }
        }

        initCode += initCodeBuilder.toString();
        code.addInitCode(initCode);
    }

    @Override
    public void generateArraysCodeC(CodeStructC code){
        String arraysCode = "/*Define arrays for block 2-D Lookup table:(" + getBlockId() + ")" + getBlockName() + "*/\n";
        arraysCode += "InterpolationTable " + getTableName() +";\n";
        code.addArraysCode(arraysCode);
    }

    @Override
    public void generateOutputCodeC(CodeStructC code){
        String outputCode="/*Code for output of block 2-D Lookup table:("+getBlockId()+")"+getBlockName()+"*/\n";
        String outputCodeBuilder = outputPortList.get(0).getOutputSignalC().getName() +
            " = " + "bilinear_interpolation(&" + getTableName() +
            ", " + inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName() +
            ", " + inputPortList.get(1).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName() +
            ");\n";
        outputCode += outputCodeBuilder;
        code.addOutputCode(outputCode);
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
