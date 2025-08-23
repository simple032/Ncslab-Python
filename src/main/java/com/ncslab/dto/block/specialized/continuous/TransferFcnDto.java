package com.ncslab.dto.block.specialized.continuous;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.common.TypedParameter;
import com.ncslab.dto.common.TypedParameterMap;
import com.ncslab.dto.mapper.validation.ValidationResult;
import com.ncslab.dto.annotations.MigrationCompatible;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.Builder;
import lombok.NoArgsConstructor;

/**
 * DTO representation of TransferFcn continuous block.
 * 
 * The Transfer Function block models a linear transfer function
 * H(s) = N(s)/D(s) where N(s) and D(s) are polynomials in s.
 * Coefficients are specified in descending powers of s.
 * 
 * @author NCSLab DTO Migration
 * @version 1.0
 * @since 2025
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("TransferFcn")
@MigrationCompatible(originalClass = "com.ncslab.block.continuous.TransferFcn")
public class TransferFcnDto extends BlockDto {
    
    /**
     * Numerator coefficients in descending powers of s
     * Default: "[1]"
     * Format: "[a0 a1 a2 ... an]"
     */
    @Builder.Default
    private TypedParameter numerator = TypedParameter.of("[1]");
    
    /**
     * Denominator coefficients in descending powers of s
     * Default: "[1 1]"
     * Format: "[b0 b1 b2 ... bn]"
     * Validation: Leading coefficient (b0) must be non-zero
     */
    @Builder.Default
    private TypedParameter denominator = TypedParameter.of("[1 1]");
    
    /**
     * Absolute tolerance for simulation
     * Default: "auto"
     */
    @Builder.Default
    private TypedParameter absoluteTolerance = TypedParameter.of("auto");
    
    /**
     * Attributes for continuous states
     * Default: "'''"
     */
    @Builder.Default
    private TypedParameter continuousStateAttributes = TypedParameter.of("'''");
    
    /**
     * Realization method for zero-pole-gain
     * Default: "off"
     * Options: "on", "off"
     */
    @Builder.Default
    private TypedParameter realizeZeroPoleGain = TypedParameter.of("off");
    
    /**
     * Sample time for the block operation
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
        return denominator != null ? denominator.getAsString() : "[1 1]";
    }
    
    public String getAbsoluteToleranceValue() {
        return absoluteTolerance != null ? absoluteTolerance.getAsString() : "auto";
    }
    
    public String getContinuousStateAttributesValue() {
        return continuousStateAttributes != null ? continuousStateAttributes.getAsString() : "'''";
    }
    
    public String getRealizeZeroPoleGainValue() {
        return realizeZeroPoleGain != null ? realizeZeroPoleGain.getAsString() : "off";
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
    
    /**
     * Format coefficient array as string "[a0 a1 a2 ... an]"
     */
    public static String formatPolynomialCoefficients(double[] coefficients) {
        if (coefficients == null || coefficients.length == 0) {
            return "[0]";
        }
        
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < coefficients.length; i++) {
            if (i > 0) sb.append(" ");
            sb.append(coefficients[i]);
        }
        sb.append("]");
        return sb.toString();
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
        
        // Validate realize zero pole gain setting
        if (realizeZeroPoleGain != null) {
            String realizeValue = realizeZeroPoleGain.getAsString();
            if (realizeValue != null && !realizeValue.equals("on") && !realizeValue.equals("off")) {
                result.addError("RealizeZeroPoleGain must be 'on' or 'off'");
            }
        }
        
        // Validate sample time
        if (sampleTime != null) {
            Double st = sampleTime.getAsDouble();
            if (st != null && st < -1.0) {
                result.addError("Sample time must be >= -1.0");
            }
        }
        
        return result;
    }
    
    // ===== UTILITY METHODS =====
    
    /**
     * Check if this block operates in continuous time
     */
    public boolean isContinuous() {
        return getSampleTimeValue() == 0.0;
    }
    
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
     * Check if transfer function has direct feedthrough (same order num and den)
     */
    public boolean hasDirectFeedthrough() {
        try {
            double[] numCoeffs = getNumeratorCoefficients();
            double[] denCoeffs = getDenominatorCoefficients();
            return numCoeffs.length == denCoeffs.length;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Get the number of states required for realization
     */
    public int getRequiredStateCount() {
        return Math.max(0, getTransferFunctionOrder());
    }
    
    /**
     * Check if this is a simple gain (numerator order 0, denominator order 0)
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
    public TransferFcnDto copy() {
        return TransferFcnDto.builder()
                .blockId(getBlockId())
                .blockName(getBlockName())
                .blockPath(getBlockPath())
                .blockUUID(getBlockUUID())
                .numerator(numerator != null ? numerator.copy() : null)
                .denominator(denominator != null ? denominator.copy() : null)
                .absoluteTolerance(absoluteTolerance != null ? absoluteTolerance.copy() : null)
                .continuousStateAttributes(continuousStateAttributes != null ? continuousStateAttributes.copy() : null)
                .realizeZeroPoleGain(realizeZeroPoleGain != null ? realizeZeroPoleGain.copy() : null)
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
                .put("AbsoluteTolerance", absoluteTolerance)
                .put("ContinuousStateAttributes", continuousStateAttributes)
                .put("RealizeZeroPoleGain", realizeZeroPoleGain)
                .put("SampleTime", sampleTime)
                .put("OutDataTypeStr", outDataTypeStr)
                .put("SaturateOnIntegerOverflow", saturateOnIntegerOverflow)
                .build();
    }
    
    @Override
    public String toString() {
        return String.format("TransferFcnDto{id=%d, name='%s', type='%s', num='%s', den='%s', order=%d}", 
                           getBlockId(), 
                           getBlockName(), 
                           getBlockType(),
                           getNumeratorValue(), 
                           getDenominatorValue(),
                           getTransferFunctionOrder());
    }
}