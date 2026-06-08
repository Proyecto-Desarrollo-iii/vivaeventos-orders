package co.empresa.vivaeventos.orders.domain.service;

import co.empresa.vivaeventos.orders.domain.model.dto.PromoCodeRequestDto;
import co.empresa.vivaeventos.orders.domain.model.dto.PromoCodeResponseDto;

import java.util.List;
import java.util.UUID;

public interface IPromoCodeService {

    PromoCodeResponseDto createPromoCode(PromoCodeRequestDto request);

    PromoCodeResponseDto getPromoCodeById(UUID id);

    PromoCodeResponseDto getPromoCodeByCode(String code);

    List<PromoCodeResponseDto> getAllPromoCodes();

    PromoCodeResponseDto updatePromoCode(UUID id, PromoCodeRequestDto request);

    void deletePromoCode(UUID id);

    boolean validatePromoCode(String code);

}
