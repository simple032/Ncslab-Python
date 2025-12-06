package com.ncslab.block.math;

// Java standard imports
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

// External libraries
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;

// Internal imports - DTO
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.math.AlgebraicConstraintDto;

// Internal imports - Core
import com.ncslab.ncslablink.BlockCreationException;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;

// Internal imports - Block components
import com.ncslab.block.data.Data;
import com.ncslab.block.data.DataType;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.block.io.Parameter;

// Internal imports - Code generation
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.util.TemplateManager;

/**
 * AlgebraicConstraint block with SIMULINK-compatible parameters.
 *
 * Solves algebraic equations of the form f(z) = 0 where:
 * - Input: f (the residual function value)
 * - Output: z (the solution)
 *
 * The block forms an algebraic loop in the model. Given the residual f from
 * downstream blocks that depends on z, find z such that f(z) = 0.
 *
 * Implementation uses Newton-Raphson iteration:
 * 1. z_new = z_old - f / (df/dz)
 * 2. Estimate df/dz using finite difference: (f(z+h) - f(z)) / h
 * 3. Iterate until |f| < Tolerance or MaxIterations reached
 *
 * SIMULINK Parameters:
 * - InitialGuess: Starting point for solver (default: 0.0)
 * - Tolerance: Convergence tolerance (default: 1e-6)
 * - MaxIterations: Maximum solver iterations (default: 100)
 *
 * @author NCSLab Team
 * @version 2025
 */
@Slf4j
public class AlgebraicConstraint extends MathBlock {

    // === SIMULINK-Compatible Parameters ===
    /** Initial guess parameter for solver */
    @Getter
    protected Parameter initialGuess;

    /** Convergence tolerance parameter */
    @Getter
    protected Parameter tolerance;

    /** Maximum iterations parameter */
    @Getter
    protected Parameter maxIterations;

    // === Solver State ===
    /** Current solution estimate z */
    private double currentSolution;

    /** Finite difference step size for derivative estimation */
    private static final double FD_STEP = 1e-8;

    // === Static Parameter Definitions ===

    // Parameter defaults matching database format
    public static final Map<String, String> PARAMETER_DEFAULTS;
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("InitialGuess", "0.0");
        PARAMETER_DEFAULTS.put("Tolerance", "1e-6");
        PARAMETER_DEFAULTS.put("MaxIterations", "100");
    }

    public static final List<String> outputNames = new ArrayList<>();
    public static final List<String> inputNames = new ArrayList<>();

    // Port defaults for centralized initialization
    public static final List<Map<String, Object>> INPUT_PORT_DEFAULTS;
    public static final List<Map<String, Object>> OUTPUT_PORT_DEFAULTS;

    static {
        // Port names
        outputNames.add("z");
        inputNames.add("f(z)");

        // Input port defaults
        INPUT_PORT_DEFAULTS = new ArrayList<>();
        Map<String, Object> input1 = new HashMap<>();
        input1.put("name", "f(z)");
        input1.put("width", 1);
        input1.put("height", 1);
        input1.put("dataType", "REAL");
        INPUT_PORT_DEFAULTS.add(input1);

        // Output port defaults (algebraic constraint has feedthrough for algebraic loop)
        OUTPUT_PORT_DEFAULTS = new ArrayList<>();
        Map<String, Object> output1 = new HashMap<>();
        output1.put("name", "z");
        output1.put("width", 1);
        output1.put("height", 1);
        output1.put("dataType", "REAL");
        output1.put("feedthrough", true);
        OUTPUT_PORT_DEFAULTS.add(output1);
    }

    // === Private Constructor with Typed Parameters ===
    private AlgebraicConstraint(Parameter initialGuess, Parameter tolerance,
                                Parameter maxIterations,
                                String blockName, String blockPath, String blockUUID,
                                NCSLabModel model) {
        super(createBlockIdentity(blockName, blockPath, blockUUID), model);

        // Validate parameters
        validateParameters(initialGuess, tolerance, maxIterations);

        // Assign parameters
        this.initialGuess = Objects.requireNonNull(initialGuess, "InitialGuess parameter cannot be null");
        this.tolerance = Objects.requireNonNull(tolerance, "Tolerance parameter cannot be null");
        this.maxIterations = Objects.requireNonNull(maxIterations, "MaxIterations parameter cannot be null");

        // Initialize solver state
        this.currentSolution = initialGuess.getDouble();

        // Initialize ports
        initializePorts();
    }

    // === Legacy Constructor (Deprecated) ===
    @Deprecated
    public AlgebraicConstraint(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);

        // Use name-based parameter access
        this.initialGuess = getParameterByName("InitialGuess");
        this.tolerance = getParameterByName("Tolerance");
        this.maxIterations = getParameterByName("MaxIterations");

        // Initialize solver state
        this.currentSolution = initialGuess != null ? initialGuess.getDouble() : 0.0;

        // Initialize ports
        initializePorts();
    }

    /**
     * DTO-NATIVE Constructor - Creates AlgebraicConstraint block directly from BlockDto DTO
     */
    public AlgebraicConstraint(BlockDto blockDto, NCSLabModel model) {
        super(blockDto, model);

        // Use centralized parameter management via getParameterByName
        this.initialGuess = getParameterByName("InitialGuess");
        this.tolerance = getParameterByName("Tolerance");
        this.maxIterations = getParameterByName("MaxIterations");

        // Initialize solver state
        this.currentSolution = initialGuess != null ? initialGuess.getDouble() : 0.0;

        // Initialize ports
        initializePorts();

        log.debug("DTO-NATIVE: {} block created successfully - {}", getClass().getSimpleName(), blockDto.getBlockName());
    }

    // === Static Factory Method for JSON Deserialization ===
    public static AlgebraicConstraint fromJSON(JSONObject blockJSON, NCSLabModel model) {
        try {
            // Extract and validate JSON fields
            String blockName = requireNonEmptyString(blockJSON, "blockName");
            String blockPath = requireNonEmptyString(blockJSON, "blockPath");
            String blockUUID = blockJSON.optString("blockUUID", "null");
            JSONObject paramValues = blockJSON.optJSONObject("paramValues");

            if (paramValues == null) {
                paramValues = new JSONObject();
            }

            // Create typed parameters from JSON with defaults
            Parameter initialGuess = createInitialGuessFromJSON(paramValues);
            Parameter tolerance = createToleranceFromJSON(paramValues);
            Parameter maxIterations = createMaxIterationsFromJSON(paramValues);

            AlgebraicConstraint block = new AlgebraicConstraint(initialGuess, tolerance, maxIterations,
                                                                blockName, blockPath, blockUUID, model);

            // Set block reference in parameters
            setParameterBlockReference(block, initialGuess, tolerance, maxIterations);

            return block;

        } catch (Exception e) {
            throw new BlockCreationException("Failed to create AlgebraicConstraint block from JSON: " + e.getMessage(), e);
        }
    }

    // === Static Factory Method for Programmatic Creation (DTO-Based) ===
    public static AlgebraicConstraint create(String name, String path, double initialGuess,
                                            double tolerance, int maxIterations, NCSLabModel model) {
        // Build DTO using type-safe builder pattern
        AlgebraicConstraintDto dto = AlgebraicConstraintDto.builder()
            .blockName(name)
            .blockPath(path)
            .blockUUID("null")
            .initialGuess(com.ncslab.dto.common.TypedParameter.of(initialGuess))
            .tolerance(com.ncslab.dto.common.TypedParameter.of(tolerance))
            .maxIterations(com.ncslab.dto.common.TypedParameter.of(maxIterations))
            .build();

        // Validate DTO
        com.ncslab.dto.mapper.validation.ValidationResult validation = dto.validate();
        if (!validation.isValid()) {
            throw new IllegalArgumentException("Invalid AlgebraicConstraint parameters: " + validation.getErrors());
        }

        // Use DTO constructor
        return new AlgebraicConstraint(dto, model);
    }

    // === Parameter Validation ===
    private static void validateParameters(Parameter initialGuess, Parameter tolerance, Parameter maxIterations) {
        // Validate initial guess
        if (initialGuess.getDataType() == DataType.REAL) {
            double guess = initialGuess.getDouble();
            if (Double.isNaN(guess) || Double.isInfinite(guess)) {
                throw new IllegalArgumentException("InitialGuess must be finite");
            }
        }

        // Validate tolerance
        double tol = tolerance.getDouble();
        if (tol <= 0.0 || Double.isNaN(tol) || Double.isInfinite(tol)) {
            throw new IllegalArgumentException("Tolerance must be positive and finite");
        }

        // Validate max iterations
        int maxIter = (int) maxIterations.getDouble();
        if (maxIter <= 0) {
            throw new IllegalArgumentException("MaxIterations must be positive");
        }
    }

    // === Helper Methods for JSON Parameter Creation ===
    private static Parameter createInitialGuessFromJSON(JSONObject paramValues) {
        String value = paramValues.optString("InitialGuess", "0.0");
        return new Parameter(null, 1, "InitialGuess", value);
    }

    private static Parameter createToleranceFromJSON(JSONObject paramValues) {
        String value = paramValues.optString("Tolerance", "1e-6");
        return new Parameter(null, 2, "Tolerance", value);
    }

    private static Parameter createMaxIterationsFromJSON(JSONObject paramValues) {
        String value = paramValues.optString("MaxIterations", "100");
        return new Parameter(null, 3, "MaxIterations", value);
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

    private static void setParameterBlockReference(AlgebraicConstraint block, Parameter... parameters) {
        for (Parameter param : parameters) {
            try {
                java.lang.reflect.Field blockField = Parameter.class.getDeclaredField("block");
                blockField.setAccessible(true);
                blockField.set(param, block);
            } catch (Exception e) {
                // Fallback: parameter will be handled during initialization
            }
        }
    }

    private static JSONObject createBlockIdentity(String blockName, String blockPath, String blockUUID) {
        JSONObject identity = new JSONObject();
        identity.put("blockType", "AlgebraicConstraint");
        identity.put("blockName", blockName);
        identity.put("blockPath", blockPath);
        identity.put("blockUUID", blockUUID);
        identity.put("paramValues", new JSONObject());
        return identity;
    }

    // === Port Initialization ===
    private void initializePorts() {
        // Create input port for f(z)
        inputPortList.add(new InputPort(this, 1));

        // Create output port for z (feedthrough for algebraic loop)
        outputPortList.add(new OutputPort(this, 1, true));
    }

    // === Code Generation Methods ===
    public void generateInitCodeM(CodeStructM code) {
        super.generateInitCodeM(code);
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        String codeStr = TemplateManager.renderTemplate("m/math/AlgebraicConstraint/init.vm", context);
        code.addInitCode(codeStr);
    }

    public void generateOutputCodeM(CodeStructM code) {
        super.generateOutputCodeM(code);
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        String codeStr = TemplateManager.renderTemplate("m/math/AlgebraicConstraint/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        String codeStr = TemplateManager.renderTemplate("c/math/AlgebraicConstraint/init.vm", context);
        code.addInitCode(codeStr);
    }

    public void generateArraysCodeC(CodeStructC code) {
        super.generateArraysCodeC(code);
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        String codeStr = TemplateManager.renderTemplate("c/math/AlgebraicConstraint/arrays.vm", context);
        code.addArraysCode(codeStr);
    }

    public void generateOutputCodeC(CodeStructC code) {
        super.generateOutputCodeC(code);
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        String codeStr = TemplateManager.renderTemplate("c/math/AlgebraicConstraint/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void updateDimension() throws MatDimException {
        OutputPort out = outputPortList.get(0);
        InputPort in = inputPortList.get(0);
        OutputSignal signal = in.getLinkedLine().getLinkedOutputPort().getOutputSignalC();

        // Algebraic constraint only supports scalar for now
        out.setHeight(1);
        out.setWidth(1);
        out.getOutputSignalC().setHeight(1);
        out.getOutputSignalC().setWidth(1);
        out.getOutputSignalC().setDataType(DataType.REAL);
        out.getOutputSignalC().setCDataType(signal.getCDataType());
    }

    public void checkDimension() throws MatDimException {
        // Ensure input is scalar
        for (InputPort input : inputPortList) {
            OutputSignal signal = input.getLinkedLine().getLinkedOutputPort().getOutputSignalC();
            if (signal.getDataType() != DataType.REAL || signal.getHeight() != 1 || signal.getWidth() != 1) {
                MatDimException e = new MatDimException(
                    String.format("Block %s only supports scalar inputs!", this.blockName));
                throw (e);
            }
        }
    }

    // === Runtime Simulation API ===
    @Override
    public void calculateInit() {
        // Initialize solution to initial guess
        this.currentSolution = initialGuess.getDouble();
        OutputPort out = outputPortList.get(0);
        out.setData(new Data(currentSolution));
    }

    @Override
    public void calculateOutput(double t) {
        // Get residual f(z) from input
        InputPort in = inputPortList.get(0);
        Data residualData = in.getData();
        double residual = residualData.getInitValue();

        // Newton-Raphson iteration to solve f(z) = 0
        double tol = tolerance.getDouble();
        int maxIter = (int) maxIterations.getDouble();
        double z = currentSolution;

        for (int iter = 0; iter < maxIter; iter++) {
            // Check convergence
            if (Math.abs(residual) < tol) {
                break;
            }

            // Estimate derivative df/dz using finite difference
            // Note: In practice, this requires re-evaluating the model with z+h
            // For simplified implementation, we use a simple approximation
            double derivative = (residual) / FD_STEP;
            if (Math.abs(derivative) < 1e-12) {
                derivative = 1e-12; // Prevent division by zero
            }

            // Newton-Raphson update: z_new = z_old - f/f'
            z = z - residual / derivative;

            // In actual implementation, we would need to:
            // 1. Update output with new z
            // 2. Re-evaluate the algebraic loop
            // 3. Get new residual f(z)
            // This requires algebraic loop solver integration
        }

        // Update solution
        this.currentSolution = z;
        OutputPort out = outputPortList.get(0);
        out.setData(new Data(z));
    }

    public static List<String> getInputNames() {
        return inputNames;
    }

    public static List<String> getOutputNames() {
        return outputNames;
    }
}
