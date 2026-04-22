package com.ncslab.block.stateflow;

import com.ncslab.block.Block;
import com.ncslab.block.io.InputPort;
import com.ncslab.line.Line;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * OutputVariable 的单个下游连接目标。
 * <p>
 * 由于 Stateflow Chart 的 OutputPort 可能连接到多个下游模块（信号分叉），
 * 每个下游目标用一个 {@link OutputVariableSink} 表示。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OutputVariableSink {

    /** 信号线 */
    private Line line;

    /** 下游模块 */
    private Block targetBlock;

    /** 下游模块的输入端口 */
    private InputPort targetInputPort;

    /** 下游输入端口号（从 1 开始） */
    private int targetPortNumber;

    /** 下游模块名称 */
    private String targetBlockName;

    /** 下游模块路径 */
    private String targetBlockPath;

    /** 下游模块 blockUUID */
    private String targetBlockUUID;

    /**
     * 从 Line 对象快速构建 Sink。
     */
    public static OutputVariableSink fromLine(Line line) {
        if (line == null) return null;
        InputPort targetPort = line.getLinkedInputPort();
        if (targetPort == null) return null;

        Block targetBlock = targetPort.getBlock();
        OutputVariableSinkBuilder builder = OutputVariableSink.builder()
            .line(line)
            .targetInputPort(targetPort)
            .targetPortNumber(targetPort.getNumber());

        if (targetBlock != null) {
            builder
                .targetBlock(targetBlock)
                .targetBlockName(targetBlock.getBlockName())
                .targetBlockPath(targetBlock.getBlockPath())
                .targetBlockUUID(targetBlock.getBlockUUID());
        }
        return builder.build();
    }
}
