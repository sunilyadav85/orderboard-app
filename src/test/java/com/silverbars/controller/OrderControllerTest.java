package com.silverbars.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.silverbars.bean.Order;
import com.silverbars.bean.OrderRequest;
import com.silverbars.bean.OrderSummary;
import com.silverbars.bean.OrderSummaryHolder;
import com.silverbars.enums.OrderType;
import com.silverbars.exception.OrderBoardInvalidOperationException;
import com.silverbars.service.OrderServiceImpl;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


public class OrderControllerTest {

    private MockMvc mockMvc;

    @Mock
    private OrderServiceImpl orderService;

    @InjectMocks
    private OrderController classToTest;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders
                .standaloneSetup(classToTest)
                .build();
    }

    @Test
    public void shouldRegisterOrderSuccessfully() throws Exception {
        // Given
        Order order = new Order(1, "Test User", 8, BigDecimal.TEN, OrderType.BUY);
        when(orderService.registerOrder(order.getUser(), order.getQuantity(), order.getPrice(), order.getOrderType())).thenReturn(order);
        OrderRequest orderRequest = new OrderRequest(order.getUser(), order.getQuantity(), order.getPrice(), order.getOrderType());

        // When
        mockMvc.perform(
                post("/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId", equalTo(1)))
                .andExpect(jsonPath("$.user", equalTo("Test User")))
                .andExpect(jsonPath("$.quantity", equalTo(8.0)))
                .andExpect(jsonPath("$.price", equalTo(10)))
                .andExpect(jsonPath("$.orderType", equalTo("BUY")))
                .andExpect(jsonPath("$.active", equalTo("Y")));

        // Then
        verify(orderService).registerOrder(order.getUser(), order.getQuantity(), order.getPrice(), order.getOrderType());
        verifyNoMoreInteractions(orderService);
    }

    @Test
    public void shouldGetInternalServerErrorWhenRegisteringOrderFailed() throws Exception {
        // Given
        Order order = new Order(1, "Test User", 8, BigDecimal.TEN, OrderType.BUY);
        when(orderService.registerOrder(order.getUser(), order.getQuantity(), order.getPrice(), order.getOrderType()))
                .thenThrow(new IllegalStateException());
        OrderRequest orderRequest = new OrderRequest(order.getUser(), order.getQuantity(), order.getPrice(), order.getOrderType());

        // When
        mockMvc.perform(
                post("/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isInternalServerError());

        // Then
        verify(orderService).registerOrder(order.getUser(), order.getQuantity(), order.getPrice(), order.getOrderType());
        verifyNoMoreInteractions(orderService);
    }

    @Test
    public void shouldGetOrdersSuccessfully() throws Exception {
        // Given
        OrderSummaryHolder resultOrderSummaryHolder = new OrderSummaryHolder();
        OrderSummary buyOrderSummary1 = new OrderSummary(2.3, BigDecimal.TEN, OrderType.BUY);
        OrderSummary buyOrderSummary2 = new OrderSummary(4.2, BigDecimal.ONE, OrderType.BUY);
        resultOrderSummaryHolder.addBuyOrderSummaries(Lists.newArrayList(buyOrderSummary1, buyOrderSummary2));

        OrderSummary sellOrderSummary1 = new OrderSummary(3.4, BigDecimal.ONE, OrderType.SELL);
        OrderSummary sellOrderSummary2 = new OrderSummary(5.7, BigDecimal.TEN, OrderType.SELL);
        resultOrderSummaryHolder.addSellOrderSummaries(Lists.newArrayList(sellOrderSummary1, sellOrderSummary2));

        when(orderService.getLiveOrders()).thenReturn(resultOrderSummaryHolder);

        // When
        this.mockMvc.perform(get("/order"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.buyOrderSummary", hasSize(2)))
                .andExpect(jsonPath("$.buyOrderSummary[0].quantity", equalTo(buyOrderSummary1.getQuantity())))
                .andExpect(jsonPath("$.buyOrderSummary[0].price", equalTo(buyOrderSummary1.getPrice().intValue())))
                .andExpect(jsonPath("$.buyOrderSummary[0].orderType", equalTo(buyOrderSummary1.getOrderType().name())))
                .andExpect(jsonPath("$.buyOrderSummary[1].quantity", equalTo(buyOrderSummary2.getQuantity())))
                .andExpect(jsonPath("$.buyOrderSummary[1].price", equalTo(buyOrderSummary2.getPrice().intValue())))
                .andExpect(jsonPath("$.buyOrderSummary[1].orderType", equalTo(buyOrderSummary2.getOrderType().name())))

                .andExpect(jsonPath("$.sellOrderSummary", hasSize(2)))
                .andExpect(jsonPath("$.sellOrderSummary[0].quantity", equalTo(sellOrderSummary1.getQuantity())))
                .andExpect(jsonPath("$.sellOrderSummary[0].price", equalTo(sellOrderSummary1.getPrice().intValue())))
                .andExpect(jsonPath("$.sellOrderSummary[0].orderType", equalTo(sellOrderSummary1.getOrderType().name())))
                .andExpect(jsonPath("$.sellOrderSummary[1].quantity", equalTo(sellOrderSummary2.getQuantity())))
                .andExpect(jsonPath("$.sellOrderSummary[1].price", equalTo(sellOrderSummary2.getPrice().intValue())))
                .andExpect(jsonPath("$.sellOrderSummary[1].orderType", equalTo(sellOrderSummary2.getOrderType().name())));

        // Then
        verify(orderService).getLiveOrders();
        verifyNoMoreInteractions(orderService);
    }

    @Test
    public void shouldGetInternalServerErrorWhenGetOrdersFailed() throws Exception {
        // Given
        OrderSummaryHolder resultOrderSummaryHolder = new OrderSummaryHolder();
        OrderSummary buyOrderSummary1 = new OrderSummary(2.3, BigDecimal.TEN, OrderType.BUY);
        OrderSummary buyOrderSummary2 = new OrderSummary(4.2, BigDecimal.ONE, OrderType.BUY);
        resultOrderSummaryHolder.addBuyOrderSummaries(Lists.newArrayList(buyOrderSummary1, buyOrderSummary2));

        OrderSummary sellOrderSummary1 = new OrderSummary(3.4, BigDecimal.ONE, OrderType.SELL);
        OrderSummary sellOrderSummary2 = new OrderSummary(5.7, BigDecimal.TEN, OrderType.SELL);
        resultOrderSummaryHolder.addSellOrderSummaries(Lists.newArrayList(sellOrderSummary1, sellOrderSummary2));

        when(orderService.getLiveOrders()).thenThrow(new IllegalStateException());

        // When
        this.mockMvc.perform(get("/order"))
                .andExpect(status().isInternalServerError());

        // Then
        verify(orderService).getLiveOrders();
        verifyNoMoreInteractions(orderService);
    }

    @Test
    public void shouldCancelOrderSuccessfully() throws Exception {
        // Given
        Order order = new Order(1, "Test User", 8, BigDecimal.TEN, OrderType.BUY);
        order.markOrderInActive();
        when(orderService.cancelOrder(order.getOrderId(), order.getUser())).thenReturn(order);

        // When
        this.mockMvc.perform(
                delete("/order/{orderId}", order.getOrderId()))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId", equalTo(1)))
                .andExpect(jsonPath("$.user", equalTo("Test User")))
                .andExpect(jsonPath("$.quantity", equalTo(8.0)))
                .andExpect(jsonPath("$.price", equalTo(10)))
                .andExpect(jsonPath("$.orderType", equalTo("BUY")))
                .andExpect(jsonPath("$.active", equalTo("N")));

        // Then
        verify(orderService).cancelOrder(order.getOrderId(), order.getUser());
        verifyNoMoreInteractions(orderService);
    }

    @Test
    public void shouldGetNotFoundErrorWhenCancellingAlreadyCancelledOrder() throws Exception {
        // Given
        int orderId = 123;
        String user = "Test User";
        when(orderService.cancelOrder(orderId, user))
                .thenThrow(new OrderBoardInvalidOperationException("Order Id [123] is already cancelled by user [Another User]"));

        // When
        this.mockMvc.perform(
                delete("/order/{orderId}", orderId))
                .andExpect(status().isNotFound());

        // Then
        verify(orderService).cancelOrder(orderId, user);
        verifyNoMoreInteractions(orderService);
    }

    @Test
    public void shouldGetInternalServerErrorWhenCancellingOrderFailed() throws Exception {
        // Given
        Order order = new Order(1, "Test User", 8, BigDecimal.TEN, OrderType.BUY);
        order.markOrderInActive();
        when(orderService.cancelOrder(order.getOrderId(), order.getUser())).thenThrow(new IllegalStateException());

        // When
        this.mockMvc.perform(
                delete("/order/{orderId}", order.getOrderId()))
                .andExpect(status().isInternalServerError());

        // Then
        verify(orderService).cancelOrder(order.getOrderId(), order.getUser());
        verifyNoMoreInteractions(orderService);
    }
}