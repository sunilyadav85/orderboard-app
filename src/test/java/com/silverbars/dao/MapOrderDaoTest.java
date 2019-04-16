package com.silverbars.dao;

import com.silverbars.bean.Order;
import com.silverbars.bean.OrderAudit;
import com.silverbars.bean.OrderSummary;
import com.silverbars.bean.OrderSummaryHolder;
import com.silverbars.enums.OrderType;
import com.silverbars.exception.OrderBoardInvalidOperationException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MapOrderDaoTest {

    @Autowired
    private MapOrderDao classToTest;

    @Test
    public void shouldRegisterOrderSuccessfully() {
        // Given
        String user = "Test User";
        double quantity = 2.5;
        BigDecimal price = BigDecimal.TEN;
        OrderType orderType = OrderType.BUY;

        // When
        Order result = classToTest.registerOrder(user, quantity, price, orderType);

        // Then
        assertThat(result, is(notNullValue()));
        assertThat(result.getOrderId(), is(notNullValue()));
        assertThat(result.getUser(), equalTo(user));
        assertThat(result.getQuantity(), equalTo(quantity));
        assertThat(result.getPrice(), equalTo(price));
        assertThat(result.getOrderType(), equalTo(orderType));

        assertThat(result.getOrderAudits(), hasSize(1));
        OrderAudit orderAudit = result.getOrderAudits().stream().findFirst().get();
        assertThat(orderAudit.getUser(), equalTo(user));
        assertThat(orderAudit.getOrderId(), is(notNullValue()));
        assertThat(orderAudit.getAddTime(), is(notNullValue()));
    }

    @Test
    public void shouldCancelOrderSuccessfully() throws Exception {
        // Given
        String user = "Test User";
        double quantity = 2.5;
        BigDecimal price = BigDecimal.TEN;
        OrderType orderType = OrderType.BUY;
        Order existingOrder = classToTest.registerOrder(user, quantity, price, orderType);

        String cancelOrderUser = "Another Test User";

        // When
        Order result = classToTest.cancelOrder(existingOrder.getOrderId(), cancelOrderUser);


        // Then
        assertThat(result, is(notNullValue()));
        assertThat(result.getOrderId(), equalTo(existingOrder.getOrderId()));
        assertThat(result.getUser(), equalTo(existingOrder.getUser()));
        assertThat(result.getQuantity(), equalTo(quantity));
        assertThat(result.getPrice(), equalTo(price));
        assertThat(result.getOrderType(), equalTo(orderType));

        Collection<OrderAudit> orderAudits = result.getOrderAudits();
        assertThat(orderAudits, hasSize(2));
        OrderAudit orderAudit = orderAudits.stream().skip(orderAudits.size() - 1).findFirst().get();
        assertThat(orderAudit.getUser(), equalTo(cancelOrderUser));
        assertThat(orderAudit.getOrderId(), equalTo(existingOrder.getOrderId()));
        assertThat(orderAudit.getAddTime(), is(notNullValue()));
    }

    @Test
    public void shouldThrowExceptionWhenNoOrderFoundForCancellation() {
        // Given
        long invalidOrderId = 10;
        String cancelOrderUser = "Another Test User";

        // When
        try {
            classToTest.cancelOrder(invalidOrderId, cancelOrderUser);
            Assert.fail("Excepted exception to be thrown");
        } catch (OrderBoardInvalidOperationException e) {
            assertThat(e.getMessage(), equalTo("Unable to find Order Id [10] in the system. Please supply the correct OrderId for cancellation"));
        }

        // Then
    }

    @Test
    public void shouldThrowExceptionWhenCancelledOrderIsAttemptedForCancellationAgain() throws OrderBoardInvalidOperationException {
        // Given
        String user = "Test User";
        double quantity = 2.5;
        BigDecimal price = BigDecimal.TEN;
        OrderType orderType = OrderType.BUY;
        Order existingOrder = classToTest.registerOrder(user, quantity, price, orderType);

        String cancelOrderUser1 = "Cancel User 1";
        classToTest.cancelOrder(existingOrder.getOrderId(), cancelOrderUser1);

        String cancelOrderUser2 = "Cancel User 2";
        // When
        try {
            classToTest.cancelOrder(existingOrder.getOrderId(), cancelOrderUser2);
            Assert.fail("Excepted exception to be thrown");
        } catch (OrderBoardInvalidOperationException e) {
            assertThat(e.getMessage(), equalTo("Order Id [1] is already cancelled by user [Cancel User 1]"));
        }

        // Then
    }

    @Test
    public void shouldGetLiveOrdersSuccessfully() {
        // Given
        classToTest.registerOrder("BuyOrderUser1", 1.6, new BigDecimal("305"), OrderType.BUY);
        classToTest.registerOrder("BuyOrderUser2", 3.5, new BigDecimal("306"), OrderType.BUY);
        classToTest.registerOrder("BuyOrderUser3", 2.0, new BigDecimal("308"), OrderType.BUY);
        classToTest.registerOrder("BuyOrderUser4", 4.3, new BigDecimal("305"), OrderType.BUY);

        classToTest.registerOrder("SellOrderUser1", 3.5, new BigDecimal("306"), OrderType.SELL);
        classToTest.registerOrder("SellOrderUser2", 1.2, new BigDecimal("310"), OrderType.SELL);
        classToTest.registerOrder("SellOrderUser3", 1.5, new BigDecimal("307"), OrderType.SELL);
        classToTest.registerOrder("SellOrderUser4", 2.0, new BigDecimal("306"), OrderType.SELL);

        // When

        OrderSummaryHolder resultOrderSummaryHolder = classToTest.getLiveOrders();

        // Then
        List<OrderSummary> resultBuyOrderSummary = resultOrderSummaryHolder.getBuyOrderSummary();
        assertThat(resultBuyOrderSummary, hasSize(3));
        assertThat(resultBuyOrderSummary.get(0).getOrderType(), equalTo(OrderType.BUY));
        assertThat(resultBuyOrderSummary.get(0).getQuantity(), equalTo(2.0));
        assertThat(resultBuyOrderSummary.get(0).getPrice(), equalTo(new BigDecimal("308")));
        assertThat(resultBuyOrderSummary.get(1).getOrderType(), equalTo(OrderType.BUY));
        assertThat(resultBuyOrderSummary.get(1).getQuantity(), equalTo(3.5));
        assertThat(resultBuyOrderSummary.get(1).getPrice(), equalTo(new BigDecimal("306")));
        assertThat(resultBuyOrderSummary.get(2).getOrderType(), equalTo(OrderType.BUY));
        assertThat(resultBuyOrderSummary.get(2).getQuantity(), equalTo(5.9));
        assertThat(resultBuyOrderSummary.get(2).getPrice(), equalTo(new BigDecimal("305")));

        List<OrderSummary> resultSellOrderSummary = resultOrderSummaryHolder.getSellOrderSummary();
        assertThat(resultSellOrderSummary, hasSize(3));
        assertThat(resultSellOrderSummary.get(0).getOrderType(), equalTo(OrderType.SELL));
        assertThat(resultSellOrderSummary.get(0).getQuantity(), equalTo(5.5));
        assertThat(resultSellOrderSummary.get(0).getPrice(), equalTo(new BigDecimal("306")));
        assertThat(resultSellOrderSummary.get(1).getOrderType(), equalTo(OrderType.SELL));
        assertThat(resultSellOrderSummary.get(1).getQuantity(), equalTo(1.5));
        assertThat(resultSellOrderSummary.get(1).getPrice(), equalTo(new BigDecimal("307")));
        assertThat(resultSellOrderSummary.get(2).getOrderType(), equalTo(OrderType.SELL));
        assertThat(resultSellOrderSummary.get(2).getQuantity(), equalTo(1.2));
        assertThat(resultSellOrderSummary.get(2).getPrice(), equalTo(new BigDecimal("310")));
    }
}