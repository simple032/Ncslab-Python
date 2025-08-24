package com.ncslab.block.math;

import com.ncslab.block.Block;
import com.ncslab.block.data.Data;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import com.ncslab.util.TemplateManager;
import com.ncslab.ncslablink.NCSLabModel;
import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.dto.core.BlockDto;

public class Matrix extends Block {

    public double elements[][];
    public int row;
    public int column;
    private boolean scalar = false;

    // === Static Parameter Definitions ===
    
    // Parameter defaults matching database format
    
    
    /**
     * DTO-NATIVE Constructor - Creates Matrix block directly from BlockDto DTO
     */
    


    public static final Map<String, String> PARAMETER_DEFAULTS;
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("MatrixValue", "[1]");  // Default 1x1 matrix
        PARAMETER_DEFAULTS.put("SampleTime", "-1");  // Inherited
        PARAMETER_DEFAULTS.put("OutDataTypeStr", "double");
        
        // SIMULINK parameter names
    }

    public static final List<String> outputNames = new ArrayList<>();
    
    public static final List<String> inputNames = new ArrayList<>();

    static {
        // Port names
        outputNames.add("out1");
        // No input ports for matrix block
    }

    public Matrix(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);
        
        // Initialize matrix from parameter
        Parameter matrixParam = getParameterByName("MatrixValue");
        if (matrixParam != null) {
            initialize(matrixParam.getInitString());
        } else {
            initialize("[1]"); // Default fallback
        }
        
        // Add output port
        outputPortList.add(new OutputPort(this, 1, false));
    }

    public void initialize(int row, int column) {
        this.row = row;
        this.column = column;
        this.elements = new double[row][column];
    }

    // Convert MATLAB-string to matrix
    public void initialize(String str) {
        if ("".equals(str)) {
            this.row = 0;
            this.column = 0;
            this.elements = null;
        } else {
            try {
                this.row = 1;
                this.column = 1;
                double value = Double.parseDouble(str);
                this.elements = new double[1][];
                this.elements[0] = new double[1];
                this.elements[0][0] = value;
                this.scalar = true;
            } catch (NumberFormatException nfe) {
                String newstr = str.replaceAll(" ", "").trim();
                newstr = newstr.substring(1, newstr.length() - 1);
                String[] strs = newstr.split(";");
                this.row = strs.length;
                elements = new double[this.row][];
                for (int i = 0; i < elements.length; i++) {
                    String[] substrs = strs[i].split(",");
                    if (i == 0) {
                        this.column = substrs.length;
                    } else if (this.column != substrs.length) {
                        throw new IllegalArgumentException("Matrix rows must have same length");
                    }
                    elements[i] = new double[this.column];
                    for (int j = 0; j < substrs.length; j++) {
                        elements[i][j] = Double.parseDouble(substrs[j]);
                    }
                }
                this.scalar = this.row * this.column == 1;
            }
        }
    }
}