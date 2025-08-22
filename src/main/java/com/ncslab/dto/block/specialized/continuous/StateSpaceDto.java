package com.ncslab.dto.block.specialized.continuous;

import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.common.TypedParameter;
import com.ncslab.dto.mapper.validation.ValidationResult;
import com.ncslab.dto.annotations.MigrationCompatible;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * DTO representation of StateSpace block with SIMULINK-compatible parameters.
 * 
 * This DTO provides a modern, type-safe interface for the StateSpace block
 * and supports migration from the legacy JSONObject-based approach.
 * 
 * SIMULINK Parameters:
 * - A: State matrix (n x n) - System dynamics matrix
 * - B: Input matrix (n x m) - Input distribution matrix
 * - C: Output matrix (p x n) - Output selection matrix
 * - D: Feedthrough matrix (p x m) - Direct feedthrough matrix
 * - X0: Initial state vector (n x 1) - Initial condition
 * - AbsoluteTolerance: Absolute tolerance for simulation (default: "auto")
 * - ContinuousStateAttributes: Attributes for continuous states (default: "'''")
 * - SampleTime: Sample time for discrete operation (-1 for inherited, 0 for continuous)
 * - OutDataTypeStr: Output data type specification
 * - SaturateOnIntegerOverflow: Handle integer overflow
 * 
 * State Space Representation:
 * dx/dt = A*x + B*u
 * y = C*x + D*u
 * 
 * Where:
 * - x: state vector (n x 1)
 * - u: input vector (m x 1)
 * - y: output vector (p x 1)
 * - n: number of states
 * - m: number of inputs
 * - p: number of outputs
 * 
 * @author BlockMigrationAutomation
 * @version 1.0
 * @since DTO Migration Week 7
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@MigrationCompatible(originalClass = "com.ncslab.block.continuous.StateSpace")
public class StateSpaceDto extends BlockDto {
    
    // ===== STATE SPACE SPECIFIC PARAMETERS =====
    
    /**
     * State matrix A (n x n) - System dynamics matrix
     * Format: Matrix string representation (e.g., "[1 2; 3 4]" for 2x2 matrix)
     * Default: "[1]" (1x1 identity)
     * Validation: Must be square matrix
     */
    private TypedParameter stateMatrix;
    
    /**
     * Input matrix B (n x m) - Input distribution matrix
     * Format: Matrix string representation (e.g., "[1; 2]" for 2x1 matrix)
     * Default: "[1]" (1x1)
     * Validation: Height must match state matrix size
     */
    private TypedParameter inputMatrix;
    
    /**
     * Output matrix C (p x n) - Output selection matrix
     * Format: Matrix string representation (e.g., "[1 0]" for 1x2 matrix)
     * Default: "[1]" (1x1)
     * Validation: Width must match state matrix size
     */
    private TypedParameter outputMatrix;
    
    /**
     * Feedthrough matrix D (p x m) - Direct feedthrough matrix
     * Format: Matrix string representation (e.g., "[0]" for no feedthrough)
     * Default: "[0]" (no feedthrough)
     * Validation: Dimensions must be compatible with C and B
     */
    private TypedParameter feedthroughMatrix;
    
    /**
     * Initial state vector X0 (n x 1) - Initial condition
     * Format: Vector string representation (e.g., "[0; 0]" for 2x1 zero vector)
     * Default: "[0]" (scalar zero)
     * Validation: Must be column vector with height matching state matrix size
     */
    private TypedParameter initialState;
    
    /**
     * Absolute tolerance for simulation
     * Default: "auto"
     * Options: "auto", or numeric value as string
     */
    private TypedParameter absoluteTolerance;
    
    /**
     * Attributes for continuous states
     * Default: "'''"
     */
    private TypedParameter continuousStateAttributes;
    
    /**
     * Output data type specification
     * Default: "Inherit: Same as input"
     */
    private TypedParameter outDataTypeStr;
    
    /**
     * Handle integer overflow
     * Default: false (off)
     */
    private TypedParameter saturateOnIntegerOverflow;
    
    // ===== FACTORY METHODS =====
    
    /**
     * Create DTO from legacy parameters map (for migration support)
     */
    public static StateSpaceDto fromLegacyParameters(Map<String, String> params) {
        StateSpaceDto.StateSpaceDtoBuilder builder = StateSpaceDto.builder()
                .blockName(params.getOrDefault("blockName", "StateSpace"))
                .blockPath(params.getOrDefault("blockPath", ""))
                .blockUUID(params.getOrDefault("blockUUID", ""))
                .blockType("StateSpace");
        
        StateSpaceDto dto = builder.build();
        
        // Map parameters to TypedParameter
        if (params.containsKey("A")) {
            dto.stateMatrix = TypedParameter.of(params.get("A"));
        }
        
        if (params.containsKey("B")) {
            dto.inputMatrix = TypedParameter.of(params.get("B"));
        }
        
        if (params.containsKey("C")) {
            dto.outputMatrix = TypedParameter.of(params.get("C"));
        }
        
        if (params.containsKey("D")) {
            dto.feedthroughMatrix = TypedParameter.of(params.get("D"));
        }
        
        if (params.containsKey("X0")) {
            dto.initialState = TypedParameter.of(params.get("X0"));
        }
        
        if (params.containsKey("AbsoluteTolerance")) {
            dto.absoluteTolerance = TypedParameter.of(params.get("AbsoluteTolerance"));
        }
        
        if (params.containsKey("ContinuousStateAttributes")) {
            dto.continuousStateAttributes = TypedParameter.of(params.get("ContinuousStateAttributes"));
        }
        
        if (params.containsKey("OutDataTypeStr")) {
            dto.outDataTypeStr = TypedParameter.of(params.get("OutDataTypeStr"));
        }
        
        if (params.containsKey("SaturateOnIntegerOverflow")) {
            dto.saturateOnIntegerOverflow = TypedParameter.of("on".equals(params.get("SaturateOnIntegerOverflow")));
        }
        
        return dto;
    }
    
    /**
     * Create builder with SIMULINK-compatible defaults
     */
    public static StateSpaceDtoBuilder builderWithDefaults() {
        StateSpaceDto dto = StateSpaceDto.builder()
                .blockType("StateSpace")
                .sampleTime(0.0) // Continuous by default
                .build();
        
        // Set default typed parameters for SISO system
        dto.stateMatrix = TypedParameter.of("[1]");
        dto.inputMatrix = TypedParameter.of("[1]");
        dto.outputMatrix = TypedParameter.of("[1]");
        dto.feedthroughMatrix = TypedParameter.of("[0]");
        dto.initialState = TypedParameter.of("[0]");
        dto.absoluteTolerance = TypedParameter.of("auto");
        dto.continuousStateAttributes = TypedParameter.of("'''");
        dto.outDataTypeStr = TypedParameter.of("Inherit: Same as input");
        dto.saturateOnIntegerOverflow = TypedParameter.of(false);
        
        return StateSpaceDto.builder()
                .blockId(dto.getBlockId())
                .blockType(dto.getBlockType())
                .blockName(dto.getBlockName())
                .blockPath(dto.getBlockPath())
                .blockUUID(dto.getBlockUUID())
                .sampleTime(dto.getSampleTime())
                .stateMatrix(dto.stateMatrix)
                .inputMatrix(dto.inputMatrix)
                .outputMatrix(dto.outputMatrix)
                .feedthroughMatrix(dto.feedthroughMatrix)
                .initialState(dto.initialState)
                .absoluteTolerance(dto.absoluteTolerance)
                .continuousStateAttributes(dto.continuousStateAttributes)
                .outDataTypeStr(dto.outDataTypeStr)
                .saturateOnIntegerOverflow(dto.saturateOnIntegerOverflow);
    }
    
    // ===== PARAMETER ACCESS HELPERS =====
    
    public String getStateMatrixValue() {
        return stateMatrix != null ? stateMatrix.getValue(String.class) : "[1]";
    }
    
    public String getInputMatrixValue() {
        return inputMatrix != null ? inputMatrix.getValue(String.class) : "[1]";
    }
    
    public String getOutputMatrixValue() {
        return outputMatrix != null ? outputMatrix.getValue(String.class) : "[1]";
    }
    
    public String getFeedthroughMatrixValue() {
        return feedthroughMatrix != null ? feedthroughMatrix.getValue(String.class) : "[0]";
    }
    
    public String getInitialStateValue() {
        return initialState != null ? initialState.getValue(String.class) : "[0]";
    }
    
    public String getAbsoluteToleranceValue() {
        return absoluteTolerance != null ? absoluteTolerance.getValue(String.class) : "auto";
    }
    
    public String getContinuousStateAttributesValue() {
        return continuousStateAttributes != null ? continuousStateAttributes.getValue(String.class) : "'''";
    }
    
    public String getOutDataTypeStrValue() {
        return outDataTypeStr != null ? outDataTypeStr.getValue(String.class) : "Inherit: Same as input";
    }
    
    public Boolean getSaturateOnIntegerOverflowValue() {
        return saturateOnIntegerOverflow != null ? saturateOnIntegerOverflow.getValue(Boolean.class) : false;
    }
    
    // ===== MATRIX PARSING AND UTILITIES =====
    
    /**
     * Parse matrix dimensions from string format "[a11 a12; a21 a22]"
     * Returns [rows, cols]
     */
    public int[] parseMatrixDimensions(String matrixStr) {
        if (matrixStr == null || matrixStr.trim().isEmpty()) {
            throw new IllegalArgumentException("Matrix string cannot be null or empty");
        }
        
        // Remove brackets and split by semicolons for rows
        String cleanStr = matrixStr.trim().replaceAll("^\\[|\\]$", "");
        String[] rows = cleanStr.split(";");
        
        if (rows.length == 0) {
            throw new IllegalArgumentException("No rows found in matrix: " + matrixStr);
        }
        
        // Count columns in first row
        String firstRow = rows[0].trim();
        if (firstRow.isEmpty()) {
            throw new IllegalArgumentException("Empty first row in matrix: " + matrixStr);
        }
        
        String[] firstRowElements = firstRow.split("\\s+");
        int cols = firstRowElements.length;
        
        // Verify all rows have same number of columns
        for (int i = 1; i < rows.length; i++) {
            String[] rowElements = rows[i].trim().split("\\s+");
            if (rowElements.length != cols) {
                throw new IllegalArgumentException("Inconsistent number of columns in matrix: " + matrixStr);
            }
        }
        
        return new int[]{rows.length, cols};
    }
    
    /**
     * Parse matrix values from string format "[a11 a12; a21 a22]"
     * Returns 2D array matrix[row][col]
     */
    public double[][] parseMatrixValues(String matrixStr) {
        int[] dims = parseMatrixDimensions(matrixStr);
        int rows = dims[0];
        int cols = dims[1];
        
        double[][] matrix = new double[rows][cols];
        
        // Remove brackets and split by semicolons for rows
        String cleanStr = matrixStr.trim().replaceAll("^\\[|\\]$", "");
        String[] rowStrings = cleanStr.split(";");
        
        for (int i = 0; i < rows; i++) {
            String[] elements = rowStrings[i].trim().split("\\s+");
            for (int j = 0; j < cols; j++) {
                try {
                    matrix[i][j] = Double.parseDouble(elements[j].trim());
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Invalid matrix element '" + elements[j] + "' at position [" + i + "," + j + "]");
                }
            }
        }
        
        return matrix;
    }
    
    /**
     * Format 2D array as matrix string "[a11 a12; a21 a22]"
     */
    public static String formatMatrix(double[][] matrix) {
        if (matrix == null || matrix.length == 0) {
            return "[0]";
        }
        
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < matrix.length; i++) {
            if (i > 0) sb.append("; ");
            for (int j = 0; j < matrix[i].length; j++) {
                if (j > 0) sb.append(" ");
                sb.append(matrix[i][j]);
            }
        }
        sb.append("]");
        return sb.toString();
    }
    
    /**
     * Get state matrix dimensions [n, n] where n is number of states
     */
    public int[] getStateMatrixDimensions() {
        try {
            return parseMatrixDimensions(getStateMatrixValue());
        } catch (Exception e) {
            return new int[]{1, 1}; // Default
        }
    }
    
    /**
     * Get number of states (n)
     */
    public int getNumberOfStates() {
        int[] dims = getStateMatrixDimensions();
        return dims[0]; // Should be square, so rows = cols = n
    }
    
    /**
     * Get number of inputs (m)
     */
    public int getNumberOfInputs() {
        try {
            int[] dims = parseMatrixDimensions(getInputMatrixValue());
            return dims[1]; // Width of B matrix
        } catch (Exception e) {
            return 1; // Default
        }
    }
    
    /**
     * Get number of outputs (p)
     */
    public int getNumberOfOutputs() {
        try {
            int[] dims = parseMatrixDimensions(getOutputMatrixValue());
            return dims[0]; // Height of C matrix
        } catch (Exception e) {
            return 1; // Default
        }
    }
    
    // ===== VALIDATION =====
    
    @Override
    public ValidationResult validate() {
        ValidationResult result = super.validate(); // Call parent validation
        
        try {
            // Parse all matrices to check format and get dimensions
            int[] aDims = parseMatrixDimensions(getStateMatrixValue());
            int[] bDims = parseMatrixDimensions(getInputMatrixValue());
            int[] cDims = parseMatrixDimensions(getOutputMatrixValue());
            int[] dDims = parseMatrixDimensions(getFeedthroughMatrixValue());
            int[] x0Dims = parseMatrixDimensions(getInitialStateValue());
            
            int n = aDims[0]; // number of states
            int m = bDims[1]; // number of inputs
            int p = cDims[0]; // number of outputs
            
            // Validate state matrix A is square
            if (aDims[0] != aDims[1]) {
                result.addError("State matrix A must be square (n x n)");
            }
            
            // Validate input matrix B dimensions (n x m)
            if (bDims[0] != n) {
                result.addError("Input matrix B height must match state matrix A size (" + n + " x m)");
            }
            
            // Validate output matrix C dimensions (p x n)
            if (cDims[1] != n) {
                result.addError("Output matrix C width must match state matrix A size (p x " + n + ")");
            }
            
            // Validate feedthrough matrix D dimensions (p x m)
            if (dDims[0] != p || dDims[1] != m) {
                result.addError("Feedthrough matrix D dimensions must be " + p + " x " + m);
            }
            
            // Validate initial state X0 dimensions (n x 1)
            if (x0Dims[0] != n || x0Dims[1] != 1) {
                result.addError("Initial state X0 must be " + n + " x 1 vector");
            }
            
            // Validate that matrices contain finite numbers
            double[][] A = parseMatrixValues(getStateMatrixValue());
            double[][] B = parseMatrixValues(getInputMatrixValue());
            double[][] C = parseMatrixValues(getOutputMatrixValue());
            double[][] D = parseMatrixValues(getFeedthroughMatrixValue());
            double[][] X0 = parseMatrixValues(getInitialStateValue());
            
            if (!isMatrixFinite(A)) result.addError("State matrix A must contain finite numbers");
            if (!isMatrixFinite(B)) result.addError("Input matrix B must contain finite numbers");
            if (!isMatrixFinite(C)) result.addError("Output matrix C must contain finite numbers");
            if (!isMatrixFinite(D)) result.addError("Feedthrough matrix D must contain finite numbers");
            if (!isMatrixFinite(X0)) result.addError("Initial state X0 must contain finite numbers");
            
        } catch (Exception e) {
            result.addError("Matrix parsing error: " + e.getMessage());
        }
        
        return result;
    }
    
    private boolean isMatrixFinite(double[][] matrix) {
        for (double[] row : matrix) {
            for (double val : row) {
                if (Double.isNaN(val) || Double.isInfinite(val)) {
                    return false;
                }
            }
        }
        return true;
    }
    
    // ===== UTILITY METHODS =====
    
    /**
     * Check if system has direct feedthrough (D != 0)
     */
    public boolean hasDirectFeedthrough() {
        try {
            double[][] D = parseMatrixValues(getFeedthroughMatrixValue());
            for (double[] row : D) {
                for (double val : row) {
                    if (Math.abs(val) > 1e-15) {
                        return true;
                    }
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Check if continuous time (sample time = 0) or discrete time
     */
    public boolean isContinuousTime() {
        return getSampleTime() != null && getSampleTime() == 0.0;
    }
    
    /**
     * Create SISO (Single Input Single Output) state space system
     */
    public static StateSpaceDto createSISO(String name, String path, double a, double b, double c, double d, double x0) {
        StateSpaceDto dto = new StateSpaceDto();
        dto.setBlockName(name);
        dto.setBlockPath(path);
        dto.setBlockType("StateSpace");
        dto.setSampleTime(0.0); // Continuous
        
        // Set default parameters
        dto.setAbsoluteTolerance(TypedParameter.of("auto"));
        dto.setContinuousStateAttributes(TypedParameter.of("'''"));
        dto.setOutDataTypeStr(TypedParameter.of("Inherit: Same as input"));
        dto.setSaturateOnIntegerOverflow(TypedParameter.of(false));
        
        dto.setStateMatrix(TypedParameter.of("[" + a + "]"));
        dto.setInputMatrix(TypedParameter.of("[" + b + "]"));
        dto.setOutputMatrix(TypedParameter.of("[" + c + "]"));
        dto.setFeedthroughMatrix(TypedParameter.of("[" + d + "]"));
        dto.setInitialState(TypedParameter.of("[" + x0 + "]"));
        
        return dto;
    }
    
    /**
     * Create second order system: 1 / (s^2 + 2*zeta*wn*s + wn^2)
     */
    public static StateSpaceDto createSecondOrder(String name, String path, double naturalFreq, double dampingRatio) {
        StateSpaceDto dto = new StateSpaceDto();
        dto.setBlockName(name);
        dto.setBlockPath(path);
        dto.setBlockType("StateSpace");
        dto.setSampleTime(0.0); // Continuous
        
        // Set default parameters
        dto.setAbsoluteTolerance(TypedParameter.of("auto"));
        dto.setContinuousStateAttributes(TypedParameter.of("'''"));
        dto.setOutDataTypeStr(TypedParameter.of("Inherit: Same as input"));
        dto.setSaturateOnIntegerOverflow(TypedParameter.of(false));
        
        double wn = naturalFreq;
        double zeta = dampingRatio;
        
        // State space form: dx1/dt = x2, dx2/dt = -wn^2*x1 - 2*zeta*wn*x2 + wn^2*u
        // Output: y = x1
        dto.setStateMatrix(TypedParameter.of("[0 1; " + (-wn*wn) + " " + (-2*zeta*wn) + "]"));
        dto.setInputMatrix(TypedParameter.of("[0; " + (wn*wn) + "]"));
        dto.setOutputMatrix(TypedParameter.of("[1 0]"));
        dto.setFeedthroughMatrix(TypedParameter.of("[0]"));
        dto.setInitialState(TypedParameter.of("[0; 0]"));
        
        return dto;
    }
    
    @Override
    public StateSpaceDto copy() {
        return StateSpaceDto.builder()
                .blockId(getBlockId())
                .blockType(getBlockType())
                .blockName(getBlockName())
                .blockPath(getBlockPath())
                .blockUUID(getBlockUUID())
                .sampleTime(getSampleTime())
                .stateMatrix(stateMatrix != null ? stateMatrix.copy() : null)
                .inputMatrix(inputMatrix != null ? inputMatrix.copy() : null)
                .outputMatrix(outputMatrix != null ? outputMatrix.copy() : null)
                .feedthroughMatrix(feedthroughMatrix != null ? feedthroughMatrix.copy() : null)
                .initialState(initialState != null ? initialState.copy() : null)
                .absoluteTolerance(absoluteTolerance != null ? absoluteTolerance.copy() : null)
                .continuousStateAttributes(continuousStateAttributes != null ? continuousStateAttributes.copy() : null)
                .outDataTypeStr(outDataTypeStr != null ? outDataTypeStr.copy() : null)
                .saturateOnIntegerOverflow(saturateOnIntegerOverflow != null ? saturateOnIntegerOverflow.copy() : null)
                .build();
    }
    
    /**
     * Convert to legacy parameters format for backward compatibility
     */
    public Map<String, String> toLegacyParameters() {
        Map<String, String> params = new HashMap<>();
        
        if (stateMatrix != null) {
            params.put("A", String.valueOf(stateMatrix.getValue()));
        }
        if (inputMatrix != null) {
            params.put("B", String.valueOf(inputMatrix.getValue()));
        }
        if (outputMatrix != null) {
            params.put("C", String.valueOf(outputMatrix.getValue()));
        }
        if (feedthroughMatrix != null) {
            params.put("D", String.valueOf(feedthroughMatrix.getValue()));
        }
        if (initialState != null) {
            params.put("X0", String.valueOf(initialState.getValue()));
        }
        if (absoluteTolerance != null) {
            params.put("AbsoluteTolerance", String.valueOf(absoluteTolerance.getValue()));
        }
        if (continuousStateAttributes != null) {
            params.put("ContinuousStateAttributes", String.valueOf(continuousStateAttributes.getValue()));
        }
        if (getSampleTime() != null) {
            params.put("SampleTime", String.valueOf(getSampleTime()));
        }
        if (outDataTypeStr != null) {
            params.put("OutDataTypeStr", String.valueOf(outDataTypeStr.getValue()));
        }
        if (saturateOnIntegerOverflow != null) {
            Boolean satVal = saturateOnIntegerOverflow.getValue(Boolean.class);
            params.put("SaturateOnIntegerOverflow", Boolean.TRUE.equals(satVal) ? "on" : "off");
        }
        
        return params;
    }
    
    @Override
    public String toString() {
        return String.format("StateSpaceDto{id=%d, name='%s', type='%s', states=%d, inputs=%d, outputs=%d, feedthrough=%s}", 
                           getBlockId(), 
                           getBlockName(), 
                           getBlockType(),
                           getNumberOfStates(),
                           getNumberOfInputs(),
                           getNumberOfOutputs(),
                           hasDirectFeedthrough());
    }
}