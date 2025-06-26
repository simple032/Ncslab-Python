package com.ncslab.block.sink;

import com.ncslab.block.data.Data;
import lombok.Getter;
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

    int inportNum; // TODO：兼容后续多输入

    ScopeStruct[] scopeStructs;

    @Getter
    public static final Vector<String> inputNames = new Vector<>();

    static {
        inputNames.add("in1");
    }

    public Scope(JSONObject scopeIn, NCSLabModel model) {
        super(scopeIn, model);

        if(paramValues.has("Inputs")) {
            inportNum = Integer.parseInt(paramValues.getString("Number"));
        }else{
            inportNum = 1;
        }

        scopeStructs = new ScopeStruct[inportNum];

        // Add an input port
        for(int i = 0; i < inportNum; i++) {
            inputPortList.add(new InputPort(this, i+1));
            scopeStructs[i] = new ScopeStruct(this, i+1, "in"+(i+1));
        }

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
            context.put("scopeStruct", scopeStructs[0]);

            String initCode = TemplateManager.renderTemplate("c/sink/Scope/init.vm", context);
            code.addInitCode(initCode);
        }
    }

    public void generateOutputCodeC(CodeStructC code) {
        if (model.getModelMode() == ModelMode.Simulation) {
            context.put("blockId", getBlockId());
            context.put("blockName", getBlockName());
            context.put("inputPortList", getInputPortList());
            context.put("scopeStruct", scopeStructs[0]);

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
            context.put("scopeStruct", scopeStructs[0]);

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

        OutputSignal signal = this.inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
        for(int i = 0; i < inportNum; i++) {
            scopeStructs[i] = new ScopeStruct(this, 1, this.blockName);
            scopeStructs[i].setDimension(signal.getWidth(), signal.getHeight());
            scopeStructs[i].setMaxDataLength(100000);

            model.addTerminal(scopeStructs[i]);
        }
    }

    @Override
    public void calculateDiscreteUpdate(double t){
        if (model.getModelMode() == ModelMode.Simulation) {
            for(int i = 0; i < inportNum; i++) {
                if (scopeStructs[i].getTimeList().isEmpty() || t > scopeStructs[i].getTimeList().lastElement()) {
                    scopeStructs[i].addTimeSeries(
                        t,
                        inputPortList.get(i).getData()
                    );
                }
            }
        }
    }


//    @Override
//    public void calculateTerminate(double t) {
//        if (model.getModelMode() == ModelMode.Simulation) {
//            for(int i = 0; i < inportNum; i++) {
//                if (scopeStructs[i].getTimeList().isEmpty() || t > scopeStructs[i].getTimeList().lastElement()) {
//                    scopeStructs[i].addTimeSeries(
//                        t,
//                        inputPortList.get(i).getData()
//                    );
//                }
//            }
//        }
//    }
}
