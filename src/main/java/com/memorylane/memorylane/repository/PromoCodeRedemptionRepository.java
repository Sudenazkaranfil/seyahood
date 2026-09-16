package com.memorylane.memorylane.repository;

import com.memorylane.memorylane.model.PromoCode;
import com.memorylane.memorylane.model.PromoCodeRedemption;
import com.memorylane.memorylane.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PromoCodeRedemptionRepository extends JpaRepository<PromoCodeRedemption, Long> {
    boolean existsByPromoCodeAndUser(PromoCode promoCode, User user);
}
