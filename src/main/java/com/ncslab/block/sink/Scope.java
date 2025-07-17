package com.ncslab.block.sink;

import com.ncslab.block.data.Data;
import lombok.Getter;
import com.ncslab.util.TemplateManager;
import org.json.JSONObject;

import com.ncslab.block.data.DataType;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.BlockCreationException;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;

import com.ncslab.block.discrete.DiscreteBlock;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.block.io.OutputPort;

import com.ncslab.block.io.terminal.ScopeStruct;

import com.ncslab.ncslablink.ModelMode;

import java.util.Objects;
import java.util.Vector;
import java.util.Map;
import java.util.HashMap;

/**
 * Scope block with SIMULINK-compatible parameters and type-safe constructors.
 * 
 * SIMULINK Parameters:
 * - NumberOfInputs: Number of input ports
 * - SampleTime: Sample time for data collection (-1 for inherited, 0 for continuous)
 * - SaveName: Variable name to save data
 * - SaveFormat: Data save format
 * - BufferSize: Size of data buffer
 */
public class Scope extends SinkBlock {

    int inportNum; // TODO：兼容后续多输入
    ScopeStruct[] scopeStructs;

    // === SIMULINK-Compatible Parameters ===
    @Getter
    private final Parameter numberOfInputs;
    @Getter
    private final Parameter sampleTime;
    @Getter
    private final Parameter saveName;
    @Getter
    private final Parameter saveFormat;
    @Getter
    private final Parameter bufferSize;

    // === Static Parameter Definitions ===
    @Getter
    public static final Vector<String> parameterNames = new Vector<>();

    @Getter
    public static final Vector<String> inputNames = new Vector<>();

    static {
        // SIMULINK parameter names
        parameterNames.add("NumberOfInputs");
        parameterNames.add("SampleTime");
        parameterNames.add("SaveName");
        parameterNames.add("SaveFormat");
        parameterNames.add("BufferSize");
        
        // Dynamic input names based on number of inputs
        inputNames.add("in1");
    }

    // === Parameter Defaults ===
    @Getter
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
            inputCount = Integer.parseInt(paramValues.getString("Number"));
        } else {
            inputCount = 1;
        }

        // Create legacy parameters for backward compatibility
        this.numberOfInputs = new Parameter(this, 1, "NumberOfInputs", String.valueOf(inputCount));
        this.sampleTime = new Parameter(this, 2, "SampleTime", "-1"); // -1 for inherited
        this.saveName = new Parameter(this, 3, "SaveName", "ScopeData");
        this.saveFormat = new Parameter(this, 4, "SaveFormat", "Array");
        this.bufferSize = new Parameter(this, 5, "BufferSize", "100000");
        
        // Add all parameters to parameter list

        this.inportNum = inputCount;
        this.scopeStructs = new ScopeStruct[inportNum];

        // Add input ports
        for(int i = 0; i < inportNum; i++) {
            inputPortList.add(new InputPort(this, i+1));
            scopeStructs[i] = new ScopeStruct(this, i+1, "in"+(i+1));
        }
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
    
    // === Static Factory Method for Programmatic Creation ===
    public static Scope create(String name, String path, int numberOfInputs, NCSLabModel model) {
        return create(name, path, String.valueOf(numberOfInputs), "-1", "ScopeData", "Array", "100000", model);
    }
    
    public static Scope create(String name, String path, String numberOfInputs, String sampleTime, 
                              String saveName, String saveFormat, String bufferSize, NCSLabModel model) {
        Parameter numberOfInputsParam = new Parameter(null, 1, "NumberOfInputs", numberOfInputs);
        Parameter sampleTimeParam = new Parameter(null, 2, "SampleTime", sampleTime);
        Parameter saveNameParam = new Parameter(null, 3, "SaveName", saveName);
        Parameter saveFormatParam = new Parameter(null, 4, "SaveFormat", saveFormat);
        Parameter bufferSizeParam = new Parameter(null, 5, "BufferSize", bufferSize);
        
        Scope block = new Scope(numberOfInputsParam, sampleTimeParam, saveNameParam, saveFormatParam, bufferSizeParam,
                               name, path, "null", model);
        
        setParameterBlockReference(block, numberOfInputsParam, sampleTimeParam, saveNameParam, saveFormatParam, bufferSizeParam);
        
        return block;
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
            + " Block" + getInputPortList().get(0).getLinkedLine().getLinkedOutputPort().getBLock().getBlockId()
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
            context.put("blockId", getBlockId());
            context.put("blockName", getBlockName());
            context.put("scopeStruct", scopeStructs[0]);

            String initCode = TemplateManager.renderTemplate("c/sink/Scope/init.vm", context);
            code.addInitCode(initCode);
        }
    }

    public void generateOutputCodeC(CodeStructC code) {
        if (model.getModelMode() == ModelMode.Simulation) {
            context.put("blockId", getBlockId());
            context.put("blockName", getBlockName());
            context.put("inputPortList", getInputPortList());
            context.put("scopeStruct", scopeStructs[0]);

            String outputCode = TemplateManager.renderTemplate("c/sink/Scope/output.vm", context);
            code.addSinkOutputCode(outputCode);

            context.put("linkedBlockId", this.getInputPortList().get(0).getLinkedLine().getLinkedOutputPort().getBLock().getBlockId());
            String sinkStatusClearCode = TemplateManager.renderTemplate("c/sink/Scope/status_clear.vm", context);
            code.addSinkStatusClearCode(sinkStatusClearCode);
        }
    }

    public void generateOutputSinkCodeC(CodeStructC code) {
        if (model.getModelMode() == ModelMode.Simulation) {
            context.put("blockId", getBlockId());
            context.put("blockName", getBlockName());
            context.put("inputPortList", getInputPortList());
            context.put("scopeStruct", scopeStructs[0]);

            String outputCode = TemplateManager.renderTemplate("c/sink/Scope/output.vm", context);
            code.addSinkOutputCode(outputCode);

            context.put("linkedBlockId", this.getInputPortList().get(0).getLinkedLine().getLinkedOutputPort().getBLock().getBlockId());
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
            scopeStructs[i] = new ScopeStruct(this, 1, this.blockName);
            scopeStructs[i].setDimension(signal.getWidth(), signal.getHeight());
            scopeStructs[i].setMaxDataLength(Integer.parseInt(bufferSize.getInitString()));

            model.addTerminal(scopeStructs[i]);
        }
    }

    @Override
    public void calculateDiscreteUpdate(double t){
        if (model.getModelMode() == ModelMode.Simulation) {
            for(int i = 0; i < inportNum; i++) {
                if (scopeStructs[i].getTimeList().isEmpty() || t > scopeStructs[i].getTimeList().lastElement()) {
                    scopeStructs[i].addTimeSeries(
                        t,
                        inputPortList.get(i).getData()
                    );
                }
            }
        }
    }
//    @Override
//    public void calculateTerminate(double t) {
//        if (model.getModelMode() == ModelMode.Simulation) {
//            for(int i = 0; i < inportNum; i++) {
//                if (scopeStructs[i].getTimeList().isEmpty() || t > scopeStructs[i].getTimeList().lastElement()) {
//                    scopeStructs[i].addTimeSeries(
//                        t,
//                        inputPortList.get(i).getData()
//                    );
//                }
//            }
//        }
//    }
}
