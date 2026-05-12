package co.empresa.vivaeventos.orders.domain.service;

import co.empresa.vivaeventos.orders.domain.model.PromoCode;
import co.empresa.vivaeventos.orders.domain.model.Dto.PromoCodeRequestDto;
import co.empresa.vivaeventos.orders.domain.model.Dto.PromoCodeResponseDto;
import co.empresa.vivaeventos.orders.domain.repository.IPromoCodeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PromoCodeServiceImpl implements IPromoCodeService {

    private final IPromoCodeRepository promoCodeRepository;

    @Override
    @Transactional
    public PromoCodeResponseDto createPromoCode(PromoCodeRequestDto request) {
        if (promoCodeRepository.existsByCode(request.getCode())) {
            throw new IllegalArgumentException("Promo code already exists: " + request.getCode());
        }

        PromoCode promoCode = new PromoCode();
        promoCode.setCode(request.getCode().toUpperCase());
        promoCode.setDescription(request.getDescription());
        promoCode.setDiscountType(request.getDiscountType().toUpperCase());
        promoCode.setDiscountValue(request.getDiscountValue());
        promoCode.setMaxUses(request.getMaxUses());
        promoCode.setUsesCount(0);
        promoCode.setMinPurchase(request.getMinPurchase());
        promoCode.setEventId(request.getEventId());
        promoCode.setStartsAt(request.getStartsAt());
        promoCode.setExpiresAt(request.getExpiresAt());
        promoCode.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);

        promoCode = promoCodeRepository.save(promoCode);
        return toResponseDto(promoCode);
    }

    @Override
    @Transactional(readOnly = true)
    public PromoCodeResponseDto getPromoCodeById(UUID id) {
        PromoCode promoCode = promoCodeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("PromoCode not found with id: " + id));
        return toResponseDto(promoCode);
    }

    @Override
    @Transactional(readOnly = true)
    public PromoCodeResponseDto getPromoCodeByCode(String code) {
        PromoCode promoCode = promoCodeRepository.findByCode(code.toUpperCase())
                .orElseThrow(() -> new EntityNotFoundException("PromoCode not found: " + code));
        return toResponseDto(promoCode);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PromoCodeResponseDto> getAllPromoCodes() {
        return promoCodeRepository.findAll()
                .stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PromoCodeResponseDto updatePromoCode(UUID id, PromoCodeRequestDto request) {
        PromoCode promoCode = promoCodeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("PromoCode not found with id: " + id));

        promoCode.setCode(request.getCode().toUpperCase());
        promoCode.setDescription(request.getDescription());
        promoCode.setDiscountType(request.getDiscountType().toUpperCase());
        promoCode.setDiscountValue(request.getDiscountValue());
        promoCode.setMaxUses(request.getMaxUses());
        promoCode.setMinPurchase(request.getMinPurchase());
        promoCode.setEventId(request.getEventId());
        promoCode.setStartsAt(request.getStartsAt());
        promoCode.setExpiresAt(request.getExpiresAt());
        promoCode.setIsActive(request.getIsActive() != null ? request.getIsActive() : promoCode.getIsActive());

        promoCode = promoCodeRepository.save(promoCode);
        return toResponseDto(promoCode);
    }

    @Override
    @Transactional
    public void deletePromoCode(UUID id) {
        if (!promoCodeRepository.existsById(id)) {
            throw new EntityNotFoundException("PromoCode not found with id: " + id);
        }
        promoCodeRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean validatePromoCode(String code) {
        return promoCodeRepository.findByCode(code.toUpperCase())
                .filter(PromoCode::getIsActive)
                .filter(p -> !LocalDateTime.now().isBefore(p.getStartsAt()))
                .filter(p -> !LocalDateTime.now().isAfter(p.getExpiresAt()))
                .filter(p -> p.getUsesCount() < p.getMaxUses())
                .isPresent();
    }

    private PromoCodeResponseDto toResponseDto(PromoCode promoCode) {
        PromoCodeResponseDto dto = new PromoCodeResponseDto();
        dto.setId(promoCode.getId());
        dto.setCode(promoCode.getCode());
        dto.setDescription(promoCode.getDescription());
        dto.setDiscountType(promoCode.getDiscountType());
        dto.setDiscountValue(promoCode.getDiscountValue());
        dto.setMaxUses(promoCode.getMaxUses());
        dto.setUsesCount(promoCode.getUsesCount());
        dto.setMinPurchase(promoCode.getMinPurchase());
        dto.setEventId(promoCode.getEventId());
        dto.setStartsAt(promoCode.getStartsAt());
        dto.setExpiresAt(promoCode.getExpiresAt());
        dto.setIsActive(promoCode.getIsActive());
        dto.setCreatedAt(promoCode.getCreatedAt());
        return dto;
    }

}
