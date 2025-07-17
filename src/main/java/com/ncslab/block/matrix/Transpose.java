package com.ncslab.block.matrix;

import lombok.Getter;
import org.json.JSONObject;

import com.ncslab.block.Block;
import com.ncslab.block.data.DataType;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.block.io.InputPort;

import java.util.Vector;
import java.util.Map;
import java.util.HashMap;
import com.ncslab.util.TemplateManager;

public class Transpose extends Block {

    @Getter
    public static final Vector<String> parameterNames = new Vector<>();
    @Getter
    public static final Vector<String> outputNames = new Vector<>();
    @Getter
    public static final Vector<String> inputNames = new Vector<>();

    // Parameter defaults matching database format
    public static final Map<String, String> PARAMETER_DEFAULTS;
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("SampleTime", "-1");
        PARAMETER_DEFAULTS.put("OutDataTypeStr", "Inherit: Same as input");
        PARAMETER_DEFAULTS.put("SaturateOnIntegerOverflow", "off");
    }

    static {
        parameterNames.add("SampleTime");
        parameterNames.add("OutDataTypeStr");
        parameterNames.add("SaturateOnIntegerOverflow");
        
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
}
