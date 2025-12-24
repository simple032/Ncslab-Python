package com.hualong.websocket;

/**
 * Represents robot pose (position and orientation)
 */
public class RobotPose {
    private Point position;
    private Pose orientation;
    
    public RobotPose() {
        this.position = new Point(0, 0, 0);
        this.orientation = new Pose(0, 0, 0);
    }
    
    public RobotPose(Point position, Pose orientation) {
        this.position = position;
        this.orientation = orientation;
    }
    
    public Point getPosition() {
        return position;
    }
    
    public void setPosition(Point position) {
        this.position = position;
    }
    
    public Pose getOrientation() {
        return orientation;
    }
    
    public void setOrientation(Pose orientation) {
        this.orientation = orientation;
    }
    
    @Override
    public String toString() {
        return "RobotPose{" +
                "position=" + position +
                ", orientation=" + orientation +
                '}';
    }
}