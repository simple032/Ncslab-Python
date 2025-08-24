package com.ncslab.block.driver;

import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.driver.ObserverDto;
import com.ncslab.block.Block;
import com.ncslab.block.data.DataType;
import com.ncslab.block.io.*;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

public class Observer extends Block {
    
    
    /**
     * DTO-NATIVE Constructor - Creates Observer block directly from BlockDto DTO
     */
    public Observer(ObserverDto blockDto, NCSLabModel model) {
        super(blockDto, model);
        System.out.println("DTO-NATIVE: Observer block created successfully - " + blockDto.getBlockName());
    }


    public static final List<String> outputNames = new ArrayList<>();
    public static final List<String> inputNames = new ArrayList<>();
    
    public static final Map<String, String> PARAMETER_DEFAULTS;

    private String observerName;

    static {
        inputNames.add("i1");
        inputNames.add("i2");
        outputNames.add("o1");
        outputNames.add("o2");
        outputNames.add("o3");
        
        // Observer typically has no configurable parameters, using fixed matrices
        PARAMETER_DEFAULTS = new HashMap<>();
        // No parameters to add for this observer implementation
    }

    public Observer(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);

        for(int i = 0; i < inputNames.size(); i++) {
            inputPortList.add(new InputPort(this, i+1));
        }

        for(int i = 0; i < outputNames.size(); i++) {
            outputPortList.add(new OutputPort(this, i+1, true));
        }

        observerName = getBlockName();
    }

    protected void paraseParamValues() {

    }

    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);
        
        context.put("block", this);
        context.put("observerName", observerName);
        context.put("x1Signal", outputPortList.get(0).getOutputSignalC().getName());
        context.put("x2Signal", outputPortList.get(1).getOutputSignalC().getName());
        context.put("x3Signal", outputPortList.get(2).getOutputSignalC().getName());
        
        String codeStr = com.ncslab.util.TemplateManager.renderTemplate("c/driver/Observer/init.vm", context);
        code.addInitCode(codeStr);
    }

    public void generateOutputCodeC(CodeStructC code) {
        super.generateOutputCodeC(code);
        
        String x1Name = outputPortList.get(0).getOutputSignalC().getName();
        String x2Name = outputPortList.get(1).getOutputSignalC().getName();
        String x3Name = outputPortList.get(2).getOutputSignalC().getName();
        String yName = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName();
        String uName = inputPortList.get(1).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName();
        
        context.put("block", this);
        context.put("observerName", observerName);
        context.put("x1Signal", x1Name);
        context.put("x2Signal", x2Name);
        context.put("x3Signal", x3Name);
        context.put("ySignal", yName);
        context.put("uSignal", uName);
        
        String codeStr = com.ncslab.util.TemplateManager.renderTemplate("c/driver/Observer/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void updateDimension() throws MatDimException {
        // 设置输出维度，都是标量输出
        for(OutputPort out : outputPortList) {
            out.setHeight(1);
            out.setWidth(1);
            out.getOutputSignalC().setHeight(1);
            out.getOutputSignalC().setWidth(1);
            out.getOutputSignalC().setDataType(DataType.REAL);
        }
    }

    public void checkDimension() throws MatDimException {
        // 检查输入信号维度，应该都是标量
        for(InputPort in : inputPortList) {
            if(in.getLinkedLine().getLinkedOutputPort().getHeight() != 1 ||
                in.getLinkedLine().getLinkedOutputPort().getWidth() != 1) {
                throw new MatDimException("Block " + blockName + ": Input dimension mismatch, expect scalar input");
            }
        }
    }
}
