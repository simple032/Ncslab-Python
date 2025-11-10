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
import com.ncslab.ncslablink.BlockCreationException;
import com.ncslab.ncslablink.ModelMode;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.util.TemplateManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RaspFan extends Block {
    String hardwareDefineName;

    // Transfer function coefficients for fan dynamics: H(s) = 1.659 / (s^2 + 1.849s + 1.566)
    // Raw coefficients (before processing)
    private static final double[] RAW_NUM = new double[] {1.659};
    private static final double[] RAW_DEN = new double[] {1.0, 1.849, 1.566};

    // Processed coefficients (after normalization, feedthrough extraction, and padding)
    private double[] num;
    private double[] den;
    private double D;
    private boolean feedThrough;

    // States for simulation mode (second-order transfer function)
    private List<State> xStateList = new ArrayList<>();

    /**
     * Process transfer function coefficients following TransferFcn logic:
     * 1. Normalize by leading denominator coefficient
     * 2. Extract feedthrough if same order
     * 3. Remove leading 1.0 from denominator
     * 4. Pad numerator with leading zeros
     */
    private void parseTransferFunction() {
        // Copy raw coefficients
        num = Arrays.copyOf(RAW_NUM, RAW_NUM.length);
        den = Arrays.copyOf(RAW_DEN, RAW_DEN.length);

        // Normalize by leading coefficient of denominator
        double unit = den[0];
        if (Math.abs(unit) < 1e-15) {
            throw new BlockCreationException("RaspFan leading coefficient of denominator cannot be zero");
        }

        for (int i = 0; i < den.length; i++) {
            den[i] = den[i] / unit;
        }

        for (int i = 0; i < num.length; i++) {
            num[i] = num[i] / unit;
        }

        // Check for direct feedthrough (same order in numerator and denominator)
        if (num.length == den.length) {
            feedThrough = true;
            D = num[0] / den[0];

            // Remove direct feedthrough from numerator
            for (int i = 0; i < num.length; i++) {
                num[i] = num[i] - D * den[i];
            }

            // Reduce order of numerator
            if (num.length > 1) {
                double[] numShort = new double[num.length - 1];
                System.arraycopy(num, 1, numShort, 0, num.length - 1);
                num = numShort;
            } else {
                num = new double[0]; // Empty array if single element
            }
        } else {
            feedThrough = false;
            D = 0.0;
        }

        // Remove leading coefficient from denominator (it's now 1)
        if (den.length > 1) {
            double[] denShort = new double[den.length - 1];
            System.arraycopy(den, 1, denShort, 0, den.length - 1);
            den = denShort;
        } else {
            den = new double[0]; // Empty array if single element
        }

        // Pad numerator with leading zeros if necessary
        if (den.length > 0) {
            double[] numShort = new double[den.length];
            for (int i = 0; i < den.length; i++) {
                if (i < den.length - num.length) {
                    numShort[i] = 0;
                } else {
                    int srcIndex = i - (den.length - num.length);
                    if (srcIndex >= 0 && srcIndex < num.length) {
                        numShort[i] = num[srcIndex];
                    } else {
                        numShort[i] = 0; // Should not happen with proper bounds
                    }
                }
            }
            num = numShort;
        }
    }

    /**
     * DTO-NATIVE Constructor - Creates RaspFan block directly from RaspFanDto DTO
     */
    public RaspFan(BlockDto blockDto, NCSLabModel model) {
        super(blockDto, model);
        this.isHardware = true;
        inputPortList.add(new InputPort(this, 1));
        outputPortList.add(new OutputPort(this, "FanSpeed", 1, false));

        // Process transfer function coefficients
        parseTransferFunction();

        // Initialize states based on denominator order (num.length after padding)
        for (int i = 0; i < num.length; i++) {
            State xState = new State(this, i + 1, "x" + (i + 1));
            xStateList.add(xState);
            stateList.add(xState);
        }
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

        // Process transfer function coefficients
        parseTransferFunction();

        // Initialize states
        for (int i = 0; i < num.length; i++) {
            State xState = new State(this, i + 1, "x" + (i + 1));
            xStateList.add(xState);
            stateList.add(xState);
        }
        context.put("states", xStateList);
    }

    /**
     * Check if we're in simulation mode
     */
    private boolean isSimulationMode() {
        return model.getModelMode() == ModelMode.Simulation;
    }

    public String getHardwareDefineCodeC() {
        String hardwareDefineCode = "";
        hardwareDefineName = "Block" + this.getBlockId() + "_RaspFan";
        hardwareDefineCode += "RASPFAN " + hardwareDefineName + ";\n";
        hardwareDefineCode += "HANDLE hComm;\n";
        return hardwareDefineCode;
    }

    @Override
    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);
        if (isSimulationMode()) {
            // Simulation mode: Use TransferFcn template with proper context
            context.put("states", xStateList);

            String codeStr = TemplateManager.renderTemplate("c/continuous/TransferFcn/init.vm", context);
            code.addInitCode(codeStr);
        } else {
            // Simulation mode: Use TransferFcn-based templates with fan dynamics
            // Populate context with internal TransferFcn's states and parameters                     
            // Compilation mode: use hardware-specific template            
            hardwareDefineName = "Block" + this.getBlockId() + "_RaspFan";
            context.put("hardwareDefineName", hardwareDefineName);

            String codeStr = TemplateManager.renderTemplate("c/testrig/RaspFan/init.vm", context);
            code.addInitCode(codeStr);
        }
    }

    @Override
    public void generateOutputCodeC(CodeStructC code) {
        if (isSimulationMode()) {
            // Simulation mode: Use TransferFcn-based templates with fan dynamics
            com.ncslab.util.TemplateUtils.populateAllContext(context, this);

            // Fail fast - validate required states exist
            context.put("states", xStateList);

            // Fail fast - validate numerator array exists
            context.put("num", Arrays.stream(num).boxed().collect(Collectors.toList()));

            context.put("feedThrough", feedThrough);
            context.put("D", D);

            // Add individual state names for easy template access
            for (int i = 0; i < xStateList.size(); i++) {
                State state = xStateList.get(i);
                context.put("stateName" + i, state.getName());
            }

            if (!xStateList.isEmpty()) {
                State firstState = xStateList.get(0);
                context.put("stateName", context.get(firstState.getLocalName())); // Use state local name mapped by TemplateUtils // C variable name // For single state access
            }
            String codeStr = TemplateManager.renderTemplate("c/continuous/TransferFcn/output.vm", context);
            code.addOutputCode(codeStr);
        } else {
            // Compilation mode: use hardware-specific template
            com.ncslab.util.TemplateUtils.populateAllContext(context, this);
            hardwareDefineName = "Block" + this.getBlockId() + "_RaspFan";
            context.put("blockOutputPortVariables", java.util.Arrays.asList(getOutputPortVariables()));
            context.put("hardwareDefineName", hardwareDefineName);

            String codeStr = TemplateManager.renderTemplate("c/testrig/RaspFan/output.vm", context);
            code.addOutputCode(codeStr);
        }
    }

    @Override
    public void generateDerivativeCodeC(CodeStructC code) {
        if (isSimulationMode()) {
            com.ncslab.util.TemplateUtils.populateAllContext(context, this);

            // Fail fast - validate required states exist
            context.put("states", xStateList);

            // Fail fast - validate denominator array exists
            context.put("den", Arrays.stream(den).boxed().collect(Collectors.toList()));

            // Add individual state names for easy template access
            for (int i = 0; i < xStateList.size(); i++) {
                State state = xStateList.get(i);
                context.put("stateName" + i, state.getName());
                context.put("stateDerivativeName" + i, state.getDerivativeName());
            }

            if (!xStateList.isEmpty()) {
                State firstState = xStateList.get(0);
                context.put("stateName", context.get(firstState.getLocalName())); // Use state local name mapped by TemplateUtils // C variable name // For single state access
            }
            String codeStr = TemplateManager.renderTemplate("c/continuous/TransferFcn/derivative.vm", context);
            code.addDerivativeCode(codeStr);
        }
        // Compilation mode: no derivative code needed (hardware-only, no continuous states)
    }

    @Override
    public void calculateInit() {
        OutputPort out = outputPortList.get(0);
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

        // Calculate output with num coefficients in reverse order (matches controller canonical form with padded numerator)
        // For padded num array, we need: y = num[last]*x1 + num[last-1]*x2 + ...
        int numIndex = num.length - 1;
        for (State xState : xStateList) {
            if (numIndex >= 0) {
                currentState = currentState.plus(xState.getData().times(new Data(num[numIndex])));
                numIndex--;
            }
        }

        out.setData(currentState);
    }

    @Override
    public void calculateDerivative(double t) {
        // First n-1 states: x'i = x(i+1)
        for(int i = 0; i < xStateList.size() - 1; i++){
            xStateList.get(i).setDerivateData(xStateList.get(i+1).getData());
        }

        // Last state: x'n = input - den[last]*x1 - den[last-1]*x2 - ... (reverse order for controller canonical form)
        Data derivativeData = inputPortList.get(0).getData();
        int denIndex = den.length - 1;
        for(State xState : xStateList) {
            if (denIndex >= 0) {
                derivativeData = derivativeData.minus(xState.getData().times(new Data(den[denIndex])));
                denIndex--;
            }
        }
        xStateList.get(xStateList.size() - 1).setDerivateData(derivativeData);
    }

}
