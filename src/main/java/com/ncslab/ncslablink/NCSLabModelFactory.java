package com.ncslab.ncslablink;

import com.ncslab.dto.core.ModelDto;

/**
 * Factory for creating specific NCSLabModel instances
 * Handles the creation of different model types based on context
 */
public class NCSLabModelFactory {
    
    /**
     * Create a model from DTO and mode
     */
    @SuppressWarnings("unchecked")
    public static <T extends NCSLabModel> T createModel(Class<T> modelClass, ModelDto dto, ModelMode mode) throws ModelException {
        if (modelClass.equals(NCSLabModel.class)) {
            // Base class - create anonymous implementation
            return (T) new NCSLabModel(dto, mode) {};
        }
        
        if (modelClass.equals(SimulationModel.class)) {
            // Create SimulationModel
            return (T) new SimulationModel(dto, mode);
        }
        
        // Add other model types as needed
        // if (modelClass.equals(CodeGenerationModel.class)) {
        //     return (T) new CodeGenerationModel(dto, mode);
        // }
        
        throw new ModelException("Unsupported model class: " + modelClass.getName());
    }
    
    /**
     * Create a model from DTO with default mode
     */
    public static <T extends NCSLabModel> T createModel(Class<T> modelClass, ModelDto dto) throws ModelException {
        return createModel(modelClass, dto, ModelMode.Simulation);
    }
    
    /**
     * Determine the appropriate model class from DTO content
     */
    public static Class<? extends NCSLabModel> determineModelClass(ModelDto dto, ModelMode mode) {
        // Logic to determine model class based on DTO content and mode
        switch (mode) {
            case Simulation:
                return SimulationModel.class;
            case Compilation:
                // return CodeGenerationModel.class;
                return NCSLabModel.class; // Fallback for now
            default:
                return NCSLabModel.class;
        }
    }
    
    /**
     * Create a model with automatic class determination
     */
    @SuppressWarnings("unchecked")
    public static <T extends NCSLabModel> T createModelAuto(ModelDto dto, ModelMode mode) throws ModelException {
        Class<? extends NCSLabModel> modelClass = determineModelClass(dto, mode);
        return (T) createModel(modelClass, dto, mode);
    }
}