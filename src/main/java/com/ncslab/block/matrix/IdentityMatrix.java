package com.ncslab.block.matrix;

import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.dto.core.BlockDto;

import java.util.Map;
import java.util.HashMap;

import com.ncslab.block.Block;
import com.ncslab.util.TemplateManager;
import com.ncslab.block.io.Parameter;
import com.ncslab.block.data.DataType;
import com.ncslab.block.io.OutputPort;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;

import java.util.ArrayList;
import java.util.List;

public class IdentityMatrix extends Block {
    private Parameter outputDimensions;


    
    
    /**
     * DTO-NATIVE Constructor - Creates IdentityMatrix block directly from BlockDto DTO
     */
    public IdentityMatrix(BlockDto blockDto, NCSLabModel model) {
        super(blockDto, model);
        System.out.println("DTO-NATIVE: IdentityMatrix block created successfully - " + blockDto.getBlockName());
    }


    public static final List<String> outputNames = new ArrayList<>();
    
    public static final Map<String, String> PARAMETER_DEFAULTS = new HashMap<>();
    static {
        
        outputNames.add("out1");
        
        PARAMETER_DEFAULTS.put("outputDimensions", "3");
        PARAMETER_DEFAULTS.put("SampleTime", "-1");
        PARAMETER_DEFAULTS.put("OutDataTypeStr", "Inherit: Same as input");
        PARAMETER_DEFAULTS.put("SaturateOnIntegerOverflow", "off");
    }

    public IdentityMatrix(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);

        outputDimensions = new Parameter(this, 1, "outputDimensions", paramValues.getString("outputDimensions"));

        outputPortList.add(new OutputPort(this, 1, false));
    }

    @Override
    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);

        context.put("block", this);
        context.put("outputDimensions", outputDimensions);

        String codeStr = TemplateManager.renderTemplate("c/matrix/IdentityMatrix/init.vm", context);
        code.addInitCode(codeStr);
    }

    @Override
    public void generateOutputCodeC(CodeStructC code) {
        context.put("block", this);
        context.put("dimensions", outputDimensions.getData().getInitValue());

        String codeStr = TemplateManager.renderTemplate("c/matrix/IdentityMatrix/output.vm", context);
        code.addOutputCode(codeStr);
    }

    @Override
    public void updateDimension() throws MatDimException {
        OutputPort out = outputPortList.get(0);
        int n = (int)outputDimensions.getData().getInitValue();
        out.setHeight(n);
        out.setWidth(n);
        out.getOutputSignalC().setHeight(n);
        out.getOutputSignalC().setWidth(n);
        out.getOutputSignalC().setDataType(DataType.MATRIX);
    }
}
