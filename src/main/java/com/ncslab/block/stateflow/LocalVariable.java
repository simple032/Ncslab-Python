package com.ncslab.block.stateflow;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Stateflow 局部变量实体。
 * 对应 DTO {@link com.ncslab.dto.block.specialized.stateflow.data.LocalVariableDto}。
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class LocalVariable extends Variable {
}
