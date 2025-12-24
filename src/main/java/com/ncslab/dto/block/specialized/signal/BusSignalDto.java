package com.ncslab.dto.block.specialized.signal;

import com.ncslab.dto.mapper.validation.ValidationResult;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.*;

/**
 * DTO for BusSignal - represents a hierarchical bus signal structure.
 *
 * A bus signal is a composite signal that groups multiple signals together.
 * It can be either virtual (no memory allocation) or non-virtual (actual data structure).
 *
 * <p>Key Features:
 * <ul>
 *   <li>Hierarchical structure - buses can contain nested buses</li>
 *   <li>Virtual vs non-virtual bus support</li>
 *   <li>Ordered element collection using LinkedHashMap</li>
 *   <li>Comprehensive validation for structure integrity</li>
 * </ul>
 *
 * @author NCSLab Bus Architecture
 * @version 1.0
 * @since Bus Architecture Phase 1
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class BusSignalDto {

    /**
     * Bus name identifier
     * Default: "bus"
     */
    @Builder.Default
    private String busName = "bus";

    /**
     * Whether this is a virtual bus (true) or non-virtual bus (false)
     * Virtual buses have no memory allocation overhead
     * Default: true
     */
    @Builder.Default
    private Boolean isVirtual = true;

    /**
     * Bus definition name (optional, for non-virtual buses)
     * References a bus object definition in the model
     */
    private String busDefinitionName;

    /**
     * Map of element names to element DTOs
     * Uses LinkedHashMap to preserve element order
     */
    @Builder.Default
    private Map<String, BusElementDto> elements = new LinkedHashMap<>();

    /**
     * Bus description/comment
     * Optional documentation for the bus
     */
    private String description;

    // ===== PARAMETER ACCESS HELPERS =====

    /**
     * Check if this is a virtual bus.
     *
     * @return true if virtual, false if non-virtual
     */
    public boolean isVirtualBus() {
        return Boolean.TRUE.equals(isVirtual);
    }

    /**
     * Check if this is a non-virtual bus.
     *
     * @return true if non-virtual, false if virtual
     */
    public boolean isNonVirtualBus() {
        return Boolean.FALSE.equals(isVirtual);
    }

    /**
     * Get the number of elements in this bus.
     *
     * @return element count
     */
    public int getElementCount() {
        return elements != null ? elements.size() : 0;
    }

    /**
     * Get the set of element names in this bus.
     *
     * @return unmodifiable set of element names
     */
    public Set<String> getElementNames() {
        return elements != null ? elements.keySet() : Collections.emptySet();
    }

    /**
     * Check if this bus contains an element with the given name.
     *
     * @param elementName the element name to check
     * @return true if element exists
     */
    public boolean hasElement(String elementName) {
        return elements != null && elements.containsKey(elementName);
    }

    /**
     * Get an element by name.
     *
     * @param elementName the element name
     * @return the element DTO, or null if not found
     */
    public BusElementDto getElement(String elementName) {
        return elements != null ? elements.get(elementName) : null;
    }

    // ===== VALIDATION =====

    /**
     * Validate this bus signal structure.
     *
     * <p>Validation checks:
     * <ul>
     *   <li>Bus name is not null or empty</li>
     *   <li>All element names are valid</li>
     *   <li>All elements are not null and valid</li>
     *   <li>Non-virtual buses have definition names (warning)</li>
     * </ul>
     *
     * @return validation result with errors and warnings
     */
    public ValidationResult validate() {
        ValidationResult result = new ValidationResult();

        // Validate bus name
        if (busName == null || busName.trim().isEmpty()) {
            result.addError("busName", "Bus name cannot be null or empty");
        }

        // Validate elements
        if (elements != null) {
            for (Map.Entry<String, BusElementDto> entry : elements.entrySet()) {
                String elementName = entry.getKey();
                BusElementDto element = entry.getValue();

                if (elementName == null || elementName.trim().isEmpty()) {
                    result.addError("elements", "Bus element name cannot be null or empty");
                }

                if (element == null) {
                    result.addError("elements", "Bus element '" + elementName + "' is null");
                } else {
                    // Validate element
                    ValidationResult elementResult = element.validate();
                    if (!elementResult.isValid()) {
                        // Merge element validation errors
                        result.merge(elementResult);
                    }
                }
            }
        }

        // Non-virtual buses should have a definition
        if (Boolean.FALSE.equals(isVirtual)) {
            if (busDefinitionName == null || busDefinitionName.trim().isEmpty()) {
                result.addWarning("busDefinitionName",
                    "Non-virtual bus should have a bus definition name");
            }
        }

        return result;
    }

    // ===== UTILITY METHODS =====

    /**
     * Add an element to this bus.
     *
     * @param name the element name
     * @param element the element DTO
     */
    public void addElement(String name, BusElementDto element) {
        if (elements == null) {
            elements = new LinkedHashMap<>();
        }
        elements.put(name, element);
    }

    /**
     * Remove an element from this bus.
     *
     * @param name the element name to remove
     * @return the removed element, or null if not found
     */
    public BusElementDto removeElement(String name) {
        if (elements != null) {
            return elements.remove(name);
        }
        return null;
    }

    /**
     * Clear all elements from this bus.
     */
    public void clearElements() {
        if (elements != null) {
            elements.clear();
        }
    }

    /**
     * Get total number of signals in this bus (including nested buses).
     *
     * @return total signal count
     */
    public int getTotalSignalCount() {
        if (elements == null) {
            return 0;
        }

        int count = 0;
        for (BusElementDto element : elements.values()) {
            if (element.isNestedBusElement() && element.getNestedBus() != null) {
                count += element.getNestedBus().getTotalSignalCount();
            } else {
                count += element.getTotalElements();
            }
        }
        return count;
    }

    /**
     * Copy this DTO (deep copy).
     *
     * @return a deep copy of this bus signal DTO
     */
    public BusSignalDto copy() {
        BusSignalDto copy = BusSignalDto.builder()
            .busName(busName)
            .isVirtual(isVirtual)
            .busDefinitionName(busDefinitionName)
            .description(description)
            .build();

        // Deep copy elements
        if (elements != null) {
            Map<String, BusElementDto> elementsCopy = new LinkedHashMap<>();
            for (Map.Entry<String, BusElementDto> entry : elements.entrySet()) {
                elementsCopy.put(entry.getKey(), entry.getValue().copy());
            }
            copy.setElements(elementsCopy);
        }

        return copy;
    }

    @Override
    public String toString() {
        return String.format("BusSignalDto{name='%s', virtual=%b, elements=%d, totalSignals=%d}",
                           busName, isVirtual, getElementCount(), getTotalSignalCount());
    }
}
