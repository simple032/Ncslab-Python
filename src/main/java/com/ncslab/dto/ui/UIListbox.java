package com.ncslab.dto.ui;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * DTO class representing a UI Listbox.
 * Corresponds to the C UIListbox structure.
 */
public class UIListbox extends UIComponent {

    @JsonProperty("items")
    private List<String> items;

    @JsonProperty("item_count")
    private int itemCount;

    @JsonProperty("selected_indices")
    private List<Integer> selectedIndices;

    @JsonProperty("selected_count")
    private int selectedCount;

    @JsonProperty("value")
    private String value;

    @JsonProperty("multiselect")
    private boolean multiselect;

    @JsonProperty("value_changed_callback")
    private String valueChangedCallback;

    public UIListbox() {
        super();
        setType(UIComponentType.UI_LISTBOX);
    }

    public UIListbox(List<String> items, int itemCount, List<Integer> selectedIndices,
                     int selectedCount, String value, boolean multiselect, String valueChangedCallback) {
        super();
        setType(UIComponentType.UI_LISTBOX);
        this.items = items;
        this.itemCount = itemCount;
        this.selectedIndices = selectedIndices;
        this.selectedCount = selectedCount;
        this.value = value;
        this.multiselect = multiselect;
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

    public List<Integer> getSelectedIndices() {
        return selectedIndices;
    }

    public void setSelectedIndices(List<Integer> selectedIndices) {
        this.selectedIndices = selectedIndices;
    }

    public int getSelectedCount() {
        return selectedCount;
    }

    public void setSelectedCount(int selectedCount) {
        this.selectedCount = selectedCount;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean isMultiselect() {
        return multiselect;
    }

    public void setMultiselect(boolean multiselect) {
        this.multiselect = multiselect;
    }

    public String getValueChangedCallback() {
        return valueChangedCallback;
    }

    public void setValueChangedCallback(String valueChangedCallback) {
        this.valueChangedCallback = valueChangedCallback;
    }

    @Override
    public String toString() {
        return "UIListbox{" +
                "items=" + items +
                ", itemCount=" + itemCount +
                ", selectedIndices=" + selectedIndices +
                ", selectedCount=" + selectedCount +
                ", value='" + value + '\'' +
                ", multiselect=" + multiselect +
                ", valueChangedCallback='" + valueChangedCallback + '\'' +
                ", " + super.toString() +
                '}';
    }
}