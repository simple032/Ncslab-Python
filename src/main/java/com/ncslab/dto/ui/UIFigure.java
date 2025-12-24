package com.ncslab.dto.ui;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO class representing a UI Figure (main app window).
 * Corresponds to the C UIFigure structure.
 */
public class UIFigure extends UIComponent {

    @JsonProperty("name")
    private String name;

    @JsonProperty("window_style")
    private String windowStyle;

    @JsonProperty("resize")
    private boolean resize;

    @JsonProperty("color")
    private UIColor color;

    public UIFigure() {
        super();
        setType(UIComponentType.UI_FIGURE);
    }

    public UIFigure(String name, String windowStyle, boolean resize, UIColor color) {
        super();
        setType(UIComponentType.UI_FIGURE);
        this.name = name;
        this.windowStyle = windowStyle;
        this.resize = resize;
        this.color = color;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getWindowStyle() {
        return windowStyle;
    }

    public void setWindowStyle(String windowStyle) {
        this.windowStyle = windowStyle;
    }

    public boolean isResize() {
        return resize;
    }

    public void setResize(boolean resize) {
        this.resize = resize;
    }

    public UIColor getColor() {
        return color;
    }

    public void setColor(UIColor color) {
        this.color = color;
    }

    @Override
    public String toString() {
        return "UIFigure{" +
                "name='" + name + '\'' +
                ", windowStyle='" + windowStyle + '\'' +
                ", resize=" + resize +
                ", color=" + color +
                ", " + super.toString() +
                '}';
    }
}