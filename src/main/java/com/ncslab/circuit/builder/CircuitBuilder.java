package com.ncslab.circuit.builder;

import com.ncslab.circuit.block.element.*;
import com.ncslab.ncslablink.NCSLabModel;

/**
 * Builder pattern for creating complex circuit assemblies.
 * Provides fluent API for constructing common circuit patterns.
 */
public class CircuitBuilder {

    private final CircuitAssembly assembly;
    private final NCSLabModel model;
    private final String basePath;

    /**
     * Create a new circuit builder
     * @param name Circuit name
     * @param path Circuit path in model
     * @param model Parent model
     */
    public CircuitBuilder(String name, String path, NCSLabModel model) {
        this.assembly = new CircuitAssembly(name, path, model);
        this.model = model;
        this.basePath = path;
    }

    /**
     * Get the constructed circuit assembly
     * @return Circuit assembly
     */
    public CircuitAssembly build() {
        return assembly;
    }

    // === Component Addition Methods ===

    /**
     * Add a resistor with specified resistance
     * @param name Component name
     * @param resistance Resistance in Ohms
     * @return This builder for chaining
     */
    public CircuitBuilder addResistor(String name, double resistance) {
        Resistor r = Resistor.create(name, basePath, resistance, model);
        assembly.addResistor(r);
        return this;
    }

    /**
     * Add a capacitor with specified capacitance
     * @param name Component name
     * @param capacitance Capacitance in Farads
     * @return This builder for chaining
     */
    public CircuitBuilder addCapacitor(String name, double capacitance) {
        Capacitor c = Capacitor.create(name, basePath, capacitance, model);
        assembly.addCapacitor(c);
        return this;
    }

    /**
     * Add a capacitor with specified capacitance and initial voltage
     * @param name Component name
     * @param capacitance Capacitance in Farads
     * @param initialVoltage Initial voltage in Volts
     * @return This builder for chaining
     */
    public CircuitBuilder addCapacitor(String name, double capacitance, double initialVoltage) {
        Capacitor c = Capacitor.create(name, basePath, capacitance, initialVoltage, model);
        assembly.addCapacitor(c);
        return this;
    }

    /**
     * Add an inductor with specified inductance
     * @param name Component name
     * @param inductance Inductance in Henries
     * @return This builder for chaining
     */
    public CircuitBuilder addInductor(String name, double inductance) {
        Inductor l = Inductor.create(name, basePath, inductance, model);
        assembly.addInductor(l);
        return this;
    }

    /**
     * Add an inductor with specified inductance and initial current
     * @param name Component name
     * @param inductance Inductance in Henries
     * @param initialCurrent Initial current in Amperes
     * @return This builder for chaining
     */
    public CircuitBuilder addInductor(String name, double inductance, double initialCurrent) {
        Inductor l = Inductor.create(name, basePath, inductance, initialCurrent, model);
        assembly.addInductor(l);
        return this;
    }

    /**
     * Add a DC voltage source
     * @param name Component name
     * @param voltage Voltage in Volts
     * @return This builder for chaining
     */
    public CircuitBuilder addDCSource(String name, double voltage) {
        DCVoltageSource v = DCVoltageSource.create(name, basePath, voltage, model);
        assembly.addDCSource(v);
        return this;
    }

    /**
     * Add an AC voltage source
     * @param name Component name
     * @param amplitude Peak amplitude in Volts
     * @param frequency Frequency in Hz
     * @return This builder for chaining
     */
    public CircuitBuilder addACSource(String name, double amplitude, double frequency) {
        ACVoltageSource v = ACVoltageSource.create(name, basePath, amplitude, frequency, model);
        assembly.addACSource(v);
        return this;
    }

    /**
     * Add an AC voltage source with phase shift
     * @param name Component name
     * @param amplitude Peak amplitude in Volts
     * @param frequency Frequency in Hz
     * @param phaseShift Phase shift in degrees
     * @return This builder for chaining
     */
    public CircuitBuilder addACSource(String name, double amplitude, double frequency, double phaseShift) {
        ACVoltageSource v = ACVoltageSource.create(name, basePath, amplitude, frequency, phaseShift, model);
        assembly.addACSource(v);
        return this;
    }

    // === Pre-configured Circuit Patterns ===

    /**
     * Create an RC low-pass filter
     * @param name Circuit name
     * @param path Circuit path
     * @param model Parent model
     * @param resistance Resistance in Ohms
     * @param capacitance Capacitance in Farads
     * @param voltage Supply voltage
     * @return Circuit assembly
     */
    public static CircuitAssembly createRCLowPassFilter(String name, String path, NCSLabModel model,
                                                        double resistance, double capacitance, double voltage) {
        return new CircuitBuilder(name, path, model)
            .addDCSource("V_in", voltage)
            .addResistor("R1", resistance)
            .addCapacitor("C1", capacitance)
            .build();
    }

    /**
     * Create an RC high-pass filter
     * @param name Circuit name
     * @param path Circuit path
     * @param model Parent model
     * @param resistance Resistance in Ohms
     * @param capacitance Capacitance in Farads
     * @param voltage Supply voltage
     * @return Circuit assembly
     */
    public static CircuitAssembly createRCHighPassFilter(String name, String path, NCSLabModel model,
                                                         double resistance, double capacitance, double voltage) {
        return new CircuitBuilder(name, path, model)
            .addDCSource("V_in", voltage)
            .addCapacitor("C1", capacitance)
            .addResistor("R1", resistance)
            .build();
    }

    /**
     * Create an RLC series circuit
     * @param name Circuit name
     * @param path Circuit path
     * @param model Parent model
     * @param resistance Resistance in Ohms
     * @param inductance Inductance in Henries
     * @param capacitance Capacitance in Farads
     * @param voltage Supply voltage
     * @return Circuit assembly
     */
    public static CircuitAssembly createRLCSeriesCircuit(String name, String path, NCSLabModel model,
                                                         double resistance, double inductance,
                                                         double capacitance, double voltage) {
        return new CircuitBuilder(name, path, model)
            .addDCSource("V_in", voltage)
            .addResistor("R1", resistance)
            .addInductor("L1", inductance)
            .addCapacitor("C1", capacitance)
            .build();
    }

    /**
     * Create an RLC parallel circuit
     * @param name Circuit name
     * @param path Circuit path
     * @param model Parent model
     * @param resistance Resistance in Ohms
     * @param inductance Inductance in Henries
     * @param capacitance Capacitance in Farads
     * @param voltage Supply voltage
     * @return Circuit assembly
     */
    public static CircuitAssembly createRLCParallelCircuit(String name, String path, NCSLabModel model,
                                                           double resistance, double inductance,
                                                           double capacitance, double voltage) {
        return new CircuitBuilder(name, path, model)
            .addDCSource("V_in", voltage)
            .addResistor("R1", resistance)
            .addInductor("L1", inductance)
            .addCapacitor("C1", capacitance)
            .build();
    }

    /**
     * Create a voltage divider circuit
     * @param name Circuit name
     * @param path Circuit path
     * @param model Parent model
     * @param r1 First resistor value in Ohms
     * @param r2 Second resistor value in Ohms
     * @param voltage Input voltage
     * @return Circuit assembly
     */
    public static CircuitAssembly createVoltageDivider(String name, String path, NCSLabModel model,
                                                       double r1, double r2, double voltage) {
        return new CircuitBuilder(name, path, model)
            .addDCSource("V_in", voltage)
            .addResistor("R1", r1)
            .addResistor("R2", r2)
            .build();
    }

    /**
     * Create an AC resonant circuit (LC tank)
     * @param name Circuit name
     * @param path Circuit path
     * @param model Parent model
     * @param inductance Inductance in Henries
     * @param capacitance Capacitance in Farads
     * @param amplitude AC amplitude in Volts
     * @param frequency AC frequency in Hz
     * @return Circuit assembly
     */
    public static CircuitAssembly createLCResonantCircuit(String name, String path, NCSLabModel model,
                                                          double inductance, double capacitance,
                                                          double amplitude, double frequency) {
        return new CircuitBuilder(name, path, model)
            .addACSource("V_ac", amplitude, frequency)
            .addInductor("L1", inductance)
            .addCapacitor("C1", capacitance)
            .build();
    }

    /**
     * Get circuit description
     * @return Description string
     */
    public String getDescription() {
        return assembly.getDescription();
    }
}
