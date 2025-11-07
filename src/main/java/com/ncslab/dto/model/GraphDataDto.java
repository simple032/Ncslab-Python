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

        @JsonProperty("props")
        private PropDataDto props;

        // Link-specific properties
        @JsonProperty("source")
        private LinkDto source;

        @JsonProperty("target")
        private LinkDto target;
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

        @JsonProperty("paramLables")
        private Map<String, String> paramLables;

        @JsonProperty("paramValues")
        private Map<String, Object> paramValues;

        @JsonProperty("srcBlock")
        private String srcBlock;

        @JsonProperty("path")
        private String path;

        @JsonProperty("title")
        private String title;
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


