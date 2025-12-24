package com.hualong.websocket;

/**
 * Represents a coordinate system for robot programming
 */
public class CoordinateSystem {
    private String name;
    private Point origin;
    private Pose orientation;
    private String description;
    
    public CoordinateSystem() {
        this.name = "default";
        this.origin = new Point(0, 0, 0);
        this.orientation = new Pose(0, 0, 0);
        this.description = "";
    }
    
    public CoordinateSystem(String name, Point origin) {
        this.name = name;
        this.origin = origin;
        this.orientation = new Pose(0, 0, 0);
        this.description = "";
    }
    
    public CoordinateSystem(String name, Point origin, Pose orientation) {
        this.name = name;
        this.origin = origin;
        this.orientation = orientation;
        this.description = "";
    }
    
    // Getters and setters
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public Point getOrigin() {
        return origin;
    }
    
    public void setOrigin(Point origin) {
        this.origin = origin;
    }
    
    public Pose getOrientation() {
        return orientation;
    }
    
    public void setOrientation(Pose orientation) {
        this.orientation = orientation;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    /**
     * Transform a point from this coordinate system to world coordinates
     */
    public Point transformToWorld(Point localPoint) {
        if (localPoint == null) return null;
        
        // Simple transformation (in real implementation would include rotation)
        return localPoint.add(origin);
    }
    
    /**
     * Transform a point from world coordinates to this coordinate system
     */
    public Point transformFromWorld(Point worldPoint) {
        if (worldPoint == null) return null;
        
        // Simple transformation (in real implementation would include rotation)
        return worldPoint.subtract(origin);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        CoordinateSystem that = (CoordinateSystem) obj;
        return name.equals(that.name);
    }
    
    @Override
    public int hashCode() {
        return name.hashCode();
    }
    
    @Override
    public String toString() {
        return "CoordinateSystem{" +
                "name='" + name + '\'' +
                ", origin=" + origin +
                ", orientation=" + orientation +
                ", description='" + description + '\'' +
                '}';
    }
}