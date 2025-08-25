package com.ncslab.dto.processor;

import com.ncslab.block.Block;
import com.ncslab.block.subsystem.Subsystem;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.model.LineDto;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Utility class for managing subsystem paths and hierarchical block organization.
 * 
 * This class provides methods for:
 * - Analyzing subsystem hierarchy levels
 * - Organizing blocks and lines by subsystem paths
 * - Managing path-based relationships in DTO processing
 * 
 * Key Design Principles:
 * - Pure utility methods with no state
 * - Path-based hierarchy analysis
 * - Support for both Block objects and BlockDto DTOs
 * - Thread-safe operations
 */
public final class SubsystemPathUtils {
    
    // Private constructor to prevent instantiation of utility class
    private SubsystemPathUtils() {
        // Default implementation - no operation needed
    }
    
    // ===== PATH ANALYSIS METHODS =====
    
    /**
     * Get the hierarchy level of a block path.
     * Root level (empty path) is level 0, first subsystem level is 1, etc.
     * 
     * @param blockPath The block path to analyze
     * @return The hierarchy level (0 for root, 1+ for subsystem levels)
     */
    public static int getHierarchyLevel(String blockPath) {
        if (blockPath == null || blockPath.trim().isEmpty()) {
            return 0; // Root level
        }
        
        // Count path separators to determine depth
        String trimmedPath = blockPath.trim();
        if (trimmedPath.equals("/")) {
            return 0; // Root level
        }
        
        // Remove leading/trailing slashes for consistent counting
        String normalizedPath = trimmedPath.replaceAll("^/+|/+$", "");
        if (normalizedPath.isEmpty()) {
            return 0; // Root level
        }
        
        return normalizedPath.split("/").length;
    }
    
    /**
     * Get the parent path of a given block path.
     * Returns null for root level paths.
     * 
     * @param blockPath The block path
     * @return The parent path, or null if at root level
     */
    public static String getParentPath(String blockPath) {
        if (blockPath == null || blockPath.trim().isEmpty()) {
            return null; // Root level has no parent
        }
        
        String trimmedPath = blockPath.trim();
        if (trimmedPath.equals("/")) {
            return null; // Root level has no parent
        }
        
        // Find last slash
        int lastSlash = trimmedPath.lastIndexOf('/');
        if (lastSlash <= 0) {
            return ""; // Parent is root level
        }
        
        return trimmedPath.substring(0, lastSlash);
    }
    
    /**
     * Check if one path is a direct child of another.
     * 
     * @param childPath The potential child path
     * @param parentPath The potential parent path
     * @return true if childPath is a direct child of parentPath
     */
    public static boolean isDirectChild(String childPath, String parentPath) {
        if (childPath == null || parentPath == null) {
            return false;
        }
        
        String actualParent = getParentPath(childPath);
        if (actualParent == null && parentPath.isEmpty()) {
            return true; // Root level children
        }
        
        return Objects.equals(actualParent, parentPath);
    }
    
    /**
     * Check if one path is a descendant (child, grandchild, etc.) of another.
     * 
     * @param descendantPath The potential descendant path
     * @param ancestorPath The potential ancestor path
     * @return true if descendantPath is a descendant of ancestorPath
     */
    public static boolean isDescendant(String descendantPath, String ancestorPath) {
        if (descendantPath == null || ancestorPath == null) {
            return false;
        }
        
        // Normalize paths
        String normalizedDescendant = normalizePath(descendantPath);
        String normalizedAncestor = normalizePath(ancestorPath);
        
        // Root is ancestor of everything
        if (normalizedAncestor.isEmpty()) {
            return !normalizedDescendant.isEmpty();
        }
        
        // Check if descendant starts with ancestor path
        return normalizedDescendant.startsWith(normalizedAncestor + "/");
    }
    
    /**
     * Normalize a path by removing leading/trailing slashes and handling empty paths.
     * 
     * @param path The path to normalize
     * @return The normalized path
     */
    public static String normalizePath(String path) {
        if (path == null) {
            return "";
        }
        
        String trimmed = path.trim();
        if (trimmed.isEmpty() || trimmed.equals("/")) {
            return "";
        }
        
        return trimmed.replaceAll("^/+|/+$", "");
    }
    
    // ===== HIERARCHY ORGANIZATION METHODS =====
    
    /**
     * Group blocks by their hierarchy level.
     * 
     * @param blocks List of blocks to group
     * @return Map from hierarchy level to list of blocks at that level
     */
    public static Map<Integer, List<Block>> groupBlocksByLevel(List<Block> blocks) {
        if (blocks == null) {
            return new HashMap<>();
        }
        
        return blocks.stream()
            .filter(Objects::nonNull)
            .collect(Collectors.groupingBy(
                block -> getHierarchyLevel(block.getBlockPath()),
                TreeMap::new, // Use TreeMap to maintain level order
                Collectors.toList()
            ));
    }
    
    /**
     * Group BlockDto DTOs by their hierarchy level.
     * 
     * @param blockDtos List of BlockDto DTOs to group
     * @return Map from hierarchy level to list of BlockDto DTOs at that level
     */
    public static Map<Integer, List<BlockDto>> groupBlockDtosByLevel(List<BlockDto> blockDtos) {
        if (blockDtos == null) {
            return new HashMap<>();
        }
        
        return blockDtos.stream()
            .filter(Objects::nonNull)
            .collect(Collectors.groupingBy(
                blockDto -> getHierarchyLevel(blockDto.getBlockPath()),
                TreeMap::new, // Use TreeMap to maintain level order
                Collectors.toList()
            ));
    }
    
    /**
     * Group blocks by their parent subsystem path.
     * 
     * @param blocks List of blocks to group
     * @return Map from parent path to list of blocks in that subsystem
     */
    public static Map<String, List<Block>> groupBlocksByParentPath(List<Block> blocks) {
        if (blocks == null) {
            return new HashMap<>();
        }
        
        return blocks.stream()
            .filter(Objects::nonNull)
            .collect(Collectors.groupingBy(
                block -> {
                    String parentPath = getParentPath(block.getBlockPath());
                    return parentPath != null ? parentPath : ""; // Use empty string for root
                },
                Collectors.toList()
            ));
    }
    
    /**
     * Group LineDto DTOs by their parent subsystem path.
     * Determines the parent path based on the blocks the line connects.
     * 
     * @param lineDtos List of LineDto DTOs to group
     * @param blocks List of all blocks for path resolution
     * @return Map from parent path to list of LineDto DTOs in that subsystem
     */
    public static Map<String, List<LineDto>> groupLineDtosByParentPath(List<LineDto> lineDtos, List<Block> blocks) {
        if (lineDtos == null) {
            return new HashMap<>();
        }
        
        // Create lookup maps for block path resolution
        Map<String, String> blockNameToPath = new HashMap<>();
        Map<String, String> blockUuidToPath = new HashMap<>();
        
        if (blocks != null) {
            for (Block block : blocks) {
                if (block.getBlockName() != null) {
                    blockNameToPath.put(block.getBlockName(), block.getBlockPath());
                }
                if (block.getBlockUUID() != null) {
                    blockUuidToPath.put(block.getBlockUUID(), block.getBlockPath());
                }
            }
        }
        
        return lineDtos.stream()
            .filter(Objects::nonNull)
            .collect(Collectors.groupingBy(
                lineDto -> determineLineParentPath(lineDto, blockNameToPath, blockUuidToPath),
                Collectors.toList()
            ));
    }
    
    /**
     * Determine the parent subsystem path for a line based on its connected blocks.
     * 
     * @param lineDto The line DTO
     * @param blockNameToPath Map from block name to path
     * @param blockUuidToPath Map from block UUID to path
     * @return The parent path for the line
     */
    private static String determineLineParentPath(LineDto lineDto, 
                                                Map<String, String> blockNameToPath, 
                                                Map<String, String> blockUuidToPath) {
        // Try to resolve paths for from and to blocks
        String fromPath = resolveBlockPath(lineDto.getFromBlockName(), lineDto.getFromBlockUUID(), 
                                         blockNameToPath, blockUuidToPath);
        String toPath = resolveBlockPath(lineDto.getToBlockName(), lineDto.getToBlockUUID(), 
                                       blockNameToPath, blockUuidToPath);
        
        // If both blocks are in the same subsystem, use that path
        if (fromPath != null && toPath != null && fromPath.equals(toPath)) {
            return fromPath;
        }
        
        // If paths differ or can't be resolved, fall back to line path or root
        if (lineDto.getLinePath() != null) {
            return lineDto.getLinePath();
        }
        
        return ""; // Root level
    }
    
    /**
     * Resolve the path for a block using name or UUID.
     * 
     * @param blockName The block name
     * @param blockUuid The block UUID
     * @param blockNameToPath Map from block name to path
     * @param blockUuidToPath Map from block UUID to path
     * @return The resolved path, or null if not found
     */
    private static String resolveBlockPath(String blockName, String blockUuid,
                                         Map<String, String> blockNameToPath,
                                         Map<String, String> blockUuidToPath) {
        // Try UUID first (more reliable)
        if (blockUuid != null && blockUuidToPath.containsKey(blockUuid)) {
            return blockUuidToPath.get(blockUuid);
        }
        
        // Fall back to name
        if (blockName != null && blockNameToPath.containsKey(blockName)) {
            return blockNameToPath.get(blockName);
        }
        
        return null;
    }
    
    // ===== SUBSYSTEM IDENTIFICATION METHODS =====
    
    /**
     * Find all subsystem blocks at a specific hierarchy level.
     * 
     * @param blocks List of all blocks
     * @param level The hierarchy level to search
     * @return List of Subsystem blocks at the specified level
     */
    public static List<Subsystem> findSubsystemsAtLevel(List<Block> blocks, int level) {
        if (blocks == null) {
            return new ArrayList<>();
        }
        
        return blocks.stream()
            .filter(Objects::nonNull)
            .filter(block -> block instanceof Subsystem)
            .filter(block -> getHierarchyLevel(block.getBlockPath()) == level)
            .map(block -> (Subsystem) block)
            .collect(Collectors.toList());
    }
    
    /**
     * Find all unique subsystem paths at a specific hierarchy level.
     * 
     * @param blocks List of all blocks
     * @param level The hierarchy level to search
     * @return Set of unique subsystem paths at the specified level
     */
    public static Set<String> findSubsystemPathsAtLevel(List<Block> blocks, int level) {
        if (blocks == null) {
            return new HashSet<>();
        }
        
        return blocks.stream()
            .filter(Objects::nonNull)
            .map(Block::getBlockPath)
            .filter(Objects::nonNull)
            .filter(path -> getHierarchyLevel(path) == level)
            .collect(Collectors.toSet());
    }
    
    /**
     * Get the maximum hierarchy level present in a list of blocks.
     * 
     * @param blocks List of blocks to analyze
     * @return The maximum hierarchy level, or 0 if no blocks
     */
    public static int getMaxHierarchyLevel(List<Block> blocks) {
        if (blocks == null || blocks.isEmpty()) {
            return 0;
        }
        
        return blocks.stream()
            .filter(Objects::nonNull)
            .mapToInt(block -> getHierarchyLevel(block.getBlockPath()))
            .max()
            .orElse(0);
    }
    
    /**
     * Get the maximum hierarchy level present in a list of BlockDto DTOs.
     * 
     * @param blockDtos List of BlockDto DTOs to analyze
     * @return The maximum hierarchy level, or 0 if no blocks
     */
    public static int getMaxHierarchyLevelFromDtos(List<BlockDto> blockDtos) {
        if (blockDtos == null || blockDtos.isEmpty()) {
            return 0;
        }
        
        return blockDtos.stream()
            .filter(Objects::nonNull)
            .mapToInt(blockDto -> getHierarchyLevel(blockDto.getBlockPath()))
            .max()
            .orElse(0);
    }
    
    // ===== VALIDATION METHODS =====
    
    /**
     * Validate that all paths in a collection are well-formed.
     * 
     * @param paths Collection of paths to validate
     * @return List of validation errors, empty if all paths are valid
     */
    public static List<String> validatePaths(Collection<String> paths) {
        List<String> errors = new ArrayList<>();
        
        if (paths != null) {
            for (String path : paths) {
                if (path != null && !isValidPath(path)) {
                    errors.add("Invalid path format: " + path);
                }
            }
        }
        
        return errors;
    }
    
    /**
     * Check if a path is well-formed.
     * 
     * @param path The path to validate
     * @return true if the path is valid, false otherwise
     */
    public static boolean isValidPath(String path) {
        if (path == null) {
            return false;
        }
        
        String trimmed = path.trim();
        
        // Empty or root path is valid
        if (trimmed.isEmpty() || trimmed.equals("/")) {
            return true;
        }
        
        // Check for invalid characters or patterns
        if (trimmed.contains("//") || trimmed.contains("..")) {
            return false; // No double slashes or relative paths
        }
        
        // Additional validation rules can be added here
        return true;
    }
}