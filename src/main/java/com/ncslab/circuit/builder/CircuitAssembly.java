package com.ncslab.circuit.builder;

import com.ncslab.circuit.block.element.*;
import com.ncslab.ncslablink.NCSLabModel;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a circuit assembly containing multiple interconnected circuit elements.
 * Provides a container for managing complex circuit configurations.
 */
@Getter
public class CircuitAssembly {

    private final String name;
    private final String path;
    private final NCSLabModel model;

    private final List<Resistor> resistors = new ArrayList<>();
    private final List<Capacitor> capacitors = new ArrayList<>();
    private final List<Inductor> inductors = new ArrayList<>();
    private final List<DCVoltageSource> dcSources = new ArrayList<>();
    private final List<ACVoltageSource> acSources = new ArrayList<>();
    private final Map<String, Object> components = new HashMap<>();

    /**
     * Create a new circuit assembly
     * @param name Assembly name
     * @param path Assembly path in model
     * @param model Parent model
     */
    public CircuitAssembly(String name, String path, NCSLabModel model) {
        this.name = name;
        this.path = path;
        this.model = model;
    }

    /**
     * Add a resistor to the assembly
     * @param resistor Resistor to add
     * @return This assembly for chaining
     */
    public CircuitAssembly addResistor(Resistor resistor) {
        resistors.add(resistor);
        components.put(resistor.getBlockName(), resistor);
        return this;
    }

    /**
     * Add a capacitor to the assembly
     * @param capacitor Capacitor to add
     * @return This assembly for chaining
     */
    public CircuitAssembly addCapacitor(Capacitor capacitor) {
        capacitors.add(capacitor);
        components.put(capacitor.getBlockName(), capacitor);
        return this;
    }

    /**
     * Add an inductor to the assembly
     * @param inductor Inductor to add
     * @return This assembly for chaining
     */
    public CircuitAssembly addInductor(Inductor inductor) {
        inductors.add(inductor);
        components.put(inductor.getBlockName(), inductor);
        return this;
    }

    /**
     * Add a DC voltage source to the assembly
     * @param source DC voltage source to add
     * @return This assembly for chaining
     */
    public CircuitAssembly addDCSource(DCVoltageSource source) {
        dcSources.add(source);
        components.put(source.getBlockName(), source);
        return this;
    }

    /**
     * Add an AC voltage source to the assembly
     * @param source AC voltage source to add
     * @return This assembly for chaining
     */
    public CircuitAssembly addACSource(ACVoltageSource source) {
        acSources.add(source);
        components.put(source.getBlockName(), source);
        return this;
    }

    /**
     * Get component by name
     * @param name Component name
     * @return Component or null if not found
     */
    public Object getComponent(String name) {
        return components.get(name);
    }

    /**
     * Get all components in the assembly
     * @return List of all components
     */
    public List<Object> getAllComponents() {
        return new ArrayList<>(components.values());
    }

    /**
     * Get total number of components
     * @return Component count
     */
    public int getComponentCount() {
        return components.size();
    }

    /**
     * Get circuit description
     * @return Description string
     */
    public String getDescription() {
        return String.format("Circuit Assembly '%s': %d resistors, %d capacitors, %d inductors, %d DC sources, %d AC sources",
            name, resistors.size(), capacitors.size(), inductors.size(), dcSources.size(), acSources.size());
    }
}
