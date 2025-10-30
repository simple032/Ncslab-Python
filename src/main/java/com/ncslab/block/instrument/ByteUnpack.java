package com.ncslab.block.instrument;

import com.ncslab.block.Block;
import com.ncslab.block.data.Data;
import com.ncslab.block.data.DataType;
import com.ncslab.block.data.CDataType;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.data.ByteUnpackDto;
import com.ncslab.ncslablink.BlockCreationException;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.util.TemplateManager;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import Jama.Matrix;

/**
 * Byte Unpack block - converts uint8 vector to output signals of different data types.
 *
 * This block is the reverse operation of Byte Pack, used to unpack byte data received
 * from serial communication, UDP, or other byte-level protocols.
 *
 * SIMULINK Parameters:
 * - datatypes: Cell array of output data types (e.g., "{'uint32','uint16','double'}")
 * - byteAlign: Byte alignment matching the Byte Pack block (1, 2, 4, or 8 bytes)
 * - dimensions: Cell array of output dimensions (e.g., "{1, 1, [2,4]}")
 *
 * Supported data types: single, double, int8, int16, int32, int64,
 *                       uint8, uint16, uint32, uint64, Boolean
 *
 * Example: With datatypes={'uint32','uint16','double'} and byteAlign=1,
 *          the block unpacks 14 bytes (4+2+8) into 3 output signals
 */
public class ByteUnpack extends Block {
    private final Parameter datatypes;
    private final Parameter byteAlign;
    private final Parameter dimensions;

    // === Static Parameter Definitions ===
    public static final Map<String, String> PARAMETER_DEFAULTS;
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("datatypes", "{'uint8'}");
        PARAMETER_DEFAULTS.put("byteAlign", "1");
        PARAMETER_DEFAULTS.put("dimensions", "{[1]}");
    }

    public static final List<String> outputNames = new ArrayList<>();
    public static final List<String> inputNames = new ArrayList<>();

    // Port defaults for centralized initialization
    public static final List<Map<String, Object>> INPUT_PORT_DEFAULTS;
    public static final List<Map<String, Object>> OUTPUT_PORT_DEFAULTS;

    static {
        // Port names
        outputNames.add("out1");
        inputNames.add("in1");

        // Input port defaults
        INPUT_PORT_DEFAULTS = new ArrayList<>();
        Map<String, Object> input1 = new HashMap<>();
        input1.put("name", "in1");
        input1.put("width", 1);
        input1.put("height", 1);
        input1.put("dataType", "REAL");
        INPUT_PORT_DEFAULTS.add(input1);

        // Output port defaults (byte unpack has feedthrough)
        OUTPUT_PORT_DEFAULTS = new ArrayList<>();
        Map<String, Object> output1 = new HashMap<>();
        output1.put("name", "out1");
        output1.put("width", 1);
        output1.put("height", 1);
        output1.put("dataType", "REAL");
        output1.put("feedthrough", true);
        OUTPUT_PORT_DEFAULTS.add(output1);
    }

    /**
     * Legacy constructor from JSONObject
     */
    public ByteUnpack(JSONObject blockIn, NCSLabModel model) {
        super(blockIn, model);

        this.datatypes = getParameterByName("datatypes");
        this.byteAlign = getParameterByName("byteAlign");
        this.dimensions = getParameterByName("dimensions");

        // Single input port for uint8 vector (byte array)
        inputPortList.add(new InputPort(this, 1));

        // Parse datatypes to determine number of output ports
        int numOutputs = parseDataTypesCount(this.datatypes);
        for (int i = 0; i < numOutputs; i++) {
            outputPortList.add(new OutputPort(this, i + 1, true)); // Has feedthrough
        }
    }

    /**
     * DTO constructor
     */
    public ByteUnpack(ByteUnpackDto dto, NCSLabModel model) {
        super(dto, model);

        this.datatypes = getParameterByName("datatypes");
        this.byteAlign = getParameterByName("byteAlign");
        this.dimensions = getParameterByName("dimensions");

        // Single input port for uint8 vector
        inputPortList.add(new InputPort(this, 1));

        // Parse datatypes to determine number of output ports
        int numOutputs = parseDataTypesCount(this.datatypes);
        for (int i = 0; i < numOutputs; i++) {
            outputPortList.add(new OutputPort(this, i + 1, true));
        }
    }

    /**
     * Generic DTO constructor for factory compatibility
     */
    public ByteUnpack(BlockDto blockDto, NCSLabModel model) {
        this((ByteUnpackDto) blockDto, model);
    }

    @Override
    public void updateDimension() throws MatDimException {
        // Parse datatypes and dimensions to set output port dimensions
        String[] dataTypes = parseDataTypes();
        String[] dims = parseDimensions();

        for (int i = 0; i < outputPortList.size() && i < dataTypes.length; i++) {
            OutputPort out = outputPortList.get(i);

            // Parse dimensions for this output
            int height = 1;
            int width = 1;
            if (i < dims.length) {
                int[] dimArray = parseDimensionString(dims[i]);
                if (dimArray.length > 0) height = dimArray[0];
                if (dimArray.length > 1) width = dimArray[1];
            }

            out.setHeight(height);
            out.setWidth(width);
            out.getOutputSignalC().setHeight(height);
            out.getOutputSignalC().setWidth(width);

            // Set data type based on dimensions
            if (height > 1 || width > 1) {
                out.getOutputSignalC().setDataType(DataType.MATRIX);
            } else {
                out.getOutputSignalC().setDataType(DataType.REAL);
            }
        }
    }

    @Override
    public void checkDimension() throws MatDimException {
        // No dimension constraints
    }

    @Override
    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);
        // Byte unpack initialization if needed
    }

    @Override
    public void generateOutputCodeC(CodeStructC code) {
        super.generateOutputCodeC(code);
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);
        com.ncslab.util.TemplateUtils.populatePortDataTypeContext(context, this);

        String codeStr = TemplateManager.renderTemplate("c/instrument/ByteUnpack/output.vm", context);
        code.addOutputCode(codeStr);
    }

    @Override
    public void generateOutputCodeM(CodeStructM code) {
        super.generateOutputCodeM(code);
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);
        com.ncslab.util.TemplateUtils.populatePortDataTypeContext(context, this);

        String codeStr = TemplateManager.renderTemplate("m/instrument/ByteUnpack/output.vm", context);
        code.addOutputCode(codeStr);
    }

    @Override
    public void calculateOutput(double t) {
        // Java simulation: unpack bytes to data
        if (!inputPortList.isEmpty() && !outputPortList.isEmpty()) {
            InputPort in = inputPortList.get(0);

            if (in.getData() != null && in.getData().getDataType() == DataType.MATRIX) {
                Matrix byteMatrix = in.getData().getMatrix();
                int byteOffset = 0;

                // Unpack byte array into each output using actual CDataType
                for (OutputPort outPort : outputPortList) {
                    CDataType cType = outPort.getOutputSignalC().getCDataType();
                    int numBytes = cType.getByteSize();

                    if (byteOffset + numBytes <= byteMatrix.getRowDimension()) {
                        // Unpack bytes into value
                        double value = unpackBytesToValue(byteMatrix, byteOffset, cType);
                        outPort.setData(new Data(value));
                        byteOffset += numBytes;
                    } else {
                        outPort.setData(new Data(0.0));
                    }
                }
            } else {
                // Fallback: set all outputs to zero
                for (OutputPort outPort : outputPortList) {
                    outPort.setData(new Data(0.0));
                }
            }
        }
    }

    /**
     * Unpack bytes into a value based on C data type
     * Assumes little-endian byte order (LSB first)
     */
    private double unpackBytesToValue(Matrix byteMatrix, int offset, CDataType cType) {
        long longValue = 0;
        int numBytes = cType.getByteSize();

        // Read bytes in little-endian order (LSB first)
        for (int i = 0; i < numBytes && offset + i < byteMatrix.getRowDimension(); i++) {
            long byteVal = (long) byteMatrix.get(offset + i, 0) & 0xFF;
            longValue |= (byteVal << (i * 8));
        }

        // Convert based on data type
        switch (cType) {
            case INT8:
                return (double) (byte) longValue;
            case INT16:
                return (double) (short) longValue;
            case INT32:
                return (double) (int) longValue;
            case INT64:
                return (double) longValue;
            case UINT8:
                return (double) (longValue & 0xFF);
            case UINT16:
                return (double) (longValue & 0xFFFF);
            case UINT32:
                return (double) (longValue & 0xFFFFFFFFL);
            case UINT64:
                return (double) longValue; // May lose precision for large values
            case SINGLE:
                return (double) Float.intBitsToFloat((int) longValue);
            case DOUBLE:
                return Double.longBitsToDouble(longValue);
            case BOOLEAN:
                return longValue != 0 ? 1.0 : 0.0;
            default:
                return Double.longBitsToDouble(longValue);
        }
    }

    @Override
    public void calculateInit() {
        // Initialize byte unpacking - create zero outputs
        for (OutputPort out : outputPortList) {
            out.setData(new Data(0.0));
        }
    }

    /**
     * Parse datatypes parameter to count number of outputs
     * Example: "{'uint32','uint16','double'}" -> 3 outputs
     */
    private int parseDataTypesCount(Parameter datatypes) {
        if (datatypes == null) {
            return 1; // Default to single output
        }

        String dtString = datatypes.getInitString();
        if (dtString == null || dtString.isEmpty()) {
            return 1;
        }

        // Count comma-separated types in cell array format
        dtString = dtString.replace("{", "").replace("}", "").replace("'", "");
        if (dtString.trim().isEmpty()) {
            return 1;
        }

        String[] types = dtString.split(",");
        return types.length;
    }

    /**
     * Parse datatypes into array of type strings
     */
    private String[] parseDataTypes() {
        if (datatypes == null) {
            return new String[]{"double"};
        }

        String dtString = datatypes.getInitString();
        if (dtString == null || dtString.isEmpty()) {
            return new String[]{"double"};
        }

        // Parse cell array format: {'uint32','uint16','double'}
        dtString = dtString.replace("{", "").replace("}", "").replace("'", "").replace(" ", "");
        if (dtString.trim().isEmpty()) {
            return new String[]{"double"};
        }

        return dtString.split(",");
    }

    /**
     * Parse dimensions parameter into array of dimension strings
     */
    private String[] parseDimensions() {
        if (dimensions == null) {
            return new String[]{"1"};
        }

        String dimString = dimensions.getInitString();
        if (dimString == null || dimString.isEmpty()) {
            return new String[]{"1"};
        }

        // Parse cell array format: {1, 1, [2,4]}
        // Simplified parsing for now
        dimString = dimString.replace("{", "").replace("}", "");

        // Split by comma, handling array notation
        List<String> dimList = new ArrayList<>();
        int depth = 0;
        StringBuilder current = new StringBuilder();

        for (char c : dimString.toCharArray()) {
            if (c == '[') depth++;
            if (c == ']') depth--;

            if (c == ',' && depth == 0) {
                dimList.add(current.toString().trim());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        if (current.length() > 0) {
            dimList.add(current.toString().trim());
        }

        return dimList.toArray(new String[0]);
    }

    /**
     * Parse a single dimension string like "1" or "[2,4]" into int array
     */
    private int[] parseDimensionString(String dimStr) {
        if (dimStr == null || dimStr.isEmpty()) {
            return new int[]{1};
        }

        dimStr = dimStr.replace("[", "").replace("]", "").trim();

        if (dimStr.contains(",")) {
            String[] parts = dimStr.split(",");
            int[] result = new int[parts.length];
            for (int i = 0; i < parts.length; i++) {
                try {
                    result[i] = Integer.parseInt(parts[i].trim());
                } catch (NumberFormatException e) {
                    result[i] = 1;
                }
            }
            return result;
        } else {
            try {
                return new int[]{Integer.parseInt(dimStr)};
            } catch (NumberFormatException e) {
                return new int[]{1};
            }
        }
    }
}
