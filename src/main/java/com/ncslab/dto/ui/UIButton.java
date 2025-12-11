package com.ncslab.dto.ui;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Enum for button styles.
 * Corresponds to the C ButtonStyle enum.
 */
enum ButtonStyle {
    BUTTON_STYLE_PUSH,
    BUTTON_STYLE_STATE
}

/**
 * DTO class representing a UI Button.
 * Corresponds to the C UIButton structure.
 */
public class UIButton extends UIComponent {

    @JsonProperty("text")
    private String text;

    @JsonProperty("icon")
    private String icon;

    @JsonProperty("style")
    private ButtonStyle style;

    @JsonProperty("callback")
    private String callback;

    @JsonProperty("value")
    private int value;

    public UIButton() {
        super();
        setType(UIComponentType.UI_BUTTON);
    }

    public UIButton(String text, String icon, ButtonStyle style, String callback, int value) {
        super();
        setType(UIComponentType.UI_BUTTON);
        this.text = text;
        this.icon = icon;
        this.style = style;
        this.callback = callback;
        this.value = value;
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

    public ButtonStyle getStyle() {
        return style;
    }

    public void setStyle(ButtonStyle style) {
        this.style = style;
    }

    public String getCallback() {
        return callback;
    }

    public void setCallback(String callback) {
        this.callback = callback;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "UIButton{" +
                "text='" + text + '\'' +
                ", icon='" + icon + '\'' +
                ", style=" + style +
                ", callback='" + callback + '\'' +
                ", value=" + value +
                ", " + super.toString() +
                '}';
    }
}