package com.ncslab.system;

import com.ncslab.block.Block;

import java.util.List;

/**
 * High-level facade for dependency analysis and execution order resolution
 * Coordinates between DependencyGraph and ExecutionOrderResolver
 */
public class DependencyAnalyzer {
    
    private final DependencyGraph dependencyGraph;
    private final ExecutionOrderResolver resolver;
    
    public DependencyAnalyzer() {
        this.dependencyGraph = new DependencyGraph();
        this.resolver = new ExecutionOrderResolver(dependencyGraph);
    }
    
    /**
     * Analyze dependencies and resolve execution order for a list of blocks
     * @param blocks List of blocks to analyze
     * @param useKahnAlgorithm Whether to use Kahn's algorithm (better for cycles) or DFS
     * @return ExecutionOrderResult with ordered blocks and loop information
     */
    public ExecutionOrderResult analyzeAndResolve(List<Block> blocks, boolean useKahnAlgorithm) {
        System.out.printf("DependencyAnalyzer: Analyzing %d blocks using %s algorithm%n", 
                         blocks.size(), useKahnAlgorithm ? "Kahn's" : "DFS");
        
        // Build dependency graph
        dependencyGraph.buildGraph(blocks);
        
        // Optional: Print dependency graph for debugging
        if (System.getProperty("debug.dependency.graph", "false").equals("true")) {
            dependencyGraph.printGraph();
        }
        
        // Resolve execution order
        ExecutionOrderResult result = useKahnAlgorithm ? 
            resolver.resolveExecutionOrderKahn() : 
            resolver.resolveExecutionOrder();
        
        // Optional: Print results for debugging
        if (System.getProperty("debug.execution.order", "false").equals("true")) {
            result.printResults();
        }
        
        return result;
    }
    
    /**
     * Analyze dependencies and resolve execution order using default DFS algorithm
     * @param blocks List of blocks to analyze
     * @return ExecutionOrderResult with ordered blocks and loop information
     */
    public ExecutionOrderResult analyzeAndResolve(List<Block> blocks) {
        return analyzeAndResolve(blocks, false); // Default to DFS
    }
    
    /**
     * Quick check for cycles without full execution order resolution
     * @param blocks List of blocks to check
     * @return true if cycles exist
     */
    public boolean hasCycles(List<Block> blocks) {
        dependencyGraph.buildGraph(blocks);
        return dependencyGraph.hasCycles();
    }
    
    /**
     * Find all cycles in the block dependencies
     * @param blocks List of blocks to analyze
     * @return List of cycles, each cycle is a list of blocks
     */
    public List<List<Block>> findCycles(List<Block> blocks) {
        dependencyGraph.buildGraph(blocks);
        return dependencyGraph.findCycles();
    }
    
    /**
     * Get the current dependency graph
     * @return DependencyGraph instance
     */
    public DependencyGraph getDependencyGraph() {
        return dependencyGraph;
    }
    
    /**
     * Get source blocks (blocks with no dependencies)
     * @param blocks List of blocks to analyze
     * @return List of source blocks
     */
    public List<Block> getSourceBlocks(List<Block> blocks) {
        dependencyGraph.buildGraph(blocks);
        return dependencyGraph.getSourceBlocks();
    }
    
    /**
     * Get terminal blocks (blocks with no dependents or marked as terminal)
     * @param blocks List of blocks to analyze
     * @return List of terminal blocks
     */
    public List<Block> getTerminalBlocks(List<Block> blocks) {
        dependencyGraph.buildGraph(blocks);
        return dependencyGraph.getTerminalBlocks();
    }
}