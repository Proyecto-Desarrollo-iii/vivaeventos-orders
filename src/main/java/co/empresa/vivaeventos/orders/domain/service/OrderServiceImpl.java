package co.empresa.vivaeventos.orders.domain.service;

import co.empresa.vivaeventos.orders.domain.model.Order;
import co.empresa.vivaeventos.orders.domain.model.OrderItem;
import co.empresa.vivaeventos.orders.domain.model.PromoCode;
import co.empresa.vivaeventos.orders.domain.model.PromoCodeUsage;
import co.empresa.vivaeventos.orders.domain.model.Dto.OrderRequestDto;
import co.empresa.vivaeventos.orders.domain.model.Dto.OrderRequestDto.OrderItemRequest;
import co.empresa.vivaeventos.orders.domain.model.Dto.OrderResponseDto;
import co.empresa.vivaeventos.orders.domain.model.Dto.OrderResponseDto.OrderItemResponse;
import co.empresa.vivaeventos.orders.domain.repository.IOrderRepository;
import co.empresa.vivaeventos.orders.domain.repository.IPromoCodeRepository;
import co.empresa.vivaeventos.orders.domain.repository.IPromoCodeUsageRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements IOrderService {

    private final IOrderRepository orderRepository;
    private final IPromoCodeRepository promoCodeRepository;
    private final IPromoCodeUsageRepository promoCodeUsageRepository;

    @Override
    @Transactional
    public OrderResponseDto createOrder(OrderRequestDto request) {
        Order order = new Order();
        order.setUserId(request.getUserId());
        order.setEventId(request.getEventId());
        order.setStatus("PENDING");
        order.setTotal(BigDecimal.ZERO);
        order.setSubtotal(BigDecimal.ZERO);
        order.setDiscount(BigDecimal.ZERO);
        order.setItems(new ArrayList<>());
        order.setNotes(request.getNotes());
        order.setClientIp(request.getClientIp());

        BigDecimal total = BigDecimal.ZERO;

        for (OrderItemRequest itemReq : request.getItems()) {
            BigDecimal subtotal = itemReq.getUnitPrice()
                    .multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            total = total.add(subtotal);

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setEventId(itemReq.getEventId());
            item.setEventName(itemReq.getEventName());
            item.setTicketTypeId(itemReq.getTicketTypeId());
            item.setTicketTypeName(itemReq.getTicketTypeName());
            item.setQuantity(itemReq.getQuantity());
            item.setUnitPrice(itemReq.getUnitPrice());
            item.setSubtotal(subtotal);
            order.getItems().add(item);
        }

        order.setSubtotal(total);
        order.setTotal(total);

        if (request.getPromoCode() != null && !request.getPromoCode().isBlank()) {
            PromoCode promo = promoCodeRepository.findByCode(request.getPromoCode().toUpperCase())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid promo code: " + request.getPromoCode()));

            validatePromoCode(promo, total);
            BigDecimal discount = calculateDiscount(promo, total);
            order.setDiscount(discount);
            order.setPromoCode(promo.getCode());
            order.setPromoCodeId(promo.getId());

            promo.setUsesCount(promo.getUsesCount() + 1);
            promoCodeRepository.save(promo);

            PromoCodeUsage usage = new PromoCodeUsage();
            usage.setPromoCode(promo);
            usage.setOrderId(order.getId());
            usage.setUserId(request.getUserId());
            promoCodeUsageRepository.save(usage);
        }

        order.setTotal(order.getSubtotal().subtract(order.getDiscount()));
        order = orderRepository.save(order);
        return toResponseDto(order);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponseDto getOrderById(UUID id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + id));
        return toResponseDto(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDto> getOrdersByUserId(UUID userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDto> getAllOrders() {
        return orderRepository.findAll()
                .stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public OrderResponseDto updateOrderStatus(UUID id, String status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + id));
        order.setStatus(status.toUpperCase());
        order = orderRepository.save(order);
        return toResponseDto(order);
    }

    @Override
    @Transactional
    public void cancelOrder(UUID id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + id));
        order.setStatus("CANCELLED");
        orderRepository.save(order);
    }

    private void validatePromoCode(PromoCode promo, BigDecimal total) {
        if (!promo.getIsActive()) {
            throw new IllegalArgumentException("Promo code is not active");
        }
        if (LocalDateTime.now().isBefore(promo.getStartsAt()) ||
                LocalDateTime.now().isAfter(promo.getExpiresAt())) {
            throw new IllegalArgumentException("Promo code is not valid at this time");
        }
        if (promo.getUsesCount() >= promo.getMaxUses()) {
            throw new IllegalArgumentException("Promo code usage limit exceeded");
        }
        if (promo.getMinPurchase() != null && total.compareTo(promo.getMinPurchase()) < 0) {
            throw new IllegalArgumentException("Minimum purchase amount not met");
        }
    }

    private BigDecimal calculateDiscount(PromoCode promo, BigDecimal total) {
        if ("PERCENTAGE".equals(promo.getDiscountType())) {
            return total.multiply(promo.getDiscountValue())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }
        return promo.getDiscountValue().min(total);
    }

    private OrderResponseDto toResponseDto(Order order) {
        OrderResponseDto dto = new OrderResponseDto();
        dto.setId(order.getId());
        dto.setUserId(order.getUserId());
        dto.setEventId(order.getEventId());
        dto.setOrderDate(order.getCreatedAt());
        dto.setStatus(order.getStatus());
        dto.setTotal(order.getTotal());
        dto.setSubtotal(order.getSubtotal());
        dto.setDiscount(order.getDiscount());
        dto.setPromoCode(order.getPromoCode());
        dto.setPromoCodeId(order.getPromoCodeId());
        dto.setNotes(order.getNotes());
        dto.setClientIp(order.getClientIp());
        dto.setItems(order.getItems().stream().map(item -> {
            OrderItemResponse itemDto = new OrderItemResponse();
            itemDto.setId(item.getId());
            itemDto.setEventId(item.getEventId());
            itemDto.setEventName(item.getEventName());
            itemDto.setTicketTypeId(item.getTicketTypeId());
            itemDto.setTicketTypeName(item.getTicketTypeName());
            itemDto.setQuantity(item.getQuantity());
            itemDto.setUnitPrice(item.getUnitPrice());
            itemDto.setSubtotal(item.getSubtotal());
            return itemDto;
        }).collect(Collectors.toList()));
        dto.setCreatedAt(order.getCreatedAt());
        dto.setUpdatedAt(order.getUpdatedAt());
        dto.setPaidAt(order.getPaidAt());
        return dto;
    }

}
