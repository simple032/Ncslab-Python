package com.ncslab.database;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class License {
    private String licenseKey;
    private String user;
    private LocalDate startDate;
    private LocalDate endDate;
    private long timestamp;
}
