package com.hualong.websocket;

/**
 * Represents a robot movement command
 */
public class RobotMovement {
    
    public enum MovementType {
        LINEAR,    // Linear movement to target position
        JOINT,     // Joint movement with specified angles
        CIRCULAR   // Circular movement through via point
    }
    
    private MovementType type;
    private Point targetPosition;
    private Point viaPoint; // For circular movements
    private JointAngles jointAngles;
    private double speed;
    private String coordinateSystem;
    
    public RobotMovement() {
        this.speed = 100.0; // Default speed
        this.coordinateSystem = "world"; // Default coordinate system
    }
    
    // Getters and setters
    public MovementType getType() {
        return type;
    }
    
    public void setType(MovementType type) {
        this.type = type;
    }
    
    public Point getTargetPosition() {
        return targetPosition;
    }
    
    public void setTargetPosition(Point targetPosition) {
        this.targetPosition = targetPosition;
    }
    
    public Point getViaPoint() {
        return viaPoint;
    }
    
    public void setViaPoint(Point viaPoint) {
        this.viaPoint = viaPoint;
    }
    
    public JointAngles getJointAngles() {
        return jointAngles;
    }
    
    public void setJointAngles(JointAngles jointAngles) {
        this.jointAngles = jointAngles;
    }
    
    public double getSpeed() {
        return speed;
    }
    
    public void setSpeed(double speed) {
        this.speed = speed;
    }
    
    public String getCoordinateSystem() {
        return coordinateSystem;
    }
    
    public void setCoordinateSystem(String coordinateSystem) {
        this.coordinateSystem = coordinateSystem;
    }
    
    @Override
    public String toString() {
        return "RobotMovement{" +
                "type=" + type +
                ", targetPosition=" + targetPosition +
                ", viaPoint=" + viaPoint +
                ", jointAngles=" + jointAngles +
                ", speed=" + speed +
                ", coordinateSystem='" + coordinateSystem + '\'' +
                '}';
    }
}