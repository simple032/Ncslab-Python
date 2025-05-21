package com.ncslab.block.sink;

import lombok.Getter;
import org.apache.velocity.VelocityContext;
import com.ncslab.util.TemplateManager;
import org.json.JSONObject;

import com.ncslab.block.data.DataType;
import com.ncslab.block.io.InputPort;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;

import com.ncslab.block.discrete.DiscreteBlock;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.block.io.OutputPort;

import com.ncslab.block.io.terminal.ScopeStruct;

import com.ncslab.ncslablink.ModelMode;

import java.util.Vector;

public class Scope extends SinkBlock {

    ScopeStruct scopeStruct;

    @Getter
    public static final Vector<String> inputNames = new Vector<>();

    static {
        inputNames.add("in1");
    }

    public Scope(JSONObject scopeIn, NCSLabModel model) {
        super(scopeIn, model);

        // Add an input port
        inputPortList.add(new InputPort(this, 1));
    }

    public void generateOutputCodeM(CodeStructM code) {
        super.generateOutputCodeM(code);
        String outputCode = "";
        outputCode += "if storeEnable>0\n";
        outputCode += getBlockName() + "=[" + getBlockName() 
            + " Block" + getInputPortList().get(0).getLinkedLine().getLinkedOutputPort().getBLock().getBlockId()
            + "_Output" + getInputPortList().get(0).getLinkedLine().getLinkedOutputPort().getNumber()
            + "]"
            + ";\n";
        outputCode += "end\n";
        code.addOutputCode(outputCode);
    }

    public void generateInitCodeM(CodeStructM code) {
        super.generateInitCodeM(code);
        String initCode = "";
        code.addGlobalDefineCode("global " + getBlockName() + ";\n");
        initCode += getBlockName() + "=[];\n";
        initCode += "ScopeNum=ScopeNum+1;\n";
        initCode += "ScopeList=[ScopeList; '" + getBlockName() + "'];\n";
        code.addInitCode(initCode);
    }

    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);
        if (model.getModelMode() == ModelMode.Simulation) {
            context.put("blockId", getBlockId());
            context.put("blockName", getBlockName());
            context.put("scopeStruct", scopeStruct);

            String initCode = TemplateManager.renderTemplate("c/sink/Scope/init.vm", context);
            code.addInitCode(initCode);
        }
    }

    public void generateOutputCodeC(CodeStructC code) {
        if (model.getModelMode() == ModelMode.Simulation) {
            context.put("blockId", getBlockId());
            context.put("blockName", getBlockName());
            context.put("inputPortList", getInputPortList());
            context.put("scopeStruct", scopeStruct);

            String outputCode = TemplateManager.renderTemplate("c/sink/Scope/output.vm", context);
            code.addSinkOutputCode(outputCode);

            String sinkStatusClearCode = "block" + this.getInputPortList().get(0).getLinkedLine().getLinkedOutputPort().getBLock().getBlockId() + ".discreteUpdated = 0;\n";
            code.addSinkStatusClearCode(sinkStatusClearCode);
        }
    }

    public void generateOutputSinkCodeC(CodeStructC code) {
        if (model.getModelMode() == ModelMode.Simulation) {
            context.put("blockId", getBlockId());
            context.put("blockName", getBlockName());
            context.put("inputPortList", getInputPortList());
            context.put("scopeStruct", scopeStruct);

            String outputCode = TemplateManager.renderTemplate("c/sink/Scope/output.vm", context);
            code.addSinkOutputCode(outputCode);

            String sinkStatusClearCode = "block" + this.getInputPortList().get(0).getLinkedLine().getLinkedOutputPort().getBLock().getBlockId() + ".discreteUpdated = 0;\n";
            code.addSinkStatusClearCode(sinkStatusClearCode);
        }
    }

    public void generateTerminateCodeC(CodeStructC code) {
        if (model.getModelMode() == ModelMode.Simulation) {
            String terminateCode = "/*Code for terminate code of block Scope:(" + getBlockId() + ")" + getBlockPath() + "/" + getBlockName() + "*/\n";

            code.addTerminateCode(terminateCode);
        }
    }

    public void updateDimension() throws MatDimException {
    }

    public void checkDimension() throws MatDimException {
        scopeStruct = new ScopeStruct(this, 1, this.blockName);

        OutputSignal signal = this.inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
        scopeStruct.setDimension(signal.getWidth(), signal.getHeight());
        scopeStruct.setMaxDataLength(100000);

        model.addTerminal(scopeStruct);
    }
}
