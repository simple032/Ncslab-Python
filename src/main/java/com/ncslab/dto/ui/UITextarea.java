package com.ncslab.dto.ui;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO class representing a UI Textarea.
 * Corresponds to the C UITextarea structure.
 */
public class UITextarea extends UIComponent {

    @JsonProperty("value")
    private String value;

    @JsonProperty("placeholder")
    private String placeholder;

    @JsonProperty("editable")
    private boolean editable;

    @JsonProperty("word_wrap")
    private boolean wordWrap;

    @JsonProperty("scroll_visible")
    private boolean scrollVisible;

    @JsonProperty("value_changed_callback")
    private String valueChangedCallback;

    public UITextarea() {
        super();
        setType(UIComponentType.UI_TEXTAREA);
    }

    public UITextarea(String value, String placeholder, boolean editable, boolean wordWrap,
                      boolean scrollVisible, String valueChangedCallback) {
        super();
        setType(UIComponentType.UI_TEXTAREA);
        this.value = value;
        this.placeholder = placeholder;
        this.editable = editable;
        this.wordWrap = wordWrap;
        this.scrollVisible = scrollVisible;
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

    public boolean isWordWrap() {
        return wordWrap;
    }

    public void setWordWrap(boolean wordWrap) {
        this.wordWrap = wordWrap;
    }

    public boolean isScrollVisible() {
        return scrollVisible;
    }

    public void setScrollVisible(boolean scrollVisible) {
        this.scrollVisible = scrollVisible;
    }

    public String getValueChangedCallback() {
        return valueChangedCallback;
    }

    public void setValueChangedCallback(String valueChangedCallback) {
        this.valueChangedCallback = valueChangedCallback;
    }

    @Override
    public String toString() {
        return "UITextarea{" +
                "value='" + value + '\'' +
                ", placeholder='" + placeholder + '\'' +
                ", editable=" + editable +
                ", wordWrap=" + wordWrap +
                ", scrollVisible=" + scrollVisible +
                ", valueChangedCallback='" + valueChangedCallback + '\'' +
                ", " + super.toString() +
                '}';
    }
}