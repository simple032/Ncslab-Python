package com.ncslab.circuit.analysis;

import lombok.Data;
import lombok.Builder;

/**
 * Contains calculated circuit parameters from analysis.
 * Includes impedance, resonance frequency, time constants, and quality factor.
 */
@Data
@Builder
public class CircuitParameters {

    // Impedance analysis
    private Double totalResistance;        // Total DC resistance (Ω)
    private Double totalCapacitance;       // Total capacitance (F)
    private Double totalInductance;        // Total inductance (H)

    // Frequency domain parameters
    private Double resonanceFrequency;     // Resonant frequency (Hz)
    private Double cutoffFrequency;        // -3dB cutoff frequency (Hz)
    private Double bandwidth;              // Bandwidth (Hz)
    private Double qualityFactor;          // Q factor (dimensionless)

    // Time domain parameters
    private Double timeConstant;           // RC or L/R time constant (s)
    private Double settlingTime;           // 2% settling time (s)
    private Double riseTime;               // 10%-90% rise time (s)

    // AC analysis
    private Double characteristicImpedance; // Z0 for LC circuits (Ω)
    private Double phaseAngle;             // Phase angle at cutoff (degrees)

    // Power analysis
    private Double powerDissipation;       // DC power dissipation (W)
    private Double energyStored;           // Energy stored in reactive elements (J)

    // Circuit type classification
    private CircuitType circuitType;
    private FilterType filterType;

    public enum CircuitType {
        RC_SERIES,
        RL_SERIES,
        LC_SERIES,
        RLC_SERIES,
        RC_PARALLEL,
        RL_PARALLEL,
        LC_PARALLEL,
        RLC_PARALLEL,
        MIXED,
        UNKNOWN
    }

    public enum FilterType {
        LOW_PASS,
        HIGH_PASS,
        BAND_PASS,
        BAND_STOP,
        ALL_PASS,
        NONE
    }

    /**
     * Get resonance frequency in radians per second
     * @return Angular frequency (rad/s)
     */
    public Double getAngularResonanceFrequency() {
        if (resonanceFrequency == null) {
            return null;
        }
        return 2.0 * Math.PI * resonanceFrequency;
    }

    /**
     * Get cutoff frequency in radians per second
     * @return Angular cutoff frequency (rad/s)
     */
    public Double getAngularCutoffFrequency() {
        if (cutoffFrequency == null) {
            return null;
        }
        return 2.0 * Math.PI * cutoffFrequency;
    }

    /**
     * Check if circuit is resonant
     * @return true if circuit has resonance frequency
     */
    public boolean isResonant() {
        return resonanceFrequency != null && resonanceFrequency > 0;
    }

    /**
     * Check if circuit is underdamped (oscillatory)
     * @return true if Q > 0.5
     */
    public boolean isUnderdamped() {
        return qualityFactor != null && qualityFactor > 0.5;
    }

    /**
     * Check if circuit is overdamped
     * @return true if Q < 0.5
     */
    public boolean isOverdamped() {
        return qualityFactor != null && qualityFactor < 0.5;
    }

    /**
     * Check if circuit is critically damped
     * @return true if Q ≈ 0.5
     */
    public boolean isCriticallyDamped() {
        if (qualityFactor == null) {
            return false;
        }
        return Math.abs(qualityFactor - 0.5) < 0.01;
    }

    /**
     * Get formatted summary of circuit parameters
     * @return Summary string
     */
    public String getSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("Circuit Parameters Summary\n");
        sb.append("=========================\n");

        if (circuitType != null) {
            sb.append(String.format("Circuit Type: %s\n", circuitType));
        }

        if (filterType != null && filterType != FilterType.NONE) {
            sb.append(String.format("Filter Type: %s\n", filterType));
        }

        if (totalResistance != null) {
            sb.append(String.format("Total Resistance: %.3e Ω\n", totalResistance));
        }

        if (totalCapacitance != null) {
            sb.append(String.format("Total Capacitance: %.3e F\n", totalCapacitance));
        }

        if (totalInductance != null) {
            sb.append(String.format("Total Inductance: %.3e H\n", totalInductance));
        }

        if (resonanceFrequency != null) {
            sb.append(String.format("Resonance Frequency: %.3f Hz (%.3f rad/s)\n",
                resonanceFrequency, getAngularResonanceFrequency()));
        }

        if (cutoffFrequency != null) {
            sb.append(String.format("Cutoff Frequency: %.3f Hz\n", cutoffFrequency));
        }

        if (qualityFactor != null) {
            sb.append(String.format("Quality Factor (Q): %.3f", qualityFactor));
            if (isUnderdamped()) {
                sb.append(" (Underdamped)");
            } else if (isOverdamped()) {
                sb.append(" (Overdamped)");
            } else if (isCriticallyDamped()) {
                sb.append(" (Critically Damped)");
            }
            sb.append("\n");
        }

        if (timeConstant != null) {
            sb.append(String.format("Time Constant: %.3e s\n", timeConstant));
        }

        if (bandwidth != null) {
            sb.append(String.format("Bandwidth: %.3f Hz\n", bandwidth));
        }

        if (powerDissipation != null) {
            sb.append(String.format("Power Dissipation: %.3e W\n", powerDissipation));
        }

        return sb.toString();
    }

    /**
     * Get compact parameter string
     * @return Compact parameter description
     */
    public String getCompactDescription() {
        if (resonanceFrequency != null && qualityFactor != null) {
            return String.format("f0=%.1fHz, Q=%.2f, τ=%.2eμs",
                resonanceFrequency, qualityFactor,
                timeConstant != null ? timeConstant * 1e6 : 0.0);
        } else if (cutoffFrequency != null) {
            return String.format("fc=%.1fHz, τ=%.2eμs",
                cutoffFrequency,
                timeConstant != null ? timeConstant * 1e6 : 0.0);
        } else if (timeConstant != null) {
            return String.format("τ=%.2eμs", timeConstant * 1e6);
        }
        return "No characteristic parameters";
    }
}
