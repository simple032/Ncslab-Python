package com.ncslab.circuit.analysis;

import com.ncslab.circuit.builder.CircuitAssembly;
import com.ncslab.circuit.builder.CircuitBuilder;
import com.ncslab.ncslablink.NCSLabModel;

/**
 * Example usage of circuit analysis tools.
 * Demonstrates impedance calculation, frequency response analysis, and parameter extraction.
 */
public class AnalysisExample {

    /**
     * Example: Analyze an RC low-pass filter
     * @param model Parent model
     */
    public static void analyzeRCLowPassFilter(NCSLabModel model) {
        // Create RC low-pass filter: R=1kΩ, C=1μF, V=5V
        CircuitAssembly circuit = CircuitBuilder.createRCLowPassFilter(
            "RC_LPF", "/test", model, 1000.0, 1e-6, 5.0);

        // Create analyzer
        CircuitAnalyzer analyzer = new CircuitAnalyzer();

        // Perform complete analysis
        CircuitParameters params = analyzer.analyze(circuit);

        // Display results
        System.out.println("RC Low-Pass Filter Analysis");
        System.out.println("===========================");
        System.out.println(params.getSummary());
        System.out.println("\nCompact: " + params.getCompactDescription());

        // Analyze frequency response at key points
        System.out.println("\nFrequency Response:");
        double[] frequencies = {10, 100, 159.15, 1000, 10000}; // Including fc ≈ 159Hz
        for (double f : frequencies) {
            double Z = analyzer.calculateImpedanceAtFrequency(circuit, f);
            double phase = analyzer.calculatePhaseAngle(circuit, f);
            System.out.printf("  f=%.1f Hz: |Z|=%.1f Ω, θ=%.1f°\n", f, Z, phase);
        }
    }

    /**
     * Example: Analyze an RLC resonant circuit
     * @param model Parent model
     */
    public static void analyzeRLCResonantCircuit(NCSLabModel model) {
        // Create RLC series circuit: R=100Ω, L=0.1H, C=1μF, V=12V
        CircuitAssembly circuit = CircuitBuilder.createRLCSeriesCircuit(
            "RLC_Series", "/test", model, 100.0, 0.1, 1e-6, 12.0);

        CircuitAnalyzer analyzer = new CircuitAnalyzer();
        CircuitParameters params = analyzer.analyze(circuit);

        System.out.println("RLC Series Resonant Circuit Analysis");
        System.out.println("====================================");
        System.out.println(params.getSummary());

        // Check damping characteristics
        if (params.isUnderdamped()) {
            System.out.println("\nCircuit is UNDERDAMPED (oscillatory response)");
        } else if (params.isOverdamped()) {
            System.out.println("\nCircuit is OVERDAMPED (slow exponential response)");
        } else if (params.isCriticallyDamped()) {
            System.out.println("\nCircuit is CRITICALLY DAMPED (optimal settling)");
        }

        // Analyze at resonance
        if (params.getResonanceFrequency() != null) {
            double f0 = params.getResonanceFrequency();
            double Z_res = analyzer.calculateImpedanceAtFrequency(circuit, f0);
            System.out.printf("\nAt resonance (%.1f Hz): |Z|=%.1f Ω\n", f0, Z_res);
        }
    }

    /**
     * Example: Compare impedance across frequency range
     * @param model Parent model
     */
    public static void frequencySweepAnalysis(NCSLabModel model) {
        CircuitAssembly circuit = CircuitBuilder.createLCResonantCircuit(
            "LC_Tank", "/test", model, 0.01, 1e-6, 10.0, 1000.0);

        CircuitAnalyzer analyzer = new CircuitAnalyzer();

        System.out.println("Frequency Sweep Analysis");
        System.out.println("========================");

        // Logarithmic frequency sweep
        for (double decade = 1; decade <= 6; decade += 0.5) {
            double f = Math.pow(10, decade);
            double Z = analyzer.calculateImpedanceAtFrequency(circuit, f);
            double phase = analyzer.calculatePhaseAngle(circuit, f);

            System.out.printf("f=%8.1f Hz: |Z|=%8.1f Ω, θ=%6.1f°\n", f, Z, phase);
        }
    }

    /**
     * Example: Analyze voltage divider
     * @param model Parent model
     */
    public static void analyzeVoltageDivider(NCSLabModel model) {
        // Voltage divider: R1=1kΩ, R2=2kΩ, Vin=9V
        CircuitAssembly circuit = CircuitBuilder.createVoltageDivider(
            "VDiv", "/test", model, 1000.0, 2000.0, 9.0);

        CircuitAnalyzer analyzer = new CircuitAnalyzer();
        CircuitParameters params = analyzer.analyze(circuit);

        System.out.println("Voltage Divider Analysis");
        System.out.println("========================");
        System.out.println(params.getSummary());

        // Calculate output voltage
        double Rtotal = 3000.0; // R1 + R2
        double Vout = 9.0 * (2000.0 / Rtotal);
        System.out.printf("\nOutput voltage: %.2f V\n", Vout);
        System.out.printf("Attenuation: %.2f dB\n", 20 * Math.log10(Vout / 9.0));
    }

    /**
     * Example: Performance comparison of different circuits
     * @param model Parent model
     */
    public static void compareCircuitPerformance(NCSLabModel model) {
        System.out.println("Circuit Performance Comparison");
        System.out.println("=============================\n");

        // Create different circuits
        CircuitAssembly[] circuits = {
            CircuitBuilder.createRCLowPassFilter("RC_LPF", "/test", model, 1000, 1e-6, 5),
            CircuitBuilder.createRCHighPassFilter("RC_HPF", "/test", model, 1000, 1e-6, 5),
            CircuitBuilder.createRLCSeriesCircuit("RLC", "/test", model, 100, 0.1, 1e-6, 12)
        };

        CircuitAnalyzer analyzer = new CircuitAnalyzer();

        for (CircuitAssembly circuit : circuits) {
            CircuitParameters params = analyzer.analyze(circuit);
            System.out.printf("%-15s: %s\n",
                circuit.getName(),
                params.getCompactDescription());
        }
    }

    /**
     * Example: DC and AC impedance analysis
     * @param model Parent model
     */
    public static void impedanceAnalysis(NCSLabModel model) {
        CircuitAssembly circuit = CircuitBuilder.createRLCSeriesCircuit(
            "Test_RLC", "/test", model, 100.0, 0.1, 1e-6, 12.0);

        CircuitAnalyzer analyzer = new CircuitAnalyzer();

        System.out.println("Impedance Analysis");
        System.out.println("==================");

        double Z_dc = analyzer.getDCImpedance(circuit);
        double Z_hf = analyzer.getHighFrequencyImpedance(circuit);

        System.out.printf("DC Impedance (0 Hz): %.1f Ω\n", Z_dc);
        System.out.printf("HF Impedance (1 GHz): %.1f Ω\n", Z_hf);

        // Calculate at specific frequencies of interest
        double[] testFreq = {50, 60, 400, 1000, 10000};
        System.out.println("\nImpedance at standard frequencies:");
        for (double f : testFreq) {
            double Z = analyzer.calculateImpedanceAtFrequency(circuit, f);
            System.out.printf("  %5.0f Hz: %.1f Ω\n", f, Z);
        }
    }

    /**
     * Example: Time domain parameter extraction
     * @param model Parent model
     */
    public static void timeDomainAnalysis(NCSLabModel model) {
        CircuitAssembly circuit = CircuitBuilder.createRCLowPassFilter(
            "RC_Test", "/test", model, 10000, 1e-6, 5);

        CircuitAnalyzer analyzer = new CircuitAnalyzer();
        CircuitParameters params = analyzer.analyze(circuit);

        System.out.println("Time Domain Analysis");
        System.out.println("===================");

        if (params.getTimeConstant() != null) {
            System.out.printf("Time Constant (τ): %.3e s (%.3f μs)\n",
                params.getTimeConstant(),
                params.getTimeConstant() * 1e6);
        }

        if (params.getRiseTime() != null) {
            System.out.printf("Rise Time (10%%-90%%): %.3e s\n", params.getRiseTime());
        }

        if (params.getSettlingTime() != null) {
            System.out.printf("Settling Time (2%%): %.3e s\n", params.getSettlingTime());
        }
    }

    /**
     * Main method for demonstration
     */
    public static void main(String[] args) {
        System.out.println("Circuit Analysis Examples");
        System.out.println("========================");
        System.out.println("\nThese examples demonstrate the circuit analysis API.");
        System.out.println("In actual use, pass a valid NCSLabModel instance.");
    }
}
