package com.hualong.websocket;

import java.util.List;
import java.util.ArrayList;

/**
 * Main controller for robot operations
 */
public class RobotController {
    
    public interface ExecutionListener {
        void onProgress(int stepIndex, String stepDescription);
        void onComplete();
        void onError(String error);
    }
    
    private List<RobotMovement> program;
    private List<CoordinateSystem> coordinateSystems;
    private RobotPose currentPose;
    private JointAngles currentJoints;
    private boolean connected;
    private boolean moving;
    private CoordinateSystem activeCoordinateSystem;
    
    public RobotController() {
        this.program = new ArrayList<>();
        this.coordinateSystems = new ArrayList<>();
        this.currentPose = new RobotPose();
        this.currentJoints = new JointAngles(6); // Default 6-axis robot
        this.connected = false;
        this.moving = false;
        
        // Initialize default coordinate system
        this.activeCoordinateSystem = new CoordinateSystem();
        this.activeCoordinateSystem.setName("world");
        this.activeCoordinateSystem.setOrigin(new Point(0, 0, 0));
    }
    
    public void loadProgram(List<RobotMovement> movements, List<CoordinateSystem> coordSystems) {
        this.program = new ArrayList<>(movements);
        this.coordinateSystems = new ArrayList<>(coordSystems);
    }
    
    public boolean validateProgram() {
        if (program.isEmpty()) {
            return false;
        }
        
        // Basic validation - check if all movements have required data
        for (RobotMovement movement : program) {
            switch (movement.getType()) {
                case LINEAR:
                case CIRCULAR:
                    if (movement.getTargetPosition() == null) {
                        return false;
                    }
                    break;
                case JOINT:
                    if (movement.getJointAngles() == null) {
                        return false;
                    }
                    break;
            }
        }
        
        return true;
    }
    
    public void executeProgram(ExecutionListener listener) {
        if (!validateProgram()) {
            listener.onError("Program validation failed");
            return;
        }
        
        // Simulate program execution in a separate thread
        new Thread(() -> {
            try {
                moving = true;
                
                for (int i = 0; i < program.size(); i++) {
                    RobotMovement movement = program.get(i);
                    
                    // Notify progress
                    listener.onProgress(i, "Executing movement " + (i + 1) + ": " + movement.getType());
                    
                    // Simulate movement execution
                    executeMovement(movement);
                    
                    // Simulate execution time
                    Thread.sleep(1000);
                }
                
                moving = false;
                listener.onComplete();
                
            } catch (Exception e) {
                moving = false;
                listener.onError("Execution failed: " + e.getMessage());
            }
        }).start();
    }
    
    private void executeMovement(RobotMovement movement) {
        switch (movement.getType()) {
            case LINEAR:
                executeLinearMovement(movement);
                break;
            case JOINT:
                executeJointMovement(movement);
                break;
            case CIRCULAR:
                executeCircularMovement(movement);
                break;
        }
    }
    
    private void executeLinearMovement(RobotMovement movement) {
        // Update current pose to target position
        if (movement.getTargetPosition() != null) {
            currentPose.setPosition(movement.getTargetPosition());
        }
    }
    
    private void executeJointMovement(RobotMovement movement) {
        // Update current joint angles
        if (movement.getJointAngles() != null) {
            currentJoints = movement.getJointAngles();
            // Update pose based on forward kinematics (simplified)
            updatePoseFromJoints();
        }
    }
    
    private void executeCircularMovement(RobotMovement movement) {
        // Simplified circular movement - just move to target
        if (movement.getTargetPosition() != null) {
            currentPose.setPosition(movement.getTargetPosition());
        }
    }
    
    private void updatePoseFromJoints() {
        // Simplified forward kinematics
        // In a real implementation, this would use the actual robot kinematics
        double[] angles = currentJoints.getAngles();
        if (angles.length >= 3) {
            Point newPosition = new Point(
                Math.cos(angles[0]) * 500,
                Math.sin(angles[0]) * 500,
                angles[2] * 100
            );
            currentPose.setPosition(newPosition);
        }
    }
    
    public void emergencyStop() {
        moving = false;
        // In a real implementation, this would send emergency stop to robot
        System.out.println("Emergency stop activated");
    }
    
    public void setCoordinateSystem(CoordinateSystem coordSystem) {
        this.activeCoordinateSystem = coordSystem;
        
        // Add to list if not already present
        boolean found = false;
        for (CoordinateSystem cs : coordinateSystems) {
            if (cs.getName().equals(coordSystem.getName())) {
                found = true;
                break;
            }
        }
        if (!found) {
            coordinateSystems.add(coordSystem);
        }
    }
    
    public void connect() {
        // Simulate connection to robot
        this.connected = true;
    }
    
    public void disconnect() {
        // Simulate disconnection from robot
        this.connected = false;
        this.moving = false;
    }
    
    // Getters
    public RobotPose getCurrentPose() {
        return currentPose;
    }
    
    public JointAngles getCurrentJointAngles() {
        return currentJoints;
    }
    
    public boolean isConnected() {
        return connected;
    }
    
    public boolean isMoving() {
        return moving;
    }
    
    public CoordinateSystem getActiveCoordinateSystem() {
        return activeCoordinateSystem;
    }
    
    public List<RobotMovement> getProgram() {
        return new ArrayList<>(program);
    }
    
    public List<CoordinateSystem> getCoordinateSystems() {
        return new ArrayList<>(coordinateSystems);
    }
}