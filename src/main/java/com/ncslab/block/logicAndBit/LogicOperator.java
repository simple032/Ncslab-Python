package com.ncslab.block.logicAndBit;

import com.ncslab.block.data.DataType;
import com.ncslab.util.TemplateManager;
import lombok.Getter;
import org.apache.velocity.VelocityContext;
import org.json.JSONObject;

import com.ncslab.block.Block;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;

import java.util.Vector;

public class LogicOperator extends Block {
    private double num;

    @Getter
    public static final Vector<String> outputNames = new Vector<>();
    @Getter
    public static final Vector<String> inputNames = new Vector<>();

    static {
        outputNames.add("out1");
    }

    public LogicOperator(JSONObject blockIn, NCSLabModel model) {
        super(blockIn, model);
        OutputPort output = new OutputPort(this, 1, true);
        output.setDimThrough(false);
        outputPortList.add(output);
        paraseParamValues();
    }

    public void paraseParamValues() {
        num = paramValues.getDouble("Inputs");
        for (int i = 0; i < num; i++) {
            inputPortList.add(new InputPort(this, i + 1));
        }
    }

    public void generateOutputCodeC(CodeStructC code) {
        OutputPort out  = outputPortList.get(0);
        OutputSignal signal1=inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
        String operator=paramValues.getString("Operator");
        OutputSignal signal[]=new OutputSignal[(int) num];
        for(int i = 0; i < num; i++) {
            signal[i] = inputPortList.get(i).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
        }
        context.put("block", this);
        context.put("inputs", inputPortList); // 输入端口列表
        context.put("inputLength", num);
        context.put("outputs", getOutputPortVariables()); // 输出端口变量（假设为List<OutputSignal>）
        context.put("opsName", out.getOutputSignalC().getName());
        context.put("operator", operator);
        context.put("signal1", signal1);
        String codeStr = TemplateManager.renderTemplate("c/logicAndBit/LogicOperator/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void updateDimension() throws MatDimException {
        OutputPort out = outputPortList.get(0);
        OutputSignal[] signal = new OutputSignal[(int) num];
        for (int i = 0; i < num; i++) {
            signal[i] = inputPortList.get(i).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
        }
        int m = signal[0].getHeight();
        int n = signal[0].getWidth();
        int v = 1;
        for (OutputSignal x : signal) {
            if ((x.getHeight() != m) || (x.getWidth() != n)) {
                v = 0;
                MatDimException e = new MatDimException("Block " + this.blockName + " input dimensions doesn't match !\n \n");
                throw (e);
            }
        }

        if (v == 1) {
            out.setHeight(signal[0].getHeight());
            out.setWidth(signal[0].getWidth());
            out.getOutputSignalC().setHeight(signal[0].getHeight());
            out.getOutputSignalC().setWidth(signal[0].getWidth());
            out.getOutputSignalC().setDataType(signal[0].getDataType());
        }
    }

    public void checkDimension() throws MatDimException {
        if (paramValues.getString("Operator").equals("NOT") && num > 1) {
            MatDimException e = new MatDimException("when Block " + this.blockName + " operater is NOT, there must be one input!\n \n");
            throw (e);
        }
    }
}
