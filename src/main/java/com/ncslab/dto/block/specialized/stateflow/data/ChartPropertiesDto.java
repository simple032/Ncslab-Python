package com.ncslab.dto.block.specialized.stateflow.data;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * Stateflow Chart 属性配置 DTO。
 * 对应前端 ChartProperties 接口。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChartPropertiesDto {

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
}
