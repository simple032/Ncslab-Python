package com.ncslab.block.discrete;

import com.ncslab.block.io.Parameter;
import com.ncslab.util.TemplateManager;
import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.discrete.Discrete_Transfer_FcnzDto;

import com.ncslab.block.data.DataType;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.BlockCreationException;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.ArrayList;
import java.util.List;
import com.ncslab.block.data.Data;
import com.ncslab.block.io.State;
import Jama.Matrix;

/**
 * Discrete_Transfer_Fcnz block with SIMULINK-compatible parameters and type-safe constructors.
 *
 * SIMULINK Parameters:
 * - SampleTime: Sample time for discrete operation
 * - OutDataTypeStr: Output data type specification
 * - SaturateOnIntegerOverflow: Handle integer overflow
 */
public class Discrete_Transfer_Fcnz extends DiscreteBlock{

    // === SIMULINK-Compatible Parameters ===
    private final Parameter sampleTimeParam;
    private final Parameter outDataType;
    private final Parameter saturateOnIntegerOverflow;

    // === Internal state ===
    private final boolean feedthrough = true; // Transfer function blocks have feedthrough
    private State[] xStates;  // State variables for the transfer function
    private State[] uStates;  // Previous input values for the transfer function
    private int numOrder = 0;  // Numerator order
    private int denOrder = 0;  // Denominator order

    // === Static Parameter Definitions ===

    // Parameter defaults matching database format
    public static final Map<String, String> PARAMETER_DEFAULTS;

    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("SampleTime", "-1");  // Inherited
        PARAMETER_DEFAULTS.put("OutDataTypeStr", "Inherit: Same as input");
        PARAMETER_DEFAULTS.put("SaturateOnIntegerOverflow", "off");
    }
    public static final List<String> outputNames = new ArrayList<>();
    public static final List<String> inputNames = new ArrayList<>();

    static {
        // SIMULINK parameter names

        // Port names
        outputNames.add("out1");
        inputNames.add("in1");
        inputNames.add("in2");
        inputNames.add("in3");
    }

    // === Private Constructor with Typed Parameters ===
    private Discrete_Transfer_Fcnz(Parameter sampleTime, Parameter outDataType, Parameter saturateOnIntegerOverflow,
                                   String blockName, String blockPath, String blockUUID, NCSLabModel model) {
        super(createBlockIdentity(blockName, blockPath, blockUUID), model);

        // Assign parameters
        this.sampleTimeParam = Objects.requireNonNull(sampleTime, "Sample time parameter cannot be null");
        this.outDataType = Objects.requireNonNull(outDataType, "Output data type parameter cannot be null");
        this.saturateOnIntegerOverflow = Objects.requireNonNull(saturateOnIntegerOverflow, "Saturate parameter cannot be null");
        // Create ports
        inputPortList.add(new InputPort(this, 1));  // Input signal
        inputPortList.add(new InputPort(this, 2));  // Numerator coefficients
        inputPortList.add(new InputPort(this, 3));  // Denominator coefficients
        outputPortList.add(new OutputPort(this, 1, feedthrough));

        setSampleTime(sampleTimeParam);
    }

    // === Legacy Constructor (Deprecated) ===
    @Deprecated
	public Discrete_Transfer_Fcnz(JSONObject blockIn, NCSLabModel model) {
		super(blockIn, model);

        // Create legacy parameters for backward compatibility
        this.sampleTimeParam = getParameterByName("SampleTime");

        // Create missing SIMULINK parameters with defaults
        this.outDataType = getParameterByName("OutDataTypeStr");
        this.saturateOnIntegerOverflow = getParameterByName("SaturateOnIntegerOverflow");

        // Add all parameters to parameter list

        // Create ports
		inputPortList.add(new InputPort(this, 1));  // Input signal
		inputPortList.add(new InputPort(this, 2));  // Numerator coefficients
		inputPortList.add(new InputPort(this, 3));  // Denominator coefficients
		outputPortList.add(new OutputPort(this, 1, feedthrough));

        setSampleTime(sampleTimeParam);
    }    /**
     * DTO-NATIVE Constructor - Creates Discrete_Transfer_Fcnz block directly from BlockDto DTO
     */
    public Discrete_Transfer_Fcnz(BlockDto blockDto, NCSLabModel model) {
        super(blockDto, model);

        // Initialize final parameters from DTO
        this.sampleTimeParam = getParameterByName("SampleTime");
        this.outDataType = getParameterByName("OutDataTypeStr");
        this.saturateOnIntegerOverflow = getParameterByName("SaturateOnIntegerOverflow");

        initializePorts();

        setSampleTime(sampleTimeParam);

        System.out.println("DTO-NATIVE: " + getClass().getSimpleName() + " block created successfully - " + blockDto.getBlockName());
    }

    // ================== 添加此 DTO 专用构造函数 ==================
    /**
     * DTO-SPECIALIZED Constructor - Required by OptimizedBlockFactory for Discrete_Transfer_FcnzDto
     */
    public Discrete_Transfer_Fcnz(Discrete_Transfer_FcnzDto dto, NCSLabModel model) {
        super(dto, model);

        // 初始化参数 (复用现有逻辑)
        this.sampleTimeParam = getParameterByName("SampleTime");
        this.outDataType = getParameterByName("OutDataTypeStr");
        this.saturateOnIntegerOverflow = getParameterByName("SaturateOnIntegerOverflow");

        // 初始化端口
        initializePorts();

        // 设置采样时间 (离散模块必须步骤)
        setSampleTime(sampleTimeParam);

        System.out.println("DTO-SPECIALIZED: Discrete_Transfer_Fcnz block created from Discrete_Transfer_FcnzDto - " + dto.getBlockName());
    }
    // ===========================================================

    private void initializePorts() {
        inputPortList.add(new InputPort(this, 1));  // Input signal
        inputPortList.add(new InputPort(this, 2));  // Numerator coefficients
        inputPortList.add(new InputPort(this, 3));  // Denominator coefficients
        outputPortList.add(new OutputPort(this, 1, feedthrough));
    }

    // === Static Factory Method for JSON Deserialization ===
    public static Discrete_Transfer_Fcnz fromJSON(JSONObject blockJSON, NCSLabModel model) {
        try {
            String blockName = requireNonEmptyString(blockJSON, "blockName");
            String blockPath = requireNonEmptyString(blockJSON, "blockPath");
            String blockUUID = blockJSON.optString("blockUUID", "null");
            JSONObject paramValues = blockJSON.optJSONObject("paramValues");

            if (paramValues == null) {
                paramValues = new JSONObject();
            }
            Parameter sampleTime = createSampleTimeFromJSON(paramValues, blockName);
            Parameter outDataType = createOutDataTypeFromJSON(paramValues, blockName);
            Parameter saturateParam = createSaturateFromJSON(paramValues, blockName);

            Discrete_Transfer_Fcnz block = new Discrete_Transfer_Fcnz(sampleTime, outDataType, saturateParam,
                                                                     blockName, blockPath, blockUUID, model);

            setParameterBlockReference(block, sampleTime, outDataType, saturateParam);

            return block;

        } catch (Exception e) {
            throw new BlockCreationException("Failed to create Discrete_Transfer_Fcnz block from JSON: " + e.getMessage(), e);
        }
    }

    // === Static Factory Method for Programmatic Creation (DTO-Based) ===
    public static Discrete_Transfer_Fcnz create(String name, String path, double sampleTime, NCSLabModel model) {
        return create(name, path, sampleTime, "Inherit: Same as input", false, model);
    }

    /**
     * Create a Discrete_Transfer_Fcnz block with full parameters (DTO-based approach).
     *
     * This modern implementation uses DTOs instead of Parameter manipulation,
     * providing type safety, automatic validation, and cleaner code.
     *
     * @param name Block name
     * @param path Block path
     * @param sampleTime Sample time for discrete operation
     * @param outDataType Output data type specification
     * @param saturateOnOverflow Handle integer overflow
     * @param model Parent model
     * @return Discrete_Transfer_Fcnz block instance
     */
    public static Discrete_Transfer_Fcnz create(String name, String path, double sampleTime, String outDataType,
                                               boolean saturateOnOverflow, NCSLabModel model) {
        // Build DTO using type-safe builder pattern
        Discrete_Transfer_FcnzDto dto = Discrete_Transfer_FcnzDto.builder()
            .blockName(name)
            .blockPath(path)
            .blockUUID("null")
            .sampleTime(com.ncslab.dto.common.TypedParameter.of(sampleTime))
            .outDataTypeStr(com.ncslab.dto.common.TypedParameter.of(outDataType))
            .saturateOnIntegerOverflow(com.ncslab.dto.common.TypedParameter.of(saturateOnOverflow))
            .build();

        // Validate DTO (automatic validation)
        com.ncslab.dto.mapper.validation.ValidationResult validation = dto.validate();
        if (!validation.isValid()) {
            throw new IllegalArgumentException("Invalid Discrete_Transfer_Fcnz parameters: " + validation.getErrors());
        }

        // Use DTO constructor (clean, no JSONObject workarounds needed!)
        return new Discrete_Transfer_Fcnz(dto, model);
    }

    // === Helper Methods for JSON Parameter Creation ===
    private static Parameter createSampleTimeFromJSON(JSONObject paramValues, String blockName) {
        String sampleTimeValue = String.valueOf(paramValues.optDouble("SampleTime", -1.0));
        return new Parameter(null, 1, "SampleTime", sampleTimeValue);
    }

    private static Parameter createOutDataTypeFromJSON(JSONObject paramValues, String blockName) {
        String outDataTypeValue = paramValues.optString("OutDataTypeStr", "Inherit: Same as input");
        return new Parameter(null, 2, "OutDataTypeStr", outDataTypeValue);
    }

    private static Parameter createSaturateFromJSON(JSONObject paramValues, String blockName) {
        String saturateValue = paramValues.optString("SaturateOnIntegerOverflow", "off");
        return new Parameter(null, 3, "SaturateOnIntegerOverflow", saturateValue);
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

    private static void setParameterBlockReference(Discrete_Transfer_Fcnz block, Parameter... parameters) {
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
        identity.put("blockType", "Discrete_Transfer_Fcnz");
        identity.put("blockName", blockName);
        identity.put("blockPath", blockPath);
        identity.put("blockUUID", blockUUID);
        return identity;
    }
	//define arrays to save data
    public void generateArraysCodeC(CodeStructC code) {
        // Populate all standard template variables first
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        OutputSignal signal1 = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
        OutputSignal signal3 = inputPortList.get(2).getLinkedLine().getLinkedOutputPort().getOutputSignalC();

        context.put("block", this);
        context.put("signal1", signal1);
        context.put("signal3", signal3);

        // Add dimension variables needed by templates
        context.put("signal1Height", signal1.getHeight());
        context.put("signal1Width", signal1.getWidth());
        context.put("stateNum", signal3.getWidth() - 1);  // Number of states

        String arraysCode = TemplateManager.renderTemplate("c/discrete/Discrete_Transfer_Fcnz/arrays.vm", context);
        code.addArraysCode(arraysCode);
    }

    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);
        OutputSignal signal1 = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
        // Populate all standard template variables first
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        context.put("block", this);
        context.put("sampleTime", sampleTimeParam);
        // sampleTimeName is already set by TemplateUtils.populateAllContext() with correct prefix
        context.put("realDataType", com.ncslab.block.data.DataType.REAL);
        context.put("signal1Height", signal1.getHeight());
        context.put("signal1Width", signal1.getWidth());

        // Add signal variables if ports are connected
        if (inputPortList.size() >= 3) {
            if (inputPortList.get(1).getLinkedLine() != null &&
                inputPortList.get(1).getLinkedLine().getLinkedOutputPort() != null) {
                OutputSignal signal2 = inputPortList.get(1).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
                // FIXED: signal2.getName() already includes Block prefix, don't add another
                context.put("signal2Name", signal2.getName()); // Already has Block{id}_Output{port} format
                context.put("signal2DataType", signal2.getDataType());
                context.put("signal2", signal2.getName());
                // Provide pre-constructed variable names with _REAL suffix for template
                context.put("signal2NameREAL", signal2.getName() + "_REAL");

            }
            if (inputPortList.get(2).getLinkedLine() != null &&
                inputPortList.get(2).getLinkedLine().getLinkedOutputPort() != null) {
                OutputSignal signal3 = inputPortList.get(2).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
                // FIXED: signal3.getName() already includes Block prefix, don't add another
                context.put("signal3Name", signal3.getName()); // Already has Block{id}_Output{port} format
                context.put("signal3DataType", signal3.getDataType());
                context.put("signal3", signal3.getName());
                // Provide pre-constructed variable names with _REAL suffix for template
                context.put("signal3NameREAL", signal3.getName() + "_REAL");

                // Add dimension variables needed by templates
                // Get vector lengths (works for both row and column vectors)

                int denLength = Math.max(signal3.getWidth(), signal3.getHeight());
                context.put("stateDim", denLength - 1);  // Denominator order - 1
                context.put("stateNum", denLength - 1);  // Number of states
            }
        }


        String initCode = TemplateManager.renderTemplate("c/discrete/Discrete_Transfer_Fcnz/init.vm", context);
        code.addInitCode(initCode);
    }


    public void generateOutputCodeC(CodeStructC code) {
        // Populate all standard template variables first
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        OutputPort out = outputPortList.get(0);
        OutputPort ops = inputPortList.get(0).getLinkedLine().getLinkedOutputPort();
        OutputSignal signal1 = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
        OutputSignal signal2 = inputPortList.get(1).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
        OutputSignal signal3 = inputPortList.get(2).getLinkedLine().getLinkedOutputPort().getOutputSignalC();

        context.put("block", this);
        context.put("inputPortList", inputPortList);
        context.put("realDataType", DataType.REAL);
        context.put("matrixDataType", DataType.MATRIX);
        context.put("signal1", signal1);
        context.put("signal2", signal2);
        context.put("signal3", signal3);

        // Add dimension variables needed by templates
        // Get vector lengths (works for both row and column vectors)
        int numLength = Math.max(signal2.getWidth(), signal2.getHeight());
        int denLength = Math.max(signal3.getWidth(), signal3.getHeight());

        context.put("signal1Height", signal1.getHeight());
        context.put("signal1Width", signal1.getWidth());
        context.put("s2Width", numLength);  // Numerator vector length (row or column)
        context.put("s3Width", denLength);  // Denominator vector length (row or column)
        context.put("stateDim", denLength - 1);  // Denominator order - 1
        context.put("stateNum", denLength - 1);  // Number of states

        // Add signal names for template
        // FIXED: Signal names already include Block prefix, don't add another
        context.put("signal1Name", signal1.getName()); // Already has Block{id}_Output{port} format
        context.put("signal2Name", signal2.getName()); // Already has Block{id}_Output{port} format
        context.put("signal3Name", signal3.getName()); // Already has Block{id}_Output{port} format
        context.put("signal2NameREAL", signal2.getName() + "_REAL");
        context.put("signal3NameREAL", signal3.getName() + "_REAL");

        // Add signal data types for template conditionals
        context.put("signal1DataType", signal1.getDataType());
        context.put("signal2DataType", signal2.getDataType());
        context.put("signal3DataType", signal3.getDataType());

        String outputCode = TemplateManager.renderTemplate("c/discrete/Discrete_Transfer_Fcnz/output.vm", context);
        code.addOutputCode(outputCode);
    }

	 public void updateDimension() throws MatDimException{
		 super.updateDimension();
			OutputPort out  = outputPortList.get(0);
			OutputSignal signal1=inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
			OutputSignal signal2=inputPortList.get(1).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
			OutputSignal signal3=inputPortList.get(2).getLinkedLine().getLinkedOutputPort().getOutputSignalC();

			// Validate coefficient inputs are vectors (either row [1×n] or column [n×1])
			boolean signal2IsVector = (signal2.getHeight() == 1 || signal2.getWidth() == 1);
			boolean signal3IsVector = (signal3.getHeight() == 1 || signal3.getWidth() == 1);

			if(!signal2IsVector || !signal3IsVector) {
				 MatDimException e=new MatDimException("The input port2 signal and input port3 signal of " +
				     this.blockName + " must be vectors (either row [1×n] or column [n×1])!\n");
				 throw(e);
			}

			// Get vector lengths (works for both row and column vectors)
			int numLength = Math.max(signal2.getHeight(), signal2.getWidth());
			int denLength = Math.max(signal3.getHeight(), signal3.getWidth());

			if(numLength > denLength) {
				MatDimException e=new MatDimException("The order of the denominator must be greater than or equal to the order of the numerator.\n");
				 throw(e);
			}

			out.setHeight(signal1.getHeight());
			out.setWidth(signal1.getWidth());
			out.getOutputSignalC().setHeight(signal1.getHeight());
			out.getOutputSignalC().setWidth(signal1.getWidth());
			out.getOutputSignalC().setDataType(signal1.getDataType());
	}
	 public void checkDimension() throws MatDimException{
		 // No additional dimension checks needed for discrete transfer function
	}

    @Override
    public void calculateInit() {
        // Initialize state arrays based on numerator and denominator orders
        OutputSignal signal2 = inputPortList.get(1).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
        OutputSignal signal3 = inputPortList.get(2).getLinkedLine().getLinkedOutputPort().getOutputSignalC();

        // Get vector lengths (works for both row and column vectors)
        int numLength = Math.max(signal2.getWidth(), signal2.getHeight());
        int denLength = Math.max(signal3.getWidth(), signal3.getHeight());

        numOrder = numLength - 1;  // Numerator order (degree of polynomial)
        denOrder = denLength - 1;  // Denominator order (degree of polynomial)

        // Initialize state arrays
        xStates = new State[Math.max(denOrder, 1)];
        uStates = new State[Math.max(numOrder, 1)];

        for (int i = 0; i < xStates.length; i++) {
            xStates[i] = new State(this, i + 1, "x_state_" + i, 1, 1);
            xStates[i].setData(new Data(0.0));
        }

        for (int i = 0; i < uStates.length; i++) {
            uStates[i] = new State(this, i + 1, "u_state_" + i, 1, 1);
            uStates[i].setData(new Data(0.0));
        }
    }

    @Override
    public void calculateOutput(double t) {
        // Discrete transfer function output calculation
        // y(k) = (b0*u(k) + b1*u(k-1) + ... + bn*u(k-n)) / a0 - (a1*y(k-1) + ... + am*y(k-m)) / a0

        InputPort input = inputPortList.get(0);  // Input signal
        InputPort numerator = inputPortList.get(1);  // Numerator coefficients [b0, b1, ..., bn]
        InputPort denominator = inputPortList.get(2);  // Denominator coefficients [a0, a1, ..., am]
        OutputPort output = outputPortList.get(0);

        // Validate input connections
        if (input.getData() == null) {
            throw new RuntimeException("Discrete Transfer Fcn block '" + blockName + "': Input signal is null");
        }
        if (numerator.getData() == null) {
            throw new RuntimeException("Discrete Transfer Fcn block '" + blockName + "': Numerator coefficients input is null");
        }
        if (denominator.getData() == null) {
            throw new RuntimeException("Discrete Transfer Fcn block '" + blockName + "': Denominator coefficients input is null");
        }

        Data inputSignal = input.getData();
        Data numCoeffs = numerator.getData();
        Data denCoeffs = denominator.getData();

        // Validate coefficient dimensions (must be vectors)
        int numLength = Math.max(numCoeffs.getWidth(), numCoeffs.getHeight());
        int denLength = Math.max(denCoeffs.getWidth(), denCoeffs.getHeight());

        if (numLength == 0) {
            throw new RuntimeException("Discrete Transfer Fcn block '" + blockName + "': Numerator coefficients have zero length");
        }
        if (denLength == 0) {
            throw new RuntimeException("Discrete Transfer Fcn block '" + blockName + "': Denominator coefficients have zero length");
        }

        // Get coefficient values (support both row and column vectors)
        double[] b = new double[numLength];
        double[] a = new double[denLength];

        if (numCoeffs.getDataType() == DataType.MATRIX) {
            Matrix numMatrix = numCoeffs.getMatrix();
            if (numMatrix == null) {
                throw new RuntimeException("Discrete Transfer Fcn block '" + blockName + "': Numerator coefficient matrix is null");
            }
            // Extract coefficients from either row vector [1×n] or column vector [n×1]
            boolean isRowVector = (numMatrix.getRowDimension() == 1);
            for (int i = 0; i < b.length; i++) {
                b[i] = isRowVector ? numMatrix.get(0, i) : numMatrix.get(i, 0);
            }
        } else {
            b[0] = numCoeffs.getInitValue();
        }

        if (denCoeffs.getDataType() == DataType.MATRIX) {
            Matrix denMatrix = denCoeffs.getMatrix();
            if (denMatrix == null) {
                throw new RuntimeException("Discrete Transfer Fcn block '" + blockName + "': Denominator coefficient matrix is null");
            }
            // Extract coefficients from either row vector [1×n] or column vector [n×1]
            boolean isRowVector = (denMatrix.getRowDimension() == 1);
            for (int i = 0; i < a.length; i++) {
                a[i] = isRowVector ? denMatrix.get(0, i) : denMatrix.get(i, 0);
            }
        } else {
            a[0] = denCoeffs.getInitValue();
        }

        // Validate a[0] is not zero
        if (Math.abs(a[0]) < 1e-10) {
            throw new RuntimeException(String.format(
                "Discrete Transfer Fcn block '%s': Denominator coefficient a[0]=%.10f is effectively zero. " +
                "The first denominator coefficient must be non-zero. Check the denominator input connection.",
                blockName, a[0]));
        }

        // Calculate output based on Direct Form II
        double outputValue = 0.0;

        if (inputSignal.getDataType() == DataType.REAL) {
            // Scalar input case
            double u = inputSignal.getInitValue();

            // Calculate numerator part: b0*u(k) + b1*u(k-1) + ...
            outputValue = b[0] * u / a[0];
            for (int i = 1; i < b.length && i <= uStates.length; i++) {
                outputValue += b[i] * uStates[i-1].getData().getInitValue() / a[0];
            }

            // Subtract denominator part: a1*y(k-1) + a2*y(k-2) + ...
            for (int i = 1; i < a.length && i <= xStates.length; i++) {
                outputValue -= a[i] * xStates[i-1].getData().getInitValue() / a[0];
            }

            output.setData(new Data(outputValue));
        } else {
            // Matrix input case - apply transfer function element-wise
            Matrix inputMatrix = inputSignal.getMatrix();
            Matrix outputMatrix = new Matrix(inputMatrix.getRowDimension(), inputMatrix.getColumnDimension());

            for (int row = 0; row < inputMatrix.getRowDimension(); row++) {
                for (int col = 0; col < inputMatrix.getColumnDimension(); col++) {
                    double u = inputMatrix.get(row, col);

                    // Calculate for this element
                    outputValue = b[0] * u / a[0];
                    // Note: For matrix inputs, states would need to be matrices too
                    // This is a simplified implementation

                    outputMatrix.set(row, col, outputValue);
                }
            }

            output.setData(new Data(outputMatrix));
        }
    }

    @Override
    public void calculateUpdate(double t) {
        // Update state variables for next time step
        // This is called at each discrete time step to shift the state variables

        InputPort input = inputPortList.get(0);
        OutputPort output = outputPortList.get(0);

        Data inputSignal = input.getData();
        Data outputSignal = output.getOutputSignalC().getData();

        if (inputSignal.getDataType() == DataType.REAL) {
            // Shift input states (u(k-1) = u(k), u(k-2) = u(k-1), etc.)
            for (int i = uStates.length - 1; i > 0; i--) {
                uStates[i].setData(uStates[i-1].getData());
            }
            if (uStates.length > 0) {
                uStates[0].setData(inputSignal);
            }

            // Shift output states (y(k-1) = y(k), y(k-2) = y(k-1), etc.)
            for (int i = xStates.length - 1; i > 0; i--) {
                xStates[i].setData(xStates[i-1].getData());
            }
            if (xStates.length > 0) {
                xStates[0].setData(outputSignal);
            }
        } else {
            // For matrix inputs, would need matrix state handling
            // Simplified implementation for now
            for (int i = uStates.length - 1; i > 0; i--) {
                uStates[i].setData(uStates[i-1].getData());
            }
            if (uStates.length > 0) {
                uStates[0].setData(inputSignal);
            }

            for (int i = xStates.length - 1; i > 0; i--) {
                xStates[i].setData(xStates[i-1].getData());
            }
            if (xStates.length > 0) {
                xStates[0].setData(outputSignal);
            }
        }
    }
}
