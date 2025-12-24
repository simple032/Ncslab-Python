package com.ncslab.dto.ui;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO class representing a UI Label.
 * Corresponds to the C UILabel structure.
 */
public class UILabel extends UIComponent {

    @JsonProperty("text")
    private String text;

    @JsonProperty("horizontal_alignment")
    private String horizontalAlignment;

    @JsonProperty("vertical_alignment")
    private String verticalAlignment;

    @JsonProperty("word_wrap")
    private boolean wordWrap;

    public UILabel() {
        super();
        setType(UIComponentType.UI_LABEL);
    }

    public UILabel(String text, String horizontalAlignment, String verticalAlignment, boolean wordWrap) {
        super();
        setType(UIComponentType.UI_LABEL);
        this.text = text;
        this.horizontalAlignment = horizontalAlignment;
        this.verticalAlignment = verticalAlignment;
        this.wordWrap = wordWrap;
    }

    // Getters and Setters
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getHorizontalAlignment() {
        return horizontalAlignment;
    }

    public void setHorizontalAlignment(String horizontalAlignment) {
        this.horizontalAlignment = horizontalAlignment;
    }

    public String getVerticalAlignment() {
        return verticalAlignment;
    }

    public void setVerticalAlignment(String verticalAlignment) {
        this.verticalAlignment = verticalAlignment;
    }

    public boolean isWordWrap() {
        return wordWrap;
    }

    public void setWordWrap(boolean wordWrap) {
        this.wordWrap = wordWrap;
    }

    @Override
    public String toString() {
        return "UILabel{" +
                "text='" + text + '\'' +
                ", horizontalAlignment='" + horizontalAlignment + '\'' +
                ", verticalAlignment='" + verticalAlignment + '\'' +
                ", wordWrap=" + wordWrap +
                ", " + super.toString() +
                '}';
    }
}