package com.ncslab.block.math;

import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.dto.BlockJson;

import com.ncslab.block.Block;
import com.ncslab.block.data.DataType;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.util.TemplateManager;
import java.util.Vector;
import java.util.Map;
import java.util.HashMap;

public class abc2dq0 extends Block{
    String function;


    
    
    /**
     * DTO-NATIVE Constructor - Creates abc2dq0 block directly from BlockJson DTO
     */
    public abc2dq0(BlockJson blockDto, NCSLabModel model) {
        super(blockDto, model);
        System.out.println("DTO-NATIVE: abc2dq0 block created successfully - " + blockDto.getBlockName());
    }


    public static final Vector<String> outputNames = new Vector<>();
    public static final Vector<String> inputNames = new Vector<>();

    // Parameter defaults matching database format
    public static final Map<String, String> PARAMETER_DEFAULTS;
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("rotatingFrame", "Stationary reference frame");
        PARAMETER_DEFAULTS.put("SampleTime", "-1");
        PARAMETER_DEFAULTS.put("OutDataTypeStr", "Inherit: Same as input");
        PARAMETER_DEFAULTS.put("SaturateOnIntegerOverflow", "off");
    }

    static {
        
        outputNames.add("out1");
        outputNames.add("out2");
        outputNames.add("out3");
        inputNames.add("in1");
        inputNames.add("in2");
        inputNames.add("in3");
        inputNames.add("in4");
    }

    public abc2dq0(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);
        inputPortList.add(new InputPort(this, 1));
        inputPortList.add(new InputPort(this, 2));
        inputPortList.add(new InputPort(this, 3));
        inputPortList.add(new InputPort(this, 4));

        outputPortList.add(new OutputPort(this, 1, true));
        outputPortList.add(new OutputPort(this, 2, true));
        outputPortList.add(new OutputPort(this, 3, true));

        function = paramValues.getString("rotatingFrame");
    }

    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);
        String initCode = "/*Code for initialization of block abc2dq0:(" + getBlockId() + ")" + getBlockName() + "*/\n";
        code.addInitCode(initCode);
    }

    public void generateOutputCodeC(CodeStructC code) {
        context.put("blockId", getBlockId());
        context.put("blockName", getBlockName());
        context.put("inputPortList", getInputPortList());
        context.put("outputPortList", getOutputPortList());
        context.put("function", getFunction());

        String codeStr = TemplateManager.renderTemplate("c/math/abc2dq0/output.vm", context);
        code.addOutputCode(codeStr);
    }

    private String getFunction() {
        return function;
    }

    public void updateDimension() throws MatDimException {
    }

    public void checkDimension() throws MatDimException {
    }
}
