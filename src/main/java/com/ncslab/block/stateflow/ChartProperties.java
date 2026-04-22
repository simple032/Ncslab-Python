package com.ncslab.block.stateflow;

import com.ncslab.dto.block.specialized.stateflow.data.ChartPropertiesDto;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * Stateflow Chart 属性实体。
 * 对应 DTO {@link ChartPropertiesDto}，用于 Block 业务层。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChartProperties {

    /** 状态机类型: Classic, Mealy, Moore */
    private String stateMachineType;

    /** 更新方法: inherited, discrete, continuous */
    private String updateMethod;

    /** 采样时间（离散更新时） */
    private String sampleTime;

    /** 是否启用零交叉检测 */
    private Boolean enableZeroCrossings;

    /** 是否启用 C 位运算 */
    private Boolean enableCBitOperations;

    /** 初始化时执行图表 */
    private Boolean executeAtInitialization;

    /** 每次唤醒时初始化输出 */
    private Boolean initializeOutputsEveryTime;

    /**
     * 从 DTO 转换为实体。
     */
    public static ChartProperties fromDto(ChartPropertiesDto dto) {
        if (dto == null) return null;
        return ChartProperties.builder()
            .stateMachineType(dto.getStateMachineType())
            .updateMethod(dto.getUpdateMethod())
            .sampleTime(dto.getSampleTime())
            .enableZeroCrossings(dto.getEnableZeroCrossings())
            .enableCBitOperations(dto.getEnableCBitOperations())
            .executeAtInitialization(dto.getExecuteAtInitialization())
            .initializeOutputsEveryTime(dto.getInitializeOutputsEveryTime())
            .build();
    }
}
