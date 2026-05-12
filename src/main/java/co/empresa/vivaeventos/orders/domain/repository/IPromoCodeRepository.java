package co.empresa.vivaeventos.orders.domain.repository;

import co.empresa.vivaeventos.orders.domain.model.PromoCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface IPromoCodeRepository extends JpaRepository<PromoCode, UUID> {

    Optional<PromoCode> findByCode(String code);

    boolean existsByCode(String code);

}
