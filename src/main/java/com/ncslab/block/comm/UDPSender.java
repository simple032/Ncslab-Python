package com.ncslab.block.comm;

import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.comm.UDPSenderDto;
import com.ncslab.block.Block;
import com.ncslab.block.io.InputPort;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.util.TemplateManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class UDPSender extends Block {

    private String name = "UDPSender";

    private String addr;
    private int port;

    
    
    
    /**
     * DTO-NATIVE Constructor - Creates UDPSender block directly from BlockDto DTO
     */
    public UDPSender(UDPSenderDto blockDto, NCSLabModel model) {
        super(blockDto, model);

        // Create input ports (can accept scalar or vector data)
        inputPortList.add(new InputPort(this, 1));
        inputPortList.add(new InputPort(this, 2));

        System.out.println("DTO-NATIVE: UDPSender block created successfully - " + blockDto.getBlockName());
    }



    
    public static final List<String> inputNames = new ArrayList<>();

    public static final Map<String, String> PARAMETER_DEFAULTS = new HashMap<>();

    static {
        
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

    /**
     * Override checkDimension to allow both REAL and MATRIX inputs.
     * UDPSender supports sending both scalar values and vector/matrix data.
     */
    @Override
    public void checkDimension() throws com.ncslab.ncslablink.MatDimException {
        // UDPSender accepts both REAL (scalar) and MATRIX (vector) inputs
        // No dimension checking needed - both types are supported
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
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);
        context.put("addr", addr);
        context.put("port", port);
        String codeStr = TemplateManager.renderTemplate("c/comm/UDPSender/init.vm", context);
        code.addInitCode(codeStr);
    }

    public void generateOutputCodeC(CodeStructC code) {
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);
        context.put("inputs", getInputPortVariables());

        String codeStr = TemplateManager.renderTemplate("c/comm/UDPSender/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void generateDerivativeCodeC(CodeStructC code) {
        super.generateDerivativeCodeC(code);
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        String codeStr = TemplateManager.renderTemplate("c/comm/UDPSender/derivative.vm", context);
        code.addDerivativeCode(codeStr);
    }

    public void generateStatementCodeC(CodeStructC code) {
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        String codeStr = TemplateManager.renderTemplate("c/comm/UDPSender/statement.vm", context);
        code.addStatementCode(codeStr);
    }
}
