package com.ncslab.dto.ui;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Base DTO class for all UI components.
 * Corresponds to the C UIComponent structure.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = UIFigure.class, name = "UI_FIGURE"),
    @JsonSubTypes.Type(value = UIButton.class, name = "UI_BUTTON"),
    @JsonSubTypes.Type(value = UILabel.class, name = "UI_LABEL"),
    @JsonSubTypes.Type(value = UIEditField.class, name = "UI_EDITFIELD"),
    @JsonSubTypes.Type(value = UIDropdown.class, name = "UI_DROPDOWN"),
    @JsonSubTypes.Type(value = UICheckbox.class, name = "UI_CHECKBOX"),
    @JsonSubTypes.Type(value = UISlider.class, name = "UI_SLIDER"),
    @JsonSubTypes.Type(value = UIListbox.class, name = "UI_LISTBOX"),
    @JsonSubTypes.Type(value = UIRadiobutton.class, name = "UI_RADIOBUTTON"),
    @JsonSubTypes.Type(value = UITextarea.class, name = "UI_TEXTAREA"),
    @JsonSubTypes.Type(value = UISpinner.class, name = "UI_SPINNER"),
    @JsonSubTypes.Type(value = UITogglebutton.class, name = "UI_TOGGLEBUTTON")
})
public abstract class UIComponent {

    @JsonProperty("type")
    private UIComponentType type;

    @JsonProperty("tag")
    private String tag;

    @JsonProperty("position")
    private UIPosition position;

    @JsonProperty("background_color")
    private UIColor backgroundColor;

    @JsonProperty("foreground_color")
    private UIColor foregroundColor;

    @JsonProperty("font")
    private UIFont font;

    @JsonProperty("visible")
    private boolean visible;

    @JsonProperty("enable")
    private boolean enable;

    @JsonProperty("tooltip")
    private String tooltip;

    public UIComponent() {}

    public UIComponent(UIComponentType type, String tag, UIPosition position,
                       UIColor backgroundColor, UIColor foregroundColor, UIFont font,
                       boolean visible, boolean enable, String tooltip) {
        this.type = type;
        this.tag = tag;
        this.position = position;
        this.backgroundColor = backgroundColor;
        this.foregroundColor = foregroundColor;
        this.font = font;
        this.visible = visible;
        this.enable = enable;
        this.tooltip = tooltip;
    }

    // Getters and Setters
    public UIComponentType getType() {
        return type;
    }

    public void setType(UIComponentType type) {
        this.type = type;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public UIPosition getPosition() {
        return position;
    }

    public void setPosition(UIPosition position) {
        this.position = position;
    }

    public UIColor getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(UIColor backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public UIColor getForegroundColor() {
        return foregroundColor;
    }

    public void setForegroundColor(UIColor foregroundColor) {
        this.foregroundColor = foregroundColor;
    }

    public UIFont getFont() {
        return font;
    }

    public void setFont(UIFont font) {
        this.font = font;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public String getTooltip() {
        return tooltip;
    }

    public void setTooltip(String tooltip) {
        this.tooltip = tooltip;
    }

    @Override
    public String toString() {
        return "UIComponent{" +
                "type=" + type +
                ", tag='" + tag + '\'' +
                ", position=" + position +
                ", backgroundColor=" + backgroundColor +
                ", foregroundColor=" + foregroundColor +
                ", font=" + font +
                ", visible=" + visible +
                ", enable=" + enable +
                ", tooltip='" + tooltip + '\'' +
                '}';
    }
}