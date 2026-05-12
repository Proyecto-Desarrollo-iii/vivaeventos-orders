package co.empresa.vivaeventos.orders.domain.service;

import co.empresa.vivaeventos.orders.domain.model.Dto.OrderRequestDto;
import co.empresa.vivaeventos.orders.domain.model.Dto.OrderResponseDto;

import java.util.List;
import java.util.UUID;

public interface IOrderService {

    OrderResponseDto createOrder(OrderRequestDto request);

    OrderResponseDto getOrderById(UUID id);

    List<OrderResponseDto> getOrdersByUserId(UUID userId);

    List<OrderResponseDto> getAllOrders();

    OrderResponseDto updateOrderStatus(UUID id, String status);

    void cancelOrder(UUID id);

}
