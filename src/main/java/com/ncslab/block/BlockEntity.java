package com.ncslab.block;

import com.ncslab.entity.Entity;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;

/**
 * Entity adapter for Block objects to implement the Entity interface.
 * This allows Block objects to work with the mapping framework while
 * maintaining backward compatibility with existing code.
 */
@Getter
@Setter
public class BlockEntity implements Entity {
    
    private final Block block;
    private String id;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Map<String, Object> metadata;
    
    public BlockEntity(Block block) {
        this.block = block;
        this.id = block.getBlockUUID();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.metadata = new HashMap<>();
        
        // Add block-specific metadata
        metadata.put("blockType", block.getBlockType());
        metadata.put("blockName", block.getBlockName());
        metadata.put("blockPath", block.getBlockPath());
    }
    
    @Override
    public String getEntityType() {
        return "Block";
    }
    
    /**
     * Get the wrapped Block instance
     * @return The wrapped Block
     */
    public Block getBlock() {
        return block;
    }
    
    /**
     * Update the ID in both the entity and the wrapped block
     * @param id The new ID
     */
    @Override
    public void setId(String id) {
        this.id = id;
        // Note: Block.blockUUID is not settable, so we only update the entity ID
    }
    
    /**
     * Sync metadata with block properties
     */
    public void syncMetadata() {
        if (metadata == null) {
            metadata = new HashMap<>();
        }
        metadata.put("blockType", block.getBlockType());
        metadata.put("blockName", block.getBlockName());
        metadata.put("blockPath", block.getBlockPath());
        metadata.put("inputPortCount", block.getInputPortList().size());
        metadata.put("outputPortCount", block.getOutputPortList().size());
        metadata.put("parameterCount", block.getParameterList().size());
        touch();
    }
    
    /**
     * Create a BlockEntity from an existing Block
     * @param block The block to wrap
     * @return New BlockEntity instance
     */
    public static BlockEntity fromBlock(Block block) {
        BlockEntity entity = new BlockEntity(block);
        entity.syncMetadata();
        return entity;
    }
    
    @Override
    public String toString() {
        return String.format("BlockEntity{id=%s, blockType=%s, blockName=%s}", 
                           id, block.getBlockType(), block.getBlockName());
    }
}