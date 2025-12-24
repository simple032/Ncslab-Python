package com.ncslab.block.io;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Represents a single element within a bus signal.
 * An element can be either:
 * 1. A regular signal (OutputSignal)
 * 2. A nested bus (BusSignal)
 *
 * <p>This class provides a wrapper around OutputSignal to support hierarchical
 * bus structures where buses can contain other buses as elements.</p>
 *
 * <p><b>Design Pattern:</b> Composite Pattern - allows uniform treatment of
 * individual signals and composite bus structures.</p>
 *
 * <p><b>Example Usage:</b></p>
 * <pre>{@code
 * // Create a simple signal element
 * OutputSignal speedSignal = new OutputSignal(block, 0, 1, "speed");
 * BusElement speedElement = new BusElement("speed", speedSignal);
 *
 * // Create a nested bus element
 * BusSignal sensorBus = new BusSignal(block, 1, 2, "sensors", busDefinition, false);
 * BusElement sensorElement = new BusElement("sensors", sensorBus);
 *
 * // Check if element is a nested bus
 * if (sensorElement.isNestedBus()) {
 *     BusSignal nestedBus = sensorElement.getNestedBus();
 *     // Process nested bus...
 * }
 * }</pre>
 *
 * @author NCSLab Bus Architecture
 * @version 1.0
 * @since Bus Architecture Phase 1
 */
@Slf4j
@Getter
@Setter
public class BusElement {

    /** Element name within the parent bus */
    private String name;

    /** The signal (can be OutputSignal or BusSignal for nested buses) */
    private OutputSignal signal;

    /** Whether this element is a nested bus (true if signal instanceof BusSignal) */
    private boolean isNestedBus;

    /** Element index within parent bus (for code generation and ordering) */
    private int index;

    // === Constructors ===

    /**
     * Create a bus element with a regular signal.
     *
     * <p>The element type (nested bus or regular signal) is automatically
     * determined based on whether the signal is an instance of BusSignal.</p>
     *
     * @param name Element name (must not be null or empty)
     * @param signal The signal associated with this element (can be OutputSignal or BusSignal)
     * @throws IllegalArgumentException if name is null or empty
     */
    public BusElement(String name, OutputSignal signal) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Bus element name cannot be null or empty");
        }

        this.name = name;
        this.signal = signal;
        this.isNestedBus = (signal instanceof BusSignal);
        this.index = -1; // Not set yet

        log.debug("Created bus element '{}' (nested bus: {})", name, isNestedBus);
    }

    /**
     * Create a bus element with a specified index.
     *
     * <p>This constructor is useful when creating elements with a known
     * position within the parent bus structure.</p>
     *
     * @param name Element name (must not be null or empty)
     * @param signal The signal associated with this element
     * @param index Element index within parent bus (0-based)
     * @throws IllegalArgumentException if name is null or empty, or index is negative
     */
    public BusElement(String name, OutputSignal signal, int index) {
        this(name, signal);

        if (index < 0) {
            throw new IllegalArgumentException("Bus element index cannot be negative: " + index);
        }

        this.index = index;
    }

    // === Utility Methods ===

    /**
     * Get the nested bus (if this element is a bus).
     *
     * <p>This method provides type-safe access to the BusSignal when the element
     * represents a nested bus structure.</p>
     *
     * @return The BusSignal if this element is a nested bus, null otherwise
     */
    public BusSignal getNestedBus() {
        if (!isNestedBus) {
            log.warn("Element '{}' is not a nested bus", name);
            return null;
        }
        return (BusSignal) signal;
    }

    /**
     * Get total signal width (number of scalar elements).
     *
     * <p>For regular signals, this is width * height. For nested buses,
     * this recursively calculates the total width of all contained elements.</p>
     *
     * @return Total width of the signal, or 0 if signal is null
     */
    public int getWidth() {
        if (signal == null) {
            log.warn("Element '{}' has null signal", name);
            return 0;
        }

        if (isNestedBus) {
            BusSignal busSignal = (BusSignal) signal;
            return busSignal.getTotalWidth();
        } else {
            return signal.getWidth() * signal.getHeight();
        }
    }

    /**
     * Get signal data type name.
     *
     * <p>Returns the string representation of the data type. For nested buses,
     * this returns "BUS". For regular signals, returns the actual data type.</p>
     *
     * @return Data type name, or "unknown" if signal or data is null
     */
    public String getDataTypeName() {
        if (signal == null) {
            log.warn("Element '{}' has null signal", name);
            return "unknown";
        }

        if (isNestedBus) {
            return "BUS";
        }

        if (signal.getData() == null) {
            log.warn("Element '{}' has null data", name);
            return "unknown";
        }

        return signal.getData().getDataType().name();
    }

    /**
     * Check if element has a valid signal.
     *
     * <p>An element is considered to have a valid signal if the signal
     * reference is not null.</p>
     *
     * @return true if signal is not null, false otherwise
     */
    public boolean hasValidSignal() {
        return signal != null;
    }

    /**
     * Get full hierarchical element path.
     *
     * <p>Constructs the full path from the parent path and element name.
     * Used for hierarchical bus structures to identify elements uniquely.</p>
     *
     * <p><b>Examples:</b></p>
     * <ul>
     * <li>parentPath="" or null, name="speed" → "speed"</li>
     * <li>parentPath="vehicle", name="speed" → "vehicle.speed"</li>
     * <li>parentPath="vehicle.sensors", name="temp" → "vehicle.sensors.temp"</li>
     * </ul>
     *
     * @param parentPath Parent bus path (can be null or empty for root elements)
     * @return Full hierarchical path with dot notation
     */
    public String getPath(String parentPath) {
        if (parentPath == null || parentPath.isEmpty()) {
            return name;
        }
        return parentPath + "." + name;
    }

    /**
     * Clone this element.
     *
     * <p><b>Note:</b> This performs a shallow copy. The signal reference
     * is shared with the original element.</p>
     *
     * @return A new BusElement with the same properties
     */
    public BusElement copy() {
        return new BusElement(name, signal, index);
    }

    /**
     * String representation for debugging.
     *
     * <p>Provides a concise summary of the element's key properties.</p>
     *
     * @return String representation including name, nested bus status, and width
     */
    @Override
    public String toString() {
        return String.format("BusElement{name='%s', nestedBus=%b, width=%d, index=%d}",
                           name, isNestedBus, getWidth(), index);
    }

    /**
     * Equals based on name and signal.
     *
     * <p>Two bus elements are considered equal if they have the same name
     * and signal reference. This allows proper comparison in collections.</p>
     *
     * @param obj Object to compare with
     * @return true if objects are equal based on name and signal
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        BusElement other = (BusElement) obj;
        return name != null && name.equals(other.name) &&
               signal != null && signal.equals(other.signal);
    }

    /**
     * Hash code based on name.
     *
     * <p>Provides a hash code based on the element name for use in hash-based
     * collections. This allows elements with the same name to be treated as
     * potentially equal candidates.</p>
     *
     * @return Hash code based on element name
     */
    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }
}
