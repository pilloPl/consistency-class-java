package com.razem.loyalty.membership;

import com.razem.loyalty.loyaltyWallet.RedemptionCadence;
import com.razem.loyalty.loyaltyWallet.RedemptionLimit;

public record TierProgram(
        double benefitRate,
        RedemptionCadence cadence,
        RedemptionLimit maxRedemptionCount
) {
    private static final TierProgram STANDARD = new TierProgram(1.0, RedemptionCadence.WEEKLY, RedemptionLimit.of(3));
    private static final TierProgram SILVER = new TierProgram(0.97, RedemptionCadence.WEEKLY, RedemptionLimit.of(5));
    private static final TierProgram GOLD = new TierProgram(0.95, RedemptionCadence.MONTHLY, RedemptionLimit.of(10));
    private static final TierProgram PLATINUM = new TierProgram(0.9, RedemptionCadence.MONTHLY, RedemptionLimit.of(20));

    public static TierProgram forTier(Tier tier) {
        return switch (tier) {
            case STANDARD -> STANDARD;
            case SILVER -> SILVER;
            case GOLD -> GOLD;
            case PLATINUM -> PLATINUM;
        };
    }
}
