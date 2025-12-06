package com.ncslab.block.subsystem;

import com.ncslab.block.Block;
import com.ncslab.block.data.Data;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.dto.block.specialized.subsystem.SwitchCaseDto;
import com.ncslab.ncslablink.BlockCreationException;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.util.TemplateManager;
import lombok.Getter;
import org.json.JSONObject;

import java.util.*;
import com.ncslab.dto.core.BlockDto;

/**
 * Switch Case block for multi-way conditional branching with SIMULINK-compatible parameters.
 *
 * <p>This block provides multi-way branching based on an integer input signal,
 * triggering different Action Subsystems based on the input value. Each case
 * connects to a SwitchCaseActionSubsystem for conditional execution.</p>
 *
 * <p><b>Key Features:</b></p>
 * <ul>
 *   <li>Multi-way conditional branching based on integer input</li>
 *   <li>Support for case ranges (e.g., [3 4] means 3 or 4)</li>
 *   <li>Optional default case for unmatched values</li>
 *   <li>Function-call outputs to Action Subsystems</li>
 * </ul>
 *
 * <p><b>SIMULINK Parameters:</b></p>
 * <ul>
 *   <li><b>CaseConditions</b>: Case values in MATLAB array format like "{1, 2, [3 4], 5}"</li>
 *   <li><b>ShowDefaultCase</b>: Whether to show default output (true/false)</li>
 *   <li><b>CaseShowStatus</b>: Visibility of each case output (boolean array)</li>
 *   <li><b>SampleTime</b>: Sample time for discrete operation (-1 for inherited)</li>
 * </ul>
 *
 * <p><b>Case Conditions Format:</b></p>
 * <ul>
 *   <li>Single values: {1, 2, 3} - creates 3 cases</li>
 *   <li>Ranges: {1, [3 4], 5} - case 2 matches 3 or 4</li>
 *   <li>Mixed: {1, [2 3 4], 5} - supports multiple values per case</li>
 * </ul>
 *
 * <p><b>Port Configuration:</b></p>
 * <ul>
 *   <li>1 input port (integer or scalar value)</li>
 *   <li>N action output ports (one per case)</li>
 *   <li>1 optional default action output port</li>
 * </ul>
 *
 * <p><b>Behavior:</b></p>
 * <ul>
 *   <li>Compares input against case conditions in order</li>
 *   <li>Triggers action output for first matching case</li>
 *   <li>If no match and ShowDefaultCase=true, triggers default output</li>
 *   <li>Action outputs connect to SwitchCaseActionSubsystem blocks</li>
 * </ul>
 *
 * @author NCSLab Team
 * @version 1.0
 * @since 2025-12-03
 */
public class SwitchCase extends Block {

    // === SIMULINK-Compatible Parameters ===
    @Getter
    private final Parameter caseConditions;

    @Getter
    private final Parameter showDefaultCase;

    @Getter
    private final Parameter caseShowStatus;

    @Getter
    private final Parameter sampleTime;

    // === Parsed Case Data ===
    /**
     * Parsed case values. Each element is a list of integers that match that case.
     * Example: {{1}, {2}, {3, 4}, {5}} means case 0 matches 1, case 1 matches 2,
     * case 2 matches 3 or 4, case 3 matches 5.
     */
    @Getter
    private List<List<Integer>> parsedCases;

    /**
     * Number of case outputs (excluding default).
     */
    @Getter
    private int numCases;

    // === Port References ===
    private InputPort input;
    private List<OutputPort> caseOutputs;
    private OutputPort defaultOutput;

    // === Static Parameter Definitions ===
    public static final Map<String, String> PARAMETER_DEFAULTS;
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("CaseConditions", "{1}");
        PARAMETER_DEFAULTS.put("ShowDefaultCase", "on");
        PARAMETER_DEFAULTS.put("CaseShowStatus", "on");
        PARAMETER_DEFAULTS.put("SampleTime", "-1");
    }

    public static final List<String> inputNames = new ArrayList<>();
    public static final List<String> outputNames = new ArrayList<>();

    static {
        inputNames.add("u");
    }

    // === Private Constructor with Typed Parameters ===
    private SwitchCase(Parameter caseConditions, Parameter showDefaultCase,
                      Parameter caseShowStatus, Parameter sampleTime,
                      String blockName, String blockPath, String blockUUID,
                      NCSLabModel model) {
        super(createBlockIdentity(blockName, blockPath, blockUUID), model);

        // Assign parameters
        this.caseConditions = Objects.requireNonNull(caseConditions, "CaseConditions parameter cannot be null");
        this.showDefaultCase = Objects.requireNonNull(showDefaultCase, "ShowDefaultCase parameter cannot be null");
        this.caseShowStatus = Objects.requireNonNull(caseShowStatus, "CaseShowStatus parameter cannot be null");
        this.sampleTime = Objects.requireNonNull(sampleTime, "Sample time parameter cannot be null");

        // Add parameters to parameterList
        parameterList.add(this.caseConditions);
        parameterList.add(this.showDefaultCase);
        parameterList.add(this.caseShowStatus);
        parameterList.add(this.sampleTime);

        // Parse case conditions
        parseCaseConditions();

        // Initialize ports
        initializePorts();
    }

    // === Legacy Constructor (Deprecated) ===
    @Deprecated
    public SwitchCase(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);

        this.caseConditions = getParameterByName("CaseConditions");
        this.showDefaultCase = getParameterByName("ShowDefaultCase");
        this.caseShowStatus = getParameterByName("CaseShowStatus");
        this.sampleTime = getParameterByName("SampleTime");

        parseCaseConditions();
        initializePorts();
    }

    // === DTO-NATIVE Constructor ===
    public SwitchCase(BlockDto blockDto, NCSLabModel model) {
        super(blockDto, model);

        this.caseConditions = getParameterByName("CaseConditions");
        this.showDefaultCase = getParameterByName("ShowDefaultCase");
        this.caseShowStatus = getParameterByName("CaseShowStatus");
        this.sampleTime = getParameterByName("SampleTime");

        parseCaseConditions();
        initializePorts();

        System.out.println("DTO-NATIVE: " + getClass().getSimpleName() + " block created successfully - " + blockDto.getBlockName());
    }

    // === Static Factory Method for JSON Deserialization ===
    public static SwitchCase fromJSON(JSONObject blockJSON, NCSLabModel model) {
        try {
            String blockName = requireNonEmptyString(blockJSON, "blockName");
            String blockPath = requireNonEmptyString(blockJSON, "blockPath");
            String blockUUID = blockJSON.optString("blockUUID", "null");
            JSONObject paramValues = blockJSON.optJSONObject("paramValues");

            if (paramValues == null) {
                paramValues = new JSONObject();
            }

            Parameter caseConditions = createCaseConditionsFromJSON(paramValues);
            Parameter showDefaultCase = createShowDefaultCaseFromJSON(paramValues);
            Parameter caseShowStatus = createCaseShowStatusFromJSON(paramValues);
            Parameter sampleTime = createSampleTimeFromJSON(paramValues);

            SwitchCase block = new SwitchCase(caseConditions, showDefaultCase, caseShowStatus, sampleTime,
                                             blockName, blockPath, blockUUID, model);

            setParameterBlockReference(block, caseConditions, showDefaultCase, caseShowStatus, sampleTime);

            return block;

        } catch (Exception e) {
            throw new BlockCreationException("Failed to create SwitchCase block from JSON: " + e.getMessage(), e);
        }
    }

    // === Static Factory Method for Programmatic Creation (DTO-Based) ===
    public static SwitchCase create(String name, String path, String caseConditions, NCSLabModel model) {
        return create(name, path, caseConditions, true, "on", -1.0, model);
    }

    public static SwitchCase create(String name, String path, String caseConditions,
                                   boolean showDefaultCase, String caseShowStatus,
                                   double sampleTime, NCSLabModel model) {
        SwitchCaseDto dto = new SwitchCaseDto(
            name,
            path,
            com.ncslab.dto.common.TypedParameter.of(caseConditions),
            com.ncslab.dto.common.TypedParameter.of(showDefaultCase),
            com.ncslab.dto.common.TypedParameter.of(caseShowStatus),
            com.ncslab.dto.common.TypedParameter.of(sampleTime)
        );

        dto.setBlockUUID("null");

        if (!dto.isValid()) {
            throw new IllegalArgumentException("Invalid SwitchCase parameters: " + dto.getValidationErrors());
        }

        return new SwitchCase(dto, model);
    }

    public static SwitchCase createFromDto(SwitchCaseDto dto, NCSLabModel model) {
        if (dto == null) {
            throw new IllegalArgumentException("SwitchCaseDto cannot be null");
        }
        if (model == null) {
            throw new IllegalArgumentException("NCSLabModel cannot be null");
        }

        if (!dto.isValid()) {
            throw new IllegalArgumentException("Invalid SwitchCaseDto: " + dto.getValidationErrors());
        }

        return new SwitchCase(dto, model);
    }

    // === Helper Methods for JSON Parameter Creation ===
    private static Parameter createCaseConditionsFromJSON(JSONObject paramValues) {
        String value = paramValues.optString("CaseConditions", "{1}");
        return new Parameter(null, 1, "CaseConditions", value);
    }

    private static Parameter createShowDefaultCaseFromJSON(JSONObject paramValues) {
        String value = paramValues.optString("ShowDefaultCase", "on");
        return new Parameter(null, 2, "ShowDefaultCase", value);
    }

    private static Parameter createCaseShowStatusFromJSON(JSONObject paramValues) {
        String value = paramValues.optString("CaseShowStatus", "on");
        return new Parameter(null, 3, "CaseShowStatus", value);
    }

    private static Parameter createSampleTimeFromJSON(JSONObject paramValues) {
        String value = paramValues.optString("SampleTime", "-1");
        return new Parameter(null, 4, "SampleTime", value);
    }

    // === Case Condition Parsing ===
    /**
     * Parses case conditions from MATLAB array format like "{1, 2, [3 4], 5}".
     * Each case can be a single value or a range (array).
     */
    private void parseCaseConditions() {
        parsedCases = new ArrayList<>();
        String condStr = caseConditions.getData().getInitString();

        // Remove outer braces and whitespace
        condStr = condStr.trim();
        if (condStr.startsWith("{")) {
            condStr = condStr.substring(1);
        }
        if (condStr.endsWith("}")) {
            condStr = condStr.substring(0, condStr.length() - 1);
        }

        // Split by commas (but not commas inside brackets)
        List<String> caseStrings = splitCases(condStr);

        for (String caseStr : caseStrings) {
            caseStr = caseStr.trim();
            List<Integer> caseValues = new ArrayList<>();

            if (caseStr.startsWith("[") && caseStr.endsWith("]")) {
                // Range case: [3 4] or [3 4 5]
                String rangeStr = caseStr.substring(1, caseStr.length() - 1).trim();
                String[] values = rangeStr.split("\\s+");
                for (String val : values) {
                    try {
                        caseValues.add(Integer.parseInt(val.trim()));
                    } catch (NumberFormatException e) {
                        System.err.println("Warning: Invalid case value '" + val + "' in SwitchCase block " + blockName);
                    }
                }
            } else {
                // Single value case
                try {
                    caseValues.add(Integer.parseInt(caseStr));
                } catch (NumberFormatException e) {
                    System.err.println("Warning: Invalid case value '" + caseStr + "' in SwitchCase block " + blockName);
                }
            }

            if (!caseValues.isEmpty()) {
                parsedCases.add(caseValues);
            }
        }

        numCases = parsedCases.size();
    }

    /**
     * Splits case string by commas, respecting brackets.
     */
    private List<String> splitCases(String str) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        int bracketDepth = 0;

        for (char c : str.toCharArray()) {
            if (c == '[') {
                bracketDepth++;
                current.append(c);
            } else if (c == ']') {
                bracketDepth--;
                current.append(c);
            } else if (c == ',' && bracketDepth == 0) {
                result.add(current.toString().trim());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }

        if (current.length() > 0) {
            result.add(current.toString().trim());
        }

        return result;
    }

    // === Port Initialization ===
    private void initializePorts() {
        caseOutputs = new ArrayList<>();

        // Input port (scalar integer)
        input = new InputPort(this, 1);
        inputPortList.add(input);

        // Case action output ports (function-call type)
        for (int i = 0; i < numCases; i++) {
            OutputPort caseOutput = new OutputPort(this, i + 1, false);
            outputPortList.add(caseOutput);
            caseOutputs.add(caseOutput);
        }

        // Default case output (if enabled)
        if (isShowDefaultCase()) {
            defaultOutput = new OutputPort(this, numCases + 1, false);
            outputPortList.add(defaultOutput);
        }
    }

    // === Code Generation Methods ===
    @Override
    public void generateOutputCodeC(CodeStructC code) {
        super.generateOutputCodeC(code);
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        // Add SwitchCase-specific context
        context.put("inputSignalName", getInputPortVariable(0));
        context.put("numCases", numCases);
        context.put("parsedCases", parsedCases);
        context.put("showDefaultCase", isShowDefaultCase());

        // Add case output signal names
        List<String> caseOutputSignalNames = new ArrayList<>();
        for (int i = 0; i < numCases; i++) {
            caseOutputSignalNames.add(getOutputPortVariable(i));
        }
        context.put("caseOutputSignalNames", caseOutputSignalNames);

        // Add default output signal name (if enabled)
        if (isShowDefaultCase()) {
            context.put("defaultOutputSignalName", getOutputPortVariable(numCases));
        }

        String outputCode = TemplateManager.renderTemplate("c/subsystem/SwitchCase/output.vm", context);
        code.addOutputCode(outputCode);
    }

    @Override
    public void updateDimension() throws MatDimException {
        // SwitchCase outputs are function-call signals (no data dimension)
    }

    @Override
    public void checkDimension() throws MatDimException {
        // No dimension constraints for function-call signals
    }

    @Override
    public void calculateOutput(double t) {
        // Get input value
        Data inputData = input.getData();
        double inputValue = inputData.getInitValue();

        // Convert to integer (round to nearest)
        int inputInt = (int) Math.round(inputValue);

        // Find matching case
        int matchedCase = -1;
        for (int i = 0; i < parsedCases.size(); i++) {
            List<Integer> caseValues = parsedCases.get(i);
            if (caseValues.contains(inputInt)) {
                matchedCase = i;
                break;
            }
        }

        // Set action outputs (1.0 for active, 0.0 for inactive)
        for (int i = 0; i < numCases; i++) {
            double actionValue = (i == matchedCase) ? 1.0 : 0.0;
            caseOutputs.get(i).setData(new Data(actionValue));
        }

        // Set default output (if enabled)
        if (isShowDefaultCase()) {
            double defaultValue = (matchedCase == -1) ? 1.0 : 0.0;
            defaultOutput.setData(new Data(defaultValue));
        }
    }

    // === Parameter Access Methods ===
    public boolean isShowDefaultCase() {
        String value = showDefaultCase.getData().getInitString();
        return "on".equalsIgnoreCase(value) || "true".equalsIgnoreCase(value);
    }

    public String getCaseConditionsValue() {
        return caseConditions.getData().getInitString();
    }

    public String getCaseShowStatusValue() {
        return caseShowStatus.getData().getInitString();
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

    private static void setParameterBlockReference(SwitchCase block, Parameter... parameters) {
        for (Parameter param : parameters) {
            try {
                java.lang.reflect.Field blockField = Parameter.class.getDeclaredField("block");
                blockField.setAccessible(true);
                blockField.set(param, block);
            } catch (Exception e) {
                // Fallback: parameter block reference will be null
            }
        }
    }

    private static JSONObject createBlockIdentity(String blockName, String blockPath, String blockUUID) {
        JSONObject identity = new JSONObject();
        identity.put("blockType", "SwitchCase");
        identity.put("blockName", blockName);
        identity.put("blockPath", blockPath);
        identity.put("blockUUID", blockUUID);
        identity.put("paramValues", new JSONObject());
        return identity;
    }
}
