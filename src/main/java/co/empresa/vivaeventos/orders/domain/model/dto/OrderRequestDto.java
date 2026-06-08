package co.empresa.vivaeventos.orders.domain.model.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequestDto {

    @NotNull
    private UUID userId;

    @NotNull
    private UUID eventId;

    private String promoCode;

    private String holderName;

    private String holderEmail;

    private String holderDocument;

    @NotEmpty
    private List<OrderItemRequest> items;

    private String notes;

    private String clientIp;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemRequest {

        @NotNull
        private UUID eventId;

        private String eventName;

        @NotNull
        private UUID ticketTypeId;

        private String ticketTypeName;

        @NotNull
        private Integer quantity;

        @NotNull
        private BigDecimal unitPrice;

        private String holderName;

        private String holderEmail;

        private String holderDocument;
    }

}
