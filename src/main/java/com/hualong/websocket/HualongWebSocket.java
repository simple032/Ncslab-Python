package com.hualong.websocket;

import java.io.IOException;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;
import org.json.JSONObject;
import org.json.JSONArray;
import java.util.List;

/**
 * WebSocket endpoint for Hualong robotics programming interface
 */
@ServerEndpoint("/hualong")
public class HualongWebSocket {
    
    private RobotController robotController;
    
    @OnOpen
    public void onOpen(Session session) {
        System.out.println("Hualong WebSocket connection opened: " + session.getId());
        this.robotController = new RobotController();
        
        // Set buffer sizes for large robot programs
        session.setMaxTextMessageBufferSize(1024 * 1024); // 1MB
        session.setMaxBinaryMessageBufferSize(1024 * 1024);
        
        sendMessage(session, createStatusMessage("connected", "Robot controller initialized").toString());
    }
    
    @OnMessage
    public void onMessage(Session session, String message) {
        try {
            JSONObject request = new JSONObject(message);
            String command = request.getString("command");
            
            switch (command) {
                case "parse_program":
                    handleParseProgram(session, request);
                    break;
                case "execute_program":
                    handleExecuteProgram(session, request);
                    break;
                case "get_robot_status":
                    handleGetRobotStatus(session, request);
                    break;
                case "emergency_stop":
                    handleEmergencyStop(session, request);
                    break;
                case "set_coordinate_system":
                    handleSetCoordinateSystem(session, request);
                    break;
                default:
                    sendErrorMessage(session, "Unknown command: " + command);
            }
        } catch (Exception e) {
            sendErrorMessage(session, "Error processing message: " + e.getMessage());
        }
    }
    
    @OnClose
    public void onClose(Session session) {
        System.out.println("Hualong WebSocket connection closed: " + session.getId());
        if (robotController != null) {
            robotController.disconnect();
        }
    }
    
    @OnError
    public void onError(Session session, Throwable throwable) {
        System.err.println("Hualong WebSocket error: " + throwable.getMessage());
        throwable.printStackTrace();
    }
    
    /**
     * Handles program parsing from Blockly JSON
     */
    private void handleParseProgram(Session session, JSONObject request) {
        try {
            JSONObject blocklyData = request.getJSONObject("blockly_data");
            
            // Parse movements
            List<RobotMovement> movements = BlocklyJsonParser.parseMovements(blocklyData);
            
            // Parse coordinate systems
            List<CoordinateSystem> coordinateSystems = BlocklyJsonParser.parseCoordinateSystems(blocklyData);
            
            // Create response
            JSONObject response = new JSONObject();
            response.put("status", "success");
            response.put("message", "Program parsed successfully");
            response.put("movement_count", movements.size());
            response.put("coordinate_system_count", coordinateSystems.size());
            
            // Store parsed program in controller
            robotController.loadProgram(movements, coordinateSystems);
            
            sendMessage(session, response.toString());
            
        } catch (Exception e) {
            sendErrorMessage(session, "Failed to parse program: " + e.getMessage());
        }
    }
    
    /**
     * Handles program execution
     */
    private void handleExecuteProgram(Session session, JSONObject request) {
        try {
            boolean dryRun = request.optBoolean("dry_run", false);
            
            if (dryRun) {
                // Validate program without executing
                boolean isValid = robotController.validateProgram();
                JSONObject response = new JSONObject();
                response.put("status", isValid ? "valid" : "invalid");
                response.put("message", isValid ? "Program is valid" : "Program has errors");
                sendMessage(session, response.toString());
            } else {
                // Execute the program
                robotController.executeProgram(new RobotController.ExecutionListener() {
                    @Override
                    public void onProgress(int stepIndex, String stepDescription) {
                        JSONObject progress = createProgressMessage(stepIndex, stepDescription);
                        sendMessage(session, progress.toString());
                    }
                    
                    @Override
                    public void onComplete() {
                        JSONObject complete = createStatusMessage("complete", "Program execution finished");
                        sendMessage(session, complete.toString());
                    }
                    
                    @Override
                    public void onError(String error) {
                        sendErrorMessage(session, "Execution error: " + error);
                    }
                });
                
                sendMessage(session, createStatusMessage("executing", "Program execution started").toString());
            }
            
        } catch (Exception e) {
            sendErrorMessage(session, "Failed to execute program: " + e.getMessage());
        }
    }
    
    /**
     * Handles robot status requests
     */
    private void handleGetRobotStatus(Session session, JSONObject request) {
        try {
            RobotPose currentPose = robotController.getCurrentPose();
            JointAngles currentJoints = robotController.getCurrentJointAngles();
            
            JSONObject response = new JSONObject();
            response.put("status", "success");
            response.put("robot_connected", robotController.isConnected());
            response.put("current_pose", poseToJson(currentPose));
            response.put("current_joints", jointsToJson(currentJoints));
            response.put("is_moving", robotController.isMoving());
            
            sendMessage(session, response.toString());
            
        } catch (Exception e) {
            sendErrorMessage(session, "Failed to get robot status: " + e.getMessage());
        }
    }
    
    /**
     * Handles emergency stop
     */
    private void handleEmergencyStop(Session session, JSONObject request) {
        try {
            robotController.emergencyStop();
            sendMessage(session, createStatusMessage("stopped", "Emergency stop activated").toString());
        } catch (Exception e) {
            sendErrorMessage(session, "Failed to execute emergency stop: " + e.getMessage());
        }
    }
    
    /**
     * Handles coordinate system setup
     */
    private void handleSetCoordinateSystem(Session session, JSONObject request) {
        try {
            String name = request.getString("name");
            JSONObject originJson = request.getJSONObject("origin");
            
            Point origin = new Point(
                originJson.getDouble("x"),
                originJson.getDouble("y"),
                originJson.getDouble("z")
            );
            
            CoordinateSystem coordSystem = new CoordinateSystem();
            coordSystem.setName(name);
            coordSystem.setOrigin(origin);
            
            robotController.setCoordinateSystem(coordSystem);
            
            sendMessage(session, createStatusMessage("success", "Coordinate system set: " + name).toString());
            
        } catch (Exception e) {
            sendErrorMessage(session, "Failed to set coordinate system: " + e.getMessage());
        }
    }
    
    /**
     * Utility methods
     */
    private void sendMessage(Session session, String message) {
        try {
            if (session != null && session.isOpen()) {
                session.getBasicRemote().sendText(message);
            }
        } catch (IOException e) {
            System.err.println("Failed to send message: " + e.getMessage());
        }
    }
    
    private void sendErrorMessage(Session session, String error) {
        JSONObject errorResponse = new JSONObject();
        errorResponse.put("status", "error");
        errorResponse.put("message", error);
        sendMessage(session, errorResponse.toString());
    }
    
    private JSONObject createStatusMessage(String status, String message) {
        JSONObject response = new JSONObject();
        response.put("status", status);
        response.put("message", message);
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }
    
    private JSONObject createProgressMessage(int stepIndex, String description) {
        JSONObject progress = new JSONObject();
        progress.put("type", "progress");
        progress.put("step", stepIndex);
        progress.put("description", description);
        progress.put("timestamp", System.currentTimeMillis());
        return progress;
    }
    
    private JSONObject poseToJson(RobotPose pose) {
        JSONObject json = new JSONObject();
        if (pose != null) {
            json.put("x", pose.getPosition().getX());
            json.put("y", pose.getPosition().getY());
            json.put("z", pose.getPosition().getZ());
            json.put("rx", pose.getOrientation().getRx());
            json.put("ry", pose.getOrientation().getRy());
            json.put("rz", pose.getOrientation().getRz());
        }
        return json;
    }
    
    private JSONObject jointsToJson(JointAngles joints) {
        JSONObject json = new JSONObject();
        if (joints != null) {
            JSONArray angles = new JSONArray();
            for (double angle : joints.getAngles()) {
                angles.put(angle);
            }
            json.put("angles", angles);
        }
        return json;
    }
}