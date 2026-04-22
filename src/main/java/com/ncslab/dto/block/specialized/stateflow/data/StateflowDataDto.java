package com.ncslab.dto.block.specialized.stateflow.data;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Stateflow Chart 完整数据 DTO。
 * 对应前端 StatechartData / innerChart 结构。
 * 替代原有的 Map<String, Object> 弱类型存储。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StateflowDataDto {

    /** 图表 ID */
    private String id;

    /** 图表名称 */
    private String name;

    /**
     * X6 原始 cell 数组，包含 nodes 和 edges。
     * 保持为 List<Map> 以保持与前端 JSON 的完全兼容性。
     */
    @Builder.Default
    private List<Map<String, Object>> cells = new ArrayList<>();

    /** 图表属性 */
    private ChartPropertiesDto properties;

    /** 数据变量定义（多态列表，Jackson 根据 scope 自动反序列化） */
    @Builder.Default
    private List<VariableDto> variables = new ArrayList<>();

    /** 事件定义 */
    @Builder.Default
    private List<EventDto> events = new ArrayList<>();

    /** 创建时间 */
    private String createdAt;

    /** 修改时间 */
    private String updatedAt;

    /**
     * 从 cells 中解析所有状态节点。
     */
    public List<StateDto> getStates() {
        if (cells == null) return new ArrayList<>();
        return cells.stream()
            .map(StateDto::fromCell)
            .filter(s -> s != null)
            .collect(Collectors.toList());
    }

    /**
     * 从 cells 中解析所有转移边。
     */
    public List<TransitionDto> getTransitions() {
        if (cells == null) return new ArrayList<>();
        return cells.stream()
            .map(TransitionDto::fromCell)
            .filter(t -> t != null)
            .collect(Collectors.toList());
    }

    public List<InputVariableDto> getInputVariables() {
        if (variables == null) return new ArrayList<>();
        return variables.stream()
            .filter(v -> v instanceof InputVariableDto)
            .map(v -> (InputVariableDto) v)
            .collect(Collectors.toList());
    }

    public List<OutputVariableDto> getOutputVariables() {
        if (variables == null) return new ArrayList<>();
        return variables.stream()
            .filter(v -> v instanceof OutputVariableDto)
            .map(v -> (OutputVariableDto) v)
            .collect(Collectors.toList());
    }

    public List<LocalVariableDto> getLocalVariables() {
        if (variables == null) return new ArrayList<>();
        return variables.stream()
            .filter(v -> v instanceof LocalVariableDto)
            .map(v -> (LocalVariableDto) v)
            .collect(Collectors.toList());
    }

    public List<ParameterVariableDto> getParameterVariables() {
        if (variables == null) return new ArrayList<>();
        return variables.stream()
            .filter(v -> v instanceof ParameterVariableDto)
            .map(v -> (ParameterVariableDto) v)
            .collect(Collectors.toList());
    }
}
