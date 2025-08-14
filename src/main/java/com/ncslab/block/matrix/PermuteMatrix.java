package com.ncslab.block.matrix;

import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.dto.BlockJson;

import com.ncslab.block.Block;
import com.ncslab.block.data.DataType;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.block.io.InputPort;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import com.ncslab.util.TemplateManager;
// TODO(squarezhong@outlook.com) compeletely wrong
public class PermuteMatrix extends Block {
    
    
    /**
     * DTO-NATIVE Constructor - Creates PermuteMatrix block directly from BlockJson DTO
     */
    public PermuteMatrix(BlockJson blockDto, NCSLabModel model) {
        super(blockDto, model);
        System.out.println("DTO-NATIVE: PermuteMatrix block created successfully - " + blockDto.getBlockName());
    }


    public static final Vector<String> outputNames = new Vector<>();
    public static final Vector<String> inputNames = new Vector<>();

    public static final Map<String, String> PARAMETER_DEFAULTS;
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        // No parameters for PermuteMatrix
    }

    static {

        outputNames.add("out1");
        inputNames.add("in1");
    }
    public PermuteMatrix(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);

        outputPortList.add(new OutputPort(this, 1, true));
        inputPortList.add(new InputPort(this, 1));
    }

    @Override
    public void generateOutputCodeC(CodeStructC code) {
        context.put("block", this);
        context.put("input", getInputPortVariables()[0]);
        context.put("output", getOutputPortVariables()[0]);

        String codeStr = TemplateManager.renderTemplate("c/matrix/PermuteMatrix/output.vm", context);
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
}
