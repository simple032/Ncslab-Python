package com.ncslab.dto.ui;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * DTO class representing a UI Dropdown.
 * Corresponds to the C UIDropdown structure.
 */
public class UIDropdown extends UIComponent {

    @JsonProperty("items")
    private List<String> items;

    @JsonProperty("item_count")
    private int itemCount;

    @JsonProperty("selected_index")
    private int selectedIndex;

    @JsonProperty("value")
    private String value;

    @JsonProperty("placeholder")
    private String placeholder;

    @JsonProperty("editable")
    private boolean editable;

    @JsonProperty("value_changed_callback")
    private String valueChangedCallback;

    public UIDropdown() {
        super();
        setType(UIComponentType.UI_DROPDOWN);
    }

    public UIDropdown(List<String> items, int itemCount, int selectedIndex, String value,
                      String placeholder, boolean editable, String valueChangedCallback) {
        super();
        setType(UIComponentType.UI_DROPDOWN);
        this.items = items;
        this.itemCount = itemCount;
        this.selectedIndex = selectedIndex;
        this.value = value;
        this.placeholder = placeholder;
        this.editable = editable;
        this.valueChangedCallback = valueChangedCallback;
    }

    // Getters and Setters
    public List<String> getItems() {
        return items;
    }

    public void setItems(List<String> items) {
        this.items = items;
    }

    public int getItemCount() {
        return itemCount;
    }

    public void setItemCount(int itemCount) {
        this.itemCount = itemCount;
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public void setSelectedIndex(int selectedIndex) {
        this.selectedIndex = selectedIndex;
    }

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

    public String getValueChangedCallback() {
        return valueChangedCallback;
    }

    public void setValueChangedCallback(String valueChangedCallback) {
        this.valueChangedCallback = valueChangedCallback;
    }

    @Override
    public String toString() {
        return "UIDropdown{" +
                "items=" + items +
                ", itemCount=" + itemCount +
                ", selectedIndex=" + selectedIndex +
                ", value='" + value + '\'' +
                ", placeholder='" + placeholder + '\'' +
                ", editable=" + editable +
                ", valueChangedCallback='" + valueChangedCallback + '\'' +
                ", " + super.toString() +
                '}';
    }
}