package com.ncslab.block.matrix;

import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.matrix.MatrixMultiplyDto;

import java.util.Map;
import java.util.HashMap;

import com.ncslab.block.Block;
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

public class MatrixMultiply extends Block {

    private String seq;

    
    
    /**
     * DTO-NATIVE Constructor - Creates MatrixMultiply block directly from BlockDto DTO
     */
    public MatrixMultiply(BlockDto blockDto, NCSLabModel model) {
        super(blockDto, model);
        System.out.println("DTO-NATIVE: MatrixMultiply block created successfully - " + blockDto.getBlockName());
    }

    /**
     * Factory method to create MatrixMultiply block from MatrixMultiplyDto.
     *
     * @param dto The MatrixMultiplyDto containing block configuration
     * @param model The NCSLabModel this block belongs to
     * @return New MatrixMultiply block instance
     * @throws BlockCreationException if block creation fails
     */
    public static MatrixMultiply createFromDto(MatrixMultiplyDto dto, NCSLabModel model) throws BlockCreationException {
        return new MatrixMultiply(dto, model);
    }


    public static final List<String> outputNames = new ArrayList<>();
    public static final List<String> inputNames = new ArrayList<>();
    
    public static final Map<String, String> PARAMETER_DEFAULTS = new HashMap<>();

    static {
        
        outputNames.add("out1");
        //输入个数不确定
        
        PARAMETER_DEFAULTS.put("Inputs", "**");
        PARAMETER_DEFAULTS.put("SampleTime", "-1");
        PARAMETER_DEFAULTS.put("OutDataTypeStr", "Inherit: Same as input");
        PARAMETER_DEFAULTS.put("SaturateOnIntegerOverflow", "off");
    }

    public MatrixMultiply(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);

        OutputPort output = new OutputPort(this, 1, true);
        output.setDimThrough(false);
        outputPortList.add(output);
        paraseParamValues();
    }

    /**
     * parse the paramValues to get the inputs
     */
    public void paraseParamValues() {
        seq = paramValues.getString("Inputs");

        for (int i = 0; i < seq.length(); i++) {
            inputPortList.add(new InputPort(this, i + 1));
        }
    }

    @Override
    public void generateOutputCodeC(CodeStructC code) {
        context.put("block", this);
        context.put("inputSignals", getInputPortVariables());
        context.put("output", getOutputPortVariables()[0]);

        String codeStr = TemplateManager.renderTemplate("c/matrix/MatrixMultiply/output.vm", context);
        code.addOutputCode(codeStr);
    }

    @Override
    public void updateDimension() throws MatDimException {
        OutputPort out = outputPortList.get(0);
        OutputSignal signal[] = new OutputSignal[seq.length()];
        int m[] = new int[seq.length()];
        int n[] = new int[seq.length()];
        for (int i = 0; i < seq.length(); i++) {
            signal[i] = inputPortList.get(i).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
            m[i] = signal[i].getHeight();
            n[i] = signal[i].getWidth();
        }

        for (int i = 0; i < seq.length() - 1; i++) {
            if (n[i] != m[i + 1]) {
                MatDimException e = new MatDimException(
                        "Block " + this.blockName + " " + seq.length() + " input dimensions doesn't match !\n \n");
                throw (e);
            }
        }
        out.setHeight(signal[0].getHeight());
        out.setWidth(signal[seq.length() - 1].getWidth());
        out.getOutputSignalC().setHeight(signal[0].getHeight());
        out.getOutputSignalC().setWidth(signal[seq.length() - 1].getWidth());
        out.getOutputSignalC().setDataType(DataType.MATRIX);
    }

    public void checkDimension() throws MatDimException {
    }

    /**
     * Calculate output by performing matrix multiplication.
     * Computes: out = A1 * A2 * A3 * ... * An
     * where the multiplication order follows the input sequence.
     *
     * @param t Current simulation time (unused for this block)
     */
    @Override
    public void calculateOutput(double t) {
        OutputPort out = outputPortList.get(0);

        // Start with first input matrix
        Matrix result = inputPortList.get(0).getData().getMatrix();

        // Multiply by each subsequent input matrix
        for (int i = 1; i < seq.length(); i++) {
            Matrix nextMatrix = inputPortList.get(i).getData().getMatrix();
            result = result.times(nextMatrix);
        }

        // Set output
        out.getOutputSignalC().getData().setMatrix(result);
    }
}
