package com.razem.loyalty.loyaltyWallet.earningPoints;

import com.razem.loyalty.loyaltyWallet.LoyaltyPoints;
import com.razem.loyalty.loyaltyWallet.access.MemberId;
import com.razem.loyalty.membership.Tier;

import java.time.Instant;
import java.util.List;

/**
 * A recorded purchase: transaction facts only. Tier is a member attribute, resolved at the edge.
 */
public record Purchase(
        Money purchaseValue,
        MemberId buyer,
        Channel channel,
        Instant at,
        List<LoyaltyPoints> promotionBonuses,
        List<Beneficiary> beneficiaries
) {
}
