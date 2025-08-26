package com.ncslab.block.discontinuous;

import com.ncslab.block.discontinuous.DiscontinuousBlock;
import com.ncslab.block.data.Data;
import com.ncslab.block.data.DataType;
import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.discontinuous.BacklashDto;

import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import Jama.Matrix;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.block.io.Parameter;
import com.ncslab.block.io.State;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.BlockCreationException;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.util.TemplateManager;

import java.util.HashMap;
import java.util.Objects;
import java.util.ArrayList;
import java.util.List;

/**
 * Backlash block with SIMULINK-compatible parameters and type-safe constructors.
 * 
 * SIMULINK Parameters:
 * - BacklashWidth: Width of the backlash gap
 * - InitialOutput: Initial output value
 * - SampleTime: Sample time for discrete operation (-1 for inherited, 0 for continuous)
 * - OutDataTypeStr: Output data type specification
 * - SaturateOnIntegerOverflow: Handle integer overflow
 */
public class Backlash extends DiscontinuousBlock {
    // Legacy fields for backward compatibility
    Parameter backlashWidth;
    Parameter initialOutput;
    private State xState;

    // === SIMULINK-Compatible Parameters ===
    private final Parameter backlashWidthParam;
    private final Parameter initialOutputParam;
    private final Parameter sampleTime;
    private final Parameter outDataType;
    private final Parameter saturateOnIntegerOverflow;

    // === Static Parameter Definitions ===

    public static final HashMap<String, String> PARAMETER_DEFAULTS = new HashMap<>();

    public static final List<String> outputNames = new ArrayList<>();
    public static final List<String> inputNames = new ArrayList<>();

    static {
        // Parameter defaults
        PARAMETER_DEFAULTS.put("BacklashWidth", "0.5");
        PARAMETER_DEFAULTS.put("InitialOutput", "0");
        PARAMETER_DEFAULTS.put("SampleTime", "-1");
        PARAMETER_DEFAULTS.put("OutDataTypeStr", "Inherit: Same as input");
        PARAMETER_DEFAULTS.put("SaturateOnIntegerOverflow", "off");
        
        // Port names
        outputNames.add("out1");
        inputNames.add("in1");
    }

    // === Private Constructor with Typed Parameters ===
    private Backlash(Parameter backlashWidth, Parameter initialOutput, Parameter sampleTime,
                    Parameter outDataType, Parameter saturateOnIntegerOverflow,
                    String blockName, String blockPath, String blockUUID, NCSLabModel model) {
        super(createBlockIdentity(blockName, blockPath, blockUUID), model);
        
        // Assign parameters
        this.backlashWidthParam = Objects.requireNonNull(backlashWidth, "BacklashWidth parameter cannot be null");
        this.initialOutputParam = Objects.requireNonNull(initialOutput, "InitialOutput parameter cannot be null");
        this.sampleTime = Objects.requireNonNull(sampleTime, "Sample time parameter cannot be null");
        this.outDataType = Objects.requireNonNull(outDataType, "Output data type parameter cannot be null");
        this.saturateOnIntegerOverflow = Objects.requireNonNull(saturateOnIntegerOverflow, "Saturate parameter cannot be null");
        // Legacy field mapping for backward compatibility
        this.backlashWidth = this.backlashWidthParam;
        this.initialOutput = this.initialOutputParam;
        
        // Create ports
        inputPortList.add(new InputPort(this, 1));
        outputPortList.add(new OutputPort(this, 1, true));
    }
    
    // === Legacy Constructor (Deprecated) ===
    @Deprecated
    public Backlash(JSONObject blockIn, NCSLabModel model) {
        super(blockIn, model);

        // Create legacy parameters for backward compatibility
        this.backlashWidthParam = getParameterByName("BacklashWidth");
        this.initialOutputParam = getParameterByName("InitialOutput");
        this.sampleTime = getParameterByName("SampleTime"); // -1 for inherited
        this.outDataType = getParameterByName("OutDataTypeStr");
        this.saturateOnIntegerOverflow = getParameterByName("SaturateOnIntegerOverflow");
        
        // Add all parameters to parameter list

        // Legacy field mapping for backward compatibility
        this.backlashWidth = this.backlashWidthParam;
        this.initialOutput = this.initialOutputParam;

        // Create ports
        inputPortList.add(new InputPort(this, 1));
        outputPortList.add(new OutputPort(this, 1, true));
    }    /**
     * DTO-NATIVE Constructor - Creates Backlash block directly from BlockDto DTO
     */
    public Backlash(BacklashDto blockDto, NCSLabModel model) {
        super(blockDto, model);

        // Initialize final parameters from DTO with proper null checking
        this.backlashWidthParam = getParameterOrDefault("BacklashWidth", 1, "BacklashWidth");
        this.initialOutputParam = getParameterOrDefault("InitialOutput", 2, "InitialOutput");
        this.sampleTime = getParameterOrDefault("SampleTime", 3, "SampleTime");
        this.outDataType = getParameterOrDefault("OutDataTypeStr", 4, "OutDataTypeStr");
        this.saturateOnIntegerOverflow = getParameterOrDefault("SaturateOnIntegerOverflow", 5, "SaturateOnIntegerOverflow");

        // Initialize ports
        initializePorts();
        
        // Legacy field mapping for backward compatibility
        this.backlashWidth = this.backlashWidthParam;
        this.initialOutput = this.initialOutputParam;

        System.out.println("DTO-NATIVE: " + getClass().getSimpleName() + " block created successfully - " + blockDto.getBlockName());
    }
    
    private void initializePorts() {
        inputPortList.add(new InputPort(this, 1));
        outputPortList.add(new OutputPort(this, 1, true));
    }
    
    /**
     * Helper method to get parameter or create default if null
     */
    private Parameter getParameterOrDefault(String paramName, int paramId, String defaultKey) {
        Parameter param = getParameterByName(paramName);
        if (param == null) {
            param = new Parameter(this, paramId, paramName, PARAMETER_DEFAULTS.get(defaultKey));
        }
        return param;
    }

    // === Static Factory Method for JSON Deserialization ===
    public static Backlash fromJSON(JSONObject blockJSON, NCSLabModel model) {
        try {
            String blockName = requireNonEmptyString(blockJSON, "blockName");
            String blockPath = requireNonEmptyString(blockJSON, "blockPath");
            String blockUUID = blockJSON.optString("blockUUID", "null");
            JSONObject paramValues = blockJSON.optJSONObject("paramValues");
            
            if (paramValues == null) {
                paramValues = new JSONObject();
            }
            
            Parameter backlashWidth = createBacklashWidthFromJSON(paramValues, blockName);
            Parameter initialOutput = createInitialOutputFromJSON(paramValues, blockName);
            Parameter sampleTime = createSampleTimeFromJSON(paramValues, blockName);
            Parameter outDataType = createOutDataTypeFromJSON(paramValues, blockName);
            Parameter saturateParam = createSaturateFromJSON(paramValues, blockName);
            
            Backlash block = new Backlash(backlashWidth, initialOutput, sampleTime,
                                         outDataType, saturateParam,
                                         blockName, blockPath, blockUUID, model);
            
            setParameterBlockReference(block, backlashWidth, initialOutput, sampleTime,
                                     outDataType, saturateParam);
            
            return block;
            
        } catch (Exception e) {
            throw new BlockCreationException("Failed to create Backlash block from JSON: " + e.getMessage(), e);
        }
    }
    
    // === Static Factory Method for Programmatic Creation ===
    public static Backlash create(String name, String path, String backlashWidth, String initialOutput, NCSLabModel model) {
        return create(name, path, backlashWidth, initialOutput, -1.0, "Inherit: Same as input", false, model);
    }
    
    public static Backlash create(String name, String path, String backlashWidth, String initialOutput,
                                 double sampleTime, String outDataType, boolean saturateOnOverflow, NCSLabModel model) {
        Parameter backlashWidthParam = new Parameter(null, 1, "BacklashWidth", backlashWidth);
        Parameter initialOutputParam = new Parameter(null, 2, "InitialOutput", initialOutput);
        Parameter sampleTimeParam = new Parameter(null, 3, "SampleTime", String.valueOf(sampleTime));
        Parameter outDataTypeParam = new Parameter(null, 4, "OutDataTypeStr", outDataType);
        Parameter saturateParam = new Parameter(null, 5, "SaturateOnIntegerOverflow", saturateOnOverflow ? "on" : "off");
        
        Backlash block = new Backlash(backlashWidthParam, initialOutputParam, sampleTimeParam,
                                     outDataTypeParam, saturateParam,
                                     name, path, "null", model);
        
        setParameterBlockReference(block, backlashWidthParam, initialOutputParam, sampleTimeParam,
                                 outDataTypeParam, saturateParam);
        
        return block;
    }
    
    // === Helper Methods for JSON Parameter Creation ===
    private static Parameter createBacklashWidthFromJSON(JSONObject paramValues, String blockName) {
        String backlashWidthValue = paramValues.optString("BacklashWidth", "1");
        return new Parameter(null, 1, "BacklashWidth", backlashWidthValue);
    }
    
    private static Parameter createInitialOutputFromJSON(JSONObject paramValues, String blockName) {
        String initialOutputValue = paramValues.optString("InitialOutput", "0");
        return new Parameter(null, 2, "InitialOutput", initialOutputValue);
    }
    
    private static Parameter createSampleTimeFromJSON(JSONObject paramValues, String blockName) {
        String sampleTimeValue = paramValues.optString("SampleTime", "-1");
        return new Parameter(null, 3, "SampleTime", sampleTimeValue);
    }
    
    private static Parameter createOutDataTypeFromJSON(JSONObject paramValues, String blockName) {
        String outDataTypeValue = paramValues.optString("OutDataTypeStr", "Inherit: Same as input");
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
    
    private static void setParameterBlockReference(Backlash block, Parameter... parameters) {
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
        identity.put("blockType", "Backlash");
        identity.put("blockName", blockName);
        identity.put("blockPath", blockPath);
        identity.put("blockUUID", blockUUID);
        return identity;
    }

    @Override
    public void calculateInit() {
        // Initialize with initial output parameter value
    }

    @Override
    public void calculateOutput(double t) {
        OutputPort out = outputPortList.get(0);
        Data inputData = inputPortList.get(0).getData();

        Data resultData;
        switch (inputData.getDataType()) {
            case REAL:
                double lowerLimit = backlashWidth.getDouble();
                double previousOutput = xState != null ? xState.getData().getInitValue() : 0.0;
                double currentInput = inputData.getInitValue();
                if (currentInput >= previousOutput + lowerLimit) {
                    resultData = new Data(currentInput - lowerLimit);
                } else if (currentInput <= previousOutput - lowerLimit) {
                    resultData = new Data(currentInput + lowerLimit);
                } else {
                    resultData = new Data(previousOutput);
                }
                break;
            case MATRIX:
                Matrix matrixResult = new Matrix(inputData.getMatrix().getRowDimension(), inputData.getMatrix().getColumnDimension());
                for (int i = 0; i < inputData.getMatrix().getRowDimension(); i++) {
                    for (int j = 0; j < inputData.getMatrix().getColumnDimension(); j++) {
                        double lowerLimitMatrix = backlashWidth.getMatrix().get(i, j);
                        double previousOutputMatrix = xState != null ? xState.getData().getMatrix().get(i, j) : 0.0;
                        double currentInputMatrix = inputData.getMatrix().get(i, j);
                        if (currentInputMatrix >= previousOutputMatrix + lowerLimitMatrix) {
                            matrixResult.set(i, j, currentInputMatrix - lowerLimitMatrix);
                        } else if (currentInputMatrix <= previousOutputMatrix - lowerLimitMatrix) {
                            matrixResult.set(i, j, currentInputMatrix + lowerLimitMatrix);
                        } else {
                            matrixResult.set(i, j, previousOutputMatrix);
                        }
                    }
                }
                resultData = new Data(matrixResult);
                break;
            default:
                resultData = new Data(0);
        }

        out.setData(resultData);
        if (xState != null) {
            xState.setData(resultData);
        }
    }

    private void prepareContext() {

        OutputPort out  = outputPortList.get(0);
        OutputPort ops = inputPortList.get(0).getLinkedLine().getLinkedOutputPort();
        OutputSignal signal=inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();

        context.put("block", this);
        context.put("inputPortList", inputPortList);
        context.put("outputPortList", outputPortList);
        context.put("backlashWidth", backlashWidth);
        context.put("initialOutput", initialOutput);
        context.put("xState", xState);
        context.put("signal",signal);
        context.put("ops", ops);
        context.put("matrixDataType", DataType.MATRIX);
    }

    public void generateInitCodeM(CodeStructM code) {
        super.generateInitCodeM(code);
        context.put("block", this);
        context.put("backlashWidth", backlashWidth);
        context.put("initialOutput", initialOutput);
        
        String codeStr = TemplateManager.renderTemplate("m/discontinuous/Backlash/init.vm", context);
        code.addInitCode(codeStr);
    }

    public void generateOutputCodeM(CodeStructM code) {
        super.generateOutputCodeM(code);
        OutputPort out  = outputPortList.get(0);
        OutputPort ops = inputPortList.get(0).getLinkedLine().getLinkedOutputPort();
        OutputSignal signal=inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
        
        context.put("block", this);
        context.put("outputSignal", out.getOutputSignalC());
        context.put("inputSignal", signal);
        context.put("backlashWidth", backlashWidth);
        context.put("inputHeight", signal.getHeight());
        context.put("inputWidth", signal.getWidth());
        
        String codeStr = TemplateManager.renderTemplate("m/discontinuous/Backlash/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void generateInitCodeC(CodeStructC code){
        super.generateInitCodeC(code);
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);
        
        // Add required template variables
        context.put("backlashWidth", backlashWidth);
        context.put("backlashWidthName", backlashWidth.getName());
        context.put("initialOutput", initialOutput);
        context.put("xState", xState);
        context.put("xStateName", xState != null ? xState.getName() : "save_data");
        
        // Generate backlash width initialization code if needed
        StringBuilder backlashWidthInitCode = new StringBuilder();
        if (backlashWidth.getDataType() == DataType.MATRIX) {
            for (int i = 0; i < backlashWidth.getHeight(); i++) {
                for (int j = 0; j < backlashWidth.getWidth(); j++) {
                    backlashWidthInitCode.append(backlashWidth.getName())
                                       .append("(").append(i).append(",").append(j).append(") = ")
                                       .append(backlashWidth.getMatrix().get(i, j)).append(";\n");
                }
            }
        } else {
            backlashWidthInitCode.append(backlashWidth.getName())
                               .append(" = ").append(backlashWidth.getDouble()).append(";\n");
        }
        context.put("backlashWidthInitCodeC", backlashWidthInitCode.toString());
        
        // Generate initial output initialization code if needed
        StringBuilder initialOutputInitCode = new StringBuilder();
        if (initialOutput.getDataType() == DataType.MATRIX) {
            for (int i = 0; i < initialOutput.getHeight(); i++) {
                for (int j = 0; j < initialOutput.getWidth(); j++) {
                    initialOutputInitCode.append(initialOutput.getName())
                                        .append("(").append(i).append(",").append(j).append(") = ")
                                        .append(initialOutput.getMatrix().get(i, j)).append(";\n");
                }
            }
        } else {
            initialOutputInitCode.append(initialOutput.getName())
                                .append(" = ").append(initialOutput.getDouble()).append(";\n");
        }
        context.put("initialOutputInitCodeC", initialOutputInitCode.toString());
        
        String initCode = TemplateManager.renderTemplate("c/discontinuous/Backlash/init.vm", context);
        code.addInitCode(initCode);
    }

    public void generateOutputCodeC(CodeStructC code){
        super.generateOutputCodeC(code);
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);
        
        // Add input signal information
        OutputSignal signal = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
        context.put("signalName", signal.getName());
        context.put("inputSignalName", signal.getName());
        
        // Add output signal information
        OutputSignal outputSignal = outputPortList.get(0).getOutputSignalC();
        context.put("outputSignalName", outputSignal.getName());
        
        // Add state information
        context.put("xState", xState);
        context.put("xStateName", xState != null ? xState.getName() : "save_data");
        
        // TODO: Add missing dimension variables for template - xStateHeight, xStateWidth, 
        // opsHeight, opsWidth, backlashWidthHeight, backlashWidthWidth to fix Velocity 
        // "Right side of range operator [n..m] has null value" errors
        
        // Add parameter names
        context.put("backlashWidth", backlashWidth);
        context.put("backlashWidthName", backlashWidth.getName());
        context.put("initialOutput", initialOutput);
        
        String outputCode = TemplateManager.renderTemplate("c/discontinuous/Backlash/output.vm", context);
        code.addOutputCode(outputCode);
    }

    public void updateDimension() throws MatDimException{
        OutputPort out = outputPortList.get(0);
        InputPort in = inputPortList.get(0);
        OutputSignal signal=in.getLinkedLine().getLinkedOutputPort().getOutputSignalC();
        if(signal.getDataType()==DataType.REAL) {
            xState=new State(this,1,"save_data",backlashWidth.getHeight(),backlashWidth.getWidth());}
        else {
            xState=new State(this,1,"save_data",signal.getHeight(),signal.getWidth());
        }
        stateList.add(xState);
        if(backlashWidth.getWidth()!=initialOutput.getWidth()||backlashWidth.getHeight()!=initialOutput.getHeight()) {
            MatDimException e=new MatDimException("Block "+this.blockName+" input dimensions don't match!All input dimensions should be same!");
            throw(e);
        }
        if(backlashWidth.getDataType()==DataType.MATRIX&&signal.getDataType()==DataType.REAL) {
            out.setHeight(backlashWidth.getHeight());
            out.setWidth(backlashWidth.getWidth());
            out.getOutputSignalC().setHeight(backlashWidth.getHeight());
            out.getOutputSignalC().setWidth(backlashWidth.getWidth());
            out.getOutputSignalC().setDataType(DataType.MATRIX);
        }
        else if(backlashWidth.getDataType()==DataType.REAL&&signal.getDataType()==DataType.MATRIX) {
            out.setHeight(signal.getHeight());
            out.setWidth(signal.getWidth());
            out.getOutputSignalC().setHeight(signal.getHeight());
            out.getOutputSignalC().setWidth(signal.getWidth());
            out.getOutputSignalC().setDataType(signal.getDataType());
        }
        else{
            if(backlashWidth.getWidth()!=signal.getWidth()||backlashWidth.getHeight()!=signal.getHeight()) {
            MatDimException e=new MatDimException("Block "+this.blockName+" input dimension doesn't match the gain dimension!\n \n");
            throw(e);
            }
            out.setHeight(backlashWidth.getHeight());
            out.setWidth(backlashWidth.getWidth());
            out.getOutputSignalC().setHeight(backlashWidth.getHeight());
            out.getOutputSignalC().setWidth(backlashWidth.getWidth());
            out.getOutputSignalC().setDataType(backlashWidth.getDataType());
        }
    }
    public void checkDimension() throws MatDimException{
    }
}
