package com.ncslab.dto.ui;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO class representing UI component position.
 * Corresponds to the C UIPosition structure.
 */
public class UIPosition {

    @JsonProperty("left")
    private double left;

    @JsonProperty("bottom")
    private double bottom;

    @JsonProperty("width")
    private double width;

    @JsonProperty("height")
    private double height;

    public UIPosition() {}

    public UIPosition(double left, double bottom, double width, double height) {
        this.left = left;
        this.bottom = bottom;
        this.width = width;
        this.height = height;
    }

    // Getters and Setters
    public double getLeft() {
        return left;
    }

    public void setLeft(double left) {
        this.left = left;
    }

    public double getBottom() {
        return bottom;
    }

    public void setBottom(double bottom) {
        this.bottom = bottom;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    @Override
    public String toString() {
        return "UIPosition{" +
                "left=" + left +
                ", bottom=" + bottom +
                ", width=" + width +
                ", height=" + height +
                '}';
    }
}