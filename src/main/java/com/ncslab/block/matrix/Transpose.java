package com.ncslab.block.matrix;

import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.matrix.TransposeDto;

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
import java.util.Map;
import java.util.HashMap;
import com.ncslab.util.TemplateManager;
import Jama.Matrix;

public class Transpose extends Block {

    
    
    /**
     * DTO-NATIVE Constructor - Creates Transpose block directly from BlockDto DTO
     */
    public Transpose(BlockDto blockDto, NCSLabModel model) {
        super(blockDto, model);
        System.out.println("DTO-NATIVE: Transpose block created successfully - " + blockDto.getBlockName());
    }

    /**
     * Factory method to create Transpose block from TransposeDto.
     *
     * @param dto The TransposeDto containing block configuration
     * @param model The NCSLabModel this block belongs to
     * @return New Transpose block instance
     * @throws BlockCreationException if block creation fails
     */
    public static Transpose createFromDto(TransposeDto dto, NCSLabModel model) throws BlockCreationException {
        return new Transpose(dto, model);
    }


    public static final List<String> outputNames = new ArrayList<>();
    public static final List<String> inputNames = new ArrayList<>();

    // Parameter defaults matching database format
    public static final Map<String, String> PARAMETER_DEFAULTS;
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("SampleTime", "-1");
        PARAMETER_DEFAULTS.put("OutDataTypeStr", "Inherit: Same as input");
        PARAMETER_DEFAULTS.put("SaturateOnIntegerOverflow", "off");
    }

    static {
        
        outputNames.add("out1");
        inputNames.add("in1");
    }
    public Transpose(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);

        outputPortList.add(new OutputPort(this, 1, true));
        inputPortList.add(new InputPort(this, 1));
    }

    @Override
    public void generateOutputCodeC(CodeStructC code) {
        context.put("block", this);
        context.put("input", getInputPortVariables()[0]);
        context.put("output", getOutputPortVariables()[0]);

        String codeStr = TemplateManager.renderTemplate("c/matrix/Transpose/output.vm", context);
        code.addOutputCode(codeStr);
    }

    @Override
    public void updateDimension() throws MatDimException {
        OutputPort out = outputPortList.get(0);
        OutputSignal in = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();

        if (in.getDataType() != DataType.MATRIX) {
            throw new MatDimException("PermuteMatrix: input is not matrix");
        }

        out.setHeight(in.getWidth());
        out.setWidth(in.getHeight());
        out.getOutputSignalC().setHeight(in.getWidth());
        out.getOutputSignalC().setWidth(in.getHeight());
        out.getOutputSignalC().setDataType(DataType.MATRIX);
    }

    /**
     * Calculate output by transposing the input matrix.
     * For a matrix A, computes A^T (transpose).
     *
     * @param t Current simulation time (unused for this block)
     */
    @Override
    public void calculateOutput(double t) {
        OutputPort out = outputPortList.get(0);

        // Get input matrix
        Matrix input = inputPortList.get(0).getData().getMatrix();

        // Perform transpose
        Matrix result = input.transpose();

        // Set output
        out.getOutputSignalC().getData().setMatrix(result);
    }
}
