package com.ncslab.block.matrix;

import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.matrix.CreateDiagonalMatrixDto;

import com.ncslab.block.Block;
import com.ncslab.util.TemplateManager;
import com.ncslab.block.data.DataType;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.BlockCreationException;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.block.io.InputPort;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import Jama.Matrix;

public class CreateDiagonalMatrix extends Block{
    
    
    /**
     * DTO-NATIVE Constructor - Creates CreateDiagonalMatrix block directly from BlockDto DTO
     */
    public CreateDiagonalMatrix(BlockDto blockDto, NCSLabModel model) {
        super(blockDto, model);
        System.out.println("DTO-NATIVE: CreateDiagonalMatrix block created successfully - " + blockDto.getBlockName());
    }

    /**
     * Factory method to create CreateDiagonalMatrix block from CreateDiagonalMatrixDto.
     *
     * @param dto The CreateDiagonalMatrixDto containing block configuration
     * @param model The NCSLabModel this block belongs to
     * @return New CreateDiagonalMatrix block instance
     * @throws BlockCreationException if block creation fails
     */
    public static CreateDiagonalMatrix createFromDto(CreateDiagonalMatrixDto dto, NCSLabModel model) throws BlockCreationException {
        return new CreateDiagonalMatrix(dto, model);
    }


    public static final List<String> outputNames = new ArrayList<>();
    public static final List<String> inputNames = new ArrayList<>();

    public static final Map<String, String> PARAMETER_DEFAULTS;
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        // No parameters for CreateDiagonalMatrix
    }

    static {

        outputNames.add("out1");
        inputNames.add("in1");
    }
    public CreateDiagonalMatrix(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);

        outputPortList.add(new OutputPort(this, 1, true));
        inputPortList.add(new InputPort(this, 1));
    }

    @Override
    public void generateOutputCodeC(CodeStructC code) {
        context.put("block", this);
        context.put("input", getInputPortVariables());
        context.put("output", getOutputPortVariables());

        String codeStr = TemplateManager.renderTemplate("c/matrix/CreateDiagonalMatrix/output.vm", context);
        code.addOutputCode(codeStr);
    }

    @Override
    public void updateDimension() throws MatDimException {
        OutputPort out = outputPortList.get(0);
        OutputSignal in = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();

        if (in.getDataType() != DataType.MATRIX) {
            throw new MatDimException("CreateDiagonalMatrix: input is not matrix");
        }

        int n = in.getHeight() > in.getWidth() ? in.getHeight() : in.getWidth();

        out.setHeight(n);
        out.setWidth(n);
        out.getOutputSignalC().setHeight(n);
        out.getOutputSignalC().setWidth(n);
        out.getOutputSignalC().setDataType(DataType.MATRIX);
    }

    /**
     * Calculate output by creating a diagonal matrix from input vector.
     * For input vector v = [v1, v2, ..., vn], creates:
     * [v1  0   0  ... 0 ]
     * [0   v2  0  ... 0 ]
     * [0   0   v3 ... 0 ]
     * [...              ]
     * [0   0   0  ... vn]
     *
     * @param t Current simulation time (unused for this block)
     */
    @Override
    public void calculateOutput(double t) {
        OutputPort out = outputPortList.get(0);

        // Get input vector (can be row or column vector)
        Matrix input = inputPortList.get(0).getData().getMatrix();

        // Determine vector size (max of height and width)
        int n = Math.max(input.getRowDimension(), input.getColumnDimension());

        // Create diagonal matrix
        Matrix result = new Matrix(n, n);

        // Fill diagonal with input vector values
        for (int i = 0; i < n; i++) {
            double value;
            if (input.getRowDimension() == 1) {
                // Row vector
                value = input.get(0, i);
            } else {
                // Column vector
                value = input.get(i, 0);
            }
            result.set(i, i, value);
        }

        // Set output
        out.getOutputSignalC().getData().setMatrix(result);
    }
}
