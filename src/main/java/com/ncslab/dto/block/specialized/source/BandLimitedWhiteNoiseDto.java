package com.ncslab.dto.block.specialized.source;

import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.common.TypedParameter;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * DTO for Band-Limited White Noise source block.
 * Generates band-limited white noise with specified noise power and sample time.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class BandLimitedWhiteNoiseDto extends BlockDto {
    
    /**
     * Noise power (variance) of the generated white noise.
     * Default: 1.0
     */
    private TypedParameter noisePower;
    
    /**
     * Sample time for noise generation.
     * Default: inherited (-1)
     */
    private TypedParameter sampleTime;
    
    /**
     * Seed for random number generation.
     * Default: 0 (auto-generated)
     */
    private TypedParameter seed;
    
    public BandLimitedWhiteNoiseDto(String blockName, String blockPath) {
        super("Band-LimitedWhiteNoise", blockName, blockPath);
        initializeDefaults();
    }
    
    private void initializeDefaults() {
        if (noisePower == null) {
            noisePower = TypedParameter.of(1.0);
        }
        if (sampleTime == null) {
            sampleTime = TypedParameter.of(-1.0);
        }
        if (seed == null) {
            seed = TypedParameter.of(0);
        }
    }
}