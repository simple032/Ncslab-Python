package com.ncslab.block.matrix;

import lombok.Getter;
import org.json.JSONObject;

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

import java.util.Vector;

public class IdentityMatrix extends Block {
    private Parameter outputDimensions;

    @Getter
    public static final Vector<String> parameterNames = new Vector<>();

    @Getter
    public static final Vector<String> outputNames = new Vector<>();
    
    public static final Map<String, String> PARAMETER_DEFAULTS = new HashMap<>();
    static {
        parameterNames.add("outputDimensions");
        parameterNames.add("SampleTime");
        parameterNames.add("OutDataTypeStr");
        parameterNames.add("SaturateOnIntegerOverflow");
        
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
