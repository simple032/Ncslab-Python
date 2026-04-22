package com.ncslab.block.stateflow;

import com.ncslab.dto.block.specialized.stateflow.data.TransitionDto;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * Stateflow 转移实体。
 * 对应 DTO {@link TransitionDto}，用于 Block 业务层。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transition {

    /** X6 cell ID */
    private String id;

    /** 边形状: sf-transition, sf-default-transition, sf-self-transition */
    private String shape;

    /** 源节点 ID */
    private String sourceId;

    /** 目标节点 ID */
    private String targetId;

    /** 触发事件 */
    private String event;

    /** 条件表达式，如 "x > 0" */
    private String condition;

    /** 条件动作（条件满足时执行） */
    private String conditionAction;

    /** 转移动作（转移时执行） */
    private String transitionAction;

    /** 转移优先级（数字越小优先级越高） */
    private Integer priority;

    /** 是否默认转移 */
    private Boolean isDefault;

    /**
     * 判断在当前运行时状态下是否可以触发此转移。
     */
    public boolean canFire(StateMachineRuntime runtime) {
        State sourceState = runtime.getStateById(sourceId);
        if (sourceState == null || !sourceState.isActive()) {
            return false;
        }
        if (event != null && !event.isEmpty()) {
            if (!runtime.hasEvent(event)) {
                return false;
            }
        }
        if (condition != null && !condition.isEmpty()) {
            return true;
        }
        return true;
    }

    /**
     * 执行转移。
     */
    public void fire(StateMachineRuntime runtime) {
        State sourceState = runtime.getStateById(sourceId);
        State targetState = runtime.getStateById(targetId);
        if (sourceState != null) {
            sourceState.onExit(runtime);
        }
        if (targetState != null) {
            targetState.onEntry(runtime);
        }
    }

    /**
     * 从 DTO 转换为实体。
     */
    public static Transition fromDto(TransitionDto dto) {
        if (dto == null) return null;
        return Transition.builder()
            .id(dto.getId())
            .shape(dto.getShape())
            .sourceId(dto.getSourceId())
            .targetId(dto.getTargetId())
            .event(dto.getEvent())
            .condition(dto.getCondition())
            .conditionAction(dto.getConditionAction())
            .transitionAction(dto.getTransitionAction())
            .priority(dto.getPriority())
            .isDefault(dto.getIsDefault())
            .build();
    }

    public boolean isDefaultTransition() {
        return Boolean.TRUE.equals(isDefault) || "sf-default-transition".equals(shape);
    }

    public boolean isSelfTransition() {
        return "sf-self-transition".equals(shape) || (sourceId != null && sourceId.equals(targetId));
    }
}
