package com.ncslab.block.math;

import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.block.Block;
import com.ncslab.block.data.DataType;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;
import org.apache.velocity.VelocityContext;
import com.ncslab.util.TemplateManager;

import java.util.Vector;

public class Bias extends Block {
    private Parameter bias;

    @Getter
    public static final Vector<String> parameterNames = new Vector<>();

    @Getter
    public static final Vector<String> outputNames = new Vector<>();
    @Getter
    public static final Vector<String> inputNames = new Vector<>();

    static {
        outputNames.add("out1");
        inputNames.add("in1");
        parameterNames.add("value");
    }

    public Bias(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);
        inputPortList.add(new InputPort(this, 1));
        outputPortList.add(new OutputPort(this, 1, true));
        bias = new Parameter(this, 1, "value", paramValues.getString("Bias"));
        parameterList.add(bias);
    }

    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);
        String initCode = "/*Code for initialization of block Bias:(" + getBlockId() + ")" + getBlockName() + "*/\n";
        context.put("bias", bias);
        initCode += TemplateManager.renderTemplate("c/math/Bias/init.vm", context);
        code.addInitCode(initCode);
    }

    public void generateOutputCodeC(CodeStructC code) {
        context.put("blockId", getBlockId());
        context.put("blockName", getBlockName());
        context.put("inputPortList", getInputPortList());
        context.put("outputPortList", getOutputPortList());
        context.put("bias", bias);

        String codeStr = TemplateManager.renderTemplate("c/math/Bias/output.vm", context);
        code.addOutputCode(codeStr);
    }


    public void updateDimension() throws MatDimException {
    }

    public void checkDimension() throws MatDimException {
    }
}
