package com.ncslab.block.stateflow;

import com.ncslab.dto.block.specialized.stateflow.data.StateflowDataDto;
import com.ncslab.dto.block.specialized.stateflow.data.StateDto;
import com.ncslab.dto.block.specialized.stateflow.data.TransitionDto;
import com.ncslab.dto.block.specialized.stateflow.data.VariableDto;
import com.ncslab.dto.block.specialized.stateflow.data.EventDto;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Stateflow Chart 数据实体。
 * 对应 DTO {@link StateflowDataDto}，用于 Block 业务层。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StateflowData {

    /** 图表 ID */
    private String id;

    /** 图表名称 */
    private String name;

    /** X6 原始 cell 数组 */
    @Builder.Default
    private List<Map<String, Object>> cells = new ArrayList<>();

    /** 图表属性 */
    private ChartProperties properties;

    /** 数据变量实体列表 */
    @Builder.Default
    private List<Variable> variables = new ArrayList<>();

    /** 事件实体列表 */
    @Builder.Default
    private List<Event> events = new ArrayList<>();

    /** 创建时间 */
    private String createdAt;

    /** 修改时间 */
    private String updatedAt;

    public List<State> getStates() {
        if (cells == null) return new ArrayList<>();
        return cells.stream()
            .map(StateDto::fromCell)
            .filter(s -> s != null)
            .map(State::fromDto)
            .collect(Collectors.toList());
    }

    public List<Transition> getTransitions() {
        if (cells == null) return new ArrayList<>();
        return cells.stream()
            .map(TransitionDto::fromCell)
            .filter(t -> t != null)
            .map(Transition::fromDto)
            .collect(Collectors.toList());
    }

    public List<InputVariable> getInputVariables() {
        if (variables == null) return new ArrayList<>();
        return variables.stream()
            .filter(v -> v instanceof InputVariable)
            .map(v -> (InputVariable) v)
            .collect(Collectors.toList());
    }

    public List<OutputVariable> getOutputVariables() {
        if (variables == null) return new ArrayList<>();
        return variables.stream()
            .filter(v -> v instanceof OutputVariable)
            .map(v -> (OutputVariable) v)
            .collect(Collectors.toList());
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

    /**
     * 从 DTO 转换为实体。
     */
    public static StateflowData fromDto(StateflowDataDto dto) {
        if (dto == null) return null;

        List<Variable> varEntities = new ArrayList<>();
        if (dto.getVariables() != null) {
            for (VariableDto var : dto.getVariables()) {
                Variable entity = Variable.fromDto(var);
                if (entity != null) varEntities.add(entity);
            }
        }

        List<Event> evtEntities = new ArrayList<>();
        if (dto.getEvents() != null) {
            for (EventDto evt : dto.getEvents()) {
                evtEntities.add(Event.fromDto(evt));
            }
        }

        return StateflowData.builder()
            .id(dto.getId())
            .name(dto.getName())
            .cells(dto.getCells() != null ? new ArrayList<>(dto.getCells()) : new ArrayList<>())
            .properties(ChartProperties.fromDto(dto.getProperties()))
            .variables(varEntities)
            .events(evtEntities)
            .createdAt(dto.getCreatedAt())
            .updatedAt(dto.getUpdatedAt())
            .build();
    }
}
