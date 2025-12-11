package com.ncslab.dto.ui;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO class representing a UI EditField.
 * Corresponds to the C UIEditField structure.
 */
public class UIEditField extends UIComponent {

    @JsonProperty("value")
    private String value;

    @JsonProperty("placeholder")
    private String placeholder;

    @JsonProperty("editable")
    private boolean editable;

    @JsonProperty("numeric")
    private boolean numeric;

    @JsonProperty("numeric_value")
    private double numericValue;

    @JsonProperty("lower_limit")
    private double lowerLimit;

    @JsonProperty("upper_limit")
    private double upperLimit;

    @JsonProperty("value_changed_callback")
    private String valueChangedCallback;

    public UIEditField() {
        super();
        setType(UIComponentType.UI_EDITFIELD);
    }

    public UIEditField(String value, String placeholder, boolean editable, boolean numeric,
                       double numericValue, double lowerLimit, double upperLimit, String valueChangedCallback) {
        super();
        setType(UIComponentType.UI_EDITFIELD);
        this.value = value;
        this.placeholder = placeholder;
        this.editable = editable;
        this.numeric = numeric;
        this.numericValue = numericValue;
        this.lowerLimit = lowerLimit;
        this.upperLimit = upperLimit;
        this.valueChangedCallback = valueChangedCallback;
    }

    // Getters and Setters
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getPlaceholder() {
        return placeholder;
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    public boolean isNumeric() {
        return numeric;
    }

    public void setNumeric(boolean numeric) {
        this.numeric = numeric;
    }

    public double getNumericValue() {
        return numericValue;
    }

    public void setNumericValue(double numericValue) {
        this.numericValue = numericValue;
    }

    public double getLowerLimit() {
        return lowerLimit;
    }

    public void setLowerLimit(double lowerLimit) {
        this.lowerLimit = lowerLimit;
    }

    public double getUpperLimit() {
        return upperLimit;
    }

    public void setUpperLimit(double upperLimit) {
        this.upperLimit = upperLimit;
    }

    public String getValueChangedCallback() {
        return valueChangedCallback;
    }

    public void setValueChangedCallback(String valueChangedCallback) {
        this.valueChangedCallback = valueChangedCallback;
    }

    @Override
    public String toString() {
        return "UIEditField{" +
                "value='" + value + '\'' +
                ", placeholder='" + placeholder + '\'' +
                ", editable=" + editable +
                ", numeric=" + numeric +
                ", numericValue=" + numericValue +
                ", lowerLimit=" + lowerLimit +
                ", upperLimit=" + upperLimit +
                ", valueChangedCallback='" + valueChangedCallback + '\'' +
                ", " + super.toString() +
                '}';
    }
}