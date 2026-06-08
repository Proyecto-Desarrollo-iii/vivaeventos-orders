package co.empresa.vivaeventos.orders.domain.model.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponseDto {

    private UUID id;
    private UUID userId;
    private UUID eventId;
    private LocalDateTime orderDate;
    private String status;
    private BigDecimal total;
    private BigDecimal subtotal;
    private BigDecimal discount;
    private String promoCode;
    private UUID promoCodeId;
    private String notes;
    private String clientIp;
    private List<OrderItemResponse> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime paidAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemResponse {
        private UUID id;
        private UUID eventId;
        private String eventName;
        private UUID ticketTypeId;
        private String ticketTypeName;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal subtotal;
        private String holderName;
        private String holderEmail;
        private String holderDocument;
    }

}
