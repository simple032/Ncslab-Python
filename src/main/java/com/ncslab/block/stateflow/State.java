package com.ncslab.block.stateflow;

import com.ncslab.dto.block.specialized.stateflow.data.StateDto;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;
import java.util.Map;

/**
 * Stateflow 状态实体。
 * 对应 DTO {@link StateDto}，用于 Block 业务层。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class State {

    /** X6 cell ID */
    private String id;

    /** 节点形状: sf-state, sf-initial, sf-final */
    private String shape;

    /** 状态名称 */
    private String name;

    /** 进入动作 */
    private String entryAction;

    /** 退出动作 */
    private String exitAction;

    /** 状态内持续执行动作（during） */
    private String duringAction;

    /** 是否为并行状态（AND 状态） */
    private Boolean isParallel;

    /** 是否为默认状态 */
    private Boolean isDefault;

    /** 子状态列表（用于超状态） */
    private List<String> substates;

    /** 状态注释 */
    private String description;

    /** X6 位置信息 */
    private Map<String, Object> position;

    /** X6 尺寸信息 */
    private Map<String, Object> size;

    /** 当前是否处于激活状态 */
    @Builder.Default
    private boolean active = false;

    /** 进入该状态的时间戳（仿真步数） */
    @Builder.Default
    private long entryTime = 0;

    /**
     * 执行进入动作。
     */
    public void onEntry(StateMachineRuntime runtime) {
        this.active = true;
        this.entryTime = runtime.getCurrentStep();
    }

    /**
     * 执行持续动作。
     */
    public void onDuring(StateMachineRuntime runtime) {
        if (!active) return;
    }

    /**
     * 执行退出动作。
     */
    public void onExit(StateMachineRuntime runtime) {
        if (!active) return;
        this.active = false;
    }

    /**
     * 从 DTO 转换为实体。
     */
    public static State fromDto(StateDto dto) {
        if (dto == null) return null;
        return State.builder()
            .id(dto.getId())
            .shape(dto.getShape())
            .name(dto.getName())
            .entryAction(dto.getEntryAction())
            .exitAction(dto.getExitAction())
            .duringAction(dto.getDuringAction())
            .isParallel(dto.getIsParallel())
            .isDefault(dto.getIsDefault())
            .substates(dto.getSubstates())
            .description(dto.getDescription())
            .position(dto.getPosition())
            .size(dto.getSize())
            .active(false)
            .entryTime(0)
            .build();
    }

    public boolean isInitial() {
        return "sf-initial".equals(shape);
    }

    public boolean isFinal() {
        return "sf-final".equals(shape);
    }
}
