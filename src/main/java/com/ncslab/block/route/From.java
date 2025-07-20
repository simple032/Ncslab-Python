package com.ncslab.block.route;

import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.BlockCreationException;
import com.ncslab.ncslablink.MatDimException;
import lombok.Getter;
import org.json.JSONObject;

import com.ncslab.block.Block;
import com.ncslab.block.io.OutputPort;
import com.ncslab.ncslablink.NCSLabModel;

import java.util.Objects;
import java.util.Vector;
import java.util.Map;
import java.util.HashMap;

/**
 * From block with SIMULINK-compatible parameters and type-safe constructors.
 *
 * SIMULINK Parameters:
 * - GotoTag: Tag name for the Goto/From pair
 * - IconDisplay: Icon display mode
 * - SampleTime: Sample time for discrete operation (-1 for inherited, 0 for continuous)
 * - OutDataTypeStr: Output data type specification
 * - SaturateOnIntegerOverflow: Handle integer overflow
 */
public class From extends Block {
    @Getter
    private String tagName;

    // === SIMULINK-Compatible Parameters ===
    private final Parameter gotoTag;
    private final Parameter iconDisplay;
    private final Parameter sampleTime;
    private final Parameter outDataType;
    private final Parameter saturateOnIntegerOverflow;

    // === Static Parameter Definitions ===

    @Getter
    public static final Vector<String> outputNames = new Vector<>();
    @Getter
    public static final Vector<String> inputNames = new Vector<>();

    static {
        // SIMULINK parameter names

        // Port names
        outputNames.add("out1");
        inputNames.add("in1");
    }

    // === Parameter Defaults ===
    @Getter
    public static final Map<String, String> PARAMETER_DEFAULTS = new HashMap<>();
    static {
        PARAMETER_DEFAULTS.put("GotoTag", "tag1");
        PARAMETER_DEFAULTS.put("IconDisplay", "Tag");
        PARAMETER_DEFAULTS.put("SampleTime", "-1");
        PARAMETER_DEFAULTS.put("OutDataTypeStr", "Inherit: auto");
        PARAMETER_DEFAULTS.put("SaturateOnIntegerOverflow", "off");
    }

    // === Private Constructor with Typed Parameters ===
    private From(Parameter gotoTag, Parameter iconDisplay, Parameter sampleTime,
                Parameter outDataType, Parameter saturateOnIntegerOverflow,
                String blockName, String blockPath, String blockUUID, NCSLabModel model) {
        super(createBlockIdentity(blockName, blockPath, blockUUID), model);

        // Assign parameters
        this.gotoTag = Objects.requireNonNull(gotoTag, "Goto tag parameter cannot be null");
        this.iconDisplay = Objects.requireNonNull(iconDisplay, "Icon display parameter cannot be null");
        this.sampleTime = Objects.requireNonNull(sampleTime, "Sample time parameter cannot be null");
        this.outDataType = Objects.requireNonNull(outDataType, "Output data type parameter cannot be null");
        this.saturateOnIntegerOverflow = Objects.requireNonNull(saturateOnIntegerOverflow, "Saturate parameter cannot be null");
        // Store tag name for easy access
        this.tagName = gotoTag.getInitString();

        // Create ports
        inputPortList.add(new InputPort(this, 1));
        outputPortList.add(new OutputPort(this, 1, true));
    }

    // === Legacy Constructor (Deprecated) ===
    @Deprecated
	public From(JSONObject blockIn, NCSLabModel model) {
		super(blockIn, model);

        // Create legacy parameters for backward compatibility
        this.gotoTag = new Parameter(this, 1, "GotoTag", paramValues.getString("GotoTag"));
        this.iconDisplay = new Parameter(this, 2, "IconDisplay", "Tag");
        this.sampleTime = new Parameter(this, 3, "SampleTime", "-1");
        this.outDataType = new Parameter(this, 4, "OutDataTypeStr", "Inherit: auto");
        this.saturateOnIntegerOverflow = new Parameter(this, 5, "SaturateOnIntegerOverflow", "off");

        // Add all parameters to parameter list

        // Store tag name for easy access
        this.tagName = paramValues.getString("GotoTag");

        // Create ports
        inputPortList.add(new InputPort(this, 1));
        outputPortList.add(new OutputPort(this, 1, true));
	}

    // === Static Factory Method for JSON Deserialization ===
    public static From fromJSON(JSONObject blockJSON, NCSLabModel model) {
        try {
            String blockName = requireNonEmptyString(blockJSON, "blockName");
            String blockPath = requireNonEmptyString(blockJSON, "blockPath");
            String blockUUID = blockJSON.optString("blockUUID", "null");
            JSONObject paramValues = blockJSON.optJSONObject("paramValues");

            if (paramValues == null) {
                paramValues = new JSONObject();
            }

            Parameter gotoTag = createGotoTagFromJSON(paramValues, blockName);
            Parameter iconDisplay = createIconDisplayFromJSON(paramValues, blockName);
            Parameter sampleTime = createSampleTimeFromJSON(paramValues, blockName);
            Parameter outDataType = createOutDataTypeFromJSON(paramValues, blockName);
            Parameter saturateParam = createSaturateFromJSON(paramValues, blockName);

            From block = new From(gotoTag, iconDisplay, sampleTime, outDataType, saturateParam,
                                 blockName, blockPath, blockUUID, model);

            setParameterBlockReference(block, gotoTag, iconDisplay, sampleTime, outDataType, saturateParam);

            return block;

        } catch (Exception e) {
            throw new BlockCreationException("Failed to create From block from JSON: " + e.getMessage(), e);
        }
    }

    // === Static Factory Method for Programmatic Creation ===
    public static From create(String name, String path, String gotoTag, NCSLabModel model) {
        return create(name, path, gotoTag, "Tag", -1.0, "Inherit: auto", false, model);
    }

    public static From create(String name, String path, String gotoTag, String iconDisplay, double sampleTime,
                             String outDataType, boolean saturateOnOverflow, NCSLabModel model) {
        Parameter gotoTagParam = new Parameter(null, 1, "GotoTag", gotoTag);
        Parameter iconDisplayParam = new Parameter(null, 2, "IconDisplay", iconDisplay);
        Parameter sampleTimeParam = new Parameter(null, 3, "SampleTime", String.valueOf(sampleTime));
        Parameter outDataTypeParam = new Parameter(null, 4, "OutDataTypeStr", outDataType);
        Parameter saturateParam = new Parameter(null, 5, "SaturateOnIntegerOverflow", saturateOnOverflow ? "on" : "off");

        From block = new From(gotoTagParam, iconDisplayParam, sampleTimeParam, outDataTypeParam, saturateParam,
                             name, path, "null", model);

        setParameterBlockReference(block, gotoTagParam, iconDisplayParam, sampleTimeParam, outDataTypeParam, saturateParam);

        return block;
    }

    // === Helper Methods for JSON Parameter Creation ===
    private static Parameter createGotoTagFromJSON(JSONObject paramValues, String blockName) {
        String gotoTagValue = paramValues.optString("GotoTag", "Tag");
        return new Parameter(null, 1, "GotoTag", gotoTagValue);
    }

    private static Parameter createIconDisplayFromJSON(JSONObject paramValues, String blockName) {
        String iconDisplayValue = paramValues.optString("IconDisplay", "Tag");
        return new Parameter(null, 2, "IconDisplay", iconDisplayValue);
    }

    private static Parameter createSampleTimeFromJSON(JSONObject paramValues, String blockName) {
        String sampleTimeValue = paramValues.optString("SampleTime", "-1");
        return new Parameter(null, 3, "SampleTime", sampleTimeValue);
    }

    private static Parameter createOutDataTypeFromJSON(JSONObject paramValues, String blockName) {
        String outDataTypeValue = paramValues.optString("OutDataTypeStr", "Inherit: auto");
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

    private static void setParameterBlockReference(From block, Parameter... parameters) {
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
        identity.put("blockType", "From");
        identity.put("blockName", blockName);
        identity.put("blockPath", blockPath);
        identity.put("blockUUID", blockUUID);
        return identity;
    }

    public void generateOutputCodeC(CodeStructC code) {
        super.generateOutputCodeC(code);

        context.put("block", this);
        context.put("gotoTag", this.gotoTag);
        context.put("iconDisplay", this.iconDisplay);
        context.put("sampleTime", this.sampleTime);
        context.put("outDataType", this.outDataType);
        context.put("saturateOnIntegerOverflow", this.saturateOnIntegerOverflow);

        String codeStr = com.ncslab.util.TemplateManager.renderTemplate("c/route/From/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void updateDimension() throws MatDimException {
        OutputPort out  = outputPortList.get(0);
        InputPort in  = inputPortList.get(0);
        OutputSignal signal=in.getLinkedLine().getLinkedOutputPort().getOutputSignalC();
        out.setHeight(signal.getHeight());
        out.setWidth(signal.getWidth());
        out.getOutputSignalC().setHeight(signal.getHeight());
        out.getOutputSignalC().setWidth(signal.getWidth());
        out.getOutputSignalC().setDataType(signal.getDataType());
    }
    public void checkDimension() throws MatDimException{
    }

    @Override
    public void calculateOutput(double t) {
        InputPort in = inputPortList.get(0);
        OutputPort out = outputPortList.get(0);
        out.setData(in.getData());
    }

    @Override
    public void calculateInit() {
        InputPort in = inputPortList.get(0);
        OutputPort out = outputPortList.get(0);
        out.setData(in.getData());
    }
}
