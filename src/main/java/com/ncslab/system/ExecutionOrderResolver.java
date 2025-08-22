package com.ncslab.system;

import com.ncslab.block.Block;

import java.util.*;

/**
 * Resolves the execution order for blocks based on their dependencies
 * Implements topological sorting with cycle detection and handling
 */
public class ExecutionOrderResolver {
    
    private final DependencyGraph dependencyGraph;
    private boolean algebraicLoopsDetected = false;
    private final List<List<Block>> detectedCycles = new ArrayList<>();
    
    public ExecutionOrderResolver(DependencyGraph dependencyGraph) {
        this.dependencyGraph = dependencyGraph;
    }
    
    /**
     * Resolve the execution order for blocks using topological sorting
     * @return List of blocks in execution order (dependencies before dependents)
     */
    public ExecutionOrderResult resolveExecutionOrder() {
        algebraicLoopsDetected = false;
        detectedCycles.clear();
        
        List<Block> executionOrder = new ArrayList<>();
        Set<Block> visited = new HashSet<>();
        Set<Block> visiting = new HashSet<>();
        
        // Start with source blocks (no dependencies)
        List<Block> sourceBlocks = dependencyGraph.getSourceBlocks();
        
        System.out.printf("Starting execution order resolution with %d source blocks%n", sourceBlocks.size());
        
        // Process source blocks first
        for (Block sourceBlock : sourceBlocks) {
            if (!visited.contains(sourceBlock)) {
                topologicalSortDFS(sourceBlock, visited, visiting, executionOrder);
            }
        }
        
        // Handle any remaining blocks (disconnected components or cycles)
        Set<Block> allBlocks = dependencyGraph.getAllBlocks();
        for (Block block : allBlocks) {
            if (!visited.contains(block)) {
                topologicalSortDFS(block, visited, visiting, executionOrder);
            }
        }
        
        // Detect and report cycles
        detectAlgebraicLoops();
        
        System.out.printf("Execution order resolved: %d blocks, algebraic loops: %s%n", 
                         executionOrder.size(), algebraicLoopsDetected ? "YES" : "NO");
        
        return new ExecutionOrderResult(executionOrder, algebraicLoopsDetected, detectedCycles);
    }
    
    /**
     * Depth-first search for topological sorting
     */
    private void topologicalSortDFS(Block block, Set<Block> visited, Set<Block> visiting, List<Block> executionOrder) {
        if (visiting.contains(block)) {
            // Cycle detected during DFS traversal
            System.out.printf("Warning: Cycle detected during DFS at block: %s%n", block.getBlockName());
            algebraicLoopsDetected = true;
            return;
        }
        
        if (visited.contains(block)) {
            return; // Already processed
        }
        
        visiting.add(block);
        
        // Visit all dependent blocks first (post-order traversal)
        Set<Block> dependents = dependencyGraph.getDependents(block);
        for (Block dependent : dependents) {
            topologicalSortDFS(dependent, visited, visiting, executionOrder);
        }
        
        visiting.remove(block);
        visited.add(block);
        
        // Add block to the beginning of execution order (reverse post-order)
        // This ensures dependencies are executed before dependents
        executionOrder.add(0, block);
    }
    
    /**
     * Detect algebraic loops using comprehensive cycle detection
     */
    private void detectAlgebraicLoops() {
        List<List<Block>> cycles = dependencyGraph.findCycles();
        
        if (!cycles.isEmpty()) {
            algebraicLoopsDetected = true;
            detectedCycles.addAll(cycles);
            
            System.out.println("=== ALGEBRAIC LOOPS DETECTED ===");
            for (int i = 0; i < cycles.size(); i++) {
                List<Block> cycle = cycles.get(i);
                System.out.printf("Loop %d: %s%n", i + 1, 
                    cycle.stream().map(Block::getBlockName).reduce((a, b) -> a + " → " + b).orElse(""));
            }
            System.out.println("=== END ALGEBRAIC LOOPS ===");
        }
    }
    
    /**
     * Alternative execution order resolution using Kahn's algorithm
     * More suitable for handling cycles gracefully
     */
    public ExecutionOrderResult resolveExecutionOrderKahn() {
        algebraicLoopsDetected = false;
        detectedCycles.clear();
        
        List<Block> executionOrder = new ArrayList<>();
        Queue<Block> zeroInDegreeQueue = new LinkedList<>();
        Map<Block, Integer> inDegreeCount = new HashMap<>();
        
        // Initialize in-degree count for all blocks
        Set<Block> allBlocks = dependencyGraph.getAllBlocks();
        for (Block block : allBlocks) {
            int inDegree = dependencyGraph.getDependencies(block).size();
            inDegreeCount.put(block, inDegree);
            
            if (inDegree == 0) {
                zeroInDegreeQueue.offer(block);
            }
        }
        
        System.out.printf("Kahn's algorithm: Starting with %d zero-degree blocks%n", zeroInDegreeQueue.size());
        
        // Process blocks with zero in-degree
        while (!zeroInDegreeQueue.isEmpty()) {
            Block currentBlock = zeroInDegreeQueue.poll();
            executionOrder.add(currentBlock);
            
            // Reduce in-degree for all dependent blocks
            Set<Block> dependents = dependencyGraph.getDependents(currentBlock);
            for (Block dependent : dependents) {
                int newInDegree = inDegreeCount.get(dependent) - 1;
                inDegreeCount.put(dependent, newInDegree);
                
                if (newInDegree == 0) {
                    zeroInDegreeQueue.offer(dependent);
                }
            }
        }
        
        // Check for remaining blocks (indicates cycles)
        if (executionOrder.size() < allBlocks.size()) {
            algebraicLoopsDetected = true;
            
            // Add remaining blocks to execution order (may not be optimal)
            for (Block block : allBlocks) {
                if (!executionOrder.contains(block)) {
                    executionOrder.add(block);
                    System.out.printf("Warning: Block %s involved in algebraic loop%n", block.getBlockName());
                }
            }
            
            // Find and report specific cycles
            detectedCycles.addAll(dependencyGraph.findCycles());
        }
        
        System.out.printf("Kahn's execution order resolved: %d blocks, algebraic loops: %s%n", 
                         executionOrder.size(), algebraicLoopsDetected ? "YES" : "NO");
        
        return new ExecutionOrderResult(executionOrder, algebraicLoopsDetected, detectedCycles);
    }
    
    /**
     * Get the current state of algebraic loop detection
     */
    public boolean hasAlgebraicLoops() {
        return algebraicLoopsDetected;
    }
    
    /**
     * Get the detected cycles
     */
    public List<List<Block>> getDetectedCycles() {
        return Collections.unmodifiableList(detectedCycles);
    }
}