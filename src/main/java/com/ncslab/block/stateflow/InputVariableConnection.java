package com.ncslab.block.stateflow;

import com.ncslab.block.Block;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Stateflow Chart 输入变量与外部信号源的连接映射。
 * <p>
 * 记录每个 {@link InputVariable} 对应的 Chart {@link InputPort}，
 * 以及该端口通过信号线连接到的前级模块 {@link OutputPort} 和 {@link Block}。
 * 用于后续仿真步进和 C 代码生成时，将外部信号值写入 InputVariable。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InputVariableConnection {

    /** 对应的输入变量 */
    private InputVariable variable;

    /** 变量在 chart 变量列表中的索引 */
    private int variableIndex;

    /** 对应的 Chart 输入端口 */
    private InputPort inputPort;

    /** 输入端口号（从 1 开始） */
    private int inputPortNumber;

    /** 信号线源模块（前级模块） */
    private Block sourceBlock;

    /** 信号线源模块的输出端口 */
    private OutputPort sourceOutputPort;

    /** 源端口号（从 1 开始） */
    private int sourcePortNumber;

    /** 源输出信号（包含数据类型、维度等信息） */
    private OutputSignal sourceOutputSignal;

    /** 源模块名称 */
    private String sourceBlockName;

    /** 源模块路径 */
    private String sourceBlockPath;

    /** 源模块 blockUUID */
    private String sourceBlockUUID;

    /**
     * 是否成功解析了连接（即该输入端口有连线接入）
     */
    public boolean isConnected() {
        return sourceBlock != null && sourceOutputPort != null;
    }

    /**
     * 获取源信号的数据类型描述（用于代码生成）
     */
    public String getSourceDataType() {
        if (sourceOutputSignal == null) return "double";
        switch (sourceOutputSignal.getDataType()) {
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
     * 获取源信号的宽度（用于代码生成）
     */
    public int getSourceWidth() {
        return sourceOutputSignal != null ? sourceOutputSignal.getWidth() : 1;
    }

    /**
     * 获取源信号的高度（用于代码生成）
     */
    public int getSourceHeight() {
        return sourceOutputSignal != null ? sourceOutputSignal.getHeight() : 1;
    }
}
