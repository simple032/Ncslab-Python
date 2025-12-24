package com.ncslab.dto.block.specialized.signal;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.ncslab.dto.mapper.validation.ValidationResult;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;
import java.util.*;

/**
 * DTO for BusDefinition - defines the schema template for bus signals.
 *
 * <p>A bus definition serves as a reusable template that specifies the structure
 * of a bus signal, including all element names, types, and dimensions. Multiple
 * bus instances can share the same definition.</p>
 *
 * <p>Bus definitions can be scoped as either Global (model-wide) or Local (subsystem-level),
 * and can be either Virtual (no memory overhead) or Non-virtual (actual data structure).</p>
 *
 * <p>Key Features:
 * <ul>
 *   <li>Reusable bus schema templates</li>
 *   <li>Hierarchical structure with nested bus support</li>
 *   <li>Global and local scope management</li>
 *   <li>Virtual and non-virtual bus support</li>
 *   <li>Comprehensive validation including circular reference detection</li>
 * </ul>
 *
 * <p>Example Usage:
 * <pre>{@code
 * // Create a simple bus definition
 * BusDefinitionDto sensorBus = BusDefinitionDto.builder()
 *     .name("SensorData")
 *     .description("Sensor readings bus")
 *     .scope("Global")
 *     .isVirtual(true)
 *     .build();
 *
 * // Add elements to the definition
 * sensorBus.addElement(BusElementDefinitionDto.builder()
 *     .name("temperature")
 *     .dataTypeName("double")
 *     .width(1)
 *     .height(1)
 *     .build());
 *
 * sensorBus.addElement(BusElementDefinitionDto.builder()
 *     .name("pressure")
 *     .dataTypeName("double")
 *     .width(1)
 *     .height(1)
 *     .build());
 *
 * // Validate the definition
 * ValidationResult result = sensorBus.validate();
 * if (!result.isValid()) {
 *     // Handle validation errors
 * }
 * }</pre>
 *
 * @author NCSLab Bus Architecture
 * @version 1.0
 * @since Bus Architecture Phase 2
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@JsonTypeName("BusDefinition")
public class BusDefinitionDto implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Bus definition name identifier.
     * Must be unique within its scope (global or local).
     * Default: "Bus"
     */
    @Builder.Default
    private String name = "Bus";

    /**
     * Bus definition description/comment.
     * Optional documentation for the bus definition.
     */
    private String description;

    /**
     * Ordered list of element definitions in this bus.
     * Uses ArrayList to preserve element order.
     */
    @Builder.Default
    private List<BusElementDefinitionDto> elements = new ArrayList<>();

    /**
     * Scope of this bus definition.
     * Values: "Global" (model-wide) or "Local" (subsystem-level)
     * Default: "Local"
     */
    @Builder.Default
    private String scope = "Local";

    /**
     * Whether this is a virtual bus definition.
     * Virtual buses have no memory allocation overhead.
     * Default: true
     */
    @Builder.Default
    private Boolean isVirtual = true;

    // ===== PARAMETER ACCESS HELPERS =====

    /**
     * Check if this is a global bus definition.
     *
     * @return true if scope is "Global", false otherwise
     */
    public boolean isGlobal() {
        return "Global".equalsIgnoreCase(scope);
    }

    /**
     * Check if this is a local bus definition.
     *
     * @return true if scope is "Local", false otherwise
     */
    public boolean isLocal() {
        return "Local".equalsIgnoreCase(scope);
    }

    /**
     * Check if this is a virtual bus definition.
     *
     * @return true if virtual, false if non-virtual
     */
    public boolean isVirtualBus() {
        return Boolean.TRUE.equals(isVirtual);
    }

    /**
     * Check if this is a non-virtual bus definition.
     *
     * @return true if non-virtual, false if virtual
     */
    public boolean isNonVirtualBus() {
        return Boolean.FALSE.equals(isVirtual);
    }

    /**
     * Get the number of elements in this bus definition.
     *
     * @return element count
     */
    public int getElementCount() {
        return elements != null ? elements.size() : 0;
    }

    /**
     * Get the set of element names in this bus definition.
     *
     * @return unmodifiable set of element names
     */
    public Set<String> getElementNames() {
        if (elements == null || elements.isEmpty()) {
            return Collections.emptySet();
        }

        Set<String> names = new LinkedHashSet<>();
        for (BusElementDefinitionDto element : elements) {
            if (element != null && element.getName() != null) {
                names.add(element.getName());
            }
        }
        return Collections.unmodifiableSet(names);
    }

    /**
     * Check if this bus definition contains an element with the given name.
     *
     * @param elementName the element name to check
     * @return true if element exists
     */
    public boolean hasElement(String elementName) {
        if (elements == null || elementName == null) {
            return false;
        }

        for (BusElementDefinitionDto element : elements) {
            if (element != null && elementName.equals(element.getName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get an element by name.
     *
     * @param elementName the element name
     * @return the element definition DTO, or null if not found
     */
    public BusElementDefinitionDto getElement(String elementName) {
        if (elements == null || elementName == null) {
            return null;
        }

        for (BusElementDefinitionDto element : elements) {
            if (element != null && elementName.equals(element.getName())) {
                return element;
            }
        }
        return null;
    }

    /**
     * Get element at specified index.
     *
     * @param index the element index (0-based)
     * @return the element definition DTO at the index
     * @throws IndexOutOfBoundsException if index is out of bounds
     */
    public BusElementDefinitionDto getElementAt(int index) {
        if (elements == null) {
            throw new IndexOutOfBoundsException("No elements in bus definition");
        }
        return elements.get(index);
    }

    // ===== VALIDATION =====

    /**
     * Validate this bus definition.
     *
     * <p>Validation checks:
     * <ul>
     *   <li>Bus name is not null or empty</li>
     *   <li>Bus name contains only valid characters</li>
     *   <li>All element names are unique (no duplicates)</li>
     *   <li>All elements are not null and valid</li>
     *   <li>No circular references in nested bus structures</li>
     *   <li>Scope is valid ("Global" or "Local")</li>
     *   <li>At least one element is defined (warning)</li>
     * </ul>
     *
     * @return validation result with errors and warnings
     */
    public ValidationResult validate() {
        ValidationResult result = new ValidationResult();

        // Validate bus name
        if (name == null || name.trim().isEmpty()) {
            result.addError("name", "Bus definition name cannot be null or empty");
        } else if (!name.matches("^[a-zA-Z_][a-zA-Z0-9_]*$")) {
            result.addError("name",
                "Bus definition name '" + name + "' must start with letter or underscore " +
                "and contain only alphanumeric characters and underscores");
        }

        // Validate scope
        if (scope != null && !scope.equalsIgnoreCase("Global") && !scope.equalsIgnoreCase("Local")) {
            result.addError("scope",
                "Bus definition scope must be 'Global' or 'Local', got '" + scope + "'");
        }

        // Validate elements
        if (elements == null || elements.isEmpty()) {
            result.addWarning("elements", "Bus definition '" + name + "' has no elements defined");
        } else {
            // Track element names to detect duplicates
            Set<String> elementNames = new HashSet<>();

            for (int i = 0; i < elements.size(); i++) {
                BusElementDefinitionDto element = elements.get(i);

                if (element == null) {
                    result.addError("elements", "Bus element at index " + i + " is null");
                    continue;
                }

                String elementName = element.getName();

                // Check for duplicate names
                if (elementName != null) {
                    if (elementNames.contains(elementName)) {
                        result.addError("elements",
                            "Duplicate element name '" + elementName + "' in bus definition '" + name + "'");
                    }
                    elementNames.add(elementName);
                }

                // Validate element
                ValidationResult elementResult = element.validate();
                if (!elementResult.isValid()) {
                    // Merge element validation errors
                    result.merge(elementResult);
                }
            }
        }

        // Validate for circular references
        ValidationResult circularResult = validateCircularReferences(new HashSet<>());
        result.merge(circularResult);

        return result;
    }

    /**
     * Validate for circular references in nested bus structures.
     *
     * <p>This method recursively checks all nested bus definitions to detect cycles
     * where a bus definition references itself through nested bus elements.</p>
     *
     * @param visitedDefinitions Set of bus definition names already visited in the hierarchy
     * @return validation result with circular reference errors
     */
    public ValidationResult validateCircularReferences(Set<String> visitedDefinitions) {
        ValidationResult result = new ValidationResult();

        if (elements == null) {
            return result;
        }

        // Add current definition to visited set
        Set<String> newVisited = new HashSet<>(visitedDefinitions);
        newVisited.add(name);

        // Check each element for circular references
        for (BusElementDefinitionDto element : elements) {
            if (element != null) {
                ValidationResult elementResult = element.validateCircularReferences(newVisited);
                result.merge(elementResult);
            }
        }

        return result;
    }

    // ===== UTILITY METHODS =====

    /**
     * Add an element to this bus definition.
     *
     * @param element the element definition to add
     * @throws IllegalArgumentException if element is null or has duplicate name
     */
    public void addElement(BusElementDefinitionDto element) {
        if (element == null) {
            throw new IllegalArgumentException("Cannot add null element to bus definition");
        }

        if (elements == null) {
            elements = new ArrayList<>();
        }

        // Check for duplicate name
        String elementName = element.getName();
        if (elementName != null && hasElement(elementName)) {
            throw new IllegalArgumentException(
                "Element with name '" + elementName + "' already exists in bus definition '" + name + "'");
        }

        elements.add(element);
    }

    /**
     * Remove an element from this bus definition by name.
     *
     * @param elementName the element name to remove
     * @return the removed element definition, or null if not found
     */
    public BusElementDefinitionDto removeElement(String elementName) {
        if (elements == null || elementName == null) {
            return null;
        }

        for (int i = 0; i < elements.size(); i++) {
            BusElementDefinitionDto element = elements.get(i);
            if (element != null && elementName.equals(element.getName())) {
                return elements.remove(i);
            }
        }
        return null;
    }

    /**
     * Remove an element from this bus definition by index.
     *
     * @param index the element index to remove (0-based)
     * @return the removed element definition
     * @throws IndexOutOfBoundsException if index is out of bounds
     */
    public BusElementDefinitionDto removeElementAt(int index) {
        if (elements == null) {
            throw new IndexOutOfBoundsException("No elements in bus definition");
        }
        return elements.remove(index);
    }

    /**
     * Clear all elements from this bus definition.
     */
    public void clearElements() {
        if (elements != null) {
            elements.clear();
        }
    }

    /**
     * Get total number of scalar elements in this bus (including nested buses).
     * This recursively counts all leaf elements.
     *
     * @return total scalar element count
     */
    public int getTotalScalarCount() {
        if (elements == null) {
            return 0;
        }

        int count = 0;
        for (BusElementDefinitionDto element : elements) {
            if (element == null) {
                continue;
            }

            if (element.isNestedBusElement() && element.getNestedBusDefinition() != null) {
                count += element.getNestedBusDefinition().getTotalScalarCount();
            } else {
                count += element.getTotalElements();
            }
        }
        return count;
    }

    /**
     * Get the maximum nesting depth of this bus definition.
     * A flat bus (no nested buses) has depth 1.
     *
     * @return maximum nesting depth
     */
    public int getMaxNestingDepth() {
        if (elements == null || elements.isEmpty()) {
            return 1;
        }

        int maxDepth = 1;
        for (BusElementDefinitionDto element : elements) {
            if (element != null && element.isNestedBusElement() &&
                element.getNestedBusDefinition() != null) {
                int nestedDepth = 1 + element.getNestedBusDefinition().getMaxNestingDepth();
                maxDepth = Math.max(maxDepth, nestedDepth);
            }
        }
        return maxDepth;
    }

    /**
     * Copy this DTO (deep copy).
     *
     * @return a deep copy of this bus definition DTO
     */
    public BusDefinitionDto copy() {
        BusDefinitionDto copy = BusDefinitionDto.builder()
            .name(name)
            .description(description)
            .scope(scope)
            .isVirtual(isVirtual)
            .build();

        // Deep copy elements
        if (elements != null) {
            List<BusElementDefinitionDto> elementsCopy = new ArrayList<>();
            for (BusElementDefinitionDto element : elements) {
                if (element != null) {
                    elementsCopy.add(element.copy());
                }
            }
            copy.setElements(elementsCopy);
        }

        return copy;
    }

    @Override
    public String toString() {
        return String.format(
            "BusDefinitionDto{name='%s', scope=%s, virtual=%b, elements=%d, scalarCount=%d, depth=%d}",
            name, scope, isVirtual, getElementCount(), getTotalScalarCount(), getMaxNestingDepth());
    }
}
