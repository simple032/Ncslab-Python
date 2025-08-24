package com.ncslab.block.math;

import com.ncslab.block.data.Data;
import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.math.ProductOfElementsDto;
import com.ncslab.block.Block;
import com.ncslab.block.data.DataType;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.block.io.InputPort;
import com.ncslab.util.TemplateManager;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

public class ProductOfElements extends Block {

    private String seq;
    boolean allDimensions = true;
    boolean multiplication = false;
    private int dimension;

    // === Static Parameter Definitions ===
    
    // Parameter defaults matching database format
    
    
    /**
     * DTO-NATIVE Constructor - Creates ProductOfElements block directly from BlockDto DTO
     */
    public ProductOfElements(ProductOfElementsDto blockDto, NCSLabModel model) {
        super(blockDto, model);
        System.out.println("DTO-NATIVE: ProductOfElements block created successfully - " + blockDto.getBlockName());
    }


    public static final Map<String, String> PARAMETER_DEFAULTS;
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("Inputs", "*");  // Single multiplication input
        PARAMETER_DEFAULTS.put("Multiplication", "Element-wise(.*)");
        PARAMETER_DEFAULTS.put("MultiplyOver", "All dimensions");
        PARAMETER_DEFAULTS.put("ElementsDimension", "1");
        PARAMETER_DEFAULTS.put("SampleTime", "-1");  // Inherited
        PARAMETER_DEFAULTS.put("OutDataTypeStr", "Inherit: Same as input");
        
        // SIMULINK parameter names
    }

    public static final List<String> outputNames = new ArrayList<>();
    public static final List<String> inputNames = new ArrayList<>();

    static {
        // Port names
        outputNames.add("out1");
    }

    public ProductOfElements(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);
        
        // Use name-based parameter access
        Parameter multiplicationParam = getParameterByName("Multiplication");
        this.multiplication = "Matrix(*)".equals(multiplicationParam.getInitString());

        OutputPort output = new OutputPort(this, 1, true);
        output.setDimThrough(false);
        outputPortList.add(output);

        paraseParamValues();
    }

    // Parse parameter values
    public void paraseParamValues() {
        Parameter inputsParam = getParameterByName("Inputs");
        seq = inputsParam.getInitString();

        for (int i = 0; i < seq.length(); i++) {
            inputPortList.add(new InputPort(this, i + 1));
        }

        Parameter multiplyOverParam = getParameterByName("MultiplyOver");
        allDimensions = "All dimensions".equals(multiplyOverParam.getInitString());
        
        Parameter dimensionParam = getParameterByName("ElementsDimension");
        dimension = Integer.parseInt(dimensionParam.getInitString());
    }
}