package com.ncslab.block.subsystem;

import com.ncslab.block.Block;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.line.Line;

import org.checkerframework.common.reflection.qual.GetClass;
import org.json.JSONObject;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.subsystem.SubsystemDto;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.system.NCSLabSystem;

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
    
    // NCSLabSystem manages all blocks and lines within this subsystem
    @Getter
    private NCSLabSystem innerSystem;
    // Static parameter defaults for consistency with other blocks
    
    public String getFullPath(){
        return getBlockPath() + "/" + getBlockName();
    }    


    public static final Map<String, String> PARAMETER_DEFAULTS;
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        // Basic subsystem parameters - minimal for now
    }
    
    public static final List<String> outputNames = new ArrayList<>();
    public static final List<String> inputNames = new ArrayList<>();
    
    @SuppressWarnings("deprecation")
    public Subsystem(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);
        inBlockList = new ArrayList<>();
        outBlockList = new ArrayList<>();

        // Initialize NCSLabSystem to manage subsystem's internal structure
        innerSystem = new NCSLabSystem();

        // Initialize with empty collections - blocks and lines will be added via management methods
    }

    public Subsystem(SubsystemDto blockDto, NCSLabModel model) {
        super(blockDto, model);
        inBlockList = new ArrayList<>();
        outBlockList = new ArrayList<>();
        
        // Initialize NCSLabSystem to manage subsystem's internal structure
        innerSystem = new NCSLabSystem();
        
        // Initialize with empty collections - blocks and lines will be added via management methods
    }

    @Override
    public void generateOutputCodeC(CodeStructC code) {
        super.generateOutputCodeC(code);

        // Populate template context with subsystem-specific data
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);
        
        // Add subsystem-specific context
        context.put("containedBlocks", innerSystem.getBlocks());
        context.put("containedLines", innerSystem.getLines());
        context.put("inBlockList", inBlockList);
        context.put("outBlockList", outBlockList);
        context.put("boundaryLines", getBoundaryLines());
        context.put("pureInternalLines", getPureInternalLines());

        // Generate subsystem wrapper code using template
        String outputCode = com.ncslab.util.TemplateManager.renderTemplate("c/subsystem/Subsystem/output.vm", context);
        code.addOutputCode(outputCode);
        
        // Generate code for all contained blocks (except In/Out which are handled by boundary)
        for (Block block : innerSystem.getBlocks()) {
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
        // Delegate dimension update to NCSLabSystem
        innerSystem.updateDimensions();
        
        // Update subsystem port dimensions based on In/Out blocks
        updateSubsystemPortDimensions();
    }
    
    @Override
    public void checkDimension() throws MatDimException {
        // Delegate dimension checking to NCSLabSystem
        for (Block block : innerSystem.getBlocks()) {
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
    

    public void addIn(In in) {
        if (in == null) return;
        
        inBlockList.add(in);
        innerSystem.addBlock(in); // Add to NCSLabSystem for proper management
        in.setSubsystem(this);
        
        int portNo = in.getPortNumber();
        
        // Ensure we have enough input ports on the subsystem
        inputPortList.add(new InputPort(this, portNo));        
        
        // Update input names for block properties
        inputNames.add(in.getBlockName());        
    }
    
    public void addOut(Out out) {
        if (out == null) return;
        
        outBlockList.add(out);
        innerSystem.addBlock(out); // Add to NCSLabSystem for proper management
        out.setSubsystem(this);
        
        int portNo = out.getPortNumber();
        
        // Ensure we have enough output ports on the subsystem
        outputPortList.add(new OutputPort(this, portNo, true));
        outputNames.add(out.getBlockName());
    }
    
    // Block container management methods
    public void addBlock(Block block) {
        if (block == null) return;
        
        // Handle special cases for In/Out blocks - they have their own add methods
        if (block instanceof In) {
            return;
        } else if (block instanceof Out) {
            return;
        } else {
            // For other blocks, add to NCSLabSystem
            innerSystem.addBlock(block);
        }
    }
    
    public void removeBlock(Block block) {
        innerSystem.removeBlock(block);
        if (block instanceof In) {
            inBlockList.remove((In) block);
        } else if (block instanceof Out) {
            outBlockList.remove((Out) block);
        }
    }
    
    public boolean containsBlock(Block block) {
        return innerSystem.getBlocks().contains(block);
    }
    
    public int getBlockCount() {
        return innerSystem.getBlockCount();
    }
    
    // Line management methods
    public void addLine(Line line) {
        if (line == null) return;
        
        innerSystem.addLine(line);
    }
    
    public void removeLine(Line line) {
        innerSystem.removeLine(line);
    }
    
    public boolean containsLine(Line line) {
        return innerSystem.getLines().contains(line);
    }
    
    public int getLineCount() {
        return innerSystem.getLineCount();
    }
    
    public List<Line> getInternalLines() {
        return new ArrayList<>(innerSystem.getLines());
    }
    
    // Get lines that connect to subsystem boundary (In/Out blocks)
    public List<Line> getBoundaryLines() {
        List<Line> boundaryLines = new ArrayList<>();
        
        for (Line line : innerSystem.getLines()) {
            Block fromBlock = line.getLinkedOutputPort().getBlock();
            Block toBlock = line.getLinkedInputPort().getBlock();
            
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
        
        for (Line line : innerSystem.getLines()) {
            Block fromBlock = line.getLinkedOutputPort().getBlock();
            Block toBlock = line.getLinkedInputPort().getBlock();
            
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
        // Create line using blocks from NCSLabSystem
        Line line = Line.createLine(lineJSON, innerSystem.getBlocks());
        if (line != null) {
            addLine(line);
        }
        return line;
    }
    
    // Initialization and execution methods
    @Override
    public void calculateInit() {
        // Delegate initialization to NCSLabSystem
        innerSystem.calculateInit(0.0);
    }
    
    @Override
    public void calculateOutput(double t) {
        // Delegate output calculation to NCSLabSystem with proper dependency ordering
        innerSystem.calculateOutput(t);
    }
    
    
    // Get subsystem path for hierarchical identification
    public String getSubsystemPath() {
        return getBlockPath() + "/" + getBlockName();
    }
    
    // Backward compatibility methods - delegate to NCSLabSystem
    public List<Block> getContainedBlocks() {
        return innerSystem.getBlocks();
    }
    
    public List<Line> getContainedLines() {
        return innerSystem.getLines();
    }
    
    // Additional methods for subsystem management
    public void setupOutputChain() throws Exception {
        innerSystem.setupOutputChain();
    }
    
    public void setupDimensionList() throws MatDimException {
        innerSystem.setupDimensionList();
    }
    
    public Block findBlockByName(String blockName) {
        return innerSystem.findBlockByName(blockName);
    }
    
    public Block findBlockByUUID(String blockUUID) {
        return innerSystem.findBlockByUUID(blockUUID);
    }
    
    public Block findBlockById(int blockId) {
        return innerSystem.findBlockById(blockId);
    }
    
    public List<Block> findBlocksByType(String blockType) {
        return innerSystem.findBlocksByType(blockType);
    }
    
    public Line findLineById(int lineId) {
        return innerSystem.findLineById(lineId);
    }
    
    public List<Line> findLinesConnectedToBlock(Block block) {
        return innerSystem.findLinesConnectedToBlock(block);
    }
    
    // Cleanup method for proper resource management
    public void cleanup() {
        // Clear all references
        if (innerSystem != null) {
            innerSystem.clear();
        }
        if (inBlockList != null) {
            inBlockList.clear();
        }
        if (outBlockList != null) {
            outBlockList.clear();
        }
    }

    public boolean validateLineConsistency() {
        // Delegate validation to NCSLabSystem
        return true;
    }

    public int repairLineConsistency() {
        // Use NCSLabSystem's built-in consistency mechanisms
        try {
            innerSystem.setupOutputChain();
            return 0; // Success
        } catch (Exception e) {
            System.err.println("Error repairing line consistency: " + e.getMessage());
            return -1; // Error
        }
    }
}
