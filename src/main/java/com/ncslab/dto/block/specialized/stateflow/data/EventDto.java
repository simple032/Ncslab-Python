package com.ncslab.dto.block.specialized.stateflow.data;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * Stateflow 事件定义 DTO。
 * 对应前端 StateflowEvent 接口。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventDto {

    /** 事件名称 */
    private String name;

    /** 事件类型: input, output, local */
    private String eventType;

    /** 触发类型（用于输入事件）: rising, falling, either, function-call */
    private String triggerType;

    /** 注释 */
    private String description;
}
