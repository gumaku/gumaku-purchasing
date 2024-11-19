package com.p2p.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Future;
import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Data
public class OrderCreateRequest {
    @NotBlank(message = "Product name is required")
    private String productName;
    
    private String productLink;
    
    @NotNull(message = "Product price is required")
    @Positive(message = "Product price must be positive")
    private BigDecimal productPrice;
    
    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    private Integer quantity;
    
    private String description;
    
    @Positive(message = "Minimum participants must be positive")
    private Integer minParticipants;
    
    @NotBlank(message = "Delivery address is required")
    private String deliveryAddress;
    
    @NotBlank(message = "Delivery country is required")
    private String deliveryCountry;
    
    @Future(message = "Deadline must be in the future")
    private ZonedDateTime deadline;
} 