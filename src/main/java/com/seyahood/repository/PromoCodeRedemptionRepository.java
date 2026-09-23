package com.seyahood.repository;

import com.seyahood.model.PromoCode;
import com.seyahood.model.PromoCodeRedemption;
import com.seyahood.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PromoCodeRedemptionRepository extends JpaRepository<PromoCodeRedemption, Long> {
    boolean existsByPromoCodeAndUser(PromoCode promoCode, User user);
}
