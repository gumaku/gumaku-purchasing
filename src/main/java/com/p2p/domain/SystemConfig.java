package com.p2p.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.ZonedDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("system_configs")
public class SystemConfig {
    @Id
    private Long id;
    private String key;
    private String value;
    private String description;
    private String type;
    private boolean encrypted;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;

    public SystemConfig(String key, String value) {
        this.key = key;
        this.value = value;
        this.createdAt = ZonedDateTime.now();
        this.updatedAt = ZonedDateTime.now();
    }

    public void setValue(String value) {
        this.value = value;
        this.updatedAt = ZonedDateTime.now();
    }
} 