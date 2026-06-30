package com.razem.loyalty.loyaltyWallet.redeemingPoints;

import com.razem.loyalty.loyaltyWallet.LoyaltyPoints;
import com.razem.loyalty.membership.Tier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.*;

class BenefitPolicyTest {

    @ParameterizedTest
    @CsvSource({
            "STANDARD, 100, 100",
            "SILVER, 100, 97",
            "GOLD, 100, 95",
            "PLATINUM, 100, 90"
    })
    void burnsTheTierRateOfRedeemedPoints(Tier tier, int redeemed, int expectedBurned) {
        LoyaltyPoints burned = BenefitPolicy.apply(LoyaltyPoints.of(redeemed), tier);

        assertThat(burned.value()).isEqualTo(expectedBurned);
    }

    @Test
    void roundsTheBurnedAmount() {
        // 99 * 0.95 = 94.05 -> rounds to 94
        LoyaltyPoints burned = BenefitPolicy.apply(LoyaltyPoints.of(99), Tier.GOLD);

        assertThat(burned.value()).isEqualTo(94);
    }

    @Test
    void neverBurnsMoreThanRedeemed() {
        LoyaltyPoints redeemed = LoyaltyPoints.of(250);

        for (Tier tier : Tier.values()) {
            LoyaltyPoints burned = BenefitPolicy.apply(redeemed, tier);
            assertThat(burned.value()).isLessThanOrEqualTo(redeemed.value());
        }
    }
}
