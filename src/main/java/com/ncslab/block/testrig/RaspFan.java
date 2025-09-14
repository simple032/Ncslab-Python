package com.ncslab.block.testrig;

import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.testrig.RaspFanDto;

import com.ncslab.block.Block;
import com.ncslab.block.data.Data;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.State;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.util.TemplateManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RaspFan extends Block {
    String hardwareDefineName;

    private final double [] num = new double[] {1.659};
    private final double [] den = new double[] {1, 1.849, 1.566};
    private List<State> xStateList = new ArrayList<>();

    /**
     * DTO-NATIVE Constructor - Creates RaspFan block directly from RaspFanDto DTO
     */
    public RaspFan(RaspFanDto blockDto, NCSLabModel model) {
        super(blockDto, model);
        this.isHardware = true;
        inputPortList.add(new InputPort(this, 1));
        outputPortList.add(new OutputPort(this, "FanSpeed", 1, false));
        // Initialize states
        for (int i = 0; i < num.length; i++) {
            State xState = new State(this, i + 1, "x" + (i + 1));
            xStateList.add(xState);
            stateList.add(xState);
        }
        System.out.println("DTO-NATIVE: RaspFan block created successfully - " + blockDto.getBlockName());
    }    

    public static final List<String> outputNames = new ArrayList<>();
    public static final List<String> inputNames = new ArrayList<>();
    @Getter
    public static final HashMap<String, String> PARAMETER_DEFAULTS
            = new HashMap<>();

    static {
        outputNames.add("FanSpeed");
        inputNames.add("in1");
    }

    public RaspFan(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);
        this.isHardware = true;
        inputPortList.add(new InputPort(this, 1));
        outputPortList.add(new OutputPort(this, "FanSpeed", 1, false));
    }

    public String getHardwareDefineCodeC() {
        String hardwareDefineCode = "";
        hardwareDefineName = "Block" + this.getBlockId() + "_RaspFan";
        hardwareDefineCode += "RASPFAN " + hardwareDefineName + ";\n";
        hardwareDefineCode += "HANDLE hComm;\n";
        return hardwareDefineCode;
    }

    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);
        
        hardwareDefineName = "Block" + this.getBlockId() + "_RaspFan";
        
        context.put("block", this);
        context.put("modelMode", model.getModelMode().name());
        context.put("hardwareDefineName", hardwareDefineName);
        
        String codeStr = TemplateManager.renderTemplate("c/testrig/RaspFan/init.vm", context);
        code.addInitCode(codeStr);
    }

    public void generateOutputCodeC(CodeStructC code) {
        context.put("block", this);
        context.put("states", stateList);
        context.put("inputPortVariable", getInputPortVariable(0));
        context.put("outputPortVariables", getOutputPortVariables());
        context.put("modelMode", model.getModelMode().name());

        String codeStr = TemplateManager.renderTemplate("c/testrig/RaspFan/output.vm", context);
        code.addOutputCode(codeStr);
    }

    @Override
    public void calculateInit() {
        
        OutputPort out = outputPortList.get(0);
        // TODO:还需要调试
//        Data data = new Data(num.length > 0 ? num[0] : 0);
        Data data = new Data(0);
        for (State state : xStateList) {
            state.setData(data);
        }
        out.setData(data);
    }

    @Override
    public void calculateOutput(double t) {
        OutputPort out = outputPortList.get(0);
        Data currentState = new Data();   

        int i=num.length-1;
        for (State xState:xStateList) {
            currentState = currentState.plus(xState.getData().times(new Data(num[i])));
            i--;
        }

        out.setData(currentState);
    }

    @Override
    public void calculateDerivative(double t) {

        for(int i = 0; i < xStateList.size() - 1; i++){
            xStateList.get(i).setDerivateData(xStateList.get(i+1).getData());
        }

        Data derivativeData = inputPortList.get(0).getData();;
        int i=den.length-1;
        for(State xState:xStateList) {
            derivativeData = derivativeData.minus(xState.getData().times(new Data(den[i])));
            i--;
        }
        xStateList.get(xStateList.size() - 1).setDerivateData(derivativeData);
    }

}
