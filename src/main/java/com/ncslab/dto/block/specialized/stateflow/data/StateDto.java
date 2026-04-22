package com.ncslab.dto.block.specialized.stateflow.data;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;
import java.util.Map;

/**
 * Stateflow 状态节点数据 DTO。
 * 从前端 X6 cell 数据中解析得到，对应 StateNodeData。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StateDto {

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

    /** X6 位置信息（可选） */
    private Map<String, Object> position;

    /** X6 尺寸信息（可选） */
    private Map<String, Object> size;

    /**
     * 从 X6 cell Map 中解析 StateDto 对象。
     *
     * @param cell X6 cell 的 Map 表示
     * @return 解析后的 StateDto，如果 shape 不是状态类型则返回 null
     */
    @SuppressWarnings("unchecked")
    public static StateDto fromCell(Map<String, Object> cell) {
        if (cell == null) return null;
        Object shapeObj = cell.get("shape");
        if (shapeObj == null) return null;
        String shape = shapeObj.toString();
        if (!shape.startsWith("sf-state") && !shape.startsWith("sf-initial") && !shape.startsWith("sf-final")) {
            return null;
        }

        StateDto.StateDtoBuilder builder = StateDto.builder()
            .id(cell.get("id") != null ? cell.get("id").toString() : null)
            .shape(shape)
            .position((Map<String, Object>) cell.get("position"))
            .size((Map<String, Object>) cell.get("size"));

        Object dataObj = cell.get("data");
        if (dataObj instanceof Map) {
            Map<String, Object> data = (Map<String, Object>) dataObj;
            builder.name(data.get("name") != null ? data.get("name").toString() : null)
                .entryAction(data.get("entryAction") != null ? data.get("entryAction").toString() : null)
                .exitAction(data.get("exitAction") != null ? data.get("exitAction").toString() : null)
                .duringAction(data.get("duringAction") != null ? data.get("duringAction").toString() : null)
                .isParallel(Boolean.TRUE.equals(data.get("isParallel")))
                .isDefault(Boolean.TRUE.equals(data.get("isDefault")))
                .description(data.get("description") != null ? data.get("description").toString() : null);

            Object substatesObj = data.get("substates");
            if (substatesObj instanceof List) {
                builder.substates((List<String>) substatesObj);
            }
        }

        return builder.build();
    }

    public boolean isInitial() {
        return "sf-initial".equals(shape);
    }

    public boolean isFinal() {
        return "sf-final".equals(shape);
    }
}
