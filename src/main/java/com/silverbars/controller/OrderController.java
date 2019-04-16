package com.silverbars.controller;

import com.silverbars.bean.Order;
import com.silverbars.bean.OrderRequest;
import com.silverbars.bean.OrderSummaryHolder;
import com.silverbars.exception.OrderBoardInvalidOperationException;
import com.silverbars.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;

/**
 * Rest Controller to facilitate operations on Order object
 */
@RestController
public class OrderController {

    @Autowired
    private OrderService orderService;

    /*
     * In a prod env User information can be extracted from the security context
     * default Test User is used for demo but can be overridden if required
     */
    @PostMapping("/order")
    public ResponseEntity<Order> registerOrder(@RequestBody @Valid OrderRequest orderRequest, @RequestParam(defaultValue = "Test User") String user) {
        try {
            Order order = orderService.registerOrder(user, orderRequest.getQuantity(), orderRequest.getPrice(), orderRequest.getOrderType());
            return new ResponseEntity<>(order, HttpStatus.CREATED);
        } catch (Exception e) {
            String errorMessage = "Unable to register order";
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, errorMessage, e);
        }
    }

    @GetMapping("/order")
    public ResponseEntity<OrderSummaryHolder> getOrders() {
        try {
            OrderSummaryHolder orderSummaryHolder = orderService.getLiveOrders();
            if (CollectionUtils.isEmpty(orderSummaryHolder.getBuyOrderSummary()) && CollectionUtils.isEmpty(orderSummaryHolder.getSellOrderSummary())) {
                String errorMessage = "No Live Orders found in the system";
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, errorMessage);
            } else {
                return new ResponseEntity<>(orderSummaryHolder, HttpStatus.OK);
            }
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            String errorMessage = "Unable to retrieve orders from the system";
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, errorMessage);
        }
    }

    /*
     * In a prod env User information can be extracted from the security context
     * default Test User is used for demo but can be overridden if required
     */
    @DeleteMapping("/order/{orderId}")
    public ResponseEntity<Order> cancelOrder(@PathVariable long orderId, @RequestParam(defaultValue = "Test User") String user) {
        try {
            Order order = orderService.cancelOrder(orderId, user);
            return new ResponseEntity<>(order, HttpStatus.OK);
        } catch (OrderBoardInvalidOperationException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        } catch (Exception e) {
            String errorMessage = "Unable to cancel order";
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, errorMessage, e);
        }
    }
}