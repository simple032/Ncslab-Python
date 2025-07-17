package com.ncslab.block.comm;

import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.block.Block;
import com.ncslab.block.io.InputPort;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.util.TemplateManager;

import java.util.Vector;
import java.util.Map;
import java.util.HashMap;

public class UDPSender extends Block {

    private String name = "UDPSender";

    private String addr;
    private int port;

    @Getter
    public static final Vector<String> parameterNames = new Vector<>();
    
    @Getter
    public static final Vector<String> inputNames = new Vector<>();

    @Getter
    public static final Map<String, String> PARAMETER_DEFAULTS = new HashMap<>();

    static {
        parameterNames.add("RemoteAddr");
        parameterNames.add("RemotePort");
        
        inputNames.add("in1");
        inputNames.add("in2");
        
        PARAMETER_DEFAULTS.put("RemoteAddr", "127.0.0.1");
        PARAMETER_DEFAULTS.put("RemotePort", "8080");
    }

    public UDPSender(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);

        addr = paramValues.getString("RemoteAddr");
        port = paramValues.getInt("RemotePort");

        inputPortList.add(new InputPort(this, 1));
        inputPortList.add(new InputPort(this, 2));
    }

    @Override
    public void calculateInit() {
        // No specific initialization needed for UDPSender
    }

    @Override
    public void calculateOutput(double t) {
        // No specific output calculation needed for UDPSender
    }

    @Override
    public void calculateDerivative(double t) {
        // No derivative calculation needed for UDPSender
    }

    public void generateInitCodeM(CodeStructM code) {
        super.generateInitCodeM(code);
        context.put("block", this);

        String codeStr = TemplateManager.renderTemplate("m/comm/UDPSender/init.vm", context);
        code.addInitCode(codeStr);
    }

    public void generateOutputCodeM(CodeStructM code) {
        super.generateOutputCodeM(code);
        context.put("block", this);
        context.put("inputs", getInputPortVariables());

        String codeStr = TemplateManager.renderTemplate("m/comm/UDPSender/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void generateDerivativeCodeM(CodeStructM code) {
        super.generateDerivativeCodeM(code);
        context.put("block", this);

        String codeStr = TemplateManager.renderTemplate("m/comm/UDPSender/derivative.vm", context);
        code.addDerivativeCode(codeStr);
    }

    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);
        context.put("block", this);
        context.put("addr", addr);
        context.put("port", port);
        String codeStr = TemplateManager.renderTemplate("c/comm/UDPSender/init.vm", context);
        code.addInitCode(codeStr);
    }

    public void generateOutputCodeC(CodeStructC code) {
        context.put("block", this);
        context.put("inputs", getInputPortVariables());

        String codeStr = TemplateManager.renderTemplate("c/comm/UDPSender/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void generateDerivativeCodeC(CodeStructC code) {
        super.generateDerivativeCodeC(code);
        context.put("block", this);

        String codeStr = TemplateManager.renderTemplate("c/comm/UDPSender/derivative.vm", context);
        code.addDerivativeCode(codeStr);
    }

    public void generateStatementCodeC(CodeStructC code) {
        context.put("block", this);

        String codeStr = TemplateManager.renderTemplate("c/comm/UDPSender/statement.vm", context);
        code.addStatementCode(codeStr);
    }
}
