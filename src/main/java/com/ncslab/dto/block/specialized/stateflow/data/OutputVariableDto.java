package com.ncslab.dto.block.specialized.stateflow.data;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Stateflow 输出变量 DTO。
 * 对应 DataVariable.scope = "output"。
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class OutputVariableDto extends VariableDto {

    public OutputVariableDto(String name, String dataType) {
        setScope("output");
        setName(name);
        setDataType(dataType);
    }
}
