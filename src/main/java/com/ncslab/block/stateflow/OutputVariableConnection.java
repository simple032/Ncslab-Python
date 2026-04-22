package com.ncslab.block.stateflow;

import com.ncslab.block.Block;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Stateflow Chart 输出变量与下游信号目标的连接映射。
 * <p>
 * 记录每个 {@link OutputVariable} 对应的 Chart {@link OutputPort}，
 * 以及该端口通过信号线连接到的所有下游模块 {@link InputPort}。
 * 用于后续仿真步进和 C 代码生成时，将 OutputVariable 的值传播到下游模块。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OutputVariableConnection {

    /** 对应的输出变量 */
    private OutputVariable variable;

    /** 变量在 chart 变量列表中的索引 */
    private int variableIndex;

    /** 对应的 Chart 输出端口 */
    private OutputPort outputPort;

    /** 输出端口号（从 1 开始） */
    private int outputPortNumber;

    /** 输出信号（包含数据类型、维度等信息） */
    private OutputSignal outputSignal;

    /** 下游连接目标列表（支持信号分叉） */
    @Builder.Default
    private List<OutputVariableSink> sinks = new ArrayList<>();

    /**
     * 是否至少有一个下游连接。
     */
    public boolean isConnected() {
        return sinks != null && !sinks.isEmpty();
    }

    /**
     * 获取下游连接数量。
     */
    public int getSinkCount() {
        return sinks != null ? sinks.size() : 0;
    }

    /**
     * 获取输出信号的数据类型描述（用于代码生成）。
     */
    public String getDataType() {
        if (outputSignal == null) return "double";
        switch (outputSignal.getDataType()) {
            case REAL:
                return "double";
            case MATRIX:
                return "Matrix";
            case STRING:
                return "String";
            case BUS:
                return "Bus";
            default:
                return "double";
        }
    }

    /**
     * 获取输出信号的宽度（用于代码生成）。
     */
    public int getWidth() {
        return outputSignal != null ? outputSignal.getWidth() : 1;
    }

    /**
     * 获取输出信号的高度（用于代码生成）。
     */
    public int getHeight() {
        return outputSignal != null ? outputSignal.getHeight() : 1;
    }
}
