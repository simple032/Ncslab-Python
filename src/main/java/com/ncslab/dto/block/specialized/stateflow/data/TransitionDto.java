package com.ncslab.dto.block.specialized.stateflow.data;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.Map;

/**
 * Stateflow 转移边数据 DTO。
 * 从前端 X6 cell 数据中解析得到，对应 TransitionEdgeData。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransitionDto {

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
     * 从 X6 cell Map 中解析 TransitionDto 对象。
     *
     * @param cell X6 cell 的 Map 表示
     * @return 解析后的 TransitionDto，如果 shape 不是转移类型则返回 null
     */
    @SuppressWarnings("unchecked")
    public static TransitionDto fromCell(Map<String, Object> cell) {
        if (cell == null) return null;
        Object shapeObj = cell.get("shape");
        if (shapeObj == null) return null;
        String shape = shapeObj.toString();
        if (!shape.startsWith("sf-transition") && !shape.startsWith("sf-default-transition") && !shape.startsWith("sf-self-transition")) {
            return null;
        }

        TransitionDto.TransitionDtoBuilder builder = TransitionDto.builder()
            .id(cell.get("id") != null ? cell.get("id").toString() : null)
            .shape(shape);

        Object sourceObj = cell.get("source");
        if (sourceObj instanceof Map) {
            Map<String, Object> source = (Map<String, Object>) sourceObj;
            Object cellObj = source.get("cell");
            if (cellObj != null) {
                builder.sourceId(cellObj.toString());
            }
        }

        Object targetObj = cell.get("target");
        if (targetObj instanceof Map) {
            Map<String, Object> target = (Map<String, Object>) targetObj;
            Object cellObj = target.get("cell");
            if (cellObj != null) {
                builder.targetId(cellObj.toString());
            }
        }

        Object dataObj = cell.get("data");
        if (dataObj instanceof Map) {
            Map<String, Object> data = (Map<String, Object>) dataObj;
            builder.event(data.get("event") != null ? data.get("event").toString() : null)
                .condition(data.get("condition") != null ? data.get("condition").toString() : null)
                .conditionAction(data.get("conditionAction") != null ? data.get("conditionAction").toString() : null)
                .transitionAction(data.get("transitionAction") != null ? data.get("transitionAction").toString() : null)
                .priority(data.get("priority") instanceof Number ? ((Number) data.get("priority")).intValue() : null)
                .isDefault(Boolean.TRUE.equals(data.get("isDefault")));
        }

        return builder.build();
    }

    public boolean isDefaultTransition() {
        return Boolean.TRUE.equals(isDefault) || "sf-default-transition".equals(shape);
    }

    public boolean isSelfTransition() {
        return "sf-self-transition".equals(shape) || (sourceId != null && sourceId.equals(targetId));
    }
}
