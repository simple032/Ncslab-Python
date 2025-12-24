package com.hualong.websocket;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.json.JSONObject;
import org.json.JSONArray;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

/**
 * Parser for Blockly-generated JSON data for robotics programming
 */
public class BlocklyJsonParser {

    /**
     * Parses Blockly JSON into robot movement commands
     * @param jsonData the JSON data from Blockly workspace
     * @return list of robot movement commands
     */
    public static List<RobotMovement> parseMovements(JSONObject jsonData) {
        List<RobotMovement> movements = new ArrayList<>();

        if (jsonData.has("blocks")) {
            JSONArray blocks = jsonData.getJSONArray("blocks");
            for (int i = 0; i < blocks.length(); i++) {
                JSONObject block = blocks.getJSONObject(i);
                RobotMovement movement = parseMovementBlock(block);
                if (movement != null) {
                    movements.add(movement);
                }
            }
        }

        return movements;
    }

    /**
     * Parses a single movement block
     * @param block the JSON block data
     * @return RobotMovement object or null if not a movement block
     */
    private static RobotMovement parseMovementBlock(JSONObject block) {
        if (!block.has("type")) {
            return null;
        }

        String blockType = block.getString("type");

        switch (blockType) {
            case "move_linear":
                return parseLinearMovement(block);
            case "move_joint":
                return parseJointMovement(block);
            case "move_circular":
                return parseCircularMovement(block);
            default:
                return null;
        }
    }

    /**
     * Parses linear movement block
     */
    private static RobotMovement parseLinearMovement(JSONObject block) {
        RobotMovement movement = new RobotMovement();
        movement.setType(RobotMovement.MovementType.LINEAR);

        if (block.has("fields")) {
            JSONObject fields = block.getJSONObject("fields");

            // Parse target position
            if (fields.has("TARGET_POSITION")) {
                JSONObject position = fields.getJSONObject("TARGET_POSITION");
                Point target = new Point(
                    position.optDouble("x", 0.0),
                    position.optDouble("y", 0.0),
                    position.optDouble("z", 0.0)
                );
                movement.setTargetPosition(target);
            }

            // Parse speed
            if (fields.has("SPEED")) {
                movement.setSpeed(fields.getDouble("SPEED"));
            }
        }

        return movement;
    }

    /**
     * Parses joint movement block
     */
    private static RobotMovement parseJointMovement(JSONObject block) {
        RobotMovement movement = new RobotMovement();
        movement.setType(RobotMovement.MovementType.JOINT);

        if (block.has("fields")) {
            JSONObject fields = block.getJSONObject("fields");

            // Parse joint angles
            if (fields.has("JOINT_ANGLES")) {
                JSONArray angles = fields.getJSONArray("JOINT_ANGLES");
                double[] jointAngles = new double[angles.length()];
                for (int i = 0; i < angles.length(); i++) {
                    jointAngles[i] = angles.getDouble(i);
                }
                movement.setJointAngles(new JointAngles(jointAngles));
            }

            // Parse speed
            if (fields.has("SPEED")) {
                movement.setSpeed(fields.getDouble("SPEED"));
            }
        }

        return movement;
    }

    /**
     * Parses circular movement block
     */
    private static RobotMovement parseCircularMovement(JSONObject block) {
        RobotMovement movement = new RobotMovement();
        movement.setType(RobotMovement.MovementType.CIRCULAR);

        if (block.has("fields")) {
            JSONObject fields = block.getJSONObject("fields");

            // Parse via point
            if (fields.has("VIA_POINT")) {
                JSONObject viaPoint = fields.getJSONObject("VIA_POINT");
                Point via = new Point(
                    viaPoint.optDouble("x", 0.0),
                    viaPoint.optDouble("y", 0.0),
                    viaPoint.optDouble("z", 0.0)
                );
                movement.setViaPoint(via);
            }

            // Parse target position
            if (fields.has("TARGET_POSITION")) {
                JSONObject position = fields.getJSONObject("TARGET_POSITION");
                Point target = new Point(
                    position.optDouble("x", 0.0),
                    position.optDouble("y", 0.0),
                    position.optDouble("z", 0.0)
                );
                movement.setTargetPosition(target);
            }

            // Parse speed
            if (fields.has("SPEED")) {
                movement.setSpeed(fields.getDouble("SPEED"));
            }
        }

        return movement;
    }

    /**
     * Parses coordinate system definitions
     * @param jsonData the JSON data
     * @return list of coordinate systems
     */
    public static List<CoordinateSystem> parseCoordinateSystems(JSONObject jsonData) {
        List<CoordinateSystem> systems = new ArrayList<>();

        if (jsonData.has("coordinate_systems")) {
            JSONArray coordSystems = jsonData.getJSONArray("coordinate_systems");
            for (int i = 0; i < coordSystems.length(); i++) {
                JSONObject system = coordSystems.getJSONObject(i);
                CoordinateSystem cs = new CoordinateSystem();

                if (system.has("name")) {
                    cs.setName(system.getString("name"));
                }

                if (system.has("origin")) {
                    JSONObject origin = system.getJSONObject("origin");
                    cs.setOrigin(new Point(
                        origin.optDouble("x", 0.0),
                        origin.optDouble("y", 0.0),
                        origin.optDouble("z", 0.0)
                    ));
                }

                systems.add(cs);
            }
        }

        return systems;
    }

    /**
     * Parses Blockly JSON and returns list of BlocklyBlock objects
     * @param jsonData the JSON string from Blockly workspace
     * @return list of parsed blocks
     */
    public static List<BlocklyBlock> parseBlocklyJson(String jsonData) {
        List<BlocklyBlock> blocks = new ArrayList<>();

        try {
            JSONObject json = new JSONObject(jsonData);
            if (json.has("blocks")) {
                JSONArray blockArray = json.getJSONArray("blocks");
                for (int i = 0; i < blockArray.length(); i++) {
                    JSONObject blockJson = blockArray.getJSONObject(i);
                    BlocklyBlock block = parseBlock(blockJson);
                    if (block != null) {
                        blocks.add(block);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error parsing Blockly JSON: " + e.getMessage());
        }

        return blocks;
    }

    /**
     * Parses a single block from JSON
     */
    private static BlocklyBlock parseBlock(JSONObject blockJson) {
        BlocklyBlock block = new BlocklyBlock();

        if (blockJson.has("type")) {
            block.setType(blockJson.getString("type"));
        }

        if (blockJson.has("id")) {
            block.setId(blockJson.getString("id"));
        }

        if (blockJson.has("fields")) {
            JSONObject fields = blockJson.getJSONObject("fields");
            Map<String, Object> fieldMap = new HashMap<>();
            for (String key : fields.keySet()) {
                fieldMap.put(key, fields.get(key));
            }
            block.setFields(fieldMap);
        }

        if (blockJson.has("inputs")) {
            JSONObject inputs = blockJson.getJSONObject("inputs");
            Map<String, Object> inputMap = new HashMap<>();
            for (String key : inputs.keySet()) {
                inputMap.put(key, inputs.get(key));
            }
            block.setInputs(inputMap);
        }

        return block;
    }

    /**
     * Gets variable map from parsed data
     * @return map of variables
     */
    public static Map<String, Object> getVariableMap() {
        // Return empty map for now - in real implementation would parse variables
        return new HashMap<>();
    }

    /**
     * Inner class representing a Blockly block
     */
    @Setter
    @Getter
    @Data
    public static class BlocklyBlock {
        // Getters and setters
        private String type;
        private String id;
        private Map<String, Object> fields;
        private Map<String, Object> inputs;
        private Map<String, Object> outputs;
        private Map<String, Object> statements;
        private List<BlocklyBlock> nextBlocks;

        public BlocklyBlock() {
            this.fields = new HashMap<>();
            this.inputs = new HashMap<>();
        }

        @Override
        public String toString() {
            return "BlocklyBlock{" +
                    "type='" + type + '\'' +
                    ", id='" + id + '\'' +
                    ", fields=" + fields +
                    ", inputs=" + inputs +
                    '}';
        }
    }
}
