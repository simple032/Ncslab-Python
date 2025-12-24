package com.hualong.websocket;

/**
 * Represents a 3D point in space
 */
public class Point {
    private double x;
    private double y;
    private double z;
    
    public Point() {
        this(0.0, 0.0, 0.0);
    }
    
    public Point(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    // Getters and setters
    public double getX() {
        return x;
    }
    
    public void setX(double x) {
        this.x = x;
    }
    
    public double getY() {
        return y;
    }
    
    public void setY(double y) {
        this.y = y;
    }
    
    public double getZ() {
        return z;
    }
    
    public void setZ(double z) {
        this.z = z;
    }
    
    // Utility methods
    public double distanceTo(Point other) {
        if (other == null) return Double.MAX_VALUE;
        
        double dx = this.x - other.x;
        double dy = this.y - other.y;
        double dz = this.z - other.z;
        
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }
    
    public Point add(Point other) {
        if (other == null) return new Point(this.x, this.y, this.z);
        return new Point(this.x + other.x, this.y + other.y, this.z + other.z);
    }
    
    public Point subtract(Point other) {
        if (other == null) return new Point(this.x, this.y, this.z);
        return new Point(this.x - other.x, this.y - other.y, this.z - other.z);
    }
    
    public Point multiply(double scalar) {
        return new Point(this.x * scalar, this.y * scalar, this.z * scalar);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Point point = (Point) obj;
        return Double.compare(point.x, x) == 0 &&
               Double.compare(point.y, y) == 0 &&
               Double.compare(point.z, z) == 0;
    }
    
    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(x);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(y);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(z);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
    
    @Override
    public String toString() {
        return String.format("Point(%.3f, %.3f, %.3f)", x, y, z);
    }
}