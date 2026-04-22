package com.ncslab.block.stateflow;

import com.ncslab.dto.block.specialized.stateflow.data.EventDto;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * Stateflow 事件实体。
 * 对应 DTO {@link EventDto}，用于 Block 业务层。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Event {

    /** 事件名称 */
    private String name;

    /** 事件类型: input, output, local */
    private String eventType;

    /** 触发类型（用于输入事件）: rising, falling, either, function-call */
    private String triggerType;

    /** 注释 */
    private String description;

    /**
     * 从 DTO 转换为实体。
     */
    public static Event fromDto(EventDto dto) {
        if (dto == null) return null;
        return Event.builder()
            .name(dto.getName())
            .eventType(dto.getEventType())
            .triggerType(dto.getTriggerType())
            .description(dto.getDescription())
            .build();
    }
}
