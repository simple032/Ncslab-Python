package com.ncslab.dto.ui;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO class representing a UI Togglebutton.
 * Corresponds to the C UITogglebutton structure.
 */
public class UITogglebutton extends UIComponent {

    @JsonProperty("text")
    private String text;

    @JsonProperty("icon")
    private String icon;

    @JsonProperty("value")
    private int value;

    @JsonProperty("state_changed_callback")
    private String stateChangedCallback;

    public UITogglebutton() {
        super();
        setType(UIComponentType.UI_TOGGLEBUTTON);
    }

    public UITogglebutton(String text, String icon, int value, String stateChangedCallback) {
        super();
        setType(UIComponentType.UI_TOGGLEBUTTON);
        this.text = text;
        this.icon = icon;
        this.value = value;
        this.stateChangedCallback = stateChangedCallback;
    }

    // Getters and Setters
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getStateChangedCallback() {
        return stateChangedCallback;
    }

    public void setStateChangedCallback(String stateChangedCallback) {
        this.stateChangedCallback = stateChangedCallback;
    }

    @Override
    public String toString() {
        return "UITogglebutton{" +
                "text='" + text + '\'' +
                ", icon='" + icon + '\'' +
                ", value=" + value +
                ", stateChangedCallback='" + stateChangedCallback + '\'' +
                ", " + super.toString() +
                '}';
    }
}