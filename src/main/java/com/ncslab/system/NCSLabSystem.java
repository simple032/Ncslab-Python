package com.ncslab.system;

import com.ncslab.block.Block;
import com.ncslab.line.Line;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.sink.Scope;
import com.ncslab.ncslablink.MatDimException;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * NCSLabSystem class - Core logical operations for block-based modeling
 * 
 * This class handles the fundamental simulation logic that is shared between
 * Models and Subsystems. It manages blocks, lines, execution order, and 
 * dependency analysis in a reusable way.
 * 
 * Key Design Principles:
 * - A Model IS a NCSLabSystem (Model contains/composes a NCSLabSystem)
 * - A Subsystem block CONTAINS a NCSLabSystem (Subsystem contains a NCSLabSystem)
 * - NCSLabSystem handles pure logical operations without model-specific concerns
 * 
 * Architecture Benefits:
 * - Eliminates code duplication between Model and Subsystem
 * - Provides consistent execution behavior everywhere
 * - Centralizes core simulation logic for easier maintenance
 * - Enables easy testing of system logic independently
 */
public class NCSLabSystem {
    
    // ===== CORE SYSTEM COMPONENTS =====
    
    /**
     * All blocks within this system (thread-safe for concurrent access)
     */
    @Getter
    private final List<Block> blocks = new CopyOnWriteArrayList<>();
    
    /**
     * All lines within this system
     */
    @Getter
    private final List<Line> lines = new ArrayList<>();
    
    /**
     * Execution order chain - determines the order blocks are calculated
     * This is the dependency-resolved execution sequence
     */
    @Getter
    private final List<Block> outputChain = new ArrayList<>();
    
    /**
     * Terminal blocks - blocks with no outputs or only terminal outputs
     * These are the starting points for dependency analysis
     */
    @Getter
    private final List<Block> terminalBlocks = new ArrayList<>();
    
    // ===== DEPENDENCY ANALYSIS STATE =====
    
    /**
     * Tracks if algebraic loops have been detected during dependency analysis
     */
    @Getter
    private boolean isAlgebraicLoop = false;
    
    /**
     * Working list for dependency analysis - tracks output ports during scanning
     */
    private final List<OutputPort> outputPortPathList = new ArrayList<>();
    
    /**
     * Working list for dependency analysis - tracks blocks during scanning
     */
    private final List<Block> scanBlockList = new ArrayList<>();
    
    /**
     * Specialized dependency analyzer for improved separation of concerns
     */
    private final DependencyAnalyzer dependencyAnalyzer = new DependencyAnalyzer();
    
    // ===== DIMENSION PROCESSING STATE =====
    
    /**
     * List of blocks organized by dimension processing order
     */
    private final List<Block> dimensionList = new ArrayList<>();
    
    /**
     * Working list for dimension scanning - tracks blocks that need dimension processing
     */
    private final List<Block> scanDimList = new ArrayList<>();
    
    /**
     * Terminal blocks for dimension processing - blocks with no outputs that need dimension analysis
     */
    private final List<Block> dimTerminalBlockList = new ArrayList<>();
    
    /**
     * Working list for dimension scanning - tracks output ports during dimension analysis
     */
    private final List<OutputPort> dimOutputPortPathList = new ArrayList<>();
    
    // ===== SYSTEM STATE VARIABLES =====
    
    /**
     * Sequence counters for generating unique IDs
     */
    @Getter
    @Setter
    private int blockSeq = 0;
    
    @Getter
    @Setter
    private int lineSeq = 0;
    
    /**
     * System state variables - core simulation state
     */
    @Getter
    private int stateNum = 0;
    
    @Getter
    private int singleStateNum = 0;
    
    @Getter
    private int matrixStateNum = 0;
    
    @Getter
    @Setter
    private int signalNum = 0;
    
    @Getter
    @Setter
    private int parameterNum = 0;
    
    @Getter
    private int inputNum = 0;
    
    @Getter
    private int outputNum = 0;
    
    // ===== SYSTEM MANAGEMENT METHODS =====
    
    /**
     * Add a block to this system
     * 
     * @param block The block to add
     */
    public void addBlock(Block block) {
        if (block != null && !blocks.contains(block)) {
            blocks.add(block);
        }
    }
    
    /**
     * Remove a block from this system
     * 
     * @param block The block to remove
     */
    public void removeBlock(Block block) {
        blocks.remove(block);
        outputChain.remove(block);
        terminalBlocks.remove(block);
        scanBlockList.remove(block);
    }
    
    /**
     * Add a line to this system
     * 
     * @param line The line to add
     */
    public void addLine(Line line) {
        if (line != null && !lines.contains(line)) {
            lines.add(line);
        }
    }
    
    /**
     * Remove a line from this system
     * 
     * @param line The line to remove
     */
    public void removeLine(Line line) {
        lines.remove(line);
    }
    
    /**
     * Clear all blocks and lines from this system
     */
    public void clear() {
        blocks.clear();
        lines.clear();
        outputChain.clear();
        terminalBlocks.clear();
        outputPortPathList.clear();
        scanBlockList.clear();
        isAlgebraicLoop = false;
        
        // Clear dimension processing state as well
        clearDimensionState();
    }
    
    // ===== CORE EXECUTION METHODS =====
    
    /**
     * Initialize all blocks in the system
     * Similar to Simulink's mdlInitialize phase
     * 
     * @param t0 Initial time
     */
    public void calculateInit(double t0) {
        for (Block block : blocks) {
            block.calculateInit();
        }
    }
    
    /**
     * Calculate outputs for all blocks in dependency order
     * Similar to Simulink's mdlOutputs phase
     * 
     * @param t Current simulation time
     */
    public void calculateOutput(double t) {
        // Execute blocks in dependency-resolved order
        for (Block block : outputChain) {
            block.calculateOutput(t);
        }
    }
    
    /**
     * Calculate derivatives for all blocks
     * Similar to Simulink's mdlDerivatives phase
     * 
     * @param t Current simulation time
     */
    public void calculateDerivative(double t) {
        for (Block block : blocks) {
            block.calculateDerivative(t);
        }
    }
    
    /**
     * Update discrete states for all blocks
     * Similar to Simulink's mdlUpdate phase
     * 
     * @param t Current simulation time
     */
    public void calculateDiscreteUpdate(double t) {
        for (Block block : blocks) {
            block.calculateDiscreteUpdate(t);
        }
    }

    /**
     * Terminate all blocks in the system
     * Similar to Simulink's mdlTerminate phase
     *
     * @param t Current simulation time
     */
    public void calculateTerminate(double t) {
        for (Block block : blocks) {
            block.calculateTerminate(t);
        }
    }

    // ===== EXECUTION ORDER AND DEPENDENCY ANALYSIS =====
    
    /**
     * Setup the execution order chain by analyzing block dependencies
     * This is the main method that establishes proper execution sequence
     */
    public void setupOutputChain() {
        // Clear previous analysis
        outputChain.clear();
        terminalBlocks.clear();
        outputPortPathList.clear();
        scanBlockList.clear();
        isAlgebraicLoop = false;
        
        System.out.println("NCSLabSystem: Setting up output chain using dependency analyzer...");
        
        // Use the new dependency analyzer for improved analysis
        ExecutionOrderResult result = dependencyAnalyzer.analyzeAndResolve(blocks);
        
        // Update our state based on the analysis
        outputChain.addAll(result.getExecutionOrder());
        isAlgebraicLoop = result.hasAlgebraicLoops();
        
        // Still maintain terminal blocks for backward compatibility
        terminalBlocks.addAll(dependencyAnalyzer.getTerminalBlocks(blocks));
        
        System.out.printf("NCSLabSystem execution chain setup complete: %d blocks in execution order, algebraic loops: %s%n", 
                         outputChain.size(), isAlgebraicLoop ? "YES" : "NO");
        
        // Print execution order for debugging
        printExecutionChain();
    }
    
    /**
     * Print the execution chain for debugging purposes
     */
    private void printExecutionChain() {
        System.out.println("=== OUTPUT CHAIN EXECUTION ORDER ===");
        for (int i = 0; i < outputChain.size(); i++) {
            Block block = outputChain.get(i);
            String dependencies = block.getInputPortList().isEmpty() ? "source" : 
                "depends on " + block.getInputPortList().size() + " inputs";
            System.out.println(String.format("  %d. Block(%d): %s [%s] (%s)", 
                i + 1, block.getBlockId(), block.getBlockName(), 
                block.getClass().getSimpleName(), dependencies));
        }
        System.out.println("=== END OUTPUT CHAIN (" + outputChain.size() + " blocks) ===");
    }
    
    /**
     * Find all terminal blocks in the system
     * Terminal blocks are those with no outputs or only terminal outputs
     */
    private void findTerminalBlocks() {
        System.out.println("Looking for terminal blocks in NCSLabSystem");
        for (Block block : blocks) {
            if (block.isTerminalBlock()) {
                System.out.println("Found terminal block (" + block.getBlockId() + "): " + block.getBlockName());
                terminalBlocks.add(block);
            }
        }
    }
    
    /**
     * Scan the output chain by traversing from terminal blocks backwards through dependencies
     */
    private void scanOutputChain() {
        System.out.println("Scanning output chain for NCSLabSystem");
        
        // Traverse all terminal blocks
        for (Block block : terminalBlocks) {
            // Add the terminal block itself to the output chain if not already present
            if (!outputChain.contains(block)) {
                System.out.println("Adding terminal block (" + block.getBlockId() + "): " + block.getBlockName() + " to output chain");
                outputChain.add(block);
            }
            
            // Debug: Print what feeds into this terminal block
            List<InputPort> inputPortList = block.getInputPortList();
            for (InputPort inputPort : inputPortList) {
                Line line = inputPort.getLinkedLine();
                if (line != null && line.getLinkedOutputPort() != null) {
                    Block sourceBlock = line.getLinkedOutputPort().getBlock();
                    System.out.println("DEBUG: Terminal " + block.getBlockName() + " gets input from " + sourceBlock.getBlockName());
                }
                scanInputPort(inputPort);
            }
        }
        
        // Sort output chain by dependency order (sources first, then dependents)
        sortOutputChainByDependencies();
        
        // Print complete output chain after scanning and sorting
        System.out.println("=== OUTPUT CHAIN EXECUTION ORDER ===");
        for (int i = 0; i < outputChain.size(); i++) {
            Block block = outputChain.get(i);
            String dependencies = block.getInputPortList().isEmpty() ? "source" : 
                "depends on " + block.getInputPortList().size() + " inputs";
            System.out.println(String.format("  %d. Block(%d): %s [%s] (%s)", 
                i + 1, block.getBlockId(), block.getBlockName(), 
                block.getClass().getSimpleName(), dependencies));
        }
        System.out.println("=== END OUTPUT CHAIN (" + outputChain.size() + " blocks) ===");
    }
    
    /**
     * Sort output chain by dependency order using proper topological sorting
     * This ensures blocks execute in the correct order based on actual connections
     */
    private void sortOutputChainByDependencies() {
        List<Block> sortedChain = new ArrayList<>();
        Set<Block> visited = new HashSet<>();
        Set<Block> visiting = new HashSet<>();
        
        // Start with blocks that have no inputs (source blocks)
        List<Block> sourceBlocks = new ArrayList<>();
        for (Block block : outputChain) {
            if (block.getInputPortList().isEmpty()) {
                sourceBlocks.add(block);
            }
        }
        
        // Sort source blocks by ID for deterministic order
        sourceBlocks.sort((a, b) -> Integer.compare(a.getBlockId(), b.getBlockId()));
        
        // Perform topological sort using DFS
        for (Block sourceBlock : sourceBlocks) {
            if (!visited.contains(sourceBlock)) {
                topologicalSortDFS(sourceBlock, visited, visiting, sortedChain);
            }
        }
        
        // Handle any remaining blocks (in case of disconnected components)
        for (Block block : outputChain) {
            if (!visited.contains(block)) {
                topologicalSortDFS(block, visited, visiting, sortedChain);
            }
        }
        
        // Replace output chain with properly sorted version
        outputChain.clear();
        outputChain.addAll(sortedChain);
    }
    
    /**
     * Depth-first search for topological sorting
     * Ensures blocks are ordered based on their actual dependencies
     * Handles loops (algebraic loops) gracefully
     */
    private void topologicalSortDFS(Block block, Set<Block> visited, Set<Block> visiting, List<Block> sortedChain) {
        if (visiting.contains(block)) {
            // Cycle detected - algebraic loop
            System.out.println("Warning: Algebraic loop detected involving block: " + block.getBlockName());
            isAlgebraicLoop = true;
            return;
        }
        
        if (visited.contains(block)) {
            return; // Already processed
        }
        
        visiting.add(block);
        
        // Visit all blocks that depend on this block's outputs
        for (OutputPort outputPort : block.getOutputPortList()) {
            // OutputPort has a list of linked lines, not a single line
            for (Line line : outputPort.getLinkedLineList()) {
                if (line != null && line.getLinkedInputPort() != null) {
                    Block dependentBlock = line.getLinkedInputPort().getBlock();
                    if (outputChain.contains(dependentBlock)) {
                        topologicalSortDFS(dependentBlock, visited, visiting, sortedChain);
                    }
                }
            }
        }
        
        visiting.remove(block);
        visited.add(block);
        
        // Add block to the beginning of sorted chain (reverse post-order)
        sortedChain.add(0, block);
    }
    
    /**
     * Recursively scan input ports to build dependency chain and detect algebraic loops
     * 
     * @param inputPort The input port to scan
     */
    private void scanInputPort(InputPort inputPort) {
        Line line = inputPort.getLinkedLine();
        if (line == null) return;
        
        OutputPort outputPort = line.getLinkedOutputPort();
        if (outputPort == null) return;
        
        // If output port already generated, skip this branch
        if (outputPort.getIsCodeGenerated()) {
            return;
        }
        
        // Check for algebraic loops
        for (OutputPort output : outputPortPathList) {
            if (output == outputPort) {
                isAlgebraicLoop = true;
                System.err.println("Algebraic loop detected in NCSLabSystem!");
                return;
            }
        }
        
        // Add to path for loop detection
        outputPortPathList.add(outputPort);
        
        Block sourceBlock = outputPort.getBlock();
        
        // Add source block to execution chain if not already present
        if (!outputChain.contains(sourceBlock)) {
            System.out.println("Adding block (" + sourceBlock.getBlockId() + "): " + sourceBlock.getBlockName() + " to output chain");
            outputChain.add(sourceBlock);
        }
        
        // Recursively scan inputs of the source block
        List<InputPort> inputPortList = sourceBlock.getInputPortList();
        for (InputPort inputPortOfSourceBlock : inputPortList) {
            scanInputPort(inputPortOfSourceBlock);
        }
        
        // Mark output port as generated
        outputPort.setIsCodeGenerated(true);
        
        // Remove from path (backtrack)
        outputPortPathList.remove(outputPort);
    }
    
    // ===== SYSTEM STATE AND INFORMATION =====
    
    /**
     * Get the number of blocks in this system
     * 
     * @return Number of blocks
     */
    public int getBlockCount() {
        return blocks.size();
    }
    
    /**
     * Get the number of lines in this system
     * 
     * @return Number of lines
     */
    public int getLineCount() {
        return lines.size();
    }
    
    /**
     * Check if the system has any blocks
     * 
     * @return true if system has blocks, false otherwise
     */
    public boolean hasBlocks() {
        return !blocks.isEmpty();
    }
    
    /**
     * Check if the system has any lines
     * 
     * @return true if system has lines, false otherwise
     */
    public boolean hasLines() {
        return !lines.isEmpty();
    }
    
    /**
     * Get execution order as a list of block names (for debugging)
     * 
     * @return List of block names in execution order
     */
    public List<String> getExecutionOrderNames() {
        List<String> names = new ArrayList<>();
        for (Block block : outputChain) {
            names.add(block.getBlockName());
        }
        return names;
    }
    
    /**
     * Check if system is ready for execution (has blocks and valid execution chain)
     * 
     * @return true if ready for execution, false otherwise
     */
    public boolean isReadyForExecution() {
        return hasBlocks() && !outputChain.isEmpty() && !isAlgebraicLoop;
    }
    
    // ===== BLOCK SEARCH AND MANAGEMENT METHODS =====
    
    /**
     * Find a block by name within this system
     * 
     * @param blockName Name of the block to find
     * @return The block if found, null otherwise
     */
    public Block findBlockByName(String blockName) {
        for (Block block : blocks) {
            if (block.getBlockName().equals(blockName)) {
                return block;
            }
        }
        return null;
    }
    
    /**
     * Find a block by UUID within this system
     * 
     * @param blockUUID UUID of the block to find
     * @return The block if found, null otherwise
     */
    public Block findBlockByUUID(String blockUUID) {
        if (blockUUID == null) return null;
        for (Block block : blocks) {
            if (blockUUID.equals(block.getBlockUUID())) {
                return block;
            }
        }
        return null;
    }
    
    /**
     * Find a block by ID within this system
     * 
     * @param blockId ID of the block to find
     * @return The block if found, null otherwise
     */
    public Block findBlockById(int blockId) {
        for (Block block : blocks) {
            if (block.getBlockId() == blockId) {
                return block;
            }
        }
        return null;
    }
    
    /**
     * Find blocks by type within this system
     * 
     * @param blockType Type of blocks to find
     * @return List of blocks matching the type
     */
    public List<Block> findBlocksByType(String blockType) {
        List<Block> matchingBlocks = new ArrayList<>();
        for (Block block : blocks) {
            if (block.getBlockType().equals(blockType)) {
                matchingBlocks.add(block);
            }
        }
        return matchingBlocks;
    }
    
    // ===== LINE SEARCH AND MANAGEMENT METHODS =====
    
    /**
     * Find a line by ID within this system
     * 
     * @param lineId ID of the line to find
     * @return The line if found, null otherwise
     */
    public Line findLineById(int lineId) {
        for (Line line : lines) {
            if (line.getLineId() == lineId) {
                return line;
            }
        }
        return null;
    }
    
    /**
     * Find lines connected to a specific block
     * 
     * @param block The block to find connected lines for
     * @return List of lines connected to the block
     */
    public List<Line> findLinesConnectedToBlock(Block block) {
        List<Line> connectedLines = new ArrayList<>();
        for (Line line : lines) {
            if (line.getLinkedOutputPort() != null && line.getLinkedOutputPort().getBlock() == block) {
                connectedLines.add(line);
            }
            if (line.getLinkedInputPort() != null && line.getLinkedInputPort().getBlock() == block) {
                connectedLines.add(line);
            }
        }
        return connectedLines;
    }
    
    // ===== VALIDATION METHODS =====
    
    /**
     * Validate that all blocks have unique names within this system
     * 
     * @return List of validation errors, empty if no errors
     */
    public List<String> validateBlockNames() {
        List<String> errors = new ArrayList<>();
        for (int i = 0; i < blocks.size(); i++) {
            Block block1 = blocks.get(i);
            for (int j = i + 1; j < blocks.size(); j++) {
                Block block2 = blocks.get(j);
                if (block1.getBlockName().equals(block2.getBlockName())) {
                    errors.add("Duplicate block name: " + block1.getBlockName());
                }
            }
        }
        return errors;
    }
    
    /**
     * Validate that all blocks have unique UUIDs within this system
     * 
     * @return List of validation errors, empty if no errors
     */
    public List<String> validateBlockUUIDs() {
        List<String> errors = new ArrayList<>();
        for (int i = 0; i < blocks.size(); i++) {
            Block block1 = blocks.get(i);
            if (block1.getBlockUUID() == null) continue;
            for (int j = i + 1; j < blocks.size(); j++) {
                Block block2 = blocks.get(j);
                if (block1.getBlockUUID().equals(block2.getBlockUUID())) {
                    errors.add("Duplicate block UUID: " + block1.getBlockUUID());
                }
            }
        }
        return errors;
    }
    
    // ===== SYSTEM STATE CALCULATION METHODS =====
    
    /**
     * Calculate and update all system state variables
     * This method analyzes all blocks to determine state, signal, and parameter counts
     */
    public void calculateSystemState() {
        int singleStateNum = 0;
        int matrixStateNum = 0;
        int signalNum = 0;
        int parameterNum = 0;
        int inputNum = 0;
        int outputNum = 0;
        
        for (Block block : blocks) {
            // Calculate state numbers
            for (com.ncslab.block.io.State state : block.getStateList()) {
                switch (state.getDataType()) {
                    case REAL:
                        singleStateNum++;
                        break;
                    case MATRIX:
                        matrixStateNum++;
                        break;
                }
            }
            
            // Calculate signal numbers
            int blockSignalNum = block.getOutputPortList().size() + block.getInputPortList().size();
            signalNum += blockSignalNum;
            block.setSignalNum(blockSignalNum);
            
            // Calculate parameter numbers
            parameterNum += block.getParameterList().size();
            
            // Calculate input/output numbers
            inputNum += block.getInputPortList().size();
            outputNum += block.getOutputPortList().size();
        }
        
        // Update system state
        setStateNum(singleStateNum, matrixStateNum);
        this.signalNum = signalNum;
        this.parameterNum = parameterNum;
        this.inputNum = inputNum;
        this.outputNum = outputNum;
    }
    
    /**
     * Set state numbers for the system
     * @param singleStateNum Number of single (scalar) state variables
     * @param matrixStateNum Number of matrix state variables
     */
    private void setStateNum(int singleStateNum, int matrixStateNum) {
        this.stateNum = singleStateNum + matrixStateNum;
        this.singleStateNum = singleStateNum;
        this.matrixStateNum = matrixStateNum;
    }
    
    /**
     * Get next available block sequence number
     * @return Next block sequence number
     */
    public int getNextBlockSeq() {
        return ++blockSeq;
    }
    
    /**
     * Get next available line sequence number
     * @return Next line sequence number
     */
    public int getNextLineSeq() {
        return ++lineSeq;
    }
    
    // ===== DIMENSION PROCESSING METHODS =====
    
    /**
     * Setup the dimension list by analyzing block dimensions and dependencies
     * This method establishes the order for dimension processing
     */
    public void setupDimensionList() {
        System.out.println("NCSLabSystem: Setting up dimension list...");
        
        // Clear previous dimension analysis
        dimensionList.clear();
        dimTerminalBlockList.clear();
        scanDimList.clear();
        dimOutputPortPathList.clear();
        
        // Find terminal blocks for dimension processing
        findDimTerminalBlocks();
        
        // Build dimension processing chain
        scanDimChain();
        
        System.out.printf("NCSLabSystem: Dimension list setup complete - %d blocks in dimensionList%n", dimensionList.size());
        showDimBlocks();
    }
    
    /**
     * Update dimensions for all blocks in the dimension processing order
     * This ensures dimension consistency across all blocks
     * 
     * @throws MatDimException if dimension conflicts are detected
     */
    public void updateDimensions() throws MatDimException {
        // First pass: Update dimensions for all blocks
        for (Block block : dimensionList) {
            block.updateDimension();
        }
        
        // Second pass: Check dimension consistency
        for (Block block : dimensionList) {
            block.checkDimension();
        }
    }
    
    /**
     * Find all terminal blocks that need dimension processing
     * These are typically Scope blocks and other terminal blocks
     */
    private void findDimTerminalBlocks() {
        System.out.printf("NCSLabSystem: Looking for terminal blocks in %d total blocks%n", blocks.size());
        int terminalCount = 0;
        int scopeCount = 0;
        
        for (Block block : blocks) {
            if (block instanceof Scope) {
                scopeCount++;
                System.out.printf("NCSLabSystem: Found Scope block '%s'%n", block.getBlockName());
            }
            if (block.isTerminalBlock()) {
                terminalCount++;
                System.out.printf("NCSLabSystem: Found terminal block '%s' (type=%s)%n", 
                    block.getBlockName(), block.getBlockType());
                dimTerminalBlockList.add(block);
            }
        }
        System.out.printf("NCSLabSystem: Total scope blocks found: %d, terminal blocks found: %d%n", scopeCount, terminalCount);
    }
    
    /**
     * Scan the dimension chain by traversing from terminal blocks backwards through dependencies
     */
    private void scanDimChain() {
        // Traverse all terminal blocks for dimension processing
        for (Block block : dimTerminalBlockList) {
            block.setIsDimScaned(true);
            List<InputPort> inputPortList = block.getInputPortList();
            for (InputPort inputPort : inputPortList) {
                scanDimInputPort(inputPort);
            }
            dimensionList.add(block);
        }
        
        // Second pass traversal for blocks with complex dimension dependencies
        while (!scanDimList.isEmpty()) {
            Block block = scanDimList.remove(0);
            if (block.getIsDimScaned()) {
                continue;
            }
            block.setIsDimScaned(true);
            List<InputPort> inputPortList = block.getInputPortList();
            for (InputPort inputPort : inputPortList) {
                scanDimInputPort(inputPort);
            }
            dimensionList.add(block);
        }
    }
    
    /**
     * Recursively scan input ports for dimension processing dependencies
     * 
     * @param inputPort The input port to scan for dimension dependencies
     */
    private void scanDimInputPort(InputPort inputPort) {
        Line line = inputPort.getLinkedLine();
        if (line == null) return;
        
        OutputPort outputPort = line.getLinkedOutputPort();
        if (outputPort == null) return;
        
        // If output port already processed for dimensions, skip this branch
        if (outputPort.getIsDimScaned()) {
            return;
        }
        
        // Check for dimension processing loops
        for (OutputPort output : dimOutputPortPathList) {
            if (output == outputPort) {
                return; // Skip to avoid infinite loops
            }
        }
        
        // Add to path for loop detection
        dimOutputPortPathList.add(outputPort);
        
        Block block = outputPort.getBlock();
        
        // Check if this block has dimension feedthrough behavior
        boolean isDimThroughBlock = false;
        List<OutputPort> outputPortList = block.getOutputPortList();
        for (OutputPort output : outputPortList) {
            if (output.getDimThrough()) {
                isDimThroughBlock = true;
                break;
            }
        }
        
        if (isDimThroughBlock) {
            // Block with dimension feedthrough - must process all inputs
            block.setIsDimScaned(true);
            List<InputPort> inputPortList = block.getInputPortList();
            for (InputPort input : inputPortList) {
                scanDimInputPort(input);
            }
            dimensionList.add(block);
        } else {
            // Block without dimension feedthrough - process first input, queue others
            block.setIsDimScaned(true);
            List<InputPort> inputPortList = block.getInputPortList();
            
            // Process first input for dimension compatibility
            if (!inputPortList.isEmpty()) {
                scanDimInputPort(inputPortList.get(0));
            }
            
            // Queue remaining inputs for second pass
            if (inputPortList.size() > 1) {
                for (int i = 1; i < inputPortList.size(); i++) {
                    InputPort input = inputPortList.get(i);
                    if (input.getLinkedLine() != null && input.getLinkedLine().getLinkedOutputPort() != null) {
                        Block linkedBlock = input.getLinkedLine().getLinkedOutputPort().getBlock();
                        if (!linkedBlock.getIsDimScaned()) {
                            scanDimList.add(linkedBlock);
                        }
                    }
                }
            }
            
            dimensionList.add(block);
        }
        
        // Remove from path (backtrack)
        dimOutputPortPathList.remove(dimOutputPortPathList.size() - 1);
    }
    
    /**
     * Display dimension blocks for debugging (similar to original showDimBlocks)
     */
    private void showDimBlocks() {
        int i = 1;
        for (Block block : dimensionList) {
            System.out.printf("NCSLabSystem: Dimension block (%d): %s (type=%s)%n", 
                i, block.getBlockName(), block.getBlockType());
            i++;
        }
    }
    
    /**
     * Get the dimension processing list
     * @return List of blocks in dimension processing order
     */
    public List<Block> getDimensionList() {
        return new ArrayList<>(dimensionList);
    }
    
    /**
     * Clear dimension processing state
     */
    private void clearDimensionState() {
        dimensionList.clear();
        dimTerminalBlockList.clear();
        scanDimList.clear();
        dimOutputPortPathList.clear();
        
        // Reset dimension scanning flags on all blocks
        for (Block block : blocks) {
            block.setIsDimScaned(false);
            for (OutputPort outputPort : block.getOutputPortList()) {
                outputPort.setIsDimScaned(false);
            }
        }
    }
    
    @Override
    public String toString() {
        return String.format("NCSLabSystem[blocks=%d, lines=%d, executionOrder=%d, algebraicLoop=%s, states=%d, signals=%d]", 
            blocks.size(), lines.size(), outputChain.size(), isAlgebraicLoop, stateNum, signalNum);
    }
}