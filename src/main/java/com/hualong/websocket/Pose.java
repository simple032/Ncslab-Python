package com.hualong.websocket;

/**
 * Represents 3D orientation using Euler angles (rx, ry, rz)
 */
public class Pose {
    private double rx; // Rotation around X-axis
    private double ry; // Rotation around Y-axis  
    private double rz; // Rotation around Z-axis
    
    public Pose() {
        this(0.0, 0.0, 0.0);
    }
    
    public Pose(double rx, double ry, double rz) {
        this.rx = rx;
        this.ry = ry;
        this.rz = rz;
    }
    
    // Getters and setters
    public double getRx() {
        return rx;
    }
    
    public void setRx(double rx) {
        this.rx = rx;
    }
    
    public double getRy() {
        return ry;
    }
    
    public void setRy(double ry) {
        this.ry = ry;
    }
    
    public double getRz() {
        return rz;
    }
    
    public void setRz(double rz) {
        this.rz = rz;
    }
    
    // Convert degrees to radians
    public Pose toRadians() {
        return new Pose(
            Math.toRadians(rx),
            Math.toRadians(ry), 
            Math.toRadians(rz)
        );
    }
    
    // Convert radians to degrees
    public Pose toDegrees() {
        return new Pose(
            Math.toDegrees(rx),
            Math.toDegrees(ry),
            Math.toDegrees(rz)
        );
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Pose pose = (Pose) obj;
        return Double.compare(pose.rx, rx) == 0 &&
               Double.compare(pose.ry, ry) == 0 &&
               Double.compare(pose.rz, rz) == 0;
    }
    
    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(rx);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(ry);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(rz);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
    
    @Override
    public String toString() {
        return String.format("Pose(%.3f°, %.3f°, %.3f°)", rx, ry, rz);
    }
}