package com.ncslab.block.matrix;

import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.matrix.AssignmentDto;

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
 * Assignment Block - Assigns values to specific elements of a signal.
 *
 * <p>This block assigns values to specified elements of the input signal, similar to
 * MATLAB's indexed assignment operation. The block has two inputs: the original signal
 * and the values to be assigned.</p>
 *
 * <h3>MATLAB Equivalent:</h3>
 * <pre>
 * % Element assignment
 * A(2,3) = value;
 *
 * % Row assignment
 * A(2,:) = values;
 *
 * % Column assignment
 * A(:,3) = values;
 *
 * % Index vector assignment
 * A([1,3,5]) = values;
 * </pre>
 *
 * <h3>Parameters:</h3>
 * <ul>
 *   <li><b>AssignmentMode</b>: Mode of assignment
 *       <ul>
 *         <li>"element": Single element assignment A(i,j) = value</li>
 *         <li>"row": Entire row assignment A(i,:) = values</li>
 *         <li>"column": Entire column assignment A(:,j) = values</li>
 *         <li>"index": Multiple element assignment A(indices) = values</li>
 *       </ul>
 *   </li>
 *   <li><b>Indices</b>: Index specification (format depends on mode)
 *       <ul>
 *         <li>element mode: "row,col" (e.g., "2,3")</li>
 *         <li>row mode: "row_number" (e.g., "2")</li>
 *         <li>column mode: "col_number" (e.g., "3")</li>
 *         <li>index mode: "i1,i2,..." (e.g., "1,3,5,7")</li>
 *       </ul>
 *   </li>
 *   <li><b>SampleTime</b>: Block sample time (default: -1 for inherited)</li>
 *   <li><b>OutDataTypeStr</b>: Output data type (default: "Inherit: Same as input")</li>
 * </ul>
 *
 * <h3>Input/Output:</h3>
 * <ul>
 *   <li><b>Input 1</b>: Original signal/matrix</li>
 *   <li><b>Input 2</b>: Values to assign</li>
 *   <li><b>Output</b>: Modified signal with assigned values</li>
 * </ul>
 *
 * <h3>Examples:</h3>
 * <pre>
 * % Element assignment - replace A(2,3) with value
 * Original: A = [1 2 3; 4 5 6; 7 8 9]
 * Value: 99
 * Result: A(2,3) = 99 -> [1 2 3; 4 5 99; 7 8 9]
 *
 * % Row assignment - replace row 2 with new values
 * Original: A = [1 2 3; 4 5 6; 7 8 9]
 * Values: [10 11 12]
 * Result: A(2,:) = [10 11 12] -> [1 2 3; 10 11 12; 7 8 9]
 * </pre>
 *
 * @author NCSLab Team
 * @version 2025
 * @see com.ncslab.block.matrix.Submatrix
 * @see com.ncslab.dto.block.specialized.matrix.AssignmentDto
 */
public class Assignment extends Block {

    /** Assignment mode parameter */
    @Getter
    private Parameter assignmentMode;

    /** Indices parameter */
    @Getter
    private Parameter indices;

    /**
     * DTO-NATIVE Constructor - Creates Assignment block directly from BlockDto DTO
     */
    public Assignment(BlockDto blockDto, NCSLabModel model) {
        super(blockDto, model);
        System.out.println("DTO-NATIVE: Assignment block created successfully - " + blockDto.getBlockName());
    }

    /**
     * Factory method to create Assignment block from AssignmentDto.
     *
     * @param dto The AssignmentDto containing block configuration
     * @param model The NCSLabModel this block belongs to
     * @return New Assignment block instance
     * @throws BlockCreationException if block creation fails
     */
    public static Assignment createFromDto(AssignmentDto dto, NCSLabModel model) throws BlockCreationException {
        // Validate configuration
        if (!dto.isValidConfiguration()) {
            throw new BlockCreationException("Assignment: invalid configuration - check assignment mode and indices");
        }

        return new Assignment(dto, model);
    }

    public static final List<String> outputNames = new ArrayList<>();
    public static final List<String> inputNames = new ArrayList<>();

    public static final Map<String, String> PARAMETER_DEFAULTS;
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("AssignmentMode", "element");
        PARAMETER_DEFAULTS.put("Indices", "1,1");
        PARAMETER_DEFAULTS.put("NumberOfInputs", "2");
        PARAMETER_DEFAULTS.put("SampleTime", "-1");
        PARAMETER_DEFAULTS.put("OutDataTypeStr", "Inherit: Same as input");
        PARAMETER_DEFAULTS.put("SaturateOnIntegerOverflow", "off");
    }

    static {
        outputNames.add("out1");
        inputNames.add("in1");  // Original signal
        inputNames.add("in2");  // Values to assign
    }

    /**
     * Legacy JSON constructor for backward compatibility.
     */
    public Assignment(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);

        assignmentMode = getParameterByName("AssignmentMode");
        indices = getParameterByName("Indices");

        outputPortList.add(new OutputPort(this, 1, true));
        inputPortList.add(new InputPort(this, 1));  // Original signal
        inputPortList.add(new InputPort(this, 2));  // Values to assign
    }

    @Override
    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);

        context.put("block", this);
        context.put("assignmentMode", assignmentMode);
        context.put("indices", indices);

        String codeStr = TemplateManager.renderTemplate("c/matrix/Assignment/init.vm", context);
        code.addInitCode(codeStr);
    }

    @Override
    public void generateOutputCodeC(CodeStructC code) {
        context.put("block", this);
        context.put("originalSignal", getInputPortVariables()[0]);
        context.put("assignValues", getInputPortVariables()[1]);
        context.put("output", getOutputPortVariables()[0]);
        context.put("assignmentMode", assignmentMode != null ? assignmentMode.getData().getInitValue() : null);
        context.put("indices", indices != null ? indices.getData().getInitValue() : null);

        String codeStr = TemplateManager.renderTemplate("c/matrix/Assignment/output.vm", context);
        code.addOutputCode(codeStr);
    }

    @Override
    public void updateDimension() throws MatDimException {
        OutputPort out = outputPortList.get(0);
        OutputSignal originalSignal = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
        OutputSignal assignValues = inputPortList.get(1).getLinkedLine().getLinkedOutputPort().getOutputSignalC();

        // Get parameters
        String mode = assignmentMode.getData().getDataString();
        String indicesStr = indices.getData().getDataString();
        String[] parts = indicesStr.split(",");

        // Validate assignment mode
        if (!mode.equals("element") && !mode.equals("row") &&
            !mode.equals("column") && !mode.equals("index")) {
            throw new MatDimException("Assignment: invalid assignment mode - must be 'element', 'row', 'column', or 'index'");
        }

        // Output dimensions match original signal dimensions
        if (originalSignal.getDataType() == DataType.MATRIX) {
            int inputHeight = originalSignal.getHeight();
            int inputWidth = originalSignal.getWidth();

            // Validate indices are within bounds
            try {
                switch (mode) {
                    case "element":
                        if (parts.length != 2) {
                            throw new MatDimException("Assignment: element mode requires exactly 2 indices (row,col)");
                        }
                        int row = Integer.parseInt(parts[0].trim());
                        int col = Integer.parseInt(parts[1].trim());
                        if (row < 1 || row > inputHeight) {
                            throw new MatDimException("Assignment: row index " + row + " out of range [1," + inputHeight + "]");
                        }
                        if (col < 1 || col > inputWidth) {
                            throw new MatDimException("Assignment: column index " + col + " out of range [1," + inputWidth + "]");
                        }
                        break;

                    case "row":
                        if (parts.length != 1) {
                            throw new MatDimException("Assignment: row mode requires exactly 1 index");
                        }
                        int rowIdx = Integer.parseInt(parts[0].trim());
                        if (rowIdx < 1 || rowIdx > inputHeight) {
                            throw new MatDimException("Assignment: row index " + rowIdx + " out of range [1," + inputHeight + "]");
                        }
                        // Values must match row width
                        if (assignValues.getDataType() == DataType.MATRIX && assignValues.getWidth() != inputWidth) {
                            throw new MatDimException("Assignment: row assignment values width " + assignValues.getWidth() +
                                    " does not match signal width " + inputWidth);
                        }
                        break;

                    case "column":
                        if (parts.length != 1) {
                            throw new MatDimException("Assignment: column mode requires exactly 1 index");
                        }
                        int colIdx = Integer.parseInt(parts[0].trim());
                        if (colIdx < 1 || colIdx > inputWidth) {
                            throw new MatDimException("Assignment: column index " + colIdx + " out of range [1," + inputWidth + "]");
                        }
                        // Values must match column height
                        if (assignValues.getDataType() == DataType.MATRIX && assignValues.getHeight() != inputHeight) {
                            throw new MatDimException("Assignment: column assignment values height " + assignValues.getHeight() +
                                    " does not match signal height " + inputHeight);
                        }
                        break;

                    case "index":
                        // Validate all linear indices are within bounds
                        int maxLinearIdx = inputHeight * inputWidth;
                        for (String part : parts) {
                            int idx = Integer.parseInt(part.trim());
                            if (idx < 1 || idx > maxLinearIdx) {
                                throw new MatDimException("Assignment: linear index " + idx +
                                        " out of range [1," + maxLinearIdx + "]");
                            }
                        }
                        break;
                }
            } catch (NumberFormatException e) {
                throw new MatDimException("Assignment: invalid index format - " + e.getMessage());
            }

            // Output dimensions same as input
            out.setHeight(inputHeight);
            out.setWidth(inputWidth);
            out.getOutputSignalC().setHeight(inputHeight);
            out.getOutputSignalC().setWidth(inputWidth);
            out.getOutputSignalC().setDataType(DataType.MATRIX);
        } else {
            // Scalar input and output
            out.setHeight(1);
            out.setWidth(1);
            out.getOutputSignalC().setHeight(1);
            out.getOutputSignalC().setWidth(1);
            out.getOutputSignalC().setDataType(DataType.REAL);
        }
    }

    /**
     * Calculate output by assigning values to specified elements of the input matrix.
     * Supports element, row, column, and index-based assignment modes.
     *
     * @param t Current simulation time (unused for this block)
     */
    @Override
    public void calculateOutput(double t) {
        OutputPort out = outputPortList.get(0);

        // Get inputs
        Matrix originalMatrix = inputPortList.get(0).getData().getMatrix();
        Matrix assignValues = inputPortList.get(1).getData().getMatrix();

        // Create a copy of the original matrix
        Matrix result = originalMatrix.copy();

        // Get parameters
        String mode = assignmentMode.getData().getDataString();
        String indicesStr = indices.getData().getDataString();
        String[] parts = indicesStr.split(",");

        // Perform assignment based on mode
        switch (mode) {
            case "element":
                // Single element assignment: A(row, col) = value
                int row = Integer.parseInt(parts[0].trim()) - 1; // Convert to 0-based
                int col = Integer.parseInt(parts[1].trim()) - 1;
                double value = assignValues.get(0, 0);
                result.set(row, col, value);
                break;

            case "row":
                // Row assignment: A(row, :) = values
                int rowIdx = Integer.parseInt(parts[0].trim()) - 1;
                for (int j = 0; j < result.getColumnDimension(); j++) {
                    result.set(rowIdx, j, assignValues.get(0, j));
                }
                break;

            case "column":
                // Column assignment: A(:, col) = values
                int colIdx = Integer.parseInt(parts[0].trim()) - 1;
                for (int i = 0; i < result.getRowDimension(); i++) {
                    result.set(i, colIdx, assignValues.get(i, 0));
                }
                break;

            case "index":
                // Linear index assignment: A([i1, i2, ...]) = values
                int rows = result.getRowDimension();
                int cols = result.getColumnDimension();
                for (int k = 0; k < parts.length; k++) {
                    int linearIdx = Integer.parseInt(parts[k].trim()) - 1; // Convert to 0-based
                    int r = linearIdx % rows;
                    int c = linearIdx / rows;
                    double val = (assignValues.getRowDimension() == 1 && assignValues.getColumnDimension() == 1)
                            ? assignValues.get(0, 0)
                            : assignValues.get(k % assignValues.getRowDimension(), k / assignValues.getRowDimension());
                    result.set(r, c, val);
                }
                break;
        }

        // Set output
        out.getOutputSignalC().getData().setMatrix(result);
    }
}
