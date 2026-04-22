package com.ncslab.block.stateflow;

import com.ncslab.dto.block.specialized.stateflow.data.VariableDto;
import com.ncslab.dto.block.specialized.stateflow.data.InputVariableDto;
import com.ncslab.dto.block.specialized.stateflow.data.OutputVariableDto;
import com.ncslab.dto.block.specialized.stateflow.data.LocalVariableDto;
import com.ncslab.dto.block.specialized.stateflow.data.ParameterVariableDto;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Stateflow 数据变量抽象实体。
 * 对应 DTO {@link VariableDto}，用于 Block 业务层。
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class Variable {

    /** 变量名 */
    private String name;

    /** 数据类型 */
    private String dataType;

    /** 初始值 */
    private String initialValue;

    /** 作用域: input, output, local, parameter */
    private String scope;

    /** 变量大小（数组维度） */
    private String size;

    /** 注释 */
    private String description;

    /** Port 编号（仅 input/output 有效） */
    private Integer port;

    /** 运行时当前值（仿真期间使用） */
    private Object currentValue;

    /**
     * 从 DTO 转换为对应类型的实体。
     */
    public static Variable fromDto(VariableDto dto) {
        if (dto == null) return null;

        Variable entity;
        String scope = dto.getScope();
        if ("input".equalsIgnoreCase(scope)) {
            entity = new InputVariable();
        } else if ("output".equalsIgnoreCase(scope)) {
            entity = new OutputVariable();
        } else if ("local".equalsIgnoreCase(scope)) {
            entity = new LocalVariable();
        } else if ("parameter".equalsIgnoreCase(scope)) {
            entity = new ParameterVariable();
        } else {
            return null;
        }

        entity.setName(dto.getName());
        entity.setDataType(dto.getDataType());
        entity.setScope(dto.getScope());
        entity.setInitialValue(dto.getInitialValue());
        entity.setSize(dto.getSize());
        entity.setDescription(dto.getDescription());
        entity.setPort(dto.getPort());
        return entity;
    }

    public boolean isInput() {
        return "input".equalsIgnoreCase(scope);
    }

    public boolean isOutput() {
        return "output".equalsIgnoreCase(scope);
    }

    public boolean isLocal() {
        return "local".equalsIgnoreCase(scope);
    }

    public boolean isParameter() {
        return "parameter".equalsIgnoreCase(scope);
    }
}
