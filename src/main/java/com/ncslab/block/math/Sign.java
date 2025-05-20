package com.ncslab.block.math;

import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.block.Block;
import com.ncslab.block.data.DataType;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.block.io.InputPort;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;

import java.util.Vector;
import org.apache.velocity.VelocityContext;
import com.ncslab.util.TemplateManager;

public class Sign extends Block {

    @Getter
    public static final Vector<String> outputNames = new Vector<>();
    @Getter
    public static final Vector<String> inputNames = new Vector<>();

    static {
        outputNames.add("out1");
        inputNames.add("in1");
    }

    public Sign(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);
        outputPortList.add(new OutputPort(this, 1, true));
        inputPortList.add(new InputPort(this, 1));
    }

    public void generateOutputCodeM(CodeStructM code) {
        super.generateOutputCodeM(code);
        OutputPort ops1 = inputPortList.get(0).getLinkedLine().getLinkedOutputPort();
        String outputCode = "";

        switch (ops1.getOutputSignalC().getDataType()) {
            case REAL:
                outputCode += "if " + getInputPortList().get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName() + ">0\n";
                outputCode += getOutputPortList().get(0).getOutputSignalC().getName() + "=1.0;\n";
                outputCode += "elseif " + getInputPortList().get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName() + "==0\n";
                outputCode += getOutputPortList().get(0).getOutputSignalC().getName() + "=0;\n";
                outputCode += "else\n";
                outputCode += getOutputPortList().get(0).getOutputSignalC().getName() + "=-1.0;\n";
                outputCode += "end\n";
                break;
            case MATRIX:
                for (int i = 1; i <= ops1.getHeight(); i++) {
                    for (int j = 1; j <= ops1.getWidth(); j++) {
                        outputCode += "if " + getInputPortList().get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName() + "(" + i + "," + j + ")>0\n";
                        outputCode += getOutputPortList().get(0).getOutputSignalC().getName() + "(" + i + "," + j + ")=1.0;\n";
                        outputCode += "elseif " + getInputPortList().get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName() + "(" + i + "," + j + ")==0\n";
                        outputCode += getOutputPortList().get(0).getOutputSignalC().getName() + "(" + i + "," + j + ")=0;\n";
                        outputCode += "else\n";
                        outputCode += getOutputPortList().get(0).getOutputSignalC().getName() + "(" + i + "," + j + "]=-1.0;\n";
                        outputCode += "end\n";
                    }
                }
                break;
        }

        code.addOutputCode(outputCode);
    }

    public void generateOutputCodeC(CodeStructC code) {
        VelocityContext context = new VelocityContext();
        context.put("blockId", getBlockId());
        context.put("blockName", getBlockName());
        context.put("inputPortList", getInputPortList());
        context.put("outputPortList", getOutputPortList());

        String codeStr = TemplateManager.renderTemplate("c/math/Sign/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void updateDimension() throws MatDimException {
        if (getInputPortList().get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getDataType() == DataType.MATRIX) {
            OutputPort out = outputPortList.get(0);
            OutputSignal signal = getInputPortList().get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();

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
