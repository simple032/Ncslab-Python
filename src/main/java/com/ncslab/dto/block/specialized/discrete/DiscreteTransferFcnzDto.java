package com.ncslab.dto.block.specialized.discrete;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.common.TypedParameter;
import com.ncslab.dto.common.TypedParameterMap;
import com.ncslab.dto.mapper.validation.ValidationResult;
import com.ncslab.dto.annotations.MigrationCompatible;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.NoArgsConstructor;
import lombok.Builder;

/**
 * DTO representation of Discrete Transfer Function Z-domain block.
 * 
 * The Discrete Transfer Function Z-domain block is a specialized discrete-time
 * transfer function that operates directly in the z-domain with multiple inputs.
 * It has feedthrough characteristics and supports multiple input ports.
 * 
 * @author NCSLab DTO Migration
 * @version 1.0
 * @since 2025
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("Discrete_Transfer_Fcnz")
@MigrationCompatible(originalClass = "com.ncslab.block.discrete.Discrete_Transfer_Fcnz")
public class DiscreteTransferFcnzDto extends BlockDto {
    
    /**
     * Numerator coefficients in z-domain
     * Default: "[1]"
     * Format: "[a0 a1 a2 ... an]"
     */
    @Builder.Default
    private TypedParameter numerator = TypedParameter.of("[1]");
    
    /**
     * Denominator coefficients in z-domain
     * Default: "[1 -1]" (represents z - 1)
     * Format: "[b0 b1 b2 ... bn]"
     * Validation: Leading coefficient (b0) must be non-zero
     */
    @Builder.Default
    private TypedParameter denominator = TypedParameter.of("[1 -1]");
    
    /**
     * Sample time for discrete operation
     * Default: -1 (inherited)
     */
    @Builder.Default
    private TypedParameter sampleTime = TypedParameter.of(-1.0);
    
    /**
     * Output data type specification
     * Default: "Inherit: Same as input"
     */
    @Builder.Default
    private TypedParameter outDataTypeStr = TypedParameter.of("Inherit: Same as input");
    
    /**
     * Handle integer overflow by saturation
     * Default: false (off)
     */
    @Builder.Default
    private TypedParameter saturateOnIntegerOverflow = TypedParameter.of(false);
    
    // ===== PARAMETER ACCESS HELPERS =====
    
    public String getNumeratorValue() {
        return numerator != null ? numerator.getAsString() : "[1]";
    }
    
    public String getDenominatorValue() {
        return denominator != null ? denominator.getAsString() : "[1 -1]";
    }
    
    public Double getSampleTimeValue() {
        return sampleTime != null ? sampleTime.getAsDouble() : -1.0;
    }
    
    public String getOutDataTypeStrValue() {
        return outDataTypeStr != null ? outDataTypeStr.getAsString() : "Inherit: Same as input";
    }
    
    public Boolean getSaturateOnIntegerOverflowValue() {
        return saturateOnIntegerOverflow != null ? saturateOnIntegerOverflow.getAsBoolean() : false;
    }
    
    // ===== POLYNOMIAL COEFFICIENT PARSING =====
    
    /**
     * Parse polynomial coefficient array from string format "[a0 a1 a2 ... an]"
     */
    public double[] parsePolynomialCoefficients(String coeffStr) {
        if (coeffStr == null || coeffStr.trim().isEmpty()) {
            throw new IllegalArgumentException("Coefficient string cannot be null or empty");
        }
        
        // Remove brackets and split by whitespace
        String cleanStr = coeffStr.trim().replaceAll("^\\[|\\]$", "");
        String[] parts = cleanStr.trim().split("\\s+");
        
        if (parts.length == 0 || (parts.length == 1 && parts[0].trim().isEmpty())) {
            throw new IllegalArgumentException("No coefficients found in: " + coeffStr);
        }
        
        double[] coefficients = new double[parts.length];
        for (int i = 0; i < parts.length; i++) {
            try {
                coefficients[i] = Double.parseDouble(parts[i].trim());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid coefficient '" + parts[i] + "' in: " + coeffStr);
            }
        }
        
        return coefficients;
    }
    
    /**
     * Get numerator coefficients as double array
     */
    public double[] getNumeratorCoefficients() {
        return parsePolynomialCoefficients(getNumeratorValue());
    }
    
    /**
     * Get denominator coefficients as double array
     */
    public double[] getDenominatorCoefficients() {
        return parsePolynomialCoefficients(getDenominatorValue());
    }
    
    // ===== VALIDATION =====
    
    @Override
    public ValidationResult validate() {
        ValidationResult result = super.validate();
        
        // Validate numerator coefficients
        if (numerator != null) {
            try {
                double[] numCoeffs = getNumeratorCoefficients();
                if (numCoeffs.length == 0) {
                    result.addError("Numerator coefficients cannot be empty");
                }
                // Check for valid numbers
                for (double coeff : numCoeffs) {
                    if (Double.isNaN(coeff) || Double.isInfinite(coeff)) {
                        result.addError("Numerator coefficients must be finite numbers");
                        break;
                    }
                }
            } catch (Exception e) {
                result.addError("Invalid numerator format: " + e.getMessage());
            }
        }
        
        // Validate denominator coefficients
        if (denominator != null) {
            try {
                double[] denCoeffs = getDenominatorCoefficients();
                if (denCoeffs.length == 0) {
                    result.addError("Denominator coefficients cannot be empty");
                } else {
                    // Check that leading coefficient is non-zero
                    if (Math.abs(denCoeffs[0]) < 1e-15) {
                        result.addError("Leading coefficient of denominator cannot be zero");
                    }
                    // Check for valid numbers
                    for (double coeff : denCoeffs) {
                        if (Double.isNaN(coeff) || Double.isInfinite(coeff)) {
                            result.addError("Denominator coefficients must be finite numbers");
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                result.addError("Invalid denominator format: " + e.getMessage());
            }
        }
        
        // Validate sample time for discrete blocks
        if (sampleTime != null) {
            Double st = sampleTime.getAsDouble();
            if (st != null && st <= 0.0 && st != -1.0) {
                result.addError("Sample time must be > 0 for discrete blocks (or -1 for inherited)");
            }
        }
        
        return result;
    }
    
    // ===== UTILITY METHODS =====
    
    /**
     * Check if this block inherits its sample time
     */
    public boolean isInherited() {
        return getSampleTimeValue() == -1.0;
    }
    
    /**
     * Get the order of the transfer function (highest power in denominator)
     */
    public int getTransferFunctionOrder() {
        try {
            return getDenominatorCoefficients().length - 1;
        } catch (Exception e) {
            return 0;
        }
    }
    
    /**
     * Check if transfer function has direct feedthrough
     * Note: Z-domain blocks typically have feedthrough
     */
    public boolean hasDirectFeedthrough() {
        return true; // Z-domain blocks typically have feedthrough
    }
    
    /**
     * Get the number of states required for realization
     */
    public int getRequiredStateCount() {
        return Math.max(0, getTransferFunctionOrder());
    }
    
    /**
     * Check if this is a simple gain (numerator and denominator both order 0)
     */
    public boolean isSimpleGain() {
        try {
            double[] numCoeffs = getNumeratorCoefficients();
            double[] denCoeffs = getDenominatorCoefficients();
            return numCoeffs.length == 1 && denCoeffs.length == 1;
        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    public DiscreteTransferFcnzDto copy() {
        return DiscreteTransferFcnzDto.builder()
                .blockId(getBlockId())
                .blockName(getBlockName())
                .blockPath(getBlockPath())
                .blockUUID(getBlockUUID())
                .numerator(numerator != null ? numerator.copy() : null)
                .denominator(denominator != null ? denominator.copy() : null)
                .sampleTime(sampleTime != null ? sampleTime.copy() : null)
                .outDataTypeStr(outDataTypeStr != null ? outDataTypeStr.copy() : null)
                .saturateOnIntegerOverflow(saturateOnIntegerOverflow != null ? saturateOnIntegerOverflow.copy() : null)
                .build();
    }
    
    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("Numerator", numerator)
                .put("Denominator", denominator)
                .put("SampleTime", sampleTime)
                .put("OutDataTypeStr", outDataTypeStr)
                .put("SaturateOnIntegerOverflow", saturateOnIntegerOverflow)
                .build();
    }
    
    @Override
    public String toString() {
        return String.format("DiscreteTransferFcnzDto{id=%d, name='%s', type='%s', num='%s', den='%s', order=%d}", 
                           getBlockId(), 
                           getBlockName(), 
                           getBlockType(),
                           getNumeratorValue(), 
                           getDenominatorValue(),
                           getTransferFunctionOrder());
    }
}