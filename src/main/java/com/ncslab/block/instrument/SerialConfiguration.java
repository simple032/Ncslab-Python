package com.ncslab.block.instrument;

// Java standard imports
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// External libraries
import lombok.Getter;
import org.json.JSONObject;

// Internal imports - DTO
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.instrument.SerialConfigurationDto;

// Internal imports - Core
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;

// Internal imports - Block components
import com.ncslab.block.Block;
import com.ncslab.block.io.Parameter;

// Internal imports - Code generation
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.util.TemplateManager;
import com.ncslab.util.TemplateUtils;

// Serial communication
import com.fazecast.jSerialComm.SerialPort;

/**
 * SerialConfiguration block - centralized serial port hardware resource management.
 *
 * This block configures a serial port that can be shared by multiple SerialSend and
 * SerialReceive blocks. Following SIMULINK R2024b architecture, this provides centralized
 * resource management to prevent port conflicts.
 *
 * SIMULINK Behavior:
 * - One SerialConfiguration block per physical serial port
 * - Multiple Send/Receive blocks reference the same configured port
 * - Port is opened during initialization, closed during termination
 * - Configuration parameters: Port, BaudRate, DataBits, Parity, StopBits, ByteOrder, FlowControl, Timeout
 *
 * Parameters:
 * - Port: String - Serial port identifier (e.g., "COM1", "/dev/ttyUSB0")
 * - BaudRate: int - Communication speed (110-921600 bps, default 9600)
 * - DataBits: int - Number of data bits (5-8, default 8)
 * - Parity: String - Parity checking ("none", "odd", "even", "mark", "space")
 * - StopBits: String - Stop bits ("1", "1.5", "2")
 * - ByteOrder: String - Byte order ("LittleEndian", "BigEndian")
 * - FlowControl: String - Flow control ("none", "hardware", "software")
 * - Timeout: double - Read timeout in seconds (-1 for blocking, 0 for non-blocking, >0 for timeout)
 *
 * Based on MATLAB/Simulink R2024b Serial Configuration block specification.
 *
 * @author NCSLab Team
 * @version 1.0
 * @since Serial Block Refactoring 2025
 */
public class SerialConfiguration extends Block {

    // === Static Port Registry for Centralized Management ===
    private static final Map<String, SerialConfiguration> portRegistry = new ConcurrentHashMap<>();
    private static final Map<String, SerialPort> activePortConnections = new ConcurrentHashMap<>();

    // === Parameters ===
    @Getter
    private final Parameter port;

    @Getter
    private final Parameter baudRate;

    @Getter
    private final Parameter dataBits;

    @Getter
    private final Parameter parity;

    @Getter
    private final Parameter stopBits;

    @Getter
    private final Parameter byteOrder;

    @Getter
    private final Parameter flowControl;

    @Getter
    private final Parameter timeout;

    // === Static Parameter Definitions ===
    public static final Map<String, String> PARAMETER_DEFAULTS;
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("Port", "COM1");
        PARAMETER_DEFAULTS.put("BaudRate", "9600");
        PARAMETER_DEFAULTS.put("DataBits", "8");
        PARAMETER_DEFAULTS.put("Parity", "none");
        PARAMETER_DEFAULTS.put("StopBits", "1");
        PARAMETER_DEFAULTS.put("ByteOrder", "LittleEndian");
        PARAMETER_DEFAULTS.put("FlowControl", "none");
        PARAMETER_DEFAULTS.put("Timeout", "10.0");
    }

    public static final List<String> outputNames = new ArrayList<>();
    public static final List<String> inputNames = new ArrayList<>();

    static {
        // SerialConfiguration block has no ports (configuration only)
    }

    /**
     * Legacy constructor from JSONObject
     */
    public SerialConfiguration(JSONObject blockIn, NCSLabModel model) {
        super(blockIn, model);

        // Get parameters using correct pattern
        this.port = getParameterByName("Port");
        this.baudRate = getParameterByName("BaudRate");
        this.dataBits = getParameterByName("DataBits");
        this.parity = getParameterByName("Parity");
        this.stopBits = getParameterByName("StopBits");
        this.byteOrder = getParameterByName("ByteOrder");
        this.flowControl = getParameterByName("FlowControl");
        this.timeout = getParameterByName("Timeout");

        // Validate parameters
        if (this.port == null || this.baudRate == null || this.dataBits == null ||
            this.parity == null || this.stopBits == null || this.byteOrder == null ||
            this.flowControl == null || this.timeout == null) {
            throw new IllegalStateException("SerialConfiguration parameters not properly initialized");
        }

        // Register this configuration
        registerConfiguration();
    }

    /**
     * DTO-NATIVE Constructor - Creates SerialConfiguration block directly from BlockDto DTO
     */
    public SerialConfiguration(SerialConfigurationDto blockDto, NCSLabModel model) {
        super(blockDto, model);

        // Get parameters using correct pattern
        this.port = getParameterByName("Port");
        this.baudRate = getParameterByName("BaudRate");
        this.dataBits = getParameterByName("DataBits");
        this.parity = getParameterByName("Parity");
        this.stopBits = getParameterByName("StopBits");
        this.byteOrder = getParameterByName("ByteOrder");
        this.flowControl = getParameterByName("FlowControl");
        this.timeout = getParameterByName("Timeout");

        // Validate parameters
        if (this.port == null || this.baudRate == null || this.dataBits == null ||
            this.parity == null || this.stopBits == null || this.byteOrder == null ||
            this.flowControl == null || this.timeout == null) {
            throw new IllegalStateException("SerialConfiguration parameters not properly initialized from DTO");
        }

        // Register this configuration
        registerConfiguration();

        System.out.println("DTO-NATIVE: SerialConfiguration block created successfully - " + blockDto.getBlockName());
    }

    /**
     * Register this configuration in the global port registry
     */
    private void registerConfiguration() {
        String portName = port.getInitString();

        // Check if port is already configured
        if (portRegistry.containsKey(portName)) {
            System.err.println("WARNING: Serial port " + portName + " is already configured. " +
                             "Multiple SerialConfiguration blocks for the same port may cause conflicts.");
        }

        portRegistry.put(portName, this);
        System.out.println("SerialConfiguration: Registered port " + portName);
    }

    /**
     * Get the SerialConfiguration for a specific port name
     */
    public static SerialConfiguration getConfiguration(String portName) {
        SerialConfiguration config = portRegistry.get(portName);
        if (config == null) {
            throw new IllegalStateException("No SerialConfiguration found for port: " + portName +
                                          ". Please add a SerialConfiguration block for this port.");
        }
        return config;
    }

    /**
     * Get or create the active SerialPort connection for this configuration
     */
    public SerialPort getSerialPort() {
        String portName = port.getInitString();

        return activePortConnections.computeIfAbsent(portName, key -> {
            SerialPort serialPort = SerialPort.getCommPort(portName);
            configureSerialPort(serialPort);
            return serialPort;
        });
    }

    /**
     * Configure a SerialPort with this configuration's parameters
     */
    private void configureSerialPort(SerialPort serialPort) {
        // Set baud rate
        int baudRateValue = Integer.parseInt(baudRate.getInitString());
        serialPort.setBaudRate(baudRateValue);

        // Set data bits
        int dataBitsValue = Integer.parseInt(dataBits.getInitString());
        serialPort.setNumDataBits(dataBitsValue);

        // Set parity
        String parityValue = parity.getInitString().toLowerCase();
        switch (parityValue) {
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
                System.err.println("Unknown parity: " + parityValue + ", using NO_PARITY");
                serialPort.setParity(SerialPort.NO_PARITY);
        }

        // Set stop bits
        String stopBitsValue = stopBits.getInitString();
        switch (stopBitsValue) {
            case "1":
                serialPort.setNumStopBits(SerialPort.ONE_STOP_BIT);
                break;
            case "1.5":
                serialPort.setNumStopBits(SerialPort.ONE_POINT_FIVE_STOP_BITS);
                break;
            case "2":
                serialPort.setNumStopBits(SerialPort.TWO_STOP_BITS);
                break;
            default:
                System.err.println("Unknown stop bits: " + stopBitsValue + ", using ONE_STOP_BIT");
                serialPort.setNumStopBits(SerialPort.ONE_STOP_BIT);
        }

        // Set flow control
        String flowControlValue = flowControl.getInitString().toLowerCase();
        switch (flowControlValue) {
            case "none":
                serialPort.setFlowControl(SerialPort.FLOW_CONTROL_DISABLED);
                break;
            case "hardware":
                serialPort.setFlowControl(SerialPort.FLOW_CONTROL_RTS_ENABLED | SerialPort.FLOW_CONTROL_CTS_ENABLED);
                break;
            case "software":
                serialPort.setFlowControl(SerialPort.FLOW_CONTROL_XONXOFF_IN_ENABLED | SerialPort.FLOW_CONTROL_XONXOFF_OUT_ENABLED);
                break;
            default:
                System.err.println("Unknown flow control: " + flowControlValue + ", using DISABLED");
                serialPort.setFlowControl(SerialPort.FLOW_CONTROL_DISABLED);
        }

        // Set timeout
        double timeoutValue = Double.parseDouble(timeout.getInitString());
        if (timeoutValue < 0) {
            // Blocking mode
            serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 0, 0);
        } else if (timeoutValue == 0) {
            // Non-blocking mode
            serialPort.setComPortTimeouts(SerialPort.TIMEOUT_NONBLOCKING, 0, 0);
        } else {
            // Timeout mode (convert seconds to milliseconds)
            int timeoutMs = (int) (timeoutValue * 1000);
            serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, timeoutMs, 0);
        }

        System.out.println("SerialConfiguration: Configured port " + port.getInitString() +
                         " [" + baudRateValue + " bps, " + dataBitsValue + " data bits, " +
                         parityValue + " parity, " + stopBitsValue + " stop bits]");
    }

    /**
     * Open the serial port (called during initialization)
     */
    public void openPort() {
        SerialPort serialPort = getSerialPort();
        if (!serialPort.isOpen()) {
            if (serialPort.openPort()) {
                System.out.println("SerialConfiguration: Opened port " + port.getInitString());
            } else {
                System.err.println("SerialConfiguration: Failed to open port " + port.getInitString());
            }
        }
    }

    /**
     * Close the serial port (called during termination)
     */
    public void closePort() {
        String portName = port.getInitString();
        SerialPort serialPort = activePortConnections.remove(portName);
        if (serialPort != null && serialPort.isOpen()) {
            serialPort.closePort();
            System.out.println("SerialConfiguration: Closed port " + portName);
        }
    }

    /**
     * Clear all port registrations (for testing)
     */
    public static void clearRegistry() {
        // Close all active ports
        for (SerialPort port : activePortConnections.values()) {
            if (port.isOpen()) {
                port.closePort();
            }
        }
        activePortConnections.clear();
        portRegistry.clear();
    }

    @Override
    public void updateDimension() throws MatDimException {
        // SerialConfiguration has no ports, no dimension updates needed
    }

    @Override
    public void checkDimension() throws MatDimException {
        // SerialConfiguration has no ports, no dimension checks needed
    }

    @Override
    public void calculateInit() {
        // Open the serial port during initialization
        openPort();
        System.out.println("SerialConfiguration '" + blockName + "' initialized port " + port.getInitString());
    }

    @Override
    public void calculateOutput(double t) {
        // SerialConfiguration has no outputs, nothing to calculate
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

        try {
            String codeStr = TemplateManager.renderTemplate("c/instrument/SerialConfiguration/init.vm", context);
            code.addInitCode(codeStr);
        } catch (Exception e) {
            System.err.println("Error rendering SerialConfiguration init template: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void generateOutputCodeC(CodeStructC code) {
        super.generateOutputCodeC(code);
        TemplateUtils.populateAllContext(context, this);

        try {
            String codeStr = TemplateManager.renderTemplate("c/instrument/SerialConfiguration/output.vm", context);
            code.addOutputCode(codeStr);
        } catch (Exception e) {
            System.err.println("Error rendering SerialConfiguration output template: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void generateOutputCodeM(CodeStructM code) {
        super.generateOutputCodeM(code);
        TemplateUtils.populateAllContext(context, this);

        try {
            String codeStr = TemplateManager.renderTemplate("m/instrument/SerialConfiguration/output.vm", context);
            code.addOutputCode(codeStr);
        } catch (Exception e) {
            System.err.println("Error rendering SerialConfiguration MATLAB template: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void generateStatementCodeC(CodeStructC code) {
        super.generateStatementCodeC(code);
        TemplateUtils.populateAllContext(context, this);

        try {
            String codeStr = TemplateManager.renderTemplate("c/instrument/SerialConfiguration/statement.vm", context);
            code.addStatementCode(codeStr);
        } catch (Exception e) {
            System.err.println("Error rendering SerialConfiguration statement template: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
