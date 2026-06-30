package com.razem.loyalty.loyaltyWallet.redeemingPoints;

import com.razem.loyalty.loyaltyWallet.LoyaltyPoints;
import com.razem.loyalty.membership.Tier;
import com.razem.loyalty.membership.TierProgram;

/**
 * Redeem-side computational policy: a tier may burn fewer points than redeemed
 * (e.g. Gold redeems 100 but only 95 leaves the balance). A pure function applied
 * before the aggregate, which receives the final amount.
 */
public class BenefitPolicy {

    public static LoyaltyPoints apply(LoyaltyPoints redeemed, Tier tier) {
        TierProgram program = TierProgram.forTier(tier);
        int burned = (int) Math.round(redeemed.value() * program.benefitRate());
        return LoyaltyPoints.of(burned);
    }
}
