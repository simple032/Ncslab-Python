package com.ncslab.dto.block.specialized.stateflow.data;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Stateflow 输入变量 DTO。
 * 对应 DataVariable.scope = "input"。
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class InputVariableDto extends VariableDto {

    public InputVariableDto(String name, String dataType) {
        setScope("input");
        setName(name);
        setDataType(dataType);
    }
}
