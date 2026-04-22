package com.ncslab.block.stateflow;

import com.ncslab.block.Block;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.stateflow.StateflowChartDto;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

/**
 * Stateflow Chart block - A state machine container block.
 * 
 * <p>This block represents a Stateflow Chart similar to Simulink's Stateflow.
 * It contains states, transitions, variables, and events that define
 * state machine behavior.</p>
 * 
 * <p>Current implementation is a placeholder that parses the DTO structure
 * and generates comment markers in C code. Full code generation for
 * state machines will be implemented in a future phase.</p>
 * 
 * @author NCSLab Development Team
 * @version 1.0
 * @since 2025-04
 */
public class StateflowChart extends Block {

    /**
     * Parsed stateflow data from DTO.
     */
    private Map<String, Object> stateflowData;

    /**
     * Chart name.
     */
    private String chartName;

    /**
     * Chart variables (input/output/local/parameter).
     */
    private List<Map<String, Object>> variables;

    /**
     * Chart events.
     */
    private List<Map<String, Object>> events;

    /**
     * Chart states.
     */
    private List<Map<String, Object>> states;

    /**
     * Chart transitions.
     */
    private List<Map<String, Object>> transitions;

    /**
     * Chart properties.
     */
    private Map<String, Object> properties;

    @Deprecated
    public StateflowChart(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);
        parseLegacyJSON(blockJSON);
    }

    /**
     * DTO-native constructor.
     * 
     * @param blockDto StateflowChartDto containing block configuration
     * @param model NCSLabModel containing the block diagram
     */
    public StateflowChart(BlockDto blockDto, NCSLabModel model) {
        super(blockDto, model);
        if (blockDto instanceof StateflowChartDto) {
            StateflowChartDto dto = (StateflowChartDto) blockDto;
            this.stateflowData = dto.getStateflowData();
            this.chartName = dto.getChartName();
            this.variables = dto.getVariables();
            this.events = dto.getEvents();
            this.states = dto.getStates();
            this.transitions = dto.getTransitions();
            this.properties = dto.getChartProperties();
        }
        initializePorts();
    }

    /**
     * Factory method to create StateflowChart from StateflowChartDto.
     *
     * @param dto   StateflowChartDto containing block configuration
     * @param model NCSLabModel containing the block diagram
     * @return Created StateflowChart block
     */
    public static StateflowChart createFromDto(StateflowChartDto dto, NCSLabModel model) {
        if (dto == null) {
            throw new IllegalArgumentException("StateflowChartDto cannot be null");
        }
        if (model == null) {
            throw new IllegalArgumentException("NCSLabModel cannot be null");
        }
        return new StateflowChart(dto, model);
    }

    @SuppressWarnings("unchecked")
    private void parseLegacyJSON(JSONObject blockJSON) {
        // Legacy JSON parsing - extract stateflow data if present
        if (blockJSON.has("innerChart")) {
            Object innerChart = blockJSON.get("innerChart");
            if (innerChart instanceof JSONObject) {
                JSONObject chartJSON = (JSONObject) innerChart;
                this.chartName = chartJSON.optString("name", getBlockName());
                
                if (chartJSON.has("variables")) {
                    this.variables = jsonArrayToList(chartJSON.getJSONArray("variables"));
                }
                if (chartJSON.has("events")) {
                    this.events = jsonArrayToList(chartJSON.getJSONArray("events"));
                }
                if (chartJSON.has("states")) {
                    this.states = jsonArrayToList(chartJSON.getJSONArray("states"));
                }
                if (chartJSON.has("transitions")) {
                    this.transitions = jsonArrayToList(chartJSON.getJSONArray("transitions"));
                }
                if (chartJSON.has("properties")) {
                    this.properties = chartJSON.getJSONObject("properties").toMap();
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> jsonArrayToList(org.json.JSONArray array) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            Object item = array.get(i);
            if (item instanceof JSONObject) {
                result.add(((JSONObject) item).toMap());
            }
        }
        return result;
    }

    // ===== Code Generation (Placeholder) =====

    @Override
    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);
        code.addInitCode(String.format("// Stateflow Chart: %s (init placeholder)\n", getBlockName()));
        
        // TODO: Generate state machine initialization code
        // - Initialize state variables
        // - Initialize chart data (local variables, outputs)
        // - Set default state
    }

    @Override
    public void generateOutputCodeC(CodeStructC code) {
        super.generateOutputCodeC(code);
        code.addOutputCode(String.format("// ======================= Stateflow Chart: %s =========================\n", getBlockName()));
        
        // TODO: Generate state machine step code
        // - Evaluate transitions based on events and conditions
        // - Execute state entry/exit/during actions
        // - Update outputs
        
        code.addOutputCode(String.format("// ======================= End Stateflow Chart: %s =========================\n", getBlockName()));
    }

    /**
     * Initialize input/output ports based on Stateflow variables.
     * Input variables create input ports, output variables create output ports.
     */
    private void initializePorts() {
        if (variables == null || variables.isEmpty()) {
            return;
        }
        int inputPortCount = 0;
        int outputPortCount = 0;
        for (Map<String, Object> var : variables) {
            String scope = var.get("scope") != null ? var.get("scope").toString() : "";
            if ("input".equalsIgnoreCase(scope)) {
                inputPortCount++;
                inputPortList.add(new InputPort(this, inputPortCount));
            } else if ("output".equalsIgnoreCase(scope)) {
                outputPortCount++;
                outputPortList.add(new OutputPort(this, outputPortCount, true));
            }
        }
    }

    @Override
    public void updateDimension() throws MatDimException {
        // Stateflow chart assumes scalar inputs/outputs (1x1)
        // OutputSignal dimensions are handled by Block.setupOutputSignal()
        for (OutputPort port : outputPortList) {
            if (port.getWidth() <= 0) port.setWidth(1);
            if (port.getHeight() <= 0) port.setHeight(1);
        }
    }

    @Override
    public void checkDimension() throws MatDimException {
        // TODO: Validate dimensions against variable size declarations
    }

    // ===== Getters for Stateflow Data =====

    public Map<String, Object> getStateflowData() {
        return stateflowData;
    }

    public String getChartName() {
        return chartName;
    }

    public List<Map<String, Object>> getVariables() {
        return variables;
    }

    public List<Map<String, Object>> getEvents() {
        return events;
    }

    public List<Map<String, Object>> getStates() {
        return states;
    }

    public List<Map<String, Object>> getTransitions() {
        return transitions;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }
}
