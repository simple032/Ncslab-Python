package com.ncslab.system;

import com.ncslab.block.Block;

import java.util.Collections;
import java.util.List;

/**
 * Result of execution order resolution containing the ordered blocks and loop information
 */
public class ExecutionOrderResult {
    
    private final List<Block> executionOrder;
    private final boolean hasAlgebraicLoops;
    private final List<List<Block>> algebraicLoops;
    
    public ExecutionOrderResult(List<Block> executionOrder, boolean hasAlgebraicLoops, 
                               List<List<Block>> algebraicLoops) {
        this.executionOrder = Collections.unmodifiableList(executionOrder);
        this.hasAlgebraicLoops = hasAlgebraicLoops;
        this.algebraicLoops = Collections.unmodifiableList(algebraicLoops);
    }
    
    /**
     * Get the resolved execution order
     * @return List of blocks in execution order
     */
    public List<Block> getExecutionOrder() {
        return executionOrder;
    }
    
    /**
     * Check if algebraic loops were detected
     * @return true if algebraic loops exist
     */
    public boolean hasAlgebraicLoops() {
        return hasAlgebraicLoops;
    }
    
    /**
     * Get the detected algebraic loops
     * @return List of loops, each loop is a list of blocks
     */
    public List<List<Block>> getAlgebraicLoops() {
        return algebraicLoops;
    }
    
    /**
     * Get the number of blocks in execution order
     * @return block count
     */
    public int getBlockCount() {
        return executionOrder.size();
    }
    
    /**
     * Get the number of detected algebraic loops
     * @return loop count
     */
    public int getAlgebraicLoopCount() {
        return algebraicLoops.size();
    }
    
    /**
     * Print execution order and loop information
     */
    public void printResults() {
        System.out.println("=== EXECUTION ORDER RESULT ===");
        System.out.printf("Total blocks: %d%n", executionOrder.size());
        System.out.printf("Algebraic loops detected: %s%n", hasAlgebraicLoops ? "YES" : "NO");
        
        if (hasAlgebraicLoops) {
            System.out.printf("Number of loops: %d%n", algebraicLoops.size());
        }
        
        System.out.println("Execution order:");
        for (int i = 0; i < executionOrder.size(); i++) {
            Block block = executionOrder.get(i);
            System.out.printf("  %d. Block(%d): %s [%s]%n", 
                i + 1, block.getBlockId(), block.getBlockName(), 
                block.getClass().getSimpleName());
        }
        
        if (hasAlgebraicLoops) {
            System.out.println("Algebraic loops:");
            for (int i = 0; i < algebraicLoops.size(); i++) {
                List<Block> loop = algebraicLoops.get(i);
                System.out.printf("  Loop %d: %s%n", i + 1,
                    loop.stream().map(Block::getBlockName).reduce((a, b) -> a + " → " + b).orElse(""));
            }
        }
        
        System.out.println("=== END EXECUTION ORDER RESULT ===");
    }
}