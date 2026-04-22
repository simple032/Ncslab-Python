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

            String entryAction = data.get("entryAction") != null ? data.get("entryAction").toString() : null;
            String exitAction = data.get("exitAction") != null ? data.get("exitAction").toString() : null;
            String duringAction = data.get("duringAction") != null ? data.get("duringAction").toString() : null;

            // Fallback: 前端可能把动作代码放在 code 字段中（格式：enty\nvar1=0）
            boolean allEmpty = (entryAction == null || entryAction.trim().isEmpty())
                && (exitAction == null || exitAction.trim().isEmpty())
                && (duringAction == null || duringAction.trim().isEmpty());

            if (allEmpty) {
                Object codeObj = data.get("code");
                if (codeObj != null) {
                    String code = codeObj.toString();
                    java.util.Map<String, String> parsed = parseStateCode(code);
                    if (parsed.containsKey("entry")) entryAction = parsed.get("entry");
                    if (parsed.containsKey("during")) duringAction = parsed.get("during");
                    if (parsed.containsKey("exit")) exitAction = parsed.get("exit");
                }
            }

            builder.name(data.get("name") != null ? data.get("name").toString() : null)
                .entryAction(entryAction)
                .exitAction(exitAction)
                .duringAction(duringAction)
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

    /**
     * 解析 Stateflow 状态节点的 code 字段。
     * <p>
     * 支持格式：
     * <pre>
     * enty
     * var1=0
     * dur
     * var1=var1+1
     * exit
     * var1=0
     * </pre>
     * 前缀说明：enty/entry=entryAction, dur/during=duringAction, exit=exitAction
     *
     * @param code 前端传递的 code 字段值
     * @return 解析后的动作映射，key 为 entry/during/exit
     */
    private static java.util.Map<String, String> parseStateCode(String code) {
        java.util.Map<String, String> result = new java.util.HashMap<>();
        if (code == null || code.trim().isEmpty()) {
            return result;
        }

        String[] lines = code.split("\n");
        String currentType = null;
        StringBuilder currentCode = new StringBuilder();

        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) continue;

            String lower = trimmed.toLowerCase();
            if (lower.equals("enty") || lower.equals("entry") ||
                lower.equals("dur") || lower.equals("during") ||
                lower.equals("exit")) {
                // 保存之前的动作
                if (currentType != null && currentCode.length() > 0) {
                    result.put(currentType, currentCode.toString().trim());
                }
                // 确定新动作类型
                if (lower.startsWith("enty") || lower.startsWith("entry")) {
                    currentType = "entry";
                } else if (lower.startsWith("dur")) {
                    currentType = "during";
                } else if (lower.startsWith("exit")) {
                    currentType = "exit";
                }
                currentCode = new StringBuilder();
            } else {
                // 代码行
                if (currentCode.length() > 0) currentCode.append("\n");
                currentCode.append(line);
            }
        }

        // 保存最后一个动作
        if (currentType != null && currentCode.length() > 0) {
            result.put(currentType, currentCode.toString().trim());
        } else if (currentType == null) {
            // 没有识别到动作类型前缀，整个 code 视为 entry action
            result.put("entry", code.trim());
        }

        return result;
    }

    public boolean isInitial() {
        return "sf-initial".equals(shape);
    }

    public boolean isFinal() {
        return "sf-final".equals(shape);
    }
}
