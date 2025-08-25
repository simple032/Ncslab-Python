package com.ncslab.block.discontinuous;

import com.ncslab.block.discontinuous.DiscontinuousBlock;
import com.ncslab.block.data.Data;
import com.ncslab.block.data.DataType;
import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.discontinuous.SaturationDto;
import java.util.HashMap;

import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import Jama.Matrix;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.BlockCreationException;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.util.TemplateManager;

import java.util.Map;
import java.util.Objects;
import java.util.ArrayList;
import java.util.List;

/**
 * Saturation block with SIMULINK-compatible parameters and type-safe constructors.
 * 
 * SIMULINK Parameters:
 * - UpperSaturationLimit: Upper saturation limit
 * - LowerSaturationLimit: Lower saturation limit
 * - SampleTime: Sample time for discrete operation (-1 for inherited, 0 for continuous)
 * - OutDataTypeStr: Output data type specification
 * - SaturateOnIntegerOverflow: Handle integer overflow
 */
public class Saturation extends DiscontinuousBlock {
    // Legacy fields for backward compatibility
    Parameter lowerLimit;
    Parameter upperLimit;

    // === SIMULINK-Compatible Parameters ===
    private final Parameter upperSaturationLimit;
    private final Parameter lowerSaturationLimit;
    private final Parameter sampleTime;
    private final Parameter outDataType;
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

    static {
        // Port names
        outputNames.add("out1");
        inputNames.add("in1");
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

        // Initialize final parameters from DTO
        this.upperSaturationLimit = getParameterByName("Uppersaturationlimit");
        this.lowerSaturationLimit = getParameterByName("Lowersaturationlimit");
        this.sampleTime = getParameterByName("SampleTime");
        this.outDataType = getParameterByName("OutDataTypeStr");
        this.saturateOnIntegerOverflow = getParameterByName("SaturateOnIntegerOverflow");

        // Initialize ports
        initializePorts();
        
        // Legacy field mapping for backward compatibility
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
        return upperSaturationLimit.getData().getInitValue();
    }
    
    public double getLowerSaturationLimit() {
        return lowerSaturationLimit.getData().getInitValue();
    }
    
    @Override
    public void calculateOutput(double t) {
        // SIMULINK Saturation block: limits signal to specified range
        Data inputData = inputPortList.get(0).getData();
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
        
        outputPortList.get(0).setData(outputData);
    }

    // === Code Generation Methods ===
    public void generateOutputCodeC(CodeStructC code) {
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);
        
        // Add input port list size
        context.put("inputPortListSize", inputPortList.size());
        
        // Add parameter objects and their names
        context.put("lowerLimit", lowerLimit);
        context.put("upperLimit", upperLimit);
        context.put("lowerLimitName", lowerLimit.getName());
        context.put("upperLimitName", upperLimit.getName());
        
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
        
        // Add parameter objects for template
        context.put("lowerLimit", lowerLimit);
        context.put("upperLimit", upperLimit);

        String codeStr = TemplateManager.renderTemplate("c/discontinuous/Saturation/init.vm", context);
        code.addInitCode(codeStr);
    }

    // === Dimension Management ===
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
        // Check dimensions if needed
    }
}