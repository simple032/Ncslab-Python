package com.ncslab.dto.mapper;

import com.ncslab.dto.block.BlockPortDto;
import com.ncslab.dto.mapper.exception.MappingException;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.Block;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.ArrayList;

/**
 * Mapper for converting between BlockPortDto and InputPort/OutputPort entities.
 * Note: Port creation requires Block references, so this mapper is primarily
 * used for DTO conversion rather than entity creation.
 */
public class PortMapper {
    
    private final Logger logger = LoggerFactory.getLogger(PortMapper.class);
    
    /**
     * Convert list of InputPort entities to BlockPortDto
     * @param inputPorts List of InputPort entities
     * @return List of BlockPortDto
     * @throws MappingException if conversion fails
     */
    public List<BlockPortDto> mapInputPortsToDto(List<InputPort> inputPorts) throws MappingException {
        if (inputPorts == null || inputPorts.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<BlockPortDto> portDtos = new ArrayList<>();
        
        for (int i = 0; i < inputPorts.size(); i++) {
            InputPort inputPort = inputPorts.get(i);
            try {
                BlockPortDto portDto = mapInputPortToDto(inputPort);
                portDtos.add(portDto);
            } catch (Exception e) {
                throw new MappingException("Failed to map input port to DTO at index " + i, e);
            }
        }
        
        return portDtos;
    }
    
    /**
     * Convert list of OutputPort entities to BlockPortDto
     * @param outputPorts List of OutputPort entities
     * @return List of BlockPortDto
     * @throws MappingException if conversion fails
     */
    public List<BlockPortDto> mapOutputPortsToDto(List<OutputPort> outputPorts) throws MappingException {
        if (outputPorts == null || outputPorts.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<BlockPortDto> portDtos = new ArrayList<>();
        
        for (int i = 0; i < outputPorts.size(); i++) {
            OutputPort outputPort = outputPorts.get(i);
            try {
                BlockPortDto portDto = mapOutputPortToDto(outputPort);
                portDtos.add(portDto);
            } catch (Exception e) {
                throw new MappingException("Failed to map output port to DTO at index " + i, e);
            }
        }
        
        return portDtos;
    }
    
    /**
     * Map InputPort entity to BlockPortDto
     */
    private BlockPortDto mapInputPortToDto(InputPort inputPort) throws MappingException {
        try {
            BlockPortDto dto = new BlockPortDto();
            
            // Map basic properties (using correct API methods)
            dto.setPortName(inputPort.getName());
            dto.setPortIndex(inputPort.getNumber());
            dto.setPortType("input");
            
            // Map dimensions (InputPort has getWidth/getHeight methods)
            List<Integer> dimensions = new ArrayList<>();
            dimensions.add(1); // Default width - InputPort doesn't expose width/height directly
            dimensions.add(1); // Default height
            dto.setDimensions(dimensions);
            
            // Set data type to string representation
            dto.setDataType("double"); // Default data type
            
            // Set as required by default for input ports
            dto.setRequired(true);
            
            logger.debug("Mapped InputPort to DTO: {}", dto.getPortName());
            return dto;
            
        } catch (Exception e) {
            throw new MappingException("Failed to map input port to DTO", e);
        }
    }
    
    /**
     * Map OutputPort entity to BlockPortDto
     */
    private BlockPortDto mapOutputPortToDto(OutputPort outputPort) throws MappingException {
        try {
            BlockPortDto dto = new BlockPortDto();
            
            // Map basic properties (using correct API methods)
            dto.setPortName(outputPort.getName());
            dto.setPortIndex(outputPort.getNumber());
            dto.setPortType("output");
            
            // Map dimensions (OutputPort has width/height getters)
            List<Integer> dimensions = new ArrayList<>();
            dimensions.add(outputPort.getWidth());
            dimensions.add(outputPort.getHeight());
            dto.setDimensions(dimensions);
            
            // Set data type to string representation
            dto.setDataType("double"); // Default data type
            
            // Output ports are typically not required
            dto.setRequired(false);
            
            logger.debug("Mapped OutputPort to DTO: {}", dto.getPortName());
            return dto;
            
        } catch (Exception e) {
            throw new MappingException("Failed to map output port to DTO", e);
        }
    }
    
    /**
     * Create InputPort from DTO - requires Block reference
     * Note: This is a helper method that would be used during Block creation
     */
    public InputPort createInputPortFromDto(BlockPortDto portDto, Block block, int portNumber) throws MappingException {
        if (block == null) {
            throw new MappingException("Block reference required for InputPort creation");
        }
        
        try {
            // InputPort constructor requires Block and port number
            InputPort inputPort = new InputPort(block, portNumber);
            
            logger.debug("Created InputPort from DTO: {}", portDto.getPortName());
            return inputPort;
            
        } catch (Exception e) {
            throw new MappingException("Failed to create InputPort from DTO", e);
        }
    }
    
    /**
     * Create OutputPort from DTO - requires Block reference
     * Note: This is a helper method that would be used during Block creation
     */
    public OutputPort createOutputPortFromDto(BlockPortDto portDto, Block block, int portNumber) throws MappingException {
        if (block == null) {
            throw new MappingException("Block reference required for OutputPort creation");
        }
        
        try {
            // OutputPort constructor requires Block and port number
            OutputPort outputPort = new OutputPort(block, portNumber);
            
            // Set dimensions if available
            if (portDto.getDimensions() != null && portDto.getDimensions().size() >= 2) {
                outputPort.setWidth(portDto.getDimensions().get(0));
                outputPort.setHeight(portDto.getDimensions().get(1));
            }
            
            logger.debug("Created OutputPort from DTO: {}", portDto.getPortName());
            return outputPort;
            
        } catch (Exception e) {
            throw new MappingException("Failed to create OutputPort from DTO", e);
        }
    }
}