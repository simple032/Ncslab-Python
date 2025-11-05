package com.ncslab.block.instrument;

// Java standard imports
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

// External libraries
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;

// Internal imports - DTO
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.instrument.SerialSendDto;

// Internal imports - Core
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;

// Internal imports - Block components
import com.ncslab.block.Block;
import com.ncslab.block.data.Data;
import com.ncslab.block.data.DataType;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.Parameter;

// Internal imports - Code generation
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.util.TemplateManager;
import com.ncslab.util.TemplateUtils;

// Serial communication
import com.fazecast.jSerialComm.SerialPort;

/**
 * SerialSend block - sends data over serial port following SIMULINK R2024b architecture.
 *
 * This block sends data through a serial port configured by a SerialConfiguration block.
 * Multiple SerialSend blocks can share the same SerialConfiguration port.
 *
 * SIMULINK Behavior:
 * - Input: Data signal (scalar or vector) to send
 * - References SerialConfiguration block for port settings
 * - Optional header and terminator bytes
 * - Configurable blocking mode
 *
 * Parameters:
 * - Port: String - Name of SerialConfiguration block to use
 * - Header: String - Optional header bytes (comma-separated hex values, e.g., "0xFF,0xFE")
 * - Terminator: String - Optional terminator bytes (comma-separated hex values)
 * - Blocking: String - Blocking mode ("on" = wait for completion, "off" = non-blocking)
 *
 * Based on MATLAB/Simulink R2024b Serial Send block specification.
 *
 * @author NCSLab Team
 * @version 2.0
 * @since Serial Block Refactoring 2025
 */
@Slf4j
public class SerialSend extends Block {

    // === Parameters ===
    @Getter
    private final Parameter port;

    @Getter
    private final Parameter header;

    @Getter
    private final Parameter terminator;

    @Getter
    private final Parameter blocking;

    // === Static Parameter Definitions ===
    public static final Map<String, String> PARAMETER_DEFAULTS;
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("Port", "SerialConfig1");
        PARAMETER_DEFAULTS.put("Header", "");  // Empty = no header
        PARAMETER_DEFAULTS.put("Terminator", "");  // Empty = no terminator
        PARAMETER_DEFAULTS.put("Blocking", "on");
    }

    public static final List<String> outputNames = new ArrayList<>();
    public static final List<String> inputNames = new ArrayList<>();

    static {
        // Single input port
        inputNames.add("data");
    }

    /**
     * Legacy constructor from JSONObject
     */
    public SerialSend(JSONObject blockIn, NCSLabModel model) {
        super(blockIn, model);

        // Get parameters using correct pattern
        this.port = getParameterByName("Port");
        this.header = getParameterByName("Header");
        this.terminator = getParameterByName("Terminator");
        this.blocking = getParameterByName("Blocking");

        // Validate parameters
        if (this.port == null || this.header == null || this.terminator == null || this.blocking == null) {
            throw new IllegalStateException("SerialSend parameters not properly initialized");
        }

        // Single input port (can be scalar or vector)
        inputPortList.add(new InputPort(this, 1));
    }

    /**
     * DTO-NATIVE Constructor - Creates SerialSend block directly from BlockDto DTO
     */
    public SerialSend(SerialSendDto blockDto, NCSLabModel model) {
        super(blockDto, model);

        // Get parameters using correct pattern
        this.port = getParameterByName("Port");
        this.header = getParameterByName("Header");
        this.terminator = getParameterByName("Terminator");
        this.blocking = getParameterByName("Blocking");

        // Validate parameters
        if (this.port == null || this.header == null || this.terminator == null || this.blocking == null) {
            throw new IllegalStateException("SerialSend parameters not properly initialized from DTO");
        }

        // Single input port (can be scalar or vector)
        inputPortList.add(new InputPort(this, 1));

        System.out.println("DTO-NATIVE: SerialSend block created successfully - " + blockDto.getBlockName());
    }

    /**
     * Get the SerialConfiguration for this block
     */
    private SerialConfiguration getSerialConfiguration() {
        String portName = port.getInitString();
        return SerialConfiguration.getConfiguration(portName);
    }

    /**
     * Get the active serial port from the configuration
     */
    private SerialPort getSerialPort() {
        SerialConfiguration config = getSerialConfiguration();
        return config.getSerialPort();
    }

    /**
     * Parse header/terminator bytes from parameter string
     * Format: "0xFF,0xFE,0x01" or empty string for no bytes
     */
    private byte[] parseBytes(String bytesStr) {
        if (bytesStr == null || bytesStr.trim().isEmpty()) {
            return new byte[0];
        }

        String[] parts = bytesStr.split(",");
        byte[] bytes = new byte[parts.length];
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i].trim();
            if (part.startsWith("0x") || part.startsWith("0X")) {
                bytes[i] = (byte) Integer.parseInt(part.substring(2), 16);
            } else {
                bytes[i] = (byte) Integer.parseInt(part);
            }
        }
        return bytes;
    }

    @Override
    public void updateDimension() throws MatDimException {
        // SerialSend accepts both scalar and vector inputs - no specific dimension constraints
    }

    @Override
    public void checkDimension() throws MatDimException {
        // SerialSend accepts both REAL (scalar) and MATRIX (vector) inputs
        // No dimension checking needed - both types are supported
    }

    @Override
    public void calculateInit() {
        // SerialSend doesn't need initialization - SerialConfiguration handles port opening
        log.info("SerialSend '{}' initialized, using port configuration '{}'",
                blockName, port.getInitString());
    }

    @Override
    public void calculateOutput(double t) {
        SerialPort serialPort = getSerialPort();
        if (serialPort == null || !serialPort.isOpen()) {
            log.warn("Serial port not open for SerialSend block {}, skipping send", blockId);
            return;
        }

        try {
            // Get the input data
            InputPort inputPort = inputPortList.get(0);
            Data inputData = inputPort.getData();

            if (inputData == null) {
                log.warn("SerialSend block {} has no input data", blockId);
                return;
            }

            // Parse header and terminator
            byte[] headerBytes = parseBytes(header.getInitString());
            byte[] terminatorBytes = parseBytes(terminator.getInitString());

            // Send header if present
            if (headerBytes.length > 0) {
                serialPort.writeBytes(headerBytes, headerBytes.length);
            }

            // Send data (handle both scalar and vector)
            if (inputData.getDataType() == DataType.REAL) {
                // Scalar - single value
                double value = inputData.getInitValue();
                sendValue(serialPort, value);
            } else {
                // Matrix/Vector - send all elements
                int height = inputData.getHeight();
                for (int i = 0; i < height; i++) {
                    double value = inputData.getMatrix().get(i, 0);
                    sendValue(serialPort, value);
                }
                log.debug("SerialSend block {} sent {} values as vector", blockId, height);
            }

            // Send terminator if present
            if (terminatorBytes.length > 0) {
                serialPort.writeBytes(terminatorBytes, terminatorBytes.length);
            }

        } catch (Exception e) {
            log.error("Error sending data on serial port for SerialSend block {}: {}",
                     blockId, e.getMessage(), e);
        }
    }

    /**
     * Helper method to send a single double value
     */
    private void sendValue(SerialPort serialPort, double value) {
        try {
            // Convert double to bytes (8 bytes for double)
            ByteBuffer buffer = ByteBuffer.allocate(8);

            // Use byte order from SerialConfiguration
            SerialConfiguration config = getSerialConfiguration();
            String byteOrderStr = config.getByteOrder().getInitString();
            if ("BigEndian".equalsIgnoreCase(byteOrderStr)) {
                buffer.order(ByteOrder.BIG_ENDIAN);
            } else {
                buffer.order(ByteOrder.LITTLE_ENDIAN);
            }

            buffer.putDouble(value);
            byte[] data = buffer.array();

            // Write data to serial port
            int bytesWritten = serialPort.writeBytes(data, data.length);

            if (bytesWritten == data.length) {
                log.debug("SerialSend block {} sent {} bytes (value: {})", blockId, bytesWritten, value);
            } else {
                log.warn("SerialSend block {} failed to send all bytes. Sent {}/{} bytes",
                        blockId, bytesWritten, data.length);
            }
        } catch (Exception e) {
            log.error("Error writing value {} to serial port: {}", value, e.getMessage(), e);
        }
    }

    // === Static Methods ===
    public static List<String> getInputNames() {
        return inputNames;
    }

    public static List<String> getOutputNames() {
        return outputNames;
    }

    // === Code Generation Methods ===
    @Override
    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);
        TemplateUtils.populateAllContext(context, this);

        // Resolve Port parameter to SerialConfiguration block ID (computed - keep)
        String portName = port.getInitString();
        SerialConfiguration portConfigBlock = null;
        int portConfigId = -1;

        try {
            portConfigBlock = SerialConfiguration.getConfiguration(portName);
            portConfigId = portConfigBlock.getBlockId();
        } catch (IllegalStateException e) {
            log.warn("SerialSend block {} cannot find SerialConfiguration block for port '{}'",
                    blockId, portName);
        }

        // Computed values (keep)
        context.put("PortConfigId", portConfigId);  // COMPUTED from SerialConfiguration lookup

        try {
            String codeStr = TemplateManager.renderTemplate("c/instrument/SerialSend/init.vm", context);
            code.addInitCode(codeStr);
        } catch (Exception e) {
            log.error("Error rendering SerialSend init template: {}", e.getMessage(), e);
        }
    }

    @Override
    public void generateOutputCodeC(CodeStructC code) {
        super.generateOutputCodeC(code);
        TemplateUtils.populateAllContext(context, this);

        // Resolve Port parameter to SerialConfiguration block ID (computed - keep)
        String portName = port.getInitString();
        SerialConfiguration portConfigBlock = null;
        int portConfigId = -1;

        try {
            portConfigBlock = SerialConfiguration.getConfiguration(portName);
            portConfigId = portConfigBlock.getBlockId();
        } catch (IllegalStateException e) {
            log.warn("SerialSend block {} cannot find SerialConfiguration block for port '{}'",
                    blockId, portName);
        }

        // Computed values (keep)
        context.put("PortConfigId", portConfigId);  // COMPUTED from SerialConfiguration lookup

        // Add input signal information (computed - keep)
        if (!inputPortList.isEmpty() && inputPortList.get(0).getLinkedLine() != null) {
            com.ncslab.block.io.OutputSignal inputSignal =
                inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
            context.put("inputCDataType", inputSignal.getCDataType());
            context.put("inputDataType", inputSignal.getDataType());
            context.put("inputHeight", inputSignal.getHeight());
            context.put("inputWidth", inputSignal.getWidth());
        }

        try {
            String codeStr = TemplateManager.renderTemplate("c/instrument/SerialSend/output.vm", context);
            code.addOutputCode(codeStr);
        } catch (Exception e) {
            log.error("Error rendering SerialSend output template: {}", e.getMessage(), e);
        }
    }

    @Override
    public void generateOutputCodeM(CodeStructM code) {
        super.generateOutputCodeM(code);
        TemplateUtils.populateAllContext(context, this);

        // Resolve Port parameter to SerialConfiguration block ID (computed - keep)
        String portName = port.getInitString();
        SerialConfiguration portConfigBlock = null;
        int portConfigId = -1;

        try {
            portConfigBlock = SerialConfiguration.getConfiguration(portName);
            portConfigId = portConfigBlock.getBlockId();
        } catch (IllegalStateException e) {
            log.warn("SerialSend block {} cannot find SerialConfiguration block for port '{}'",
                    blockId, portName);
        }

        // Computed values (keep)
        context.put("PortConfigId", portConfigId);  // COMPUTED from SerialConfiguration lookup

        try {
            String codeStr = TemplateManager.renderTemplate("m/instrument/SerialSend/output.vm", context);
            code.addOutputCode(codeStr);
        } catch (Exception e) {
            log.error("Error rendering SerialSend MATLAB template: {}", e.getMessage(), e);
        }
    }

    @Override
    public void generateStatementCodeC(CodeStructC code) {
        super.generateStatementCodeC(code);
        TemplateUtils.populateAllContext(context, this);

        try {
            String codeStr = TemplateManager.renderTemplate("c/instrument/SerialSend/statement.vm", context);
            code.addStatementCode(codeStr);
        } catch (Exception e) {
            log.error("Error rendering SerialSend statement template: {}", e.getMessage(), e);
        }
    }
}
