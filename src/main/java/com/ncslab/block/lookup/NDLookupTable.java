package com.ncslab.block.lookup;

import com.ncslab.block.Block;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.Parameter;
import com.ncslab.block.data.Data;
import com.ncslab.block.data.DataType;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.util.TemplateManager;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * N-Dimensional Lookup Table block with SIMULINK-compatible parameters.
 *
 * Provides multi-dimensional table interpolation with support for:
 * - 1-D to n-D lookup tables (currently supports 1-D and 2-D)
 * - Multiple interpolation methods (Linear, Flat, Nearest)
 * - Multiple extrapolation methods (Linear, Clip)
 * - Explicit or evenly-spaced breakpoint specification
 *
 * SIMULINK Parameters:
 * - NumberOfTableDimensions: Number of table dimensions (1 to n)
 * - BreakpointsSpecification: "Explicit values" or "Even spacing"
 * - BreakpointsForDimension1: Breakpoints for dimension 1
 * - BreakpointsForDimension2: Breakpoints for dimension 2 (if n>=2)
 * - Table: Table data values (n-dimensional array)
 * - InterpMethod: Interpolation method ("Linear point-slope", "Flat", "Nearest")
 * - ExtrapMethod: Extrapolation method ("Linear", "Clip")
 * - SampleTime: Sample time for discrete operation
 * - OutDataTypeStr: Output data type specification
 *
 * @author NCSLab Team
 * @version 2025
 */
@Slf4j
public class NDLookupTable extends Block {

    // === Static Parameter Definitions ===
    public static final Map<String, String> PARAMETER_DEFAULTS;
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("NumberOfTableDimensions", "1");
        PARAMETER_DEFAULTS.put("BreakpointsSpecification", "Explicit values");
        PARAMETER_DEFAULTS.put("BreakpointsForDimension1", "[0 1]");
        PARAMETER_DEFAULTS.put("BreakpointsForDimension2", "[-1 1]");
        PARAMETER_DEFAULTS.put("Table", "[0 1]");
        PARAMETER_DEFAULTS.put("InterpMethod", "Linear point-slope");
        PARAMETER_DEFAULTS.put("ExtrapMethod", "Clip");
        PARAMETER_DEFAULTS.put("SampleTime", "-1");
        PARAMETER_DEFAULTS.put("OutDataTypeStr", "Inherit: Same as first input");
    }

    // === SIMULINK-Compatible Parameters ===
    @Getter
    private Parameter numberOfTableDimensions;

    @Getter
    private Parameter breakpointsSpecification;

    @Getter
    private Parameter breakpointsForDimension1;

    @Getter
    private Parameter breakpointsForDimension2;

    @Getter
    private Parameter breakpointsForDimension3;

    @Getter
    private Parameter breakpointsForDimension4;

    @Getter
    private Parameter table;

    @Getter
    private Parameter interpMethod;

    @Getter
    private Parameter extrapMethod;

    @Getter
    private Parameter sampleTime;

    @Getter
    private Parameter outDataTypeStr;

    // === Parsed Data Arrays ===
    private double[] breakpoints1 = null;
    private double[] breakpoints2 = null;
    private double[] breakpoints3 = null;
    private double[] breakpoints4 = null;
    private double[] tableData1D = null;
    private double[][] tableData2D = null;

    // === Operational Variables ===
    @Getter
    private int numDimensions;

    // === Legacy Constructor (JSONObject) ===
    @Deprecated
    public NDLookupTable(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);
        parseParameters();
        initializePorts();
    }

    // === DTO-NATIVE Constructor ===
    public NDLookupTable(BlockDto blockDto, NCSLabModel model) {
        super(blockDto, model);
        parseParameters();
        initializePorts();
        log.debug("DTO-NATIVE: NDLookupTable block created successfully - {}", blockDto.getBlockName());
    }

    // === Parameter Parsing ===
    protected void parseParameters() {
        // Get parameters using name-based access
        numberOfTableDimensions = getParameterByName("NumberOfTableDimensions");
        breakpointsSpecification = getParameterByName("BreakpointsSpecification");
        breakpointsForDimension1 = getParameterByName("BreakpointsForDimension1");
        breakpointsForDimension2 = getParameterByName("BreakpointsForDimension2");
        breakpointsForDimension3 = getParameterByName("BreakpointsForDimension3");
        breakpointsForDimension4 = getParameterByName("BreakpointsForDimension4");
        table = getParameterByName("Table");
        interpMethod = getParameterByName("InterpMethod");
        extrapMethod = getParameterByName("ExtrapMethod");
        sampleTime = getParameterByName("SampleTime");
        outDataTypeStr = getParameterByName("OutDataTypeStr");

        // Parse number of dimensions
        numDimensions = 1;
        if (numberOfTableDimensions != null) {
            try {
                numDimensions = Integer.parseInt(numberOfTableDimensions.getDataString().trim());
            } catch (NumberFormatException e) {
                log.warn("Failed to parse NumberOfTableDimensions, defaulting to 1: {}", e.getMessage());
                numDimensions = 1;
            }
        }

        // Parse breakpoints and table data
        try {
            if (breakpointsForDimension1 != null) {
                breakpoints1 = parseMatlabVector(breakpointsForDimension1.getDataString());
            }

            if (numDimensions >= 2 && breakpointsForDimension2 != null) {
                breakpoints2 = parseMatlabVector(breakpointsForDimension2.getDataString());
            }

            if (numDimensions >= 3 && breakpointsForDimension3 != null) {
                breakpoints3 = parseMatlabVector(breakpointsForDimension3.getDataString());
            }

            if (numDimensions >= 4 && breakpointsForDimension4 != null) {
                breakpoints4 = parseMatlabVector(breakpointsForDimension4.getDataString());
            }

            // Parse table data
            if (table != null) {
                if (numDimensions == 1) {
                    tableData1D = parseMatlabVector(table.getDataString());
                } else if (numDimensions == 2) {
                    tableData2D = parseMatlabMatrix(table.getDataString());
                }
                // For higher dimensions, would need additional parsing logic
            }
        } catch (Exception e) {
            log.error("Error parsing lookup table data: {}", e.getMessage(), e);
        }
    }

    // === Port Initialization ===
    private void initializePorts() {
        // Create input ports - one per dimension
        for (int i = 0; i < numDimensions; i++) {
            inputPortList.add(new InputPort(this, i + 1));
        }

        // Create single output port with feedthrough
        outputPortList.add(new OutputPort(this, 1, true));
    }

    // === MATLAB Vector/Matrix Parsing ===
    protected double[] parseMatlabVector(String vecString) {
        // Remove brackets and handle spacing
        vecString = vecString.replace("[", "").replace("]", "").trim();

        // Handle colon notation (e.g., "0:5" or "0:0.5:5")
        if (vecString.contains(":")) {
            return parseColonNotation(vecString);
        }

        // Handle space or comma separated values
        String regEx = "[' ']+"; // One or more spaces
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(vecString);
        JSONArray numArray = new JSONArray(m.replaceAll(",").trim());

        double[] arr = new double[numArray.length()];
        for (int i = 0; i < numArray.length(); i++) {
            arr[i] = numArray.getDouble(i);
        }
        return arr;
    }

    protected double[][] parseMatlabMatrix(String matrixString) {
        // Remove outer brackets
        matrixString = matrixString.replace("[", "").replace("]", "");
        // Split by semicolon for rows
        String[] rows = matrixString.split(";");

        double[][] matrix = new double[rows.length][];
        for (int i = 0; i < rows.length; i++) {
            String row = "[" + rows[i].trim() + "]";
            matrix[i] = parseMatlabVector(row);
        }
        return matrix;
    }

    protected double[] parseColonNotation(String colonStr) {
        String[] parts = colonStr.split(":");
        if (parts.length == 2) {
            // Format: start:end (step = 1)
            double start = Double.parseDouble(parts[0].trim());
            double end = Double.parseDouble(parts[1].trim());
            double step = (end >= start) ? 1.0 : -1.0;
            return generateRange(start, step, end);
        } else if (parts.length == 3) {
            // Format: start:step:end
            double start = Double.parseDouble(parts[0].trim());
            double step = Double.parseDouble(parts[1].trim());
            double end = Double.parseDouble(parts[2].trim());
            return generateRange(start, step, end);
        }
        return new double[]{0.0};
    }

    protected double[] generateRange(double start, double step, double end) {
        int numElements = (int) Math.floor((end - start) / step) + 1;
        double[] result = new double[numElements];
        for (int i = 0; i < numElements; i++) {
            result[i] = start + i * step;
        }
        return result;
    }

    // === Helper Method for Table Name ===
    protected String getTableName() {
        return "block" + getBlockId() + "_table";
    }

    // === Code Generation - C ===
    @Override
    public void generateArraysCodeC(CodeStructC code) {
        super.generateArraysCodeC(code);

        context.put("block", this);
        context.put("tableName", getTableName());

        String codeStr = TemplateManager.renderTemplate("c/lookup/NDLookupTable/arrays.vm", context);
        code.addArraysCode(codeStr);
    }

    @Override
    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);

        context.put("block", this);
        context.put("tableName", getTableName());
        context.put("numDimensions", numDimensions);

        // Add dimension-specific data
        if (numDimensions == 1) {
            context.put("breakpoints1", breakpoints1);
            context.put("breakpoints1Length", breakpoints1 != null ? breakpoints1.length : 0);
            context.put("tableData1D", tableData1D);
        } else if (numDimensions == 2) {
            context.put("breakpoints1", breakpoints1);
            context.put("breakpoints2", breakpoints2);
            context.put("breakpoints1Length", breakpoints1 != null ? breakpoints1.length : 0);
            context.put("breakpoints2Length", breakpoints2 != null ? breakpoints2.length : 0);
            context.put("tableData2D", tableData2D);
        }

        String codeStr = TemplateManager.renderTemplate("c/lookup/NDLookupTable/init.vm", context);
        code.addInitCode(codeStr);
    }

    @Override
    public void generateOutputCodeC(CodeStructC code) {
        super.generateOutputCodeC(code);

        // Populate all standard template variables
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        context.put("tableName", getTableName());
        context.put("numDimensions", numDimensions);

        String codeStr = TemplateManager.renderTemplate("c/lookup/NDLookupTable/output.vm", context);
        code.addOutputCode(codeStr);
    }

    // === Code Generation - MATLAB ===
    @Override
    public void generateInitCodeM(CodeStructM code) {
        super.generateInitCodeM(code);
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        context.put("tableName", getTableName());
        context.put("numDimensions", numDimensions);

        String codeStr = TemplateManager.renderTemplate("m/lookup/NDLookupTable/init.vm", context);
        code.addInitCode(codeStr);
    }

    @Override
    public void generateOutputCodeM(CodeStructM code) {
        super.generateOutputCodeM(code);
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        context.put("tableName", getTableName());
        context.put("numDimensions", numDimensions);

        String codeStr = TemplateManager.renderTemplate("m/lookup/NDLookupTable/output.vm", context);
        code.addOutputCode(codeStr);
    }

    // === Dimension Checking ===
    @Override
    public void checkDimension() throws MatDimException {
        // Check that all input ports are connected
        for (int i = 0; i < numDimensions; i++) {
            if (inputPortList.get(i).getLinkedLine() == null) {
                throw new MatDimException(
                    "Input port " + (i + 1) + " of block n-D Lookup Table:(" +
                    getBlockId() + ")" + getBlockName() + " is not connected"
                );
            }
        }

        // Validate table dimensions match breakpoints
        if (numDimensions == 1 && breakpoints1 != null && tableData1D != null) {
            if (breakpoints1.length != tableData1D.length) {
                throw new MatDimException(
                    "Dimension of table of block n-D Lookup Table:(" + getBlockId() + ")" +
                    getBlockName() + " does not match breakpoints (expected " +
                    breakpoints1.length + ", got " + tableData1D.length + ")"
                );
            }
        } else if (numDimensions == 2 && breakpoints1 != null && breakpoints2 != null && tableData2D != null) {
            if (breakpoints1.length != tableData2D.length ||
                breakpoints2.length != tableData2D[0].length) {
                throw new MatDimException(
                    "Dimension of table of block n-D Lookup Table:(" + getBlockId() + ")" +
                    getBlockName() + " does not match breakpoints"
                );
            }
        }
    }

    @Override
    public void updateDimension() throws MatDimException {
        // Output is always scalar for lookup table
        OutputPort out = outputPortList.get(0);
        out.setHeight(1);
        out.setWidth(1);
        out.getOutputSignalC().setHeight(1);
        out.getOutputSignalC().setWidth(1);
        out.getOutputSignalC().setDataType(DataType.REAL);
    }

    // === Runtime Simulation API ===
    @Override
    public void calculateInit() {
        performLookup();
    }

    @Override
    public void calculateOutput(double t) {
        performLookup();
    }

    private void performLookup() {
        OutputPort out = outputPortList.get(0);

        if (numDimensions == 1 && breakpoints1 != null && tableData1D != null) {
            // 1-D interpolation
            double input = inputPortList.get(0).getData().getInitValue();
            double result = interpolate1D(input, breakpoints1, tableData1D);
            out.setData(new Data(result));
        } else if (numDimensions == 2 && breakpoints1 != null && breakpoints2 != null && tableData2D != null) {
            // 2-D interpolation
            double input1 = inputPortList.get(0).getData().getInitValue();
            double input2 = inputPortList.get(1).getData().getInitValue();
            double result = interpolate2D(input1, input2, breakpoints1, breakpoints2, tableData2D);
            out.setData(new Data(result));
        } else {
            // Default output
            out.setData(new Data(0.0));
        }
    }

    // === Interpolation Methods ===
    private double interpolate1D(double x, double[] xData, double[] yData) {
        // Handle edge cases
        if (x <= xData[0]) {
            return handleExtrapolation(x, xData[0], xData[1], yData[0], yData[1], true);
        }
        if (x >= xData[xData.length - 1]) {
            int n = xData.length - 1;
            return handleExtrapolation(x, xData[n - 1], xData[n], yData[n - 1], yData[n], false);
        }

        // Find bracketing indices
        for (int i = 0; i < xData.length - 1; i++) {
            if (x >= xData[i] && x <= xData[i + 1]) {
                // Linear interpolation
                double t = (x - xData[i]) / (xData[i + 1] - xData[i]);
                return yData[i] + t * (yData[i + 1] - yData[i]);
            }
        }

        return yData[0]; // Fallback
    }

    private double interpolate2D(double x, double y, double[] xData, double[] yData, double[][] zData) {
        // Find x bracketing indices
        int xi = findInterval(x, xData);
        int yi = findInterval(y, yData);

        // Bilinear interpolation
        double x0 = xData[xi];
        double x1 = xData[Math.min(xi + 1, xData.length - 1)];
        double y0 = yData[yi];
        double y1 = yData[Math.min(yi + 1, yData.length - 1)];

        double z00 = zData[xi][yi];
        double z01 = zData[xi][Math.min(yi + 1, yData.length - 1)];
        double z10 = zData[Math.min(xi + 1, xData.length - 1)][yi];
        double z11 = zData[Math.min(xi + 1, xData.length - 1)][Math.min(yi + 1, yData.length - 1)];

        double tx = (x1 != x0) ? (x - x0) / (x1 - x0) : 0.0;
        double ty = (y1 != y0) ? (y - y0) / (y1 - y0) : 0.0;

        double z0 = z00 + tx * (z10 - z00);
        double z1 = z01 + tx * (z11 - z01);

        return z0 + ty * (z1 - z0);
    }

    private int findInterval(double value, double[] data) {
        for (int i = 0; i < data.length - 1; i++) {
            if (value >= data[i] && value <= data[i + 1]) {
                return i;
            }
        }
        return Math.max(0, data.length - 2);
    }

    private double handleExtrapolation(double x, double x0, double x1, double y0, double y1, boolean below) {
        String extrapMethodStr = extrapMethod != null ? extrapMethod.getDataString() : "Clip";

        if ("Clip".equalsIgnoreCase(extrapMethodStr)) {
            return below ? y0 : y1;
        } else {
            // Linear extrapolation
            double slope = (y1 - y0) / (x1 - x0);
            return below ? y0 + slope * (x - x0) : y1 + slope * (x - x1);
        }
    }
}
