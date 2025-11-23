package com.ncslab.block.matrix;

import lombok.Getter;
import org.checkerframework.checker.units.qual.min;
import org.json.JSONObject;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.matrix.ExtractDiagonalDto;

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

public class ExtractDiagonal extends Block {

    
    
    /**
     * DTO-NATIVE Constructor - Creates ExtractDiagonal block directly from BlockDto DTO
     */
    public ExtractDiagonal(BlockDto blockDto, NCSLabModel model) {
        super(blockDto, model);
        System.out.println("DTO-NATIVE: ExtractDiagonal block created successfully - " + blockDto.getBlockName());
    }

    /**
     * Factory method to create ExtractDiagonal block from ExtractDiagonalDto.
     *
     * @param dto The ExtractDiagonalDto containing block configuration
     * @param model The NCSLabModel this block belongs to
     * @return New ExtractDiagonal block instance
     * @throws BlockCreationException if block creation fails
     */
    public static ExtractDiagonal createFromDto(ExtractDiagonalDto dto, NCSLabModel model) throws BlockCreationException {
        return new ExtractDiagonal(dto, model);
    }


    public static final List<String> outputNames = new ArrayList<>();
    public static final List<String> inputNames = new ArrayList<>();

    public static final Map<String, String> PARAMETER_DEFAULTS;
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        // No parameters for ExtractDiagonal
    }

    static {

        outputNames.add("out1");
        inputNames.add("in1");
    }
    public ExtractDiagonal(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);

        outputPortList.add(new OutputPort(this, 1, true));
        inputPortList.add(new InputPort(this, 1));
    }

    @Override
    public void generateOutputCodeC(CodeStructC code) {
        context.put("block", this);
        context.put("input", inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName());
        context.put("output", getOutputPortVariables()[0]);

        String codeStr = TemplateManager.renderTemplate("c/matrix/ExtractDiagonal/output.vm", context);
        code.addOutputCode(codeStr);
    }

    @Override
    public void updateDimension() throws MatDimException {
        OutputPort out = outputPortList.get(0);
        OutputSignal in = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();

        if (in.getDataType() != DataType.MATRIX) {
            throw new MatDimException("PermuteMatrix: input is not matrix");
        }

        int nColumn = in.getHeight() < in.getWidth() ? in.getHeight() : in.getWidth();

        out.setHeight(1);
        out.setWidth(nColumn);
        out.getOutputSignalC().setHeight(1);
        out.getOutputSignalC().setWidth(nColumn);
        out.getOutputSignalC().setDataType(DataType.MATRIX);
    }

    /**
     * Calculate output by extracting diagonal elements from the input matrix.
     * Returns a row vector containing the diagonal elements.
     *
     * @param t Current simulation time (unused for this block)
     */
    @Override
    public void calculateOutput(double t) {
        OutputPort out = outputPortList.get(0);

        // Get input matrix
        Matrix input = inputPortList.get(0).getData().getMatrix();

        // Determine diagonal length (minimum of rows and columns)
        int rows = input.getRowDimension();
        int cols = input.getColumnDimension();
        int diagonalLength = Math.min(rows, cols);

        // Create result as row vector
        Matrix result = new Matrix(1, diagonalLength);

        // Extract diagonal elements
        for (int i = 0; i < diagonalLength; i++) {
            result.set(0, i, input.get(i, i));
        }

        // Set output
        out.getOutputSignalC().getData().setMatrix(result);
    }
}
