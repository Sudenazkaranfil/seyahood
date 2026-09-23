package com.seyahood.service;

import com.seyahood.model.PromoCode;
import com.seyahood.model.PromoCodeRedemption;
import com.seyahood.model.User;
import com.seyahood.repository.PromoCodeRedemptionRepository;
import com.seyahood.repository.PromoCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PromoCodeService {
    private final PromoCodeRepository promoCodeRepository;
    private final PromoCodeRedemptionRepository redemptionRepository;
    private final UserService userService;

    public PromoCode createCode(String code, User.SubscriptionPlan plan, int durationDays,
                                 int maxRedemptions, LocalDateTime expiresAt, String note) {
        if (promoCodeRepository.findByCodeIgnoreCase(code).isPresent()) {
            throw new RuntimeException("CODE_ALREADY_EXISTS");
        }
        PromoCode promoCode = new PromoCode();
        promoCode.setCode(code.toUpperCase());
        promoCode.setPlan(plan);
        promoCode.setDurationDays(durationDays);
        promoCode.setMaxRedemptions(maxRedemptions);
        promoCode.setExpiresAt(expiresAt);
        promoCode.setNote(note);
        return promoCodeRepository.save(promoCode);
    }

    public List<PromoCode> listCodes() {
        return promoCodeRepository.findAll();
    }

    public void setActive(Long id, boolean active) {
        PromoCode promoCode = promoCodeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("CODE_NOT_FOUND"));
        promoCode.setActive(active);
        promoCodeRepository.save(promoCode);
    }

    @Transactional
    public User redeemCode(String username, String code) {
        PromoCode promoCode = promoCodeRepository.findByCodeIgnoreCase(code.trim())
                .orElseThrow(() -> new RuntimeException("CODE_NOT_FOUND"));

        if (!promoCode.isActive()) {
            throw new RuntimeException("CODE_INACTIVE");
        }
        if (promoCode.getExpiresAt() != null &&
                promoCode.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("CODE_EXPIRED");
        }
        if (promoCode.getTimesRedeemed() >= promoCode.getMaxRedemptions()) {
            throw new RuntimeException("CODE_LIMIT_REACHED");
        }

        User user = userService.getProfile(username);

        if (redemptionRepository.existsByPromoCodeAndUser(promoCode, user)) {
            throw new RuntimeException("CODE_ALREADY_USED");
        }

        user.setSubscriptionPlan(promoCode.getPlan());
        user.setSubscriptionExpiresAt(LocalDateTime.now().plusDays(promoCode.getDurationDays()));
        userService.save(user);

        promoCode.setTimesRedeemed(promoCode.getTimesRedeemed() + 1);
        promoCodeRepository.save(promoCode);

        PromoCodeRedemption redemption = new PromoCodeRedemption();
        redemption.setPromoCode(promoCode);
        redemption.setUser(user);
        redemptionRepository.save(redemption);

        return user;
    }
}
