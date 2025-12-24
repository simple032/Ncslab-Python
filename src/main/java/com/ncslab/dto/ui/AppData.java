package com.ncslab.dto.ui;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO class representing an individual app with its components.
 * Corresponds to the C AppData structure.
 */
public class AppData {

    @JsonProperty("app_id")
    private int appId;

    @JsonProperty("name")
    private String name;

    @JsonProperty("main_figure")
    private UIFigure mainFigure;

    @JsonProperty("components")
    private List<UIComponent> components;

    @JsonProperty("component_count")
    private int componentCount;

    @JsonProperty("is_running")
    private boolean isRunning;

    public AppData() {}

    public AppData(int appId, String name, UIFigure mainFigure, List<UIComponent> components,
                   int componentCount, boolean isRunning) {
        this.appId = appId;
        this.name = name;
        this.mainFigure = mainFigure;
        this.components = components;
        this.componentCount = componentCount;
        this.isRunning = isRunning;
    }

    // Getters and Setters
    public int getAppId() {
        return appId;
    }

    public void setAppId(int appId) {
        this.appId = appId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UIFigure getMainFigure() {
        return mainFigure;
    }

    public void setMainFigure(UIFigure mainFigure) {
        this.mainFigure = mainFigure;
    }

    public List<UIComponent> getComponents() {
        return components;
    }

    public void setComponents(List<UIComponent> components) {
        this.components = components;
    }

    public int getComponentCount() {
        return componentCount;
    }

    public void setComponentCount(int componentCount) {
        this.componentCount = componentCount;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void setRunning(boolean running) {
        isRunning = running;
    }

    @Override
    public String toString() {
        return "AppData{" +
                "appId=" + appId +
                ", name='" + name + '\'' +
                ", mainFigure=" + mainFigure +
                ", components=" + components +
                ", componentCount=" + componentCount +
                ", isRunning=" + isRunning +
                '}';
    }
}