package co.empresa.vivaeventos.orders.domain.model.Dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PromoCodeRequestDto {

    @NotBlank
    private String code;

    private String description;

    @NotBlank
    private String discountType;

    @NotNull
    private BigDecimal discountValue;

    private BigDecimal minPurchase;

    private Integer maxUses;

    private UUID eventId;

    @NotNull
    private LocalDateTime startsAt;

    @NotNull
    private LocalDateTime expiresAt;

    private Boolean isActive;

}
