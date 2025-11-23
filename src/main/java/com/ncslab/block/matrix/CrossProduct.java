package com.ncslab.block.matrix;

import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.matrix.CrossProductDto;

import java.util.Map;
import java.util.HashMap;

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

import java.util.ArrayList;
import java.util.List;
import Jama.Matrix;

public class CrossProduct extends Block {

    
    
    /**
     * DTO-NATIVE Constructor - Creates CrossProduct block directly from BlockDto DTO
     */
    public CrossProduct(BlockDto blockDto, NCSLabModel model) {
        super(blockDto, model);
        System.out.println("DTO-NATIVE: CrossProduct block created successfully - " + blockDto.getBlockName());
    }

    /**
     * Factory method to create CrossProduct block from CrossProductDto.
     *
     * @param dto The CrossProductDto containing block configuration
     * @param model The NCSLabModel this block belongs to
     * @return New CrossProduct block instance
     * @throws BlockCreationException if block creation fails
     */
    public static CrossProduct createFromDto(CrossProductDto dto, NCSLabModel model) throws BlockCreationException {
        return new CrossProduct(dto, model);
    }


    public static final List<String> outputNames = new ArrayList<>();
    public static final List<String> inputNames = new ArrayList<>();
    
    public static final Map<String, String> PARAMETER_DEFAULTS = new HashMap<>();

    static {
        
        outputNames.add("out1");
        inputNames.add("in1");
        inputNames.add("in2");
        
        PARAMETER_DEFAULTS.put("SampleTime", "-1");
        PARAMETER_DEFAULTS.put("OutDataTypeStr", "Inherit: Same as input");
        PARAMETER_DEFAULTS.put("SaturateOnIntegerOverflow", "off");
    }
    public CrossProduct(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);

        outputPortList.add(new OutputPort(this, 1, true));
        inputPortList.add(new InputPort(this, 1));
        inputPortList.add(new InputPort(this, 2));
    }

    @Override
    public void generateOutputCodeC(CodeStructC code) {
        context.put("block", this);
        context.put("input1", inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName());
        context.put("input2", inputPortList.get(1).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName());
        context.put("output", getOutputPortVariables()[0]);

        String codeStr = TemplateManager.renderTemplate("c/matrix/CrossProduct/output.vm", context);
        code.addOutputCode(codeStr);
    }

    @Override
    public void updateDimension() throws MatDimException {
        OutputPort out = outputPortList.get(0);
        OutputSignal in1 = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
        OutputSignal in2 = inputPortList.get(1).getLinkedLine().getLinkedOutputPort().getOutputSignalC();

        if (in1.getDataType() != DataType.MATRIX || in2.getDataType() != DataType.MATRIX) {
            throw new MatDimException("CrossProduct: input is not matrix");
        }

        if (in1.getHeight() * in1.getWidth() != 3 || in2.getHeight() * in2.getWidth() != 3) {
            throw new MatDimException("CrossProduct: input is not 3x1 or 1x3 matrix");
        }

        out.setHeight(1);
        out.setWidth(3);
        out.getOutputSignalC().setHeight(1);
        out.getOutputSignalC().setWidth(3);
        out.getOutputSignalC().setDataType(DataType.MATRIX);
    }

    public void checkDimension() throws MatDimException {
    }

    /**
     * Calculate output by computing the cross product of two 3D vectors.
     * For vectors a = [a1, a2, a3] and b = [b1, b2, b3], computes:
     * a × b = [a2*b3 - a3*b2, a3*b1 - a1*b3, a1*b2 - a2*b1]
     *
     * @param t Current simulation time (unused for this block)
     */
    @Override
    public void calculateOutput(double t) {
        OutputPort out = outputPortList.get(0);

        // Get input matrices (both should be 3-element vectors)
        Matrix input1 = inputPortList.get(0).getData().getMatrix();
        Matrix input2 = inputPortList.get(1).getData().getMatrix();

        // Extract vector elements (handle both row and column vectors)
        double a1, a2, a3, b1, b2, b3;

        if (input1.getRowDimension() == 1) {
            // Row vector [1x3]
            a1 = input1.get(0, 0);
            a2 = input1.get(0, 1);
            a3 = input1.get(0, 2);
        } else {
            // Column vector [3x1]
            a1 = input1.get(0, 0);
            a2 = input1.get(1, 0);
            a3 = input1.get(2, 0);
        }

        if (input2.getRowDimension() == 1) {
            // Row vector [1x3]
            b1 = input2.get(0, 0);
            b2 = input2.get(0, 1);
            b3 = input2.get(0, 2);
        } else {
            // Column vector [3x1]
            b1 = input2.get(0, 0);
            b2 = input2.get(1, 0);
            b3 = input2.get(2, 0);
        }

        // Compute cross product: a × b
        // [a2*b3 - a3*b2, a3*b1 - a1*b3, a1*b2 - a2*b1]
        Matrix result = new Matrix(1, 3);
        result.set(0, 0, a2 * b3 - a3 * b2);
        result.set(0, 1, a3 * b1 - a1 * b3);
        result.set(0, 2, a1 * b2 - a2 * b1);

        // Set output
        out.getOutputSignalC().getData().setMatrix(result);
    }
}
