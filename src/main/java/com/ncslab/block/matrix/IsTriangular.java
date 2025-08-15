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
import java.util.ArrayList;
import java.util.List;

public class IsTriangular extends Block {

    
    
    /**
     * DTO-NATIVE Constructor - Creates IsTriangular block directly from BlockJson DTO
     */
    public IsTriangular(BlockJson blockDto, NCSLabModel model) {
        super(blockDto, model);
        System.out.println("DTO-NATIVE: IsTriangular block created successfully - " + blockDto.getBlockName());
    }


    public static final List<String> outputNames = new ArrayList<>();
    public static final List<String> inputNames = new ArrayList<>();

    public static final Map<String, String> PARAMETER_DEFAULTS;
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        // No parameters for IsTriangular
    }

    static {

        outputNames.add("out1");
        inputNames.add("in1");
    }
    public IsTriangular(JSONObject blockJSON, NCSLabModel model) {

        super(blockJSON, model);

        outputPortList.add(new OutputPort(this, 1, true));
        inputPortList.add(new InputPort(this, 1));
    }

    @Override
    public void generateOutputCodeC(CodeStructC code) {
        super.generateOutputCodeC(code);
        
        String outputSignal = outputPortList.get(0).getOutputSignalC().getName();
        String inputSignal = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName();
        
        context.put("block", this);
        context.put("outputSignal", outputSignal);
        context.put("inputSignal", inputSignal);
        
        String codeStr = com.ncslab.util.TemplateManager.renderTemplate("c/matrix/IsTriangular/output.vm", context);
        code.addOutputCode(codeStr);
    }

    @Override
    public void updateDimension() throws MatDimException {
        OutputPort out = outputPortList.get(0);
        OutputSignal in = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();

        if (in.getDataType() != DataType.MATRIX) {
            throw new MatDimException("IsTriangular: input is not matrix");
        }

        out.setHeight(1);
        out.setWidth(1);
        out.getOutputSignalC().setHeight(1);
        out.getOutputSignalC().setWidth(1);
        out.getOutputSignalC().setDataType(DataType.REAL);
    }
}
