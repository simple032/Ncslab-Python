package com.ncslab.dto.ui;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO class representing a UI Spinner.
 * Corresponds to the C UISpinner structure.
 */
public class UISpinner extends UIComponent {

    @JsonProperty("value")
    private double value;

    @JsonProperty("min")
    private double min;

    @JsonProperty("max")
    private double max;

    @JsonProperty("step")
    private double step;

    @JsonProperty("editable")
    private boolean editable;

    @JsonProperty("value_changed_callback")
    private String valueChangedCallback;

    public UISpinner() {
        super();
        setType(UIComponentType.UI_SPINNER);
    }

    public UISpinner(double value, double min, double max, double step, boolean editable, String valueChangedCallback) {
        super();
        setType(UIComponentType.UI_SPINNER);
        this.value = value;
        this.min = min;
        this.max = max;
        this.step = step;
        this.editable = editable;
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

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    public String getValueChangedCallback() {
        return valueChangedCallback;
    }

    public void setValueChangedCallback(String valueChangedCallback) {
        this.valueChangedCallback = valueChangedCallback;
    }

    @Override
    public String toString() {
        return "UISpinner{" +
                "value=" + value +
                ", min=" + min +
                ", max=" + max +
                ", step=" + step +
                ", editable=" + editable +
                ", valueChangedCallback='" + valueChangedCallback + '\'' +
                ", " + super.toString() +
                '}';
    }
}