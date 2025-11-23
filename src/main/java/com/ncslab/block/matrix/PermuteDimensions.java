package com.ncslab.block.matrix;

import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.matrix.PermuteDimensionsDto;

import java.util.Map;
import java.util.HashMap;

import com.ncslab.block.Block;
import com.ncslab.block.io.Parameter;
import com.ncslab.block.data.DataType;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.BlockCreationException;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.block.io.InputPort;

import java.util.ArrayList;
import java.util.List;
import com.ncslab.util.TemplateManager;
import Jama.Matrix;

/**
 * PermuteDimensions Block - Rearranges dimensions of multi-dimensional arrays.
 *
 * <p>This block rearranges the dimensions of the input signal according to the specified
 * permutation order, similar to MATLAB's permute() function.</p>
 *
 * <h3>MATLAB Equivalent:</h3>
 * <pre>
 * % 2D transpose
 * B = permute(A, [2, 1]);  % Equivalent to transpose(A)
 *
 * % 3D permutation
 * B = permute(A, [3, 1, 2]);  % Moves 3rd dimension to 1st, 1st to 2nd, 2nd to 3rd
 * </pre>
 *
 * <h3>Parameters:</h3>
 * <ul>
 *   <li><b>PermutationOrder</b>: Comma-separated integers specifying dimension rearrangement (1-based indexing)
 *       <ul>
 *         <li>Example: "2,1" for 2D transpose</li>
 *         <li>Example: "3,1,2" for 3D permutation</li>
 *         <li>Must be a valid permutation of [1,2,...,n]</li>
 *       </ul>
 *   </li>
 *   <li><b>SampleTime</b>: Block sample time (default: -1 for inherited)</li>
 *   <li><b>OutDataTypeStr</b>: Output data type (default: "Inherit: Same as input")</li>
 * </ul>
 *
 * <h3>Input/Output:</h3>
 * <ul>
 *   <li><b>Input</b>: Single input signal (scalar or matrix)</li>
 *   <li><b>Output</b>: Permuted signal with rearranged dimensions</li>
 * </ul>
 *
 * <h3>Examples:</h3>
 * <pre>
 * % 2D Matrix transpose
 * A = [1 2 3; 4 5 6];  % 2x3 matrix
 * B = permute(A, [2,1]);  % 3x2 matrix
 *
 * % For higher dimensions, permutation follows the specified order
 * </pre>
 *
 * @author NCSLab Team
 * @version 2025
 * @see com.ncslab.block.matrix.Transpose
 * @see com.ncslab.dto.block.specialized.matrix.PermuteDimensionsDto
 */
public class PermuteDimensions extends Block {

    /** Permutation order parameter */
    @Getter
    private Parameter permutationOrder;

    /**
     * DTO-NATIVE Constructor - Creates PermuteDimensions block directly from BlockDto DTO
     */
    public PermuteDimensions(BlockDto blockDto, NCSLabModel model) {
        super(blockDto, model);
        System.out.println("DTO-NATIVE: PermuteDimensions block created successfully - " + blockDto.getBlockName());
    }

    /**
     * Factory method to create PermuteDimensions block from PermuteDimensionsDto.
     *
     * @param dto The PermuteDimensionsDto containing block configuration
     * @param model The NCSLabModel this block belongs to
     * @return New PermuteDimensions block instance
     * @throws BlockCreationException if block creation fails
     */
    public static PermuteDimensions createFromDto(PermuteDimensionsDto dto, NCSLabModel model) throws BlockCreationException {
        // Validate permutation order format
        try {
            int[] order = dto.getPermutationOrderArray();
            if (order.length < 1) {
                throw new BlockCreationException("PermuteDimensions: permutation order must have at least one dimension");
            }
        } catch (Exception e) {
            throw new BlockCreationException("PermuteDimensions: invalid permutation order format - " + e.getMessage());
        }

        return new PermuteDimensions(dto, model);
    }

    public static final List<String> outputNames = new ArrayList<>();
    public static final List<String> inputNames = new ArrayList<>();

    public static final Map<String, String> PARAMETER_DEFAULTS;
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("PermutationOrder", "2,1");
        PARAMETER_DEFAULTS.put("SampleTime", "-1");
        PARAMETER_DEFAULTS.put("OutDataTypeStr", "Inherit: Same as input");
        PARAMETER_DEFAULTS.put("SaturateOnIntegerOverflow", "off");
    }

    static {
        outputNames.add("out1");
        inputNames.add("in1");
    }

    /**
     * Legacy JSON constructor for backward compatibility.
     */
    public PermuteDimensions(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);

        permutationOrder = getParameterByName("PermutationOrder");
        outputPortList.add(new OutputPort(this, 1, true));
        inputPortList.add(new InputPort(this, 1));
    }

    @Override
    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);

        context.put("block", this);
        context.put("permutationOrder", permutationOrder);

        String codeStr = TemplateManager.renderTemplate("c/matrix/PermuteDimensions/init.vm", context);
        code.addInitCode(codeStr);
    }

    @Override
    public void generateOutputCodeC(CodeStructC code) {
        context.put("block", this);
        context.put("input", getInputPortVariables()[0]);
        context.put("output", getOutputPortVariables()[0]);
        context.put("permutationOrder", permutationOrder != null ? permutationOrder.getData().getInitValue() : null);

        String codeStr = TemplateManager.renderTemplate("c/matrix/PermuteDimensions/output.vm", context);
        code.addOutputCode(codeStr);
    }

    @Override
    public void updateDimension() throws MatDimException {
        OutputPort out = outputPortList.get(0);
        OutputSignal in = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();

        // Get permutation order
        String orderStr = permutationOrder.getData().getDataString();
        String[] parts = orderStr.split(",");
        int[] order = new int[parts.length];

        try {
            for (int i = 0; i < parts.length; i++) {
                order[i] = Integer.parseInt(parts[i].trim());
            }
        } catch (NumberFormatException e) {
            throw new MatDimException("PermuteDimensions: invalid permutation order format");
        }

        // Validate permutation order
        if (order.length < 1 || order.length > 2) {
            throw new MatDimException("PermuteDimensions: permutation order must have 1 or 2 dimensions");
        }

        // Check for valid permutation (all values must be unique and in range)
        boolean[] seen = new boolean[order.length + 1];
        for (int idx : order) {
            if (idx < 1 || idx > order.length) {
                throw new MatDimException("PermuteDimensions: permutation order values must be in range [1, " + order.length + "]");
            }
            if (seen[idx]) {
                throw new MatDimException("PermuteDimensions: permutation order contains duplicate values");
            }
            seen[idx] = true;
        }

        // Update output dimensions based on permutation
        if (in.getDataType() == DataType.MATRIX) {
            int inputHeight = in.getHeight();
            int inputWidth = in.getWidth();

            if (order.length == 2) {
                int[] dims = {inputHeight, inputWidth};
                int outHeight = dims[order[0] - 1];
                int outWidth = dims[order[1] - 1];

                out.setHeight(outHeight);
                out.setWidth(outWidth);
                out.getOutputSignalC().setHeight(outHeight);
                out.getOutputSignalC().setWidth(outWidth);
                out.getOutputSignalC().setDataType(DataType.MATRIX);
            } else {
                // For 1D, dimensions don't change
                out.setHeight(inputHeight);
                out.setWidth(inputWidth);
                out.getOutputSignalC().setHeight(inputHeight);
                out.getOutputSignalC().setWidth(inputWidth);
                out.getOutputSignalC().setDataType(in.getDataType());
            }
        } else {
            // Scalar input
            out.setHeight(1);
            out.setWidth(1);
            out.getOutputSignalC().setHeight(1);
            out.getOutputSignalC().setWidth(1);
            out.getOutputSignalC().setDataType(DataType.REAL);
        }
    }

    /**
     * Calculate output by permuting dimensions of the input matrix.
     * For 2D case with order [2,1], this is equivalent to transpose.
     *
     * @param t Current simulation time (unused for this block)
     */
    @Override
    public void calculateOutput(double t) {
        OutputPort out = outputPortList.get(0);

        // Get input matrix
        Matrix input = inputPortList.get(0).getData().getMatrix();

        // Get permutation order
        String orderStr = permutationOrder.getData().getDataString();
        String[] parts = orderStr.split(",");
        int[] order = new int[parts.length];

        for (int i = 0; i < parts.length; i++) {
            order[i] = Integer.parseInt(parts[i].trim());
        }

        Matrix result;

        if (order.length == 2) {
            // 2D permutation
            if (order[0] == 2 && order[1] == 1) {
                // Transpose case [2,1]
                result = input.transpose();
            } else {
                // Identity permutation [1,2] - no change
                result = input.copy();
            }
        } else {
            // For 1D or identity permutation, just copy
            result = input.copy();
        }

        // Set output
        out.getOutputSignalC().getData().setMatrix(result);
    }
}
