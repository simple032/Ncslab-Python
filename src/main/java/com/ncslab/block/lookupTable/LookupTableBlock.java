package com.ncslab.block.lookupTable;

import com.ncslab.block.Block;
import com.ncslab.ncslablink.NCSLabModel;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

abstract public class LookupTableBlock extends Block {

    // === Static Parameter Definitions ===
    public static final Vector<String> parameterNames = new Vector<>();
    
    // Parameter defaults matching database format
    public static final Map<String, String> PARAMETER_DEFAULTS;
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("InterpMethod", "Linear");
        PARAMETER_DEFAULTS.put("ExtrapMethod", "Clip");
        PARAMETER_DEFAULTS.put("SampleTime", "-1");
        PARAMETER_DEFAULTS.put("OutDataTypeStr", "Inherit: Same as first input");
    }

    static {
        // Common parameter names for lookup tables
        parameterNames.add("InterpMethod");
        parameterNames.add("ExtrapMethod");
        parameterNames.add("SampleTime");
        parameterNames.add("OutDataTypeStr");
    }

    protected LookupTableBlock(JSONObject blockIn, NCSLabModel model) {
        super(blockIn, model);
    }

    protected String getTableName(){
        return "block" + getBlockId() + "_table";
    }

    protected double[] parseMatlabVector(String vecString){
        String regEx = "[' ']+"; // 一个或多个空格
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(vecString);
        JSONArray numArray=new JSONArray(m.replaceAll(",").trim());

        double[] arr = new double[numArray.length()];
        for(int i=0;i<numArray.length();i++) {
            arr[i] = numArray.getDouble(i);
        }
        return arr;
    }

    protected double[][] parseMatlabMatrix(String matrixString) {
        // 去除方括号
        matrixString = matrixString.replace("[", "").replace("]", "");
        // 按分号分割每一行
        String[] rows = matrixString.split(";");

        double[][] matrix = new double[rows.length][];
        for (int i = 0; i < rows.length; i++) {
            String row = "["+rows[i].trim()+"]";
            // 用逗号分割每一行的元素
            matrix[i] = parseMatlabVector(row);
        }
        return matrix;
    }

    abstract protected void parseParameters();
}
