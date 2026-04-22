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
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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

    /**
     * InputVariable 到外部信号源的连接映射。
     * 懒加载：第一次访问时通过 {@link #resolveInputConnections()} 自动建立。
     */
    private List<InputVariableConnection> inputVariableConnections = new ArrayList<>();

    /**
     * OutputVariable 到下游信号目标的连接映射。
     * 懒加载：第一次访问时通过 {@link #resolveOutputConnections()} 自动建立。
     */
    private List<OutputVariableConnection> outputVariableConnections = new ArrayList<>();

    /**
     * 输入连接解析标志，防止重复解析。
     */
    private boolean inputConnectionsResolved = false;

    /**
     * 输出连接解析标志，防止重复解析。
     */
    private boolean outputConnectionsResolved = false;

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

    // ===== C Code Generation =====

    /**
     * 生成 Stateflow Chart 的 C 语言初始化代码。
     * <p>
     * 包括：全局变量/枚举声明、local variables 初始化、状态变量初始化、默认状态 entry 动作。
     */
    @Override
    public void generateInitCodeC(CodeStructC code) {
        // 先生成全局声明（枚举、变量声明），确保在头文件中
        generateGlobalDeclarations(code);

        super.generateInitCodeC(code);

        StringBuilder initCode = new StringBuilder();
        initCode.append(String.format("// Stateflow Chart: %s (id=%d) init\n", getBlockName(), getBlockId()));

        // 1. 初始化 local variables
        List<LocalVariable> localVars = getLocalVariables();
        if (!localVars.isEmpty()) {
            initCode.append("// Initialize local variables\n");
            for (LocalVariable var : localVars) {
                String initValue = var.getInitialValue() != null ? var.getInitialValue() : "0.0";
                initCode.append(String.format("    %s = %s;\n", getVariableCName(var), initValue));
            }
        }

        // 2. 初始化当前状态为默认状态（Initial 状态连接的 target）
        String initialStateEnum = findInitialStateEnum();
        if (initialStateEnum != null) {
            initCode.append("// Initialize state machine to default state\n");
            initCode.append(String.format("    %s = %s;\n", getStateVarCName(), initialStateEnum));
            // 执行初始状态的 entry action
            String entryAction = findInitialStateEntryAction();
            if (entryAction != null && !entryAction.trim().isEmpty()) {
                initCode.append("// Default state entry action\n");
                initCode.append(String.format("    { %s }\n", ensureStatementTerminator(replaceVariableNames(entryAction))));
            }
        } else {
            initCode.append("// Warning: No initial state found\n");
        }

        code.addInitCode(initCode.toString());
    }

    /**
     * 生成 Stateflow Chart 的 C 语言输出代码（每步执行）。
     * <p>
     * 包括：input mapping → during actions → transition checks → output mapping。
     */
    @Override
    public void generateOutputCodeC(CodeStructC code) {
        super.generateOutputCodeC(code);

        StringBuilder outputCode = new StringBuilder();
        outputCode.append(String.format("// ======================= Stateflow Chart: %s (id=%d) =========================\n",
                                         getBlockName(), getBlockId()));

        List<State> states = getStates();
        List<Transition> transitions = getTransitions();
        if (states == null || states.isEmpty()) {
            outputCode.append("// No states defined\n");
            code.addOutputCode(outputCode.toString());
            return;
        }

        // --- 1. Input mapping: read input ports into InputVariables ---
        List<InputVariable> inputVars = getInputVariables();
        if (!inputVars.isEmpty()) {
            outputCode.append("// Input mapping\n");
            for (int i = 0; i < inputVars.size() && i < getInputPortList().size(); i++) {
                String inputVarName = getVariableCName(inputVars.get(i));
                String inputSignalName = safeGetInputPortVariable(i, "0.0");
                outputCode.append(String.format("    %s = %s;\n", inputVarName, inputSignalName));
            }
        }

        // --- 2. State machine execution ---
        outputCode.append("// State machine execution\n");
        outputCode.append(String.format("    switch (%s) {\n", getStateVarCName()));

        for (int sIdx = 0; sIdx < states.size(); sIdx++) {
            State state = states.get(sIdx);
            String stateEnum = getStateEnumName(sIdx);
            outputCode.append(String.format("    case %s: // %s\n", stateEnum, escapeCComment(state.getName())));

            // During action
            String duringAction = state.getDuringAction();
            if (duringAction != null && !duringAction.trim().isEmpty()) {
                outputCode.append(String.format("        { %s }\n", ensureStatementTerminator(replaceVariableNames(duringAction))));
            }

            // Check outgoing transitions
            List<Transition> outgoing = getOutgoingTransitions(state, transitions);
            for (int tIdx = 0; tIdx < outgoing.size(); tIdx++) {
                Transition trans = outgoing.get(tIdx);
                String condition = replaceVariableNames(trans.getCondition());
                String transAction = replaceVariableNames(trans.getTransitionAction());
                String targetStateEnum = getTargetStateEnumName(trans.getTargetId(), states);

                if (condition != null && !condition.trim().isEmpty()) {
                    outputCode.append(String.format("        if (%s) {\n", condition));
                } else {
                    outputCode.append(String.format("        { // unconditional transition\n"));
                }

                // Exit action
                String exitAction = state.getExitAction();
                if (exitAction != null && !exitAction.trim().isEmpty()) {
                    outputCode.append(String.format("            { %s }\n", ensureStatementTerminator(replaceVariableNames(exitAction))));
                }

                // Transition action
                if (transAction != null && !transAction.trim().isEmpty()) {
                    outputCode.append(String.format("            { %s }\n", ensureStatementTerminator(transAction)));
                }

                // State change
                if (targetStateEnum != null) {
                    outputCode.append(String.format("            %s = %s;\n", getStateVarCName(), targetStateEnum));
                    // Entry action of target state
                    State targetState = findStateById(trans.getTargetId(), states);
                    if (targetState != null) {
                        String targetEntryAction = targetState.getEntryAction();
                        if (targetEntryAction != null && !targetEntryAction.trim().isEmpty()) {
                            outputCode.append(String.format("            { %s }\n", ensureStatementTerminator(replaceVariableNames(targetEntryAction))));
                        }
                    }
                }

                outputCode.append(String.format("        }\n"));
            }

            outputCode.append(String.format("        break;\n"));
        }

        outputCode.append(String.format("    default:\n"));
        outputCode.append(String.format("        break;\n"));
        outputCode.append(String.format("    }\n"));

        // --- 3. Output mapping: write OutputVariables to output ports ---
        List<OutputVariable> outputVars = getOutputVariables();
        if (!outputVars.isEmpty()) {
            outputCode.append("// Output mapping\n");
            for (int i = 0; i < outputVars.size() && i < getOutputPortList().size(); i++) {
                String outputVarName = getVariableCName(outputVars.get(i));
                String outputSignalName = safeGetOutputPortVariable(i, "0.0");
                outputCode.append(String.format("    %s = %s;\n", outputSignalName, outputVarName));
            }
        }

        outputCode.append(String.format("// ======================= End Stateflow Chart: %s =========================\n", getBlockName()));
        code.addOutputCode(outputCode.toString());
    }

    // ===== C Code Generation Helpers =====

    private String getStateVarCName() {
        return "Block" + getBlockId() + "_ChartState";
    }

    private String getVariableCName(Variable var) {
        return "Block" + getBlockId() + "_" + sanitizeCName(var.getName());
    }

    private String getStateEnumName(int stateIndex) {
        return "STATE_Block" + getBlockId() + "_" + stateIndex;
    }

    private String getTargetStateEnumName(String stateId, List<State> states) {
        for (int i = 0; i < states.size(); i++) {
            if (states.get(i).getId().equals(stateId)) {
                return getStateEnumName(i);
            }
        }
        return null;
    }

    private State findStateById(String stateId, List<State> states) {
        for (State state : states) {
            if (state.getId().equals(stateId)) {
                return state;
            }
        }
        return null;
    }

    private String findInitialStateEnum() {
        List<State> states = getStates();
        List<Transition> transitions = getTransitions();
        for (Transition trans : transitions) {
            State source = findStateById(trans.getSourceId(), states);
            if (source != null && source.isInitial()) {
                return getTargetStateEnumName(trans.getTargetId(), states);
            }
        }
        // Fallback: if there's an sf-initial state, find its outgoing transition
        for (int i = 0; i < states.size(); i++) {
            if (states.get(i).isInitial()) {
                for (Transition trans : transitions) {
                    if (trans.getSourceId().equals(states.get(i).getId())) {
                        return getTargetStateEnumName(trans.getTargetId(), states);
                    }
                }
            }
        }
        return states.isEmpty() ? null : getStateEnumName(0);
    }

    private String findInitialStateEntryAction() {
        List<State> states = getStates();
        List<Transition> transitions = getTransitions();
        for (Transition trans : transitions) {
            State source = findStateById(trans.getSourceId(), states);
            if (source != null && source.isInitial()) {
                State target = findStateById(trans.getTargetId(), states);
                return target != null ? target.getEntryAction() : null;
            }
        }
        for (int i = 0; i < states.size(); i++) {
            if (states.get(i).isInitial()) {
                for (Transition trans : transitions) {
                    if (trans.getSourceId().equals(states.get(i).getId())) {
                        State target = findStateById(trans.getTargetId(), states);
                        return target != null ? target.getEntryAction() : null;
                    }
                }
            }
        }
        return null;
    }

    private List<Transition> getOutgoingTransitions(State state, List<Transition> allTransitions) {
        List<Transition> result = new ArrayList<>();
        for (Transition trans : allTransitions) {
            if (trans.getSourceId() != null && trans.getSourceId().equals(state.getId())) {
                result.add(trans);
            }
        }
        return result;
    }

    private String sanitizeCName(String name) {
        if (name == null) return "unnamed";
        return name.replaceAll("[^a-zA-Z0-9_]", "_");
    }

    private String escapeCComment(String text) {
        if (text == null) return "";
        return text.replace("*/", "* /").replace("\n", " ");
    }

    // ===== Global Declaration Generation =====

    /**
     * 生成 Stateflow Chart 需要的全局 C 声明代码（枚举类型、状态变量、数据变量）。
     * 这些声明会被输出到 ncslabccode.hpp 头文件中。
     */
    private void generateGlobalDeclarations(CodeStructC code) {
        StringBuilder decl = new StringBuilder();
        int bid = getBlockId();
        List<State> states = getStates();

        // 1. 状态枚举类型定义
        if (states != null && !states.isEmpty()) {
            decl.append(String.format("// Stateflow Chart %d state enum\n", bid));
            decl.append(String.format("typedef enum {\n"));
            for (int i = 0; i < states.size(); i++) {
                String sep = (i < states.size() - 1) ? "," : "";
                decl.append(String.format("    %s%s\n", getStateEnumName(i), sep));
            }
            decl.append(String.format("} ChartState_Block%d;\n", bid));
            // 状态变量声明
            decl.append(String.format("ChartState_Block%d %s;\n", bid, getStateVarCName()));
        }

        // 2. 所有 Stateflow 变量声明（input/output/local/parameter）
        if (variables != null && !variables.isEmpty()) {
            decl.append(String.format("// Stateflow Chart %d variables\n", bid));
            for (Variable var : variables) {
                String varCName = getVariableCName(var);
                String cType = getVariableCType(var);
                decl.append(String.format("%s %s;\n", cType, varCName));
            }
        }

        if (decl.length() > 0) {
            code.addGlobalDeclareCode(decl.toString());
        }
    }

    /**
     * 根据 Stateflow 变量的数据类型返回对应的 C 类型字符串。
     */
    private String getVariableCType(Variable var) {
        if (var == null) return "REAL";
        String dataType = var.getDataType();
        if (dataType == null) return "REAL";
        switch (dataType.toLowerCase()) {
            case "int":
            case "integer":
            case "int32":
                return "int";
            case "int8":
                return "int8_t";
            case "int16":
                return "int16_t";
            case "boolean":
            case "bool":
                return "bool";
            case "single":
            case "float":
                return "float";
            case "double":
            default:
                return "REAL";
        }
    }

    /**
     * 将 Stateflow 表达式中的变量名替换为带 Block{id}_ 前缀的 C 变量名。
     * 使用正则表达式确保只替换完整的标识符（避免替换函数名等）。
     */
    private String replaceVariableNames(String expression) {
        if (expression == null || expression.trim().isEmpty()) {
            return expression;
        }

        String result = expression;
        // 收集所有变量名，按长度降序排列（避免短名替换长名的一部分）
        List<String> varNames = new ArrayList<>();
        if (variables != null) {
            for (Variable var : variables) {
                if (var.getName() != null && !var.getName().trim().isEmpty()) {
                    varNames.add(var.getName().trim());
                }
            }
        }
        varNames.sort((a, b) -> Integer.compare(b.length(), a.length()));

        for (String varName : varNames) {
            Variable var = findVariableByName(varName);
            if (var == null) continue;
            String cName = getVariableCName(var);
            // 正则：匹配完整的标识符，前后不能是字母/数字/下划线
            String regex = "(?<![a-zA-Z0-9_])" + Pattern.quote(varName) + "(?![a-zA-Z0-9_])";
            result = result.replaceAll(regex, cName);
        }

        return result;
    }

    /**
     * 确保 C 语句以分号结尾。如果代码已经以分号或右花括号结尾，则不做修改。
     */
    private String ensureStatementTerminator(String code) {
        if (code == null || code.trim().isEmpty()) {
            return code;
        }
        String trimmed = code.trim();
        // 已以分号或右花括号结尾，或者是复合语句，不添加分号
        if (trimmed.endsWith(";") || trimmed.endsWith("}") || trimmed.endsWith("{")) {
            return trimmed;
        }
        return trimmed + ";";
    }

    /**
     * 根据变量名查找对应的 Variable 对象。
     */
    private Variable findVariableByName(String name) {
        if (variables == null || name == null) return null;
        for (Variable var : variables) {
            if (name.equals(var.getName())) {
                return var;
            }
        }
        return null;
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

    public List<LocalVariable> getLocalVariables() {
        if (variables == null) return new ArrayList<>();
        return variables.stream()
            .filter(v -> v instanceof LocalVariable)
            .map(v -> (LocalVariable) v)
            .collect(Collectors.toList());
    }

    public List<ParameterVariable> getParameterVariables() {
        if (variables == null) return new ArrayList<>();
        return variables.stream()
            .filter(v -> v instanceof ParameterVariable)
            .map(v -> (ParameterVariable) v)
            .collect(Collectors.toList());
    }

    // ===== Input Variable Connection Resolution =====

    /**
     * 解析所有 InputVariable 的外部信号连接。
     * <p>
     * 遍历 Chart 的输入端口，通过连接的 {@link com.ncslab.line.Line} 找到前级模块的
     * {@link OutputPort}，并将该映射记录到 {@link InputVariableConnection} 中。
     * <p>
     * <b>调用时机</b>：必须在 {@link com.ncslab.ncslablink.NCSLabModel} 完成所有 blocks
     * 和 lines 的解析之后调用，否则端口尚未建立连线关联。
     */
    /**
     * 解析所有 InputVariable 的外部信号连接。
     * <p>
     * 遍历 Chart 的输入端口，通过连接的 {@link com.ncslab.line.Line} 找到前级模块的
     * {@link OutputPort}，并将该映射记录到 {@link InputVariableConnection} 中。
     * <p>
     * <b>调用时机</b>：必须在模型所有 blocks 和 lines 解析完成后调用，
     * 否则端口尚未建立连线关联。可以在仿真启动或代码生成前显式调用，
     * 也可以通过 {@link #getInputVariableConnections()} 懒加载自动触发。
     */
    public void resolveInputConnections() {
        if (inputConnectionsResolved) {
            return;
        }
        if (variables == null || variables.isEmpty()) {
            inputConnectionsResolved = true;
            return;
        }

        inputVariableConnections.clear();

        // 收集所有 InputVariable，保持它们在 variables 列表中的顺序
        List<InputVariable> inputVars = variables.stream()
            .filter(v -> v instanceof InputVariable)
            .map(v -> (InputVariable) v)
            .collect(Collectors.toList());

        // inputPortList 中的 InputPort 也是按创建顺序排列的
        List<com.ncslab.block.io.InputPort> inputPorts = getInputPortList();

        for (int i = 0; i < inputVars.size() && i < inputPorts.size(); i++) {
            InputVariable var = inputVars.get(i);
            com.ncslab.block.io.InputPort inputPort = inputPorts.get(i);

            InputVariableConnection.InputVariableConnectionBuilder connBuilder = InputVariableConnection.builder()
                .variable(var)
                .variableIndex(i)
                .inputPort(inputPort)
                .inputPortNumber(inputPort.getNumber());

            // 通过 linkedLine 追踪到源 OutputPort
            com.ncslab.line.Line linkedLine = inputPort.getLinkedLine();
            if (linkedLine != null) {
                com.ncslab.block.io.OutputPort sourceOutputPort = linkedLine.getLinkedOutputPort();
                if (sourceOutputPort != null) {
                    Block sourceBlock = sourceOutputPort.getBlock();
                    connBuilder
                        .sourceOutputPort(sourceOutputPort)
                        .sourcePortNumber(sourceOutputPort.getNumber())
                        .sourceOutputSignal(sourceOutputPort.getOutputSignalC())
                        .sourceBlock(sourceBlock);
                    if (sourceBlock != null) {
                        connBuilder
                            .sourceBlockName(sourceBlock.getBlockName())
                            .sourceBlockPath(sourceBlock.getBlockPath())
                            .sourceBlockUUID(sourceBlock.getBlockUUID());
                    }
                }
            }

            inputVariableConnections.add(connBuilder.build());
        }

        System.out.println("StateflowChart '" + getBlockName() + "' resolved " +
                           inputVariableConnections.size() + " input variable connections");
        for (InputVariableConnection conn : inputVariableConnections) {
            if (conn.isConnected()) {
                System.out.println("  InputVariable '" + conn.getVariable().getName() + "' -> " +
                                   conn.getSourceBlockName() + " [out" + conn.getSourcePortNumber() + "]");
            } else {
                System.out.println("  InputVariable '" + conn.getVariable().getName() + "' -> (unconnected)");
            }
        }

        inputConnectionsResolved = true;
    }

    /**
     * 获取所有 InputVariable 的连接映射。
     * <p>
     * 懒加载：如果尚未解析，会自动调用 {@link #resolveInputConnections()}。
     * 因此应在模型所有 blocks 和 lines 解析完成后调用此方法，以确保连接正确。
     */
    public List<InputVariableConnection> getInputVariableConnections() {
        if (!inputConnectionsResolved) {
            resolveInputConnections();
        }
        return inputVariableConnections;
    }

    /**
     * 根据变量名获取对应的连接映射。
     */
    public InputVariableConnection getInputVariableConnection(String variableName) {
        if (inputVariableConnections == null || variableName == null) return null;
        return inputVariableConnections.stream()
            .filter(c -> variableName.equals(c.getVariable().getName()))
            .findFirst()
            .orElse(null);
    }

    /**
     * 根据输入端口号获取对应的连接映射。
     */
    public InputVariableConnection getInputVariableConnectionByPort(int portNumber) {
        if (inputVariableConnections == null) return null;
        return inputVariableConnections.stream()
            .filter(c -> c.getInputPortNumber() == portNumber)
            .findFirst()
            .orElse(null);
    }

    // ===== Output Variable Connection Resolution =====

    /**
     * 解析所有 OutputVariable 的下游信号连接。
     * <p>
     * 遍历 Chart 的输出端口，通过 {@link OutputPort#getLinkedLineList()} 找到所有
     * 下游模块的 {@link com.ncslab.block.io.InputPort}，并将映射记录到
     * {@link OutputVariableConnection} 中（支持信号分叉，一个输出可连接多个下游）。
     * <p>
     * <b>调用时机</b>：必须在模型所有 blocks 和 lines 解析完成后调用。
     */
    public void resolveOutputConnections() {
        if (outputConnectionsResolved) {
            return;
        }
        if (variables == null || variables.isEmpty()) {
            outputConnectionsResolved = true;
            return;
        }

        outputVariableConnections.clear();

        // 收集所有 OutputVariable，保持它们在 variables 列表中的顺序
        List<OutputVariable> outputVars = variables.stream()
            .filter(v -> v instanceof OutputVariable)
            .map(v -> (OutputVariable) v)
            .collect(Collectors.toList());

        // outputPortList 中的 OutputPort 也是按创建顺序排列的
        List<com.ncslab.block.io.OutputPort> outputPorts = getOutputPortList();

        for (int i = 0; i < outputVars.size() && i < outputPorts.size(); i++) {
            OutputVariable var = outputVars.get(i);
            com.ncslab.block.io.OutputPort outputPort = outputPorts.get(i);

            List<OutputVariableSink> sinks = new ArrayList<>();
            List<com.ncslab.line.Line> linkedLines = outputPort.getLinkedLineList();
            if (linkedLines != null) {
                for (com.ncslab.line.Line line : linkedLines) {
                    OutputVariableSink sink = OutputVariableSink.fromLine(line);
                    if (sink != null) {
                        sinks.add(sink);
                    }
                }
            }

            OutputVariableConnection conn = OutputVariableConnection.builder()
                .variable(var)
                .variableIndex(i)
                .outputPort(outputPort)
                .outputPortNumber(outputPort.getNumber())
                .outputSignal(outputPort.getOutputSignalC())
                .sinks(sinks)
                .build();

            outputVariableConnections.add(conn);
        }

        System.out.println("StateflowChart '" + getBlockName() + "' resolved " +
                           outputVariableConnections.size() + " output variable connections");
        for (OutputVariableConnection conn : outputVariableConnections) {
            if (conn.isConnected()) {
                StringBuilder sinkNames = new StringBuilder();
                for (OutputVariableSink sink : conn.getSinks()) {
                    if (sinkNames.length() > 0) sinkNames.append(", ");
                    sinkNames.append(sink.getTargetBlockName())
                             .append(" [in").append(sink.getTargetPortNumber()).append("]");
                }
                System.out.println("  OutputVariable '" + conn.getVariable().getName() + "' -> " + sinkNames);
            } else {
                System.out.println("  OutputVariable '" + conn.getVariable().getName() + "' -> (unconnected)");
            }
        }

        outputConnectionsResolved = true;
    }

    /**
     * 获取所有 OutputVariable 的连接映射。
     * <p>
     * 懒加载：如果尚未解析，会自动调用 {@link #resolveOutputConnections()}。
     */
    public List<OutputVariableConnection> getOutputVariableConnections() {
        if (!outputConnectionsResolved) {
            resolveOutputConnections();
        }
        return outputVariableConnections;
    }

    /**
     * 根据变量名获取对应的输出连接映射。
     */
    public OutputVariableConnection getOutputVariableConnection(String variableName) {
        if (outputVariableConnections == null || variableName == null) return null;
        return getOutputVariableConnections().stream()
            .filter(c -> variableName.equals(c.getVariable().getName()))
            .findFirst()
            .orElse(null);
    }

    /**
     * 根据输出端口号获取对应的连接映射。
     */
    public OutputVariableConnection getOutputVariableConnectionByPort(int portNumber) {
        if (outputVariableConnections == null) return null;
        return getOutputVariableConnections().stream()
            .filter(c -> c.getOutputPortNumber() == portNumber)
            .findFirst()
            .orElse(null);
    }
}
