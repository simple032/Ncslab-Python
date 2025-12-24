package com.ncslab.block.io;

import java.util.*;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import com.ncslab.block.data.Data;
import com.ncslab.block.Block;
import Jama.Matrix;

/**
 * Represents a hierarchical bus signal that can contain multiple named elements.
 *
 * <p>A bus signal groups multiple signals together, similar to Simulink bus signals.
 * Each element in the bus has a name and can be either a scalar/matrix signal or
 * another nested bus signal.</p>
 *
 * <h2>Virtual vs. Non-Virtual Buses</h2>
 * <ul>
 *   <li><b>Virtual Bus:</b> Signals stored separately, no C struct generated (zero overhead)</li>
 *   <li><b>Non-Virtual Bus:</b> Signals packed into C struct (copy overhead, for hardware interfaces)</li>
 * </ul>
 *
 * <h2>Usage Example</h2>
 * <pre>
 * // Create a virtual bus
 * BusSignal sensorBus = BusSignal.createVirtualBus("SensorBus");
 * sensorBus.addElement("temperature", temperatureSignal);
 * sensorBus.addElement("pressure", pressureSignal);
 *
 * // Access elements
 * OutputSignal temp = sensorBus.getElementSignal("temperature");
 *
 * // Create nested bus
 * BusSignal vehicleBus = BusSignal.createVirtualBus("VehicleBus");
 * vehicleBus.addElement("speed", speedSignal);
 * vehicleBus.addNestedBus("sensors", sensorBus);
 *
 * // Access nested element using hierarchical path
 * OutputSignal temp2 = vehicleBus.getNestedElement("sensors.temperature");
 * </pre>
 *
 * @author NCSLab
 * @since Phase 1 - Bus Architecture Implementation
 */
@Slf4j
public class BusSignal extends OutputSignal {

    // === Core Properties ===

    /**
     * Bus definition (template) for this bus signal.
     * Null for virtual buses without formal definition.
     */
    @Getter
    @Setter
    private BusDefinition busDefinition;

    /**
     * Map of element name to BusElement.
     * LinkedHashMap preserves insertion order for consistent code generation.
     */
    @Getter
    private Map<String, BusElement> elements;

    /**
     * Whether this is a virtual bus (no runtime overhead) or non-virtual (C struct).
     * Virtual buses have zero overhead and are optimized away during code generation.
     * Non-virtual buses generate C struct definitions for hardware interfaces.
     */
    @Getter
    @Setter
    private boolean isVirtual;

    /**
     * Bus name used for code generation and display.
     */
    @Getter
    @Setter
    private String busName;

    // === Constructors ===

    /**
     * Creates a virtual bus signal (default).
     * This constructor is for standalone bus signals not associated with a block.
     *
     * @param busName Name of the bus
     */
    public BusSignal(String busName) {
        super(null, 0, 0, busName != null ? busName : "bus");
        this.busName = busName;
        this.elements = new LinkedHashMap<>();
        this.isVirtual = true;
        this.busDefinition = null;
        log.debug("Created virtual bus signal: '{}'", busName);
    }

    /**
     * Creates a bus signal with specific definition.
     * This constructor is for standalone bus signals not associated with a block.
     *
     * @param busName Name of the bus
     * @param busDefinition Bus definition template (can be null for virtual buses)
     * @param isVirtual Whether this is a virtual bus
     */
    public BusSignal(String busName, BusDefinition busDefinition, boolean isVirtual) {
        super(null, 0, 0, busName != null ? busName : "bus");
        this.busName = busName;
        this.busDefinition = busDefinition;
        this.isVirtual = isVirtual;
        this.elements = new LinkedHashMap<>();
        log.debug("Created {} bus signal: '{}' with definition: '{}'",
                 isVirtual ? "virtual" : "non-virtual",
                 busName,
                 busDefinition != null ? busDefinition.getName() : "none");
    }

    /**
     * Creates a bus signal associated with a block and output port.
     * This constructor is for bus signals created by blocks (e.g., BusCreator).
     *
     * @param block Parent block
     * @param id Signal ID
     * @param outputPortId Output port ID
     * @param localName Local signal name
     * @param busDefinition Bus definition template (can be null for virtual buses)
     * @param isVirtual Whether this is a virtual bus
     */
    public BusSignal(Block block, int id, int outputPortId, String localName,
                     BusDefinition busDefinition, boolean isVirtual) {
        super(block, id, outputPortId, localName);
        this.busName = localName;
        this.busDefinition = busDefinition;
        this.isVirtual = isVirtual;
        this.elements = new LinkedHashMap<>();
        log.debug("Created {} bus signal: '{}' with definition: '{}' for block {}",
                 isVirtual ? "virtual" : "non-virtual",
                 localName,
                 busDefinition != null ? busDefinition.getName() : "none",
                 block != null ? block.getBlockId() : "null");
    }

    /**
     * Creates a virtual bus signal (factory method).
     * Virtual buses have zero overhead and are optimized during code generation.
     *
     * @param busName Name of the bus
     * @return New virtual bus signal
     */
    public static BusSignal createVirtualBus(String busName) {
        return new BusSignal(busName);
    }

    /**
     * Creates a non-virtual bus signal (factory method).
     * Non-virtual buses generate C struct definitions.
     *
     * @param busName Name of the bus
     * @param busDefinition Bus definition template
     * @return New non-virtual bus signal
     */
    public static BusSignal createNonVirtualBus(String busName, BusDefinition busDefinition) {
        return new BusSignal(busName, busDefinition, false);
    }

    // === Element Management ===

    /**
     * Adds an element (signal) to this bus.
     *
     * @param elementName Name of the element (must be unique within this bus)
     * @param signal Signal for this element
     * @throws IllegalArgumentException if elementName is null/empty or signal is null
     */
    public void addElement(String elementName, OutputSignal signal) {
        if (elementName == null || elementName.isEmpty()) {
            throw new IllegalArgumentException("Element name cannot be null or empty");
        }
        if (signal == null) {
            throw new IllegalArgumentException("Signal cannot be null");
        }

        BusElement element = new BusElement(elementName, signal);
        elements.put(elementName, element);

        log.debug("Added element '{}' to bus '{}' (type: {})",
                 elementName, busName, signal.getClass().getSimpleName());
    }

    /**
     * Adds a nested bus to this bus.
     *
     * @param elementName Name of the nested bus element
     * @param nestedBus Nested bus signal
     * @throws IllegalArgumentException if elementName is null/empty or nestedBus is null
     */
    public void addNestedBus(String elementName, BusSignal nestedBus) {
        if (elementName == null || elementName.isEmpty()) {
            throw new IllegalArgumentException("Element name cannot be null or empty");
        }
        if (nestedBus == null) {
            throw new IllegalArgumentException("Nested bus cannot be null");
        }

        BusElement element = new BusElement(elementName, nestedBus);
        elements.put(elementName, element);

        log.debug("Added nested bus '{}' to bus '{}'", elementName, busName);
    }

    /**
     * Gets an element by name.
     *
     * @param elementName Name of the element
     * @return BusElement if found, null otherwise
     */
    public BusElement getElement(String elementName) {
        return elements.get(elementName);
    }

    /**
     * Gets a signal from an element.
     *
     * @param elementName Name of the element
     * @return OutputSignal if found, null otherwise with warning logged
     */
    public OutputSignal getElementSignal(String elementName) {
        BusElement element = elements.get(elementName);
        if (element == null) {
            log.warn("Element '{}' not found in bus '{}'", elementName, busName);
            return null;
        }
        return element.getSignal();
    }

    /**
     * Gets nested element using hierarchical path (e.g., "motor.controller.setpoint").
     *
     * <p>The path is split by '.' delimiter and traversed through nested buses.
     * For example, "sensors.temperature" will first find the "sensors" element
     * (which must be a BusSignal), then find "temperature" within that bus.</p>
     *
     * @param path Hierarchical path using dot notation
     * @return OutputSignal at the end of the path, null if not found
     */
    public OutputSignal getNestedElement(String path) {
        if (path == null || path.isEmpty()) {
            return null;
        }

        String[] parts = path.split("\\.");
        if (parts.length == 0) {
            return null;
        }

        // First part is immediate element
        BusElement element = elements.get(parts[0]);
        if (element == null) {
            log.warn("Element '{}' not found in bus '{}'", parts[0], busName);
            return null;
        }

        // If only one part, return the signal
        if (parts.length == 1) {
            return element.getSignal();
        }

        // Multiple parts - navigate nested buses
        OutputSignal current = element.getSignal();
        for (int i = 1; i < parts.length; i++) {
            if (!(current instanceof BusSignal)) {
                log.warn("Element '{}' is not a bus, cannot navigate to '{}'",
                        parts[i-1], parts[i]);
                return null;
            }

            BusSignal currentBus = (BusSignal) current;
            current = currentBus.getElementSignal(parts[i]);

            if (current == null) {
                log.warn("Element '{}' not found", parts[i]);
                return null;
            }
        }

        return current;
    }

    /**
     * Checks if an element exists in this bus.
     *
     * @param elementName Name of the element
     * @return true if element exists, false otherwise
     */
    public boolean hasElement(String elementName) {
        return elements.containsKey(elementName);
    }

    /**
     * Gets the number of elements in this bus (not including nested elements).
     *
     * @return Number of direct elements
     */
    public int getElementCount() {
        return elements.size();
    }

    /**
     * Gets all element names in this bus.
     *
     * @return Set of element names
     */
    public Set<String> getElementNames() {
        return elements.keySet();
    }

    /**
     * Removes an element from this bus.
     *
     * @param elementName Name of element to remove
     */
    public void removeElement(String elementName) {
        elements.remove(elementName);
        log.debug("Removed element '{}' from bus '{}'", elementName, busName);
    }

    /**
     * Clears all elements from this bus.
     */
    public void clearElements() {
        elements.clear();
        log.debug("Cleared all elements from bus '{}'", busName);
    }

    // === Validation ===

    /**
     * Validates bus structure against definition.
     *
     * <p>For Phase 1, this always returns true for virtual buses without definition.
     * Full validation against BusDefinition will be implemented in Phase 2.</p>
     *
     * @return true if bus structure is valid
     */
    public boolean validate() {
        if (busDefinition == null) {
            // Virtual bus without definition - always valid
            return true;
        }

        // TODO Phase 2: Implement validation against BusDefinition
        // - Check all required elements present
        // - Validate element types and dimensions
        // - Check nested bus compatibility
        return true;
    }

    // === Utility Methods ===

    /**
     * Gets total width of all elements (for code generation).
     *
     * <p>This recursively calculates the total number of scalars in the bus,
     * including nested buses. Used for code generation and dimension propagation.</p>
     *
     * @return Total width (sum of all scalar elements)
     */
    public int getTotalWidth() {
        int totalWidth = 0;
        for (BusElement element : elements.values()) {
            OutputSignal signal = element.getSignal();
            if (signal instanceof BusSignal) {
                totalWidth += ((BusSignal) signal).getTotalWidth();
            } else {
                totalWidth += signal.getWidth() * signal.getHeight();
            }
        }
        return totalWidth;
    }

    /**
     * Converts bus to string representation for debugging.
     *
     * @return String representation showing bus name, virtual flag, and element count
     */
    @Override
    public String toString() {
        return String.format("BusSignal{name='%s', virtual=%b, elements=%d}",
                           busName, isVirtual, elements.size());
    }
}
