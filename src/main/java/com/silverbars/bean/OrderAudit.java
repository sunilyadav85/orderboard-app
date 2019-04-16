package com.silverbars.bean;

import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * Container of Order Audit information
 */
public class OrderAudit {

    private final long orderId;
    private final String user;
    private final OffsetDateTime addTime;

    public OrderAudit(long orderId, String user, OffsetDateTime addTime) {
        this.orderId = orderId;
        this.user = user;
        this.addTime = addTime;
    }

    public long getOrderId() {
        return orderId;
    }

    public String getUser() {
        return user;
    }

    public OffsetDateTime getAddTime() {
        return addTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderAudit that = (OrderAudit) o;
        return orderId == that.orderId &&
                Objects.equals(user, that.user) &&
                Objects.equals(addTime, that.addTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderId, user, addTime);
    }

    @Override
    public String toString() {
        return "OrderAudit{" +
                "orderId=" + orderId +
                ", user='" + user + '\'' +
                ", addTime=" + addTime +
                '}';
    }
}
