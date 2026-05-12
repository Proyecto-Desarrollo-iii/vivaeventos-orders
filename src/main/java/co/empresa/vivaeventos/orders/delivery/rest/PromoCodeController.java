package co.empresa.vivaeventos.orders.delivery.rest;

import co.empresa.vivaeventos.orders.domain.model.Dto.PromoCodeRequestDto;
import co.empresa.vivaeventos.orders.domain.model.Dto.PromoCodeResponseDto;
import co.empresa.vivaeventos.orders.domain.service.IPromoCodeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/promocodes")
@RequiredArgsConstructor
public class PromoCodeController {

    private final IPromoCodeService promoCodeService;

    @PostMapping
    public ResponseEntity<PromoCodeResponseDto> createPromoCode(@Valid @RequestBody PromoCodeRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(promoCodeService.createPromoCode(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PromoCodeResponseDto> getPromoCodeById(@PathVariable UUID id) {
        return ResponseEntity.ok(promoCodeService.getPromoCodeById(id));
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<PromoCodeResponseDto> getPromoCodeByCode(@PathVariable String code) {
        return ResponseEntity.ok(promoCodeService.getPromoCodeByCode(code));
    }

    @GetMapping
    public ResponseEntity<List<PromoCodeResponseDto>> getAllPromoCodes() {
        return ResponseEntity.ok(promoCodeService.getAllPromoCodes());
    }

    @PutMapping("/{id}")
    public ResponseEntity<PromoCodeResponseDto> updatePromoCode(
            @PathVariable UUID id,
            @Valid @RequestBody PromoCodeRequestDto request) {
        return ResponseEntity.ok(promoCodeService.updatePromoCode(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePromoCode(@PathVariable UUID id) {
        promoCodeService.deletePromoCode(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/validate/{code}")
    public ResponseEntity<Boolean> validatePromoCode(@PathVariable String code) {
        return ResponseEntity.ok(promoCodeService.validatePromoCode(code));
    }

}
