package com.ncslab.block.sink;

import com.ncslab.block.data.Data;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.block.io.terminal.ScopeStruct;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.ModelMode;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.util.TemplateManager;
import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.sink.DisplayDto;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

public class Display extends Scope {

    // Parameter defaults matching database format
    
    
    /**
     * DTO-NATIVE Constructor - Creates Display block directly from DisplayDto DTO
     */
    public Display(com.ncslab.dto.block.specialized.sink.DisplayDto displayDto, NCSLabModel model) {
        super(createDisplayJSON(displayDto), model);
        scopeStructs[0].setMaxDataLength(1);
        System.out.println("DTO-NATIVE: Display block created successfully from DisplayDto - " + displayDto.getBlockName());
    }


    // === DTO Helper Methods ===
    private static JSONObject createDisplayJSON(com.ncslab.dto.block.specialized.sink.DisplayDto displayDto) {
        JSONObject json = new JSONObject();
        json.put("blockType", "Display");
        json.put("blockName", displayDto.getBlockName());
        json.put("blockPath", displayDto.getBlockPath());
        json.put("blockUUID", displayDto.getBlockUUID() != null ? displayDto.getBlockUUID() : "null");
        
        JSONObject paramValues = new JSONObject();
        paramValues.put("SampleTime", displayDto.getSampleTimeValue());        
        json.put("paramValues", paramValues);
        
        return json;
    }


    public static final Map<String, String> PARAMETER_DEFAULTS;
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("SampleTime", "-1");  // Inherited
    }

    public static final List<String> inputNames = new ArrayList<>();

    static {
        inputNames.add("in1");
    }

    public Display(JSONObject scopeIn, NCSLabModel model) {
        super(scopeIn, model);

        scopeStructs[0].setMaxDataLength(1);
    }

    @Override
    public void checkDimension() throws MatDimException {

        OutputSignal signal = this.inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
        for(int i = 0; i < inportNum; i++) {
            scopeStructs[i] = new ScopeStruct(this, 1, this.blockName);
            scopeStructs[i].setDimension(signal.getWidth(), signal.getHeight());
            scopeStructs[i].setMaxDataLength(1);

            model.addTerminal(scopeStructs[i]);
        }
    }

    @Override
    public void calculateOutput(double t) {
        // Display blocks show the current input value but don't produce output
        // Similar to Scope but only stores the latest value (maxDataLength = 1)

        // Store input data in scope structures for display
        if (model.getModelMode() == ModelMode.Simulation) {
            for(int i = 0; i < inportNum && i < inputPortList.size(); i++) {
                if (scopeStructs != null && scopeStructs[i] != null) {
                    Data inputData = inputPortList.get(i).getData();
                    if (inputData != null) {
                        // Display blocks only keep the most recent value
                        // Clear previous data and add current value
                        scopeStructs[i].getTimeList().clear();
                        scopeStructs[i].getDataList().clear();
                        scopeStructs[i].addTimeSeries(t, inputData);
                    }
                }
            }
        }
    }

    @Override
    public void calculateInit() {
        // Initialize scope data structures with initial input value
        if (model.getModelMode() == ModelMode.Simulation) {
            for(int i = 0; i < inportNum; i++) {
                if (scopeStructs != null && scopeStructs[i] != null) {
                    // Add initial data point at t=0 if there's input data
                    if (i < inputPortList.size() && inputPortList.get(i).getData() != null) {
                        scopeStructs[i].addTimeSeries(0.0, inputPortList.get(i).getData());
                    }
                }
            }
        }
    }

    @Override
    public void generateOutputCodeC(com.ncslab.code.c.CodeStructC code) {
        if (model.getModelMode() == ModelMode.Simulation) {
            com.ncslab.util.TemplateUtils.populateAllContext(context, this);

            context.put("blockId", getBlockId());
            context.put("blockName", getBlockName());
            context.put("inputPortList", getInputPortList());
            context.put("inputPortListSize", inputPortList.size());
            context.put("scopeStruct", scopeStructs[0]);

            // Add scopeStructName for template
            String scopeStructName = scopeStructs[0].getName();
            context.put("scopeStructName", scopeStructName);

            // Add input signal dimensions and details
            if (!inputPortList.isEmpty() && inputPortList.get(0).getLinkedLine() != null) {
                OutputSignal inputSignal = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
                context.put("inputSignal1Height", inputSignal.getHeight());
                context.put("inputSignal1Width", inputSignal.getWidth());
                context.put("inputSignal1Name", "Block" + getBlockId() + "_" + inputSignal.getName());
                context.put("inputSignal", inputSignal.getName());
                context.put("inputSignal1DataType", inputSignal.getDataType());
                context.put("realDataType", com.ncslab.block.data.DataType.REAL);
            }

            String outputCode = TemplateManager.renderTemplate("c/sink/Display/output.vm", context);
            code.addSinkOutputCode(outputCode);

            // Add status clear code (reuse Scope's template)
            context.put("linkedBlockId", this.getInputPortList().get(0).getLinkedLine().getLinkedOutputPort().getBlock().getBlockId());
            String sinkStatusClearCode = TemplateManager.renderTemplate("c/sink/Scope/status_clear.vm", context);
            code.addSinkStatusClearCode(sinkStatusClearCode);
        }
    }

    @Override
    public void generateInitCodeC(com.ncslab.code.c.CodeStructC code) {
        super.generateInitCodeC(code);
        // Display uses the same initialization as Scope (inherited from parent)
    }
}
