package com.ncslab.block.matrix;

import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.matrix.MatrixSquareDto;

import com.ncslab.block.Block;
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
import com.ncslab.util.TemplateManager;
/**
 * matrix square = A * A^H;
 * for real matrix, A^H = A^T
 */
public class MatrixSquare extends Block {

    
    
    /**
     * DTO-NATIVE Constructor - Creates MatrixSquare block directly from BlockDto DTO
     */
    public MatrixSquare(BlockDto blockDto, NCSLabModel model) {
        super(blockDto, model);
        System.out.println("DTO-NATIVE: MatrixSquare block created successfully - " + blockDto.getBlockName());
    }

    /**
     * Factory method to create MatrixSquare block from MatrixSquareDto.
     *
     * @param dto The MatrixSquareDto containing block configuration
     * @param model The NCSLabModel this block belongs to
     * @return New MatrixSquare block instance
     * @throws BlockCreationException if block creation fails
     */
    public static MatrixSquare createFromDto(MatrixSquareDto dto, NCSLabModel model) throws BlockCreationException {
        return new MatrixSquare(dto, model);
    }


    public static final List<String> outputNames = new ArrayList<>();
    public static final List<String> inputNames = new ArrayList<>();

    public static final Map<String, String> PARAMETER_DEFAULTS;
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        // No parameters for MatrixSquare
    }

    static {

        outputNames.add("out1");
        inputNames.add("in1");
    }
    public MatrixSquare(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);

        outputPortList.add(new OutputPort(this, 1, true));
        inputPortList.add(new InputPort(this, 1));
    }

    @Override
    public void generateOutputCodeC(CodeStructC code) {
        context.put("block", this);
        context.put("input", getInputPortVariables()[0]);
        context.put("output", getOutputPortVariables()[0]);

        String codeStr = TemplateManager.renderTemplate("c/matrix/MatrixSquare/output.vm", context);
        code.addOutputCode(codeStr);
    }

    @Override
    public void updateDimension() throws MatDimException {
        OutputPort out = outputPortList.get(0);
        OutputSignal in = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();

        if (in.getDataType() != DataType.MATRIX) {
            throw new MatDimException("MatrixSquare: input is not matrix");
        }

        out.setHeight(in.getHeight());
        out.setWidth(in.getHeight());
        out.getOutputSignalC().setHeight(in.getHeight());
        out.getOutputSignalC().setWidth(in.getHeight());
        out.getOutputSignalC().setDataType(DataType.MATRIX);
    }
}
