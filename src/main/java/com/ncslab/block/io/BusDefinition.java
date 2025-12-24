package com.ncslab.block.io;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * Immutable bus definition (template) for creating bus signals.
 *
 * <p>A bus definition serves as a reusable template/schema that defines the structure of bus signals.
 * It specifies the element names, data types, dimensions, and nested bus hierarchies. Multiple
 * {@link BusCreator} and {@link BusSelector} blocks can share the same bus definition to ensure
 * consistent bus structure across the model.</p>
 *
 * <h2>Key Features</h2>
 * <ul>
 *   <li><b>Immutable Design:</b> Once created via builder pattern, the definition cannot be modified</li>
 *   <li><b>Hierarchical Structure:</b> Supports nested buses for complex data structures</li>
 *   <li><b>Validation:</b> Comprehensive validation prevents duplicate names and circular references</li>
 *   <li><b>Virtual/Non-Virtual:</b> Defines whether buses created from this definition are virtual</li>
 *   <li><b>Scope Management:</b> Global or local scope for registry management</li>
 * </ul>
 *
 * <h2>Usage Examples</h2>
 * <pre>{@code
 * // Simple sensor bus definition
 * BusDefinition sensorBus = BusDefinition.builder()
 *     .name("SensorBus")
 *     .description("Temperature and pressure sensors")
 *     .isVirtual(true)
 *     .element(BusElementDefinition.scalar("temperature", "double"))
 *     .element(BusElementDefinition.scalar("pressure", "double"))
 *     .build();
 *
 * // Nested bus definition
 * BusDefinition vehicleBus = BusDefinition.builder()
 *     .name("VehicleBus")
 *     .isVirtual(true)
 *     .element(BusElementDefinition.scalar("speed", "double"))
 *     .element(BusElementDefinition.vector("position", "double", 3))
 *     .element(BusElementDefinition.nestedBus("sensors", sensorBus))
 *     .build();
 *
 * // Non-virtual bus for hardware interface
 * BusDefinition canBus = BusDefinition.builder()
 *     .name("CANBus")
 *     .description("CAN bus message structure")
 *     .isVirtual(false)
 *     .scope("Global")
 *     .element(BusElementDefinition.scalar("id", "uint32"))
 *     .element(BusElementDefinition.vector("data", "uint8", 8))
 *     .build();
 * }</pre>
 *
 * <h2>Virtual vs. Non-Virtual Buses</h2>
 * <ul>
 *   <li><b>Virtual Buses (isVirtual=true):</b> Zero runtime overhead, signals stored separately,
 *       optimized away during code generation. Best for most applications.</li>
 *   <li><b>Non-Virtual Buses (isVirtual=false):</b> Generate C struct definitions, signals packed
 *       into struct (copy overhead). Required for hardware interfaces and external communication.</li>
 * </ul>
 *
 * <h2>Scope Management</h2>
 * <ul>
 *   <li><b>Global:</b> Bus definition available across the entire model, registered in global registry</li>
 *   <li><b>Local:</b> Bus definition scoped to specific subsystem or block</li>
 * </ul>
 *
 * @author NCSLab
 * @since Phase 2 - Bus Architecture Implementation
 */
@Slf4j
@Getter
@Builder
public class BusDefinition {

    /**
     * Definition name (e.g., "SensorBus", "MotorControlBus").
     * Must be unique within its scope and be a valid C identifier.
     */
    private final String name;

    /**
     * Optional description of this bus definition.
     */
    @Builder.Default
    private final String description = "";

    /**
     * Ordered list of element definitions.
     * Order is preserved for consistent code generation.
     * Use @Singular to enable incremental builder pattern.
     */
    @Singular
    private final List<BusElementDefinition> elements;

    /**
     * Scope of this definition: "Global" or "Local".
     * Global definitions are shared across the entire model.
     * Local definitions are scoped to specific subsystems.
     */
    @Builder.Default
    private final String scope = "Global";

    /**
     * Whether buses created from this definition are virtual.
     * Virtual buses have zero overhead and are optimized during code generation.
     * Non-virtual buses generate C struct definitions.
     */
    @Builder.Default
    private final boolean isVirtual = true;

    // Internal cached values (computed during validation)
    private transient Map<String, BusElementDefinition> elementMap;
    private transient Map<String, Integer> elementIndexMap;
    private transient Integer totalWidthCache = null;

    // === Element Access Methods ===

    /**
     * Adds an element definition to the builder.
     * This is used internally by the builder pattern.
     *
     * <p><b>Note:</b> This method should not be called directly.
     * Use the builder pattern instead.</p>
     *
     * @param element Element definition to add
     * @return Builder instance for chaining
     */
    public static class BusDefinitionBuilder {
        // Lombok generates this automatically with @Singular
    }

    /**
     * Gets an element definition by name.
     *
     * @param elementName Name of the element
     * @return Element definition if found, null otherwise
     */
    public BusElementDefinition getElement(String elementName) {
        ensureElementMapInitialized();
        return elementMap.get(elementName);
    }

    /**
     * Gets the index of an element within the bus.
     * Used for code generation and offset calculations.
     *
     * @param elementName Name of the element
     * @return Element index (0-based), or -1 if not found
     */
    public int getElementIndex(String elementName) {
        ensureElementMapInitialized();
        Integer index = elementIndexMap.get(elementName);
        return index != null ? index : -1;
    }

    /**
     * Checks if an element exists in this bus definition.
     *
     * @param elementName Name of the element
     * @return true if element exists, false otherwise
     */
    public boolean hasElement(String elementName) {
        ensureElementMapInitialized();
        return elementMap.containsKey(elementName);
    }

    /**
     * Gets all element names in this bus definition.
     * Names are returned in the order they were added.
     *
     * @return Ordered set of element names
     */
    public Set<String> getElementNames() {
        ensureElementMapInitialized();
        // LinkedHashSet preserves insertion order
        return new LinkedHashSet<>(elementIndexMap.keySet());
    }

    /**
     * Gets the number of elements in this bus definition (non-recursive).
     *
     * @return Number of direct elements
     */
    public int getElementCount() {
        return elements != null ? elements.size() : 0;
    }

    // === Dimension Methods ===

    /**
     * Calculates the total width of all elements in this bus.
     * This is the sum of all scalar signals, including nested buses.
     *
     * <p>The total width represents the number of individual scalar signals
     * that make up this bus structure. For example:</p>
     * <ul>
     *   <li>Scalar element contributes 1</li>
     *   <li>Vector[4] element contributes 4</li>
     *   <li>Matrix[3x3] element contributes 9</li>
     *   <li>Nested bus contributes its total width recursively</li>
     * </ul>
     *
     * @return Total width (number of scalar signals)
     */
    public int getTotalWidth() {
        // Return cached value if available
        if (totalWidthCache != null) {
            return totalWidthCache;
        }

        // Calculate total width
        if (elements == null || elements.isEmpty()) {
            totalWidthCache = 0;
            return 0;
        }

        int width = 0;
        for (BusElementDefinition element : elements) {
            width += element.getTotalWidth();
        }

        totalWidthCache = width;
        return totalWidthCache;
    }

    // === Validation ===

    /**
     * Validates this bus definition.
     *
     * <p>Validation checks include:</p>
     * <ul>
     *   <li>Name is not null/empty and is a valid C identifier</li>
     *   <li>At least one element is defined</li>
     *   <li>No duplicate element names</li>
     *   <li>All elements are valid (recursive validation)</li>
     *   <li>No circular nested bus references</li>
     *   <li>Valid scope value</li>
     * </ul>
     *
     * @throws IllegalStateException if validation fails
     */
    public void validate() {
        // Validate name
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalStateException("Bus definition name cannot be null or empty");
        }

        // Validate name format (alphanumeric and underscore only, must start with letter)
        if (!name.matches("^[a-zA-Z][a-zA-Z0-9_]*$")) {
            throw new IllegalStateException(
                String.format("Invalid bus definition name '%s': must start with letter and contain only alphanumeric characters and underscore", name)
            );
        }

        // Validate at least one element
        if (elements == null || elements.isEmpty()) {
            throw new IllegalStateException(
                String.format("Bus definition '%s' must have at least one element", name)
            );
        }

        // Validate scope
        if (scope == null || (!scope.equals("Global") && !scope.equals("Local"))) {
            throw new IllegalStateException(
                String.format("Bus definition '%s' has invalid scope '%s': must be 'Global' or 'Local'", name, scope)
            );
        }

        // Check for duplicate element names and validate each element
        Set<String> nameSet = new HashSet<>();
        for (BusElementDefinition element : elements) {
            // Validate element structure
            element.validate();

            // Check for duplicate names
            String elementName = element.getName();
            if (nameSet.contains(elementName)) {
                throw new IllegalStateException(
                    String.format("Bus definition '%s' has duplicate element name '%s'", name, elementName)
                );
            }
            nameSet.add(elementName);
        }

        // Check for circular references
        validateNoCircularReferences(new HashSet<>());

        // Initialize element maps for fast lookup
        ensureElementMapInitialized();

        log.debug("Bus definition '{}' validated successfully: {} elements, total width = {}, virtual = {}",
            name, elements.size(), getTotalWidth(), isVirtual);
    }

    /**
     * Validates that there are no circular nested bus references.
     *
     * @param visitedDefinitions Set of bus definition names already visited
     * @throws IllegalStateException if circular reference detected
     */
    private void validateNoCircularReferences(Set<String> visitedDefinitions) {
        // Check if we've already visited this definition
        if (visitedDefinitions.contains(name)) {
            throw new IllegalStateException(
                String.format("Circular nested bus reference detected involving bus definition '%s'", name)
            );
        }

        // Add this definition to visited set
        visitedDefinitions.add(name);

        // Recursively check nested buses
        if (elements != null) {
            for (BusElementDefinition element : elements) {
                if (element.isNestedBus() && element.getNestedBusDefinition() != null) {
                    element.getNestedBusDefinition().validateNoCircularReferences(new HashSet<>(visitedDefinitions));
                }
            }
        }

        // Remove from visited set (backtrack for other paths)
        visitedDefinitions.remove(name);
    }

    // === Internal Helper Methods ===

    /**
     * Initializes element maps for fast lookup.
     * This is called lazily during first access or after validation.
     */
    private void ensureElementMapInitialized() {
        if (elementMap == null || elementIndexMap == null) {
            elementMap = new LinkedHashMap<>();
            elementIndexMap = new LinkedHashMap<>();

            if (elements != null) {
                for (int i = 0; i < elements.size(); i++) {
                    BusElementDefinition element = elements.get(i);
                    String elementName = element.getName();
                    elementMap.put(elementName, element);
                    elementIndexMap.put(elementName, i);
                }
            }
        }
    }

    // === Utility Methods ===

    /**
     * Checks if this bus definition is compatible with another.
     * Two bus definitions are compatible if they have the same structure
     * (element names, types, and dimensions).
     *
     * @param other Other bus definition
     * @return true if compatible, false otherwise
     */
    public boolean isCompatibleWith(BusDefinition other) {
        if (other == null) {
            return false;
        }

        // Check element count
        if (getElementCount() != other.getElementCount()) {
            return false;
        }

        // Check each element
        if (elements != null) {
            for (int i = 0; i < elements.size(); i++) {
                BusElementDefinition thisElement = elements.get(i);
                BusElementDefinition otherElement = other.getElements().get(i);

                // Element names must match
                if (!thisElement.getName().equals(otherElement.getName())) {
                    return false;
                }

                // Element types must match
                if (thisElement.isNestedBus() != otherElement.isNestedBus()) {
                    return false;
                }

                if (thisElement.isNestedBus()) {
                    // Recursively check nested bus compatibility
                    if (!thisElement.getNestedBusDefinition().isCompatibleWith(
                            otherElement.getNestedBusDefinition())) {
                        return false;
                    }
                } else {
                    // Check primitive element compatibility
                    if (!thisElement.getDataTypeName().equals(otherElement.getDataTypeName()) ||
                        thisElement.getWidth() != otherElement.getWidth() ||
                        thisElement.getHeight() != otherElement.getHeight()) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    /**
     * Gets a hierarchical string representation of this bus definition.
     *
     * @return Multi-line string showing bus structure
     */
    public String toStructureString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("BusDefinition '%s' (%s, %s, %d elements, total width: %d):\n",
            name, isVirtual ? "virtual" : "non-virtual", scope, getElementCount(), getTotalWidth()));

        if (description != null && !description.isEmpty()) {
            sb.append("  Description: ").append(description).append("\n");
        }

        if (elements != null) {
            for (int i = 0; i < elements.size(); i++) {
                BusElementDefinition element = elements.get(i);
                sb.append(String.format("  [%d] %s: %s\n", i, element.getName(), element.getTypeString()));

                // Show nested bus structure
                if (element.isNestedBus() && element.getNestedBusDefinition() != null) {
                    String nestedStructure = element.getNestedBusDefinition().toStructureString();
                    for (String line : nestedStructure.split("\n")) {
                        sb.append("    ").append(line).append("\n");
                    }
                }
            }
        }

        return sb.toString();
    }

    // === DTO Conversion Methods ===

    /**
     * Convert this entity to a DTO.
     *
     * @return BusDefinitionDto representation of this bus definition
     */
    public com.ncslab.dto.block.specialized.signal.BusDefinitionDto toDto() {
        // Convert elements to DTOs
        java.util.List<com.ncslab.dto.block.specialized.signal.BusElementDefinitionDto> elementDtos = new java.util.ArrayList<>();
        if (elements != null) {
            for (BusElementDefinition element : elements) {
                elementDtos.add(element.toDto());
            }
        }

        return com.ncslab.dto.block.specialized.signal.BusDefinitionDto.builder()
            .name(name)
            .description(description)
            .elements(elementDtos)
            .scope(scope)
            .isVirtual(isVirtual)
            .build();
    }

    /**
     * Create an entity from a DTO.
     *
     * @param dto the BusDefinitionDto to convert
     * @return BusDefinition entity
     * @throws IllegalArgumentException if dto is null
     */
    public static BusDefinition fromDto(com.ncslab.dto.block.specialized.signal.BusDefinitionDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Cannot create BusDefinition from null DTO");
        }

        BusDefinitionBuilder builder = BusDefinition.builder()
            .name(dto.getName())
            .description(dto.getDescription())
            .scope(dto.getScope())
            .isVirtual(dto.isVirtualBus());

        // Convert element DTOs to entities
        if (dto.getElements() != null) {
            for (com.ncslab.dto.block.specialized.signal.BusElementDefinitionDto elementDto : dto.getElements()) {
                builder.element(BusElementDefinition.fromDto(elementDto));
            }
        }

        return builder.build();
    }

    // === Equals and HashCode ===

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        BusDefinition other = (BusDefinition) obj;
        return isVirtual == other.isVirtual &&
               Objects.equals(name, other.name) &&
               Objects.equals(scope, other.scope) &&
               Objects.equals(elements, other.elements);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, scope, elements, isVirtual);
    }

    @Override
    public String toString() {
        return String.format("BusDefinition{name='%s', scope='%s', virtual=%b, elements=%d, totalWidth=%d}",
            name, scope, isVirtual, getElementCount(), getTotalWidth());
    }
}
