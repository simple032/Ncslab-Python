package com.ncslab.system;

import com.ncslab.block.Block;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.line.Line;

import java.util.*;

/**
 * Represents the dependency graph between blocks in a simulation model
 * Provides functionality to analyze dependencies and detect cycles
 */
public class DependencyGraph {
    
    private final Map<Block, Set<Block>> dependencies;
    private final Map<Block, Set<Block>> dependents;
    private final Set<Block> allBlocks;
    
    public DependencyGraph() {
        this.dependencies = new HashMap<>();
        this.dependents = new HashMap<>();
        this.allBlocks = new HashSet<>();
    }
    
    /**
     * Build dependency graph from a list of blocks
     * @param blocks List of blocks to analyze
     */
    public void buildGraph(List<Block> blocks) {
        clear();
        
        // Initialize all blocks
        for (Block block : blocks) {
            allBlocks.add(block);
            dependencies.put(block, new HashSet<>());
            dependents.put(block, new HashSet<>());
        }
        
        // Analyze dependencies based on connections
        for (Block block : blocks) {
            analyzeBlockDependencies(block);
        }
    }
    
    /**
     * Analyze dependencies for a single block
     */
    private void analyzeBlockDependencies(Block block) {
        Set<Block> blockDependencies = dependencies.get(block);
        
        // Check each input port to find dependencies
        for (InputPort inputPort : block.getInputPortList()) {
            Line line = inputPort.getLinkedLine();
            if (line != null && line.getLinkedOutputPort() != null) {
                OutputPort sourcePort = line.getLinkedOutputPort();
                Block sourceBlock = sourcePort.getBlock();
                
                if (allBlocks.contains(sourceBlock) && !sourceBlock.equals(block)) {
                    // block depends on sourceBlock
                    blockDependencies.add(sourceBlock);
                    
                    // sourceBlock has block as dependent
                    dependents.get(sourceBlock).add(block);
                }
            }
        }
    }
    
    /**
     * Get all blocks that the given block depends on
     */
    public Set<Block> getDependencies(Block block) {
        return Collections.unmodifiableSet(dependencies.getOrDefault(block, Collections.emptySet()));
    }
    
    /**
     * Get all blocks that depend on the given block
     */
    public Set<Block> getDependents(Block block) {
        return Collections.unmodifiableSet(dependents.getOrDefault(block, Collections.emptySet()));
    }
    
    /**
     * Get all blocks with no dependencies (source blocks)
     */
    public List<Block> getSourceBlocks() {
        List<Block> sources = new ArrayList<>();
        for (Block block : allBlocks) {
            if (dependencies.get(block).isEmpty()) {
                sources.add(block);
            }
        }
        
        // Sort by ID for deterministic order
        sources.sort((a, b) -> Integer.compare(a.getBlockId(), b.getBlockId()));
        return sources;
    }
    
    /**
     * Get all blocks with no dependents (terminal blocks)
     */
    public List<Block> getTerminalBlocks() {
        List<Block> terminals = new ArrayList<>();
        for (Block block : allBlocks) {
            if (block.isTerminalBlock()) {
                terminals.add(block);
            }
        }
        return terminals;
    }
    
    /**
     * Get all blocks in the graph
     */
    public Set<Block> getAllBlocks() {
        return Collections.unmodifiableSet(allBlocks);
    }
    
    /**
     * Check if there are any cyclic dependencies (algebraic loops)
     */
    public boolean hasCycles() {
        Set<Block> visited = new HashSet<>();
        Set<Block> visiting = new HashSet<>();
        
        for (Block block : allBlocks) {
            if (!visited.contains(block)) {
                if (hasCycleDFS(block, visited, visiting)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Find all cycles in the dependency graph
     */
    public List<List<Block>> findCycles() {
        List<List<Block>> cycles = new ArrayList<>();
        Set<Block> visited = new HashSet<>();
        Set<Block> visiting = new HashSet<>();
        List<Block> currentPath = new ArrayList<>();
        
        for (Block block : allBlocks) {
            if (!visited.contains(block)) {
                findCyclesDFS(block, visited, visiting, currentPath, cycles);
            }
        }
        
        return cycles;
    }
    
    /**
     * DFS helper for cycle detection
     */
    private boolean hasCycleDFS(Block block, Set<Block> visited, Set<Block> visiting) {
        if (visiting.contains(block)) {
            return true; // Cycle found
        }
        
        if (visited.contains(block)) {
            return false; // Already processed
        }
        
        visiting.add(block);
        
        for (Block dependent : dependents.get(block)) {
            if (hasCycleDFS(dependent, visited, visiting)) {
                return true;
            }
        }
        
        visiting.remove(block);
        visited.add(block);
        return false;
    }
    
    /**
     * DFS helper for finding all cycles
     */
    private void findCyclesDFS(Block block, Set<Block> visited, Set<Block> visiting, 
                             List<Block> currentPath, List<List<Block>> cycles) {
        if (visiting.contains(block)) {
            // Cycle found - extract the cycle from current path
            int cycleStart = currentPath.indexOf(block);
            if (cycleStart >= 0) {
                List<Block> cycle = new ArrayList<>(currentPath.subList(cycleStart, currentPath.size()));
                cycle.add(block); // Complete the cycle
                cycles.add(cycle);
            }
            return;
        }
        
        if (visited.contains(block)) {
            return;
        }
        
        visiting.add(block);
        currentPath.add(block);
        
        for (Block dependent : dependents.get(block)) {
            findCyclesDFS(dependent, visited, visiting, currentPath, cycles);
        }
        
        currentPath.remove(currentPath.size() - 1);
        visiting.remove(block);
        visited.add(block);
    }
    
    /**
     * Clear the dependency graph
     */
    public void clear() {
        dependencies.clear();
        dependents.clear();
        allBlocks.clear();
    }
    
    /**
     * Print the dependency graph for debugging
     */
    public void printGraph() {
        System.out.println("=== DEPENDENCY GRAPH ===");
        for (Block block : allBlocks) {
            Set<Block> deps = dependencies.get(block);
            if (!deps.isEmpty()) {
                System.out.printf("Block %s depends on: %s%n", 
                    block.getBlockName(), 
                    deps.stream().map(Block::getBlockName).reduce((a, b) -> a + ", " + b).orElse(""));
            } else {
                System.out.printf("Block %s is a source block (no dependencies)%n", block.getBlockName());
            }
        }
        System.out.println("=== END DEPENDENCY GRAPH ===");
    }
}