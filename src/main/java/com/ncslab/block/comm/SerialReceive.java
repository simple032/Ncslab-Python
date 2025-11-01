package com.ncslab.block.comm;

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
import Jama.Matrix;

// Internal imports - DTO
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.comm.SerialReceiveDto;

// Internal imports - Core
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;

// Internal imports - Block components
import com.ncslab.block.Block;
import com.ncslab.block.data.Data;
import com.ncslab.block.data.DataType;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.Parameter;

// Internal imports - Code generation
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.util.TemplateManager;
import com.ncslab.util.TemplateUtils;

// Serial communication
import com.fazecast.jSerialComm.SerialPort;

/**
 * SerialReceive block - receives data over serial port following SIMULINK R2024b architecture.
 *
 * This block receives data through a serial port configured by a SerialConfiguration block.
 * Multiple SerialReceive blocks can share the same SerialConfiguration port.
 *
 * SIMULINK Behavior:
 * - Output: Data signal (scalar or vector) received from serial port
 * - References SerialConfiguration block for port settings
 * - Configurable sample time for periodic reading
 * - Optional header and terminator bytes to skip
 * - Configurable data size (number of values to receive)
 *
 * Parameters:
 * - Port: String - Name of SerialConfiguration block to use
 * - SampleTime: double - Sample time for receiving data (seconds)
 * - DataSize: int - Number of double values to receive (1 = scalar, >1 = vector)
 * - Header: String - Optional header bytes to skip (comma-separated hex values)
 * - Terminator: String - Optional terminator bytes to skip (comma-separated hex values)
 * - Blocking: String - Blocking mode ("on" = wait for data, "off" = non-blocking)
 *
 * Based on MATLAB/Simulink R2024b Serial Receive block specification.
 *
 * @author NCSLab Team
 * @version 2.0
 * @since Serial Block Refactoring 2025
 */
@Slf4j
public class SerialReceive extends Block {

    // === Parameters ===
    @Getter
    private final Parameter port;

    @Getter
    private final Parameter sampleTime;

    @Getter
    private final Parameter dataSize;

    @Getter
    private final Parameter header;

    @Getter
    private final Parameter terminator;

    @Getter
    private final Parameter blocking;

    // Buffer for received data
    private byte[] receiveBuffer = new byte[8]; // 8 bytes for double

    // === Static Parameter Definitions ===
    public static final Map<String, String> PARAMETER_DEFAULTS;
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("Port", "SerialConfig1");
        PARAMETER_DEFAULTS.put("SampleTime", "0.1");  // 100ms default
        PARAMETER_DEFAULTS.put("DataSize", "1");  // Scalar by default
        PARAMETER_DEFAULTS.put("Header", "");  // Empty = no header
        PARAMETER_DEFAULTS.put("Terminator", "");  // Empty = no terminator
        PARAMETER_DEFAULTS.put("Blocking", "on");
    }

    public static final List<String> outputNames = new ArrayList<>();
    public static final List<String> inputNames = new ArrayList<>();

    static {
        // Single output port
        outputNames.add("data");
    }

    /**
     * Legacy constructor from JSONObject
     */
    public SerialReceive(JSONObject blockIn, NCSLabModel model) {
        super(blockIn, model);

        // Get parameters using correct pattern
        this.port = getParameterByName("Port");
        this.sampleTime = getParameterByName("SampleTime");
        this.dataSize = getParameterByName("DataSize");
        this.header = getParameterByName("Header");
        this.terminator = getParameterByName("Terminator");
        this.blocking = getParameterByName("Blocking");

        // Validate parameters
        if (this.port == null || this.sampleTime == null || this.dataSize == null ||
            this.header == null || this.terminator == null || this.blocking == null) {
            throw new IllegalStateException("SerialReceive parameters not properly initialized");
        }

        // Single output port (can be scalar or vector)
        outputPortList.add(new OutputPort(this, 1, false));
    }

    /**
     * DTO-NATIVE Constructor - Creates SerialReceive block directly from BlockDto DTO
     */
    public SerialReceive(SerialReceiveDto blockDto, NCSLabModel model) {
        super(blockDto, model);

        // Get parameters using correct pattern
        this.port = getParameterByName("Port");
        this.sampleTime = getParameterByName("SampleTime");
        this.dataSize = getParameterByName("DataSize");
        this.header = getParameterByName("Header");
        this.terminator = getParameterByName("Terminator");
        this.blocking = getParameterByName("Blocking");

        // Validate parameters
        if (this.port == null || this.sampleTime == null || this.dataSize == null ||
            this.header == null || this.terminator == null || this.blocking == null) {
            throw new IllegalStateException("SerialReceive parameters not properly initialized from DTO");
        }

        // Single output port (can be scalar or vector)
        outputPortList.add(new OutputPort(this, 1, false));

        System.out.println("DTO-NATIVE: SerialReceive block created successfully - " + blockDto.getBlockName());
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
        if (!outputPortList.isEmpty()) {
            OutputPort out = outputPortList.get(0);
            int dataSizeValue = Integer.parseInt(dataSize.getInitString());

            if (dataSizeValue == 1) {
                // Scalar output
                out.setHeight(1);
                out.setWidth(1);
                out.getOutputSignalC().setHeight(1);
                out.getOutputSignalC().setWidth(1);
                out.getOutputSignalC().setDataType(DataType.REAL);
            } else {
                // Vector output
                out.setHeight(dataSizeValue);
                out.setWidth(1);
                out.getOutputSignalC().setHeight(dataSizeValue);
                out.getOutputSignalC().setWidth(1);
                out.getOutputSignalC().setDataType(DataType.MATRIX);
            }
        }
    }

    @Override
    public void checkDimension() throws MatDimException {
        // SerialReceive output dimension is determined by DataSize parameter
    }

    @Override
    public void calculateInit() {
        // Initialize output data
        OutputPort out = outputPortList.get(0);
        int dataSizeValue = Integer.parseInt(dataSize.getInitString());

        if (dataSizeValue == 1) {
            // Scalar output
            Data outputData = new Data(1, 1);
            outputData.setInitValue(0.0);
            out.setData(outputData);
        } else {
            // Vector output
            Data outputData = new Data(dataSizeValue, 1);
            Matrix mat = new Matrix(dataSizeValue, 1);
            outputData.setMatrix(mat);
            out.setData(outputData);
        }

        log.info("SerialReceive '{}' initialized, using port configuration '{}'",
                blockName, port.getInitString());
    }

    @Override
    public void calculateOutput(double t) {
        SerialPort serialPort = getSerialPort();
        if (serialPort == null || !serialPort.isOpen()) {
            log.warn("Serial port not open for SerialReceive block {}, skipping receive", blockId);
            return;
        }

        try {
            OutputPort out = outputPortList.get(0);
            int dataSizeValue = Integer.parseInt(dataSize.getInitString());

            // Parse header and terminator
            byte[] headerBytes = parseBytes(header.getInitString());
            byte[] terminatorBytes = parseBytes(terminator.getInitString());

            // Skip header if present
            if (headerBytes.length > 0) {
                byte[] headerBuffer = new byte[headerBytes.length];
                int headerRead = serialPort.readBytes(headerBuffer, headerBytes.length);
                if (headerRead != headerBytes.length) {
                    log.warn("SerialReceive block {} failed to read header bytes", blockId);
                    return;
                }
            }

            // Read data
            if (dataSizeValue == 1) {
                // Scalar - read single value
                double value = receiveValue(serialPort);
                Data outputData = new Data(1, 1);
                outputData.setInitValue(value);
                out.setData(outputData);
                log.debug("SerialReceive block {} received scalar: {}", blockId, value);
            } else {
                // Vector - read multiple values
                Data outputData = new Data(dataSizeValue, 1);
                Matrix mat = new Matrix(dataSizeValue, 1);
                for (int i = 0; i < dataSizeValue; i++) {
                    double value = receiveValue(serialPort);
                    mat.set(i, 0, value);
                }
                outputData.setMatrix(mat);
                out.setData(outputData);
                log.debug("SerialReceive block {} received {} values as vector", blockId, dataSizeValue);
            }

            // Skip terminator if present
            if (terminatorBytes.length > 0) {
                byte[] terminatorBuffer = new byte[terminatorBytes.length];
                serialPort.readBytes(terminatorBuffer, terminatorBytes.length);
            }

        } catch (Exception e) {
            log.error("Error receiving data on serial port for SerialReceive block {}: {}",
                     blockId, e.getMessage(), e);
        }
    }

    /**
     * Helper method to receive a single double value
     */
    private double receiveValue(SerialPort serialPort) {
        try {
            // Read 8 bytes for double
            int bytesRead = serialPort.readBytes(receiveBuffer, 8);

            if (bytesRead == 8) {
                // Convert bytes to double
                ByteBuffer buffer = ByteBuffer.wrap(receiveBuffer);

                // Use byte order from SerialConfiguration
                SerialConfiguration config = getSerialConfiguration();
                String byteOrderStr = config.getByteOrder().getInitString();
                if ("BigEndian".equalsIgnoreCase(byteOrderStr)) {
                    buffer.order(ByteOrder.BIG_ENDIAN);
                } else {
                    buffer.order(ByteOrder.LITTLE_ENDIAN);
                }

                double value = buffer.getDouble();
                log.debug("SerialReceive block {} received {} bytes (value: {})", blockId, bytesRead, value);
                return value;
            } else {
                log.warn("SerialReceive block {} failed to read all bytes. Read {}/8 bytes", blockId, bytesRead);
                return 0.0;
            }
        } catch (Exception e) {
            log.error("Error reading value from serial port: {}", e.getMessage(), e);
            return 0.0;
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

        // Resolve Port parameter to SerialConfiguration block ID
        // Port parameter contains the port name (e.g., "/dev/ttyUSB0"), not block name
        String portName = port.getInitString();
        SerialConfiguration portConfigBlock = null;
        int portConfigId = -1;

        try {
            portConfigBlock = SerialConfiguration.getConfiguration(portName);
            portConfigId = portConfigBlock.getBlockId();
        } catch (IllegalStateException e) {
            log.warn("SerialReceive block {} cannot find SerialConfiguration block for port '{}'",
                    blockId, portName);
        }

        // Add SerialReceive-specific context
        context.put("PortConfigId", portConfigId);  // Pass block ID for C code generation
        context.put("PortName", portName);  // Keep port name for reference
        context.put("SampleTime", sampleTime.getInitString());
        context.put("DataSize", dataSize.getInitString());
        context.put("Header", header.getInitString());
        context.put("Terminator", terminator.getInitString());
        context.put("Blocking", blocking.getInitString());

        try {
            String codeStr = TemplateManager.renderTemplate("c/comm/SerialReceive/init.vm", context);
            code.addInitCode(codeStr);
        } catch (Exception e) {
            log.error("Error rendering SerialReceive init template: {}", e.getMessage(), e);
        }
    }

    @Override
    public void generateOutputCodeC(CodeStructC code) {
        super.generateOutputCodeC(code);
        TemplateUtils.populateAllContext(context, this);

        // Resolve Port parameter to SerialConfiguration block ID
        // Port parameter contains the port name (e.g., "/dev/ttyUSB0"), not block name
        String portName = port.getInitString();
        SerialConfiguration portConfigBlock = null;
        int portConfigId = -1;

        try {
            portConfigBlock = SerialConfiguration.getConfiguration(portName);
            portConfigId = portConfigBlock.getBlockId();
        } catch (IllegalStateException e) {
            log.warn("SerialReceive block {} cannot find SerialConfiguration block for port '{}'",
                    blockId, portName);
        }

        // Add SerialReceive-specific context
        context.put("PortConfigId", portConfigId);  // Pass block ID for C code generation
        context.put("PortName", portName);  // Keep port name for reference
        context.put("SampleTime", sampleTime.getInitString());
        context.put("DataSize", dataSize.getInitString());
        context.put("Header", header.getInitString());
        context.put("Terminator", terminator.getInitString());
        context.put("Blocking", blocking.getInitString());

        try {
            String codeStr = TemplateManager.renderTemplate("c/comm/SerialReceive/output.vm", context);
            code.addOutputCode(codeStr);
        } catch (Exception e) {
            log.error("Error rendering SerialReceive output template: {}", e.getMessage(), e);
        }
    }

    @Override
    public void generateOutputCodeM(CodeStructM code) {
        super.generateOutputCodeM(code);
        TemplateUtils.populateAllContext(context, this);

        // Resolve Port parameter to SerialConfiguration block ID
        // Port parameter contains the port name (e.g., "/dev/ttyUSB0"), not block name
        String portName = port.getInitString();
        SerialConfiguration portConfigBlock = null;
        int portConfigId = -1;

        try {
            portConfigBlock = SerialConfiguration.getConfiguration(portName);
            portConfigId = portConfigBlock.getBlockId();
        } catch (IllegalStateException e) {
            log.warn("SerialReceive block {} cannot find SerialConfiguration block for port '{}'",
                    blockId, portName);
        }

        // Add SerialReceive-specific context
        context.put("PortConfigId", portConfigId);  // Pass block ID for MATLAB code generation
        context.put("PortName", portName);  // Keep port name for reference
        context.put("SampleTime", sampleTime.getInitString());
        context.put("DataSize", dataSize.getInitString());
        context.put("Header", header.getInitString());
        context.put("Terminator", terminator.getInitString());
        context.put("Blocking", blocking.getInitString());

        try {
            String codeStr = TemplateManager.renderTemplate("m/comm/SerialReceive/output.vm", context);
            code.addOutputCode(codeStr);
        } catch (Exception e) {
            log.error("Error rendering SerialReceive MATLAB template: {}", e.getMessage(), e);
        }
    }

    @Override
    public void generateStatementCodeC(CodeStructC code) {
        super.generateStatementCodeC(code);
        TemplateUtils.populateAllContext(context, this);

        try {
            String codeStr = TemplateManager.renderTemplate("c/comm/SerialReceive/statement.vm", context);
            code.addStatementCode(codeStr);
        } catch (Exception e) {
            log.error("Error rendering SerialReceive statement template: {}", e.getMessage(), e);
        }
    }
}
