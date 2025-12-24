package com.ncslab.util;

import com.ncslab.dto.simulation.ScopeDataDto;
import com.ncslab.dto.simulation.SimulationResultsDto;
import lombok.extern.slf4j.Slf4j;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for generating PNG plot files from scope simulation data
 */
@Slf4j
public class ScopePlotGenerator {

    private static final int DEFAULT_WIDTH = 800;
    private static final int DEFAULT_HEIGHT = 600;

    /**
     * Generate PNG files for all scopes in simulation results
     *
     * @param resultsDto simulation results containing scope data
     * @param outputDirectory base directory for output files
     * @return list of generated file paths
     * @throws IOException if file writing fails
     */
    public static List<String> generateAllScopePlots(SimulationResultsDto resultsDto, String outputDirectory) throws IOException {
        List<String> generatedFiles = new ArrayList<>();

        if (resultsDto == null || resultsDto.getScopes() == null || resultsDto.getScopes().isEmpty()) {
            log.warn("No scopes found in simulation results");
            return generatedFiles;
        }

        // Create output directory if it doesn't exist
        Path outputPath = Paths.get(outputDirectory);
        if (!Files.exists(outputPath)) {
            Files.createDirectories(outputPath);
        }

        // Generate PNG for each scope
        for (int i = 0; i < resultsDto.getScopes().size(); i++) {
            ScopeDataDto scope = resultsDto.getScopes().get(i);
            String fileName = generateScopePlot(scope, outputDirectory, i);
            if (fileName != null) {
                generatedFiles.add(fileName);
            }
        }

        log.info("Generated {} scope plot files in {}", generatedFiles.size(), outputDirectory);
        return generatedFiles;
    }

    /**
     * Generate a single PNG plot for one scope
     *
     * @param scopeData scope data to plot
     * @param outputDirectory output directory
     * @param index scope index for filename
     * @return generated file path or null if failed
     */
    public static String generateScopePlot(ScopeDataDto scopeData, String outputDirectory, int index) {
        try {
            // Create XY series from scope data
            XYSeries series = new XYSeries(scopeData.getName() != null ? scopeData.getName() : "Scope " + index);

            List<Double> timeData = scopeData.getTime();
            List<Double> valueData = scopeData.getData();

            if (timeData == null || valueData == null || timeData.isEmpty() || valueData.isEmpty()) {
                log.warn("Scope {} has no data to plot", index);
                return null;
            }

            int dataPoints = Math.min(timeData.size(), valueData.size());
            for (int i = 0; i < dataPoints; i++) {
                series.add(timeData.get(i), valueData.get(i));
            }

            // Create dataset
            XYSeriesCollection dataset = new XYSeriesCollection(series);

            // Create chart
            JFreeChart chart = ChartFactory.createXYLineChart(
                scopeData.getName() != null ? scopeData.getName() : "Scope " + index,
                "Time (s)",
                "Value",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
            );

            // Generate filename
            String safeName = scopeData.getName() != null ?
                scopeData.getName().replaceAll("[^a-zA-Z0-9]", "_") :
                "scope_" + index;
            String fileName = safeName + ".png";
            String filePath = Paths.get(outputDirectory, fileName).toString();

            // Save as PNG
            ChartUtils.saveChartAsPNG(new File(filePath), chart, DEFAULT_WIDTH, DEFAULT_HEIGHT);

            log.info("Generated scope plot: {}", filePath);
            return filePath;

        } catch (Exception e) {
            log.error("Failed to generate plot for scope {}: {}", index, e.getMessage());
            return null;
        }
    }

    /**
     * Generate PNG files from results.json file path
     *
     * @param resultsJsonPath path to results.json file
     * @param outputDirectory output directory for PNG files
     * @return list of generated file paths
     * @throws IOException if file reading or writing fails
     */
    public static List<String> generateScopePlotsFromFile(String resultsJsonPath, String outputDirectory) throws IOException {
        try {
            // Read and parse results.json
            SimulationResultsDto resultsDto = JsonUtils.getObjectMapper()
                .readValue(new File(resultsJsonPath), SimulationResultsDto.class);

            return generateAllScopePlots(resultsDto, outputDirectory);

        } catch (IOException e) {
            log.error("Failed to read results.json from {}: {}", resultsJsonPath, e.getMessage());
            throw e;
        }
    }
}
