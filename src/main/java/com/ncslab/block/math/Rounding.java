package com.ncslab.block.math;

import com.ncslab.block.Block;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;
import lombok.Getter;
import org.json.JSONObject;

import java.util.Vector;
import org.apache.velocity.VelocityContext;
import com.ncslab.util.TemplateManager;

public class Rounding extends Block {

    Parameter operator;
    String operatorString;

    @Getter
    public static final Vector<String> outputNames = new Vector<>();
    @Getter
    public static final Vector<String> inputNames = new Vector<>();

    static {
        outputNames.add("out1");
        inputNames.add("in1");
    }

    public Rounding(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);
        outputPortList.add(new OutputPort(this, 1, true));

        inputPortList.add(new InputPort(this, 1));

        operatorString = paramValues.optString("Operator", "floor");

        operator = new Parameter(this, 1, "operator", operatorString);

        parameterList.add(operator);

        if (getOperatorString().equals("fix")) {
            operatorString = "trunc";
        }
    }

    public void generateOutputCodeC(CodeStructC code) {
        context.put("blockId", getBlockId());
        context.put("blockName", getBlockName());
        context.put("inputPortList", getInputPortList());
        context.put("outputPortList", getOutputPortList());
        context.put("operator", getOperator());

        String codeStr = TemplateManager.renderTemplate("c/math/Rounding/output.vm", context);
        code.addOutputCode(codeStr);
    }

    private Parameter getOperator() {
        return operator;
    }

    private String getOperatorString() {
        return operatorString;
    }

    public void updateDimension() throws MatDimException {
        OutputPort out = outputPortList.get(0);
        InputPort in = inputPortList.get(0);
        OutputSignal signal = in.getLinkedLine().getLinkedOutputPort().getOutputSignalC();

        out.setHeight(signal.getHeight());
        out.setWidth(signal.getWidth());
        out.getOutputSignalC().setHeight(signal.getHeight());
        out.getOutputSignalC().setWidth(signal.getWidth());
        out.getOutputSignalC().setDataType(signal.getDataType());
    }

    public void checkDimension() throws MatDimException {
    }
}
