package com.ncslab.block.subsystem;

import com.ncslab.block.Block;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.dto.block.specialized.subsystem.SwitchCaseActionSubsystemDto;
import com.ncslab.ncslablink.BlockCreationException;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.util.TemplateManager;
import lombok.Getter;
import lombok.Setter;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.ArrayList;
import java.util.List;
import com.ncslab.dto.core.BlockDto;

/**
 * Switch Case Action Subsystem block - Subsystem for SwitchCase conditional execution.
 *
 * <p>This block extends the standard Subsystem block to work with SwitchCase blocks,
 * providing conditional execution based on which case is matched. The subsystem
 * executes only when its corresponding case in the parent SwitchCase block is active.</p>
 *
 * <p><b>Key Features:</b></p>
 * <ul>
 *   <li>Integrates with SwitchCase block for multi-way branching</li>
 *   <li>Receives function-call signal from SwitchCase output</li>
 *   <li>Automatically includes ActionPort for execution control</li>
 *   <li>Supports state management (held/reset) via ActionPort</li>
 * </ul>
 *
 * <p><b>SIMULINK-Compatible Parameters:</b></p>
 * <ul>
 *   <li><b>ActionPortType</b>: Type of action port ("default", "error", "message")</li>
 *   <li><b>InitializeStates</b>: State handling mode ("held" or "reset")</li>
 *   <li><b>PropagateVarSize</b>: Variable size propagation ("on" or "off")</li>
 *   <li><b>SampleTime</b>: Sample time for discrete operation (-1 for inherited)</li>
 * </ul>
 *
 * <p><b>Port Configuration:</b></p>
 * <ul>
 *   <li>1 action input port (function-call from SwitchCase)</li>
 *   <li>Variable data input ports (from internal Inport blocks)</li>
 *   <li>Variable data output ports (from internal Outport blocks)</li>
 * </ul>
 *
 * <p><b>Internal Structure:</b></p>
 * <ul>
 *   <li>Automatically creates internal ActionPort block with matching parameters</li>
 *   <li>ActionPort receives function-call signal from parent SwitchCase</li>
 *   <li>Regular In/Out blocks for data flow (inherited from Subsystem)</li>
 *   <li>State management handled by internal ActionPort</li>
 * </ul>
 *
 * <p><b>State Handling:</b></p>
 * <ul>
 *   <li><b>held mode</b>: Subsystem states freeze when case is inactive</li>
 *   <li><b>reset mode</b>: Subsystem states reset when case becomes active again</li>
 * </ul>
 *
 * <p><b>Execution Behavior:</b></p>
 * <ul>
 *   <li>Action signal > 0: Subsystem executes normally</li>
 *   <li>Action signal = 0: Subsystem execution suspended (states held or reset)</li>
 *   <li>State transitions handled automatically by internal ActionPort</li>
 * </ul>
 *
 * @author NCSLab Team
 * @version 1.0
 * @since 2025-12-03
 */
public class SwitchCaseActionSubsystem extends Subsystem {

    // === SIMULINK-Compatible Parameters ===
    @Getter
    private final Parameter actionPortType;

    @Getter
    private final Parameter initializeStates;

    @Getter
    private final Parameter propagateVarSize;

    @Getter
    private final Parameter sampleTimeParam;

    // === Internal ActionPort Block ===
    /**
     * Internal ActionPort block for conditional execution control.
     * Created automatically during subsystem initialization.
     */
    @Getter
    @Setter
    private ActionPort actionPort;

    // === Action Port Reference ===
    /**
     * Action input port on the subsystem boundary (top edge).
     * Index 0 in inputPortList is reserved for the action port.
     */
    @Getter
    private InputPort actionInputPort;

    // === Static Parameter Definitions ===
    public static final Map<String, String> PARAMETER_DEFAULTS;
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("ActionPortType", "default");
        PARAMETER_DEFAULTS.put("InitializeStates", "held");
        PARAMETER_DEFAULTS.put("PropagateVarSize", "off");
        PARAMETER_DEFAULTS.put("SampleTime", "-1");  // Inherited
    }

    public static final List<String> outputNames = new ArrayList<>();
    public static final List<String> inputNames = new ArrayList<>();

    static {
        // Action port is always present at port index 0
        inputNames.add("action");
        // Data ports added dynamically based on In/Out blocks
    }

    // === Private Constructor with Typed Parameters ===
    private SwitchCaseActionSubsystem(Parameter actionPortType, Parameter initializeStates,
                                     Parameter propagateVarSize, Parameter sampleTimeParam,
                                     String blockName, String blockPath, String blockUUID,
                                     NCSLabModel model) {
        super(createBlockIdentityJSON(blockName, blockPath, blockUUID), model);

        // Assign parameters
        this.actionPortType = Objects.requireNonNull(actionPortType,
            "ActionPortType parameter cannot be null");
        this.initializeStates = Objects.requireNonNull(initializeStates,
            "InitializeStates parameter cannot be null");
        this.propagateVarSize = Objects.requireNonNull(propagateVarSize,
            "PropagateVarSize parameter cannot be null");
        this.sampleTimeParam = Objects.requireNonNull(sampleTimeParam,
            "SampleTime parameter cannot be null");

        // Add parameters to parameterList for template context population
        parameterList.add(this.actionPortType);
        parameterList.add(this.initializeStates);
        parameterList.add(this.propagateVarSize);
        parameterList.add(this.sampleTimeParam);

        // Initialize action port
        initializeActionPort();
    }

    // === Legacy Constructor (Deprecated) ===
    @Deprecated
    public SwitchCaseActionSubsystem(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);

        this.actionPortType = getParameterByName("ActionPortType");
        this.initializeStates = getParameterByName("InitializeStates");
        this.propagateVarSize = getParameterByName("PropagateVarSize");
        this.sampleTimeParam = getParameterByName("SampleTime");

        initializeActionPort();
    }

    // === DTO-NATIVE Constructor ===
    public SwitchCaseActionSubsystem(BlockDto blockDto, NCSLabModel model) {
        super(blockDto, model);

        this.actionPortType = getParameterByName("ActionPortType");
        this.initializeStates = getParameterByName("InitializeStates");
        this.propagateVarSize = getParameterByName("PropagateVarSize");
        this.sampleTimeParam = getParameterByName("SampleTime");

        initializeActionPort();

        System.out.println("DTO-NATIVE: " + getClass().getSimpleName() + " block created successfully - " + blockDto.getBlockName());
    }

    // === Static Factory Method for JSON Deserialization ===
    public static SwitchCaseActionSubsystem fromJSON(JSONObject blockJSON, NCSLabModel model) {
        try {
            String blockName = requireNonEmptyString(blockJSON, "blockName");
            String blockPath = requireNonEmptyString(blockJSON, "blockPath");
            String blockUUID = blockJSON.optString("blockUUID", "null");
            JSONObject paramValues = blockJSON.optJSONObject("paramValues");

            if (paramValues == null) {
                paramValues = new JSONObject();
            }

            Parameter actionPortType = createActionPortTypeFromJSON(paramValues);
            Parameter initializeStates = createInitializeStatesFromJSON(paramValues);
            Parameter propagateVarSize = createPropagateVarSizeFromJSON(paramValues);
            Parameter sampleTimeParam = createSampleTimeFromJSON(paramValues);

            SwitchCaseActionSubsystem block = new SwitchCaseActionSubsystem(
                actionPortType, initializeStates, propagateVarSize, sampleTimeParam,
                blockName, blockPath, blockUUID, model);

            setParameterBlockReference(block, actionPortType, initializeStates, propagateVarSize, sampleTimeParam);

            return block;

        } catch (Exception e) {
            throw new BlockCreationException("Failed to create SwitchCaseActionSubsystem block from JSON: " + e.getMessage(), e);
        }
    }

    // === Static Factory Method for Programmatic Creation (DTO-Based) ===
    public static SwitchCaseActionSubsystem create(String name, String path, NCSLabModel model) {
        return create(name, path, "default", "held", "off", -1.0, model);
    }

    public static SwitchCaseActionSubsystem create(String name, String path, String actionPortType,
                                                  String initializeStates, String propagateVarSize,
                                                  double sampleTime, NCSLabModel model) {
        SwitchCaseActionSubsystemDto dto = new SwitchCaseActionSubsystemDto(
            name,
            path,
            com.ncslab.dto.common.TypedParameter.of(actionPortType),
            com.ncslab.dto.common.TypedParameter.of(initializeStates),
            com.ncslab.dto.common.TypedParameter.of(propagateVarSize),
            com.ncslab.dto.common.TypedParameter.of(sampleTime)
        );

        dto.setBlockUUID("null");

        if (!dto.isValid()) {
            throw new IllegalArgumentException("Invalid SwitchCaseActionSubsystem parameters: " + dto.getValidationErrors());
        }

        return new SwitchCaseActionSubsystem(dto, model);
    }

    public static SwitchCaseActionSubsystem createFromDto(SwitchCaseActionSubsystemDto dto, NCSLabModel model) {
        if (dto == null) {
            throw new IllegalArgumentException("SwitchCaseActionSubsystemDto cannot be null");
        }
        if (model == null) {
            throw new IllegalArgumentException("NCSLabModel cannot be null");
        }

        if (!dto.isValid()) {
            throw new IllegalArgumentException("Invalid SwitchCaseActionSubsystemDto: " + dto.getValidationErrors());
        }

        return new SwitchCaseActionSubsystem(dto, model);
    }

    // === Helper Methods for JSON Parameter Creation ===
    private static Parameter createActionPortTypeFromJSON(JSONObject paramValues) {
        String value = paramValues.optString("ActionPortType", "default");
        return new Parameter(null, 1, "ActionPortType", value);
    }

    private static Parameter createInitializeStatesFromJSON(JSONObject paramValues) {
        String value = paramValues.optString("InitializeStates", "held");
        return new Parameter(null, 2, "InitializeStates", value);
    }

    private static Parameter createPropagateVarSizeFromJSON(JSONObject paramValues) {
        String value = paramValues.optString("PropagateVarSize", "off");
        return new Parameter(null, 3, "PropagateVarSize", value);
    }

    private static Parameter createSampleTimeFromJSON(JSONObject paramValues) {
        String value = paramValues.optString("SampleTime", "-1");
        return new Parameter(null, 4, "SampleTime", value);
    }

    // === Action Port Initialization ===
    private void initializeActionPort() {
        // Create action input port on subsystem boundary (top edge)
        actionInputPort = new InputPort(this, 1);
        inputPortList.add(0, actionInputPort); // Insert at beginning

        // Note: Internal ActionPort block will be created during subsystem initialization
        // when the model processes the subsystem's internal structure
    }

    // === Code Generation Methods ===
    @Override
    public void generateOutputCodeC(CodeStructC code) {
        super.generateOutputCodeC(code);

        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        // Add SwitchCaseActionSubsystem-specific context
        context.put("actionPortType", getActionPortTypeValue());
        context.put("initializeStates", getInitializeStatesValue());
        context.put("propagateVarSize", getPropagateVarSizeValue());
        context.put("actionPort", actionPort);

        // Check if action is active
        if (actionInputPort != null && actionInputPort.getLinkedLine() != null &&
            actionInputPort.getLinkedLine().getLinkedOutputPort() != null) {
            String actionSignalName = actionInputPort.getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName();
            context.put("actionSignalName", actionSignalName);
            context.put("hasActionSignal", true);
        } else {
            context.put("actionSignalName", "0");
            context.put("hasActionSignal", false);
        }

        String outputCode = TemplateManager.renderTemplate("c/subsystem/SwitchCaseActionSubsystem/output.vm", context);
        code.addOutputCode(outputCode);
    }

    @Override
    public void calculateOutput(double t) {
        // Check if action is currently active
        if (actionInputPort != null && actionInputPort.getLinkedLine() != null &&
            actionInputPort.getLinkedLine().getLinkedOutputPort() != null) {
            double actionSignal = actionInputPort.getData().getInitValue();

            // Only execute subsystem if action is active
            if (actionSignal != 0.0) {
                super.calculateOutput(t);
            }
        }
    }

    // === Parameter Access Methods ===
    public String getActionPortTypeValue() {
        return actionPortType.getData().getInitString();
    }

    public String getInitializeStatesValue() {
        return initializeStates.getData().getInitString();
    }

    public String getPropagateVarSizeValue() {
        return propagateVarSize.getData().getInitString();
    }

    public boolean isStatesHeld() {
        return "held".equals(getInitializeStatesValue());
    }

    public boolean isStatesReset() {
        return "reset".equals(getInitializeStatesValue());
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

    private static void setParameterBlockReference(SwitchCaseActionSubsystem block, Parameter... parameters) {
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

    private static JSONObject createBlockIdentityJSON(String blockName, String blockPath, String blockUUID) {
        JSONObject identity = new JSONObject();
        identity.put("blockType", "SwitchCaseActionSubsystem");
        identity.put("blockName", blockName);
        identity.put("blockPath", blockPath);
        identity.put("blockUUID", blockUUID);
        identity.put("paramValues", new JSONObject());
        return identity;
    }
}
