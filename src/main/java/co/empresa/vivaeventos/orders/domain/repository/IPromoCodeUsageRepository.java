package co.empresa.vivaeventos.orders.domain.repository;

import co.empresa.vivaeventos.orders.domain.model.PromoCodeUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface IPromoCodeUsageRepository extends JpaRepository<PromoCodeUsage, UUID> {

    long countByPromoCodeId(UUID promoCodeId);

}
