package com.silverbars.bean;

import com.silverbars.enums.OrderType;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * Container of Order information
 */
public class Order {

    private final long orderId;
    private final String user;
    private final double quantity;
    private final BigDecimal price;
    private final OrderType orderType;
    private char active;

    /**
     * I have added this basic Order Audit here to keep track of users who create and cancel orders.
     * In Prod application Order Audit entity would probably have its own workflow for CRUD operations and
     * probably this orderAudits object will not be part of this Order object
     */
    private final Collection<OrderAudit> orderAudits = new ArrayList<>();

    public Order(long orderId, String user, double quantity, BigDecimal price, OrderType orderType) {
        this.orderId = orderId;
        this.user = user;
        this.quantity = quantity;
        this.price = price;
        this.orderType = orderType;
        this.active = 'Y';
    }

    public long getOrderId() {
        return orderId;
    }

    public String getUser() {
        return user;
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

    public char getActive() {
        return active;
    }

    public void markOrderInActive() {
        this.active = 'N';
    }

    public Collection<OrderAudit> getOrderAudits() {
        return orderAudits;
    }

    public void addOrderAudit(OrderAudit orderAudit) {
        this.orderAudits.add(orderAudit);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return orderId == order.orderId &&
                Double.compare(order.quantity, quantity) == 0 &&
                active == order.active &&
                Objects.equals(user, order.user) &&
                Objects.equals(price, order.price) &&
                orderType == order.orderType &&
                Objects.equals(orderAudits, order.orderAudits);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderId, user, quantity, price, orderType, active, orderAudits);
    }

    @Override
    public String toString() {
        return "Order{" +
                "orderId=" + orderId +
                ", user='" + user + '\'' +
                ", quantity=" + quantity +
                ", price=" + price +
                ", orderType=" + orderType +
                ", active=" + active +
                ", orderAudits=" + orderAudits +
                '}';
    }
}


