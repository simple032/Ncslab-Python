package com.ncslab.dto.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * DTO for JointJS Graph Data structure
 * Represents the visual graph representation of the model
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class GraphDataDto {
    @JsonProperty("cells")
    private CellDataDto[] cells;

    /**
     * Cell Data DTO - represents a single cell (block or link) in the graph
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CellDataDto {
        @JsonProperty("type")
        private String type; // standard.Link or block type

        @JsonProperty("id")
        private String id;

        @JsonProperty("path")
        private String path;

        @JsonProperty("subGraph")
        private SubGraphDataDto subGraph;

        // Use Object to handle both PropDataDto objects and arrays gracefully
        @JsonProperty("props")
        private Object props;

        // Link-specific properties
        @JsonProperty("source")
        private LinkDto source;

        @JsonProperty("target")
        private LinkDto target;

        /**
         * Get props as PropDataDto if it's an object, null otherwise
         */
        public PropDataDto getPropsAsObject() {
            if (props instanceof Map) {
                // Convert Map to PropDataDto using Jackson
                try {
                    com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    return mapper.convertValue(props, PropDataDto.class);
                } catch (Exception e) {
                    return null;
                }
            }
            return props instanceof PropDataDto ? (PropDataDto) props : null;
        }
    }

    /**
     * Block Properties DTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PropDataDto {
        @JsonProperty("blockName")
        private String blockName;

        @JsonProperty("blockType")
        private String blockType;

        @JsonProperty("description")
        private String description;

        // Can be either Map (object) or empty array []
        @JsonProperty("paramLables")
        private Object paramLables;

        @JsonProperty("paramValues")
        private Object paramValues;

        @JsonProperty("srcBlock")
        private String srcBlock;

        @JsonProperty("path")
        private String path;

        @JsonProperty("title")
        private String title;

        /**
         * Get paramLables as Map if it's an object, empty map if it's an array or null
         */
        @SuppressWarnings("unchecked")
        public Map<String, String> getParamLablesAsMap() {
            if (paramLables instanceof Map) {
                return (Map<String, String>) paramLables;
            }
            return new java.util.HashMap<>();
        }

        /**
         * Get paramValues as Map if it's an object, empty map if it's an array or null
         */
        @SuppressWarnings("unchecked")
        public Map<String, Object> getParamValuesAsMap() {
            if (paramValues instanceof Map) {
                return (Map<String, Object>) paramValues;
            }
            return new java.util.HashMap<>();
        }
    }

    /**
     * Sub-Graph Data DTO - for nested subsystem graphs
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SubGraphDataDto {
        @JsonProperty("init")
        private Boolean init;

        @JsonProperty("path")
        private String path;

        @JsonProperty("cells")
        private CellDataDto[] cells;
    }

    /**
     * Link Connection DTO - source/target port information
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LinkDto {
        @JsonProperty("id")
        private String id;

        @JsonProperty("port")
        private String port;
    }
}


