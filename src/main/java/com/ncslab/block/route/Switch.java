package com.ncslab.block.route;

import com.ncslab.block.route.RouteBlock;
import lombok.Getter;

import org.apache.yetus.audience.InterfaceAudience.Public;
import org.json.JSONObject;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.route.SwitchDto;

import com.ncslab.block.data.DataType;
import com.ncslab.block.data.Data;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.BlockCreationException;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.util.TemplateManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.ArrayList;
import java.util.List;

/**
 * Switch block with SIMULINK-compatible parameters and type-safe constructors.
 *
 * SIMULINK Parameters:
 * - Threshold: Threshold value for switching condition
 * - Criteria: Switching criteria (>=, >, ~=)
 * - SampleTime: Sample time for discrete operation (-1 for inherited, 0 for continuous)
 * - OutDataTypeStr: Output data type specification
 * - SaturateOnIntegerOverflow: Handle integer overflow
 */
public class Switch extends RouteBlock {

    // === SIMULINK-Compatible Parameters ===
    private final Parameter threshold;
    private final Parameter criteria;
    private final Parameter sampleTime;
    private final Parameter outDataType;
    private final Parameter saturateOnIntegerOverflow;

    // === Port References ===
    private OutputPort output;
    private InputPort input1; // First data input
    private InputPort inputControl; // Control signal
    private InputPort input2; // Second data input

    // === Static Parameter Definitions ===

    public static final List<String> outputNames = new ArrayList<>();

    public static final List<String> inputNames = new ArrayList<>();

    // Port defaults for centralized initialization
    public static final List<Map<String, Object>> INPUT_PORT_DEFAULTS;
    public static final List<Map<String, Object>> OUTPUT_PORT_DEFAULTS;

    // Parameter defaults matching database format
    public static final Map<String, String> PARAMETER_DEFAULTS;
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("Threshold", "0");
        PARAMETER_DEFAULTS.put("Criteria", ">=");
        PARAMETER_DEFAULTS.put("SampleTime", "-1");
        PARAMETER_DEFAULTS.put("OutDataTypeStr", "Inherit: Same as input");
        PARAMETER_DEFAULTS.put("SaturateOnIntegerOverflow", "off");
    }
    static {
        // SIMULINK parameter names

        // Port names
        outputNames.add("out1");
        inputNames.add("in1"); // First data input
        inputNames.add("in2"); // Control signal
        inputNames.add("in3"); // Second data input
        
        // Input port defaults (3 inputs: data1, control, data2)
        INPUT_PORT_DEFAULTS = new ArrayList<>();
        Map<String, Object> input1 = new HashMap<>();
        input1.put("name", "in1");
        input1.put("width", 1);
        input1.put("height", 1);
        input1.put("dataType", "REAL");
        INPUT_PORT_DEFAULTS.add(input1);
        
        Map<String, Object> input2 = new HashMap<>();
        input2.put("name", "in2");
        input2.put("width", 1);
        input2.put("height", 1);
        input2.put("dataType", "REAL");
        INPUT_PORT_DEFAULTS.add(input2);
        
        Map<String, Object> input3 = new HashMap<>();
        input3.put("name", "in3");
        input3.put("width", 1);
        input3.put("height", 1);
        input3.put("dataType", "REAL");
        INPUT_PORT_DEFAULTS.add(input3);
        
        // Output port defaults (switch has feedthrough)
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
    private Switch(Parameter threshold, Parameter criteria, Parameter sampleTime,
                  Parameter outDataType, Parameter saturateOnIntegerOverflow,
                  String blockName, String blockPath, String blockUUID, NCSLabModel model) {
        super(createBlockIdentity(blockName, blockPath, blockUUID), model);

        // Assign parameters
        this.threshold = Objects.requireNonNull(threshold, "Threshold parameter cannot be null");
        this.criteria = Objects.requireNonNull(criteria, "Criteria parameter cannot be null");
        this.sampleTime = Objects.requireNonNull(sampleTime, "Sample time parameter cannot be null");
        this.outDataType = Objects.requireNonNull(outDataType, "Output data type parameter cannot be null");
        this.saturateOnIntegerOverflow = Objects.requireNonNull(saturateOnIntegerOverflow, "Saturate parameter cannot be null");

        // Add parameters to parameterList for template context population
        parameterList.add(this.threshold);
        parameterList.add(this.criteria);
        parameterList.add(this.sampleTime);
        parameterList.add(this.outDataType);
        parameterList.add(this.saturateOnIntegerOverflow);


        initializePorts();
    }

    // === Legacy Constructor (Deprecated) ===
    @Deprecated
    public Switch(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);

        // Create legacy parameters for backward compatibility
        this.threshold = getParameterByName("Threshold");
        this.criteria = getParameterByName("Criteria");

        // Create missing SIMULINK parameters with defaults
        this.sampleTime = getParameterByName("SampleTime"); // -1 for inherited
        this.outDataType = getParameterByName("OutDataTypeStr");
        this.saturateOnIntegerOverflow = getParameterByName("SaturateOnIntegerOverflow");

        // Add all parameters to parameter list if they exist
        if (this.threshold != null) parameterList.add(this.threshold);
        if (this.criteria != null) parameterList.add(this.criteria);
        if (this.sampleTime != null) parameterList.add(this.sampleTime);
        if (this.outDataType != null) parameterList.add(this.outDataType);
        if (this.saturateOnIntegerOverflow != null) parameterList.add(this.saturateOnIntegerOverflow);

        // Initialize ports
        initializePorts();
    }    

    // ===== DUAL CONSTRUCTOR PATTERN - MIGRATION SUPPORT =====
    // This pattern maintains backward compatibility while enabling DTO migration

    /**
     * Enhanced DTO-based constructor - preferred for new implementations
     * @param dto The DTO containing block configuration
     * @param model The parent model
     */
    public Switch(SwitchDto dto, NCSLabModel model) {
        super(dto, model);
        
        // Validate DTO before initialization
        com.ncslab.dto.mapper.validation.ValidationResult validation = dto.validate();
        if (!validation.isValid()) {
            throw new BlockCreationException("DTO validation failed: " + validation.getErrors());
        }
        
        // Initialize from DTO parameters using new Parameter creation
        this.threshold = getParameterByName("Threshold");
        this.criteria = getParameterByName("Criteria");
        this.sampleTime = getParameterByName("SampleTime");
        this.outDataType = getParameterByName("OutDataTypeStr");
        this.saturateOnIntegerOverflow = getParameterByName("SaturateOnIntegerOverflow");

        // Add all parameters to parameter list if they exist
        if (this.threshold != null) parameterList.add(this.threshold);
        if (this.criteria != null) parameterList.add(this.criteria);
        if (this.sampleTime != null) parameterList.add(this.sampleTime);
        if (this.outDataType != null) parameterList.add(this.outDataType);
        if (this.saturateOnIntegerOverflow != null) parameterList.add(this.saturateOnIntegerOverflow);

         logic exactly like JSONObject constructor
        initializePorts();
        
        // Complete initialization
        System.out.println("Enhanced DTO: " + getClass().getSimpleName() + " block created successfully - " + dto.getBlockName());
    }
    
    /**
     * Generic DTO Constructor for factory compatibility
     */
    public Switch(BlockDto blockDto, NCSLabModel model) {
        this((SwitchDto) blockDto, model);
    }

    // === Static Factory Method for JSON Deserialization ===
    public static Switch fromJSON(JSONObject blockJSON, NCSLabModel model) {
        try {
            String blockName = requireNonEmptyString(blockJSON, "blockName");
            String blockPath = requireNonEmptyString(blockJSON, "blockPath");
            String blockUUID = blockJSON.optString("blockUUID", "null");
            JSONObject paramValues = blockJSON.optJSONObject("paramValues");

            if (paramValues == null) {
                paramValues = new JSONObject();
            }

            Parameter threshold = createThresholdFromJSON(paramValues, blockName);
            Parameter criteria = createCriteriaFromJSON(paramValues, blockName);
            Parameter sampleTime = createSampleTimeFromJSON(paramValues, blockName);
            Parameter outDataType = createOutDataTypeFromJSON(paramValues, blockName);
            Parameter saturateParam = createSaturateFromJSON(paramValues, blockName);

            Switch block = new Switch(threshold, criteria, sampleTime, outDataType, saturateParam,
                                     blockName, blockPath, blockUUID, model);

            setParameterBlockReference(block, threshold, criteria, sampleTime, outDataType, saturateParam);

            return block;

        } catch (Exception e) {
            throw new BlockCreationException("Failed to create Switch block from JSON: " + e.getMessage(), e);
        }
    }

    // === Static Factory Method for Programmatic Creation ===
    public static Switch create(String name, String path, double threshold, String criteria, NCSLabModel model) {
        return create(name, path, threshold, criteria, -1.0, "Inherit: Inherit via internal rule", false, model);
    }

    public static Switch create(String name, String path, double threshold, String criteria, double sampleTime,
                               String outDataType, boolean saturateOnOverflow, NCSLabModel model) {
        Parameter thresholdParam = new Parameter(null, 1, "Threshold", String.valueOf(threshold));
        Parameter criteriaParam = new Parameter(null, 2, "Criteria", criteria);
        Parameter sampleTimeParam = new Parameter(null, 3, "SampleTime", String.valueOf(sampleTime));
        Parameter outDataTypeParam = new Parameter(null, 4, "OutDataTypeStr", outDataType);
        Parameter saturateParam = new Parameter(null, 5, "SaturateOnIntegerOverflow", saturateOnOverflow ? "on" : "off");

        Switch block = new Switch(thresholdParam, criteriaParam, sampleTimeParam, outDataTypeParam, saturateParam,
                                 name, path, "null", model);

        setParameterBlockReference(block, thresholdParam, criteriaParam, sampleTimeParam, outDataTypeParam, saturateParam);

        return block;
    }

    // === Helper Methods for JSON Parameter Creation ===
    private static Parameter createThresholdFromJSON(JSONObject paramValues, String blockName) {
        String thresholdValue = paramValues.optString("Threshold", "0.0");
        return new Parameter(null, 1, "Threshold", thresholdValue);
    }

    private static Parameter createCriteriaFromJSON(JSONObject paramValues, String blockName) {
        String criteriaValue = paramValues.optString("Relop", ">=");
        return new Parameter(null, 2, "Criteria", criteriaValue);
    }

    private static Parameter createSampleTimeFromJSON(JSONObject paramValues, String blockName) {
        String sampleTimeValue = paramValues.optString("SampleTime", "-1");
        return new Parameter(null, 3, "SampleTime", sampleTimeValue);
    }

    private static Parameter createOutDataTypeFromJSON(JSONObject paramValues, String blockName) {
        String outDataTypeValue = paramValues.optString("OutDataTypeStr", "Inherit: Inherit via internal rule");
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

    private static void setParameterBlockReference(Switch block, Parameter... parameters) {
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
        identity.put("blockType", "Switch");
        identity.put("blockName", blockName);
        identity.put("blockPath", blockPath);
        identity.put("blockUUID", blockUUID);
        return identity;
    }

    // === Port Initialization ===
    private void initializePorts() {
        // First data input
        input1 = new InputPort(this, 1);
        inputPortList.add(input1);

        // Control signal input
        inputControl = new InputPort(this, 2);
        inputPortList.add(inputControl);

        // Second data input
        input2 = new InputPort(this, 3);
        inputPortList.add(input2);

        // Main output port (has feedthrough)
        output = new OutputPort(this, 1, true);
        outputPortList.add(output);
    }
	public void generateInitCodeC(CodeStructC code) {
		super.generateInitCodeC(code);
		com.ncslab.util.TemplateUtils.populateAllContext(context, this);
		
		// Add threshold parameter for template
		context.put("threshold", threshold);
		context.put("thresholdInitCodeC", threshold.getInitCodeC());

		String initCode = TemplateManager.renderTemplate("c/route/Switch/init.vm", context);
		code.addInitCode(initCode);
	}
	public void generateOutputCodeC(CodeStructC code) {
		com.ncslab.util.TemplateUtils.populateAllContext(context, this);
		
		// Add input port list size
		context.put("inputPortListSize", inputPortList.size());
		
		// Add input port signal names
		if (inputPortList.size() >= 3) {
			context.put("inputPort0SignalName", getInputPortVariable(0));
			context.put("inputPort1SignalName", getInputPortVariable(1));
			context.put("inputPort2SignalName", getInputPortVariable(2));
		}
		
		// Add output port signal name
		context.put("outputPort0SignalName", getOutputPortVariable(0));
		
		// Add threshold parameter and name
		context.put("threshold", threshold);
		context.put("thresholdName", context.get(threshold.getLocalName())); // Use parameter local name mapped by TemplateUtils // C variable name

		String outputCode = TemplateManager.renderTemplate("c/route/Switch/output.vm", context);
		code.addOutputCode(outputCode);
	}
    public void updateDimension() throws MatDimException{
        OutputPort out  = outputPortList.get(0);
        InputPort in1  = inputPortList.get(0);
        InputPort in3  =  inputPortList.get(2);
        OutputSignal signal1=in1.getLinkedLine().getLinkedOutputPort().getOutputSignalC();
        OutputSignal signal3=in3.getLinkedLine().getLinkedOutputPort().getOutputSignalC();
        if(signal1.getHeight()!=signal3.getHeight()||signal1.getWidth()!=signal3.getWidth()) {
            MatDimException e=new MatDimException("Block "+this.blockName+" input1 and input3 dimension doesn't match!\n \n");
            throw(e);
            }
        out.setHeight(signal1.getHeight());
        out.setWidth(signal1.getWidth());
        out.getOutputSignalC().setHeight(signal1.getHeight());
        out.getOutputSignalC().setWidth(signal1.getWidth());
        out.getOutputSignalC().setDataType(signal1.getDataType());
    }

	public void checkDimension() throws MatDimException{
	   // No additional dimension checks needed for switch block
	}

    @Override
    public void calculateOutput(double t) {
        // SIMULINK Switch block logic:
        // If control signal meets criteria with threshold, output = input1 (port 0)
        // Otherwise, output = input2 (port 2)
        // Control signal is at port 1
        
        // Get input data
        Data input1Data = inputPortList.get(0).getData();  // First input
        Data controlData = inputPortList.get(1).getData(); // Control signal
        Data input2Data = inputPortList.get(2).getData();  // Second input
        
        // Get threshold and criteria parameters
        double thresholdValue = threshold.getDouble();
        String criteriaStr = criteria.getInitString();
        
        // Get control signal value (assuming scalar control for now)
        double controlValue = controlData.getInitValue();
        
        // Apply switching criteria
        boolean condition = false;
        switch (criteriaStr) {
            case ">=":
                condition = (controlValue >= thresholdValue);
                break;
            case ">":
                condition = (controlValue > thresholdValue);
                break;
            case "~=":
            case "!=":
                condition = (Math.abs(controlValue - thresholdValue) > 1e-12); // Not equal with tolerance
                break;
            case "<=":
                condition = (controlValue <= thresholdValue);
                break;
            case "<":
                condition = (controlValue < thresholdValue);
                break;
            case "==":
                condition = (Math.abs(controlValue - thresholdValue) <= 1e-12); // Equal with tolerance
                break;
            default:
                condition = (controlValue >= thresholdValue); // Default to ">="
                break;
        }
        
        // Set output based on condition
        Data outputData;
        if (condition) {
            // Condition is true, output input1
            outputData = input1Data;
        } else {
            // Condition is false, output input2  
            outputData = input2Data;
        }
        
        // Set the output port data
        outputPortList.get(0).setData(outputData);
    }
}