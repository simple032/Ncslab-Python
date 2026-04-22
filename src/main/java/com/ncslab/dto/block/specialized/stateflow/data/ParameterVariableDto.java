package com.ncslab.dto.block.specialized.stateflow.data;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Stateflow 参数 DTO。
 * 对应 DataVariable.scope = "parameter"。
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ParameterVariableDto extends VariableDto {

    public ParameterVariableDto(String name, String dataType) {
        setScope("parameter");
        setName(name);
        setDataType(dataType);
    }
}
