package com.ncslab.block.stateflow;

import com.ncslab.block.Block;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.stateflow.StateflowChartDto;
import com.ncslab.dto.block.specialized.stateflow.data.StateflowDataDto;
import com.ncslab.dto.block.specialized.stateflow.data.VariableDto;
import com.ncslab.dto.block.specialized.stateflow.data.EventDto;
import com.ncslab.dto.block.specialized.stateflow.data.ChartPropertiesDto;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

/**
 * Stateflow Chart block - A state machine container block.
 *
 * @author NCSLab Development Team
 * @version 1.2
 * @since 2025-04
 */
public class StateflowChart extends Block {

    /**
     * Stateflow data entity (business layer).
     * Decoupled from DTO for domain logic independence.
     */
    private StateflowData stateflowData;

    /**
     * Chart name.
     */
    private String chartName;

    /**
     * Chart variables (input/output/local/parameter).
     */
    private List<Variable> variables;

    /**
     * Chart events.
     */
    private List<Event> events;

    /**
     * Chart properties.
     */
    private ChartProperties properties;

    /**
     * State machine runtime (initialized during simulation).
     */
    private StateMachineRuntime runtime;

    @Deprecated
    public StateflowChart(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);
        parseLegacyJSON(blockJSON);
    }

    /**
     * DTO-native constructor.
     */
    public StateflowChart(BlockDto blockDto, NCSLabModel model) {
        super(blockDto, model);
        if (blockDto instanceof StateflowChartDto) {
            StateflowChartDto dto = (StateflowChartDto) blockDto;
            this.stateflowData = StateflowData.fromDto(dto.getStateflowData());
            this.chartName = dto.getChartName();
            if (this.stateflowData != null) {
                this.variables = this.stateflowData.getVariables();
                this.events = this.stateflowData.getEvents();
                this.properties = this.stateflowData.getProperties();
            }
        }
        initializePorts();
    }

    /**
     * Factory method to create StateflowChart from StateflowChartDto.
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
        if (blockJSON.has("innerChart")) {
            Object innerChart = blockJSON.get("innerChart");
            if (innerChart instanceof JSONObject) {
                JSONObject chartJSON = (JSONObject) innerChart;
                this.chartName = chartJSON.optString("name", getBlockName());

                StateflowDataDto.StateflowDataDtoBuilder dataBuilder = StateflowDataDto.builder()
                    .id(chartJSON.optString("id", null))
                    .name(this.chartName);

                if (chartJSON.has("cells")) {
                    dataBuilder.cells(jsonArrayToList(chartJSON.getJSONArray("cells")));
                }
                if (chartJSON.has("variables")) {
                    List<Map<String, Object>> varList = jsonArrayToList(chartJSON.getJSONArray("variables"));
                    List<VariableDto> typedVars = new ArrayList<>();
                    for (Map<String, Object> varMap : varList) {
                        VariableDto var = variableFromMap(varMap);
                        if (var != null) typedVars.add(var);
                    }
                    dataBuilder.variables(typedVars);
                }
                if (chartJSON.has("events")) {
                    List<Map<String, Object>> evtList = jsonArrayToList(chartJSON.getJSONArray("events"));
                    List<EventDto> typedEvents = new ArrayList<>();
                    for (Map<String, Object> evtMap : evtList) {
                        EventDto evt = eventFromMap(evtMap);
                        if (evt != null) typedEvents.add(evt);
                    }
                    dataBuilder.events(typedEvents);
                }
                if (chartJSON.has("properties")) {
                    JSONObject props = chartJSON.getJSONObject("properties");
                    ChartPropertiesDto chartProps = ChartPropertiesDto.builder()
                        .stateMachineType(props.optString("stateMachineType", "Classic"))
                        .updateMethod(props.optString("updateMethod", "inherited"))
                        .sampleTime(props.optString("sampleTime", null))
                        .enableZeroCrossings(props.has("enableZeroCrossings") ? props.getBoolean("enableZeroCrossings") : null)
                        .enableCBitOperations(props.has("enableCBitOperations") ? props.getBoolean("enableCBitOperations") : null)
                        .executeAtInitialization(props.has("executeAtInitialization") ? props.getBoolean("executeAtInitialization") : null)
                        .initializeOutputsEveryTime(props.has("initializeOutputsEveryTime") ? props.getBoolean("initializeOutputsEveryTime") : null)
                        .build();
                    dataBuilder.properties(chartProps);
                }

                this.stateflowData = StateflowData.fromDto(dataBuilder.build());
                if (this.stateflowData != null) {
                    this.variables = this.stateflowData.getVariables();
                    this.events = this.stateflowData.getEvents();
                    this.properties = this.stateflowData.getProperties();
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

    private static VariableDto variableFromMap(Map<String, Object> map) {
        if (map == null) return null;
        String scope = map.get("scope") != null ? map.get("scope").toString() : "";
        String name = map.get("name") != null ? map.get("name").toString() : null;
        String dataType = map.get("dataType") != null ? map.get("dataType").toString() : null;

        VariableDto var;
        switch (scope.toLowerCase()) {
            case "input":
                var = new com.ncslab.dto.block.specialized.stateflow.data.InputVariableDto();
                break;
            case "output":
                var = new com.ncslab.dto.block.specialized.stateflow.data.OutputVariableDto();
                break;
            case "local":
                var = new com.ncslab.dto.block.specialized.stateflow.data.LocalVariableDto();
                break;
            case "parameter":
                var = new com.ncslab.dto.block.specialized.stateflow.data.ParameterVariableDto();
                break;
            default:
                return null;
        }
        var.setName(name);
        var.setDataType(dataType);
        var.setScope(scope);
        var.setInitialValue(map.get("initialValue") != null ? map.get("initialValue").toString() : null);
        var.setSize(map.get("size") != null ? map.get("size").toString() : null);
        var.setDescription(map.get("description") != null ? map.get("description").toString() : null);
        if (map.get("port") instanceof Number) {
            var.setPort(((Number) map.get("port")).intValue());
        }
        return var;
    }

    private static EventDto eventFromMap(Map<String, Object> map) {
        if (map == null) return null;
        return EventDto.builder()
            .name(map.get("name") != null ? map.get("name").toString() : null)
            .eventType(map.get("eventType") != null ? map.get("eventType").toString() : null)
            .triggerType(map.get("triggerType") != null ? map.get("triggerType").toString() : null)
            .description(map.get("description") != null ? map.get("description").toString() : null)
            .build();
    }

    // ===== Code Generation (Placeholder) =====

    @Override
    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);
        code.addInitCode(String.format("// Stateflow Chart: %s (init placeholder)\n", getBlockName()));
    }

    @Override
    public void generateOutputCodeC(CodeStructC code) {
        super.generateOutputCodeC(code);
        code.addOutputCode(String.format("// ======================= Stateflow Chart: %s =========================\n", getBlockName()));
        code.addOutputCode(String.format("// ======================= End Stateflow Chart: %s =========================\n", getBlockName()));
    }

    /**
     * Initialize input/output ports based on Stateflow variables.
     */
    private void initializePorts() {
        if (variables == null || variables.isEmpty()) {
            return;
        }
        int inputPortCount = 0;
        int outputPortCount = 0;
        for (Variable var : variables) {
            if (var instanceof InputVariable) {
                inputPortCount++;
                inputPortList.add(new InputPort(this, inputPortCount));
            } else if (var instanceof OutputVariable) {
                outputPortCount++;
                outputPortList.add(new OutputPort(this, outputPortCount, true));
            }
        }
    }

    @Override
    public void updateDimension() throws MatDimException {
        for (OutputPort port : outputPortList) {
            if (port.getWidth() <= 0) port.setWidth(1);
            if (port.getHeight() <= 0) port.setHeight(1);
        }
    }

    @Override
    public void checkDimension() throws MatDimException {
        // TODO: Validate dimensions against variable size declarations
    }

    // ===== State Machine Runtime =====

    public void initializeRuntime() {
        if (runtime == null) {
            runtime = new StateMachineRuntime();
        }
        runtime.initialize(this);
    }

    public StateMachineRuntime getRuntime() {
        return runtime;
    }

    // ===== Getters =====

    public StateflowData getStateflowData() {
        return stateflowData;
    }

    public String getChartName() {
        return chartName;
    }

    public List<Variable> getVariables() {
        return variables;
    }

    public List<Event> getEvents() {
        return events;
    }

    public ChartProperties getProperties() {
        return properties;
    }

    public List<State> getStates() {
        return stateflowData != null ? stateflowData.getStates() : new ArrayList<>();
    }

    public List<Transition> getTransitions() {
        return stateflowData != null ? stateflowData.getTransitions() : new ArrayList<>();
    }

    public List<InputVariable> getInputVariables() {
        if (variables == null) return new ArrayList<>();
        List<InputVariable> result = new ArrayList<>();
        for (Variable var : variables) {
            if (var instanceof InputVariable) {
                result.add((InputVariable) var);
            }
        }
        return result;
    }

    public List<OutputVariable> getOutputVariables() {
        if (variables == null) return new ArrayList<>();
        List<OutputVariable> result = new ArrayList<>();
        for (Variable var : variables) {
            if (var instanceof OutputVariable) {
                result.add((OutputVariable) var);
            }
        }
        return result;
    }
}
