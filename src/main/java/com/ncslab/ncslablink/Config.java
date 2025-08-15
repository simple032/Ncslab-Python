package com.ncslab.ncslablink;

import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.dto.ConfigJson;

@Getter
public class Config {

    private String step; // "VariableStep" or "FixedStep"
	private double fixedStep;
	private String solver;
	private double startTime;
	private double stopTime;
    private int maxDataPoints=2000;
    private double maxStep;
    private double minStep;
    private double initialStep;
    private double relTol;
    private double absTol;

	Config(JSONObject configIn,ModelMode mode){

        // Parse step type
        this.step = configIn.optString("Step", "VariableStep");

        // Parse FixedStep with auto handling
        Object fixedStepObj = configIn.opt("FixedStep");
        if (fixedStepObj instanceof String && "auto".equals(fixedStepObj)) {
            this.fixedStep = 0.01; // Default auto value
        } else {
            this.fixedStep = configIn.optDouble("FixedStep", 0.01);
        }

        // Parse solver
        this.solver = configIn.optString("Solver", "VariableStepAuto");

        // Parse time parameters
        this.startTime = parseDoubleOrDefault(configIn, "StartTime", 0.0);
        this.stopTime = parseDoubleOrDefault(configIn, "StopTime", 20.0);
        
        // Parse data points
        this.maxDataPoints = parseIntOrDefault(configIn, "MaxDataPoints", 1000);

        // Parse step size parameters with auto handling
        this.maxStep = parseDoubleWithAuto(configIn, "MaxStep", this.fixedStep * 10);
        this.minStep = parseDoubleWithAuto(configIn, "MinStep", 1e-6);
        this.initialStep = parseDoubleWithAuto(configIn, "InitialStep", this.fixedStep);

        // Parse tolerance parameters with auto handling  
        this.relTol = parseDoubleWithAuto(configIn, "RelTol", 1e-3);
        this.absTol = parseDoubleWithAuto(configIn, "AbsTol", 1e-6);

	}

	public static Config createFromJSON(JSONObject configIn,ModelMode mode) {
        return new Config(configIn,mode);
	}

	/**
	 * Create Config from ConfigJson DTO
	 */
	public static Config createFromConfigJson(ConfigJson configDto, ModelMode mode) {
		try {
			// Convert ConfigJson DTO to JSONObject for compatibility with existing constructor
			JSONObject configJson = new JSONObject();
			if (configDto.getFixedStep() != null) {
				configJson.put("FixedStep", configDto.getFixedStep());
			}
			if (configDto.getSolver() != null) {
				configJson.put("Solver", configDto.getSolver());
			}
			if (configDto.getStartTime() != null) {
				configJson.put("StartTime", configDto.getStartTime());
			}
			if (configDto.getStopTime() != null) {
				configJson.put("StopTime", configDto.getStopTime());
			}
			if (configDto.getMinStep() != null) {
				configJson.put("MinStep", configDto.getMinStep());
			}
			if (configDto.getAbsTol() != null) {
				configJson.put("AbsTol", configDto.getAbsTol());
			}
			if (configDto.getRelTol() != null) {
				configJson.put("RelTol", configDto.getRelTol());
			}
			if (configDto.getMaxDataPoints() != null) {
				configJson.put("MaxDataPoints", configDto.getMaxDataPoints());
			}
			
			return createFromJSON(configJson, mode);
		} catch (Exception e) {
			System.err.println("Failed to create Config from DTO: " + e.getMessage());
			// Return default config on error
			return createFromJSON(new JSONObject(), mode);
		}
	}

    // Helper methods for parsing configuration values
    
    private double parseDoubleOrDefault(JSONObject json, String key, double defaultValue) {
        Object value = json.opt(key);
        if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        } else if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return defaultValue;
    }
    
    private int parseIntOrDefault(JSONObject json, String key, int defaultValue) {
        Object value = json.opt(key);
        if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        } else if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return defaultValue;
    }
    
    private double parseDoubleWithAuto(JSONObject json, String key, double autoValue) {
        Object value = json.opt(key);
        if (value instanceof String) {
            String strValue = (String) value;
            if ("auto".equals(strValue)) {
                return autoValue;
            }
            try {
                return Double.parseDouble(strValue);
            } catch (NumberFormatException e) {
                return autoValue;
            }
        } else if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return autoValue;
    }

    // Utility methods for backward compatibility and step control
    
    public boolean isVariableStep() {
        return "VariableStep".equals(step);
    }
    
    public boolean isFixedStep() {
        return "FixedStep".equals(step);
    }
    
    public double getEffectiveStepSize() {
        return isFixedStep() ? fixedStep : initialStep;
    }

}
