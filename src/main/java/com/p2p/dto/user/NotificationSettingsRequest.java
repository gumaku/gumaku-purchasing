package com.p2p.dto.user;

import com.p2p.domain.User;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class NotificationSettingsRequest {
    @NotNull
    private Boolean emailEnabled;
    
    @NotNull
    private Boolean pushEnabled;
    
    @NotNull
    private Boolean orderUpdates;
    
    @NotNull
    private Boolean paymentUpdates;
    
    @NotNull
    private Boolean marketingEmails;

    public User.NotificationSettings toSettings() {
        User.NotificationSettings settings = new User.NotificationSettings();
        settings.setEmailEnabled(emailEnabled);
        settings.setPushEnabled(pushEnabled);
        settings.setOrderUpdates(orderUpdates);
        settings.setPaymentUpdates(paymentUpdates);
        settings.setMarketingEmails(marketingEmails);
        return settings;
    }
} 