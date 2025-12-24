package com.ncslab.dto.ui;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO class representing an app message containing multiple apps and their UI components.
 * Corresponds to the C AppMessage structure.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppMessage {

    @JsonProperty("app_count")
    private int appCount;

    @JsonProperty("apps")
    private List<AppData> apps;

    @Override
    public String toString() {
        return "AppMessage{" +
                "appCount=" + appCount +
                ", apps=" + apps +
                '}';
    }
}