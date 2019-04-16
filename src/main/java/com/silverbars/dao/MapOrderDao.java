package com.silverbars.dao;

import com.silverbars.bean.Order;
import com.silverbars.bean.OrderAudit;
import com.silverbars.bean.OrderSummary;
import com.silverbars.bean.OrderSummaryHolder;
import com.silverbars.enums.OrderType;
import com.silverbars.exception.OrderBoardInvalidOperationException;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * Map based implementation of Order Dao.
 * I have implemented most of the logic in this class in order to ease the testing.
 * For ex- you may want to have a generic method getOrders() instead of method getLiveOrders() in a prod application
 * and then you may want to do filtering of Live Orders and Sorting in the Service class
 */
@Repository
public class MapOrderDao implements OrderDao {

    /**
     * Concurrent HashMap is used to avoid ConcurrentModificationException because multiple threads could be reading and writing to this HashMap.
     * In a production environment where an application is deployed in a cluster you can avoid this issue by various techniques. For ex - using Transactions and locking shared resources
     */
    private Map<Long, Order> orderIdToOrderMap = new ConcurrentHashMap<>();

    /**
     * AtomicLong here is used to imitate a Database sequence that can be used safely in a concurrent app.
     */
    private AtomicLong sequenceId = new AtomicLong(1);

    private Lock lock = new ReentrantLock();

    /**
     * Adds the supplied order details to the system
     *
     * @param user      User initiating the order registration
     * @param quantity  Order quantity in KG
     * @param price     Order Price per KG
     * @param orderType Order Type (BUY/SELL)
     * @return
     */
    @Override
    public Order registerOrder(String user,
                               double quantity,
                               BigDecimal price,
                               OrderType orderType) {
        long orderId = sequenceId.getAndIncrement();

        Order order = new Order(orderId, user, quantity, price, orderType);
        OrderAudit orderAudit = new OrderAudit(orderId, order.getUser(), OffsetDateTime.now(ZoneOffset.UTC));
        order.addOrderAudit(orderAudit);

        orderIdToOrderMap.put(orderId, order);

        return order;
    }

    /**
     * Cancels the supplied Order.
     * Locking the whole method avoids the concurrent modification issues when multiple threads tries to read and cancel existing Order.
     * In a production environment this locking could be avoided by using transactions and other ways like SELECT FOR UPDATE.
     *
     * @param orderId OrderId to be cancelled
     * @param user    User initiating the order cancellation
     * @return Cancelled Order
     * @throws OrderBoardInvalidOperationException This Exception is thrown in two Scenarios
     *                                             1. If the supplied Order Id is not recognised
     *                                             2. If OrderId supplied is already cancelled by the user
     */
    @Override
    public Order cancelOrder(long orderId, String user) throws OrderBoardInvalidOperationException {
        lock.lock();
        try {
            Order orderFound = orderIdToOrderMap.get(orderId);
            if (orderFound != null) {
                if (orderFound.getActive() == 'Y') {
                    orderFound.markOrderInActive();

                    OrderAudit orderAudit = new OrderAudit(orderId, user, OffsetDateTime.now(ZoneOffset.UTC));
                    orderFound.addOrderAudit(orderAudit);
                    orderIdToOrderMap.put(orderId, orderFound);

                    return orderFound;
                } else {
                    Collection<OrderAudit> orderAudits = orderFound.getOrderAudits();
                    /*
                     * Domain objects and its associated audits can be managed in many ways.
                     * In this application it is known that the only change that can happen to the Order object is
                     * to move from Active = 'Y' to Active = 'N'. For this reason I have created a simple Order and OrderAudit object
                     * containing basic information of the order, the user initiating the request and the time of execution.
                     *
                     * In this statement "orderAudits.stream().skip(orderAudits.size() - 1).findFirst().get().getUser()"
                     * it is assumed that there are just two entries in the audit list and the last audit entry is extracted
                     * without checking if the get() method has returned a non-null entry or not.
                     * In prod env I would have designed audit differently but for this exercise this will suffice.
                     *
                     * In a production environment you can design audit in various ways.
                     * For ex - storing snapshot of the whole object information upon each request of change.
                     * Link all the audit for same order with parentId and store Active_From to Active_To timestamp
                     * where Active_To for the latest audit will be time in Infinity.
                     */
                    throw new OrderBoardInvalidOperationException(String.format("Order Id [%s] is already cancelled by user [%s]",
                            orderId, orderAudits.stream().skip(orderAudits.size() - 1).findFirst().get().getUser()));
                }
            } else {
                throw new OrderBoardInvalidOperationException(String.format("Unable to find Order Id [%s] in the system. " +
                        "Please supply the correct OrderId for cancellation", orderId));
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Returns the Order Summary of all Live Orders.
     * <p>
     * In a Prod Environment this method could select all live Order and aggregate them by price directly from the DB.
     * You can use select order status = 'Y' and group by price, order type and get sum of quantity
     *
     * @return OrderSummaryHolder containing BUY and SELL OrderSummary list
     */
    @Override
    public OrderSummaryHolder getLiveOrders() {
        Map<BigDecimal, OrderSummary> priceToBuyOrderSummaryMap = new HashMap<>();
        Map<BigDecimal, OrderSummary> priceToSellOrderSummaryMap = new HashMap<>();

        orderIdToOrderMap.forEach((orderId, order) -> {
            if (order.getActive() == 'Y') {
                if (order.getOrderType().equals(OrderType.BUY)) {
                    populatePriceToOrderSummaryMap(priceToBuyOrderSummaryMap, order);
                } else {
                    populatePriceToOrderSummaryMap(priceToSellOrderSummaryMap, order);
                }
            }
        });

        OrderSummaryHolder orderSummaryHolder = new OrderSummaryHolder();
        orderSummaryHolder.addBuyOrderSummaries(priceToBuyOrderSummaryMap.values().stream()
                .sorted(Comparator.comparing(OrderSummary::getPrice, Comparator.reverseOrder())).collect(Collectors.toList()));
        orderSummaryHolder.addSellOrderSummaries(priceToSellOrderSummaryMap.values().stream()
                .sorted(Comparator.comparing(OrderSummary::getPrice)).collect(Collectors.toList()));
        return orderSummaryHolder;
    }

    private void populatePriceToOrderSummaryMap(Map<BigDecimal, OrderSummary> priceToOrderSummaryMap, Order order) {
        OrderSummary orderSummaryFound = priceToOrderSummaryMap.get(order.getPrice());
        double orderQuantity = order.getQuantity();
        if (orderSummaryFound != null) {
            orderQuantity += orderSummaryFound.getQuantity();
        }
        priceToOrderSummaryMap.put(order.getPrice(), new OrderSummary(orderQuantity, order.getPrice(), order.getOrderType()));
    }
}