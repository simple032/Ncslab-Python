package com.ncslab.block.discontinuous;

import lombok.Getter;
import org.json.JSONObject;

import com.ncslab.block.Block;
import com.ncslab.block.data.DataType;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;
import org.apache.velocity.VelocityContext;
import com.ncslab.util.TemplateManager;

import java.util.Vector;

public class DeadZone extends Block {
    Parameter lowerValue;
    Parameter upperValue;

    @Getter
    public static final Vector<String> parameterNames = new Vector<>();

    @Getter
    public static final Vector<String> outputNames = new Vector<>();
    @Getter
    public static final Vector<String> inputNames = new Vector<>();

    // Add a method to calculate lower value indices
    private int[] calculateLowerValueIndices(int height, int width) {
        int[] indices = new int[height * width];
        int index = 0;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                indices[index++] = i * width + j;
            }
        }
        return indices;
    }

    static {
        parameterNames.add("LowerValue");
        parameterNames.add("UpperValue");
        outputNames.add("out1");
        inputNames.add("in1");
    }

    public DeadZone(JSONObject blockIn, NCSLabModel model) {
        super(blockIn, model);

        // Initialize ports
        inputPortList.add(new InputPort(this, 1));
        outputPortList.add(new OutputPort(this, 1, true));

        // Initialize parameters
        lowerValue = new Parameter(this, 1, "LowerValue", paramValues.getString("LowerValue"));
        upperValue = new Parameter(this, 2, "UpperValue", paramValues.getString("UpperValue"));
        parameterList.add(lowerValue);
        parameterList.add(upperValue);
    }

    public void generateInitCodeM(CodeStructM code) {
        super.generateInitCodeM(code);
        context.put("block", this);
        context.put("lowerValue", lowerValue);
        context.put("upperValue", upperValue);

        String codeStr = TemplateManager.renderTemplate("m/discontinuous/DeadZone/init.vm", context);
        code.addInitCode(codeStr);
    }

    public void generateOutputCodeM(CodeStructM code) {
        super.generateOutputCodeM(code);

        OutputPort out = outputPortList.get(0);
        OutputPort ops = inputPortList.get(0).getLinkedLine().getLinkedOutputPort();
        OutputSignal signal = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
        context.put("block", this);
        context.put("lowerValue", lowerValue);
        context.put("upperValue", upperValue);
        context.put("out", out);
        context.put("ops", ops);
        context.put("signal", signal);
        context.put("outputs", getOutputPortVariables());

        String codeStr = TemplateManager.renderTemplate("m/discontinuous/DeadZone/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);
        context.put("block", this);
        context.put("lowerValue", lowerValue);
        context.put("upperValue", upperValue);

        String codeStr = TemplateManager.renderTemplate("c/discontinuous/DeadZone/init.vm", context);
        code.addInitCode(codeStr);
    }

    public void generateOutputCodeC(CodeStructC code) {
        OutputPort out = outputPortList.get(0);
        OutputPort ops = inputPortList.get(0).getLinkedLine().getLinkedOutputPort();
        OutputSignal signal = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
        context.put("block", this);

        context.put("lowerValue", lowerValue);
        context.put("upperValue", upperValue);
        context.put("lowerValueWidthIndex", lowerValue.getWidth()-1);
        context.put("lowerValueHeightIndex", lowerValue.getHeight()-1);
        context.put("out", out);
        context.put("ops", ops);
        context.put("opsWidthIndex", ops.getWidth()-1);
        context.put("opsHeightIndex", ops.getHeight()-1);
        context.put("signal", signal);
        context.put("outputs", getOutputPortVariables());

        String codeStr = TemplateManager.renderTemplate("c/discontinuous/DeadZone/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void updateDimension() throws MatDimException {
        OutputPort out = outputPortList.get(0);
        InputPort in = inputPortList.get(0);
        OutputSignal signal = in.getLinkedLine().getLinkedOutputPort().getOutputSignalC();

        if (lowerValue.getWidth() != upperValue.getWidth() || lowerValue.getHeight() != upperValue.getHeight()) {
            MatDimException e = new MatDimException(
                    "Block " + this.blockName + " input dimensions don't match! All input dimensions should be same!");
            throw (e);
        }

        if (lowerValue.getDataType() == DataType.MATRIX && signal.getDataType() == DataType.REAL) {
            out.setHeight(lowerValue.getHeight());
            out.setWidth(lowerValue.getWidth());
            out.getOutputSignalC().setHeight(lowerValue.getHeight());
            out.getOutputSignalC().setWidth(lowerValue.getWidth());
            out.getOutputSignalC().setDataType(DataType.MATRIX);
        } else if (lowerValue.getDataType() == DataType.REAL && signal.getDataType() == DataType.MATRIX) {
            out.setHeight(signal.getHeight());
            out.setWidth(signal.getWidth());
            out.getOutputSignalC().setHeight(signal.getHeight());
            out.getOutputSignalC().setWidth(signal.getWidth());
            out.getOutputSignalC().setDataType(signal.getDataType());
        } else {
            if (lowerValue.getWidth() != signal.getWidth() || lowerValue.getHeight() != signal.getHeight()) {
                MatDimException e = new MatDimException(
                        "Block " + this.blockName + " input dimension doesn't match the gain dimension!\n \n");
                throw (e);
            }

            out.setHeight(lowerValue.getHeight());
            out.setWidth(lowerValue.getWidth());
            out.getOutputSignalC().setHeight(lowerValue.getHeight());
            out.getOutputSignalC().setWidth(lowerValue.getWidth());
            out.getOutputSignalC().setDataType(lowerValue.getDataType());
        }
    }

    public void checkDimension() throws MatDimException {
        // No specific dimension checking needed
    }
}
