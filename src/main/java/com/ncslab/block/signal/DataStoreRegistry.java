package com.ncslab.block.signal;

import com.ncslab.block.data.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Centralized registry for Data Store blocks providing global variable access.
 *
 * <p>This registry manages model-specific data stores to ensure proper isolation
 * between different simulation models. Each model has its own namespace of data stores
 * identified by the model's UUID.</p>
 *
 * <p>Thread-safe implementation using ConcurrentHashMap for concurrent access.</p>
 *
 * <h3>Usage Pattern:</h3>
 * <pre>
 * // Register a data store
 * DataStoreRegistry.registerDataStore(modelUUID, "GlobalCounter", new Data(0.0));
 *
 * // Read from data store
 * Data value = DataStoreRegistry.readDataStore(modelUUID, "GlobalCounter");
 *
 * // Write to data store
 * DataStoreRegistry.writeDataStore(modelUUID, "GlobalCounter", new Data(42.0));
 *
 * // Clean up when simulation ends
 * DataStoreRegistry.clearModelDataStores(modelUUID);
 * </pre>
 *
 * @author NCSLab Team
 * @version 1.0
 * @since 2025
 */
@Slf4j
public class DataStoreRegistry {

    /**
     * Model-specific data store registries.
     * Outer map key: model UUID
     * Inner map key: data store name
     * Inner map value: Data object storing the current value
     */
    private static final Map<String, Map<String, Data>> modelDataStores = new ConcurrentHashMap<>();

    /**
     * Registers a new data store for a specific model.
     * If a data store with the same name already exists, it will be replaced.
     *
     * @param modelUUID Unique identifier for the model
     * @param dataStoreName Name of the data store (must be unique within the model)
     * @param initialValue Initial value for the data store
     * @throws IllegalArgumentException if any parameter is null or empty
     */
    public static void registerDataStore(String modelUUID, String dataStoreName, Data initialValue) {
        validateParameters(modelUUID, dataStoreName, "initialValue", initialValue);

        // Get or create the model's data store map
        Map<String, Data> modelStores = modelDataStores.computeIfAbsent(
            modelUUID,
            k -> new ConcurrentHashMap<>()
        );

        // Register the data store
        Data previousValue = modelStores.put(dataStoreName, initialValue);

        if (previousValue != null) {
            log.warn("Data store '{}' in model '{}' was already registered and has been replaced",
                    dataStoreName, modelUUID);
        } else {
            log.debug("Registered data store '{}' in model '{}' with initial value: {}",
                     dataStoreName, modelUUID, initialValue.getDataString());
        }
    }

    /**
     * Reads the current value from a data store.
     *
     * @param modelUUID Unique identifier for the model
     * @param dataStoreName Name of the data store to read from
     * @return Current value of the data store
     * @throws IllegalArgumentException if parameters are null/empty or data store doesn't exist
     */
    public static Data readDataStore(String modelUUID, String dataStoreName) {
        validateParameters(modelUUID, dataStoreName);

        Map<String, Data> modelStores = modelDataStores.get(modelUUID);
        if (modelStores == null) {
            throw new IllegalArgumentException(
                String.format("No data stores found for model '%s'. Model may not be initialized.", modelUUID)
            );
        }

        Data value = modelStores.get(dataStoreName);
        if (value == null) {
            throw new IllegalArgumentException(
                String.format("Data store '%s' not found in model '%s'. Available stores: %s",
                            dataStoreName, modelUUID, modelStores.keySet())
            );
        }

        log.trace("Read data store '{}' in model '{}': {}",
                 dataStoreName, modelUUID, value.getDataString());
        return value;
    }

    /**
     * Writes a new value to an existing data store.
     *
     * @param modelUUID Unique identifier for the model
     * @param dataStoreName Name of the data store to write to
     * @param newValue New value to store
     * @throws IllegalArgumentException if parameters are null/empty or data store doesn't exist
     */
    public static void writeDataStore(String modelUUID, String dataStoreName, Data newValue) {
        validateParameters(modelUUID, dataStoreName, "newValue", newValue);

        Map<String, Data> modelStores = modelDataStores.get(modelUUID);
        if (modelStores == null) {
            throw new IllegalArgumentException(
                String.format("No data stores found for model '%s'. Model may not be initialized.", modelUUID)
            );
        }

        Data previousValue = modelStores.put(dataStoreName, newValue);
        if (previousValue == null) {
            throw new IllegalArgumentException(
                String.format("Data store '%s' not found in model '%s'. Available stores: %s",
                            dataStoreName, modelUUID, modelStores.keySet())
            );
        }

        log.trace("Wrote data store '{}' in model '{}': {} (previous: {})",
                 dataStoreName, modelUUID, newValue.getDataString(), previousValue.getDataString());
    }

    /**
     * Checks if a data store exists in a model.
     *
     * @param modelUUID Unique identifier for the model
     * @param dataStoreName Name of the data store to check
     * @return true if the data store exists, false otherwise
     */
    public static boolean dataStoreExists(String modelUUID, String dataStoreName) {
        if (modelUUID == null || modelUUID.trim().isEmpty()) {
            return false;
        }
        if (dataStoreName == null || dataStoreName.trim().isEmpty()) {
            return false;
        }

        Map<String, Data> modelStores = modelDataStores.get(modelUUID);
        return modelStores != null && modelStores.containsKey(dataStoreName);
    }

    /**
     * Clears all data stores for a specific model.
     * This should be called when a simulation ends to prevent memory leaks.
     *
     * @param modelUUID Unique identifier for the model
     */
    public static void clearModelDataStores(String modelUUID) {
        if (modelUUID == null || modelUUID.trim().isEmpty()) {
            log.warn("Attempted to clear data stores for null or empty model UUID");
            return;
        }

        Map<String, Data> removedStores = modelDataStores.remove(modelUUID);
        if (removedStores != null) {
            log.info("Cleared {} data stores for model '{}'", removedStores.size(), modelUUID);
        } else {
            log.debug("No data stores to clear for model '{}'", modelUUID);
        }
    }

    /**
     * Clears all data stores for all models.
     * Use with caution - typically only for testing or application shutdown.
     */
    public static void clearAllDataStores() {
        int totalModels = modelDataStores.size();
        int totalStores = modelDataStores.values().stream()
            .mapToInt(Map::size)
            .sum();

        modelDataStores.clear();

        log.info("Cleared all data stores: {} stores across {} models", totalStores, totalModels);
    }

    /**
     * Gets the number of data stores registered for a specific model.
     *
     * @param modelUUID Unique identifier for the model
     * @return Number of data stores, or 0 if model not found
     */
    public static int getDataStoreCount(String modelUUID) {
        if (modelUUID == null || modelUUID.trim().isEmpty()) {
            return 0;
        }

        Map<String, Data> modelStores = modelDataStores.get(modelUUID);
        return modelStores != null ? modelStores.size() : 0;
    }

    /**
     * Gets the total number of models with registered data stores.
     *
     * @return Number of models
     */
    public static int getModelCount() {
        return modelDataStores.size();
    }

    // ===== Validation Helpers =====

    private static void validateParameters(String modelUUID, String dataStoreName) {
        if (modelUUID == null || modelUUID.trim().isEmpty()) {
            throw new IllegalArgumentException("Model UUID cannot be null or empty");
        }
        if (dataStoreName == null || dataStoreName.trim().isEmpty()) {
            throw new IllegalArgumentException("Data store name cannot be null or empty");
        }
    }

    private static void validateParameters(String modelUUID, String dataStoreName,
                                          String valueName, Object value) {
        validateParameters(modelUUID, dataStoreName);
        if (value == null) {
            throw new IllegalArgumentException(valueName + " cannot be null");
        }
    }
}
