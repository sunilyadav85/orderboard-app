package com.silverbars.dao;

import com.silverbars.bean.Order;
import com.silverbars.bean.OrderSummaryHolder;
import com.silverbars.enums.OrderType;
import com.silverbars.exception.OrderBoardInvalidOperationException;

import java.math.BigDecimal;

/**
 * Dao for handling Order CRUD operations
 */
public interface OrderDao {

    Order registerOrder(String user,
                        double quantity,
                        BigDecimal price,
                        OrderType orderType);

    Order cancelOrder(long orderId, String user) throws OrderBoardInvalidOperationException;

    OrderSummaryHolder getLiveOrders();
}