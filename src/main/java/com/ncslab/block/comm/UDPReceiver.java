package com.ncslab.block.comm;

import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.block.Block;
import com.ncslab.block.io.OutputPort;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.util.TemplateManager;

import java.util.Vector;

public class UDPReceiver extends Block {

    private String name = "UDPReceiver";

    private String addr;
    private int port;

    @Getter
    public static final Vector<String> outputNames = new Vector<>();

    static {
        outputNames.add("out1");
        outputNames.add("out2");
    }

    public UDPReceiver(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);

        addr = paramValues.getString("RemoteAddr");
        port = paramValues.getInt("LocalPort");

        outputPortList.add(new OutputPort(this, 1, false));
        outputPortList.add(new OutputPort(this, 2, false));
    }

    @Override
    public void calculateInit() {
        // No specific initialization needed for UDPReceiver
    }

    @Override
    public void calculateOutput(double t) {
        // No specific output calculation needed for UDPReceiver
    }

    @Override
    public void calculateDerivative(double t) {
        // No derivative calculation needed for UDPReceiver
    }

    public void generateInitCodeM(CodeStructM code) {
        super.generateInitCodeM(code);
        context.put("block", this);

        String codeStr = TemplateManager.renderTemplate("m/comm/UDPReceiver/init.vm", context);
        code.addInitCode(codeStr);
    }

    public void generateOutputCodeM(CodeStructM code) {
        super.generateOutputCodeM(code);
        context.put("block", this);
        context.put("outputs", getOutputPortVariables());

        String codeStr = TemplateManager.renderTemplate("m/comm/UDPReceiver/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void generateDerivativeCodeM(CodeStructM code) {
        super.generateDerivativeCodeM(code);
        context.put("block", this);

        String codeStr = TemplateManager.renderTemplate("m/comm/UDPReceiver/derivative.vm", context);
        code.addDerivativeCode(codeStr);
    }

    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);
        context.put("block", this);

        String codeStr = TemplateManager.renderTemplate("c/comm/UDPReceiver/init.vm", context);
        code.addInitCode(codeStr);
    }

    public void generateOutputCodeC(CodeStructC code) {
        context.put("block", this);
        context.put("outputs", getOutputPortVariables());

        String codeStr = TemplateManager.renderTemplate("c/comm/UDPReceiver/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void generateDerivativeCodeC(CodeStructC code) {
        super.generateDerivativeCodeC(code);
        context.put("block", this);

        String codeStr = TemplateManager.renderTemplate("c/comm/UDPReceiver/derivative.vm", context);
        code.addDerivativeCode(codeStr);
    }

    public void generateStatementCodeC(CodeStructC code) {
        context.put("block", this);

        String codeStr = TemplateManager.renderTemplate("c/comm/UDPReceiver/statement.vm", context);
        code.addStatementCode(codeStr);
    }
}
