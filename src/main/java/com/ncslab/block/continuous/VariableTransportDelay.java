package com.ncslab.block.continuous;

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

import java.util.Vector;
import com.ncslab.util.TemplateManager;

public class VariableTransportDelay extends Block {

    Parameter DelayType;
    Parameter MaxDelayTime;
    Parameter InitialOutput;
    Parameter InitialBuffsize;
    Parameter PadeOrder;

    @Getter
    public static final Vector<String> parameterNames = new Vector<>();

    @Getter
    public static final Vector<String> outputNames = new Vector<>();
    @Getter
    public static final Vector<String> inputNames = new Vector<>();

    static {
        parameterNames.add("DelayType");
        parameterNames.add("MaxDelayTime");
        parameterNames.add("InitialOutput");
        parameterNames.add("InitialBuffsize");
        parameterNames.add("PadeOrder");
        outputNames.add("out1");
        inputNames.add("in1");
        inputNames.add("in2");
    }

    public VariableTransportDelay(JSONObject blockIn, NCSLabModel model) {
        super(blockIn, model);

        // 2个输入，1个输出
        inputPortList.add(new InputPort(this, 1));
        inputPortList.add(new InputPort(this, 2));
        outputPortList.add(new OutputPort(this, 1, false));

        DelayType = new Parameter(this, parameterList.size() + 1, "DelayType", paramValues.getString("VariableDelayType"));
        parameterList.add(DelayType);
        MaxDelayTime = new Parameter(this, parameterList.size() + 1, "MaxDelayTime", paramValues.getString("MaxDelayTime"));
        parameterList.add(MaxDelayTime);
        InitialOutput = new Parameter(this, parameterList.size() + 1, "InitialOutput", paramValues.getString("InitialOutput"));
        parameterList.add(InitialOutput);
        InitialBuffsize = new Parameter(this, parameterList.size() + 1, "InitialBuffsize", paramValues.getString("InitialBuffsize"));
        parameterList.add(InitialBuffsize);
        PadeOrder = new Parameter(this, parameterList.size() + 1, "PadeOrder", paramValues.getString("PadeOrder"));
        parameterList.add(PadeOrder);
    }

    public void generateArraysCodeC(CodeStructC code) {        
        context.put("block", this);
        context.put("blockId", getBlockId());
        context.put("blockName", getBlockName());
        context.put("maxDelayTime", MaxDelayTime.getData().getInitValue());
        context.put("initialBufferSize", InitialBuffsize.getData().getInitValue());

        String arraysCode = TemplateManager.renderTemplate("c/continuous/VariableTransportDelay/arrays.vm", context);
        code.addArraysCode(arraysCode);
    }

    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);
        context.put("block", this);
        context.put("MaxDelayTime", MaxDelayTime);
        context.put("PadeOrder", PadeOrder);
        context.put("InitialOutput", InitialOutput);
        String codeStr = TemplateManager.renderTemplate("c/continuous/VariableTransportDelay/init.vm", context);
        code.addInitCode(codeStr);
    }

    public String getCurrentIndexName(){
        return "currentIndex_VariableTransportDelay" + getBlockId();
    }

    public void generateOutputCodeC(CodeStructC code) {
        context.put("block", this);
        context.put("signal", inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC());
        context.put("signal2", inputPortList.get(1).getLinkedLine().getLinkedOutputPort().getOutputSignalC());
        context.put("MaxDelayTime", MaxDelayTime);
        context.put("InitialOutput", InitialOutput);
        context.put("outputs", getOutputPortVariables());
        String codeStr = TemplateManager.renderTemplate("c/continuous/VariableTransportDelay/output.vm", context);
        code.addOutputCode(codeStr);
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
        if (MaxDelayTime.getDataType() != DataType.REAL || InitialOutput.getDataType() != DataType.REAL || InitialBuffsize.getDataType() != DataType.REAL || PadeOrder.getDataType() != DataType.REAL) {
            MatDimException e = new MatDimException("Parameter of Block " + this.blockName + " can't be Matrix!\n \n");
            throw (e);
        }
    }
}
