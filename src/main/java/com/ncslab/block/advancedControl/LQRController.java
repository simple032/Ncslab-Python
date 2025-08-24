package com.ncslab.block.advancedControl;

import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.advancedControl.LQRControllerDto;
import com.ncslab.block.Block;
import com.ncslab.block.data.DataType;
import com.ncslab.block.data.Data;
import com.ncslab.block.io.GlobalVariable;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.util.TemplateManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * only support matrix input
 */
public class LQRController extends Block {
    // u = -Kx, directly related
    private boolean feedThrough = true;

    private Parameter A;
    private Parameter B;
    private Parameter Q;
    private Parameter R;

    private LQRVariable LQR_K;


    public static final List<String> outputNames = new ArrayList<>();
    public static final List<String> inputNames = new ArrayList<>();

    static {
        outputNames.add("out1");
        inputNames.add("in1");
    }

    // === Parameter Defaults ===
    public static final Map<String, String> PARAMETER_DEFAULTS = new HashMap<>();
    static {
        PARAMETER_DEFAULTS.put("A", "[1 1; 0 1]");
        PARAMETER_DEFAULTS.put("B", "[0; 1]");
        PARAMETER_DEFAULTS.put("Q", "[1 0; 0 1]");
        PARAMETER_DEFAULTS.put("R", "1");
    }

    public LQRController(JSONObject blockIn, NCSLabModel model) {
        super(blockIn, model);

        A = new Parameter(this, 1, "A", paramValues.getString("A"));
        B = new Parameter(this, 2, "B", paramValues.getString("B"));
        Q = new Parameter(this, 3, "Q", paramValues.getString("Q"));
        R = new Parameter(this, 4, "R", paramValues.getString("R"));
        this.LQR_K = new LQRVariable(this, 1, "LQR_K", "[]");
        this.globalVariableList.add(this.LQR_K);

        InputPort input = new InputPort(this, 1);
        OutputPort output = new OutputPort(this, 1, feedThrough);
        output.setHeight(B.getWidth());

        inputPortList.add(input);
        outputPortList.add(output);
    }
    
    /**
     * DTO-NATIVE Constructor - Creates LQRController block directly from BlockDto DTO
     */
    public LQRController(LQRControllerDto blockDto, NCSLabModel model) {
        super(blockDto, model);
        // TODO: Add specific initialization if needed
        System.out.println("DTO-NATIVE: LQRController block created successfully - " + blockDto.getBlockName());
    }

    @Override
    public void calculateInit() {
        OutputPort out = outputPortList.get(0);
        for (int i = 0; i < globalVariableList.size(); i++) {
            // Avoid using setData(Data) as it is not defined in GlobalVariable
        }
    }

    @Override
    public void calculateOutput(double t) {
        OutputPort out = outputPortList.get(0);
        Data currentState = new Data();
        Data inputSignal = inputPortList.get(0).getData();

        switch (out.getOutputSignalC().getDataType()) {
            case MATRIX:
                currentState = LQR_K.getData().times(inputSignal).negative();
                break;
            case REAL:
                currentState = new Data(LQR_K.getData().times(inputSignal).getInitValue()).negative();
                break;
        }

        out.setData(currentState);
    }

    @Override
    public void calculateDerivative(double t) {
        // No derivative calculation needed for LQRController
    }

    public void generateInitCodeM(CodeStructM code) {
        super.generateInitCodeM(code);
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);
        context.put("globals", globalVariableList);

        String codeStr = TemplateManager.renderTemplate("m/continuous/LQRController/init.vm", context);
        code.addInitCode(codeStr);
    }

    public void generateOutputCodeM(CodeStructM code) {
        super.generateOutputCodeM(code);
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);
        context.put("globals", globalVariableList);

        String codeStr = TemplateManager.renderTemplate("m/continuous/LQRController/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void generateDerivativeCodeM(CodeStructM code) {
        // No derivative code needed for LQRController
    }

    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);
        context.put("globals", globalVariableList);

        String codeStr = TemplateManager.renderTemplate("c/continuous/LQRController/init.vm", context);
        code.addInitCode(codeStr);
    }

    public void generateOutputCodeC(CodeStructC code) {
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);
        context.put("globals", globalVariableList);

        String codeStr = TemplateManager.renderTemplate("c/continuous/LQRController/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void generateDerivativeCodeC(CodeStructC code) {
        // No derivative code needed for LQRController
    }

    protected class LQRVariable extends GlobalVariable {
        public LQRVariable(Block block, int id, String localName, String dataString) {
            super(block, id, localName, dataString);
        }

        @Override
        public String getInitCodeC() {
            return String.format("%s = %s;\n",
                    LQRVariable.this.getName(),
                    String.format("lqr(%s, %s, %s, %s)", A.getName(), B.getName(), Q.getName(), R.getName()));
        }

        @Override
        public String getDefineCodeC() {
            return String.format("%s %s;\n",
                    "Matrix",
                    LQRVariable.this.getName());
        }
    }
}
