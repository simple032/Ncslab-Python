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
     * Seed for random number generation.
     * Default: 0 (auto-generated)
     */
    private TypedParameter seed;

    /**
     * Noise power/covariance (variance) of the generated white noise.
     * Default: 1.0
     */
    private TypedParameter cov;

    /**
     * Sample period (Ts) for the noise correlation time.
     * Default: 0.1
     */
    private TypedParameter ts;

    /**
     * Output data type specification.
     * Default: "double"
     */
    private TypedParameter outDataTypeStr;

    /**
     * Handle integer overflow by saturation.
     * Default: false (off)
     */
    private TypedParameter saturateOnIntegerOverflow;
    
    public BandLimitedWhiteNoiseDto(String blockName, String blockPath) {
        super(blockName, blockPath);
        initializeDefaults();
    }
    
    private void initializeDefaults() {
        if (seed == null) {
            seed = TypedParameter.of(0);
        }
        if (cov == null) {
            cov = TypedParameter.of(1.0);
        }
        if (ts == null) {
            ts = TypedParameter.of(0.1);
        }
        if (outDataTypeStr == null) {
            outDataTypeStr = TypedParameter.of("double");
        }
        if (saturateOnIntegerOverflow == null) {
            saturateOnIntegerOverflow = TypedParameter.of(false);
        }
    }
}