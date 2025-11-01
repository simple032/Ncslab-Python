package com.ncslab.block.comm;

import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.comm.SerialSenderDto;
import com.ncslab.block.Block;
import com.ncslab.block.data.Data;
import com.ncslab.block.data.DataType;
import com.ncslab.block.io.InputPort;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.util.TemplateManager;
import com.fazecast.jSerialComm.SerialPort;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.nio.ByteBuffer;

/**
 * SerialSender block for sending data over serial communication (RS232/UART).
 * Supports configurable baud rate, data bits, stop bits, and parity.
 * Implements AutoCloseable for proper resource management.
 */
@Slf4j
public class SerialSender extends Block implements AutoCloseable {

    private String name = "SerialSender";

    private String portName;
    private int baudRate;
    private int dataBits;
    private int stopBits;
    private String parity;

    // Serial port instance
    private SerialPort serialPort;

    /**
     * DTO-NATIVE Constructor - Creates SerialSender block directly from BlockDto DTO
     */
    public SerialSender(SerialSenderDto blockDto, NCSLabModel model) {
        super(blockDto, model);

        // Single input port (can be scalar or vector)
        inputPortList.add(new InputPort(this, 1));

        System.out.println("DTO-NATIVE: SerialSender block created successfully - " + blockDto.getBlockName());
    }

    public static final List<String> inputNames = new ArrayList<>();

    public static final Map<String, String> PARAMETER_DEFAULTS = new HashMap<>();

    static {
        // Single input port that can handle vectors
        inputNames.add("in");

        PARAMETER_DEFAULTS.put("PortName", "COM1");
        PARAMETER_DEFAULTS.put("BaudRate", "9600");
        PARAMETER_DEFAULTS.put("DataBits", "8");
        PARAMETER_DEFAULTS.put("StopBits", "1");
        PARAMETER_DEFAULTS.put("Parity", "None");
    }

    public SerialSender(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);

        portName = paramValues.optString("PortName", "COM1");
        baudRate = paramValues.optInt("BaudRate", 9600);
        dataBits = paramValues.optInt("DataBits", 8);
        stopBits = paramValues.optInt("StopBits", 1);
        parity = paramValues.optString("Parity", "None");

        // Single input port (can be scalar or vector)
        inputPortList.add(new InputPort(this, 1));
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

            // Set timeouts
            serialPort.setComPortTimeouts(SerialPort.TIMEOUT_WRITE_BLOCKING, 1000, 0);

            // Open the port
            if (serialPort.openPort()) {
                log.info("Serial port {} opened successfully for SerialSender block {}", portName, blockId);
            } else {
                log.error("Failed to open serial port {} for SerialSender block {}", portName, blockId);
            }
        } catch (Exception e) {
            log.error("Error initializing serial port {} for SerialSender block {}: {}",
                     portName, blockId, e.getMessage(), e);
        }
    }

    @Override
    public void calculateOutput(double t) {
        if (serialPort == null || !serialPort.isOpen()) {
            log.warn("Serial port not open for SerialSender block {}, skipping send", blockId);
            return;
        }

        try {
            // Get the input port (single port that can be vector)
            InputPort inputPort = inputPortList.get(0);
            Data inputData = inputPort.getData();

            // Handle both scalar and vector inputs
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
                log.debug("SerialSender block {} sent {} values as vector on port {}",
                         blockId, height, portName);
            }
        } catch (Exception e) {
            log.error("Error sending data on serial port {} for SerialSender block {}: {}",
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
                log.debug("SerialSender block {} sent {} bytes (value: {}) on port {}",
                         blockId, bytesWritten, value, portName);
            } else {
                log.warn("SerialSender block {} failed to send all bytes. Sent {}/{} bytes",
                        blockId, bytesWritten, data.length);
            }
        } catch (Exception e) {
            log.error("Error writing value {} to serial port {}: {}",
                     value, portName, e.getMessage(), e);
        }
    }

    @Override
    public void calculateDerivative(double t) {
        // No derivative calculation needed for SerialSender
    }

    /**
     * Override checkDimension to allow both REAL and MATRIX inputs.
     * SerialSender supports sending both scalar values and vector/matrix data.
     */
    @Override
    public void checkDimension() throws com.ncslab.ncslablink.MatDimException {
        // SerialSender accepts both REAL (scalar) and MATRIX (vector) inputs
        // No dimension checking needed - both types are supported
        // The C code uses function overloading to handle both cases
    }

    /**
     * Close the serial port when block is destroyed.
     * Implements AutoCloseable interface for proper resource management.
     */
    @Override
    public void close() {
        if (serialPort != null && serialPort.isOpen()) {
            serialPort.closePort();
            log.info("Serial port {} closed for SerialSender block {}", portName, blockId);
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

        String codeStr = TemplateManager.renderTemplate("m/comm/SerialSender/init.vm", context);
        code.addInitCode(codeStr);
    }

    public void generateOutputCodeM(CodeStructM code) {
        super.generateOutputCodeM(code);
        context.put("block", this);
        // Don't override inputs - TemplateUtils already populated it as List<String>

        String codeStr = TemplateManager.renderTemplate("m/comm/SerialSender/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void generateDerivativeCodeM(CodeStructM code) {
        super.generateDerivativeCodeM(code);
        context.put("block", this);

        String codeStr = TemplateManager.renderTemplate("m/comm/SerialSender/derivative.vm", context);
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

        String codeStr = TemplateManager.renderTemplate("c/comm/SerialSender/init.vm", context);
        code.addInitCode(codeStr);
    }

    public void generateOutputCodeC(CodeStructC code) {
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);
        // Don't override inputs - TemplateUtils already populated it as List<String>

        // Add input CDataType information for proper byte sending
        if (!inputPortList.isEmpty() && inputPortList.get(0).getLinkedLine() != null) {
            com.ncslab.block.io.OutputSignal inputSignal =
                inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
            context.put("inputCDataType", inputSignal.getCDataType());
            context.put("inputDataType", inputSignal.getDataType());
            context.put("inputHeight", inputSignal.getHeight());
            context.put("inputWidth", inputSignal.getWidth());
        }

        String codeStr = TemplateManager.renderTemplate("c/comm/SerialSender/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void generateDerivativeCodeC(CodeStructC code) {
        super.generateDerivativeCodeC(code);
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        String codeStr = TemplateManager.renderTemplate("c/comm/SerialSender/derivative.vm", context);
        code.addDerivativeCode(codeStr);
    }

    public void generateStatementCodeC(CodeStructC code) {
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        // Add input CDataType information for proper function signature generation
        if (!inputPortList.isEmpty() && inputPortList.get(0).getLinkedLine() != null) {
            com.ncslab.block.io.OutputSignal inputSignal =
                inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
            context.put("inputCDataType", inputSignal.getCDataType());
            context.put("inputDataType", inputSignal.getDataType());
        }

        String codeStr = TemplateManager.renderTemplate("c/comm/SerialSender/statement.vm", context);
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
