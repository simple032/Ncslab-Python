package com.hualong.websocket;

import java.util.Arrays;

/**
 * Represents robot joint angles
 */
public class JointAngles {
    private double[] angles;
    
    public JointAngles(int numJoints) {
        this.angles = new double[numJoints];
    }
    
    public JointAngles(double[] angles) {
        this.angles = angles.clone();
    }
    
    public double[] getAngles() {
        return angles.clone();
    }
    
    public void setAngles(double[] angles) {
        this.angles = angles.clone();
    }
    
    public double getAngle(int jointIndex) {
        if (jointIndex >= 0 && jointIndex < angles.length) {
            return angles[jointIndex];
        }
        return 0.0;
    }
    
    public void setAngle(int jointIndex, double angle) {
        if (jointIndex >= 0 && jointIndex < angles.length) {
            angles[jointIndex] = angle;
        }
    }
    
    public int getNumJoints() {
        return angles.length;
    }
    
    // Convert degrees to radians
    public JointAngles toRadians() {
        double[] radiansAngles = new double[angles.length];
        for (int i = 0; i < angles.length; i++) {
            radiansAngles[i] = Math.toRadians(angles[i]);
        }
        return new JointAngles(radiansAngles);
    }
    
    // Convert radians to degrees
    public JointAngles toDegrees() {
        double[] degreesAngles = new double[angles.length];
        for (int i = 0; i < angles.length; i++) {
            degreesAngles[i] = Math.toDegrees(angles[i]);
        }
        return new JointAngles(degreesAngles);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        JointAngles that = (JointAngles) obj;
        return Arrays.equals(angles, that.angles);
    }
    
    @Override
    public int hashCode() {
        return Arrays.hashCode(angles);
    }
    
    @Override
    public String toString() {
        return "JointAngles" + Arrays.toString(angles);
    }
}