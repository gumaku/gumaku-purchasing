package com.p2p.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.ZonedDateTime;

@Data
@Table("notification_templates")
public class NotificationTemplate {
    @Id
    private Long id;
    private String templateCode;
    private String content;
    private String description;
    private String type;
    private boolean active;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;

    public NotificationTemplate() {
        this.active = true;
        this.createdAt = ZonedDateTime.now();
        this.updatedAt = ZonedDateTime.now();
    }

    public static NotificationTemplate create(String templateCode, String content) {
        NotificationTemplate template = new NotificationTemplate();
        template.setTemplateCode(templateCode);
        template.setContent(content);
        return template;
    }

    public void setContent(String content) {
        this.content = content;
        this.updatedAt = ZonedDateTime.now();
    }

    public String getContent() {
        return this.content;
    }

    public void activate() {
        this.active = true;
        this.updatedAt = ZonedDateTime.now();
    }

    public void deactivate() {
        this.active = false;
        this.updatedAt = ZonedDateTime.now();
    }

    public boolean isActive() {
        return this.active;
    }
} 