package co.empresa.vivaeventos.orders.domain.model.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PromoCodeResponseDto {

    private UUID id;
    private String code;
    private String description;
    private String discountType;
    private BigDecimal discountValue;
    private BigDecimal minPurchase;
    private Integer maxUses;
    private Integer usesCount;
    private UUID eventId;
    private LocalDateTime startsAt;
    private LocalDateTime expiresAt;
    private Boolean isActive;
    private LocalDateTime createdAt;

}
