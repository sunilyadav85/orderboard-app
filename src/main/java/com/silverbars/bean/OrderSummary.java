package com.silverbars.bean;

import com.silverbars.enums.OrderType;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Container for simplified order information.
 * <p>
 * In a production environment you could have UI version of this object which will be passed to UI from the REST api exposed by the application.
 * For simplicity sake this object is used to pass around within the application and externally through REST api.
 */
public class OrderSummary {

    private final double quantity;
    private final BigDecimal price;
    private final OrderType orderType;

    public OrderSummary(double quantity, BigDecimal price, OrderType orderType) {
        this.quantity = quantity;
        this.price = price;
        this.orderType = orderType;
    }

    public double getQuantity() {
        return quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public OrderType getOrderType() {
        return orderType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderSummary that = (OrderSummary) o;
        return Double.compare(that.quantity, quantity) == 0 &&
                Objects.equals(price, that.price) &&
                orderType == that.orderType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(quantity, price, orderType);
    }

    @Override
    public String toString() {
        return "OrderSummary{" +
                "quantity=" + quantity +
                ", price=" + price +
                ", orderType=" + orderType +
                '}';
    }
}
