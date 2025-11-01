package com.ncslab.block.comm;

import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.comm.SerialBidirectionalDto;
import com.ncslab.block.Block;
import com.ncslab.block.data.Data;
import com.ncslab.block.data.DataType;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.util.TemplateManager;
import com.fazecast.jSerialComm.SerialPort;
import lombok.extern.slf4j.Slf4j;
import Jama.Matrix;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.nio.ByteBuffer;

/**
 * SerialBidirectional block for bidirectional serial communication (RS232/UART).
 * Combines both send and receive functionality in a single block.
 * Supports configurable baud rate, data bits, stop bits, and parity.
 * Implements AutoCloseable for proper resource management.
 */
@Slf4j
public class SerialBidirectional extends Block implements AutoCloseable {

    private String name = "SerialBidirectional";

    private String portName;
    private int baudRate;
    private int dataBits;
    private int stopBits;
    private String parity;

    // Serial port instance
    private SerialPort serialPort;

    // Buffer for received data
    private byte[] receiveBuffer = new byte[8]; // 8 bytes for double

    /**
     * DTO-NATIVE Constructor - Creates SerialBidirectional block directly from BlockDto DTO
     */
    public SerialBidirectional(SerialBidirectionalDto blockDto, NCSLabModel model) {
        super(blockDto, model);

        // Input port for TX data (can be scalar or vector)
        inputPortList.add(new InputPort(this, 1));

        // Output port for RX data (can be scalar or vector)
        outputPortList.add(new OutputPort(this, 1, false));

        System.out.println("DTO-NATIVE: SerialBidirectional block created successfully - " + blockDto.getBlockName());
    }

    public static final List<String> inputNames = new ArrayList<>();
    public static final List<String> outputNames = new ArrayList<>();

    public static final Map<String, String> PARAMETER_DEFAULTS = new HashMap<>();

    static {
        // Single input port for TX data (can be scalar or vector)
        inputNames.add("tx");

        // Single output port for RX data (can be scalar or vector)
        outputNames.add("rx");

        PARAMETER_DEFAULTS.put("PortName", "COM1");
        PARAMETER_DEFAULTS.put("BaudRate", "9600");
        PARAMETER_DEFAULTS.put("DataBits", "8");
        PARAMETER_DEFAULTS.put("StopBits", "1");
        PARAMETER_DEFAULTS.put("Parity", "None");
    }

    public SerialBidirectional(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);

        portName = paramValues.optString("PortName", "COM1");
        baudRate = paramValues.optInt("BaudRate", 9600);
        dataBits = paramValues.optInt("DataBits", 8);
        stopBits = paramValues.optInt("StopBits", 1);
        parity = paramValues.optString("Parity", "None");

        // Input port for TX data (can be scalar or vector)
        inputPortList.add(new InputPort(this, 1));

        // Output port for RX data (can be scalar or vector)
        outputPortList.add(new OutputPort(this, 1, false));
    }

    @Override
    public void calculateInit() {
        try {
            // Open serial port
            serialPort = SerialPort.getCommPort(portName);

            // Configure serial port parameters
            serialPort.setBaudRate(baudRate);
            serialPort.setNumDataBits(dataBits);
            serialPort.setNumStopBits(stopBits);

            // Set parity
            switch (parity.toLowerCase()) {
                case "none":
                    serialPort.setParity(SerialPort.NO_PARITY);
                    break;
                case "odd":
                    serialPort.setParity(SerialPort.ODD_PARITY);
                    break;
                case "even":
                    serialPort.setParity(SerialPort.EVEN_PARITY);
                    break;
                case "mark":
                    serialPort.setParity(SerialPort.MARK_PARITY);
                    break;
                case "space":
                    serialPort.setParity(SerialPort.SPACE_PARITY);
                    break;
                default:
                    serialPort.setParity(SerialPort.NO_PARITY);
                    log.warn("Unknown parity value: {}, using NO_PARITY", parity);
            }

            // Set timeouts - semi-blocking for read with 100ms timeout, blocking for write
            serialPort.setComPortTimeouts(
                SerialPort.TIMEOUT_READ_SEMI_BLOCKING | SerialPort.TIMEOUT_WRITE_BLOCKING,
                100, 1000);

            // Open the port
            if (serialPort.openPort()) {
                log.info("Serial port {} opened successfully for SerialBidirectional block {}",
                        portName, blockId);
            } else {
                log.error("Failed to open serial port {} for SerialBidirectional block {}",
                         portName, blockId);
            }
        } catch (Exception e) {
            log.error("Error initializing serial port {} for SerialBidirectional block {}: {}",
                     portName, blockId, e.getMessage(), e);
        }
    }

    @Override
    public void calculateOutput(double t) {
        if (serialPort == null || !serialPort.isOpen()) {
            log.warn("Serial port not open for SerialBidirectional block {}, skipping operation", blockId);
            return;
        }

        try {
            // SEND operation (TX)
            InputPort inputPort = inputPortList.get(0);
            Data inputData = inputPort.getData();

            // Handle both scalar and vector inputs for TX
            if (inputData.getDataType() == DataType.REAL) {
                // Scalar - single value
                double value = inputData.getInitValue();
                sendValue(value);
            } else {
                // Matrix/Vector - send all elements
                int height = inputData.getHeight();

                // Send as column vector (height x 1)
                for (int i = 0; i < height; i++) {
                    double value = inputData.getMatrix().get(i, 0);
                    sendValue(value);
                }
                log.debug("SerialBidirectional block {} sent {} values as vector on port {}",
                         blockId, height, portName);
            }

            // RECEIVE operation (RX)
            OutputPort outputPort = outputPortList.get(0);

            // Determine the number of values to receive based on output port dimensions
            // The width is determined by what is connected to the output port
            int rxWidth = outputPort.getHeight();
            if (rxWidth <= 0) {
                rxWidth = 1;  // Default to scalar if not determined yet
            }

            // Check if enough data is available (8 bytes per value)
            int requiredBytes = rxWidth * 8;
            int available = serialPort.bytesAvailable();

            if (available >= requiredBytes) {
                if (rxWidth == 1) {
                    // Scalar output - single value
                    double value = receiveValue();
                    outputPort.setData(new Data(value));
                } else {
                    // Vector output - multiple values (column vector)
                    Matrix matrix = new Matrix(rxWidth, 1);
                    for (int i = 0; i < rxWidth; i++) {
                        double value = receiveValue();
                        matrix.set(i, 0, value);
                    }
                    Data vectorData = new Data(matrix);
                    outputPort.setData(vectorData);

                    log.debug("SerialBidirectional block {} received {} values as vector from port {}",
                             blockId, rxWidth, portName);
                }
            }
        } catch (Exception e) {
            log.error("Error during bidirectional serial communication on port {} for block {}: {}",
                     portName, blockId, e.getMessage(), e);
        }
    }

    /**
     * Helper method to send a single double value
     */
    private void sendValue(double value) {
        try {
            // Convert double to bytes (8 bytes for double)
            ByteBuffer buffer = ByteBuffer.allocate(8);
            buffer.putDouble(value);
            byte[] data = buffer.array();

            // Write data to serial port
            int bytesWritten = serialPort.writeBytes(data, data.length);

            if (bytesWritten == data.length) {
                log.debug("SerialBidirectional block {} sent {} bytes (value: {}) on port {}",
                         blockId, bytesWritten, value, portName);
            } else {
                log.warn("SerialBidirectional block {} failed to send all bytes. Sent {}/{} bytes",
                        blockId, bytesWritten, data.length);
            }
        } catch (Exception e) {
            log.error("Error writing value {} to serial port {}: {}",
                     value, portName, e.getMessage(), e);
        }
    }

    /**
     * Helper method to receive a single double value
     */
    private double receiveValue() {
        try {
            // Read 8 bytes (double)
            int bytesRead = serialPort.readBytes(receiveBuffer, 8);

            if (bytesRead == 8) {
                // Convert bytes to double
                ByteBuffer buffer = ByteBuffer.wrap(receiveBuffer);
                double value = buffer.getDouble();

                log.debug("SerialBidirectional block {} received {} bytes (value: {}) from port {}",
                         blockId, bytesRead, value, portName);

                return value;
            } else {
                log.warn("SerialBidirectional block {} received incomplete data. Expected 8 bytes, got {} bytes",
                        blockId, bytesRead);
                return 0.0;
            }
        } catch (Exception e) {
            log.error("Error reading value from serial port {}: {}",
                     portName, e.getMessage(), e);
            return 0.0;
        }
    }

    @Override
    public void calculateDerivative(double t) {
        // No derivative calculation needed for SerialBidirectional
    }

    /**
     * Override checkDimension to allow both REAL and MATRIX inputs.
     * SerialBidirectional supports sending both scalar values and vector/matrix data.
     */
    @Override
    public void checkDimension() throws com.ncslab.ncslablink.MatDimException {
        // SerialBidirectional accepts both REAL (scalar) and MATRIX (vector) inputs
        // No dimension checking needed - both types are supported
    }

    /**
     * Close the serial port when block is destroyed.
     * Implements AutoCloseable interface for proper resource management.
     */
    @Override
    public void close() {
        if (serialPort != null && serialPort.isOpen()) {
            serialPort.closePort();
            log.info("Serial port {} closed for SerialBidirectional block {}", portName, blockId);
        }
    }

    /**
     * Legacy cleanup method for backward compatibility.
     * @deprecated Use {@link #close()} instead.
     */
    @Deprecated
    public void cleanup() {
        close();
    }

    public void generateInitCodeM(CodeStructM code) {
        super.generateInitCodeM(code);
        context.put("block", this);
        context.put("portName", portName);
        context.put("baudRate", baudRate);
        context.put("dataBits", dataBits);
        context.put("stopBits", stopBits);
        context.put("parity", parity);

        String codeStr = TemplateManager.renderTemplate("m/comm/SerialBidirectional/init.vm", context);
        code.addInitCode(codeStr);
    }

    public void generateOutputCodeM(CodeStructM code) {
        super.generateOutputCodeM(code);
        context.put("block", this);
        // Don't override inputs/outputs - TemplateUtils already populated them as List<String>

        String codeStr = TemplateManager.renderTemplate("m/comm/SerialBidirectional/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void generateDerivativeCodeM(CodeStructM code) {
        super.generateDerivativeCodeM(code);
        context.put("block", this);

        String codeStr = TemplateManager.renderTemplate("m/comm/SerialBidirectional/derivative.vm", context);
        code.addDerivativeCode(codeStr);
    }

    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);
        context.put("portName", portName);
        context.put("baudRate", baudRate);
        context.put("dataBits", dataBits);
        context.put("stopBits", stopBits);
        context.put("parity", parity);

        String codeStr = TemplateManager.renderTemplate("c/comm/SerialBidirectional/init.vm", context);
        code.addInitCode(codeStr);
    }

    public void generateOutputCodeC(CodeStructC code) {
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);
        // Don't override inputs/outputs - TemplateUtils already populated them as List<String>

        String codeStr = TemplateManager.renderTemplate("c/comm/SerialBidirectional/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void generateDerivativeCodeC(CodeStructC code) {
        super.generateDerivativeCodeC(code);
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        String codeStr = TemplateManager.renderTemplate("c/comm/SerialBidirectional/derivative.vm", context);
        code.addDerivativeCode(codeStr);
    }

    public void generateStatementCodeC(CodeStructC code) {
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        String codeStr = TemplateManager.renderTemplate("c/comm/SerialBidirectional/statement.vm", context);
        code.addStatementCode(codeStr);
    }

    // Getters for template access
    public String getPortName() {
        return portName;
    }

    public int getBaudRate() {
        return baudRate;
    }

    public int getDataBits() {
        return dataBits;
    }

    public int getStopBits() {
        return stopBits;
    }

    public String getParity() {
        return parity;
    }
}
