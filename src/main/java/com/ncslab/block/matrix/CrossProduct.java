package com.ncslab.block.matrix;

import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.dto.core.BlockDto;

import java.util.Map;
import java.util.HashMap;

import com.ncslab.block.Block;
import com.ncslab.util.TemplateManager;
import com.ncslab.block.data.DataType;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.block.io.InputPort;

import java.util.ArrayList;
import java.util.List;

public class CrossProduct extends Block {

    
    
    /**
     * DTO-NATIVE Constructor - Creates CrossProduct block directly from BlockDto DTO
     */
    public CrossProduct(BlockDto blockDto, NCSLabModel model) {
        super(blockDto, model);
        System.out.println("DTO-NATIVE: CrossProduct block created successfully - " + blockDto.getBlockName());
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
}
