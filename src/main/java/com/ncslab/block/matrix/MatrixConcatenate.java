package com.ncslab.block.matrix;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.matrix.MatrixConcatenateDto;

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

import com.ncslab.util.TemplateManager;
import Jama.Matrix;

public class MatrixConcatenate extends Block {
    private String seq;
    private Parameter ConcatenateDimension;

    
    
    /**
     * DTO-NATIVE Constructor - Creates MatrixConcatenate block directly from BlockDto DTO
     */
    public MatrixConcatenate(BlockDto blockDto, NCSLabModel model) {
        super(blockDto, model);
        System.out.println("DTO-NATIVE: MatrixConcatenate block created successfully - " + blockDto.getBlockName());
    }

    /**
     * Factory method to create MatrixConcatenate block from MatrixConcatenateDto.
     *
     * @param dto The MatrixConcatenateDto containing block configuration
     * @param model The NCSLabModel this block belongs to
     * @return New MatrixConcatenate block instance
     * @throws BlockCreationException if block creation fails
     */
    public static MatrixConcatenate createFromDto(MatrixConcatenateDto dto, NCSLabModel model) throws BlockCreationException {
        return new MatrixConcatenate(dto, model);
    }


    public static final List<String> outputNames = new ArrayList<>();
    public static final List<String> inputNames = new ArrayList<>();
    
    public static final Map<String, String> PARAMETER_DEFAULTS = new HashMap<>();

    static {
        
        outputNames.add("out1");
        //输入的个数不确定
        
        PARAMETER_DEFAULTS.put("Inputs", "2");
        PARAMETER_DEFAULTS.put("ConcatenateDimension", "1");
        PARAMETER_DEFAULTS.put("SampleTime", "-1");
        PARAMETER_DEFAULTS.put("OutDataTypeStr", "Inherit: Same as input");
        PARAMETER_DEFAULTS.put("SaturateOnIntegerOverflow", "off");
    }

    public MatrixConcatenate(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);

        ConcatenateDimension = getParameterByName("ConcatenateDimension");
        outputPortList.add(new OutputPort(this, 1, true));

        seq = paramValues.getString("Inputs");

        for (int i = 0; i < seq.length(); i++) {
            inputPortList.add(new InputPort(this, i + 1));
        }

    }

    @Override
    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);

        context.put("block", this);
        context.put("ConcatenateDimension", ConcatenateDimension);

        String codeStr = TemplateManager.renderTemplate("c/matrix/MatrixConcatenate/init.vm", context);
        code.addInitCode(codeStr);
    }

    @Override
    public void generateOutputCodeC(CodeStructC code) {
        context.put("block", this);
        context.put("inputSignals", getInputPortVariables());
        context.put("output", getOutputPortVariables()[0]);
        context.put("concatDimension", ConcatenateDimension.getData().getInitValue());

        String codeStr = TemplateManager.renderTemplate("c/matrix/MatrixConcatenate/output.vm", context);
        code.addOutputCode(codeStr);
    }

    @Override
    public void updateDimension() throws MatDimException {
        OutputPort out = outputPortList.get(0);
        OutputSignal signal[] = new OutputSignal[seq.length()];
        int m[] = new int[seq.length()]; // height
        int n[] = new int[seq.length()]; // width
        for (int i = 0; i < seq.length(); i++) {
            signal[i] = inputPortList.get(i).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
            m[i] = signal[i].getHeight();
            n[i] = signal[i].getWidth();
        }

        int dim = (int) ConcatenateDimension.getData().getInitValue();

        if (dim != 1 && dim != 2) {
            throw new MatDimException("MatrixConcatenate: ConcatenateDimension is not 1 or 2");
        }

        for (int i = 0; i < seq.length() - 1; i++) {
            if (dim == 1 && n[i] != n[i + 1]) {
                throw new MatDimException("MatrixConcatenate: input column dimensions doesn't match");
            }
            if (dim == 2 && m[i] != m[i + 1]) {
                throw new MatDimException("MatrixConcatenate: input row dimensions doesn't match");
            }
        }

        int outHeight = (dim == 1) ? Arrays.stream(m).sum() : m[0];
        int outWidth = (dim == 1) ? n[0] : Arrays.stream(n).sum();

        out.setHeight(outHeight);
        out.setWidth(outWidth);
        out.getOutputSignalC().setHeight(outHeight);
        out.getOutputSignalC().setWidth(outWidth);
        out.getOutputSignalC().setDataType(DataType.MATRIX);
    }

    /**
     * Calculate output by concatenating input matrices.
     * Dimension 1: Vertical concatenation (stack rows)
     * Dimension 2: Horizontal concatenation (stack columns)
     *
     * @param t Current simulation time (unused for this block)
     */
    @Override
    public void calculateOutput(double t) {
        OutputPort out = outputPortList.get(0);

        int dim = (int) ConcatenateDimension.getData().getInitValue();
        int numInputs = seq.length();

        // Get all input matrices
        Matrix[] inputs = new Matrix[numInputs];
        for (int i = 0; i < numInputs; i++) {
            inputs[i] = inputPortList.get(i).getData().getMatrix();
        }

        Matrix result;

        if (dim == 1) {
            // Vertical concatenation (concatenate rows)
            int totalRows = 0;
            int cols = inputs[0].getColumnDimension();

            // Calculate total rows
            for (Matrix input : inputs) {
                totalRows += input.getRowDimension();
            }

            // Create result matrix
            result = new Matrix(totalRows, cols);

            // Copy data
            int currentRow = 0;
            for (Matrix input : inputs) {
                int rows = input.getRowDimension();
                for (int i = 0; i < rows; i++) {
                    for (int j = 0; j < cols; j++) {
                        result.set(currentRow + i, j, input.get(i, j));
                    }
                }
                currentRow += rows;
            }
        } else {
            // Horizontal concatenation (concatenate columns)
            int rows = inputs[0].getRowDimension();
            int totalCols = 0;

            // Calculate total columns
            for (Matrix input : inputs) {
                totalCols += input.getColumnDimension();
            }

            // Create result matrix
            result = new Matrix(rows, totalCols);

            // Copy data
            int currentCol = 0;
            for (Matrix input : inputs) {
                int cols = input.getColumnDimension();
                for (int i = 0; i < rows; i++) {
                    for (int j = 0; j < cols; j++) {
                        result.set(i, currentCol + j, input.get(i, j));
                    }
                }
                currentCol += cols;
            }
        }

        // Set output
        out.getOutputSignalC().getData().setMatrix(result);
    }
}
