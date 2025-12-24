package com.ncslab.dto.ui;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO class representing a UI Slider.
 * Corresponds to the C UISlider structure.
 */
public class UISlider extends UIComponent {

    @JsonProperty("value")
    private double value;

    @JsonProperty("min")
    private double min;

    @JsonProperty("max")
    private double max;

    @JsonProperty("step")
    private double step;

    @JsonProperty("orientation")
    private String orientation;

    @JsonProperty("major_ticks")
    private boolean majorTicks;

    @JsonProperty("minor_ticks")
    private boolean minorTicks;

    @JsonProperty("value_changed_callback")
    private String valueChangedCallback;

    public UISlider() {
        super();
        setType(UIComponentType.UI_SLIDER);
    }

    public UISlider(double value, double min, double max, double step, String orientation,
                    boolean majorTicks, boolean minorTicks, String valueChangedCallback) {
        super();
        setType(UIComponentType.UI_SLIDER);
        this.value = value;
        this.min = min;
        this.max = max;
        this.step = step;
        this.orientation = orientation;
        this.majorTicks = majorTicks;
        this.minorTicks = minorTicks;
        this.valueChangedCallback = valueChangedCallback;
    }

    // Getters and Setters
    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public double getMin() {
        return min;
    }

    public void setMin(double min) {
        this.min = min;
    }

    public double getMax() {
        return max;
    }

    public void setMax(double max) {
        this.max = max;
    }

    public double getStep() {
        return step;
    }

    public void setStep(double step) {
        this.step = step;
    }

    public String getOrientation() {
        return orientation;
    }

    public void setOrientation(String orientation) {
        this.orientation = orientation;
    }

    public boolean isMajorTicks() {
        return majorTicks;
    }

    public void setMajorTicks(boolean majorTicks) {
        this.majorTicks = majorTicks;
    }

    public boolean isMinorTicks() {
        return minorTicks;
    }

    public void setMinorTicks(boolean minorTicks) {
        this.minorTicks = minorTicks;
    }

    public String getValueChangedCallback() {
        return valueChangedCallback;
    }

    public void setValueChangedCallback(String valueChangedCallback) {
        this.valueChangedCallback = valueChangedCallback;
    }

    @Override
    public String toString() {
        return "UISlider{" +
                "value=" + value +
                ", min=" + min +
                ", max=" + max +
                ", step=" + step +
                ", orientation='" + orientation + '\'' +
                ", majorTicks=" + majorTicks +
                ", minorTicks=" + minorTicks +
                ", valueChangedCallback='" + valueChangedCallback + '\'' +
                ", " + super.toString() +
                '}';
    }
}