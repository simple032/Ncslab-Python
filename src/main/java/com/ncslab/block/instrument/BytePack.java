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
import com.ncslab.dto.block.specialized.data.BytePackDto;
import com.ncslab.ncslablink.BlockCreationException;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.util.TemplateManager;
import org.json.JSONObject;
import Jama.Matrix;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Byte Pack block - converts input signals of one or more data types to a uint8 vector output.
 *
 * This block is used to pack data for efficient transmission over serial communication,
 * UDP, or other protocols that require byte-level data formatting.
 *
 * SIMULINK Parameters:
 * - datatypes: Cell array of input data types (e.g., "{'uint32','uint16','double'}")
 * - byteAlign: Byte alignment for packed data (1, 2, 4, or 8 bytes, default: "1")
 *
 * Supported data types: single, double, int8, int16, int32, int64,
 *                       uint8, uint16, uint32, uint64, Boolean
 *
 * Example: With datatypes={'uint32','uint16','double'} and byteAlign=1,
 *          the block packs 4+2+8=14 bytes into the output vector
 */
public class BytePack extends Block {
    private final Parameter datatypes;
    private final Parameter byteAlign;

    // === Static Parameter Definitions ===
    public static final Map<String, String> PARAMETER_DEFAULTS;
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("datatypes", "{'uint8'}");
        PARAMETER_DEFAULTS.put("byteAlign", "1");
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

        // Output port defaults (byte pack has feedthrough)
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
    public BytePack(JSONObject blockIn, NCSLabModel model) {
        super(blockIn, model);

        this.datatypes = getParameterByName("datatypes");
        this.byteAlign = getParameterByName("byteAlign");

        // Add single output port for uint8 vector (byte array)
        outputPortList.add(new OutputPort(this, 1, true)); // Has feedthrough

        // Parse datatypes to determine number of input ports
        int numInputs = parseDataTypesCount(this.datatypes);
        for (int i = 0; i < numInputs; i++) {
            inputPortList.add(new InputPort(this, i + 1));
        }
    }

    /**
     * DTO constructor
     */
    public BytePack(BytePackDto dto, NCSLabModel model) {
        super(dto, model);

        this.datatypes = getParameterByName("datatypes");
        this.byteAlign = getParameterByName("byteAlign");

        // Add single output port for uint8 vector
        outputPortList.add(new OutputPort(this, 1, true));

        // Parse datatypes to determine number of input ports
        int numInputs = parseDataTypesCount(this.datatypes);
        for (int i = 0; i < numInputs; i++) {
            inputPortList.add(new InputPort(this, i + 1));
        }
    }

    @Override
    public void updateDimension() throws MatDimException {
        if (!outputPortList.isEmpty()) {
            OutputPort out = outputPortList.get(0);

            // Calculate total bytes needed based on actual input dimensions
            int totalBytes = calculateTotalBytesFromInputs();

            System.out.println("BytePack updateDimension: totalBytes=" + totalBytes +
                             ", blockId=" + this.blockId + ", blockName=" + this.blockName);

            // Output is a uint8 vector (column vector as per SIMULINK convention: N x 1)
            out.setHeight(totalBytes);
            out.setWidth(1);
            out.getOutputSignalC().setHeight(totalBytes);
            out.getOutputSignalC().setWidth(1);
            // setHeight() should automatically set dataType to MATRIX when totalBytes > 1
            // But explicitly set it to ensure correct behavior
            out.getOutputSignalC().setDataType(totalBytes > 1 ? DataType.MATRIX : DataType.REAL);

            System.out.println("BytePack after setDataType: dataType=" + out.getOutputSignalC().getDataType() +
                             ", height=" + out.getOutputSignalC().getHeight() +
                             ", width=" + out.getOutputSignalC().getWidth());
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
        // Byte pack initialization if needed
    }

    @Override
    public void generateOutputCodeC(CodeStructC code) {
        super.generateOutputCodeC(code);
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);
        com.ncslab.util.TemplateUtils.populatePortDataTypeContext(context, this);

        String codeStr = TemplateManager.renderTemplate("c/instrument/BytePack/output.vm", context);
        code.addOutputCode(codeStr);
    }

    @Override
    public void generateOutputCodeM(CodeStructM code) {
        super.generateOutputCodeM(code);
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);
        com.ncslab.util.TemplateUtils.populatePortDataTypeContext(context, this);

        String codeStr = TemplateManager.renderTemplate("m/instrument/BytePack/output.vm", context);
        code.addOutputCode(codeStr);
    }

    @Override
    public void calculateOutput(double t) {
        // Java simulation: pack input data to bytes
        if (!outputPortList.isEmpty()) {
            OutputPort out = outputPortList.get(0);
            int totalBytes = calculateTotalBytesFromInputs();

            // Create byte array matrix as column vector (totalBytes x 1)
            Matrix byteMatrix = new Matrix(totalBytes, 1);
            int byteOffset = 0;

            // Pack each input into the byte array by converting to individual bytes
            for (InputPort inputPort : inputPortList) {
                if (inputPort.getData() != null && inputPort.getLinkedLine() != null) {
                    Data inputData = inputPort.getData();
                    OutputSignal inputSignal = inputPort.getLinkedLine().getLinkedOutputPort().getOutputSignalC();
                    CDataType cType = inputSignal.getCDataType();

                    // Pack data based on type
                    if (inputData.getDataType() == DataType.REAL) {
                        double value = inputData.getInitValue();
                        byteOffset = packValueToBytes(byteMatrix, byteOffset, value, cType);
                    } else if (inputData.getDataType() == DataType.MATRIX) {
                        Matrix inputMatrix = inputData.getMatrix();
                        for (int i = 0; i < inputMatrix.getRowDimension(); i++) {
                            for (int j = 0; j < inputMatrix.getColumnDimension(); j++) {
                                double value = inputMatrix.get(i, j);
                                byteOffset = packValueToBytes(byteMatrix, byteOffset, value, cType);
                            }
                        }
                    }
                }
            }

            out.setData(new Data(byteMatrix));
        }
    }

    /**
     * Pack a value into individual bytes in the byte matrix (column vector format)
     * Uses little-endian byte order (LSB first)
     */
    private int packValueToBytes(Matrix byteMatrix, int offset, double value, CDataType cType) {
        long longValue;
        int numBytes = cType.getByteSize();

        // Convert to appropriate integer representation
        switch (cType) {
            case INT8:
                longValue = (byte) Math.round(value);
                break;
            case INT16:
                longValue = (short) Math.round(value);
                break;
            case INT32:
                longValue = (int) Math.round(value);
                break;
            case INT64:
                longValue = (long) Math.round(value);
                break;
            case UINT8:
                longValue = ((int) Math.round(value)) & 0xFF;
                break;
            case UINT16:
                longValue = ((int) Math.round(value)) & 0xFFFF;
                break;
            case UINT32:
                longValue = ((long) Math.round(value)) & 0xFFFFFFFFL;
                break;
            case UINT64:
                longValue = (long) Math.round(value);
                break;
            case SINGLE:
                // For float, convert to int bits
                longValue = Float.floatToIntBits((float) value);
                break;
            case DOUBLE:
                // For double, convert to long bits
                longValue = Double.doubleToLongBits(value);
                break;
            case BOOLEAN:
                longValue = value != 0.0 ? 1 : 0;
                break;
            default:
                longValue = Double.doubleToLongBits(value);
        }

        // Pack bytes in little-endian order (LSB first) into column vector
        for (int i = 0; i < numBytes && offset < byteMatrix.getRowDimension(); i++) {
            int byteVal = (int) ((longValue >> (i * 8)) & 0xFF);
            byteMatrix.set(offset++, 0, byteVal);
        }

        return offset;
    }

    @Override
    public void calculateInit() {
        // Initialize byte packing - create zero-filled byte array as column vector
        if (!outputPortList.isEmpty()) {
            OutputPort out = outputPortList.get(0);
            int totalBytes = calculateTotalBytesFromInputs();

            // Create a zero-filled byte array represented as column vector matrix (totalBytes x 1)
            Matrix byteMatrix = new Matrix(totalBytes, 1); // height x width = totalBytes x 1
            for (int i = 0; i < totalBytes; i++) {
                byteMatrix.set(i, 0, 0.0);
            }

            out.setData(new Data(byteMatrix));
        }
    }

    /**
     * Parse datatypes parameter to count number of inputs
     * Example: "{'uint32','uint16','double'}" -> 3 inputs
     */
    private int parseDataTypesCount(Parameter datatypes) {
        if (datatypes == null) {
            return 1; // Default to single input
        }

        String dtString = datatypes.getInitString();
        if (dtString == null || dtString.isEmpty()) {
            return 1;
        }

        // Count comma-separated types in cell array format
        // Remove curly braces and count commas
        dtString = dtString.replace("{", "").replace("}", "").replace("'", "");
        if (dtString.trim().isEmpty()) {
            return 1;
        }

        String[] types = dtString.split(",");
        return types.length;
    }

    /**
     * Calculate total bytes needed based on actual input signal dimensions and C data types
     */
    private int calculateTotalBytesFromInputs() {
        int totalBytes = 0;

        int alignment = 1; // Default alignment
        if (byteAlign != null) {
            try {
                alignment = Integer.parseInt(byteAlign.getInitString());
            } catch (NumberFormatException e) {
                alignment = 1;
            }
        }

        // Calculate bytes based on actual input dimensions and their C data types
        for (InputPort inputPort : inputPortList) {
            if (inputPort.getLinkedLine() != null &&
                inputPort.getLinkedLine().getLinkedOutputPort() != null) {

                OutputSignal inputSignal = inputPort.getLinkedLine().getLinkedOutputPort().getOutputSignalC();
                int inputHeight = inputSignal.getHeight();
                int inputWidth = inputSignal.getWidth();

                // Get actual C data type size (int16 = 2 bytes, double = 8 bytes, etc.)
                CDataType cType = inputSignal.getCDataType();
                int elementSize = cType.getByteSize();

                int numElements = inputHeight * inputWidth;
                int inputBytes = elementSize * numElements;

                // Apply alignment padding before adding this input
                if (alignment > 1 && totalBytes % alignment != 0) {
                    totalBytes += alignment - (totalBytes % alignment);
                }

                totalBytes += inputBytes;
            }
        }

        // If no inputs connected, use parameter-based calculation
        if (totalBytes == 0) {
            totalBytes = calculateTotalBytesFromParameter();
        }

        return Math.max(totalBytes, 1); // At least 1 byte
    }

    /**
     * Calculate total bytes needed based on datatypes parameter (fallback)
     */
    private int calculateTotalBytesFromParameter() {
        if (datatypes == null) {
            return 4; // Default size
        }

        String dtString = datatypes.getInitString();
        if (dtString == null || dtString.isEmpty()) {
            return 4;
        }

        int alignment = 1; // Default alignment
        if (byteAlign != null) {
            try {
                alignment = Integer.parseInt(byteAlign.getInitString());
            } catch (NumberFormatException e) {
                alignment = 1;
            }
        }

        // Parse data types and calculate bytes
        dtString = dtString.replace("{", "").replace("}", "").replace("'", "").replace(" ", "");
        String[] types = dtString.split(",");

        int totalBytes = 0;
        for (String type : types) {
            int typeSize = getDataTypeSize(type.trim());

            // Apply alignment padding
            if (alignment > 1 && totalBytes % alignment != 0) {
                totalBytes += alignment - (totalBytes % alignment);
            }

            totalBytes += typeSize;
        }

        return totalBytes;
    }

    /**
     * Get size in bytes for a data type string
     */
    private int getDataTypeSize(String dataType) {
        switch (dataType.toLowerCase()) {
            case "int8":
            case "uint8":
            case "boolean":
                return 1;
            case "int16":
            case "uint16":
                return 2;
            case "int32":
            case "uint32":
            case "single":
                return 4;
            case "int64":
            case "uint64":
            case "double":
                return 8;
            default:
                return 4; // Default to 4 bytes
        }
    }
}
