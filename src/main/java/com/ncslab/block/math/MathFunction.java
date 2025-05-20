package com.ncslab.block.math;

import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.block.Block;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.block.io.InputPort;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;

import java.util.Objects;
import java.util.Vector;
import org.apache.velocity.VelocityContext;
import com.ncslab.util.TemplateManager;

public class MathFunction extends Block {

    private String seq;

    @Getter
    public static final Vector<String> outputNames = new Vector<>();
    @Getter
    public static final Vector<String> inputNames = new Vector<>();

    static {
        outputNames.add("out1");
        inputNames.add("in1");
    }

    public MathFunction(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);
        outputPortList.add(new OutputPort(this, 1, true));
        inputPortList.add(new InputPort(this, 1));

        if (paramValues.has("Operator"))
            seq = paramValues.getString("Operator");
        else
            seq = paramValues.getString("MathFunctionOperator");
        if (Objects.equals(seq, "pow")) {
            inputPortList.add(new InputPort(this, 2));
        }
    }

    public void generateOutputCodeM(CodeStructM code) {
        super.generateOutputCodeM(code);

        String outputCode = "j=" + inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName() + ";\n";
        outputCode += "Block" + this.getBlockId() + "_Output1=" + getSeq() + "(j);\n";
        code.addOutputCode(outputCode);
    }

    public void generateOutputCodeC(CodeStructC code) {
        VelocityContext context = new VelocityContext();
        context.put("blockId", getBlockId());
        context.put("blockName", getBlockName());
        context.put("inputPortList", getInputPortList());
        context.put("outputPortList", getOutputPortList());
        context.put("function", getFunction());

        String codeStr = TemplateManager.renderTemplate("c/math/MathFunction/output.vm", context);
        code.addOutputCode(codeStr);
    }

    private String getFunction() {
        return seq;
    }

    private String getSeq() {
        return seq;
    }

    public void updateDimension() throws MatDimException {
        OutputPort out = outputPortList.get(0);
        InputPort in = inputPortList.get(0);
        OutputSignal signal = in.getLinkedLine().getLinkedOutputPort().getOutputSignalC();

        if (getFunction().equals("transpose")) {
            out.setHeight(signal.getWidth());
            out.setWidth(signal.getHeight());
            out.getOutputSignalC().setHeight(signal.getWidth());
            out.getOutputSignalC().setWidth(signal.getHeight());
            out.getOutputSignalC().setDataType(signal.getDataType());
        } else {
            out.setHeight(signal.getHeight());
            out.setWidth(signal.getWidth());
            out.getOutputSignalC().setHeight(signal.getHeight());
            out.getOutputSignalC().setWidth(signal.getWidth());
            out.getOutputSignalC().setDataType(signal.getDataType());
        }
    }

    public void checkDimension() throws MatDimException {
    }
}
