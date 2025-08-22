package com.ncslab.block.continuous;

import org.json.JSONObject;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.block.Block;
import com.ncslab.block.data.Data;
import com.ncslab.block.data.DataType;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.Parameter;
import com.ncslab.block.io.State;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.util.TemplateManager;
import com.ncslab.block.data.DataType;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.ncslablink.MatDimException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import lombok.Getter;

public class OldPIDController extends Block {

    private Parameter cparaP;
    private Parameter cparaI;
    private Parameter cparaD;
    private Parameter cparaN;
    private State stateIntegral;
    private State stateFilter;

    
    
    /**
     * DTO-NATIVE Constructor - Creates OldPIDController block directly from BlockDto DTO
     */
    public OldPIDController(BlockDto blockDto, NCSLabModel model) {
        super(blockDto, model);
        System.out.println("DTO-NATIVE: OldPIDController block created successfully - " + blockDto.getBlockName());
    }



    public static final List<String> outputNames = new ArrayList<>();
    public static final List<String> inputNames = new ArrayList<>();

    static {
        outputNames.add("out1");
        inputNames.add("in1");
    }

    // === Parameter Defaults ===
    public static final Map<String, String> PARAMETER_DEFAULTS = new HashMap<>();
    static {
        PARAMETER_DEFAULTS.put("P", "1");
        PARAMETER_DEFAULTS.put("I", "1");
        PARAMETER_DEFAULTS.put("D", "0");
        PARAMETER_DEFAULTS.put("N", "100");
    }

    @Deprecated
    public OldPIDController(JSONObject blockIn, NCSLabModel model) {
        super(blockIn, model);

        // One input, one output
        inputPortList.add(new InputPort(this, 1));
        outputPortList.add(new OutputPort(this, 1, true));

        cparaP = new Parameter(this, parameterList.size() + 1, "P", paramValues.getString("P"));
        cparaI = new Parameter(this, parameterList.size() + 1, "I", paramValues.getString("I"));
        cparaD = new Parameter(this, parameterList.size() + 1, "D", paramValues.getString("D"));
        cparaN = new Parameter(this, parameterList.size() + 1, "N", paramValues.getString("N"));
    }

    @Override
    public void calculateInit() {
        OutputPort out = outputPortList.get(0);
        InputPort in = inputPortList.get(0);
        Data data = cparaP.getData();
        stateIntegral = new State(this, 1, "stateIntegral", in.getHeight(), in.getWidth());
        stateFilter = new State(this, 2, "stateFilter", in.getHeight(), in.getWidth());

        stateIntegral.setData(data);
        stateFilter.setData(data);

        out.setData(data);
    }

    @Override
    public void calculateOutput(double t) {
        OutputPort out = outputPortList.get(0);
        Data currentState = new Data();

        if (cparaP.getDataType() == DataType.REAL && stateIntegral.getDataType() == DataType.REAL) {
            currentState = currentState.plus(cparaP.getData().times(inputPortList.get(0).getData()));
            currentState = currentState.plus(stateIntegral.getData());
            currentState = currentState.plus(stateFilter.getData().times(cparaN.getData()));
        } else {
            for (int i = 0; i < stateIntegral.getHeight(); i++) {
                for (int j = 0; j < stateIntegral.getWidth(); j++) {
                    currentState = currentState.plus(new Data(cparaP.getData().getMatrix().get(i, j) * inputPortList.get(0).getData().getMatrix().get(i, j)));
                    currentState = currentState.plus(new Data(stateIntegral.getData().getMatrix().get(i, j)));
                    currentState = currentState.plus(new Data((stateFilter.getData().getMatrix().get(i, j) - stateFilter.getData().getMatrix().get(i, j)) * cparaN.getData().getMatrix().get(i, j)));
                }
            }
        }

        out.setData(currentState);
    }

    @Override
    public void calculateDerivative(double t) {
        Data derivativeData = new Data();

        if (cparaP.getDataType() == DataType.REAL && stateIntegral.getDataType() == DataType.REAL) {
            derivativeData = derivativeData.plus(cparaI.getData().times(inputPortList.get(0).getData()));
            derivativeData = derivativeData.plus(cparaD.getData().times(inputPortList.get(0).getData()).minus(stateFilter.getData()).times(cparaN.getData()));
        } else {
            for (int i = 0; i < stateIntegral.getHeight(); i++) {
                for (int j = 0; j < stateIntegral.getWidth(); j++) {
                    derivativeData = derivativeData.plus(new Data(cparaI.getData().getMatrix().get(i, j) * inputPortList.get(0).getData().getMatrix().get(i, j)));
                    derivativeData = derivativeData.plus(new Data((cparaD.getData().getMatrix().get(i, j) * inputPortList.get(0).getData().getMatrix().get(i, j) - stateFilter.getData().getMatrix().get(i, j)) * cparaN.getData().getMatrix().get(i, j)));
                }
            }
        }

        stateIntegral.setDerivateData(derivativeData);
        stateFilter.setDerivateData(derivativeData);
    }

    public void generateArraysCodeC(CodeStructC code) {
        OutputSignal signal = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
        context.put("block", this);
        context.put("signal", signal);
        context.put("inputSignal", signal);
        context.put("signalHeight", signal.getHeight());
        context.put("signalWidth", signal.getWidth());
        context.put("signalDataType", signal.getDataType());
        context.put("cparaP", cparaP);
        context.put("proportionalGain", cparaP);
        context.put("proportionalGainHeight", cparaP.getHeight());
        context.put("proportionalGainWidth", cparaP.getWidth());
        context.put("proportionalGainDataType", cparaP.getDataType());
        context.put("realDataType", DataType.REAL);
        context.put("matrixDataType", DataType.MATRIX);

        String codeStr = TemplateManager.renderTemplate("c/continuous/OldPIDController/arrays.vm", context);
        code.addArraysCode(codeStr);
    }

    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);
        context.put("block", this);
        context.put("stateIntegral", stateIntegral);
        context.put("stateFilter", stateFilter);
        context.put("realDataType", DataType.REAL);
        context.put("matrixDataType", DataType.MATRIX);

        // Add states list for template
        java.util.List<State> states = new java.util.ArrayList<>();
        states.add(stateIntegral);
        states.add(stateFilter);
        context.put("states", states);

        String codeStr = TemplateManager.renderTemplate("c/continuous/OldPIDController/init.vm", context);
        code.addInitCode(codeStr);
    }

    public void generateOutputCodeC(CodeStructC code) {
        context.put("block", this);
        context.put("stateIntegral", stateIntegral);
        context.put("stateFilter", stateFilter);
        context.put("realDataType", DataType.REAL);
        context.put("matrixDataType", DataType.MATRIX);

        // Set up inputs and outputs for template
        java.util.List<String> inputs = new java.util.ArrayList<>();
        java.util.List<String> outputs = new java.util.ArrayList<>();
        inputs.add(getInputPortVariable(0));
        outputs.add(getOutputPortVariable(0));
        context.put("inputs", inputs);
        context.put("outputs", outputs);

        String codeStr = TemplateManager.renderTemplate("c/continuous/OldPIDController/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void generateDerivativeCodeC(CodeStructC code) {
        context.put("block", this);
        context.put("stateIntegral", stateIntegral);
        context.put("stateFilter", stateFilter);
        context.put("realDataType", DataType.REAL);
        context.put("matrixDataType", DataType.MATRIX);

        // Set up inputs for template
        java.util.List<String> inputs = new java.util.ArrayList<>();
        inputs.add(getInputPortVariable(0));
        context.put("inputs", inputs);

        String codeStr = TemplateManager.renderTemplate("c/continuous/OldPIDController/derivative.vm", context);
        code.addDerivativeCode(codeStr);
    }

    public void generateUpdateCodeC(CodeStructC code) {
        OutputSignal signal = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
        context.put("block", this);
        context.put("signal", signal);
        context.put("cparaP", cparaP);
        context.put("stateIntegral", stateIntegral);
        context.put("stateFilter", stateFilter);
        context.put("realDataType", DataType.REAL);
        context.put("matrixDataType", DataType.MATRIX);

        String codeStr = TemplateManager.renderTemplate("c/continuous/OldPIDController/update.vm", context);
        code.addUpdateCode(codeStr);
    }

    public void updateDimension() throws MatDimException {
        OutputPort out = outputPortList.get(0);
        InputPort in = inputPortList.get(0);
        OutputSignal signal = in.getLinkedLine().getLinkedOutputPort().getOutputSignalC();

        if (signal.getDataType() == DataType.REAL) {
            stateIntegral = new State(this, 1, "stateIntegral", cparaP.getHeight(), cparaP.getWidth());
            stateFilter = new State(this, 2, "stateFilter", cparaP.getHeight(), cparaP.getWidth());
        } else {
            stateIntegral = new State(this, 1, "stateIntegral", signal.getHeight(), signal.getWidth());
            stateFilter = new State(this, 2, "stateFilter", signal.getHeight(), signal.getWidth());
        }
        stateList.add(stateIntegral);
        stateList.add(stateFilter);

        if (cparaP.getWidth() != cparaD.getWidth() ||
           cparaP.getWidth() != cparaI.getWidth() ||
           cparaP.getWidth() != cparaN.getWidth() ||
           cparaP.getHeight() != cparaD.getHeight() ||
           cparaP.getHeight() != cparaI.getHeight() ||
           cparaP.getHeight() != cparaN.getHeight()) {
            throw new MatDimException("Block " + this.blockName + " input dimension doesn't match! All input dimensions must be the same!\n \n");
        }

        if (cparaP.getDataType() == DataType.MATRIX && signal.getDataType() == DataType.REAL) {
            out.setHeight(cparaP.getHeight());
            out.setWidth(cparaP.getWidth());
            out.getOutputSignalC().setHeight(cparaP.getHeight());
            out.getOutputSignalC().setWidth(cparaP.getWidth());
            out.getOutputSignalC().setDataType(DataType.MATRIX);
        } else if (cparaP.getDataType() == DataType.REAL && signal.getDataType() == DataType.MATRIX) {
            out.setHeight(signal.getHeight());
            out.setWidth(signal.getWidth());
            out.getOutputSignalC().setHeight(signal.getHeight());
            out.getOutputSignalC().setWidth(signal.getWidth());
            out.getOutputSignalC().setDataType(signal.getDataType());
        } else {
            if (cparaP.getWidth() != signal.getWidth() || cparaP.getHeight() != signal.getHeight()) {
                throw new MatDimException("Block " + this.blockName + " input dimension doesn't match the P dimension!\n \n");
            }
            out.setHeight(cparaP.getHeight());
            out.setWidth(cparaP.getWidth());
            out.getOutputSignalC().setHeight(cparaP.getHeight());
            out.getOutputSignalC().setWidth(cparaP.getWidth());
            out.getOutputSignalC().setDataType(cparaP.getDataType());
        }
    }

    public void checkDimension() throws MatDimException {
    }
}
