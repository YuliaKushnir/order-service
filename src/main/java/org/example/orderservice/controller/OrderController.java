package org.example.orderservice.controller;

import lombok.RequiredArgsConstructor;
import org.example.orderservice.data.enums.OrderStatus;
import org.example.orderservice.dto.item.CreateOrderItemRequest;
import org.example.orderservice.dto.item.UpdateOrderItemRequest;
import org.example.orderservice.dto.order.CreateOrderRequest;
import org.example.orderservice.dto.order.OrderDto;
import org.example.orderservice.dto.order.UpdateOrderFullRequest;
import org.example.orderservice.dto.order.UpdateOrderRequest;
import org.example.orderservice.service.OrderService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping(consumes = "multipart/form-data")
    public OrderDto createOrder(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader, @ModelAttribute CreateOrderRequest request) {
        return orderService.createOrder(request, authorizationHeader);
    }

    @GetMapping("/{id}")
    public OrderDto getById(@PathVariable Long id) {
        return orderService.getById(id);
    }

    @PatchMapping("/{id}")
    public OrderDto patchOrder(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,@PathVariable Long id, @RequestBody UpdateOrderRequest request) {
        return orderService.patchOrder(id, request, authorizationHeader);
    }

    @GetMapping
    public List<OrderDto> getAllOrders(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDate deadlineDate
    ) {
        return orderService.getAllOrders(status, userId, dateFrom, dateTo, deadlineDate);
    }

    @GetMapping("/user/{userId}")
    public List<OrderDto> getUserOrders(@PathVariable String userId) {
        return orderService.getUserOrders(userId);
    }

    @DeleteMapping("/{id}")
    public void deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public OrderDto updateOrder(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader, @PathVariable Long id, @ModelAttribute UpdateOrderFullRequest request) {
        return orderService.updateOrder(id, request, authorizationHeader);
    }



}