package com.ncslab.dto.block.specialized.stateflow.data;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Stateflow 局部变量 DTO。
 * 对应 DataVariable.scope = "local"。
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class LocalVariableDto extends VariableDto {

    public LocalVariableDto(String name, String dataType) {
        setScope("local");
        setName(name);
        setDataType(dataType);
    }
}
