package com.ncslab.block.logicAndBit;

import com.ncslab.block.data.Data;
import com.ncslab.block.data.DataType;
import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.logic.IntervalTestDto;

import com.ncslab.block.Block;
import Jama.Matrix;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.BlockCreationException;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;

import java.util.Objects;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import com.ncslab.util.TemplateManager;

/**
 * IntervalTest block with SIMULINK-compatible parameters and type-safe constructors.
 * 
 * SIMULINK Parameters:
 * - UpperLimit: Upper bound of the interval
 * - LowerLimit: Lower bound of the interval
 * - SampleTime: Sample time for discrete operation (-1 for inherited, 0 for continuous)
 * - OutDataTypeStr: Output data type specification
 * - SaturateOnIntegerOverflow: Handle integer overflow
 */
public class IntervalTest extends Block {
    // Legacy fields for backward compatibility
    Parameter upLimit;
    Parameter lowLimit;

    // === SIMULINK-Compatible Parameters ===
    private final Parameter upperLimit;
    private final Parameter lowerLimit;
    private final Parameter sampleTime;
    private final Parameter outDataType;
    private final Parameter saturateOnIntegerOverflow;

    // === Static Parameter Definitions ===

    public static final List<String> outputNames = new ArrayList<>();
    public static final List<String> inputNames = new ArrayList<>();

    public static final Map<String, String> PARAMETER_DEFAULTS = new HashMap<>();

    static {
        // SIMULINK parameter names
        
        // Port names
        outputNames.add("out1");
        inputNames.add("in1");

        // Parameter defaults
        PARAMETER_DEFAULTS.put("UpperLimit", "1");
        PARAMETER_DEFAULTS.put("LowerLimit", "-1");
        PARAMETER_DEFAULTS.put("SampleTime", "-1");
        PARAMETER_DEFAULTS.put("OutDataTypeStr", "Inherit: Logical (see Configuration Parameters: Optimization)");
        PARAMETER_DEFAULTS.put("SaturateOnIntegerOverflow", "off");
    }

    // === Private Constructor with Typed Parameters ===
    private IntervalTest(Parameter upperLimit, Parameter lowerLimit, Parameter sampleTime,
                        Parameter outDataType, Parameter saturateOnIntegerOverflow,
                        String blockName, String blockPath, String blockUUID, NCSLabModel model) {
        super(createBlockIdentity(blockName, blockPath, blockUUID), model);
        
        // Assign parameters
        this.upperLimit = Objects.requireNonNull(upperLimit, "Upper limit parameter cannot be null");
        this.lowerLimit = Objects.requireNonNull(lowerLimit, "Lower limit parameter cannot be null");
        this.sampleTime = Objects.requireNonNull(sampleTime, "Sample time parameter cannot be null");
        this.outDataType = Objects.requireNonNull(outDataType, "Output data type parameter cannot be null");
        this.saturateOnIntegerOverflow = Objects.requireNonNull(saturateOnIntegerOverflow, "Saturate parameter cannot be null");
        // Legacy field mapping for backward compatibility
        this.upLimit = this.upperLimit;
        this.lowLimit = this.lowerLimit;
        
        // Create ports
        inputPortList.add(new InputPort(this, 1));
        outputPortList.add(new OutputPort(this, 1, true));
    }
    
    // === Legacy Constructor (Deprecated) ===
    @Deprecated
    public IntervalTest(JSONObject blockIn, NCSLabModel model) {
        super(blockIn, model);

        // Create legacy parameters for backward compatibility
        this.lowerLimit = new Parameter(this, 1, "LowerLimit", paramValues.getString("lowlimit"));
        this.upperLimit = new Parameter(this, 2, "UpperLimit", paramValues.getString("uplimit"));
        this.sampleTime = new Parameter(this, 3, "SampleTime", "-1"); // -1 for inherited
        this.outDataType = new Parameter(this, 4, "OutDataTypeStr", "Inherit: Logical (see Configuration Parameters: Optimization)");
        this.saturateOnIntegerOverflow = new Parameter(this, 5, "SaturateOnIntegerOverflow", "off");
        
        // Add all parameters to parameter list

        // Legacy field mapping for backward compatibility
        this.lowLimit = this.lowerLimit;
        this.upLimit = this.upperLimit;

        // Create ports
        inputPortList.add(new InputPort(this, 1));
        outputPortList.add(new OutputPort(this, 1, true));
    }    /**
     * DTO-NATIVE Constructor - Creates IntervalTest block directly from BlockDto DTO
     */
    public IntervalTest(IntervalTestDto blockDto, NCSLabModel model) {
        super(blockDto, model);

        // Initialize final parameters from DTO
        this.upperLimit = new Parameter(this, 1, "Upperlimit", "0");
        this.lowerLimit = new Parameter(this, 2, "Lowerlimit", "0");
        this.sampleTime = new Parameter(this, 3, "SampleTime", "-1");
        this.outDataType = new Parameter(this, 4, "OutDataTypeStr", "Inherit: Logical (see Configuration Parameters: Optimization)");
        this.saturateOnIntegerOverflow = new Parameter(this, 5, "SaturateOnIntegerOverflow", "off");

        // Initialize ports
        initializePorts();

        System.out.println("DTO-NATIVE: " + getClass().getSimpleName() + " block created successfully - " + blockDto.getBlockName());
    }    
    private void initializePorts() {
        inputPortList.add(new InputPort(this, 1));
        outputPortList.add(new OutputPort(this, 1, true));
    }
    
    // === Static Factory Method for JSON Deserialization ===
    public static IntervalTest fromJSON(JSONObject blockJSON, NCSLabModel model) {
        try {
            String blockName = requireNonEmptyString(blockJSON, "blockName");
            String blockPath = requireNonEmptyString(blockJSON, "blockPath");
            String blockUUID = blockJSON.optString("blockUUID", "null");
            JSONObject paramValues = blockJSON.optJSONObject("paramValues");
            
            if (paramValues == null) {
                paramValues = new JSONObject();
            }
            
            Parameter upperLimit = createUpperLimitFromJSON(paramValues, blockName);
            Parameter lowerLimit = createLowerLimitFromJSON(paramValues, blockName);
            Parameter sampleTime = createSampleTimeFromJSON(paramValues, blockName);
            Parameter outDataType = createOutDataTypeFromJSON(paramValues, blockName);
            Parameter saturateParam = createSaturateFromJSON(paramValues, blockName);
            
            IntervalTest block = new IntervalTest(upperLimit, lowerLimit, sampleTime,
                                                 outDataType, saturateParam,
                                                 blockName, blockPath, blockUUID, model);
            
            setParameterBlockReference(block, upperLimit, lowerLimit, sampleTime,
                                     outDataType, saturateParam);
            
            return block;
            
        } catch (Exception e) {
            throw new BlockCreationException("Failed to create IntervalTest block from JSON: " + e.getMessage(), e);
        }
    }
    
    // === Static Factory Method for Programmatic Creation ===
    public static IntervalTest create(String name, String path, double lowerLimit, double upperLimit, NCSLabModel model) {
        return create(name, path, String.valueOf(lowerLimit), String.valueOf(upperLimit), -1.0, 
                     "Inherit: Logical (see Configuration Parameters: Optimization)", false, model);
    }
    
    public static IntervalTest create(String name, String path, String lowerLimit, String upperLimit, 
                                     double sampleTime, String outDataType, boolean saturateOnOverflow, NCSLabModel model) {
        Parameter upperLimitParam = new Parameter(null, 1, "UpperLimit", upperLimit);
        Parameter lowerLimitParam = new Parameter(null, 2, "LowerLimit", lowerLimit);
        Parameter sampleTimeParam = new Parameter(null, 3, "SampleTime", String.valueOf(sampleTime));
        Parameter outDataTypeParam = new Parameter(null, 4, "OutDataTypeStr", outDataType);
        Parameter saturateParam = new Parameter(null, 5, "SaturateOnIntegerOverflow", saturateOnOverflow ? "on" : "off");
        
        IntervalTest block = new IntervalTest(upperLimitParam, lowerLimitParam, sampleTimeParam,
                                             outDataTypeParam, saturateParam,
                                             name, path, "null", model);
        
        setParameterBlockReference(block, upperLimitParam, lowerLimitParam, sampleTimeParam,
                                 outDataTypeParam, saturateParam);
        
        return block;
    }
    
    // === Helper Methods for JSON Parameter Creation ===
    private static Parameter createUpperLimitFromJSON(JSONObject paramValues, String blockName) {
        String upperLimitValue = paramValues.optString("uplimit", "1");
        return new Parameter(null, 1, "UpperLimit", upperLimitValue);
    }
    
    private static Parameter createLowerLimitFromJSON(JSONObject paramValues, String blockName) {
        String lowerLimitValue = paramValues.optString("lowlimit", "0");
        return new Parameter(null, 2, "LowerLimit", lowerLimitValue);
    }
    
    private static Parameter createSampleTimeFromJSON(JSONObject paramValues, String blockName) {
        String sampleTimeValue = paramValues.optString("SampleTime", "-1");
        return new Parameter(null, 3, "SampleTime", sampleTimeValue);
    }
    
    private static Parameter createOutDataTypeFromJSON(JSONObject paramValues, String blockName) {
        String outDataTypeValue = paramValues.optString("OutDataTypeStr", "Inherit: Logical (see Configuration Parameters: Optimization)");
        return new Parameter(null, 4, "OutDataTypeStr", outDataTypeValue);
    }
    
    private static Parameter createSaturateFromJSON(JSONObject paramValues, String blockName) {
        String saturateValue = paramValues.optString("SaturateOnIntegerOverflow", "off");
        return new Parameter(null, 5, "SaturateOnIntegerOverflow", saturateValue);
    }
    
    // === Utility Methods ===
    private static String requireNonEmptyString(JSONObject json, String key) {
        if (!json.has(key)) {
            throw new IllegalArgumentException("Required field '" + key + "' is missing");
        }
        String value = json.getString(key);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Field '" + key + "' cannot be empty");
        }
        return value;
    }
    
    private static void setParameterBlockReference(IntervalTest block, Parameter... parameters) {
        for (Parameter param : parameters) {
            try {
                java.lang.reflect.Field blockField = Parameter.class.getDeclaredField("block");
                blockField.setAccessible(true);
                blockField.set(param, block);
            } catch (Exception e) {
                // Fallback: parameter block reference will be null, but should work for basic operations
            }
        }
    }
    
    private static JSONObject createBlockIdentity(String blockName, String blockPath, String blockUUID) {
        JSONObject identity = new JSONObject();
        identity.put("blockType", "IntervalTest");
        identity.put("blockName", blockName);
        identity.put("blockPath", blockPath);
        identity.put("blockUUID", blockUUID);
        return identity;
    }

    @Override
    public void calculateInit() {
        // Initialization logic for IntervalTest block
    }

    @Override
    public void calculateOutput(double t) {
        OutputPort out = outputPortList.get(0);
        Data inputData = inputPortList.get(0).getData();

        double lowerBound = lowerLimit.getDouble();
        double upperBound = upperLimit.getDouble();

        Data resultData;
        switch (inputData.getDataType()) {
            case REAL:
                resultData = new Data(lowerBound <= inputData.getInitValue() && inputData.getInitValue() < upperBound ? 1.0 : 0.0);
                break;
            case MATRIX:
                Matrix matrixResult = new Matrix(inputData.getMatrix().getRowDimension(), inputData.getMatrix().getColumnDimension());
                for (int i = 0; i < inputData.getMatrix().getRowDimension(); i++) {
                    for (int j = 0; j < inputData.getMatrix().getColumnDimension(); j++) {
                        matrixResult.set(i, j, lowerBound <= inputData.getMatrix().get(i, j) && inputData.getMatrix().get(i, j) < upperBound ? 1.0 : 0.0);
                    }
                }
                resultData = new Data(matrixResult);
                break;
            default:
                resultData = new Data(0);
        }

        out.setData(resultData);
    }

    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);
        context.put("block", this);
        context.put("lowLimit", lowLimit);
        context.put("upLimit", upLimit);
        
        String codeStr = TemplateManager.renderTemplate("c/logicAndBit/IntervalTest/init.vm", context);
        code.addInitCode(codeStr);
    }

    public void generateOutputCodeC(CodeStructC code) {
        context.put("block", this);
        context.put("inputs", getInputPortVariables());
        context.put("outputs", getOutputPortVariables());
        context.put("lowLimit", lowLimit.getDouble());  // 直接传递Parameter对象
        context.put("upLimit", upLimit.getDouble());    // 由模板处理参数值获取
        OutputSignal signal = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
        context.put("signal", signal);

        String codeStr = TemplateManager.renderTemplate("c/logicAndBit/IntervalTest/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void updateDimension() throws MatDimException {
        OutputPort out = outputPortList.get(0);
        InputPort in = inputPortList.get(0);
        OutputSignal signal = in.getLinkedLine().getLinkedOutputPort().getOutputSignalC();
        out.setHeight(signal.getHeight());
        out.setWidth(signal.getWidth());
        out.getOutputSignalC().setHeight(signal.getHeight());
        out.getOutputSignalC().setWidth(signal.getWidth());
        out.getOutputSignalC().setDataType(signal.getDataType());
    }

    public void checkDimension() throws MatDimException {
    }
}
