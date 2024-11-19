package com.p2p.dto.order;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Data
public class OrderRequest {
    @NotBlank(message = "商品名稱不能為空")
    private String productName;

    private String productLink;

    @NotNull(message = "商品價格不能為空")
    @Min(value = 0, message = "商品價格必須大於0")
    private BigDecimal productPrice;

    @NotNull(message = "商品數量不能為空")
    @Min(value = 1, message = "商品數量必須大於0")
    private Integer quantity;

    private String description;

    @NotBlank(message = "配送地址不能為空")
    private String deliveryAddress;

    @NotBlank(message = "配送國家不能為空")
    private String deliveryCountry;

    private Integer minParticipants;

    private ZonedDateTime deadline;
} 