package com.ncslab.dto.block.specialized.stateflow.data;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Stateflow 数据变量抽象基类 DTO。
 * 根据 scope 字段自动反序列化为对应子类。
 * 对应前端 DataVariable 接口。
 */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    property = "scope",
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    visible = true
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = InputVariableDto.class, name = "input"),
    @JsonSubTypes.Type(value = OutputVariableDto.class, name = "output"),
    @JsonSubTypes.Type(value = LocalVariableDto.class, name = "local"),
    @JsonSubTypes.Type(value = ParameterVariableDto.class, name = "parameter")
})
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class VariableDto {

    /** 变量名 */
    private String name;

    /** 数据类型: double, single, int8, int16, int32, uint8, uint16, uint32, boolean, string */
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
