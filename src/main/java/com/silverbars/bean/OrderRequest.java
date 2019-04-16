package com.silverbars.bean;

import com.silverbars.enums.OrderType;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

public class OrderRequest {

    @NotNull(message = "User cannot be null")
    private String user;

    @NotNull(message = "Quantity cannot be null")
    private Double quantity;

    @NotNull(message = "Price cannot be null")
    private BigDecimal price;

    @NotNull(message = "Order Type cannot be null")
    private OrderType orderType;

    /* Needed for Jackson */
    public OrderRequest() {
    }

    public OrderRequest(String user, Double quantity, BigDecimal price, OrderType orderType) {
        this.user = user;
        this.quantity = quantity;
        this.price = price;
        this.orderType = orderType;
    }

    public String getUser() {
        return user;
    }

    public Double getQuantity() {
        return quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public OrderType getOrderType() {
        return orderType;
    }
}
