package com.ncslab.dto.block.specialized.hardware;

import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.common.TypedParameter;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * DTO for PWM for STM32 block.
 * Controls PWM output on STM32 microcontroller.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PWMForStm32Dto extends BlockDto {
    
    /**
     * PWM timer number on STM32.
     * Default: 1
     */
    private TypedParameter timerNumber;
    
    /**
     * PWM channel number.
     * Default: 1
     */
    private TypedParameter channelNumber;
    
    /**
     * PWM frequency in Hz.
     * Default: 1000
     */
    private TypedParameter frequency;
    
    /**
     * PWM duty cycle (0-100%).
     * Default: 50
     */
    private TypedParameter dutyCycle;
    
    /**
     * Sample time for PWM updates.
     * Default: 0.01
     */
    private TypedParameter sampleTime;
    
    public PWMForStm32Dto(String blockName, String blockPath) {
        super("PWMForStm32", blockName, blockPath);
        initializeDefaults();
    }
    
    private void initializeDefaults() {
        if (timerNumber == null) {
            timerNumber = TypedParameter.of(1);
        }
        if (channelNumber == null) {
            channelNumber = TypedParameter.of(1);
        }
        if (frequency == null) {
            frequency = TypedParameter.of(1000);
        }
        if (dutyCycle == null) {
            dutyCycle = TypedParameter.of(50);
        }
        if (sampleTime == null) {
            sampleTime = TypedParameter.of(0.01);
        }
    }
}