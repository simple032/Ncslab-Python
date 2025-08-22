package com.ncslab.dto.block.specialized.continuous;

import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.common.TypedParameter;
import com.ncslab.dto.mapper.validation.ValidationResult;
import com.ncslab.dto.annotations.MigrationCompatible;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * DTO representation of TransferFcn block with SIMULINK-compatible parameters.
 * 
 * This DTO provides a modern, type-safe interface for the TransferFcn block
 * and supports migration from the legacy JSONObject-based approach.
 * 
 * SIMULINK Parameters:
 * - Numerator: Numerator coefficients in descending powers of s (e.g., "[1 2 3]")
 * - Denominator: Denominator coefficients in descending powers of s (e.g., "[1 2 3 4]")
 * - AbsoluteTolerance: Absolute tolerance for simulation (default: "auto")
 * - ContinuousStateAttributes: Attributes for continuous states (default: "'''")
 * - RealizeZeroPoleGain: Realization method for zero-pole-gain (default: "off")
 * - SampleTime: Sample time for discrete operation (-1 for inherited, 0 for continuous)
 * - OutDataTypeStr: Output data type specification
 * - SaturateOnIntegerOverflow: Handle integer overflow
 * 
 * Transfer Function Representation:
 * H(s) = (num[0]*s^n + num[1]*s^(n-1) + ... + num[n]) / (den[0]*s^m + den[1]*s^(m-1) + ... + den[m])
 * 
 * @author BlockMigrationAutomation
 * @version 1.0
 * @since DTO Migration Week 7
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@MigrationCompatible(originalClass = "com.ncslab.block.continuous.TransferFcn")
public class TransferFcnDto extends BlockDto {
    
    // ===== TRANSFER FUNCTION SPECIFIC PARAMETERS =====
    
    /**
     * Numerator coefficients in descending powers of s
     * Format: "[a0 a1 a2 ... an]" where H(s) numerator = a0*s^n + a1*s^(n-1) + ... + an
     * Default: "[1]"
     * Validation: Must be valid polynomial coefficient array
     */
    private TypedParameter numerator;
    
    /**
     * Denominator coefficients in descending powers of s  
     * Format: "[b0 b1 b2 ... bm]" where H(s) denominator = b0*s^m + b1*s^(m-1) + ... + bm
     * Default: "[1 1]"
     * Validation: Must be valid polynomial coefficient array, leading coefficient != 0
     */
    private TypedParameter denominator;
    
    /**
     * Absolute tolerance for simulation
     * Default: "auto"
     * Options: "auto", or numeric value as string
     */
    private TypedParameter absoluteTolerance;
    
    /**
     * Attributes for continuous states
     * Default: "'''"
     */
    private TypedParameter continuousStateAttributes;
    
    /**
     * Realization method for zero-pole-gain representation
     * Default: "off"
     * Options: "on", "off"
     */
    private TypedParameter realizeZeroPoleGain;
    
    /**
     * Output data type specification
     * Default: "Inherit: Same as input"
     */
    private TypedParameter outDataTypeStr;
    
    /**
     * Handle integer overflow
     * Default: false (off)
     */
    private TypedParameter saturateOnIntegerOverflow;
    
    // ===== FACTORY METHODS =====
    
    /**
     * Create DTO from legacy parameters map (for migration support)
     */
    public static TransferFcnDto fromLegacyParameters(Map<String, String> params) {
        TransferFcnDto.TransferFcnDtoBuilder builder = TransferFcnDto.builder()
                .blockName(params.getOrDefault("blockName", "TransferFcn"))
                .blockPath(params.getOrDefault("blockPath", ""))
                .blockUUID(params.getOrDefault("blockUUID", ""))
                .blockType("TransferFcn");
        
        TransferFcnDto dto = builder.build();
        
        // Map parameters to TypedParameter
        if (params.containsKey("Numerator")) {
            dto.numerator = TypedParameter.of(params.get("Numerator"));
        }
        
        if (params.containsKey("Denominator")) {
            dto.denominator = TypedParameter.of(params.get("Denominator"));
        }
        
        if (params.containsKey("AbsoluteTolerance")) {
            dto.absoluteTolerance = TypedParameter.of(params.get("AbsoluteTolerance"));
        }
        
        if (params.containsKey("ContinuousStateAttributes")) {
            dto.continuousStateAttributes = TypedParameter.of(params.get("ContinuousStateAttributes"));
        }
        
        if (params.containsKey("RealizeZeroPoleGain")) {
            dto.realizeZeroPoleGain = TypedParameter.of(params.get("RealizeZeroPoleGain"));
        }
        
        if (params.containsKey("OutDataTypeStr")) {
            dto.outDataTypeStr = TypedParameter.of(params.get("OutDataTypeStr"));
        }
        
        if (params.containsKey("SaturateOnIntegerOverflow")) {
            dto.saturateOnIntegerOverflow = TypedParameter.of("on".equals(params.get("SaturateOnIntegerOverflow")));
        }
        
        return dto;
    }
    
    /**
     * Create builder with SIMULINK-compatible defaults
     */
    public static TransferFcnDtoBuilder builderWithDefaults() {
        TransferFcnDto dto = TransferFcnDto.builder()
                .blockType("TransferFcn")
                .sampleTime(0.0) // Continuous by default
                .build();
        
        // Set default typed parameters
        dto.numerator = TypedParameter.of("[1]");
        dto.denominator = TypedParameter.of("[1 1]");
        dto.absoluteTolerance = TypedParameter.of("auto");
        dto.continuousStateAttributes = TypedParameter.of("'''");
        dto.realizeZeroPoleGain = TypedParameter.of("off");
        dto.outDataTypeStr = TypedParameter.of("Inherit: Same as input");
        dto.saturateOnIntegerOverflow = TypedParameter.of(false);
        
        return TransferFcnDto.builder()
                .blockId(dto.getBlockId())
                .blockType(dto.getBlockType())
                .blockName(dto.getBlockName())
                .blockPath(dto.getBlockPath())
                .blockUUID(dto.getBlockUUID())
                .sampleTime(dto.getSampleTime())
                .numerator(dto.numerator)
                .denominator(dto.denominator)
                .absoluteTolerance(dto.absoluteTolerance)
                .continuousStateAttributes(dto.continuousStateAttributes)
                .realizeZeroPoleGain(dto.realizeZeroPoleGain)
                .outDataTypeStr(dto.outDataTypeStr)
                .saturateOnIntegerOverflow(dto.saturateOnIntegerOverflow);
    }
    
    // ===== PARAMETER ACCESS HELPERS =====
    
    public String getNumeratorValue() {
        return numerator != null ? numerator.getValue(String.class) : "[1]";
    }
    
    public String getDenominatorValue() {
        return denominator != null ? denominator.getValue(String.class) : "[1 1]";
    }
    
    public String getAbsoluteToleranceValue() {
        return absoluteTolerance != null ? absoluteTolerance.getValue(String.class) : "auto";
    }
    
    public String getContinuousStateAttributesValue() {
        return continuousStateAttributes != null ? continuousStateAttributes.getValue(String.class) : "'''";
    }
    
    public String getRealizeZeroPoleGainValue() {
        return realizeZeroPoleGain != null ? realizeZeroPoleGain.getValue(String.class) : "off";
    }
    
    public String getOutDataTypeStrValue() {
        return outDataTypeStr != null ? outDataTypeStr.getValue(String.class) : "Inherit: Same as input";
    }
    
    public Boolean getSaturateOnIntegerOverflowValue() {
        return saturateOnIntegerOverflow != null ? saturateOnIntegerOverflow.getValue(Boolean.class) : false;
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
        ValidationResult result = super.validate(); // Call parent validation
        
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
            String realizeValue = realizeZeroPoleGain.getValue(String.class);
            if (realizeValue != null && !realizeValue.equals("on") && !realizeValue.equals("off")) {
                result.addError("RealizeZeroPoleGain must be 'on' or 'off'");
            }
        }
        
        return result;
    }
    
    // ===== UTILITY METHODS =====
    
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
     * Check if continuous time (sample time = 0) or discrete time
     */
    public boolean isContinuousTime() {
        return getSampleTime() != null && getSampleTime() == 0.0;
    }
    
    /**
     * Create simple transfer function: K / (s + a)
     */
    public static TransferFcnDto createFirstOrder(String name, String path, double gain, double pole) {
        TransferFcnDto dto = new TransferFcnDto();
        dto.setBlockName(name);
        dto.setBlockPath(path);
        dto.setBlockType("TransferFcn");
        dto.setSampleTime(0.0); // Continuous
        
        // Set default parameters
        dto.setAbsoluteTolerance(TypedParameter.of("auto"));
        dto.setContinuousStateAttributes(TypedParameter.of("'''"));
        dto.setRealizeZeroPoleGain(TypedParameter.of("off"));
        dto.setOutDataTypeStr(TypedParameter.of("Inherit: Same as input"));
        dto.setSaturateOnIntegerOverflow(TypedParameter.of(false));
        
        // Set specific transfer function coefficients
        dto.setNumerator(TypedParameter.of(formatPolynomialCoefficients(new double[]{gain})));
        dto.setDenominator(TypedParameter.of(formatPolynomialCoefficients(new double[]{1, pole})));
        
        return dto;
    }
    
    /**
     * Create second order transfer function: K / (s^2 + 2*zeta*wn*s + wn^2)
     */
    public static TransferFcnDto createSecondOrder(String name, String path, double gain, 
                                                   double naturalFreq, double dampingRatio) {
        TransferFcnDto dto = new TransferFcnDto();
        dto.setBlockName(name);
        dto.setBlockPath(path);
        dto.setBlockType("TransferFcn");
        dto.setSampleTime(0.0); // Continuous
        
        // Set default parameters
        dto.setAbsoluteTolerance(TypedParameter.of("auto"));
        dto.setContinuousStateAttributes(TypedParameter.of("'''"));
        dto.setRealizeZeroPoleGain(TypedParameter.of("off"));
        dto.setOutDataTypeStr(TypedParameter.of("Inherit: Same as input"));
        dto.setSaturateOnIntegerOverflow(TypedParameter.of(false));
        
        double wn = naturalFreq;
        double wn2 = wn * wn;
        double twoZetaWn = 2 * dampingRatio * wn;
        
        dto.setNumerator(TypedParameter.of(formatPolynomialCoefficients(new double[]{gain})));
        dto.setDenominator(TypedParameter.of(formatPolynomialCoefficients(new double[]{1, twoZetaWn, wn2})));
        
        return dto;
    }
    
    @Override
    public TransferFcnDto copy() {
        return TransferFcnDto.builder()
                .blockId(getBlockId())
                .blockType(getBlockType())
                .blockName(getBlockName())
                .blockPath(getBlockPath())
                .blockUUID(getBlockUUID())
                .sampleTime(getSampleTime())
                .numerator(numerator != null ? numerator.copy() : null)
                .denominator(denominator != null ? denominator.copy() : null)
                .absoluteTolerance(absoluteTolerance != null ? absoluteTolerance.copy() : null)
                .continuousStateAttributes(continuousStateAttributes != null ? continuousStateAttributes.copy() : null)
                .realizeZeroPoleGain(realizeZeroPoleGain != null ? realizeZeroPoleGain.copy() : null)
                .outDataTypeStr(outDataTypeStr != null ? outDataTypeStr.copy() : null)
                .saturateOnIntegerOverflow(saturateOnIntegerOverflow != null ? saturateOnIntegerOverflow.copy() : null)
                .build();
    }
    
    /**
     * Convert to legacy parameters format for backward compatibility
     */
    public Map<String, String> toLegacyParameters() {
        Map<String, String> params = new HashMap<>();
        
        if (numerator != null) {
            params.put("Numerator", String.valueOf(numerator.getValue()));
        }
        if (denominator != null) {
            params.put("Denominator", String.valueOf(denominator.getValue()));
        }
        if (absoluteTolerance != null) {
            params.put("AbsoluteTolerance", String.valueOf(absoluteTolerance.getValue()));
        }
        if (continuousStateAttributes != null) {
            params.put("ContinuousStateAttributes", String.valueOf(continuousStateAttributes.getValue()));
        }
        if (realizeZeroPoleGain != null) {
            params.put("RealizeZeroPoleGain", String.valueOf(realizeZeroPoleGain.getValue()));
        }
        if (getSampleTime() != null) {
            params.put("SampleTime", String.valueOf(getSampleTime()));
        }
        if (outDataTypeStr != null) {
            params.put("OutDataTypeStr", String.valueOf(outDataTypeStr.getValue()));
        }
        if (saturateOnIntegerOverflow != null) {
            Boolean satVal = saturateOnIntegerOverflow.getValue(Boolean.class);
            params.put("SaturateOnIntegerOverflow", Boolean.TRUE.equals(satVal) ? "on" : "off");
        }
        
        return params;
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