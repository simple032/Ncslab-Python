package com.ncslab.block.subsystem;

import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.dto.block.specialized.subsystem.EnabledAndTriggeredSubsystemDto;
import com.ncslab.ncslablink.BlockCreationException;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.util.TemplateManager;
import lombok.Getter;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import com.ncslab.dto.core.BlockDto;

/**
 * EnabledAndTriggeredSubsystem block - Combined enable and trigger control with SIMULINK-compatible parameters.
 *
 * <p>This subsystem extends the base Subsystem class and combines both Enable and Trigger port
 * functionality. The subsystem executes ONLY when BOTH conditions are met:</p>
 * <ul>
 *   <li>1. Enable signal is active (> 0)</li>
 *   <li>2. Trigger event occurred (edge detected)</li>
 * </ul>
 *
 * <p><b>SIMULINK-Compatible Parameters:</b></p>
 * <ul>
 *   <li><b>StatesWhenEnabling</b>: "held" (default) - states maintain values when disabled,
 *                                   "reset" - states reset to initial conditions when re-enabled</li>
 *   <li><b>TriggerType</b>: "rising" (default) - rising edge detection,
 *                           "falling" - falling edge detection,
 *                           "either" - either edge detection,
 *                           "function-call" - function call mechanism</li>
 *   <li><b>ShowEnablePort</b>: "on" (default) - show enable port, "off" - hide enable port</li>
 *   <li><b>ShowTriggerPort</b>: "on" (default) - show trigger port, "off" - hide trigger port</li>
 *   <li><b>ZeroCross</b>: "on" - enable zero-crossing detection, "off" (default) - no zero-crossing</li>
 * </ul>
 *
 * <p><b>Port Configuration:</b></p>
 * <ul>
 *   <li>1 enable input port (on top edge) - controls subsystem enable state</li>
 *   <li>1 trigger input port (on top edge) - detects trigger events</li>
 *   <li>Regular In/Out blocks for data flow (inherited from Subsystem)</li>
 * </ul>
 *
 * <p><b>Execution Logic:</b></p>
 * <ul>
 *   <li>Enable state is checked first (signal > 0)</li>
 *   <li>Trigger event is detected based on TriggerType parameter</li>
 *   <li>Subsystem executes if BOTH conditions are true: if(isEnabled() && isTriggered())</li>
 *   <li>State management follows Enable block settings (held or reset)</li>
 * </ul>
 *
 * <p><b>State Handling:</b></p>
 * <ul>
 *   <li><b>held mode</b>: States freeze at current values when disabled, resume from frozen values when re-enabled</li>
 *   <li><b>reset mode</b>: States reset to initial conditions when re-enabled, previous state values discarded</li>
 * </ul>
 *
 * @author NCSLab Team
 * @version 1.0
 * @since 2025-01-03
 */
public class EnabledAndTriggeredSubsystem extends Subsystem {

    // === SIMULINK-Compatible Parameters ===
    @Getter
    private final Parameter statesWhenEnabling;

    @Getter
    private final Parameter triggerType;

    @Getter
    private final Parameter showEnablePort;

    @Getter
    private final Parameter showTriggerPort;

    @Getter
    private final Parameter zeroCross;

    // === Internal Control Blocks ===
    @Getter
    private Enable enableBlock;

    @Getter
    private Trigger triggerBlock;

    // === Static Parameter Definitions ===
    public static final Map<String, String> PARAMETER_DEFAULTS;
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("StatesWhenEnabling", "held");
        PARAMETER_DEFAULTS.put("TriggerType", "rising");
        PARAMETER_DEFAULTS.put("ShowEnablePort", "on");
        PARAMETER_DEFAULTS.put("ShowTriggerPort", "on");
        PARAMETER_DEFAULTS.put("ZeroCross", "off");
    }

    public static final List<String> outputNames = new ArrayList<>();
    public static final List<String> inputNames = new ArrayList<>();

    static {
        // Port names for enable and trigger inputs
        inputNames.add("enable");
        inputNames.add("trigger");
        // Data ports inherited from Subsystem's In/Out blocks
    }

    // === Legacy Constructor (Deprecated) ===
    /**
     * Legacy constructor for backward compatibility with JSONObject-based initialization.
     *
     * @deprecated Use DTO-based constructor {@link #EnabledAndTriggeredSubsystem(EnabledAndTriggeredSubsystemDto, NCSLabModel)} instead.
     * @param blockJSON JSON object containing block configuration
     * @param model Parent model
     */
    @Deprecated
    @SuppressWarnings("deprecation")
    public EnabledAndTriggeredSubsystem(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);

        // Create SIMULINK parameters with defaults
        this.statesWhenEnabling = getParameterByName("StatesWhenEnabling");
        this.triggerType = getParameterByName("TriggerType");
        this.showEnablePort = getParameterByName("ShowEnablePort");
        this.showTriggerPort = getParameterByName("ShowTriggerPort");
        this.zeroCross = getParameterByName("ZeroCross");

        // FIXME: Internal block creation deferred to execution phase for proper initialization order
        // Create internal Enable and Trigger blocks
        // createEnableBlock();
        // createTriggerBlock();
    }

    /**
     * DTO-NATIVE Constructor - Creates EnabledAndTriggeredSubsystem block directly from DTO.
     * This is the preferred constructor following the DTO-first approach.
     *
     * @param blockDto EnabledAndTriggeredSubsystem block DTO with validated parameters
     * @param model Parent model
     */
    public EnabledAndTriggeredSubsystem(BlockDto blockDto, NCSLabModel model) {
        super(blockDto, model);

        // Initialize final parameters from DTO
        this.statesWhenEnabling = getParameterByName("StatesWhenEnabling");
        this.triggerType = getParameterByName("TriggerType");
        this.showEnablePort = getParameterByName("ShowEnablePort");
        this.showTriggerPort = getParameterByName("ShowTriggerPort");
        this.zeroCross = getParameterByName("ZeroCross");

        // FIXME: Internal block creation deferred to execution phase for proper initialization order
        // Create internal Enable and Trigger blocks
        // createEnableBlock();
        // createTriggerBlock();

        System.out.println("DTO-NATIVE: " + getClass().getSimpleName() +
            " block created successfully - " + blockDto.getBlockName());
    }

    // === Static Factory Method for JSON Deserialization ===
    /**
     * Creates an EnabledAndTriggeredSubsystem block from JSON configuration.
     *
     * @param blockJSON JSON object containing block parameters
     * @param model Parent model
     * @return EnabledAndTriggeredSubsystem block instance
     * @throws BlockCreationException if block creation fails
     */
    public static EnabledAndTriggeredSubsystem fromJSON(JSONObject blockJSON, NCSLabModel model) {
        try {
            String blockName = requireNonEmptyString(blockJSON, "blockName");
            String blockPath = requireNonEmptyString(blockJSON, "blockPath");
            String blockUUID = blockJSON.optString("blockUUID", "null");
            JSONObject paramValues = blockJSON.optJSONObject("paramValues");

            if (paramValues == null) {
                paramValues = new JSONObject();
            }

            // Create block using legacy constructor
            JSONObject identity = createBlockIdentity(blockName, blockPath, blockUUID);
            identity.put("paramValues", paramValues);

            return new EnabledAndTriggeredSubsystem(identity, model);

        } catch (Exception e) {
            throw new BlockCreationException("Failed to create EnabledAndTriggeredSubsystem block from JSON: " +
                e.getMessage(), e);
        }
    }

    // === Static Factory Method for Programmatic Creation (DTO-Based) ===
    /**
     * Creates an EnabledAndTriggeredSubsystem block with default parameters.
     *
     * @param name Block name
     * @param path Block path
     * @param model Parent model
     * @return EnabledAndTriggeredSubsystem block instance
     */
    public static EnabledAndTriggeredSubsystem create(String name, String path, NCSLabModel model) {
        return create(name, path, "held", "rising", "on", "on", "off", model);
    }

    /**
     * Creates an EnabledAndTriggeredSubsystem block with full parameters using DTO-based approach.
     *
     * @param name Block name
     * @param path Block path
     * @param statesWhenEnabling State handling mode ("held" or "reset")
     * @param triggerType Trigger type ("rising", "falling", "either", "function-call")
     * @param showEnablePort Enable port visibility ("on" or "off")
     * @param showTriggerPort Trigger port visibility ("on" or "off")
     * @param zeroCross Zero-crossing detection ("on" or "off")
     * @param model Parent model
     * @return EnabledAndTriggeredSubsystem block instance
     */
    public static EnabledAndTriggeredSubsystem create(String name, String path, String statesWhenEnabling,
                                                     String triggerType, String showEnablePort,
                                                     String showTriggerPort, String zeroCross,
                                                     NCSLabModel model) {
        // Build DTO using constructor with individual parameters
        EnabledAndTriggeredSubsystemDto dto = new EnabledAndTriggeredSubsystemDto(
            name,
            path,
            com.ncslab.dto.common.TypedParameter.of(statesWhenEnabling),
            com.ncslab.dto.common.TypedParameter.of(triggerType),
            com.ncslab.dto.common.TypedParameter.of(showEnablePort),
            com.ncslab.dto.common.TypedParameter.of(showTriggerPort),
            com.ncslab.dto.common.TypedParameter.of(zeroCross)
        );

        // Set blockUUID
        dto.setBlockUUID("null");

        // Validate DTO (automatic validation)
        if (!dto.isValid()) {
            throw new IllegalArgumentException("Invalid EnabledAndTriggeredSubsystem parameters: " +
                dto.getValidationErrors());
        }

        // Use DTO constructor (clean, no JSONObject workarounds needed!)
        return new EnabledAndTriggeredSubsystem(dto, model);
    }

    // === Internal Block Creation ===
    /**
     * Creates and configures the internal Enable block.
     * The Enable block controls whether the subsystem is enabled based on the enable signal.
     */
    // private void createEnableBlock() {
    //     try {
    //         // Create Enable block using DTO-based approach
    //         String enableBlockName = getBlockName() + "_Enable";
    //         String enableBlockPath = getFullPath();

    //         enableBlock = Enable.create(
    //             enableBlockName,
    //             enableBlockPath,
    //             getStatesWhenEnablingValue(),
    //             "off", // No output port needed for internal enable
    //             getZeroCrossValue(),
    //             -1.0, // Inherited sample time
    //             getModel()
    //         );

    //         // Link enable block to this subsystem
    //         enableBlock.setSubsystem(this);

    //         // Add to inner system for proper management
    //         getInnerSystem().addBlock(enableBlock);

    //     } catch (Exception e) {
    //         System.err.println("Failed to create Enable block: " + e.getMessage());
    //         e.printStackTrace();
    //     }
    // }

    /**
     * Creates and configures the internal Trigger block.
     * The Trigger block detects trigger events based on the trigger signal.
     */
    // private void createTriggerBlock() {
    //     try {
    //         // Create Trigger block using DTO-based approach
    //         String triggerBlockName = getBlockName() + "_Trigger";
    //         String triggerBlockPath = getFullPath();

    //         triggerBlock = Trigger.create(
    //             triggerBlockName,
    //             triggerBlockPath,
    //             getTriggerTypeValue(),
    //             false, // No output port needed for internal trigger
    //             isZeroCrossEnabled(),
    //             -1.0, // Inherited sample time
    //             "auto",
    //             getModel()
    //         );

    //         // Link trigger block to this subsystem
    //         triggerBlock.setSubsystem(this);

    //         // Add to inner system for proper management
    //         getInnerSystem().addBlock(triggerBlock);

    //     } catch (Exception e) {
    //         System.err.println("Failed to create Trigger block: " + e.getMessage());
    //         e.printStackTrace();
    //     }
    // }

    // === Code Generation Methods ===
    /**
     * Generates C code for the EnabledAndTriggeredSubsystem using Velocity templates.
     *
     * @param code Code structure for C code generation
     */
    @Override
    public void generateOutputCodeC(CodeStructC code) {
        // First generate base subsystem code
        super.generateOutputCodeC(code);

        // Populate template context with EnabledAndTriggeredSubsystem-specific data
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        // Add EnabledAndTriggeredSubsystem-specific context
        context.put("statesWhenEnabling", getStatesWhenEnablingValue());
        context.put("triggerType", getTriggerTypeValue());
        context.put("showEnablePort", isEnablePortShown());
        context.put("showTriggerPort", isTriggerPortShown());
        context.put("zeroCross", isZeroCrossEnabled());
        context.put("isHeldMode", isHeldMode());
        context.put("isResetMode", isResetMode());
        context.put("enableBlock", enableBlock);
        context.put("triggerBlock", triggerBlock);

        // Generate EnabledAndTriggeredSubsystem wrapper code using template
        String outputCode = TemplateManager.renderTemplate(
            "c/subsystem/EnabledAndTriggeredSubsystem/output.vm", context);
        code.addOutputCode(outputCode);

        // Generate code for Enable and Trigger blocks
        if (enableBlock != null) {
            enableBlock.generateOutputCodeC(code);
        }
        if (triggerBlock != null) {
            triggerBlock.generateOutputCodeC(code);
        }
    }

    /**
     * Generates array declarations for state variables.
     */
    @Override
    public void generateArraysCodeC(CodeStructC code) {
        super.generateArraysCodeC(code);

        try {
            com.ncslab.util.TemplateUtils.populateAllContext(context, this);
            String codeStr = TemplateManager.renderTemplate(
                "c/subsystem/EnabledAndTriggeredSubsystem/arrays.vm", context);
            code.addArraysCode(codeStr);

            // Generate arrays code for Enable and Trigger blocks
            if (enableBlock != null) {
                enableBlock.generateArraysCodeC(code);
            }
            if (triggerBlock != null) {
                triggerBlock.generateArraysCodeC(code);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Generates initialization code.
     */
    @Override
    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);

        try {
            com.ncslab.util.TemplateUtils.populateAllContext(context, this);
            String templatePath = "c/subsystem/EnabledAndTriggeredSubsystem/init.vm";
            String codeStr = TemplateManager.renderTemplate(templatePath, context);
            code.addInitCode(codeStr);

            // Generate init code for Enable and Trigger blocks
            if (enableBlock != null) {
                enableBlock.generateInitCodeC(code);
            }
            if (triggerBlock != null) {
                triggerBlock.generateInitCodeC(code);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // === Execution Methods ===
    /**
     * Initializes the EnabledAndTriggeredSubsystem and its internal blocks.
     * Calls initialization for both Enable and Trigger blocks.
     */
    @Override
    public void calculateInit() {
        // Initialize Enable and Trigger blocks first
        if (enableBlock != null) {
            enableBlock.calculateInit();
        }
        if (triggerBlock != null) {
            triggerBlock.calculateInit();
        }

        // Initialize subsystem if initially enabled AND triggered
        if (isEnabled() && isTriggered()) {
            super.calculateInit();
        }
    }

    /**
     * Calculates output and manages subsystem execution based on enable and trigger conditions.
     * Subsystem executes ONLY when BOTH enable signal is active AND trigger event occurred.
     *
     * @param t Current simulation time
     */
    @Override
    public void calculateOutput(double t) {
        // Update Enable block state
        if (enableBlock != null) {
            enableBlock.calculateOutput(t);
        }

        // Update Trigger block state and detect edges
        if (triggerBlock != null) {
            triggerBlock.calculateOutput(t);
        }

        // Execute subsystem ONLY if BOTH enabled AND triggered
        if (isEnabled() && isTriggered()) {
            // Handle state reset if transitioning from disabled to enabled in reset mode
            if (isResetMode() && enableBlock != null && !enableBlock.isEnabled()) {
                super.calculateInit();
            }

            // Execute subsystem
            super.calculateOutput(t);
        }
        // If not enabled or not triggered, states are held (in held mode) or will reset on next enable (in reset mode)
    }

    // === Dimension Handling ===
    @Override
    public void updateDimension() throws MatDimException {
        // Update dimensions for Enable and Trigger blocks
        if (enableBlock != null) {
            enableBlock.updateDimension();
        }
        if (triggerBlock != null) {
            triggerBlock.updateDimension();
        }

        // Update subsystem dimensions
        super.updateDimension();
    }

    @Override
    public void checkDimension() throws MatDimException {
        // Check dimensions for Enable and Trigger blocks
        if (enableBlock != null) {
            enableBlock.checkDimension();
        }
        if (triggerBlock != null) {
            triggerBlock.checkDimension();
        }

        // Check subsystem dimensions
        super.checkDimension();
    }

    // === Helper Methods ===
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
        identity.put("blockType", "EnabledAndTriggeredSubsystem");
        identity.put("blockName", blockName);
        identity.put("blockPath", blockPath);
        identity.put("blockUUID", blockUUID);
        identity.put("paramValues", new JSONObject());
        return identity;
    }

    // === Accessor Methods ===
    /**
     * Gets the state handling mode value.
     *
     * @return "held" or "reset"
     */
    public String getStatesWhenEnablingValue() {
        return statesWhenEnabling.getData().getInitString();
    }

    /**
     * Gets the trigger type value.
     *
     * @return "rising", "falling", "either", or "function-call"
     */
    public String getTriggerTypeValue() {
        return triggerType.getData().getInitString();
    }

    /**
     * Checks if states are held when disabled.
     *
     * @return true if mode is "held"
     */
    public boolean isHeldMode() {
        return "held".equalsIgnoreCase(getStatesWhenEnablingValue());
    }

    /**
     * Checks if states reset when re-enabled.
     *
     * @return true if mode is "reset"
     */
    public boolean isResetMode() {
        return "reset".equalsIgnoreCase(getStatesWhenEnablingValue());
    }

    /**
     * Checks if enable port is shown.
     *
     * @return true if ShowEnablePort="on"
     */
    public boolean isEnablePortShown() {
        String value = showEnablePort.getData().getInitString();
        return "on".equalsIgnoreCase(value);
    }

    /**
     * Checks if trigger port is shown.
     *
     * @return true if ShowTriggerPort="on"
     */
    public boolean isTriggerPortShown() {
        String value = showTriggerPort.getData().getInitString();
        return "on".equalsIgnoreCase(value);
    }

    /**
     * Checks if zero-crossing detection is enabled.
     *
     * @return true if ZeroCross="on"
     */
    public boolean isZeroCrossEnabled() {
        String value = zeroCross.getData().getInitString();
        return "on".equalsIgnoreCase(value);
    }

    /**
     * Gets the zero-crossing detection value.
     *
     * @return "on" or "off"
     */
    public String getZeroCrossValue() {
        return zeroCross.getData().getInitString();
    }

    /**
     * Checks if the subsystem is currently enabled (enable signal > 0).
     *
     * @return true if subsystem is enabled
     */
    public boolean isEnabled() {
        return enableBlock != null && enableBlock.isCurrentlyEnabled();
    }

    /**
     * Checks if a trigger event has occurred in the current time step.
     *
     * @return true if trigger event detected
     */
    public boolean isTriggered() {
        return triggerBlock != null && triggerBlock.isTriggered();
    }

    // === Cleanup ===
    @Override
    public void cleanup() {
        // Clean up Enable and Trigger blocks
        if (enableBlock != null) {
            getInnerSystem().removeBlock(enableBlock);
            enableBlock = null;
        }
        if (triggerBlock != null) {
            getInnerSystem().removeBlock(triggerBlock);
            triggerBlock = null;
        }

        // Clean up base subsystem
        super.cleanup();
    }
}
