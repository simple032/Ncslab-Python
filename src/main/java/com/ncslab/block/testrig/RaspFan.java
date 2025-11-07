package com.ncslab.block.testrig;

import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.testrig.RaspFanDto;

import com.ncslab.block.Block;
import com.ncslab.block.continuous.TransferFcn;
import com.ncslab.block.data.Data;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.State;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.ModelMode;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.util.TemplateManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RaspFan extends Block {
    String hardwareDefineName;

    // Transfer function coefficients for fan dynamics: H(s) = 1.659 / (s^2 + 1.849s + 1.566)
    private final double [] num = new double[] {1.659};
    private final double [] den = new double[] {1, 1.849, 1.566};

    // Internal transfer function for simulation mode (both normal and RT)
    private TransferFcn internalTransferFcn;

    /**
     * DTO-NATIVE Constructor - Creates RaspFan block directly from RaspFanDto DTO
     */
    public RaspFan(BlockDto blockDto, NCSLabModel model) {
        super(blockDto, model);
        this.isHardware = true;
        inputPortList.add(new InputPort(this, 1));
        outputPortList.add(new OutputPort(this, "FanSpeed", 1, false));

        // In simulation mode: create internal transfer function for delegation
        // In compilation mode: no states needed (hardware-specific code only)
        if (isSimulationMode()) {
            initializeInternalTransferFcn();
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

        // In simulation mode: create internal transfer function for delegation
        if (isSimulationMode()) {
            initializeInternalTransferFcn();
        }
    }

    /**
     * Check if we're in simulation mode (both normal simulation and RT simulation)
     */
    private boolean isSimulationMode() {
        return model.getModelMode() == ModelMode.Simulation;
    }

    /**
     * Initialize internal transfer function with fan dynamics
     * H(s) = 1.659 / (s^2 + 1.849s + 1.566)
     */
    private void initializeInternalTransferFcn() {
        String numStr = "[" + num[0] + "]";
        String denStr = "[" + den[0];
        for (int i = 1; i < den.length; i++) {
            denStr += " " + den[i];
        }
        denStr += "]";

        internalTransferFcn = TransferFcn.create(
            "InternalTF_" + this.getBlockName(),
            this.getBlockPath() + "/InternalTF",
            numStr,  // "[1.659]"
            denStr,  // "[1 1.849 1.566]"
            "auto",  // absoluteTolerance
            "''",    // continuousStateAttributes
            "off",   // realizeZeroPoleGain
            -1,      // sampleTime (continuous)
            "auto",  // outDataType
            false,   // saturateOnOverflow
            model
        );
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

        if (isSimulationMode()) {
            // Simulation mode: delegate to internal transfer function
            internalTransferFcn.generateInitCodeC(code);
        } else {
            // Compilation mode: use hardware-specific template
            com.ncslab.util.TemplateUtils.populateAllContext(context, this);
            hardwareDefineName = "Block" + this.getBlockId() + "_RaspFan";
            context.put("hardwareDefineName", hardwareDefineName);

            String codeStr = TemplateManager.renderTemplate("c/testrig/RaspFan/init.vm", context);
            code.addInitCode(codeStr);
        }
    }

    @Override
    public void generateOutputCodeC(CodeStructC code) {
        if (isSimulationMode()) {
            // Simulation mode: delegate to internal transfer function
            internalTransferFcn.generateOutputCodeC(code);
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
            // Simulation mode: delegate to internal transfer function
            internalTransferFcn.generateDerivativeCodeC(code);
        }
        // Compilation mode: no derivative code needed (hardware-only, no continuous states)
    }

    @Override
    public void calculateInit() {
        if (isSimulationMode() && internalTransferFcn != null) {
            // Simulation mode: delegate to internal transfer function
            internalTransferFcn.calculateInit();

            // Copy output from internal transfer function to RaspFan's output
            OutputPort tfOut = internalTransferFcn.getOutputPortList().get(0);
            outputPortList.get(0).setData(tfOut.getOutputSignalC().getData());
        }
        // Compilation mode: no calculation needed (hardware-only)
    }

    @Override
    public void calculateOutput(double t) {
        if (isSimulationMode() && internalTransferFcn != null) {
            // Copy input from RaspFan to internal transfer function
            Data inputData = inputPortList.get(0).getData();
            internalTransferFcn.getInputPortList().get(0).getLinkedLine().getLinkedOutputPort().setData(inputData);

            // Simulation mode: delegate to internal transfer function
            internalTransferFcn.calculateOutput(t);

            // Copy output from internal transfer function to RaspFan's output
            OutputPort tfOut = internalTransferFcn.getOutputPortList().get(0);
            outputPortList.get(0).setData(tfOut.getOutputSignalC().getData());
        }
        // Compilation mode: no calculation needed (hardware-only)
    }

    @Override
    public void calculateDerivative(double t) {
        if (isSimulationMode() && internalTransferFcn != null) {
            // Simulation mode: delegate to internal transfer function
            internalTransferFcn.calculateDerivative(t);
        }
        // Compilation mode: no derivative calculation needed (hardware-only)
    }

}
