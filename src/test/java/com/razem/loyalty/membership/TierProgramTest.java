package com.razem.loyalty.membership;

import com.razem.loyalty.loyaltyWallet.RedemptionCadence;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.*;

class TierProgramTest {

    @ParameterizedTest
    @CsvSource({
            "STANDARD, 1.0, WEEKLY, 3",
            "SILVER, 0.97, WEEKLY, 5",
            "GOLD, 0.95, MONTHLY, 10",
            "PLATINUM, 0.9, MONTHLY, 20"
    })
    void mapsToItsBenefitRateCadenceAndRedemptionLimit(
            Tier tier, double benefitRate, RedemptionCadence cadence, int maxRedemptionCount
    ) {
        TierProgram program = TierProgram.forTier(tier);

        assertThat(program.benefitRate()).isEqualTo(benefitRate);
        assertThat(program.cadence()).isEqualTo(cadence);
        assertThat(program.maxRedemptionCount().value()).isEqualTo(maxRedemptionCount);
    }
}
