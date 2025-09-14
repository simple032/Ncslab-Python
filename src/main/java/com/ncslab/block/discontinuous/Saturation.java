package com.ncslab.block.discontinuous;

// Java standard imports
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

// External libraries
import lombok.Getter;
import org.json.JSONObject;
import Jama.Matrix;

// Internal imports - DTO
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.discontinuous.SaturationDto;

// Internal imports - Core
import com.ncslab.ncslablink.BlockCreationException;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;

// Internal imports - Block components
import com.ncslab.block.data.Data;
import com.ncslab.block.data.DataType;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.block.io.Parameter;

// Internal imports - Code generation
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.util.TemplateManager;

/**
 * Saturation block with SIMULINK-compatible parameters and type-safe constructors.
 * 
 * Implements signal saturation by limiting output values between configurable upper and lower bounds.
 * Input signals exceeding the limits are clipped to the respective boundary values.
 * 
 * SIMULINK Parameters:
 * - UpperLimit/UpperSaturationLimit: Upper saturation limit
 * - LowerLimit/LowerSaturationLimit: Lower saturation limit
 * - SampleTime: Sample time for discrete operation (-1 for inherited, 0 for continuous)
 * - OutDataTypeStr: Output data type specification
 * - SaturateOnIntegerOverflow: Handle integer overflow
 * 
 * @author NCSLab Team
 * @version 2025
 */
public class Saturation extends DiscontinuousBlock {
    // === Legacy Compatibility ===
    /** Legacy lower limit parameter for backward compatibility */
    Parameter lowerLimit;
    
    /** Legacy upper limit parameter for backward compatibility */
    Parameter upperLimit;
    
    // === SIMULINK-Compatible Parameters ===
    /** Upper saturation limit parameter */
    private final Parameter upperSaturationLimit;
    
    /** Lower saturation limit parameter */
    private final Parameter lowerSaturationLimit;
    
    /** Sample time parameter */
    private final Parameter sampleTime;
    
    /** Output data type specification parameter */
    private final Parameter outDataType;
    
    /** Integer overflow handling parameter */
    private final Parameter saturateOnIntegerOverflow;

    // === Static Parameter Definitions ===
    
    // Parameter defaults matching database format
    public static final Map<String, String> PARAMETER_DEFAULTS;
    
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("UpperLimit", "1.0");
        PARAMETER_DEFAULTS.put("LowerLimit", "-1.0");
        PARAMETER_DEFAULTS.put("SampleTime", "-1");
        PARAMETER_DEFAULTS.put("OutDataTypeStr", "Inherit: Same as input");
        PARAMETER_DEFAULTS.put("SaturateOnIntegerOverflow", "off");
    }

    public static final List<String> outputNames = new ArrayList<>();
    public static final List<String> inputNames = new ArrayList<>();

    // Port defaults for centralized initialization
    public static final List<Map<String, Object>> INPUT_PORT_DEFAULTS;
    public static final List<Map<String, Object>> OUTPUT_PORT_DEFAULTS;

    static {
        // Port names
        outputNames.add("out1");
        inputNames.add("in1");
        
        // Input port defaults
        INPUT_PORT_DEFAULTS = new ArrayList<>();
        Map<String, Object> input1 = new HashMap<>();
        input1.put("name", "in1");
        input1.put("width", 1);
        input1.put("height", 1);
        input1.put("dataType", "REAL");
        INPUT_PORT_DEFAULTS.add(input1);
        
        // Output port defaults (saturation has feedthrough)
        OUTPUT_PORT_DEFAULTS = new ArrayList<>();
        Map<String, Object> output1 = new HashMap<>();
        output1.put("name", "out1");
        output1.put("width", 1);
        output1.put("height", 1);
        output1.put("dataType", "REAL");
        output1.put("feedthrough", true);
        OUTPUT_PORT_DEFAULTS.add(output1);
    }

    // === Private Constructor with Typed Parameters ===
    private Saturation(String blockName, String blockPath, String blockUUID, NCSLabModel model) {
        super(createBlockIdentity(blockName, blockPath, blockUUID), model);
        
        // Get parameters by name from the automatically populated parameterList (via parseParameterList())
        this.upperSaturationLimit = getParameterByName("UpperLimit");
        this.lowerSaturationLimit = getParameterByName("LowerLimit");
        this.sampleTime = getParameterByName("SampleTime");
        this.outDataType = getParameterByName("OutDataTypeStr");
        this.saturateOnIntegerOverflow = getParameterByName("SaturateOnIntegerOverflow");
        
        // Legacy field mapping for backward compatibility
        this.upperLimit = this.upperSaturationLimit;
        this.lowerLimit = this.lowerSaturationLimit;
        
        // Create ports
        inputPortList.add(new InputPort(this, 1));
        outputPortList.add(new OutputPort(this, 1, true));
    }

    // === Legacy Constructor (Deprecated) ===
    @Deprecated
    public Saturation(JSONObject blockIn, NCSLabModel model) {
        super(blockIn, model); // This calls parseParameterList() automatically
        
        // Get parameters by name from the automatically populated parameterList
        this.upperSaturationLimit = getParameterByName("UpperLimit");
        this.lowerSaturationLimit = getParameterByName("LowerLimit");
        this.sampleTime = getParameterByName("SampleTime");
        this.outDataType = getParameterByName("OutDataTypeStr");
        this.saturateOnIntegerOverflow = getParameterByName("SaturateOnIntegerOverflow");

        // Legacy field mapping for backward compatibility
        this.upperLimit = this.upperSaturationLimit;
        this.lowerLimit = this.lowerSaturationLimit;

        // Create ports
        inputPortList.add(new InputPort(this, 1));
        outputPortList.add(new OutputPort(this, 1, true));
    }    /**
     * DTO-NATIVE Constructor - Creates Saturation block directly from BlockDto DTO
     */
    public Saturation(SaturationDto blockDto, NCSLabModel model) {
        super(blockDto, model);

        // Initialize final parameters from DTO - use correct parameter names matching JSON constructor
        this.upperSaturationLimit = getParameterByName("UpperLimit");
        this.lowerSaturationLimit = getParameterByName("LowerLimit");
        this.sampleTime = getParameterByName("SampleTime");
        this.outDataType = getParameterByName("OutDataTypeStr");
        this.saturateOnIntegerOverflow = getParameterByName("SaturateOnIntegerOverflow");

        // Add all parameters to parameter list if they exist
        if (this.upperSaturationLimit != null) parameterList.add(this.upperSaturationLimit);
        if (this.lowerSaturationLimit != null) parameterList.add(this.lowerSaturationLimit);
        if (this.sampleTime != null) parameterList.add(this.sampleTime);
        if (this.outDataType != null) parameterList.add(this.outDataType);
        if (this.saturateOnIntegerOverflow != null) parameterList.add(this.saturateOnIntegerOverflow);

        
        initializePorts();
        
        // Legacy field mapping for backward compatibility - add null checks
        this.upperLimit = this.upperSaturationLimit;
        this.lowerLimit = this.lowerSaturationLimit;

        System.out.println("DTO-NATIVE: " + getClass().getSimpleName() + " block created successfully - " + blockDto.getBlockName());
    }
    
    private void initializePorts() {
        inputPortList.add(new InputPort(this, 1));
        outputPortList.add(new OutputPort(this, 1, true));
    }// === Static Factory Method for JSON Deserialization ===
    public static Saturation fromJSON(JSONObject blockJSON, NCSLabModel model) {
        try {
            String blockName = requireNonEmptyString(blockJSON, "blockName");
            String blockPath = requireNonEmptyString(blockJSON, "blockPath");
            String blockUUID = blockJSON.optString("blockUUID", "null");
            
            return new Saturation(blockName, blockPath, blockUUID, model);
            
        } catch (Exception e) {
            throw new BlockCreationException("Failed to create Saturation block from JSON: " + e.getMessage(), e);
        }
    }
    
    // === Static Factory Method for Programmatic Creation ===
    public static Saturation create(String name, String path, String upperLimit, String lowerLimit, NCSLabModel model) {
        return create(name, path, upperLimit, lowerLimit, -1.0, "Inherit: Same as input", false, model);
    }

    public static Saturation create(String name, String path, String upperLimit, String lowerLimit,
                                   double sampleTime, String outDataType, boolean saturateOnOverflow, NCSLabModel model) {
        // Create a JSONObject with parameter values for centralized parsing
        JSONObject paramValues = new JSONObject();
        paramValues.put("UpperLimit", upperLimit);
        paramValues.put("LowerLimit", lowerLimit);
        paramValues.put("SampleTime", String.valueOf(sampleTime));
        paramValues.put("OutDataTypeStr", outDataType);
        paramValues.put("SaturateOnIntegerOverflow", saturateOnOverflow ? "on" : "off");
        
        JSONObject blockJSON = createBlockIdentity(name, path, "null");
        blockJSON.put("paramValues", paramValues);
        
        return new Saturation(blockJSON, model);
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
    
    private static JSONObject createBlockIdentity(String blockName, String blockPath, String blockUUID) {
        JSONObject identity = new JSONObject();
        identity.put("blockType", "Saturation");
        identity.put("blockName", blockName);
        identity.put("blockPath", blockPath);
        identity.put("blockUUID", blockUUID);
        return identity;
    }
    
    // === Getter Methods ===
    public double getUpperSaturationLimit() {
        if (upperSaturationLimit == null || upperSaturationLimit.getData() == null) {
            throw new IllegalStateException("Saturation block upper limit parameter is not properly configured");
        }
        return upperSaturationLimit.getData().getInitValue();
    }
    
    public double getLowerSaturationLimit() {
        if (lowerSaturationLimit == null || lowerSaturationLimit.getData() == null) {
            throw new IllegalStateException("Saturation block lower limit parameter is not properly configured");
        }
        return lowerSaturationLimit.getData().getInitValue();
    }
    
    @Override
    public void calculateOutput(double t) {
        // SIMULINK Saturation block: limits signal to specified range
        
        // Fail fast - validate required ports and parameters exist
        if (inputPortList == null || inputPortList.isEmpty()) {
            throw new IllegalStateException("Saturation block cannot calculate output: no input ports configured");
        }
        if (outputPortList == null || outputPortList.isEmpty()) {
            throw new IllegalStateException("Saturation block cannot calculate output: no output ports configured");
        }
        
        InputPort inputPort = inputPortList.get(0);
        if (inputPort == null || inputPort.getData() == null) {
            throw new IllegalStateException("Saturation block cannot calculate output: input data is null");
        }
        
        OutputPort outputPort = outputPortList.get(0);
        if (outputPort == null) {
            throw new IllegalStateException("Saturation block cannot calculate output: output port is null");
        }
        
        Data inputData = inputPort.getData();
        double lowerLim = getLowerSaturationLimit();
        double upperLim = getUpperSaturationLimit();
        Data outputData;
        
        if (inputData.getDataType() == DataType.MATRIX) {
            // Matrix input - apply saturation element-wise
            Matrix inputMatrix = inputData.getMatrix();
            Matrix outputMatrix = new Matrix(inputMatrix.getRowDimension(), inputMatrix.getColumnDimension());
            
            for (int i = 0; i < inputMatrix.getRowDimension(); i++) {
                for (int j = 0; j < inputMatrix.getColumnDimension(); j++) {
                    double value = inputMatrix.get(i, j);
                    double saturatedValue = applySaturation(value, lowerLim, upperLim);
                    outputMatrix.set(i, j, saturatedValue);
                }
            }
            outputData = new Data(outputMatrix);
        } else {
            // Scalar input
            double inputValue = inputData.getInitValue();
            double saturatedValue = applySaturation(inputValue, lowerLim, upperLim);
            outputData = new Data(1, 1);
            outputData.setInitValue(saturatedValue);
        }
        
        outputPort.setData(outputData);
    }

    /**
     * Apply saturation limits to a value.
     * @param value Input value to saturate
     * @param lowerLimit Lower saturation limit
     * @param upperLimit Upper saturation limit
     * @return Saturated value within specified limits
     */
    protected double applySaturation(double value, double lowerLimit, double upperLimit) {
        if (value < lowerLimit) {
            return lowerLimit;
        } else if (value > upperLimit) {
            return upperLimit;
        } else {
            return value;
        }
    }

    // === Code Generation Methods ===
    public void generateOutputCodeC(CodeStructC code) {
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);
        
        // Fail fast - validate required ports exist
        if (inputPortList == null || inputPortList.isEmpty()) {
            throw new BlockCreationException("Saturation block requires input port for code generation");
        }
        context.put("inputPortListSize", inputPortList.size());
        
        // Fail fast - validate required parameters exist
        if (lowerSaturationLimit == null) {
            throw new BlockCreationException("Saturation block requires lower saturation limit parameter");
        }
        if (upperSaturationLimit == null) {
            throw new BlockCreationException("Saturation block requires upper saturation limit parameter");
        }
        
        // Use legacy fields if available, otherwise use SIMULINK parameters
        Parameter lowerParam = (lowerLimit != null) ? lowerLimit : lowerSaturationLimit;
        Parameter upperParam = (upperLimit != null) ? upperLimit : upperSaturationLimit;
        
        context.put("lowerLimit", lowerParam);
        context.put("lowerLimitName", context.get(lowerParam.getLocalName())); // Use parameter local name mapped by TemplateUtils
        context.put("upperLimit", upperParam);
        context.put("upperLimitName", context.get(upperParam.getLocalName())); // Use parameter local name mapped by TemplateUtils
        
        // Add port signal names
        context.put("inputPort0SignalName", getInputPortVariable(0));
        context.put("outputPort0SignalName", getOutputPortVariable(0));
        context.put("inputSignalName", getInputPortVariable(0));
        context.put("outputSignalName", getOutputPortVariable(0));

        String codeStr = TemplateManager.renderTemplate("c/discontinuous/Saturation/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);
        
        // Fail fast - validate required parameters exist
        if (lowerSaturationLimit == null) {
            throw new BlockCreationException("Saturation block requires lower saturation limit parameter for initialization");
        }
        if (upperSaturationLimit == null) {
            throw new BlockCreationException("Saturation block requires upper saturation limit parameter for initialization");
        }
        
        // Use legacy fields if available, otherwise use SIMULINK parameters
        Parameter lowerParam = (lowerLimit != null) ? lowerLimit : lowerSaturationLimit;
        Parameter upperParam = (upperLimit != null) ? upperLimit : upperSaturationLimit;

        context.put("lowerLimit", lowerParam);
        context.put("lowerLimitName", context.get(lowerParam.getLocalName())); // Use parameter local name mapped by TemplateUtils
        context.put("upperLimit", upperParam);
        context.put("upperLimitName", context.get(upperParam.getLocalName())); // Use parameter local name mapped by TemplateUtils

        String codeStr = TemplateManager.renderTemplate("c/discontinuous/Saturation/init.vm", context);
        code.addInitCode(codeStr);
    }

    // === Dimension Management ===
    public void updateDimension() throws MatDimException {
        // Fail fast - validate required ports and connections exist
        if (outputPortList == null || outputPortList.isEmpty()) {
            throw new MatDimException("Saturation block cannot update dimensions: no output ports configured");
        }
        if (inputPortList == null || inputPortList.isEmpty()) {
            throw new MatDimException("Saturation block cannot update dimensions: no input ports configured");
        }
        
        OutputPort out = outputPortList.get(0);
        InputPort in = inputPortList.get(0);
        
        if (out == null) {
            throw new MatDimException("Saturation block cannot update dimensions: output port is null");
        }
        if (in == null) {
            throw new MatDimException("Saturation block cannot update dimensions: input port is null");
        }
        if (in.getLinkedLine() == null || in.getLinkedLine().getLinkedOutputPort() == null) {
            throw new MatDimException("Saturation block cannot update dimensions: input not properly connected");
        }
        
        OutputSignal signal = in.getLinkedLine().getLinkedOutputPort().getOutputSignalC();
        if (signal == null) {
            throw new MatDimException("Saturation block cannot update dimensions: input signal is null");
        }

        out.setHeight(signal.getHeight());
        out.setWidth(signal.getWidth());
        out.getOutputSignalC().setHeight(signal.getHeight());
        out.getOutputSignalC().setWidth(signal.getWidth());
        out.getOutputSignalC().setDataType(signal.getDataType());
    }

    public void checkDimension() throws MatDimException {
        // Check dimensions if needed
    }
}