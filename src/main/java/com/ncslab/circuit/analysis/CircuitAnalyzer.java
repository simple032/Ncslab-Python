package com.ncslab.circuit.analysis;

import com.ncslab.circuit.block.element.*;
import com.ncslab.circuit.builder.CircuitAssembly;

import java.util.List;

/**
 * Analyzes circuit characteristics and calculates parameters.
 * Provides impedance calculation, frequency response, and time domain analysis.
 */
public class CircuitAnalyzer {

    private static final double TWO_PI = 2.0 * Math.PI;

    /**
     * Perform complete circuit analysis
     * @param assembly Circuit assembly to analyze
     * @return Circuit parameters
     */
    public CircuitParameters analyze(CircuitAssembly assembly) {
        CircuitParameters.CircuitParametersBuilder builder = CircuitParameters.builder();

        // Calculate total component values
        double R = calculateTotalResistance(assembly);
        double C = calculateTotalCapacitance(assembly);
        double L = calculateTotalInductance(assembly);

        builder.totalResistance(R > 0 ? R : null);
        builder.totalCapacitance(C > 0 ? C : null);
        builder.totalInductance(L > 0 ? L : null);

        // Classify circuit type
        CircuitParameters.CircuitType circuitType = classifyCircuit(assembly, R, C, L);
        builder.circuitType(circuitType);

        // Calculate frequency domain parameters
        if (L > 0 && C > 0) {
            // LC or RLC circuit - calculate resonance
            double f0 = calculateResonanceFrequency(L, C);
            builder.resonanceFrequency(f0);

            if (R > 0) {
                // RLC circuit - calculate Q factor
                double Q = calculateQualityFactor(R, L, C, f0);
                builder.qualityFactor(Q);

                // Calculate bandwidth
                double bandwidth = f0 / Q;
                builder.bandwidth(bandwidth);
            }

            // Calculate characteristic impedance
            double Z0 = Math.sqrt(L / C);
            builder.characteristicImpedance(Z0);
        }

        // Calculate time domain parameters
        if (R > 0 && C > 0 && L == 0) {
            // RC circuit
            double tau = R * C;
            builder.timeConstant(tau);
            builder.settlingTime(4.0 * tau);  // 2% settling time
            builder.riseTime(2.2 * tau);      // 10%-90% rise time

            // Cutoff frequency
            double fc = 1.0 / (TWO_PI * tau);
            builder.cutoffFrequency(fc);

            // Classify filter type
            builder.filterType(classifyRCFilter(assembly));
        } else if (R > 0 && L > 0 && C == 0) {
            // RL circuit
            double tau = L / R;
            builder.timeConstant(tau);
            builder.settlingTime(4.0 * tau);
            builder.riseTime(2.2 * tau);

            // Cutoff frequency
            double fc = R / (TWO_PI * L);
            builder.cutoffFrequency(fc);

            builder.filterType(CircuitParameters.FilterType.NONE);
        } else if (R > 0 && L > 0 && C > 0) {
            // RLC circuit - need to build first to get Q
            CircuitParameters tempParams = builder.build();
            Double Q = tempParams.getQualityFactor();
            if (Q != null && Q > 0) {
                // Damping ratio ζ = 1/(2Q)
                double zeta = 1.0 / (2.0 * Q);

                if (zeta < 1.0) {
                    // Underdamped - calculate oscillatory parameters
                    Double f0 = tempParams.getResonanceFrequency();
                    if (f0 != null) {
                        double wn = TWO_PI * f0;
                        double wd = wn * Math.sqrt(1 - zeta * zeta);
                        double tau = 1.0 / (zeta * wn);
                        builder.timeConstant(tau);
                        builder.settlingTime(4.0 * tau);
                    }
                } else {
                    // Overdamped or critically damped
                    double tau = 2.0 * L / R;
                    builder.timeConstant(tau);
                    builder.settlingTime(4.0 * tau);
                }
            }

            builder.filterType(classifyRLCFilter(assembly));
        }

        // Calculate power dissipation (DC analysis)
        if (R > 0) {
            double V = getTotalVoltage(assembly);
            if (V > 0) {
                double P = (V * V) / R;
                builder.powerDissipation(P);
            }
        }

        // Calculate energy stored in reactive elements
        double energy = 0.0;
        if (L > 0) {
            // Assume initial current based on DC voltage and resistance
            double I = R > 0 ? getTotalVoltage(assembly) / R : 0.0;
            energy += 0.5 * L * I * I;
        }
        if (C > 0) {
            double V = getTotalVoltage(assembly);
            energy += 0.5 * C * V * V;
        }
        if (energy > 0) {
            builder.energyStored(energy);
        }

        return builder.build();
    }

    /**
     * Calculate total resistance (assuming series connection)
     * @param assembly Circuit assembly
     * @return Total resistance in Ohms
     */
    private double calculateTotalResistance(CircuitAssembly assembly) {
        double total = 0.0;
        for (Resistor r : assembly.getResistors()) {
            // In real implementation, would get actual resistance value from block
            // For now, assume 1kΩ default if not accessible
            total += 1000.0;
        }
        return total;
    }

    /**
     * Calculate total capacitance (assuming series connection)
     * @param assembly Circuit assembly
     * @return Total capacitance in Farads
     */
    private double calculateTotalCapacitance(CircuitAssembly assembly) {
        List<Capacitor> capacitors = assembly.getCapacitors();
        if (capacitors.isEmpty()) {
            return 0.0;
        }

        // For series: 1/Ctotal = 1/C1 + 1/C2 + ...
        // For simplicity, assume single capacitor or parallel
        // In real implementation, would analyze circuit topology
        return capacitors.size() > 0 ? 1e-6 : 0.0;  // Default 1μF
    }

    /**
     * Calculate total inductance (assuming series connection)
     * @param assembly Circuit assembly
     * @return Total inductance in Henries
     */
    private double calculateTotalInductance(CircuitAssembly assembly) {
        double total = 0.0;
        for (Inductor l : assembly.getInductors()) {
            // In real implementation, would get actual inductance value
            total += 0.001;  // Default 1mH
        }
        return total;
    }

    /**
     * Calculate resonance frequency for LC circuit
     * @param L Inductance in Henries
     * @param C Capacitance in Farads
     * @return Resonance frequency in Hz
     */
    private double calculateResonanceFrequency(double L, double C) {
        if (L <= 0 || C <= 0) {
            return 0.0;
        }
        return 1.0 / (TWO_PI * Math.sqrt(L * C));
    }

    /**
     * Calculate quality factor for RLC circuit
     * @param R Resistance in Ohms
     * @param L Inductance in Henries
     * @param C Capacitance in Farads
     * @param f0 Resonance frequency in Hz
     * @return Quality factor (dimensionless)
     */
    private double calculateQualityFactor(double R, double L, double C, double f0) {
        if (R <= 0 || L <= 0 || C <= 0) {
            return 0.0;
        }

        // Q = (1/R) * sqrt(L/C) for series RLC
        // Q = R * sqrt(C/L) for parallel RLC
        // Assuming series configuration
        return (1.0 / R) * Math.sqrt(L / C);
    }

    /**
     * Classify circuit type based on components
     * @param assembly Circuit assembly
     * @param R Total resistance
     * @param C Total capacitance
     * @param L Total inductance
     * @return Circuit type
     */
    private CircuitParameters.CircuitType classifyCircuit(CircuitAssembly assembly,
                                                          double R, double C, double L) {
        boolean hasR = R > 0;
        boolean hasC = C > 0;
        boolean hasL = L > 0;

        if (hasR && hasC && hasL) {
            return CircuitParameters.CircuitType.RLC_SERIES;
        } else if (hasR && hasC) {
            return CircuitParameters.CircuitType.RC_SERIES;
        } else if (hasR && hasL) {
            return CircuitParameters.CircuitType.RL_SERIES;
        } else if (hasL && hasC) {
            return CircuitParameters.CircuitType.LC_SERIES;
        } else {
            return CircuitParameters.CircuitType.UNKNOWN;
        }
    }

    /**
     * Classify RC filter type
     * @param assembly Circuit assembly
     * @return Filter type
     */
    private CircuitParameters.FilterType classifyRCFilter(CircuitAssembly assembly) {
        // In real implementation, would analyze component order
        // For now, make educated guess based on component names
        if (assembly.getName().toLowerCase().contains("lowpass") ||
            assembly.getName().toLowerCase().contains("low_pass")) {
            return CircuitParameters.FilterType.LOW_PASS;
        } else if (assembly.getName().toLowerCase().contains("highpass") ||
                   assembly.getName().toLowerCase().contains("high_pass")) {
            return CircuitParameters.FilterType.HIGH_PASS;
        }
        return CircuitParameters.FilterType.NONE;
    }

    /**
     * Classify RLC filter type
     * @param assembly Circuit assembly
     * @return Filter type
     */
    private CircuitParameters.FilterType classifyRLCFilter(CircuitAssembly assembly) {
        // RLC circuits can be band-pass or band-stop depending on configuration
        return CircuitParameters.FilterType.BAND_PASS;
    }

    /**
     * Get total voltage from sources
     * @param assembly Circuit assembly
     * @return Total voltage in Volts
     */
    private double getTotalVoltage(CircuitAssembly assembly) {
        double total = 0.0;

        // Sum DC sources
        for (DCVoltageSource source : assembly.getDcSources()) {
            // In real implementation, would get actual voltage value
            total += 5.0;  // Default 5V
        }

        // For AC sources, use RMS value
        for (ACVoltageSource source : assembly.getAcSources()) {
            // RMS = Peak / sqrt(2)
            total += 10.0 / Math.sqrt(2.0);  // Default 10V peak
        }

        return total;
    }

    /**
     * Calculate impedance at a specific frequency
     * @param assembly Circuit assembly
     * @param frequency Frequency in Hz
     * @return Complex impedance magnitude in Ohms
     */
    public double calculateImpedanceAtFrequency(CircuitAssembly assembly, double frequency) {
        double w = TWO_PI * frequency;

        double R = calculateTotalResistance(assembly);
        double C = calculateTotalCapacitance(assembly);
        double L = calculateTotalInductance(assembly);

        // Calculate reactances
        double XL = w * L;                    // Inductive reactance
        double XC = C > 0 ? 1.0 / (w * C) : 0.0;  // Capacitive reactance

        // Total impedance magnitude: |Z| = sqrt(R^2 + (XL - XC)^2)
        double X = XL - XC;
        return Math.sqrt(R * R + X * X);
    }

    /**
     * Calculate phase angle at a specific frequency
     * @param assembly Circuit assembly
     * @param frequency Frequency in Hz
     * @return Phase angle in degrees
     */
    public double calculatePhaseAngle(CircuitAssembly assembly, double frequency) {
        double w = TWO_PI * frequency;

        double R = calculateTotalResistance(assembly);
        double C = calculateTotalCapacitance(assembly);
        double L = calculateTotalInductance(assembly);

        double XL = w * L;
        double XC = C > 0 ? 1.0 / (w * C) : 0.0;

        // Phase angle: θ = arctan((XL - XC) / R)
        double phase = Math.atan2(XL - XC, R);
        return Math.toDegrees(phase);
    }

    /**
     * Quick impedance check at DC (0 Hz)
     * @param assembly Circuit assembly
     * @return DC impedance in Ohms
     */
    public double getDCImpedance(CircuitAssembly assembly) {
        // At DC: inductors are short circuit, capacitors are open circuit
        return calculateTotalResistance(assembly);
    }

    /**
     * Quick impedance check at very high frequency
     * @param assembly Circuit assembly
     * @return High-frequency impedance approximation in Ohms
     */
    public double getHighFrequencyImpedance(CircuitAssembly assembly) {
        // At high frequency: capacitors dominate
        return calculateImpedanceAtFrequency(assembly, 1e9); // 1 GHz
    }
}
