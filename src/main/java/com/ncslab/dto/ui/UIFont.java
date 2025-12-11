package com.ncslab.dto.ui;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO class representing UI component font.
 * Corresponds to the C UIFont structure.
 */
public class UIFont {

    @JsonProperty("name")
    private String name;

    @JsonProperty("size")
    private double size;

    @JsonProperty("weight")
    private String weight;

    @JsonProperty("angle")
    private String angle;

    public UIFont() {}

    public UIFont(String name, double size, String weight, String angle) {
        this.name = name;
        this.size = size;
        this.weight = weight;
        this.angle = angle;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getSize() {
        return size;
    }

    public void setSize(double size) {
        this.size = size;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public String getAngle() {
        return angle;
    }

    public void setAngle(String angle) {
        this.angle = angle;
    }

    @Override
    public String toString() {
        return "UIFont{" +
                "name='" + name + '\'' +
                ", size=" + size +
                ", weight='" + weight + '\'' +
                ", angle='" + angle + '\'' +
                '}';
    }
}