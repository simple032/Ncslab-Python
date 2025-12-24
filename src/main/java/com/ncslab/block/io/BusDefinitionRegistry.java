package com.ncslab.block.io;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Singleton registry for managing bus definitions in a thread-safe manner.
 *
 * <p>The BusDefinitionRegistry provides centralized storage and lookup for bus definitions
 * across the entire system. It supports both global (model-wide) and local (subsystem-level)
 * bus definitions with proper scoping and name conflict resolution.</p>
 *
 * <p>Key Features:
 * <ul>
 *   <li>Singleton pattern for system-wide access</li>
 *   <li>Thread-safe operations using ConcurrentHashMap</li>
 *   <li>Separate storage for global and local definitions</li>
 *   <li>Hierarchical lookup (local first, then global)</li>
 *   <li>Duplicate name prevention in global scope</li>
 *   <li>Clear/reset capability for testing</li>
 * </ul>
 *
 * <p>Usage Pattern:
 * <pre>{@code
 * // Get registry instance
 * BusDefinitionRegistry registry = BusDefinitionRegistry.getInstance();
 *
 * // Register a global bus definition
 * BusDefinition sensorBus = new BusDefinition("SensorData");
 * registry.registerGlobal(sensorBus);
 *
 * // Register a local bus definition (subsystem-specific)
 * BusDefinition controlBus = new BusDefinition("ControlData");
 * registry.registerLocal(controlBus);
 *
 * // Lookup bus definition (checks local first, then global)
 * BusDefinition found = registry.lookup("SensorData");
 *
 * // Lookup only global definitions
 * BusDefinition globalBus = registry.lookupGlobal("SensorData");
 *
 * // Clear all definitions (useful for testing)
 * registry.clear();
 * }</pre>
 *
 * <p>Thread Safety:
 * <br>All public methods are thread-safe and can be called from multiple threads concurrently.
 * The registry uses ConcurrentHashMap internally to provide lock-free reads and fine-grained
 * locking for writes.</p>
 *
 * <p>Scope Resolution:
 * <br>When looking up a bus definition, the registry first checks the local scope, then falls
 * back to the global scope. This allows subsystems to override global definitions with
 * subsystem-specific versions while maintaining a default global definition.</p>
 *
 * @author NCSLab Bus Architecture
 * @version 1.0
 * @since Bus Architecture Phase 2
 */
@Slf4j
public class BusDefinitionRegistry {

    /**
     * Singleton instance of the registry.
     */
    private static volatile BusDefinitionRegistry instance;

    /**
     * Storage for global bus definitions (model-wide scope).
     * Key: bus definition name
     * Value: BusDefinition object
     */
    private final Map<String, BusDefinition> globalDefinitions;

    /**
     * Storage for local bus definitions (subsystem-level scope).
     * Key: bus definition name
     * Value: BusDefinition object
     */
    private final Map<String, BusDefinition> localDefinitions;

    /**
     * Private constructor to enforce singleton pattern.
     * Initializes the internal storage maps as thread-safe ConcurrentHashMaps.
     */
    private BusDefinitionRegistry() {
        this.globalDefinitions = new ConcurrentHashMap<>();
        this.localDefinitions = new ConcurrentHashMap<>();
        log.debug("BusDefinitionRegistry initialized");
    }

    /**
     * Get the singleton instance of the registry.
     * Uses double-checked locking for thread-safe lazy initialization.
     *
     * @return the singleton BusDefinitionRegistry instance
     */
    public static BusDefinitionRegistry getInstance() {
        if (instance == null) {
            synchronized (BusDefinitionRegistry.class) {
                if (instance == null) {
                    instance = new BusDefinitionRegistry();
                    log.info("BusDefinitionRegistry singleton instance created");
                }
            }
        }
        return instance;
    }

    // ===== REGISTRATION METHODS =====

    /**
     * Register a bus definition in the global scope.
     *
     * <p>Global definitions are accessible throughout the entire model and can be
     * referenced by any block. If a definition with the same name already exists
     * in the global scope, it will be replaced and a warning will be logged.</p>
     *
     * @param definition the bus definition to register
     * @throws IllegalArgumentException if definition is null or has no name
     */
    public void registerGlobal(BusDefinition definition) {
        if (definition == null) {
            throw new IllegalArgumentException("Cannot register null bus definition");
        }

        String name = definition.getName();
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Bus definition must have a non-empty name");
        }

        BusDefinition existing = globalDefinitions.put(name, definition);
        if (existing != null) {
            log.warn("Replaced existing global bus definition: {}", name);
        } else {
            log.info("Registered global bus definition: {}", name);
        }
    }

    /**
     * Register a bus definition in the local scope.
     *
     * <p>Local definitions are typically used for subsystem-specific bus structures
     * that should not be accessible globally. If a definition with the same name
     * already exists in the local scope, it will be replaced and a warning will be logged.</p>
     *
     * <p>Note: Local definitions take precedence over global definitions during lookup,
     * allowing subsystems to override global definitions with local versions.</p>
     *
     * @param definition the bus definition to register
     * @throws IllegalArgumentException if definition is null or has no name
     */
    public void registerLocal(BusDefinition definition) {
        if (definition == null) {
            throw new IllegalArgumentException("Cannot register null bus definition");
        }

        String name = definition.getName();
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Bus definition must have a non-empty name");
        }

        BusDefinition existing = localDefinitions.put(name, definition);
        if (existing != null) {
            log.warn("Replaced existing local bus definition: {}", name);
        } else {
            log.debug("Registered local bus definition: {}", name);
        }
    }

    /**
     * Register a bus definition with automatic scope detection.
     *
     * <p>This method inspects the definition's scope property and registers it
     * in the appropriate scope (global or local). This is useful when working
     * with BusDefinition objects that already have their scope configured.</p>
     *
     * @param definition the bus definition to register
     * @param scope the scope for registration ("Global" or "Local")
     * @throws IllegalArgumentException if definition is null, has no name, or scope is invalid
     */
    public void register(BusDefinition definition, String scope) {
        if (scope == null) {
            throw new IllegalArgumentException("Scope cannot be null");
        }

        if ("Global".equalsIgnoreCase(scope)) {
            registerGlobal(definition);
        } else if ("Local".equalsIgnoreCase(scope)) {
            registerLocal(definition);
        } else {
            throw new IllegalArgumentException(
                "Invalid scope: " + scope + ". Must be 'Global' or 'Local'");
        }
    }

    // ===== LOOKUP METHODS =====

    /**
     * Lookup a bus definition by name.
     *
     * <p>This method first searches the local scope, then falls back to the global scope.
     * This allows subsystems to override global definitions with local versions while
     * maintaining a default global definition.</p>
     *
     * <p>Lookup Priority:
     * <ol>
     *   <li>Local scope (subsystem-specific)</li>
     *   <li>Global scope (model-wide)</li>
     * </ol>
     *
     * @param name the bus definition name to lookup
     * @return the bus definition, or null if not found in either scope
     */
    public BusDefinition lookup(String name) {
        if (name == null || name.trim().isEmpty()) {
            log.warn("Attempted to lookup bus definition with null or empty name");
            return null;
        }

        // Check local scope first
        BusDefinition definition = localDefinitions.get(name);
        if (definition != null) {
            log.debug("Found bus definition '{}' in local scope", name);
            return definition;
        }

        // Fall back to global scope
        definition = globalDefinitions.get(name);
        if (definition != null) {
            log.debug("Found bus definition '{}' in global scope", name);
            return definition;
        }

        log.debug("Bus definition '{}' not found in any scope", name);
        return null;
    }

    /**
     * Lookup a bus definition in the global scope only.
     *
     * <p>This method only searches the global scope and does not check local definitions.
     * Use this when you specifically need a global definition and want to avoid local
     * overrides.</p>
     *
     * @param name the bus definition name to lookup
     * @return the global bus definition, or null if not found
     */
    public BusDefinition lookupGlobal(String name) {
        if (name == null || name.trim().isEmpty()) {
            log.warn("Attempted to lookup global bus definition with null or empty name");
            return null;
        }

        BusDefinition definition = globalDefinitions.get(name);
        if (definition != null) {
            log.debug("Found global bus definition: {}", name);
        } else {
            log.debug("Global bus definition '{}' not found", name);
        }
        return definition;
    }

    /**
     * Lookup a bus definition in the local scope only.
     *
     * <p>This method only searches the local scope and does not check global definitions.
     * Use this when you specifically need a local definition.</p>
     *
     * @param name the bus definition name to lookup
     * @return the local bus definition, or null if not found
     */
    public BusDefinition lookupLocal(String name) {
        if (name == null || name.trim().isEmpty()) {
            log.warn("Attempted to lookup local bus definition with null or empty name");
            return null;
        }

        BusDefinition definition = localDefinitions.get(name);
        if (definition != null) {
            log.debug("Found local bus definition: {}", name);
        } else {
            log.debug("Local bus definition '{}' not found", name);
        }
        return definition;
    }

    // ===== QUERY METHODS =====

    /**
     * Check if a bus definition exists in any scope.
     *
     * @param name the bus definition name to check
     * @return true if definition exists in local or global scope
     */
    public boolean contains(String name) {
        return containsLocal(name) || containsGlobal(name);
    }

    /**
     * Check if a bus definition exists in the global scope.
     *
     * @param name the bus definition name to check
     * @return true if definition exists in global scope
     */
    public boolean containsGlobal(String name) {
        return name != null && globalDefinitions.containsKey(name);
    }

    /**
     * Check if a bus definition exists in the local scope.
     *
     * @param name the bus definition name to check
     * @return true if definition exists in local scope
     */
    public boolean containsLocal(String name) {
        return name != null && localDefinitions.containsKey(name);
    }

    /**
     * Get the number of global bus definitions.
     *
     * @return count of global definitions
     */
    public int getGlobalDefinitionCount() {
        return globalDefinitions.size();
    }

    /**
     * Get the number of local bus definitions.
     *
     * @return count of local definitions
     */
    public int getLocalDefinitionCount() {
        return localDefinitions.size();
    }

    /**
     * Get the total number of bus definitions (global + local).
     *
     * @return total count of definitions
     */
    public int getTotalDefinitionCount() {
        return globalDefinitions.size() + localDefinitions.size();
    }

    // ===== REMOVAL METHODS =====

    /**
     * Remove a bus definition from the global scope.
     *
     * @param name the bus definition name to remove
     * @return the removed definition, or null if not found
     */
    public BusDefinition removeGlobal(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }

        BusDefinition removed = globalDefinitions.remove(name);
        if (removed != null) {
            log.info("Removed global bus definition: {}", name);
        }
        return removed;
    }

    /**
     * Remove a bus definition from the local scope.
     *
     * @param name the bus definition name to remove
     * @return the removed definition, or null if not found
     */
    public BusDefinition removeLocal(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }

        BusDefinition removed = localDefinitions.remove(name);
        if (removed != null) {
            log.debug("Removed local bus definition: {}", name);
        }
        return removed;
    }

    /**
     * Clear all bus definitions from both global and local scopes.
     * This method is primarily useful for testing and cleanup.
     *
     * <p>Warning: This operation is irreversible and will remove all registered
     * bus definitions. Use with caution in production code.</p>
     */
    public void clear() {
        int globalCount = globalDefinitions.size();
        int localCount = localDefinitions.size();

        globalDefinitions.clear();
        localDefinitions.clear();

        log.info("Cleared all bus definitions: {} global, {} local", globalCount, localCount);
    }

    /**
     * Clear all global bus definitions only.
     */
    public void clearGlobal() {
        int count = globalDefinitions.size();
        globalDefinitions.clear();
        log.info("Cleared {} global bus definitions", count);
    }

    /**
     * Clear all local bus definitions only.
     */
    public void clearLocal() {
        int count = localDefinitions.size();
        localDefinitions.clear();
        log.debug("Cleared {} local bus definitions", count);
    }

    // ===== UTILITY METHODS =====

    /**
     * Get a snapshot of all global definition names.
     *
     * @return unmodifiable set of global definition names
     */
    public java.util.Set<String> getGlobalDefinitionNames() {
        return java.util.Collections.unmodifiableSet(globalDefinitions.keySet());
    }

    /**
     * Get a snapshot of all local definition names.
     *
     * @return unmodifiable set of local definition names
     */
    public java.util.Set<String> getLocalDefinitionNames() {
        return java.util.Collections.unmodifiableSet(localDefinitions.keySet());
    }

    /**
     * Get string representation of the registry status.
     *
     * @return status string with global and local definition counts
     */
    @Override
    public String toString() {
        return String.format("BusDefinitionRegistry{global=%d, local=%d, total=%d}",
            getGlobalDefinitionCount(), getLocalDefinitionCount(), getTotalDefinitionCount());
    }

    /**
     * Reset the singleton instance (for testing purposes only).
     *
     * <p>Warning: This method should only be used in test code to reset the registry
     * between tests. Using this in production code can lead to inconsistent state.</p>
     */
    public static synchronized void resetInstance() {
        if (instance != null) {
            instance.clear();
            instance = null;
            log.warn("BusDefinitionRegistry singleton instance reset (use only in tests)");
        }
    }
}
