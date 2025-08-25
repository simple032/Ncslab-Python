package com.ncslab.block.math;

import com.ncslab.block.math.MathBlock;
import com.ncslab.block.data.Data;
import com.ncslab.block.data.DataType;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;
import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.math.RoundingDto;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import com.ncslab.util.TemplateManager;

public class Rounding extends MathBlock {

    Parameter operator;
    String operatorString;

    // === Static Parameter Definitions ===
    
    // Parameter defaults matching database format
    
    
    /**
     * DTO-NATIVE Constructor - Creates Rounding block directly from RoundingDto DTO
     */
    public Rounding(RoundingDto roundingDto, NCSLabModel model) {
        super(roundingDto, model);
        
        outputPortList.add(new OutputPort(this, 1, true));
        inputPortList.add(new InputPort(this, 1));

        // Get operator parameter and set operator string
        operator = getParameterByName("Operator");
        operatorString = operator.getInitString();

        if ("fix".equals(operatorString)) {
            operatorString = "trunc";
        }
        
        System.out.println("DTO-NATIVE: Rounding block created successfully from RoundingDto - " + roundingDto.getBlockName());
    }


    public static final Map<String, String> PARAMETER_DEFAULTS;
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("Operator", "floor");
        PARAMETER_DEFAULTS.put("SampleTime", "-1");  // Inherited
        PARAMETER_DEFAULTS.put("OutDataTypeStr", "Inherit: Same as input");
        
        // SIMULINK parameter names
    }

    public static final List<String> outputNames = new ArrayList<>();
    public static final List<String> inputNames = new ArrayList<>();

    static {
        // Port names
        outputNames.add("out1");
        inputNames.add("in1");
    }

    public Rounding(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);
        outputPortList.add(new OutputPort(this, 1, true));

        inputPortList.add(new InputPort(this, 1));

        // Use name-based parameter access
        operator = getParameterByName("Operator");
        operatorString = operator.getInitString();

        if ("fix".equals(operatorString)) {
            operatorString = "trunc";
        }
    }
    
    @Override
    public void calculateOutput(double t) {
        // SIMULINK Rounding block: applies rounding function to input
        Data inputData = inputPortList.get(0).getData();
        Data outputData;
        
        if (inputData.getDataType() == DataType.MATRIX) {
            // Matrix input - apply rounding function element-wise
            Jama.Matrix inputMatrix = inputData.getMatrix();
            Jama.Matrix outputMatrix = new Jama.Matrix(inputMatrix.getRowDimension(), inputMatrix.getColumnDimension());
            
            for (int i = 0; i < inputMatrix.getRowDimension(); i++) {
                for (int j = 0; j < inputMatrix.getColumnDimension(); j++) {
                    double value = inputMatrix.get(i, j);
                    double roundedValue = applyRoundingFunction(value, operatorString);
                    outputMatrix.set(i, j, roundedValue);
                }
            }
            outputData = new Data(outputMatrix);
        } else {
            // Scalar input
            double inputValue = inputData.getInitValue();
            double roundedValue = applyRoundingFunction(inputValue, operatorString);
            outputData = new Data(1, 1);
            outputData.setInitValue(roundedValue);
        }
        
        outputPortList.get(0).setData(outputData);
    }
    
    private double applyRoundingFunction(double value, String operator) {
        switch (operator) {
            case "floor":
                return Math.floor(value);
            case "ceil":
                return Math.ceil(value);
            case "round":
                return Math.round(value);
            case "trunc":
            case "fix":
                return value >= 0 ? Math.floor(value) : Math.ceil(value); // Truncate towards zero
            default:
                return Math.floor(value); // Default to floor
        }
    }
}