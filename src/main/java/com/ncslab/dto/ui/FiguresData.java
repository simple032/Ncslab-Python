package com.ncslab.dto.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
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

        @JsonProperty("surface")
        private Surface surface;

        @JsonProperty("subplot")
        private Subplot subplot;

        @JsonProperty("plot_type")
        private String plotType; // "2D", "3D", "surface", or "subplot"

        /**
         * Check if this figure contains subplots
         * @return true if subplot data exists
         */
        public boolean hasSubplot() {
            return subplot != null && subplot.getPlots() != null && !subplot.getPlots().isEmpty();
        }

        /**
         * Check if this is a single plot figure (no subplots)
         * @return true if single plot
         */
        public boolean isSinglePlot() {
            return !hasSubplot() && (plot != null || surface != null);
        }

        /**
         * Get the number of subplots
         * @return subplot count, or 1 if single plot, or 0 if empty
         */
        public int getPlotCount() {
            if (hasSubplot()) {
                return subplot.getPlots().size();
            }
            return isSinglePlot() ? 1 : 0;
        }

        /**
         * Subplot container for multiple plots in a grid layout
         */
        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Subplot {
            /**
             * Layout as [rows, cols] - e.g., [2, 1] for 2 rows, 1 column
             */
            @JsonProperty("layout")
            private int[] layout;

            /**
             * List of subplot entries with position and plot data
             */
            @JsonProperty("plots")
            private List<SubplotEntry> plots;

            /**
             * Get number of rows in the subplot grid
             * @return row count
             */
            public int getRows() {
                return (layout != null && layout.length >= 1) ? layout[0] : 1;
            }

            /**
             * Get number of columns in the subplot grid
             * @return column count
             */
            public int getCols() {
                return (layout != null && layout.length >= 2) ? layout[1] : 1;
            }

            /**
             * Get total capacity of the subplot grid
             * @return rows * cols
             */
            public int getCapacity() {
                return getRows() * getCols();
            }

            /**
             * Get subplot entry at a specific position (1-based)
             * @param position 1-based position in the grid
             * @return SubplotEntry or null if not found
             */
            public SubplotEntry getPlotAt(int position) {
                if (plots == null) return null;
                return plots.stream()
                        .filter(p -> p.getPosition() != null && p.getPosition() == position)
                        .findFirst()
                        .orElse(null);
            }

            /**
             * Convert 1-based position to row index (0-based)
             * @param position 1-based position
             * @return row index (0-based)
             */
            public int positionToRow(int position) {
                return (position - 1) / getCols();
            }

            /**
             * Convert 1-based position to column index (0-based)
             * @param position 1-based position
             * @return column index (0-based)
             */
            public int positionToCol(int position) {
                return (position - 1) % getCols();
            }
        }

        /**
         * Individual subplot entry with position and plot/surface data
         */
        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class SubplotEntry {
            /**
             * Position in the subplot grid (1-based, row-major order)
             * For a 2x2 grid: 1=top-left, 2=top-right, 3=bottom-left, 4=bottom-right
             */
            @JsonProperty("position")
            private Integer position;

            /**
             * 2D or 3D line plot data
             */
            @JsonProperty("plot")
            private Plot plot;

            /**
             * 3D surface/mesh/contour data
             */
            @JsonProperty("surface")
            private Surface surface;

            /**
             * Check if this entry has plot data
             */
            public boolean hasPlot() {
                return plot != null;
            }

            /**
             * Check if this entry has surface data
             */
            public boolean hasSurface() {
                return surface != null;
            }
        }

        /**
         * Axis limit configuration for plot axes
         */
        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class AxisLimit {
            /**
             * Minimum value for the axis
             */
            @JsonProperty("min")
            private Double min;

            /**
             * Maximum value for the axis
             */
            @JsonProperty("max")
            private Double max;

            /**
             * Limit mode: "auto" or "manual"
             */
            @JsonProperty("mode")
            private String mode;

            /**
             * Limit method: "tickaligned", "tight", "padded", etc.
             */
            @JsonProperty("method")
            private String method;

            /**
             * Check if this limit is in auto mode
             */
            public boolean isAuto() {
                return "auto".equalsIgnoreCase(mode);
            }

            /**
             * Check if this limit is in manual mode
             */
            public boolean isManual() {
                return "manual".equalsIgnoreCase(mode);
            }

            /**
             * Get the range (max - min)
             * @return range or null if min/max not set
             */
            public Double getRange() {
                if (min != null && max != null) {
                    return max - min;
                }
                return null;
            }
        }

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

            @JsonProperty("xlim")
            private AxisLimit xlim;

            @JsonProperty("ylim")
            private AxisLimit ylim;

            @JsonProperty("zlim")
            private AxisLimit zlim; // For 3D plots

            @JsonProperty("legend")
            @JsonDeserialize(using = LegendDeserializer.class)
            private Legend legend;

            @JsonProperty("line_count")
            private Integer lineCount;

            @JsonProperty("lines")
            private List<Line> lines;

            /**
             * Check if X axis has custom limits
             */
            public boolean hasXlim() {
                return xlim != null && xlim.getMin() != null && xlim.getMax() != null;
            }

            /**
             * Check if Y axis has custom limits
             */
            public boolean hasYlim() {
                return ylim != null && ylim.getMin() != null && ylim.getMax() != null;
            }

            /**
             * Check if Z axis has custom limits (for 3D plots)
             */
            public boolean hasZlim() {
                return zlim != null && zlim.getMin() != null && zlim.getMax() != null;
            }

            /**
             * Check if legend is visible and has labels
             */
            public boolean hasLegend() {
                return legend != null && legend.getVisible() != null && legend.getVisible()
                        && legend.getLabels() != null && !legend.getLabels().isEmpty();
            }

            /**
             * Custom deserializer for Legend that handles both:
             * - Legacy format: ["label1", "label2"] (array of strings)
             * - New format: {"visible": true, "labels": [...], ...} (full object)
             */
            public static class LegendDeserializer extends JsonDeserializer<Legend> {
                @Override
                public Legend deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                    JsonToken token = p.getCurrentToken();

                    if (token == JsonToken.START_ARRAY) {
                        // Legacy format: ["label1", "label2"]
                        List<String> labels = new ArrayList<>();
                        while (p.nextToken() != JsonToken.END_ARRAY) {
                            labels.add(p.getText());
                        }
                        return Legend.builder()
                                .visible(true)
                                .labels(labels)
                                .location("northeast")
                                .orientation("vertical")
                                .build();
                    } else if (token == JsonToken.START_OBJECT) {
                        // New format: full Legend object
                        return p.getCodec().readValue(p, Legend.class);
                    } else if (token == JsonToken.VALUE_NULL) {
                        return null;
                    }

                    throw new IOException("Unexpected token for Legend: " + token);
                }
            }

            /**
             * Legend configuration for plot
             */
            @Data
            @NoArgsConstructor
            @AllArgsConstructor
            @Builder
            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class Legend {
                /**
                 * Whether the legend is visible
                 */
                @JsonProperty("visible")
                private Boolean visible;

                /**
                 * Labels for each series in the legend
                 */
                @JsonProperty("labels")
                private List<String> labels;

                /**
                 * Legend location: "northeast", "northwest", "southeast", "southwest",
                 * "north", "south", "east", "west", "best", "none"
                 */
                @JsonProperty("location")
                private String location;

                /**
                 * Legend orientation: "vertical" or "horizontal"
                 */
                @JsonProperty("orientation")
                private String orientation;

                /**
                 * Number of columns in the legend
                 */
                @JsonProperty("numColumns")
                private Integer numColumns;

                /**
                 * Whether to show the legend box
                 */
                @JsonProperty("box")
                private Boolean box;

                /**
                 * Box edge/border color (e.g., "#000000")
                 */
                @JsonProperty("boxEdgeColor")
                private String boxEdgeColor;

                /**
                 * Box border line width
                 */
                @JsonProperty("boxLineWidth")
                private Double boxLineWidth;

                /**
                 * Box background/face color (e.g., "#FFFFFF")
                 */
                @JsonProperty("boxFaceColor")
                private String boxFaceColor;

                /**
                 * Box background transparency (0.0 to 1.0)
                 */
                @JsonProperty("boxFaceAlpha")
                private Double boxFaceAlpha;

                /**
                 * Font size for legend text
                 */
                @JsonProperty("fontSize")
                private Integer fontSize;

                /**
                 * Font family name (e.g., "Helvetica", "Arial")
                 */
                @JsonProperty("fontName")
                private String fontName;

                /**
                 * Font weight: "normal" or "bold"
                 */
                @JsonProperty("fontWeight")
                private String fontWeight;

                /**
                 * Text color (e.g., "#000000")
                 */
                @JsonProperty("textColor")
                private String textColor;

                /**
                 * Check if legend should be displayed
                 */
                public boolean isVisible() {
                    return visible != null && visible;
                }

                /**
                 * Check if legend box should be shown
                 */
                public boolean hasBox() {
                    return box != null && box;
                }

                /**
                 * Check if orientation is horizontal
                 */
                public boolean isHorizontal() {
                    return "horizontal".equalsIgnoreCase(orientation);
                }

                /**
                 * Check if orientation is vertical
                 */
                public boolean isVertical() {
                    return orientation == null || "vertical".equalsIgnoreCase(orientation);
                }

                /**
                 * Get label count
                 */
                public int getLabelCount() {
                    return labels != null ? labels.size() : 0;
                }
            }

            @Data
            @NoArgsConstructor
            @AllArgsConstructor
            @Builder
            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class Line {
                @JsonProperty("point_count")
                private Integer pointCount;

                @JsonProperty("style")
                private String style;

                @JsonProperty("x_data")
                private double[] xData;

                @JsonProperty("y_data")
                private double[] yData;

                @JsonProperty("z_data")
                private double[] zData; // For 3D plots
            }
        }

        /**
         * Surface data for surf/mesh/contour plots
         */
        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Surface {
            @JsonProperty("title")
            private String title;

            @JsonProperty("xlabel")
            private String xlabel;

            @JsonProperty("ylabel")
            private String ylabel;

            @JsonProperty("zlabel")
            private String zlabel;

            @JsonProperty("xlim")
            private AxisLimit xlim;

            @JsonProperty("ylim")
            private AxisLimit ylim;

            @JsonProperty("zlim")
            private AxisLimit zlim;

            @JsonProperty("rows")
            private Integer rows;

            @JsonProperty("cols")
            private Integer cols;

            @JsonProperty("surface_type")
            private String surfaceType; // "surf", "mesh", or "contour"

            @JsonProperty("x_data")
            private double[] xData; // Flattened X grid (rows * cols elements)

            @JsonProperty("y_data")
            private double[] yData; // Flattened Y grid (rows * cols elements)

            @JsonProperty("z_data")
            private double[] zData; // Flattened Z grid (rows * cols elements)

            @JsonProperty("show_colorbar")
            private Boolean showColorbar;

            @JsonProperty("hidden3d")
            private Boolean hidden3d;

            @JsonProperty("colormap")
            private String colormap;

            @JsonProperty("contour_levels")
            private double[] contourLevels; // Specific contour level values

            @JsonProperty("num_contours")
            private Integer numContours; // Number of auto contour levels

            /**
             * Check if X axis has custom limits
             */
            public boolean hasXlim() {
                return xlim != null && xlim.getMin() != null && xlim.getMax() != null;
            }

            /**
             * Check if Y axis has custom limits
             */
            public boolean hasYlim() {
                return ylim != null && ylim.getMin() != null && ylim.getMax() != null;
            }

            /**
             * Check if Z axis has custom limits
             */
            public boolean hasZlim() {
                return zlim != null && zlim.getMin() != null && zlim.getMax() != null;
            }

            /**
             * Get Z value at grid position (row, col)
             * @param row Row index (0-based)
             * @param col Column index (0-based)
             * @return Z value at the specified position
             */
            public double getZ(int row, int col) {
                if (zData == null || rows == null || cols == null) {
                    throw new IllegalStateException("Surface data not initialized");
                }
                if (row < 0 || row >= rows || col < 0 || col >= cols) {
                    throw new IndexOutOfBoundsException("Invalid grid position");
                }
                return zData[row * cols + col];
            }

            /**
             * Get X value at grid position (row, col)
             * @param row Row index (0-based)
             * @param col Column index (0-based)
             * @return X value at the specified position
             */
            public double getX(int row, int col) {
                if (xData == null || rows == null || cols == null) {
                    throw new IllegalStateException("Surface data not initialized");
                }
                if (row < 0 || row >= rows || col < 0 || col >= cols) {
                    throw new IndexOutOfBoundsException("Invalid grid position");
                }
                return xData[row * cols + col];
            }

            /**
             * Get Y value at grid position (row, col)
             * @param row Row index (0-based)
             * @param col Column index (0-based)
             * @return Y value at the specified position
             */
            public double getY(int row, int col) {
                if (yData == null || rows == null || cols == null) {
                    throw new IllegalStateException("Surface data not initialized");
                }
                if (row < 0 || row >= rows || col < 0 || col >= cols) {
                    throw new IndexOutOfBoundsException("Invalid grid position");
                }
                return yData[row * cols + col];
            }

            /**
             * Get Z data as 2D array [rows][cols]
             * @return 2D array of Z values
             */
            public double[][] getZMatrix() {
                if (zData == null || rows == null || cols == null) {
                    return null;
                }
                double[][] matrix = new double[rows][cols];
                for (int i = 0; i < rows; i++) {
                    for (int j = 0; j < cols; j++) {
                        matrix[i][j] = zData[i * cols + j];
                    }
                }
                return matrix;
            }

            /**
             * Get X data as 2D array [rows][cols]
             * @return 2D array of X values
             */
            public double[][] getXMatrix() {
                if (xData == null || rows == null || cols == null) {
                    return null;
                }
                double[][] matrix = new double[rows][cols];
                for (int i = 0; i < rows; i++) {
                    for (int j = 0; j < cols; j++) {
                        matrix[i][j] = xData[i * cols + j];
                    }
                }
                return matrix;
            }

            /**
             * Get Y data as 2D array [rows][cols]
             * @return 2D array of Y values
             */
            public double[][] getYMatrix() {
                if (yData == null || rows == null || cols == null) {
                    return null;
                }
                double[][] matrix = new double[rows][cols];
                for (int i = 0; i < rows; i++) {
                    for (int j = 0; j < cols; j++) {
                        matrix[i][j] = yData[i * cols + j];
                    }
                }
                return matrix;
            }

            /**
             * Check if this is a surf plot
             */
            public boolean isSurf() {
                return "surf".equalsIgnoreCase(surfaceType);
            }

            /**
             * Check if this is a mesh plot
             */
            public boolean isMesh() {
                return "mesh".equalsIgnoreCase(surfaceType);
            }

            /**
             * Check if this is a contour plot
             */
            public boolean isContour() {
                return "contour".equalsIgnoreCase(surfaceType);
            }
        }
    }
}