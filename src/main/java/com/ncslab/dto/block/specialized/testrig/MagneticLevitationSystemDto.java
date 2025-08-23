package com.ncslab.dto.block.specialized.testrig;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.common.TypedParameter;
import com.ncslab.dto.common.TypedParameterMap;
import com.ncslab.dto.block.BlockPositionDto;
import com.ncslab.dto.block.BlockDimensionDto;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * DTO for MagneticLevitationSystem block - Magnetic levitation test rig simulation.
 * 
 * <p>This block provides a mathematical model of a magnetic levitation system
 * with state variables for position and velocity, and parameters for physical constants:</p>
 * <ul>
 *   <li><b>gravity</b>: Gravitational acceleration constant (default: 9.8 m/s²)</li>
 *   <li><b>EQUILIBRIUM_POINT_x0</b>: Equilibrium position (default: 0.2 m)</li>
 *   <li><b>EQUILIBRIUM_POINT_i0</b>: Equilibrium current (default: 6.105 A)</li>
 *   <li><b>TRANSDUCER_AIRGAP_VOLTAGE_CONSTANT</b>: Ks constant (default: -4.5871056)</li>
 *   <li><b>INPUT_RESISTANCE</b>: Ka constant (default: 5.8929)</li>
 *   <li><b>SampleTime</b>: Sample time for simulation (-1 for inherited)</li>
 *   <li><b>OutDataTypeStr</b>: Output data type specification</li>
 * </ul>
 * 
 * <p><b>Outputs:</b></p>
 * <ul>
 *   <li>Position: Current ball position</li>
 *   <li>Velocity: Current ball velocity</li>
 * </ul>
 * 
 * <p><b>Validation Rules:</b></p>
 * <ul>
 *   <li>All physical constants must be finite numbers</li>
 *   <li>SampleTime must be >= -1.0 and finite</li>
 *   <li>Equilibrium points must be non-zero for mathematical stability</li>
 * </ul>
 *
 * @author NCSLab DTO Generator
 * @version 1.0
 * @since 2025-01-22
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("MagneticLevitationSystem")
public class MagneticLevitationSystemDto extends BlockDto {
    
    // Constructor to set blockType for Jackson deserialization

    /**
     * Gravitational acceleration constant.
     * Physical constant for gravitational force calculation.
     */
    private TypedParameter gravity = TypedParameter.of(9.8);

    /**
     * Equilibrium position.
     * The stable position where the ball hovers in equilibrium.
     */
    private TypedParameter equilibriumPointX0 = TypedParameter.of(0.2);

    /**
     * Equilibrium current.
     * The current required to maintain equilibrium position.
     */
    private TypedParameter equilibriumPointI0 = TypedParameter.of(6.105);

    /**
     * Transducer airgap voltage constant (Ks).
     * System constant relating voltage to airgap.
     */
    private TypedParameter transducerAirgapVoltageConstant = TypedParameter.of(-4.5871056);

    /**
     * Input resistance constant (Ka).
     * System constant for input resistance modeling.
     */
    private TypedParameter inputResistance = TypedParameter.of(5.8929);

    /**
     * Output data type specification.
     * Controls the data type of the block outputs.
     */
    private TypedParameter outDataTypeStr = TypedParameter.of("Inherit: Same as input");

    /**
     * Constructs MagneticLevitationSystemDto with individual parameters.
     *
     * @param blockName     Name of the block
     * @param blockPath     Path of the block in the model hierarchy
     * @param gravity       Gravitational acceleration
     * @param equilibriumPointX0 Equilibrium position
     * @param equilibriumPointI0 Equilibrium current
     * @param transducerAirgapVoltageConstant Ks constant
     * @param inputResistance Ka constant
     * @param sampleTime    Sample time parameter
     * @param outDataTypeStr Output data type parameter
     */
    public MagneticLevitationSystemDto(String blockName, String blockPath,
                                      TypedParameter gravity,
                                      TypedParameter equilibriumPointX0,
                                      TypedParameter equilibriumPointI0,
                                      TypedParameter transducerAirgapVoltageConstant,
                                      TypedParameter inputResistance,
                                      TypedParameter sampleTime,
                                      TypedParameter outDataTypeStr) {
        super("MagneticLevitationSystem", blockName, blockPath);
        this.gravity = gravity;
        this.equilibriumPointX0 = equilibriumPointX0;
        this.equilibriumPointI0 = equilibriumPointI0;
        this.transducerAirgapVoltageConstant = transducerAirgapVoltageConstant;
        this.inputResistance = inputResistance;
        this.sampleTime = sampleTime;
        this.outDataTypeStr = outDataTypeStr;
    }

    /**
     * Constructs MagneticLevitationSystemDto with typed parameter map.
     *
     * @param blockName  Name of the block
     * @param blockPath  Path of the block in the model hierarchy
     * @param parameters Map of typed parameters
     */
    public MagneticLevitationSystemDto(String blockName, String blockPath, TypedParameterMap parameters) {
        super("MagneticLevitationSystem", blockName, blockPath);
        this.gravity = parameters.getTypedParameter("gravity", Double.class, 9.8);
        this.equilibriumPointX0 = parameters.getTypedParameter("EQUILIBRIUM_POINT_x0", Double.class, 0.2);
        this.equilibriumPointI0 = parameters.getTypedParameter("EQUILIBRIUM_POINT_i0", Double.class, 6.105);
        this.transducerAirgapVoltageConstant = parameters.getTypedParameter("TRANSDUCER_AIRGAP_VOLTAGE_CONSTANT", Double.class, -4.5871056);
        this.inputResistance = parameters.getTypedParameter("INPUT_RESISTANCE", Double.class, 5.8929);
        this.sampleTime = parameters.getTypedParameter("SampleTime", Double.class, -1.0);
        this.outDataTypeStr = parameters.getTypedParameter("OutDataTypeStr", String.class, "Inherit: Same as input");
    }

    /**
     * Creates MagneticLevitationSystemDto with specified block metadata and default parameters.
     *
     * @param blockName Block instance name
     * @param blockPath Hierarchical path in model
     * @param position Block position in diagram
     * @param dimension Block visual dimensions
     */
    public MagneticLevitationSystemDto(String blockName, String blockPath, BlockPositionDto position, BlockDimensionDto dimension) {
        super("MagneticLevitationSystem", blockName, blockPath, position, dimension);
        this.gravity = TypedParameter.of(9.8);
        this.equilibriumPointX0 = TypedParameter.of(0.2);
        this.equilibriumPointI0 = TypedParameter.of(6.105);
        this.transducerAirgapVoltageConstant = TypedParameter.of(-4.5871056);
        this.inputResistance = TypedParameter.of(5.8929);
        this.sampleTime = TypedParameter.of(-1.0);
        this.outDataTypeStr = TypedParameter.of("Inherit: Same as input");
    }

    // === Validation Methods ===

    @Override
    public boolean isValid() {
        if (!super.isValid()) {
            return false;
        }

        // Validate gravity
        if (gravity == null || gravity.getAsDouble() == null) {
            addValidationError("Gravity parameter cannot be null");
            return false;
        }

        double gravityValue = getGravityValue();
        if (Double.isNaN(gravityValue) || Double.isInfinite(gravityValue)) {
            addValidationError("Gravity must be finite");
            return false;
        }

        // Validate equilibrium points
        if (equilibriumPointX0 == null || equilibriumPointX0.getAsDouble() == null) {
            addValidationError("Equilibrium point x0 cannot be null");
            return false;
        }

        if (equilibriumPointI0 == null || equilibriumPointI0.getAsDouble() == null) {
            addValidationError("Equilibrium point i0 cannot be null");
            return false;
        }

        double x0Value = getEquilibriumPointX0Value();
        double i0Value = getEquilibriumPointI0Value();
        
        if (Double.isNaN(x0Value) || Double.isInfinite(x0Value) || x0Value == 0.0) {
            addValidationError("Equilibrium point x0 must be finite and non-zero");
            return false;
        }

        if (Double.isNaN(i0Value) || Double.isInfinite(i0Value) || i0Value == 0.0) {
            addValidationError("Equilibrium point i0 must be finite and non-zero");
            return false;
        }

        // Validate system constants
        if (transducerAirgapVoltageConstant == null || transducerAirgapVoltageConstant.getAsDouble() == null) {
            addValidationError("Transducer airgap voltage constant cannot be null");
            return false;
        }

        if (inputResistance == null || inputResistance.getAsDouble() == null) {
            addValidationError("Input resistance cannot be null");
            return false;
        }

        double ksValue = getTransducerAirgapVoltageConstantValue();
        double kaValue = getInputResistanceValue();

        if (Double.isNaN(ksValue) || Double.isInfinite(ksValue)) {
            addValidationError("Transducer airgap voltage constant must be finite");
            return false;
        }

        if (Double.isNaN(kaValue) || Double.isInfinite(kaValue)) {
            addValidationError("Input resistance must be finite");
            return false;
        }

        // Validate sample time
        if (sampleTime == null || sampleTime.getAsDouble() == null) {
            addValidationError("Sample time cannot be null");
            return false;
        }

        double sampleTimeValue = getSampleTimeValue();
        if (sampleTimeValue < -1.0 || Double.isNaN(sampleTimeValue) || Double.isInfinite(sampleTimeValue)) {
            addValidationError("Sample time must be >= -1.0 and finite");
            return false;
        }

        // Validate output data type
        if (outDataTypeStr == null || outDataTypeStr.getAsString() == null || 
            outDataTypeStr.getAsString().trim().isEmpty()) {
            addValidationError("Output data type cannot be null or empty");
            return false;
        }

        return true;
    }

    @Override
    public List<String> validateParameters() {
        List<String> errors = super.validateParameters();
        
        // Validate all physical constants for finite values
        if (gravity != null && gravity.getAsDouble() != null) {
            double gValue = getGravityValue();
            if (Double.isNaN(gValue) || Double.isInfinite(gValue)) {
                errors.add("Gravity must be finite");
            }
            if (gValue <= 0.0) {
                errors.add("Gravity should be positive for realistic simulation");
            }
        }
        
        if (equilibriumPointX0 != null && equilibriumPointX0.getAsDouble() != null) {
            double x0Value = getEquilibriumPointX0Value();
            if (Double.isNaN(x0Value) || Double.isInfinite(x0Value) || x0Value == 0.0) {
                errors.add("Equilibrium position x0 must be finite and non-zero");
            }
        }
        
        if (equilibriumPointI0 != null && equilibriumPointI0.getAsDouble() != null) {
            double i0Value = getEquilibriumPointI0Value();
            if (Double.isNaN(i0Value) || Double.isInfinite(i0Value) || i0Value == 0.0) {
                errors.add("Equilibrium current i0 must be finite and non-zero");
            }
        }
        
        // Validate sample time
        if (sampleTime != null && sampleTime.getAsDouble() != null) {
            double stValue = getSampleTimeValue();
            if (stValue < -1.0 || Double.isNaN(stValue) || Double.isInfinite(stValue)) {
                errors.add("Sample time must be >= -1.0 and finite");
            }
        }
        
        return errors;
    }

    // === Parameter Access Methods ===

    /**
     * Gets the gravity value.
     *
     * @return Gravitational acceleration constant
     */
    public double getGravityValue() {
        if (gravity != null && gravity.getAsDouble() != null) {
            return gravity.getAsDouble();
        }
        return 9.8; // Default value
    }

    /**
     * Gets the equilibrium position value.
     *
     * @return Equilibrium position x0
     */
    public double getEquilibriumPointX0Value() {
        if (equilibriumPointX0 != null && equilibriumPointX0.getAsDouble() != null) {
            return equilibriumPointX0.getAsDouble();
        }
        return 0.2; // Default value
    }

    /**
     * Gets the equilibrium current value.
     *
     * @return Equilibrium current i0
     */
    public double getEquilibriumPointI0Value() {
        if (equilibriumPointI0 != null && equilibriumPointI0.getAsDouble() != null) {
            return equilibriumPointI0.getAsDouble();
        }
        return 6.105; // Default value
    }

    /**
     * Gets the transducer airgap voltage constant value.
     *
     * @return Ks constant value
     */
    public double getTransducerAirgapVoltageConstantValue() {
        if (transducerAirgapVoltageConstant != null && transducerAirgapVoltageConstant.getAsDouble() != null) {
            return transducerAirgapVoltageConstant.getAsDouble();
        }
        return -4.5871056; // Default value
    }

    /**
     * Gets the input resistance value.
     *
     * @return Ka constant value
     */
    public double getInputResistanceValue() {
        if (inputResistance != null && inputResistance.getAsDouble() != null) {
            return inputResistance.getAsDouble();
        }
        return 5.8929; // Default value
    }

    /**
     * Gets the sample time value.
     *
     * @return Sample time for simulation
     */
    public double getSampleTimeValue() {
        if (sampleTime != null && sampleTime.getAsDouble() != null) {
            return sampleTime.getAsDouble();
        }
        return -1.0; // Default inherited
    }

    /**
     * Gets the output data type string.
     *
     * @return Output data type specification
     */
    public String getOutDataTypeStrValue() {
        if (outDataTypeStr != null && outDataTypeStr.getAsString() != null) {
            return outDataTypeStr.getAsString();
        }
        return "Inherit: Same as input";
    }

    // === Helper Methods ===

    /**
     * Checks if the system is configured for continuous time operation.
     *
     * @return true if sample time is 0 (continuous)
     */
    public boolean isContinuous() {
        return getSampleTimeValue() == 0.0;
    }

    /**
     * Checks if the system inherits its sample time.
     *
     * @return true if sample time is -1 (inherited)
     */
    public boolean isInherited() {
        return getSampleTimeValue() == -1.0;
    }

    /**
     * Checks if the system is configured for discrete time operation.
     *
     * @return true if sample time is positive (discrete)
     */
    public boolean isDiscrete() {
        return getSampleTimeValue() > 0.0;
    }

    // === Factory Methods ===
    
    @Override
    public MagneticLevitationSystemDto copy() {
        MagneticLevitationSystemDto copy = new MagneticLevitationSystemDto();
        
        // Copy base fields
        copy.setBlockId(getBlockId());
        copy.setBlockName(getBlockName());
        copy.setBlockPath(getBlockPath());
        copy.setBlockUUID(getBlockUUID());
        copy.setPosition(getPosition());
        copy.setDimension(getDimension());
        copy.setSampleTime(getSampleTime());
        
        // Copy DTO-specific fields
        copy.gravity = gravity != null ? gravity.copy() : null;
        copy.equilibriumPointX0 = equilibriumPointX0 != null ? equilibriumPointX0.copy() : null;
        copy.equilibriumPointI0 = equilibriumPointI0 != null ? equilibriumPointI0.copy() : null;
        copy.transducerAirgapVoltageConstant = transducerAirgapVoltageConstant != null ? transducerAirgapVoltageConstant.copy() : null;
        copy.inputResistance = inputResistance != null ? inputResistance.copy() : null;
        copy.outDataTypeStr = outDataTypeStr != null ? outDataTypeStr.copy() : null;
        
        return copy;
    }
    
    /**
     * Creates a TypedParameterMap from this DTO's parameters.
     *
     * @return TypedParameterMap containing all block parameters
     */
    @Override
    public TypedParameterMap toParameterMap() {
        return TypedParameterMap.builder()
                .put("gravity", gravity)
                .put("EQUILIBRIUM_POINT_x0", equilibriumPointX0)
                .put("EQUILIBRIUM_POINT_i0", equilibriumPointI0)
                .put("TRANSDUCER_AIRGAP_VOLTAGE_CONSTANT", transducerAirgapVoltageConstant)
                .put("INPUT_RESISTANCE", inputResistance)
                .put("SampleTime", sampleTime)
                .put("OutDataTypeStr", outDataTypeStr)
                .build();
    }

    /**
     * Gets parameter metadata for documentation and UI generation.
     *
     * @return Map of parameter names to their descriptions
     */
    public static Map<String, String> getParameterDescriptions() {
        return Map.of(
            "gravity", "Gravitational acceleration constant (m/s²)",
            "EQUILIBRIUM_POINT_x0", "Equilibrium position (m)",
            "EQUILIBRIUM_POINT_i0", "Equilibrium current (A)",
            "TRANSDUCER_AIRGAP_VOLTAGE_CONSTANT", "Ks - Transducer airgap voltage constant",
            "INPUT_RESISTANCE", "Ka - Input resistance constant",
            "SampleTime", "Sample time for simulation (-1 for inherited, 0 for continuous)",
            "OutDataTypeStr", "Output data type specification"
        );
    }

    @Override
    public String toString() {
        return String.format("MagneticLevitationSystemDto{blockName='%s', gravity=%.3f, x0=%.3f, i0=%.3f, Ks=%.6f, Ka=%.4f, sampleTime=%.3f}",
                           getBlockName(), getGravityValue(), getEquilibriumPointX0Value(), getEquilibriumPointI0Value(), 
                           getTransducerAirgapVoltageConstantValue(), getInputResistanceValue(), getSampleTimeValue());
    }
}