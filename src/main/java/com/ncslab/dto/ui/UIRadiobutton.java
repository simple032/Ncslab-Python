package com.ncslab.dto.ui;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO class representing a UI Radiobutton.
 * Corresponds to the C UIRadiobutton structure.
 */
public class UIRadiobutton extends UIComponent {

    @JsonProperty("text")
    private String text;

    @JsonProperty("group_id")
    private String groupId;

    @JsonProperty("value")
    private int value;

    @JsonProperty("value_changed_callback")
    private String valueChangedCallback;

    public UIRadiobutton() {
        super();
        setType(UIComponentType.UI_RADIOBUTTON);
    }

    public UIRadiobutton(String text, String groupId, int value, String valueChangedCallback) {
        super();
        setType(UIComponentType.UI_RADIOBUTTON);
        this.text = text;
        this.groupId = groupId;
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

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
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
        return "UIRadiobutton{" +
                "text='" + text + '\'' +
                ", groupId='" + groupId + '\'' +
                ", value=" + value +
                ", valueChangedCallback='" + valueChangedCallback + '\'' +
                ", " + super.toString() +
                '}';
    }
}