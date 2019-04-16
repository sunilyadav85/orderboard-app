package com.silverbars.bean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class OrderSummaryHolder {

    private final List<OrderSummary> buyOrderSummary = new ArrayList<>();
    private final List<OrderSummary> sellOrderSummary = new ArrayList<>();

    public List<OrderSummary> getBuyOrderSummary() {
        return buyOrderSummary;
    }

    public void addBuyOrderSummaries(Collection<OrderSummary> orderSummaries) {
        this.buyOrderSummary.addAll(orderSummaries);
    }

    public List<OrderSummary> getSellOrderSummary() {
        return sellOrderSummary;
    }

    public void addSellOrderSummaries(Collection<OrderSummary> orderSummaries) {
        this.sellOrderSummary.addAll(orderSummaries);
    }
}
