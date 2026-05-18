package org.example.orderservice.service;

import org.example.orderservice.data.enums.OrderStatus;
import org.example.orderservice.dto.order.CreateOrderRequest;
import org.example.orderservice.dto.order.OrderDto;
import org.example.orderservice.dto.order.UpdateOrderFullRequest;
import org.example.orderservice.dto.order.UpdateOrderRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface OrderService {

    OrderDto createOrder(CreateOrderRequest request, String authorizationHeader);

    OrderDto getById(Long id);

    OrderDto patchOrder(Long id, UpdateOrderRequest request, String authorizationHeader);

    List<OrderDto> getAllOrders(OrderStatus status, String userId, LocalDateTime dateFrom, LocalDateTime dateTo, LocalDate deadlineDate);

    List<OrderDto> getUserOrders(String userId);

    void deleteOrder(Long id);

    OrderDto updateOrder(Long id, UpdateOrderFullRequest request, String authorizationHeader);

}