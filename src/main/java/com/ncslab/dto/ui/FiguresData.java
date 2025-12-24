package com.ncslab.dto.ui;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
/**
 * Inner class to represent figures data structure
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class FiguresData {
    @JsonProperty("version")
    private String version;
    
    @JsonProperty("type")
    private String type;
    
    @JsonProperty("figure_count")
    private Integer figureCount;
    
    @JsonProperty("figures")
    private List<Figure> figures;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Figure {
        @JsonProperty("figure_id")
        private Integer figureId;

        @JsonProperty("plot")
        private Plot plot;

        @JsonProperty("plot_type")
        private String plotType; // "2D" or "3D"
        
        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Plot {
            @JsonProperty("title")
            private String title;

            @JsonProperty("xlabel")
            private String xlabel;

            @JsonProperty("ylabel")
            private String ylabel;

            @JsonProperty("zlabel")
            private String zlabel; // For 3D plots

            @JsonProperty("legend")
            private List<String> legend;
            
            @JsonProperty("line_count")
            private Integer lineCount;
            
            @JsonProperty("lines")
            private List<Line> lines;
            
            @Data
            @NoArgsConstructor
            @AllArgsConstructor
            @Builder
            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class Line {
                @JsonProperty("point_count")
                private Integer pointCount;

                @JsonProperty("style")
                private String style; // TODO: mfcalc server传输回来的字符串有问题

                @JsonProperty("x_data")
                private double[] xData;

                @JsonProperty("y_data")
                private double[] yData;

                @JsonProperty("z_data")
                private double[] zData; // For 3D plots
            }
        }
    }
}
