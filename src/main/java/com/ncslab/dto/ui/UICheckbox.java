package com.ncslab.dto.ui;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO class representing a UI Checkbox.
 * Corresponds to the C UICheckbox structure.
 */
public class UICheckbox extends UIComponent {

    @JsonProperty("text")
    private String text;

    @JsonProperty("value")
    private int value;

    @JsonProperty("value_changed_callback")
    private String valueChangedCallback;

    public UICheckbox() {
        super();
        setType(UIComponentType.UI_CHECKBOX);
    }

    public UICheckbox(String text, int value, String valueChangedCallback) {
        super();
        setType(UIComponentType.UI_CHECKBOX);
        this.text = text;
        this.value = value;
        this.valueChangedCallback = valueChangedCallback;
    }

    // Getters and Setters
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getValueChangedCallback() {
        return valueChangedCallback;
    }

    public void setValueChangedCallback(String valueChangedCallback) {
        this.valueChangedCallback = valueChangedCallback;
    }

    @Override
    public String toString() {
        return "UICheckbox{" +
                "text='" + text + '\'' +
                ", value=" + value +
                ", valueChangedCallback='" + valueChangedCallback + '\'' +
                ", " + super.toString() +
                '}';
    }
}