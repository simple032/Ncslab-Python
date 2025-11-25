package com.ncslab.block.sink;

// Java standard imports
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

// External libraries
import lombok.Getter;
import org.json.JSONObject;

// Internal imports - DTO
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.sink.ScopeDto;

// Internal imports - Core
import com.ncslab.ncslablink.BlockCreationException;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.ModelMode;
import com.ncslab.ncslablink.NCSLabModel;

// Internal imports - Block components
import com.ncslab.block.data.Data;
import com.ncslab.block.data.DataType;
import com.ncslab.block.discrete.DiscreteBlock;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.block.io.Parameter;
import com.ncslab.block.io.terminal.ScopeStruct;

// Internal imports - Code generation
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.util.TemplateManager;

/**
 * Scope block with SIMULINK-compatible parameters and type-safe constructors.
 * 
 * Provides signal visualization and data logging capabilities for simulation analysis.
 * Collects and stores input signal data for plotting and post-processing.
 * 
 * SIMULINK Parameters:
 * - NumberOfInputs: Number of input ports
 * - SampleTime: Sample time for data collection (-1 for inherited, 0 for continuous)
 * - SaveName: Variable name to save data
 * - SaveFormat: Data save format
 * - BufferSize: Size of data buffer
 * 
 * @author NCSLab Team
 * @version 2025
 */
public class Scope extends SinkBlock {

    // === Configuration ===
    /** Number of input ports (TODO: Support for multiple inputs) */
    int inportNum;
    
    /** Scope data structures for each input */
    ScopeStruct[] scopeStructs;
    
    // === SIMULINK-Compatible Parameters ===
    /** Number of input ports parameter */
    private final Parameter numberOfInputs;
    
    /** Sample time parameter for data collection */
    private final Parameter sampleTime;
    private final Parameter saveName;
    private final Parameter saveFormat;
    private final Parameter bufferSize;

    // === Static Parameter Definitions ===
    public static final List<String> inputNames = new ArrayList<>();

    // Port defaults for centralized initialization (sink block has no outputs)
    public static final List<Map<String, Object>> INPUT_PORT_DEFAULTS;
    public static final List<Map<String, Object>> OUTPUT_PORT_DEFAULTS;

    static {        
        // Dynamic input names based on number of inputs
        inputNames.add("in1");
        
        // Input port defaults (basic single input, can be expanded dynamically)
        INPUT_PORT_DEFAULTS = new ArrayList<>();
        Map<String, Object> input1 = new HashMap<>();
        input1.put("name", "in1");
        input1.put("width", 1);
        input1.put("height", 1);
        input1.put("dataType", "REAL");
        INPUT_PORT_DEFAULTS.add(input1);
        
        // Output port defaults (sink block has no outputs)
        OUTPUT_PORT_DEFAULTS = new ArrayList<>();
    }

    // === Parameter Defaults ===
    public static final Map<String, String> PARAMETER_DEFAULTS = new HashMap<>();
    static {
        PARAMETER_DEFAULTS.put("NumberOfInputs", "1");
        PARAMETER_DEFAULTS.put("SampleTime", "-1");
        PARAMETER_DEFAULTS.put("SaveName", "ScopeData");
        PARAMETER_DEFAULTS.put("SaveFormat", "Array");
        PARAMETER_DEFAULTS.put("BufferSize", "100000");
    }

    // === Private Constructor with Typed Parameters ===
    private Scope(Parameter numberOfInputs, Parameter sampleTime, Parameter saveName,
                 Parameter saveFormat, Parameter bufferSize,
                 String blockName, String blockPath, String blockUUID, NCSLabModel model) {
        super(createBlockIdentity(blockName, blockPath, blockUUID), model);
        
        // Assign parameters
        this.numberOfInputs = Objects.requireNonNull(numberOfInputs, "NumberOfInputs parameter cannot be null");
        this.sampleTime = Objects.requireNonNull(sampleTime, "Sample time parameter cannot be null");
        this.saveName = Objects.requireNonNull(saveName, "SaveName parameter cannot be null");
        this.saveFormat = Objects.requireNonNull(saveFormat, "SaveFormat parameter cannot be null");
        this.bufferSize = Objects.requireNonNull(bufferSize, "BufferSize parameter cannot be null");
        // Parse number of inputs and create ports
        this.inportNum = Integer.parseInt(numberOfInputs.getInitString());
        this.scopeStructs = new ScopeStruct[inportNum];
        
        // Add input ports
        for(int i = 0; i < inportNum; i++) {
            inputPortList.add(new InputPort(this, i+1));
            scopeStructs[i] = new ScopeStruct(this, i+1, "in"+(i+1));
        }
    }
    
    // === Legacy Constructor (Deprecated) ===
    @Deprecated
    public Scope(JSONObject scopeIn, NCSLabModel model) {
        super(scopeIn, model);

        // Determine number of inputs from legacy parameters
        int inputCount;
        if(paramValues.has("Inputs")) {
            inputCount = Integer.parseInt(paramValues.getString("Inputs"));
        } else {
            inputCount = 1;
        }

        // Create legacy parameters for backward compatibility
        this.numberOfInputs = getParameterByName("NumberOfInputs");
        this.sampleTime = getParameterByName("SampleTime"); // -1 for inherited
        this.saveName = getParameterByName("SaveName");
        this.saveFormat = getParameterByName("SaveFormat");
        this.bufferSize = getParameterByName("BufferSize");
        
        // Add all parameters to parameter list

        this.inportNum = inputCount;
        this.scopeStructs = new ScopeStruct[inportNum];

        // Add input ports
        for(int i = 0; i < inportNum; i++) {
            inputPortList.add(new InputPort(this, i+1));
            scopeStructs[i] = new ScopeStruct(this, i+1, "in"+(i+1));
        }
    }    /**
     * DTO-NATIVE Constructor - Creates Scope block directly from BlockDto DTO
     */
    public Scope(BlockDto blockDto, NCSLabModel model) {
        super(blockDto, model);

        // Use centralized parameter management via getParameterByName
        this.numberOfInputs = getParameterByName("NumberOfInputs");
        this.sampleTime = getParameterByName("SampleTime");
        this.saveName = getParameterByName("SaveName");
        this.saveFormat = getParameterByName("SaveFormat");
        this.bufferSize = getParameterByName("BufferSize");

        // Initialize scope arrays and ports
        this.inportNum = Integer.parseInt(numberOfInputs.getInitString());
        this.scopeStructs = new ScopeStruct[inportNum];

        // Add input ports and create scope structures
        for(int i = 0; i < inportNum; i++) {
            inputPortList.add(new InputPort(this, i+1));
            scopeStructs[i] = new ScopeStruct(this, i+1, "in"+(i+1));
        }

        System.out.println("DTO-NATIVE: " + getClass().getSimpleName() + " block created successfully - " + blockDto.getBlockName());
    }

    /**
     * Factory method to create Scope from ScopeDto.
     *
     * @param dto The ScopeDto containing block configuration
     * @param model The NCSLabModel this block belongs to
     * @return New Scope instance
     * @throws BlockCreationException if block creation fails
     */
    public static Scope createFromDto(ScopeDto dto, NCSLabModel model) throws BlockCreationException {
        return new Scope(dto, model);
    }

    // === Static Factory Method for JSON Deserialization ===
    public static Scope fromJSON(JSONObject blockJSON, NCSLabModel model) {
        try {
            String blockName = requireNonEmptyString(blockJSON, "blockName");
            String blockPath = requireNonEmptyString(blockJSON, "blockPath");
            String blockUUID = blockJSON.optString("blockUUID", "null");
            JSONObject paramValues = blockJSON.optJSONObject("paramValues");
            
            if (paramValues == null) {
                paramValues = new JSONObject();
            }
            
            Parameter numberOfInputs = createNumberOfInputsFromJSON(paramValues, blockName);
            Parameter sampleTime = createSampleTimeFromJSON(paramValues, blockName);
            Parameter saveName = createSaveNameFromJSON(paramValues, blockName);
            Parameter saveFormat = createSaveFormatFromJSON(paramValues, blockName);
            Parameter bufferSize = createBufferSizeFromJSON(paramValues, blockName);
            
            Scope block = new Scope(numberOfInputs, sampleTime, saveName, saveFormat, bufferSize,
                                   blockName, blockPath, blockUUID, model);
            
            setParameterBlockReference(block, numberOfInputs, sampleTime, saveName, saveFormat, bufferSize);
            
            return block;
            
        } catch (Exception e) {
            throw new BlockCreationException("Failed to create Scope block from JSON: " + e.getMessage(), e);
        }
    }
    
    // === Static Factory Method for Programmatic Creation (DTO-Based) ===
    public static Scope create(String name, String path, int numberOfInputs, NCSLabModel model) {
        return create(name, path, String.valueOf(numberOfInputs), "-1", "ScopeData", "Array", "100000", model);
    }

    /**
     * Create a Scope block with full parameters (DTO-based approach).
     *
     * This modern implementation uses DTOs instead of Parameter manipulation,
     * providing type safety, automatic validation, and cleaner code.
     *
     * @param name Block name
     * @param path Block path
     * @param numberOfInputs Number of input ports (1-10)
     * @param sampleTime Sample time (-1 for inherited, 0 for continuous, >0 for discrete)
     * @param saveName Variable name to save scope data
     * @param saveFormat Data save format ("Array", "Structure", or "Structure with time")
     * @param bufferSize Data buffer size (positive integer, typically 100000)
     * @param model Parent model
     * @return Scope block instance
     */
    public static Scope create(String name, String path, String numberOfInputs, String sampleTime,
                              String saveName, String saveFormat, String bufferSize, NCSLabModel model) {
        // Build DTO using type-safe builder pattern
        ScopeDto dto = ScopeDto.builder()
            .blockName(name)
            .blockPath(path)
            .blockUUID("null")
            .numberOfInputs(com.ncslab.dto.common.TypedParameter.of(numberOfInputs))
            .sampleTime(com.ncslab.dto.common.TypedParameter.of(sampleTime))
            .saveName(com.ncslab.dto.common.TypedParameter.of(saveName))
            .saveFormat(com.ncslab.dto.common.TypedParameter.of(saveFormat))
            .bufferSize(com.ncslab.dto.common.TypedParameter.of(bufferSize))
            .build();

        // Validate DTO (automatic validation)
        com.ncslab.dto.mapper.validation.ValidationResult validation = dto.validate();
        if (!validation.isValid()) {
            throw new IllegalArgumentException("Invalid Scope parameters: " + validation.getErrors());
        }

        // Use DTO constructor (clean, no JSONObject workarounds needed!)
        return new Scope(dto, model);
    }
    
    // === Helper Methods for JSON Parameter Creation ===
    private static Parameter createNumberOfInputsFromJSON(JSONObject paramValues, String blockName) {
        int inputCount;
        if(paramValues.has("Inputs")) {
            inputCount = Integer.parseInt(paramValues.getString("Number"));
        } else {
            inputCount = 1;
        }
        return new Parameter(null, 1, "NumberOfInputs", String.valueOf(inputCount));
    }
    
    private static Parameter createSampleTimeFromJSON(JSONObject paramValues, String blockName) {
        String sampleTimeValue = paramValues.optString("SampleTime", "-1");
        return new Parameter(null, 2, "SampleTime", sampleTimeValue);
    }
    
    private static Parameter createSaveNameFromJSON(JSONObject paramValues, String blockName) {
        String saveNameValue = paramValues.optString("SaveName", "ScopeData");
        return new Parameter(null, 3, "SaveName", saveNameValue);
    }
    
    private static Parameter createSaveFormatFromJSON(JSONObject paramValues, String blockName) {
        String saveFormatValue = paramValues.optString("SaveFormat", "Array");
        return new Parameter(null, 4, "SaveFormat", saveFormatValue);
    }
    
    private static Parameter createBufferSizeFromJSON(JSONObject paramValues, String blockName) {
        String bufferSizeValue = paramValues.optString("BufferSize", "100000");
        return new Parameter(null, 5, "BufferSize", bufferSizeValue);
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
    
    private static void setParameterBlockReference(Scope block, Parameter... parameters) {
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
        identity.put("blockType", "Scope");
        identity.put("blockName", blockName);
        identity.put("blockPath", blockPath);
        identity.put("blockUUID", blockUUID);
        return identity;
    }

    public void generateOutputCodeM(CodeStructM code) {
        super.generateOutputCodeM(code);
        String outputCode = "";
        outputCode += "if storeEnable>0\n";
        outputCode += getBlockName() + "=[" + getBlockName()
            + " Block" + getInputPortList().get(0).getLinkedLine().getLinkedOutputPort().getBlock().getBlockId()
            + "_Output" + getInputPortList().get(0).getLinkedLine().getLinkedOutputPort().getNumber()
            + "]"
            + ";\n";
        outputCode += "end\n";
        code.addOutputCode(outputCode);
    }

    public void generateInitCodeM(CodeStructM code) {
        super.generateInitCodeM(code);
        String initCode = "";
        code.addGlobalDefineCode("global " + getBlockName() + ";\n");
        initCode += getBlockName() + "=[];\n";
        initCode += "ScopeNum=ScopeNum+1;\n";
        initCode += "ScopeList=[ScopeList; '" + getBlockName() + "'];\n";
        code.addInitCode(initCode);
    }

    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);
        if (model.getModelMode() == ModelMode.Simulation) {
            com.ncslab.util.TemplateUtils.populateAllContext(context, this);
            
            // FIXME: scopeStructName template variable not resolving properly
            // The template engine is not resolving $scopeStructName variable, appears to be
            // an issue with variable resolution order or template engine configuration
            String scopeStructName = scopeStructs[0].getName();
            context.put("scopeStructName", scopeStructName);

            String initCode = TemplateManager.renderTemplate("c/sink/Scope/init.vm", context);
            code.addInitCode(initCode);
        }
    }

    public void generateOutputCodeC(CodeStructC code) {
        if (model.getModelMode() == ModelMode.Simulation) {
            com.ncslab.util.TemplateUtils.populateAllContext(context, this);
            
            context.put("blockId", getBlockId());
            context.put("blockName", getBlockName());
            context.put("inputPortList", getInputPortList());
            context.put("inputPortListSize", inputPortList.size());
            context.put("scopeStruct", scopeStructs[0]);
            
            // TODO: Fix scopeStructName template variable not resolving properly
            // The template engine is not resolving $scopeStructName variable, appears to be
            // an issue with variable resolution order or template engine configuration
            String scopeStructName = scopeStructs[0].getName();
            context.put("scopeStructName", scopeStructName);
            
            // Add input signal dimensions and details
            if (!inputPortList.isEmpty()) {
                OutputSignal inputSignal = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
                context.put("inputSignal1Height", inputSignal.getHeight());
                context.put("inputSignal1Width", inputSignal.getWidth());
                context.put("inputSignal1Name", "Block" + getBlockId() + "_" + inputSignal.getName()); // C variable name
                context.put("inputSignal", inputSignal.getName());
                context.put("inputSignal1DataType", inputSignal.getDataType());
                context.put("realDataType", com.ncslab.block.data.DataType.REAL);
            }

            String outputCode = TemplateManager.renderTemplate("c/sink/Scope/output.vm", context);
            code.addSinkOutputCode(outputCode);

            context.put("linkedBlockId", this.getInputPortList().get(0).getLinkedLine().getLinkedOutputPort().getBlock().getBlockId());
            String sinkStatusClearCode = TemplateManager.renderTemplate("c/sink/Scope/status_clear.vm", context);
            code.addSinkStatusClearCode(sinkStatusClearCode);
        }
    }

    public void generateOutputSinkCodeC(CodeStructC code) {
        if (model.getModelMode() == ModelMode.Simulation) {
            com.ncslab.util.TemplateUtils.populateAllContext(context, this);
            
            context.put("blockId", getBlockId());
            context.put("blockName", getBlockName());
            context.put("inputPortList", getInputPortList());
            context.put("inputPortListSize", inputPortList.size());
            context.put("scopeStruct", scopeStructs[0]);
            
            // TODO: Fix scopeStructName template variable not resolving properly
            // The template engine is not resolving $scopeStructName variable, appears to be
            // an issue with variable resolution order or template engine configuration
            String scopeStructName = scopeStructs[0].getName();
            context.put("scopeStructName", scopeStructName);
            
            // Add input signal dimensions and details
            if (!inputPortList.isEmpty()) {
                OutputSignal inputSignal = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
                context.put("inputSignal1Height", inputSignal.getHeight());
                context.put("inputSignal1Width", inputSignal.getWidth());
                context.put("inputSignal1Name", "Block" + getBlockId() + "_" + inputSignal.getName()); // C variable name
                context.put("inputSignal", inputSignal.getName());
                context.put("inputSignal1DataType", inputSignal.getDataType());
                context.put("realDataType", com.ncslab.block.data.DataType.REAL);
            }

            String outputCode = TemplateManager.renderTemplate("c/sink/Scope/output.vm", context);
            code.addSinkOutputCode(outputCode);

            context.put("linkedBlockId", this.getInputPortList().get(0).getLinkedLine().getLinkedOutputPort().getBlock().getBlockId());
            String sinkStatusClearCode = TemplateManager.renderTemplate("c/sink/Scope/status_clear.vm", context);
            code.addSinkStatusClearCode(sinkStatusClearCode);
        }
    }

    public void generateTerminateCodeC(CodeStructC code) {
        if (model.getModelMode() == ModelMode.Simulation) {
            context.put("block", this);
            
            String codeStr = TemplateManager.renderTemplate("c/sink/Scope/terminate.vm", context);
            code.addTerminateCode(codeStr);
        }
    }

    public void updateDimension() throws MatDimException {
    }

    public void checkDimension() throws MatDimException {

        OutputSignal signal = this.inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
        for(int i = 0; i < inportNum; i++) {
            // FIXME:one-time fix 
            // if(scopeStructs[i] != null) {
            //     continue;
            // }
            scopeStructs[i] = new ScopeStruct(this, 1, this.blockName);
            scopeStructs[i].setDimension(signal.getWidth(), signal.getHeight());
            scopeStructs[i].setMaxDataLength(Integer.parseInt(bufferSize.getInitString()));

            model.addTerminal(scopeStructs[i]);
        }
    }

    @Override
    public void calculateOutput(double t) {
        // Scope blocks are sink blocks - they collect data but don't produce output
        // For SIMULINK compatibility, we still need to implement calculateOutput
        // The actual data collection happens in calculateDiscreteUpdate
        
        // Store input data in scope structures if in simulation mode
        if (model.getModelMode() == ModelMode.Simulation) {
            for(int i = 0; i < inportNum && i < inputPortList.size(); i++) {
                if (scopeStructs != null && scopeStructs[i] != null) {
                    Data inputData = inputPortList.get(i).getData();
                    if (inputData != null) {
                        // For continuous-time operation, we may need to sample data here
                        // Check if we should sample at this time point
                        double sampleTimeValue = sampleTime.getDouble();
                        boolean shouldSample = false;
                        
                        if (sampleTimeValue <= 0) {
                            // Continuous sampling - sample every call
                            shouldSample = true;
                        } else {
                            // Discrete sampling - check if it's time to sample
                            double lastSampleTime = scopeStructs[i].getTimeList().isEmpty() ? 
                                -1.0 : scopeStructs[i].getTimeList().get(scopeStructs[i].getTimeList().size() - 1);
                            shouldSample = (t - lastSampleTime) >= sampleTimeValue * 0.99; // Small tolerance
                        }
                        
                        if (shouldSample && (scopeStructs[i].getTimeList().isEmpty() || 
                            t > scopeStructs[i].getTimeList().get(scopeStructs[i].getTimeList().size() - 1))) {
                            scopeStructs[i].addTimeSeries(t, inputData);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void calculateInit() {
        // Initialize scope data structures
        if (model.getModelMode() == ModelMode.Simulation) {
            for(int i = 0; i < inportNum; i++) {
                if (scopeStructs != null && scopeStructs[i] != null) {                    
                    // Add initial data point at t=0 if there's input data
                    if (i < inputPortList.size() && inputPortList.get(i).getData() != null) {
                        scopeStructs[i].addTimeSeries(0.0, inputPortList.get(i).getData());
                    }
                }
            }
        }
    }

    @Override
    public void calculateDiscreteUpdate(double t){
        if (model.getModelMode() == ModelMode.Simulation) {
            for(int i = 0; i < inportNum; i++) {
                if (scopeStructs[i].getTimeList().isEmpty() || t > scopeStructs[i].getTimeList().get(scopeStructs[i].getTimeList().size() - 1)) {
                    scopeStructs[i].addTimeSeries(
                        t,
                        inputPortList.get(i).getData()
                    );
                    // Debug: Periodic logging (every 1000 data points)
                    if (scopeStructs[i].getTimeList().size() % 1000 == 0) {
                        System.out.printf("RT Simulation: Scope %s collected %d data points (latest t=%.3f)%n", 
                            this.getBlockName(), scopeStructs[i].getTimeList().size(), t);
                    }
                }
            }
        }
    }
//    @Override
//    public void calculateTerminate(double t) {
//        if (model.getModelMode() == ModelMode.Simulation) {
//            for(int i = 0; i < inportNum; i++) {
//                if (scopeStructs[i].getTimeList().isEmpty() || t > scopeStructs[i].getTimeList().get(scopeStructs[i].getTimeList().size() - 1)) {
//                    scopeStructs[i].addTimeSeries(
//                        t,
//                        inputPortList.get(i).getData()
//                    );
//                }
//            }
//        }
//    }
}
