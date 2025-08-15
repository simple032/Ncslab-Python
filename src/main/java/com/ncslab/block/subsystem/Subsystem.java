package com.ncslab.block.subsystem;

import com.ncslab.block.Block;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.line.Line;
import org.json.JSONObject;
import com.ncslab.dto.BlockJson;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

public class Subsystem extends Block{

    @Getter
    private List<In> inBlockList;
    @Getter
    private List<Out> outBlockList;
    
    // Container for all blocks within this subsystem
    @Getter
    private List<Block> containedBlocks;
    
    // Container for all lines within this subsystem  
    @Getter
    private List<Line> containedLines;
    
    
    // Static parameter defaults for consistency with other blocks
    
    
    /**
     * DTO-NATIVE Constructor - Creates Subsystem block directly from BlockJson DTO
     */
    public Subsystem(BlockJson blockDto, NCSLabModel model) {
        super(blockDto, model);
        System.out.println("DTO-NATIVE: Subsystem block created successfully - " + blockDto.getBlockName());
    }


    public static final Map<String, String> PARAMETER_DEFAULTS;
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        // Basic subsystem parameters - minimal for now
    }
    
    public static final List<String> outputNames = new ArrayList<>();
    public static final List<String> inputNames = new ArrayList<>();
    
    public Subsystem(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);
        inBlockList = new ArrayList<>();
        outBlockList = new ArrayList<>();
        containedBlocks = new ArrayList<>();
        containedLines = new ArrayList<>();
        
        
        // Initialize with empty collections - blocks and lines will be added via management methods
    }
    @Override
    public void generateOutputCodeC(CodeStructC code) {
        super.generateOutputCodeC(code);

        // Populate template context with subsystem-specific data
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);
        
        // Add subsystem-specific context
        context.put("containedBlocks", containedBlocks);
        context.put("containedLines", containedLines);
        context.put("inBlockList", inBlockList);
        context.put("outBlockList", outBlockList);
        context.put("boundaryLines", getBoundaryLines());
        context.put("pureInternalLines", getPureInternalLines());

        // Generate subsystem wrapper code using template
        String outputCode = com.ncslab.util.TemplateManager.renderTemplate("c/subsystem/Subsystem/output.vm", context);
        code.addOutputCode(outputCode);
        
        // Generate code for all contained blocks (except In/Out which are handled by boundary)
        for (Block block : containedBlocks) {
            try {
                if (!(block instanceof In || block instanceof Out)) {
                    // Generate code for internal blocks
                    block.generateOutputCodeC(code);
                }
            } catch (Exception e) {
                System.err.println("Error generating code for block " + block.getBlockName() + ": " + e.getMessage());
            }
        }
    }
    
    @Override
    public void updateDimension() throws MatDimException {
        // Update dimensions for all contained blocks first
        for (Block block : containedBlocks) {
            try {
                block.updateDimension();
            } catch (MatDimException e) {
                System.err.println("Dimension error in block " + block.getBlockName() + ": " + e.getMessage());
                throw e;
            }
        }
        
        // Propagate dimensions along internal lines
        propagateLineDimensions();
        
        // Update subsystem port dimensions based on In/Out blocks
        updateSubsystemPortDimensions();
    }
    
    @Override
    public void checkDimension() throws MatDimException {
        // Check dimensions for all contained blocks
        for (Block block : containedBlocks) {
            try {
                block.checkDimension();
            } catch (MatDimException e) {
                System.err.println("Dimension check failed in block " + block.getBlockName() + ": " + e.getMessage());
                throw e;
            }
        }
    }
    
    private void updateSubsystemPortDimensions() throws MatDimException {
        // Update input port dimensions from In blocks
        for (In inBlock : inBlockList) {
            int portIndex = inBlock.getPortNumber() - 1;
            if (portIndex < inputPortList.size()) {
                // Copy dimensions from the In block's output to the subsystem's input port
                // This ensures proper dimension propagation
                inBlock.updateDimension();
            }
        }
        
        // Update output port dimensions from Out blocks  
        for (Out outBlock : outBlockList) {
            int portIndex = outBlock.getPortNumber() - 1;
            if (portIndex < outputPortList.size()) {
                // Copy dimensions from the Out block's input to the subsystem's output port
                outBlock.updateDimension();
            }
        }
    }
    
    private void propagateLineDimensions() throws MatDimException {
        // Propagate dimensions along all internal lines
        // Note: InputPort gets dimensions from connected OutputPort automatically via getHeight()/getWidth()
        // This method serves as validation and explicit dimension checking
        for (Line line : containedLines) {
            try {
                if (line.getLinkedOutputPort() != null && line.getLinkedInputPort() != null) {
                    OutputPort outputPort = line.getLinkedOutputPort();
                    InputPort inputPort = line.getLinkedInputPort();
                    
                    // Validate dimensions are consistent
                    if (outputPort.getOutputSignalC() != null) {
                        // InputPort automatically gets dimensions from connected OutputPort
                        // Just validate they are accessible
                        int outputHeight = outputPort.getHeight();
                        int outputWidth = outputPort.getWidth();
                        int inputHeight = inputPort.getHeight();
                        int inputWidth = inputPort.getWidth();
                        
                        // Basic dimension validation
                        if (outputHeight != inputHeight || outputWidth != inputWidth) {
                            System.out.println("Dimension mismatch on line from " + 
                                outputPort.getBLock().getBlockName() + " to " + 
                                inputPort.getBLock().getBlockName());
                        }
                    }
                }
            } catch (Exception e) {
                throw new MatDimException("Failed to propagate dimensions along line connecting " +
                    line.getLinkedOutputPort().getBLock().getBlockName() + " to " +
                    line.getLinkedInputPort().getBLock().getBlockName() + ": " + e.getMessage());
            }
        }
    }

    public void addIn(In in) {
        if (in == null) return;
        
        inBlockList.add(in);
        containedBlocks.add(in); // Add to contained blocks for proper counting
        in.setSubsystem(this);
        
        int portNo = in.getPortNumber();
        
        // Ensure we have enough input ports on the subsystem
        while (inputPortList.size() < portNo) {
            inputPortList.add(new InputPort(this, inputPortList.size() + 1));
        }
        
        // Update input names for block properties
        while (inputNames.size() < portNo) {
            inputNames.add("in" + (inputNames.size() + 1));
        }
    }
    
    public void addOut(Out out) {
        if (out == null) return;
        
        outBlockList.add(out);
        containedBlocks.add(out); // Add to contained blocks for proper counting
        out.setSubsystem(this);
        
        int portNo = out.getPortNumber();
        
        // Ensure we have enough output ports on the subsystem
        while (outputPortList.size() < portNo) {
            outputPortList.add(new OutputPort(this, outputPortList.size() + 1, true));
        }
        
        // Update output names for block properties
        while (outputNames.size() < portNo) {
            outputNames.add("out" + (outputNames.size() + 1));
        }
    }
    
    // Block container management methods
    public void addBlock(Block block) {
        if (block == null) return;
        
        // Handle special cases for In/Out blocks - they have their own add methods
        if (block instanceof In) {
            addIn((In) block);
        } else if (block instanceof Out) {
            addOut((Out) block);
        } else {
            // For other blocks, just add to contained blocks
            if (!containedBlocks.contains(block)) {
                containedBlocks.add(block);
            }
        }
    }
    
    public void removeBlock(Block block) {
        containedBlocks.remove(block);
        if (block instanceof In) {
            inBlockList.remove((In) block);
        } else if (block instanceof Out) {
            outBlockList.remove((Out) block);
        }
    }
    
    public boolean containsBlock(Block block) {
        return containedBlocks.contains(block);
    }
    
    public int getBlockCount() {
        return containedBlocks.size();
    }
    
    // Line management methods
    public void addLine(Line line) {
        if (line == null) return;
        
        if (!containedLines.contains(line)) {
            containedLines.add(line);
        }
    }
    
    public void removeLine(Line line) {
        containedLines.remove(line);
    }
    
    public boolean containsLine(Line line) {
        return containedLines.contains(line);
    }
    
    public int getLineCount() {
        return containedLines.size();
    }
    
    public List<Line> getInternalLines() {
        return new ArrayList<>(containedLines);
    }
    
    // Get lines that connect to subsystem boundary (In/Out blocks)
    public List<Line> getBoundaryLines() {
        List<Line> boundaryLines = new ArrayList<>();
        
        for (Line line : containedLines) {
            Block fromBlock = line.getLinkedOutputPort().getBLock();
            Block toBlock = line.getLinkedInputPort().getBLock();
            
            // Line connects to boundary if either end is an In or Out block
            if ((fromBlock instanceof In) || (fromBlock instanceof Out) || 
                (toBlock instanceof In) || (toBlock instanceof Out)) {
                boundaryLines.add(line);
            }
        }
        
        return boundaryLines;
    }
    
    // Get purely internal lines (not connecting to In/Out blocks)
    public List<Line> getPureInternalLines() {
        List<Line> internalLines = new ArrayList<>();
        
        for (Line line : containedLines) {
            Block fromBlock = line.getLinkedOutputPort().getBLock();
            Block toBlock = line.getLinkedInputPort().getBLock();
            
            // Line is purely internal if neither end is an In or Out block
            if (!(fromBlock instanceof In) && !(fromBlock instanceof Out) && 
                !(toBlock instanceof In) && !(toBlock instanceof Out)) {
                internalLines.add(line);
            }
        }
        
        return internalLines;
    }
    
    // Create line within subsystem using JSON specification
    public Line createInternalLine(JSONObject lineJSON) {
        // Create line using contained blocks only
        Line line = Line.createLine(lineJSON, containedBlocks);
        if (line != null) {
            addLine(line);
        }
        return line;
    }
    
    // Initialization and execution methods
    @Override
    public void calculateInit() {
        // Initialize all contained blocks
        for (Block block : containedBlocks) {
            try {
                block.calculateInit();
            } catch (Exception e) {
                System.err.println("Error initializing block " + block.getBlockName() + ": " + e.getMessage());
            }
        }
    }
    
    @Override
    public void calculateOutput(double t) {
        // Execute all contained blocks in dependency order
        // For now, simple sequential execution - could be enhanced with proper ordering
        for (Block block : containedBlocks) {
            try {
                block.calculateOutput(t);
            } catch (Exception e) {
                System.err.println("Error executing block " + block.getBlockName() + " at time " + t + ": " + e.getMessage());
            }
        }
    }
    
    
    // Get subsystem path for hierarchical identification
    public String getSubsystemPath() {
        return getBlockPath() + "/" + getBlockName();
    }
    
    // Cleanup method for proper resource management
    public void cleanup() {
        // Clear all references
        if (containedBlocks != null) {
            containedBlocks.clear();
        }
        if (containedLines != null) {
            containedLines.clear();
        }
        if (inBlockList != null) {
            inBlockList.clear();
        }
        if (outBlockList != null) {
            outBlockList.clear();
        }
        
    }
}
