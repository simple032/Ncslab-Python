package com.ncslab.dto.block.specialized.driver;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.common.TypedParameter;
import com.ncslab.dto.common.TypedParameterMap;
import com.ncslab.dto.mapper.validation.ValidationResult;
import com.ncslab.dto.annotations.MigrationCompatible;
import com.ncslab.dto.versioning.VersionedDto;
import com.ncslab.dto.versioning.VersionInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.Builder;
import lombok.NoArgsConstructor;

/**
 * DTO representation of EtherCATAI block with SIMULINK-compatible parameters.
 * 
 * This DTO provides a modern, type-safe interface for the EtherCAT Analog Input block
 * and supports migration from the legacy JSONObject-based approach.
 * 
 * EtherCAT Parameters:
 * - SlaveID: EtherCAT slave device ID
 * - Interface: Network interface name (e.g., "eth0")
 * - timeSample: Sample time for real-time operation
 * - OutDataTypeStr: Output data type specification
 * 
 * @author BlockMigrationAutomation
 * @version 1.0
 * @since DTO Migration Week 5
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("EtherCATAI")
@MigrationCompatible(originalClass = "com.ncslab.block.driver.EtherCATAI")
public class EtherCATAIDto extends BlockDto implements VersionedDto {
    
    // Constructor for Jackson deserialization
    
    // ===== ETHERCAT AI SPECIFIC PARAMETERS =====
    
    /**
     * EtherCAT slave device ID
     * Default: 1
     * Validation: Must be positive integer
     */
    @Builder.Default
    private TypedParameter slaveID = TypedParameter.of(1);
    
    /**
     * Network interface name
     * Default: "eth0"
     * Validation: Must be valid interface name
     */
    @Builder.Default
    private TypedParameter interfaceName = TypedParameter.of("eth0");
    
    /**
     * Sample time for real-time operation
     * Default: 0.001 (1ms)
     * Validation: Must be positive
     */
    @Builder.Default
    private TypedParameter timeSample = TypedParameter.of(0.001);
    
    /**
     * Output data type specification
     * Default: "double"
     */
    @Builder.Default
    private TypedParameter outDataTypeStr = TypedParameter.of("double");
    
    // ===== PARAMETER ACCESS HELPERS =====
    
    public Integer getSlaveIDValue() {
        return slaveID != null ? slaveID.getAsInteger() : 1;
    }
    
    public String getInterfaceNameValue() {
        return interfaceName != null ? interfaceName.getAsString() : "eth0";
    }
    
    public Double getTimeSampleValue() {
        return timeSample != null ? timeSample.getAsDouble() : 0.001;
    }
    
    public String getOutDataTypeStrValue() {
        return outDataTypeStr != null ? outDataTypeStr.getAsString() : "double";
    }
    
    // ===== VALIDATION =====
    
    @Override
    public ValidationResult validate() {
        ValidationResult result = super.validate();
        
        // Validate slave ID is positive
        if (slaveID != null) {
            Integer sidValue = slaveID.getAsInteger();
            if (sidValue != null && sidValue <= 0) {
                result.addError("Slave ID must be positive");
            }
        }
        
        // Validate interface name is not empty
        if (interfaceName != null) {
            String ifaceValue = interfaceName.getAsString();
            if (ifaceValue == null || ifaceValue.trim().isEmpty()) {
                result.addError("Interface name cannot be empty");
            }
        }
        
        // Validate sample time is positive
        if (timeSample != null) {
            Double tsValue = timeSample.getAsDouble();
            if (tsValue != null && tsValue <= 0.0) {
                result.addError("Sample time must be positive");
            }
        }
        
        return result;
    }
    
    // ===== UTILITY METHODS =====
    
    /**
     * Check if using standard Ethernet interface
     */
    public boolean isStandardInterface() {
        String iface = getInterfaceNameValue();
        return iface.startsWith("eth") || iface.startsWith("ens") || iface.startsWith("enp");
    }
    
    /**
     * Check if using high-frequency sampling (< 10ms)
     */
    public boolean isHighFrequency() {
        return getTimeSampleValue() < 0.01;
    }
    
    @Override
    public EtherCATAIDto copy() {
        return EtherCATAIDto.builder()
                .blockId(getBlockId())
                .blockName(getBlockName())
                .blockPath(getBlockPath())
                .blockUUID(getBlockUUID())
                .sampleTime(getSampleTime())
                .slaveID(slaveID != null ? slaveID.copy() : null)
                .interfaceName(interfaceName != null ? interfaceName.copy() : null)
                .timeSample(timeSample != null ? timeSample.copy() : null)
                .outDataTypeStr(outDataTypeStr != null ? outDataTypeStr.copy() : null)
                .build();
    }
    
    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("SlaveID", slaveID)
                .put("Interface", interfaceName)
                .put("timeSample", timeSample)
                .put("OutDataTypeStr", outDataTypeStr)
                .build();
    }
    
    @Override
    public boolean isValidConfiguration() {
        ValidationResult result = validate();
        return result.isValid() && 
               slaveID != null && getSlaveIDValue() > 0 &&
               interfaceName != null && !getInterfaceNameValue().isEmpty() &&
               timeSample != null && getTimeSampleValue() > 0.0;
    }
    
    // ===== VERSIONING SUPPORT =====
    
    @Override
    public VersionInfo getVersionInfo() {
        return VersionInfo.of("1.0", "Initial DTO implementation");
    }
    
    @Override
    public String toString() {
        return String.format("EtherCATAIDto{id=%d, name='%s', slave=%d, interface='%s', sample=%.3fms}", 
                           getBlockId(), 
                           getBlockName(), 
                           getSlaveIDValue(),
                           getInterfaceNameValue(),
                           getTimeSampleValue() * 1000);
    }
}